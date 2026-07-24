package com.zagirlek.finance.api.income

import com.zagirlek.finance.api.transaction.TransactionPeriod

@Deprecated("Use TransactionsRepository.")
interface IncomesRepository {
    suspend fun getIncomes(period: TransactionPeriod): List<Income>
}
