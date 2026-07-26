package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.CreateAccount
import com.zagirlek.finance.api.account.UpdateAccount
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.PendingOperationType
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.sync.PendingOperationEntity
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import java.time.Clock
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class RoomAccountsCommandHandler(
    private val localDataSource: AccountsLocalDataSource,
    private val syncLocalDataSource: SyncLocalDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
    private val clock: Clock,
    private val json: Json,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) : AccountsCommandHandler {
    override suspend fun create(command: CreateAccount): AccountId {
        val accountId = AccountId(idGenerator())
        val nowMillis = clock.millis()
        val account = AccountEntity(
            clientId = accountId.value,
            remoteId = null,
            name = command.name.trim(),
            emoji = command.emoji.trim(),
            balance = command.initialBalance.amount.toPlainString(),
            currency = command.initialBalance.currency.value,
            createdAtMillis = nowMillis,
            updatedAtMillis = nowMillis,
            updatedAtLocalMillis = nowMillis,
            syncStatus = SyncStatus.PendingCreate,
        )

        transactionRunner.run {
            localDataSource.upsert(account)
            syncLocalDataSource.upsert(
                account.toPendingOperation(
                    operationType = PendingOperationType.Create,
                    createdAtMillis = nowMillis,
                ),
            )
        }
        return accountId
    }

    override suspend fun update(command: UpdateAccount) {
        mutate(command.accountId) { account, nowMillis ->
            account.copy(
                name = command.name.trim(),
                emoji = command.emoji.trim(),
                balance = command.balance.amount.toPlainString(),
                currency = command.balance.currency.value,
                updatedAtMillis = nowMillis,
            )
        }
    }

    private suspend fun mutate(
        accountId: AccountId,
        transform: (AccountEntity, Long) -> AccountEntity,
    ) {
        val nowMillis = clock.millis()
        transactionRunner.run {
            val previous = requireNotNull(localDataSource.getEntity(accountId)) {
                "Account ${accountId.value} was not found."
            }
            val existingOperation = syncLocalDataSource.latestOperation(
                entityType = PendingEntityType.Account,
                entityClientId = previous.clientId,
            )
            val operationType = resolveAccountOperationType(
                syncStatus = previous.syncStatus,
                existingOperationType = existingOperation?.operationType,
            )
            val account = transform(previous, nowMillis).copy(
                updatedAtLocalMillis = nowMillis,
                syncStatus = if (operationType == PendingOperationType.Create) {
                    SyncStatus.PendingCreate
                } else {
                    SyncStatus.PendingUpdate
                },
            )

            localDataSource.upsert(account)
            syncLocalDataSource.upsert(
                account.toPendingOperation(
                    operationType = operationType,
                    createdAtMillis = existingOperation?.createdAtMillis ?: nowMillis,
                ),
            )
        }
    }

    private fun AccountEntity.toPendingOperation(
        operationType: PendingOperationType,
        createdAtMillis: Long,
    ): PendingOperationEntity = PendingOperationEntity(
        id = operationId(clientId),
        entityType = PendingEntityType.Account,
        operationType = operationType,
        entityClientId = clientId,
        payloadJson = json.encodeToString(toPendingPayload()),
        dependsOnOperationId = null,
        status = PendingOperationStatus.Pending,
        attemptCount = 0,
        lastError = null,
        createdAtMillis = createdAtMillis,
        nextAttemptAtMillis = null,
    )

    private companion object {
        fun operationId(clientId: String): String = "account:$clientId"
    }
}

internal fun resolveAccountOperationType(
    syncStatus: SyncStatus,
    existingOperationType: PendingOperationType?,
): PendingOperationType =
    if (
        syncStatus == SyncStatus.PendingCreate ||
        existingOperationType == PendingOperationType.Create
    ) {
        PendingOperationType.Create
    } else {
        PendingOperationType.Update
    }
