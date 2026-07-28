package com.zagirlek.finance.impl.transaction.remote

import java.math.BigDecimal
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

internal fun String.toTransactionRequestAmount(): String =
    BigDecimal(this).setScale(TransactionAmountScale).toPlainString()

internal fun Instant.toTransactionRequestDate(): String =
    TransactionDateFormatter.format(this)

private const val TransactionAmountScale = 2

private val TransactionDateFormatter: DateTimeFormatter =
    DateTimeFormatterBuilder().appendInstant(3).toFormatter()
