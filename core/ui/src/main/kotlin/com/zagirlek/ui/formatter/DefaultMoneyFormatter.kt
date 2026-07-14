package com.zagirlek.ui.formatter

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class DefaultMoneyFormatter : MoneyFormatter {
    override fun format(money: Money): String {
        val amount = formatAmount(money.amount)
        val currency = money.currency.toSymbol()

        return "$amount\u00A0$currency"
    }

    private fun formatAmount(amount: BigDecimal): String {
        val symbols = DecimalFormatSymbols().apply {
            groupingSeparator = '\u00A0'
        }

        return DecimalFormat("#,##0", symbols).format(amount)
    }
}