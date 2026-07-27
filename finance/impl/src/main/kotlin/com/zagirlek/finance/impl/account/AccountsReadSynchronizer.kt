package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.impl.account.remote.AccountsRemoteDataSource
import com.zagirlek.finance.impl.account.remote.mergeIntoLocal
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import com.zagirlek.finance.impl.sync.retryServerFailures
import java.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AccountsReadSynchronizer(
    private val localDataSource: AccountsLocalDataSource,
    private val transactionsLocalDataSource: TransactionsLocalDataSource,
    private val remoteDataSource: AccountsRemoteDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
    private val clock: Clock,
) {
    private val refreshMutex = Mutex()

    suspend fun refresh() = refreshMutex.withLock {
        val remoteAccounts = retryServerFailures {
            remoteDataSource.getAccounts()
        }
        val syncedAtMillis = clock.millis()

        transactionRunner.run {
            remoteAccounts.forEach { remoteAccount ->
                val existing = localDataSource.getEntity(remoteAccount.id.toLong())
                val merged = remoteAccount.mergeIntoLocal(
                    existing = existing,
                    syncedAtMillis = syncedAtMillis,
                )
                localDataSource.upsert(
                    if (
                        existing != null &&
                        transactionsLocalDataSource.hasPendingForAccount(AccountId(existing.clientId))
                    ) {
                        merged.copy(balance = existing.balance)
                    } else {
                        merged
                    },
                )
            }
        }
    }
}
