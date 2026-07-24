package com.zagirlek.ui.formatter

import com.zagirlek.finance.api.money.Money

fun Money.format(moneyFormatter: MoneyFormatter): String = moneyFormatter.format(this)
