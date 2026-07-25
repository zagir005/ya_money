package com.zagirlek.finance.impl.transaction

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionBalanceProjectionTest {
    @Test
    fun `expense decreases balance and revert restores it`() {
        val balance = BigDecimal("1000.00")
        val amount = BigDecimal("125.50")

        val projected = applyTransactionImpact(
            balance = balance,
            amount = amount,
            isIncome = false,
        )
        val restored = revertTransactionImpact(
            balance = projected,
            amount = amount,
            isIncome = false,
        )

        assertEquals(BigDecimal("874.50"), projected)
        assertEquals(balance, restored)
    }

    @Test
    fun `income increases balance and revert restores it`() {
        val balance = BigDecimal("1000.00")
        val amount = BigDecimal("125.50")

        val projected = applyTransactionImpact(
            balance = balance,
            amount = amount,
            isIncome = true,
        )
        val restored = revertTransactionImpact(
            balance = projected,
            amount = amount,
            isIncome = true,
        )

        assertEquals(BigDecimal("1125.50"), projected)
        assertEquals(balance, restored)
    }

    @Test
    fun `editing transaction reverses old impact before applying new impact`() {
        val oldProjectedBalance = applyTransactionImpact(
            balance = BigDecimal("1000.00"),
            amount = BigDecimal("100.00"),
            isIncome = false,
        )

        val newProjectedBalance = applyTransactionImpact(
            balance = revertTransactionImpact(
                balance = oldProjectedBalance,
                amount = BigDecimal("100.00"),
                isIncome = false,
            ),
            amount = BigDecimal("250.00"),
            isIncome = true,
        )

        assertEquals(BigDecimal("1250.00"), newProjectedBalance)
    }
}
