package com.zagirlek.finance.api.category

import com.zagirlek.finance.api.transaction.TransactionType

@JvmInline
value class CategoryId(val value: Int) {
    init {
        require(value > 0) { "Category ID must be positive." }
    }
}

data class Category(
    val id: CategoryId,
    val name: String,
    val emoji: String,
    val type: TransactionType,
)
