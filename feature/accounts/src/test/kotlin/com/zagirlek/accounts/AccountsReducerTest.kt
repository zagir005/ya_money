package com.zagirlek.accounts

import com.zagirlek.finance.api.error.NetworkError
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountsReducerTest {

    private val content = AccountsState.Content(
        total = "1 000 ₽",
        items = listOf(
            AccountItemUi(
                id = "account-1",
                lead = "💳",
                content = "Основной счёт",
                trail = "1 000 ₽",
            ),
        ),
    )

    @Test
    fun `refresh keeps content visible and enables indicator`() {
        assertEquals(
            content.copy(isRefreshing = true),
            AccountsReducer.reduce(content, AccountsMutation.Refreshing),
        )
    }

    @Test
    fun `successful refresh stops indicator without a new room emission`() {
        assertEquals(
            content,
            AccountsReducer.reduce(
                content.copy(isRefreshing = true),
                AccountsMutation.RefreshCompleted,
            ),
        )
    }

    @Test
    fun `loading error switches screen to error state`() {
        val error = NetworkError.Network

        assertEquals(
            AccountsState.Error(error),
            AccountsReducer.reduce(content, AccountsMutation.Error(error)),
        )
    }

    @Test
    fun `refresh error keeps current content and hides indicator`() {
        val error = NetworkError.Network

        assertEquals(
            content.copy(refreshError = error),
            AccountsReducer.reduce(
                content.copy(isRefreshing = true),
                AccountsMutation.RefreshFailed(error),
            ),
        )
    }
}
