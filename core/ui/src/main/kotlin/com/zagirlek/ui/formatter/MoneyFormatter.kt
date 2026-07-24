package com.zagirlek.ui.formatter

import com.zagirlek.finance.api.money.Money

interface MoneyFormatter {
    fun format(money: Money): String
}
