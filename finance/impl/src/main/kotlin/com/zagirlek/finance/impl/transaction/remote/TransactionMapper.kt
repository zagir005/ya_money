package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.finance.impl.category.remote.CategoryDto
import java.math.BigDecimal
import java.time.Instant

fun TransactionResponseDto.toDomain(): Transaction = try {
    Transaction(
        id = TransactionId(id.toString()),
        accountId = AccountId(account.id.toString()),
        category = Category(
            id = CategoryId(category.id),
            name = category.name,
            emoji = category.emoji,
            type = if (category.isIncome) TransactionType.Income else TransactionType.Expense,
        ),
        money = Money(
            amount = BigDecimal(amount),
            currency = CurrencyCode.parse(account.currency),
        ),
        occurredAt = Instant.parse(transactionDate),
        comment = comment,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}
