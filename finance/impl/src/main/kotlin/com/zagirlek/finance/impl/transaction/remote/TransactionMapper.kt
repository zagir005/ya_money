package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpenseId
import com.zagirlek.finance.api.expense.ExpenseType
import com.zagirlek.finance.api.expense.ExpenseTypeId
import com.zagirlek.finance.api.income.Income
import com.zagirlek.finance.api.income.IncomeId
import com.zagirlek.finance.api.income.IncomeType
import com.zagirlek.finance.api.income.IncomeTypeId
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset

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

@Deprecated(
    message = "Use toDomain() and the unified Transaction model.",
)
fun TransactionResponseDto.toExpense(): Expense = try {
    require(!category.isIncome) { "Income category cannot be mapped to expense." }
    val occurredAt = Instant.parse(transactionDate)
    Expense(
        id = ExpenseId(id.toString()),
        accountId = AccountId(account.id.toString()),
        type = ExpenseType(
            id = ExpenseTypeId(category.id.toString()),
            name = category.name,
            emoji = category.emoji,
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

@Deprecated(
    message = "Use toDomain() and the unified Transaction model.",
)
fun TransactionResponseDto.toIncome(): Income = try {
    require(category.isIncome) { "Expense category cannot be mapped to income." }
    val occurredAt = Instant.parse(transactionDate)
    Income(
        id = IncomeId(id.toString()),
        accountId = AccountId(account.id.toString()),
        type = IncomeType(
            id = IncomeTypeId(category.id.toString()),
            name = category.name,
            emoji = category.emoji,
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
