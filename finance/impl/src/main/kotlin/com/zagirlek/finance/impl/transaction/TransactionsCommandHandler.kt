package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.api.transaction.CreateTransaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.UpdateTransaction

internal interface TransactionsCommandHandler {
    suspend fun create(command: CreateTransaction): TransactionId

    suspend fun update(command: UpdateTransaction)
}
