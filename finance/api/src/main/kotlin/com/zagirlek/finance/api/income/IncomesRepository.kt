package com.zagirlek.finance.api.income

interface IncomesRepository {
    fun getIncomes(): List<Income>
}
