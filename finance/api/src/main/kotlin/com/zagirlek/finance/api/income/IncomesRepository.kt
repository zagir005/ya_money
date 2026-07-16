package com.zagirlek.finance.api.income

interface IncomesRepository {
    suspend fun getIncomes(): List<Income>
}
