package com.zagirlek.analytics

import com.zagirlek.finance.api.error.NetworkError
import com.zagirlek.finance.api.transaction.TransactionPeriod
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsReducerTest {

    private val content = AnalyticsState.Content(
        period = TransactionPeriod(
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 27),
        ),
        allTransactions = emptyList(),
        transactions = emptyList(),
        accounts = emptyList(),
        filters = AnalyticsFilters(),
        summary = AnalyticsSummaryUi(
            total = "1 000 ₽",
            categories = emptyList(),
        ),
        transactionItems = listOf(
            AnalyticsTransactionItemUi(
                id = "transaction-1",
                title = "Продукты",
                subtitle = "Основной счёт",
                emoji = "🛒",
                amount = "1 000 ₽",
            ),
        ),
        filterOptions = AnalyticsFilterOptions(
            categories = emptyList(),
            accounts = emptyList(),
        ),
    )

    @Test
    fun `refresh error keeps cached analytics content visible`() {
        val error = NetworkError.Network

        assertEquals(
            content.copy(historyError = error),
            AnalyticsReducer.reduce(
                content.copy(isRefreshing = true),
                AnalyticsMutation.RefreshFailed(error),
            ),
        )
    }

    @Test
    fun `successful refresh only stops indicator until room emits data`() {
        assertEquals(
            content,
            AnalyticsReducer.reduce(
                content.copy(isRefreshing = true),
                AnalyticsMutation.RefreshCompleted,
            ),
        )
    }
}
