package com.zagirlek.transactions

import com.zagirlek.finance.api.error.NetworkError
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionsReducerTest {

    private val content = TransactionsState.Content(
        total = "1 000 ₽",
        items = listOf(
            TransactionItemUi(
                id = "transaction-1",
                lead = "🛒",
                title = "Продукты",
                trail = "1 000 ₽",
            ),
        ),
    )

    @Test
    fun `successful refresh stops indicator without a new room emission`() {
        assertEquals(
            content,
            TransactionsReducer.reduce(
                content.copy(isRefreshing = true),
                TransactionsMutation.RefreshCompleted,
            ),
        )
    }

    @Test
    fun `refresh error keeps cached transactions visible`() {
        val error = NetworkError.Network

        assertEquals(
            content.copy(refreshError = error),
            TransactionsReducer.reduce(
                content.copy(isRefreshing = true),
                TransactionsMutation.RefreshFailed(error),
            ),
        )
    }
}
