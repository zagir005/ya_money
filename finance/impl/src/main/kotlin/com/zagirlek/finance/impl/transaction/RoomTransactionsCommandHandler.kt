package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.transaction.CreateTransaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.UpdateTransaction
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.PendingOperationType
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.category.CategoriesLocalDataSource
import com.zagirlek.finance.impl.local.category.CategoryEntity
import com.zagirlek.finance.impl.local.sync.PendingOperationEntity
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import com.zagirlek.finance.impl.local.transaction.TransactionEntity
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import java.math.BigDecimal
import java.time.Clock
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class RoomTransactionsCommandHandler(
    private val accountsLocalDataSource: AccountsLocalDataSource,
    private val categoriesLocalDataSource: CategoriesLocalDataSource,
    private val transactionsLocalDataSource: TransactionsLocalDataSource,
    private val syncLocalDataSource: SyncLocalDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
    private val clock: Clock,
    private val json: Json,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) : TransactionsCommandHandler {
    override suspend fun create(command: CreateTransaction): TransactionId {
        val transactionId = TransactionId(idGenerator())
        val nowMillis = clock.millis()

        transactionRunner.run {
            val account = requireAccount(command.accountId)
            val category = requireCategory(command.categoryId)
            require(account.currency == command.money.currency.value) {
                "Transaction and account currencies must match."
            }

            val transaction = TransactionEntity(
                clientId = transactionId.value,
                remoteId = null,
                accountClientId = account.clientId,
                categoryId = category.id,
                amount = command.money.amount.toPlainString(),
                currency = command.money.currency.value,
                transactionDateMillis = command.occurredAt.toEpochMilli(),
                comment = command.comment?.trim()?.ifBlank { null },
                createdAtMillis = nowMillis,
                updatedAtMillis = nowMillis,
                updatedAtLocalMillis = nowMillis,
                syncStatus = SyncStatus.PendingCreate,
            )

            accountsLocalDataSource.upsert(
                account.withBalance(
                    applyTransactionImpact(
                        balance = BigDecimal(account.balance),
                        amount = command.money.amount,
                        isIncome = category.isIncome,
                    ),
                    nowMillis = nowMillis,
                ),
            )
            transactionsLocalDataSource.upsert(transaction)
            syncLocalDataSource.upsert(
                transaction.toPendingOperation(
                    operationType = PendingOperationType.Create,
                    createdAtMillis = nowMillis,
                    dependsOnOperationId = accountCreateDependency(account.clientId),
                ),
            )
        }

        return transactionId
    }

    override suspend fun update(command: UpdateTransaction) {
        val nowMillis = clock.millis()

        transactionRunner.run {
            val previous = requireNotNull(transactionsLocalDataSource.getEntity(command.transactionId)) {
                "Transaction ${command.transactionId.value} was not found."
            }
            val previousAccount = requireAccount(AccountId(previous.accountClientId))
            val previousCategory = requireCategory(CategoryId(previous.categoryId))
            val updatedAccount = requireAccount(command.accountId)
            val updatedCategory = requireCategory(command.categoryId)
            require(updatedAccount.currency == command.money.currency.value) {
                "Transaction and account currencies must match."
            }

            val balances = linkedMapOf(
                previousAccount.clientId to BigDecimal(previousAccount.balance),
                updatedAccount.clientId to BigDecimal(updatedAccount.balance),
            )
            balances[previousAccount.clientId] = revertTransactionImpact(
                balance = requireNotNull(balances[previousAccount.clientId]),
                amount = BigDecimal(previous.amount),
                isIncome = previousCategory.isIncome,
            )
            balances[updatedAccount.clientId] = applyTransactionImpact(
                balance = requireNotNull(balances[updatedAccount.clientId]),
                amount = command.money.amount,
                isIncome = updatedCategory.isIncome,
            )

            val existingOperation = syncLocalDataSource.latestOperation(
                entityType = PendingEntityType.Transaction,
                entityClientId = previous.clientId,
            )
            val operationType = if (
                previous.syncStatus == SyncStatus.PendingCreate ||
                existingOperation?.operationType == PendingOperationType.Create
            ) {
                PendingOperationType.Create
            } else {
                PendingOperationType.Update
            }
            val transaction = previous.copy(
                accountClientId = updatedAccount.clientId,
                categoryId = updatedCategory.id,
                amount = command.money.amount.toPlainString(),
                currency = command.money.currency.value,
                transactionDateMillis = command.occurredAt.toEpochMilli(),
                comment = command.comment?.trim()?.ifBlank { null },
                updatedAtMillis = nowMillis,
                updatedAtLocalMillis = nowMillis,
                syncStatus = if (operationType == PendingOperationType.Create) {
                    SyncStatus.PendingCreate
                } else {
                    SyncStatus.PendingUpdate
                },
            )

            val affectedAccounts = listOf(previousAccount, updatedAccount)
                .distinctBy(AccountEntity::clientId)
                .map { account ->
                    account.withBalance(
                        balance = requireNotNull(balances[account.clientId]),
                        nowMillis = nowMillis,
                    )
                }
            accountsLocalDataSource.upsertAll(affectedAccounts)
            transactionsLocalDataSource.upsert(transaction)
            syncLocalDataSource.upsert(
                transaction.toPendingOperation(
                    operationType = operationType,
                    createdAtMillis = existingOperation?.createdAtMillis ?: nowMillis,
                    dependsOnOperationId = accountCreateDependency(updatedAccount.clientId),
                ),
            )
        }
    }

    private suspend fun requireAccount(accountId: AccountId): AccountEntity =
        requireNotNull(accountsLocalDataSource.getEntity(accountId)) {
            "Account ${accountId.value} was not found."
        }

    private suspend fun requireCategory(categoryId: CategoryId): CategoryEntity =
        requireNotNull(categoriesLocalDataSource.getEntity(categoryId)) {
            "Category ${categoryId.value} was not found."
        }

    private suspend fun accountCreateDependency(accountClientId: String): String? =
        syncLocalDataSource.latestOperation(
            entityType = PendingEntityType.Account,
            entityClientId = accountClientId,
        )?.takeIf { it.operationType == PendingOperationType.Create }?.id

    private fun TransactionEntity.toPendingOperation(
        operationType: PendingOperationType,
        createdAtMillis: Long,
        dependsOnOperationId: String?,
    ): PendingOperationEntity = PendingOperationEntity(
        id = operationId(clientId),
        entityType = PendingEntityType.Transaction,
        operationType = operationType,
        entityClientId = clientId,
        payloadJson = json.encodeToString(toPendingPayload()),
        dependsOnOperationId = dependsOnOperationId,
        status = PendingOperationStatus.Pending,
        attemptCount = 0,
        lastError = null,
        createdAtMillis = createdAtMillis,
        nextAttemptAtMillis = null,
    )

    private fun AccountEntity.withBalance(
        balance: BigDecimal,
        nowMillis: Long,
    ): AccountEntity = copy(
        balance = balance.toPlainString(),
        updatedAtLocalMillis = nowMillis,
    )

    private companion object {
        fun operationId(clientId: String): String = "transaction:$clientId"
    }
}
