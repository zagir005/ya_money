package com.zagirlek.accounts

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
    fun `loading error switches screen to error state`() {
        assertEquals(
            AccountsState.Error,
            AccountsReducer.reduce(content, AccountsMutation.Error),
        )
    }
}
