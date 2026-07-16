package com.zagirlek.finance.impl.income

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.income.Income
import com.zagirlek.finance.api.income.IncomeId
import com.zagirlek.finance.api.income.IncomeType
import com.zagirlek.finance.api.income.IncomeTypeId
import com.zagirlek.finance.api.income.IncomesRepository
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class FakeIncomesRepository : IncomesRepository {
    override suspend fun getIncomes(): List<Income> = incomes

    private companion object {
        val incomes = listOf(
            Income(
                id = IncomeId("income-1"),
                accountId = AccountId("account-main"),
                type = IncomeType(IncomeTypeId("salary"), "Зарплата", "💼"),
                amount = BigDecimal("120000.00"),
                occurredOn = LocalDate.of(2026, 7, 10),
                createdAt = Instant.parse("2026-07-10T07:00:00Z"),
                description = "Зарплата за июль",
            ),
            Income(
                id = IncomeId("income-2"),
                accountId = AccountId("account-main"),
                type = IncomeType(IncomeTypeId("cashback"), "Кэшбэк", "✨"),
                amount = BigDecimal("385.42"),
                occurredOn = LocalDate.of(2026, 7, 12),
                createdAt = Instant.parse("2026-07-12T09:15:00Z"),
                description = "Кэшбэк за покупки",
            ),
        )
    }
}
