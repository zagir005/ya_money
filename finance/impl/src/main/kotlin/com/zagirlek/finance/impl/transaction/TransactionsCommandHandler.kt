package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.api.transaction.CreateTransaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.UpdateTransaction

/**
 * Локальные create/update-команды подключаются на этапе редактора транзакции.
 * Чтение и refresh при этом уже полностью Room-backed.
 */
internal interface TransactionsCommandHandler {
    suspend fun create(command: CreateTransaction): TransactionId

    suspend fun update(command: UpdateTransaction)
}
