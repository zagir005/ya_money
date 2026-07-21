package com.zagirlek.finance.api.income

import com.zagirlek.finance.api.transaction.TransactionPeriod

interface IncomesRepository {
    suspend fun getIncomes(period: TransactionPeriod): List<Income>
}
