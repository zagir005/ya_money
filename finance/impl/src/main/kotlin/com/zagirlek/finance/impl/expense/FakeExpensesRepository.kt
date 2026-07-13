package com.zagirlek.finance.impl.expense

import com.zagirlek.finance.api.expense.AccountId
import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpenseId
import com.zagirlek.finance.api.expense.ExpenseType
import com.zagirlek.finance.api.expense.ExpenseTypeId
import com.zagirlek.finance.api.expense.ExpensesRepository
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/** Temporary source for the first homework iteration. */
class FakeExpensesRepository : ExpensesRepository {
    override fun getExpenses(): List<Expense> = expenses

    private companion object {
        val expenses = listOf(
            Expense(
                id = ExpenseId("expense-1"),
                accountId = AccountId("account-main"),
                type = ExpenseType(ExpenseTypeId("products"), "Продукты", "🛒"),
                amount = BigDecimal("1280.50"),
                occurredOn = LocalDate.of(2026, 7, 13),
                createdAt = Instant.parse("2026-07-13T08:25:00Z"),
                description = "Перекрёсток",
            ),
            Expense(
                id = ExpenseId("expense-2"),
                accountId = AccountId("account-main"),
                type = ExpenseType(ExpenseTypeId("transport"), "Транспорт", "🚇"),
                amount = BigDecimal("65.00"),
                occurredOn = LocalDate.of(2026, 7, 13),
                createdAt = Instant.parse("2026-07-13T06:40:00Z"),
                description = "Метро",
            ),
            Expense(
                id = ExpenseId("expense-3"),
                accountId = AccountId("account-main"),
                type = ExpenseType(ExpenseTypeId("coffee"), "Кафе", "☕"),
                amount = BigDecimal("420.00"),
                occurredOn = LocalDate.of(2026, 7, 13),
                createdAt = Instant.parse("2026-07-13T10:10:00Z"),
                description = "Кофе с собой",
            ),
        )
    }
}
