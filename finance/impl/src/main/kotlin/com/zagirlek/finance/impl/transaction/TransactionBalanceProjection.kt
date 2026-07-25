package com.zagirlek.finance.impl.transaction

import java.math.BigDecimal

internal fun applyTransactionImpact(
    balance: BigDecimal,
    amount: BigDecimal,
    isIncome: Boolean,
): BigDecimal = if (isIncome) balance + amount else balance - amount

internal fun revertTransactionImpact(
    balance: BigDecimal,
    amount: BigDecimal,
    isIncome: Boolean,
): BigDecimal = if (isIncome) balance - amount else balance + amount
