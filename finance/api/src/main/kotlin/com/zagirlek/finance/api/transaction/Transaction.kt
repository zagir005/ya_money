package com.zagirlek.finance.api.transaction

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.money.Money
import java.time.Instant

@JvmInline
value class TransactionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Transaction ID must not be blank." }
    }
}

enum class TransactionType {
    Expense,
    Income,
}

data class Transaction(
    val id: TransactionId,
    val accountId: AccountId,
    val category: Category,
    val money: Money,
    val occurredAt: Instant,
    val comment: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CreateTransaction(
    val accountId: AccountId,
    val categoryId: CategoryId,
    val money: Money,
    val occurredAt: Instant,
    val comment: String?,
) {
    init {
        require(money.amount.signum() > 0) { "Transaction amount must be positive." }
    }
}

data class UpdateTransaction(
    val transactionId: TransactionId,
    val accountId: AccountId,
    val categoryId: CategoryId,
    val money: Money,
    val occurredAt: Instant,
    val comment: String?,
) {
    init {
        require(money.amount.signum() > 0) { "Transaction amount must be positive." }
    }
}
