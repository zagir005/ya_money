package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.account.AccountsReadSynchronizer
import com.zagirlek.finance.impl.category.CategoriesReadSynchronizer
import com.zagirlek.finance.impl.category.remote.toEntity
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.category.CategoriesLocalDataSource
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import com.zagirlek.finance.impl.transaction.remote.TransactionResponseDto
import com.zagirlek.finance.impl.transaction.remote.TransactionsRemoteDataSource
import com.zagirlek.finance.impl.transaction.remote.mergeIntoLocal
import java.time.Clock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class TransactionsReadSynchronizer(
    private val accountsSynchronizer: AccountsReadSynchronizer,
    private val categoriesSynchronizer: CategoriesReadSynchronizer,
    private val accountsLocalDataSource: AccountsLocalDataSource,
    private val categoriesLocalDataSource: CategoriesLocalDataSource,
    private val transactionsLocalDataSource: TransactionsLocalDataSource,
    private val syncLocalDataSource: SyncLocalDataSource,
    private val remoteDataSource: TransactionsRemoteDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
    private val clock: Clock,
) {
    private val refreshMutex = Mutex()

    suspend fun refresh(period: TransactionPeriod) = refreshMutex.withLock {
        accountsSynchronizer.refresh()
        categoriesSynchronizer.refresh()

        val accounts = accountsLocalDataSource.getRemoteBackedEntities()
        val remoteTransactions = loadForAccounts(accounts, period)
        val syncedAt = clock.instant()

        transactionRunner.run {
            remoteTransactions.forEach { accountResult ->
                accountResult.transactions.forEach { remoteTransaction ->
                    categoriesLocalDataSource.upsertEntities(
                        categories = listOf(remoteTransaction.category.toEntity()),
                    )
                    val referencedAccount = accountsLocalDataSource.getEntity(
                        remoteId = remoteTransaction.account.id.toLong(),
                    ) ?: throw FinanceNetworkException.InvalidResponse(
                        IllegalStateException(
                            "Transaction ${remoteTransaction.id} references unknown account " +
                                remoteTransaction.account.id,
                        ),
                    )
                    val existing = transactionsLocalDataSource.getEntity(
                        remoteId = remoteTransaction.id.toLong(),
                    )
                    transactionsLocalDataSource.upsert(
                        remoteTransaction.mergeIntoLocal(
                            accountClientId = referencedAccount.clientId,
                            existing = existing,
                            syncedAtMillis = syncedAt.toEpochMilli(),
                        ),
                    )
                }

                syncLocalDataSource.markSynced(
                    accountId = AccountId(accountResult.account.clientId),
                    period = period,
                    syncedAt = syncedAt,
                )
            }
        }
    }

    private suspend fun loadForAccounts(
        accounts: List<AccountEntity>,
        period: TransactionPeriod,
    ): List<AccountTransactions> = coroutineScope {
        accounts.map { account ->
            async {
                AccountTransactions(
                    account = account,
                    transactions = remoteDataSource.getTransactions(
                        accountRemoteId = requireNotNull(account.remoteId),
                        period = period,
                    ),
                )
            }
        }.awaitAll()
    }
}

private data class AccountTransactions(
    val account: AccountEntity,
    val transactions: List<TransactionResponseDto>,
)
