package com.zagirlek.transactions

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.CategoryId
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionEditorReducerTest {
    @Test
    fun `selecting category closes selector and clears save error`() {
        val state = content(
            activeSelector = TransactionEditorSelector.Category,
            saveError = "Ошибка",
        )

        val result = TransactionEditorReducer.reduce(
            state,
            TransactionEditorMutation.CategorySelected(CategoryId(2)),
        ) as TransactionEditorState.Content

        assertEquals(CategoryId(2), result.selectedCategoryId)
        assertNull(result.activeSelector)
        assertNull(result.saveError)
    }

    @Test
    fun `saving blocks repeated submit`() {
        val result = TransactionEditorReducer.reduce(
            content(),
            TransactionEditorMutation.Saving,
        ) as TransactionEditorState.Content

        assertTrue(result.isSaving)
        assertFalse(result.isSaveEnabled)
    }

    @Test
    fun `save failure keeps input and exposes message`() {
        val result = TransactionEditorReducer.reduce(
            content(amountInput = "1250,50"),
            TransactionEditorMutation.SaveFailed("Нет соединения"),
        ) as TransactionEditorState.Content

        assertEquals("1250,50", result.amountInput)
        assertEquals("Нет соединения", result.saveError)
        assertFalse(result.isSaving)
    }

    private fun content(
        amountInput: String = "100",
        activeSelector: TransactionEditorSelector? = null,
        saveError: String? = null,
    ) = TransactionEditorState.Content(
        type = TransactionType.Expense,
        amountInput = amountInput,
        categories = emptyList(),
        selectedCategoryId = CategoryId(1),
        accounts = emptyList(),
        selectedAccountId = AccountId("account"),
        date = LocalDate.of(2026, 7, 25),
        time = LocalTime.of(12, 30),
        activeSelector = activeSelector,
        saveError = saveError,
    )
}
