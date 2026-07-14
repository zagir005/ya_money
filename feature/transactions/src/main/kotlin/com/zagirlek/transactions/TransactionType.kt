package com.zagirlek.transactions

sealed interface TransactionType {
    data object Expense : TransactionType
    data object Income : TransactionType
}
