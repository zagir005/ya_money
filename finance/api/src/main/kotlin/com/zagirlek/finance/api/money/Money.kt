package com.zagirlek.finance.api.money

import java.math.BigDecimal
import java.util.Locale

@JvmInline
value class CurrencyCode(val value: String) {
    init {
        require(value.length == ISO_CODE_LENGTH && value.all(Char::isUpperCase)) {
            "Currency code must be a three-letter uppercase ISO 4217 code."
        }
    }

    companion object {
        val RUB = CurrencyCode("RUB")
        val USD = CurrencyCode("USD")
        val EUR = CurrencyCode("EUR")

        fun parse(value: String): CurrencyCode = CurrencyCode(
            value = value.trim().uppercase(Locale.ROOT),
        )

        private const val ISO_CODE_LENGTH = 3
    }
}

data class Money(
    val amount: BigDecimal,
    val currency: CurrencyCode,
) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add money in different currencies." }
        return copy(amount = amount + other.amount)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract money in different currencies." }
        return copy(amount = amount - other.amount)
    }

    fun negate(): Money = copy(amount = amount.negate())
}
