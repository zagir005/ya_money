package com.zagirlek.transactions

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.error.NetworkError
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State
import java.time.LocalDate
import java.time.LocalTime

sealed interface TransactionEditorMode {
    data class Create(val type: TransactionType) : TransactionEditorMode
    data class Edit(val transactionId: TransactionId) : TransactionEditorMode
}

data class TransactionEditorCategoryUi(
    val id: CategoryId,
    val name: String,
    val emoji: String,
)

data class TransactionEditorAccountUi(
    val id: AccountId,
    val name: String,
    val emoji: String,
    val currency: String,
)

enum class TransactionEditorSelector {
    Category,
    Date,
    Time,
    Account,
}

sealed interface TransactionEditorState : State {
    data object Loading : TransactionEditorState
    data class Error(val error: NetworkError) : TransactionEditorState

    data class Content(
        val type: TransactionType,
        val amountInput: String,
        val categories: List<TransactionEditorCategoryUi>,
        val selectedCategoryId: CategoryId?,
        val accounts: List<TransactionEditorAccountUi>,
        val selectedAccountId: AccountId?,
        val date: LocalDate,
        val time: LocalTime,
        val activeSelector: TransactionEditorSelector? = null,
        val isSaving: Boolean = false,
        val saveError: String? = null,
    ) : TransactionEditorState {
        val isSaveEnabled: Boolean
            get() = amountInput
                .replace(',', '.')
                .toBigDecimalOrNull()
                ?.signum() == 1 &&
                categories.any { it.id == selectedCategoryId } &&
                accounts.any { it.id == selectedAccountId } &&
                !isSaving
    }
}

sealed interface TransactionEditorIntent : Intent {
    data class AmountChanged(val value: String) : TransactionEditorIntent
    data class CategorySelected(val id: CategoryId) : TransactionEditorIntent
    data class AccountSelected(val id: AccountId) : TransactionEditorIntent
    data class DateSelected(val value: LocalDate) : TransactionEditorIntent
    data class TimeSelected(val value: LocalTime) : TransactionEditorIntent
    data class SelectorOpened(val selector: TransactionEditorSelector) : TransactionEditorIntent
    data object SelectorDismissed : TransactionEditorIntent
    data object SaveClicked : TransactionEditorIntent
    data object Dismissed : TransactionEditorIntent
}

sealed interface TransactionEditorMutation : Mutation {
    data class Loaded(val state: TransactionEditorState.Content) : TransactionEditorMutation
    data class LoadFailed(val error: NetworkError) : TransactionEditorMutation
    data class AmountChanged(val value: String) : TransactionEditorMutation
    data class CategorySelected(val id: CategoryId) : TransactionEditorMutation
    data class AccountSelected(val id: AccountId) : TransactionEditorMutation
    data class DateSelected(val value: LocalDate) : TransactionEditorMutation
    data class TimeSelected(val value: LocalTime) : TransactionEditorMutation
    data class SelectorChanged(val value: TransactionEditorSelector?) : TransactionEditorMutation
    data object Saving : TransactionEditorMutation
    data class SaveFailed(val message: String) : TransactionEditorMutation
}

sealed interface TransactionEditorEffect : Effect

object TransactionEditorReducer :
    MviReducer<TransactionEditorState, TransactionEditorMutation> {
    override fun reduce(
        state: TransactionEditorState,
        mutation: TransactionEditorMutation,
    ): TransactionEditorState = when (mutation) {
        is TransactionEditorMutation.Loaded -> mutation.state
        is TransactionEditorMutation.LoadFailed -> TransactionEditorState.Error(mutation.error)
        is TransactionEditorMutation.AmountChanged -> state.contentOrSame {
            copy(amountInput = mutation.value, saveError = null)
        }
        is TransactionEditorMutation.CategorySelected -> state.contentOrSame {
            copy(
                selectedCategoryId = mutation.id,
                activeSelector = null,
                saveError = null,
            )
        }
        is TransactionEditorMutation.AccountSelected -> state.contentOrSame {
            copy(
                selectedAccountId = mutation.id,
                activeSelector = null,
                saveError = null,
            )
        }
        is TransactionEditorMutation.DateSelected -> state.contentOrSame {
            copy(date = mutation.value, activeSelector = null, saveError = null)
        }
        is TransactionEditorMutation.TimeSelected -> state.contentOrSame {
            copy(time = mutation.value, activeSelector = null, saveError = null)
        }
        is TransactionEditorMutation.SelectorChanged -> state.contentOrSame {
            copy(activeSelector = mutation.value)
        }
        TransactionEditorMutation.Saving -> state.contentOrSame {
            copy(isSaving = true, saveError = null, activeSelector = null)
        }
        is TransactionEditorMutation.SaveFailed -> state.contentOrSame {
            copy(isSaving = false, saveError = mutation.message)
        }
    }
}

private inline fun TransactionEditorState.contentOrSame(
    transform: TransactionEditorState.Content.() -> TransactionEditorState.Content,
): TransactionEditorState = (this as? TransactionEditorState.Content)?.transform() ?: this
