package com.zagirlek.finance.impl.local

import androidx.room.withTransaction

internal class FinanceLocalTransactionRunner(
    private val database: FinanceDatabase,
) {
    suspend fun <Result> run(block: suspend () -> Result): Result =
        database.withTransaction { block() }
}
