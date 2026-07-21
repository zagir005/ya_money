package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.transaction.TransactionCategory
import com.zagirlek.finance.api.transaction.TransactionHistoryEntry
import com.zagirlek.finance.api.transaction.TransactionHistoryRepository
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.finance.impl.network.FinanceHttpClient
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset

class RemoteTransactionHistoryRepository(
    accountsRepository: AccountsRepository,
    httpClient: FinanceHttpClient,
) : TransactionHistoryRepository {
    private val historyLoader = RemoteTransactionHistoryLoader(accountsRepository, httpClient)

    override suspend fun getHistory(period: TransactionPeriod): List<TransactionHistoryEntry> =
        historyLoader.load(period)
            .asSequence()
            .map(TransactionResponseDto::toTransactionHistoryEntry)
            .sortedByDescending(TransactionHistoryEntry::occurredAt)
            .toList()
}

private fun TransactionResponseDto.toTransactionHistoryEntry(): TransactionHistoryEntry = try {
    val occurredAt = Instant.parse(transactionDate)
    TransactionHistoryEntry(
        id = TransactionId(id.toString()),
        accountId = AccountId(account.id.toString()),
        category = TransactionCategory(
            id = category.id,
            name = category.name,
            emoji = category.emoji,
            type = if (category.isIncome) TransactionType.Income else TransactionType.Expense,
        ),
        amount = BigDecimal(amount),
        occurredAt = occurredAt,
        occurredOn = occurredAt.atZone(ZoneOffset.UTC).toLocalDate(),
        createdAt = Instant.parse(createdAt),
        description = comment,
    )
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}
