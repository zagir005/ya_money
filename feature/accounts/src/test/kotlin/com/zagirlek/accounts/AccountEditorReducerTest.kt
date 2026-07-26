package com.zagirlek.accounts

import com.zagirlek.finance.api.money.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountEditorReducerTest {
    @Test
    fun `create save requires name emoji and a valid balance`() {
        assertFalse(content(nameInput = "").isSaveEnabled)
        assertFalse(content(emojiInput = "").isSaveEnabled)
        assertFalse(content(emojiInput = "карта").isSaveEnabled)
        assertFalse(content(emojiInput = "💳💵").isSaveEnabled)
        assertFalse(content(balanceInput = "").isSaveEnabled)
        assertFalse(content(balanceInput = "1.2.3").isSaveEnabled)
        assertTrue(content(balanceInput = "0").isSaveEnabled)
        assertTrue(content(balanceInput = "-250,50").isSaveEnabled)
    }

    @Test
    fun `edit also requires a valid balance in the common form`() {
        assertFalse(content(isCreating = false, balanceInput = "").isSaveEnabled)
        assertFalse(content(isCreating = false, balanceInput = "12.3.4").isSaveEnabled)
        assertTrue(content(isCreating = false, balanceInput = "132000.04").isSaveEnabled)
    }

    @Test
    fun `single composite emoji and flag are accepted`() {
        assertTrue(content(emojiInput = "💳").isSaveEnabled)
        assertTrue(content(emojiInput = "👍🏽").isSaveEnabled)
        assertTrue(content(emojiInput = "👨‍👩‍👧‍👦").isSaveEnabled)
        assertTrue(content(emojiInput = "🇷🇺").isSaveEnabled)
        assertTrue(content(emojiInput = "1️⃣").isSaveEnabled)
    }

    @Test
    fun `currency selection closes selector and clears error`() {
        val result = AccountEditorReducer.reduce(
            content(
                activeSelector = AccountEditorSelector.Currency,
                saveError = "Ошибка",
            ),
            AccountEditorMutation.CurrencySelected(CurrencyCode.USD),
        ) as AccountEditorState.Content

        assertEquals(CurrencyCode.USD, result.currency)
        assertNull(result.activeSelector)
        assertNull(result.saveError)
    }

    @Test
    fun `saving disables button without clearing fields`() {
        val result = AccountEditorReducer.reduce(
            content(),
            AccountEditorMutation.Saving,
        ) as AccountEditorState.Content

        assertTrue(result.isSaving)
        assertFalse(result.isSaveEnabled)
        assertEquals("Основной счёт", result.nameInput)
    }

    private fun content(
        isCreating: Boolean = true,
        nameInput: String = "Основной счёт",
        emojiInput: String = "💳",
        balanceInput: String = "1000",
        activeSelector: AccountEditorSelector? = null,
        saveError: String? = null,
    ) = AccountEditorState.Content(
        isCreating = isCreating,
        nameInput = nameInput,
        emojiInput = emojiInput,
        balanceInput = balanceInput,
        currency = CurrencyCode.RUB,
        activeSelector = activeSelector,
        saveError = saveError,
    )
}
