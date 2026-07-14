package com.zagirlek.ui.formatter

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: Currency
)

sealed class Currency(val code: String){
    data object Ruble: Currency(code = "RUB")

    fun toSymbol(): String = when(this) {
        Ruble -> "₽"
    }
}
