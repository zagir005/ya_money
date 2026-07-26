package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.account.PendingAccountPayload
import com.zagirlek.finance.impl.account.remote.AccountCreateRequestDto
import com.zagirlek.finance.impl.account.remote.AccountDto
import com.zagirlek.finance.impl.account.remote.AccountUpdateRequestDto
import com.zagirlek.finance.impl.account.remote.AccountsRemoteDataSource
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.PendingOperationType
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.category.CategoriesLocalDataSource
import com.zagirlek.finance.impl.local.sync.PendingOperationEntity
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import com.zagirlek.finance.impl.transaction.PendingTransactionPayload
import com.zagirlek.finance.impl.transaction.revertTransactionImpact
import com.zagirlek.finance.impl.transaction.remote.TransactionRequestDto
import com.zagirlek.finance.impl.transaction.remote.TransactionWriteResult
import com.zagirlek.finance.impl.transaction.remote.TransactionsRemoteDataSource
import java.math.BigDecimal
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class OutboxDelivery(
    private val accountsLocalDataSource: AccountsLocalDataSource,
    private val transactionsLocalDataSource: TransactionsLocalDataSource,
    private val categoriesLocalDataSource: CategoriesLocalDataSource,
    private val syncLocalDataSource: SyncLocalDataSource,
    private val accountsRemoteDataSource: AccountsRemoteDataSource,
    private val transactionsRemoteDataSource: TransactionsRemoteDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
    private val clock: Clock,
    private val json: Json,
    private val retryDelay: suspend (Long) -> Unit = { delay(it) },
) {
    suspend fun reconcileUnknownResults() {
        reconcileUnknownAccounts()
        reconcileUnknownTransactions()
    }

    suspend fun deliver(): OutboxDeliveryResult {
        var needsRetry = false

        syncLocalDataSource.readyOperations(clock.instant())
            .filter { operation -> operation.entityType == PendingEntityType.Account }
            .forEach { operation ->
                needsRetry = deliverWithPolicy(operation, ::sendAccount) || needsRetry
            }

        syncLocalDataSource.readyOperations(clock.instant())
            .filter { operation -> operation.entityType == PendingEntityType.Transaction }
            .forEach { operation ->
                val dependency = operation.dependsOnOperationId
                    ?.let { operationId -> syncLocalDataSource.operation(operationId) }
                if (dependency == null) {
                    needsRetry = deliverWithPolicy(operation, ::sendTransaction) || needsRetry
                }
            }

        return OutboxDeliveryResult(needsRetry = needsRetry)
    }

    private suspend fun reconcileUnknownAccounts() {
        val operations = syncLocalDataSource
            .operations(PendingOperationStatus.UnknownResult)
            .filter { operation -> operation.entityType == PendingEntityType.Account }
        if (operations.isEmpty()) return

        val remoteAccounts = accountsRemoteDataSource.getAccounts()
        operations.forEach { operation ->
            val payload = json.decodeFromString<PendingAccountPayload>(operation.payloadJson)
            val submittedBalance = accountBaseBalance(payload)
            val matches = remoteAccounts.filter { remote ->
                remote.name == payload.name &&
                    remote.emoji == payload.emoji &&
                    runCatching { BigDecimal(remote.balance) == submittedBalance }
                        .getOrDefault(false) &&
                    remote.currency == payload.currency &&
                    isCloseToLocalCreation(remote.createdAt, payload.createdAtMillis)
            }
            if (matches.size == 1) {
                completeAccount(operation, matches.single())
            }
        }
    }

    private suspend fun reconcileUnknownTransactions() {
        val operations = syncLocalDataSource
            .operations(PendingOperationStatus.UnknownResult)
            .filter { operation -> operation.entityType == PendingEntityType.Transaction }
        operations.forEach { operation ->
            val payload = json.decodeFromString<PendingTransactionPayload>(operation.payloadJson)
            val account = accountsLocalDataSource.getEntity(
                com.zagirlek.finance.api.account.AccountId(payload.accountClientId),
            ) ?: return@forEach
            val accountRemoteId = account.remoteId ?: return@forEach
            val transactionDate = Instant.ofEpochMilli(payload.transactionDateMillis)
            val localDate = transactionDate.atZone(ZoneId.systemDefault()).toLocalDate()
            val matches = transactionsRemoteDataSource.getTransactions(
                accountRemoteId = accountRemoteId,
                period = TransactionPeriod(localDate, localDate),
            ).filter { remote ->
                remote.account.id.toLong() == accountRemoteId &&
                    remote.category.id == payload.categoryId &&
                    runCatching { BigDecimal(remote.amount) == BigDecimal(payload.amount) }
                        .getOrDefault(false) &&
                    runCatching { Instant.parse(remote.transactionDate) == transactionDate }
                        .getOrDefault(false) &&
                    remote.comment == payload.comment &&
                    isCloseToLocalCreation(remote.createdAt, payload.createdAtMillis)
            }
            if (matches.size == 1) {
                val match = matches.single()
                completeTransaction(
                    sentOperation = operation,
                    sentPayload = payload,
                    response = TransactionWriteResult(
                        remoteId = match.id.toLong(),
                        createdAt = match.createdAt,
                        updatedAt = match.updatedAt,
                    ),
                )
            }
        }
    }

    private suspend fun deliverWithPolicy(
        initialOperation: PendingOperationEntity,
        sender: suspend (PendingOperationEntity) -> Unit,
    ): Boolean {
        var operation = initialOperation

        while (true) {
            try {
                sender(operation)
                return false
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (!isStillCurrent(operation)) return true

                when (
                    val action = classifySyncFailure(
                        operationType = operation.operationType,
                        completedAttempts = operation.attemptCount,
                        error = error,
                    )
                ) {
                    is SyncFailureAction.RetryServerFailure -> {
                        markFailure(
                            operation = operation,
                            status = PendingOperationStatus.Pending,
                            error = error,
                            nextAttemptAt = clock.instant().plusMillis(action.delayMillis),
                        )
                        retryDelay(action.delayMillis)
                        val latest = syncLocalDataSource.operation(operation.id)
                            ?: return false
                        if (latest.payloadJson != operation.payloadJson) return true
                        operation = latest
                    }
                    is SyncFailureAction.RetryLater -> {
                        markFailure(
                            operation = operation,
                            status = PendingOperationStatus.Pending,
                            error = error,
                            nextAttemptAt = clock.instant().plusMillis(action.delayMillis),
                        )
                        return true
                    }
                    SyncFailureAction.MarkFailed -> {
                        markFailure(
                            operation = operation,
                            status = PendingOperationStatus.Failed,
                            error = error,
                            nextAttemptAt = null,
                        )
                        return false
                    }
                    SyncFailureAction.MarkUnknownResult -> {
                        markFailure(
                            operation = operation,
                            status = PendingOperationStatus.UnknownResult,
                            error = error,
                            nextAttemptAt = null,
                        )
                        return false
                    }
                }
            }
        }
    }

    private suspend fun sendAccount(operation: PendingOperationEntity) {
        val payload = json.decodeFromString<PendingAccountPayload>(operation.payloadJson)
        val request = AccountUpdateRequestDto(
            name = payload.name,
            emoji = payload.emoji,
            balance = accountBaseBalance(payload).toPlainString(),
            currency = payload.currency,
        )
        val response = when (operation.operationType) {
            PendingOperationType.Create -> {
                val existingRemoteId = accountsLocalDataSource
                    .getEntity(com.zagirlek.finance.api.account.AccountId(payload.clientId))
                    ?.remoteId
                if (existingRemoteId == null) {
                    accountsRemoteDataSource.createAccount(
                        AccountCreateRequestDto(
                            name = request.name,
                            emoji = request.emoji,
                            balance = request.balance,
                            currency = request.currency,
                        ),
                    )
                } else {
                    accountsRemoteDataSource.updateAccount(existingRemoteId, request)
                }
            }
            PendingOperationType.Update -> accountsRemoteDataSource.updateAccount(
                remoteId = payload.remoteId
                    ?: requireNotNull(
                        accountsLocalDataSource
                            .getEntity(com.zagirlek.finance.api.account.AccountId(payload.clientId))
                            ?.remoteId,
                    ) { "Account ${payload.clientId} has no remote ID." },
                request = request,
            )
        }
        completeAccount(operation, response)
    }

    private suspend fun accountBaseBalance(payload: PendingAccountPayload): BigDecimal {
        val pendingTransactions = transactionsLocalDataSource.getPendingEntities(
            com.zagirlek.finance.api.account.AccountId(payload.clientId),
        )
        val impacts = pendingTransactions.map { transaction ->
            val category = requireNotNull(
                categoriesLocalDataSource.getEntity(
                    com.zagirlek.finance.api.category.CategoryId(transaction.categoryId),
                ),
            ) { "Category ${transaction.categoryId} was not found." }
            PendingBalanceImpact(
                amount = BigDecimal(transaction.amount),
                isIncome = category.isIncome,
            )
        }
        return calculateRemoteAccountBalance(
            displayedBalance = BigDecimal(payload.balance),
            pendingImpacts = impacts,
        )
    }

    private suspend fun sendTransaction(operation: PendingOperationEntity) {
        val payload = json.decodeFromString<PendingTransactionPayload>(operation.payloadJson)
        val account = requireNotNull(
            accountsLocalDataSource.getEntity(
                com.zagirlek.finance.api.account.AccountId(payload.accountClientId),
            ),
        ) { "Account ${payload.accountClientId} was not found." }
        val remoteAccountId = requireNotNull(account.remoteId) {
            "Account ${payload.accountClientId} has no remote ID."
        }
        require(account.currency == payload.currency) {
            "Transaction and account currencies must match."
        }
        val request = TransactionRequestDto(
            accountId = remoteAccountId,
            categoryId = payload.categoryId,
            amount = payload.amount,
            transactionDate = Instant.ofEpochMilli(payload.transactionDateMillis).toString(),
            comment = payload.comment,
        )
        val response = when (operation.operationType) {
            PendingOperationType.Create -> {
                val existingRemoteId = transactionsLocalDataSource
                    .getEntity(com.zagirlek.finance.api.transaction.TransactionId(payload.clientId))
                    ?.remoteId
                if (existingRemoteId == null) {
                    transactionsRemoteDataSource.createTransaction(request)
                } else {
                    transactionsRemoteDataSource.updateTransaction(existingRemoteId, request)
                }
            }
            PendingOperationType.Update -> transactionsRemoteDataSource.updateTransaction(
                remoteId = payload.remoteId
                    ?: requireNotNull(
                        transactionsLocalDataSource
                            .getEntity(
                                com.zagirlek.finance.api.transaction.TransactionId(payload.clientId),
                            )
                            ?.remoteId,
                    ) { "Transaction ${payload.clientId} has no remote ID." },
                request = request,
            )
        }
        completeTransaction(operation, payload, response)
    }

    private suspend fun completeAccount(
        sentOperation: PendingOperationEntity,
        response: AccountDto,
    ) {
        val syncedAtMillis = clock.millis()
        transactionRunner.run {
            val currentOperation = syncLocalDataSource.operation(sentOperation.id)
                ?: return@run
            val currentEntity = requireNotNull(
                accountsLocalDataSource.getEntity(
                    com.zagirlek.finance.api.account.AccountId(sentOperation.entityClientId),
                ),
            )

            if (currentOperation.payloadJson == sentOperation.payloadJson) {
                val syncedEntity = response.toSyncedEntity(
                    clientId = currentEntity.clientId,
                    syncedAtMillis = syncedAtMillis,
                )
                accountsLocalDataSource.upsert(
                    if (
                        transactionsLocalDataSource.hasPendingForAccount(
                            com.zagirlek.finance.api.account.AccountId(currentEntity.clientId),
                        )
                    ) {
                        syncedEntity.copy(balance = currentEntity.balance)
                    } else {
                        syncedEntity
                    },
                )
                syncLocalDataSource.remove(currentOperation.id)
            } else {
                val currentPayload =
                    json.decodeFromString<PendingAccountPayload>(currentOperation.payloadJson)
                accountsLocalDataSource.upsert(
                    currentEntity.copy(
                        remoteId = response.id.toLong(),
                        updatedAtLocalMillis = syncedAtMillis,
                        syncStatus = SyncStatus.PendingUpdate,
                    ),
                )
                syncLocalDataSource.upsert(
                    currentOperation.copy(
                        operationType = PendingOperationType.Update,
                        payloadJson = json.encodeToString(
                            currentPayload.copy(remoteId = response.id.toLong()),
                        ),
                        status = PendingOperationStatus.Pending,
                        attemptCount = 0,
                        lastError = null,
                        nextAttemptAtMillis = null,
                    ),
                )
            }
        }
    }

    private suspend fun completeTransaction(
        sentOperation: PendingOperationEntity,
        sentPayload: PendingTransactionPayload,
        response: TransactionWriteResult,
    ) {
        val syncedAtMillis = clock.millis()
        transactionRunner.run {
            val currentOperation = syncLocalDataSource.operation(sentOperation.id)
                ?: return@run
            val currentEntity = requireNotNull(
                transactionsLocalDataSource.getEntity(
                    com.zagirlek.finance.api.transaction.TransactionId(
                        sentOperation.entityClientId,
                    ),
                ),
            )

            if (currentOperation.payloadJson == sentOperation.payloadJson) {
                transactionsLocalDataSource.upsert(
                    currentEntity.copy(
                        remoteId = response.remoteId,
                        accountClientId = sentPayload.accountClientId,
                        categoryId = sentPayload.categoryId,
                        amount = sentPayload.amount,
                        currency = sentPayload.currency,
                        transactionDateMillis = sentPayload.transactionDateMillis,
                        comment = sentPayload.comment,
                        createdAtMillis = Instant.parse(response.createdAt).toEpochMilli(),
                        updatedAtMillis = Instant.parse(response.updatedAt).toEpochMilli(),
                        updatedAtLocalMillis = syncedAtMillis,
                        syncStatus = SyncStatus.Synced,
                    ),
                )
                syncLocalDataSource.remove(currentOperation.id)
            } else {
                val currentPayload =
                    json.decodeFromString<PendingTransactionPayload>(currentOperation.payloadJson)
                transactionsLocalDataSource.upsert(
                    currentEntity.copy(
                        remoteId = response.remoteId,
                        updatedAtLocalMillis = syncedAtMillis,
                        syncStatus = SyncStatus.PendingUpdate,
                    ),
                )
                syncLocalDataSource.upsert(
                    currentOperation.copy(
                        operationType = PendingOperationType.Update,
                        payloadJson = json.encodeToString(
                            currentPayload.copy(remoteId = response.remoteId),
                        ),
                        status = PendingOperationStatus.Pending,
                        attemptCount = 0,
                        lastError = null,
                        nextAttemptAtMillis = null,
                    ),
                )
            }
        }
    }

    private suspend fun markFailure(
        operation: PendingOperationEntity,
        status: PendingOperationStatus,
        error: Exception,
        nextAttemptAt: Instant?,
    ) {
        transactionRunner.run {
            val current = syncLocalDataSource.operation(operation.id)
            if (current?.payloadJson != operation.payloadJson) return@run

            syncLocalDataSource.recordFailure(
                operationId = operation.id,
                status = status,
                lastError = error.message,
                nextAttemptAt = nextAttemptAt,
            )
            val syncStatus = when (status) {
                PendingOperationStatus.Failed -> SyncStatus.Failed
                PendingOperationStatus.UnknownResult -> SyncStatus.UnknownResult
                PendingOperationStatus.Pending -> null
            }
            if (syncStatus != null) {
                when (operation.entityType) {
                    PendingEntityType.Account -> accountsLocalDataSource
                        .getEntity(
                            com.zagirlek.finance.api.account.AccountId(
                                operation.entityClientId,
                            ),
                        )
                        ?.let { entity ->
                            accountsLocalDataSource.upsert(
                                entity.copy(
                                    updatedAtLocalMillis = clock.millis(),
                                    syncStatus = syncStatus,
                                ),
                            )
                        }
                    PendingEntityType.Transaction -> transactionsLocalDataSource
                        .getEntity(
                            com.zagirlek.finance.api.transaction.TransactionId(
                                operation.entityClientId,
                            ),
                        )
                        ?.let { entity ->
                            transactionsLocalDataSource.upsert(
                                entity.copy(
                                    updatedAtLocalMillis = clock.millis(),
                                    syncStatus = syncStatus,
                                ),
                            )
                        }
                }
            }
        }
    }

    private suspend fun isStillCurrent(operation: PendingOperationEntity): Boolean =
        syncLocalDataSource.operation(operation.id)?.payloadJson == operation.payloadJson
}

internal data class OutboxDeliveryResult(
    val needsRetry: Boolean,
)

internal data class PendingBalanceImpact(
    val amount: BigDecimal,
    val isIncome: Boolean,
)

internal fun calculateRemoteAccountBalance(
    displayedBalance: BigDecimal,
    pendingImpacts: List<PendingBalanceImpact>,
): BigDecimal = pendingImpacts.fold(displayedBalance) { balance, impact ->
    revertTransactionImpact(
        balance = balance,
        amount = impact.amount,
        isIncome = impact.isIncome,
    )
}

internal sealed interface SyncFailureAction {
    data class RetryServerFailure(val delayMillis: Long) : SyncFailureAction
    data class RetryLater(val delayMillis: Long) : SyncFailureAction
    data object MarkFailed : SyncFailureAction
    data object MarkUnknownResult : SyncFailureAction
}

internal fun classifySyncFailure(
    operationType: PendingOperationType,
    completedAttempts: Int,
    error: Exception,
): SyncFailureAction = when (error) {
    is FinanceNetworkException.ServerFailure -> {
        if (completedAttempts + 1 < MaxServerAttempts) {
            SyncFailureAction.RetryServerFailure(ServerRetryDelayMillis)
        } else {
            SyncFailureAction.MarkFailed
        }
    }
    is FinanceNetworkException.Network -> {
        if (
            operationType == PendingOperationType.Create &&
            !error.cause.wasDefinitelyNotSent()
        ) {
            SyncFailureAction.MarkUnknownResult
        } else {
            SyncFailureAction.RetryLater(NetworkRetryDelayMillis)
        }
    }
    is FinanceNetworkException.InvalidResponse -> {
        if (operationType == PendingOperationType.Create) {
            SyncFailureAction.MarkUnknownResult
        } else {
            SyncFailureAction.RetryLater(NetworkRetryDelayMillis)
        }
    }
    is FinanceNetworkException.Unauthorized,
    is FinanceNetworkException.ClientFailure,
    is FinanceNetworkException.UnexpectedResponse,
    -> SyncFailureAction.MarkFailed
    else -> SyncFailureAction.MarkFailed
}

private fun Throwable?.wasDefinitelyNotSent(): Boolean {
    var current = this
    while (current != null) {
        if (
            current is UnknownHostException ||
            current is ConnectException ||
            current is NoRouteToHostException
        ) {
            return true
        }
        current = current.cause
    }
    return false
}

private fun isCloseToLocalCreation(
    remoteCreatedAt: String,
    localCreatedAtMillis: Long,
): Boolean = runCatching {
    val difference = kotlin.math.abs(
        Instant.parse(remoteCreatedAt).toEpochMilli() - localCreatedAtMillis,
    )
    difference <= UnknownResultMatchWindowMillis
}.getOrDefault(false)

private fun AccountDto.toSyncedEntity(
    clientId: String,
    syncedAtMillis: Long,
): AccountEntity = try {
    AccountEntity(
        clientId = clientId,
        remoteId = id.toLong(),
        name = name,
        emoji = emoji,
        balance = BigDecimal(balance).toPlainString(),
        currency = CurrencyCode.parse(currency).value,
        createdAtMillis = Instant.parse(createdAt).toEpochMilli(),
        updatedAtMillis = Instant.parse(updatedAt).toEpochMilli(),
        updatedAtLocalMillis = syncedAtMillis,
        syncStatus = SyncStatus.Synced,
    )
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}

private const val MaxServerAttempts = 3
private const val ServerRetryDelayMillis = 2_000L
private const val NetworkRetryDelayMillis = 10_000L
private const val UnknownResultMatchWindowMillis = 5 * 60 * 1_000L
