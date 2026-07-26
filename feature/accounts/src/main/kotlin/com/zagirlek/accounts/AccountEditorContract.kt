package com.zagirlek.accounts

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.error.NetworkError
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State

sealed interface AccountEditorMode {
    data object Create : AccountEditorMode
    data class Edit(val accountId: AccountId) : AccountEditorMode
}

enum class AccountEditorSelector {
    Currency,
}

sealed interface AccountEditorState : State {
    data object Loading : AccountEditorState
    data class Error(val error: NetworkError) : AccountEditorState

    data class Content(
        val isCreating: Boolean,
        val nameInput: String,
        val emojiInput: String,
        val balanceInput: String,
        val currency: CurrencyCode,
        val activeSelector: AccountEditorSelector? = null,
        val isSaving: Boolean = false,
        val saveError: String? = null,
    ) : AccountEditorState {
        val isSaveEnabled: Boolean
            get() = nameInput.isNotBlank() &&
                emojiInput.isSingleEmoji() &&
                balanceInput.normalizedAmountOrNull() != null &&
                !isSaving
    }
}

sealed interface AccountEditorIntent : Intent {
    data class NameChanged(val value: String) : AccountEditorIntent
    data class EmojiChanged(val value: String) : AccountEditorIntent
    data class BalanceChanged(val value: String) : AccountEditorIntent
    data class CurrencySelected(val value: CurrencyCode) : AccountEditorIntent
    data class SelectorOpened(val value: AccountEditorSelector) : AccountEditorIntent
    data object SelectorDismissed : AccountEditorIntent
    data object SaveClicked : AccountEditorIntent
    data object Dismissed : AccountEditorIntent
}

sealed interface AccountEditorMutation : Mutation {
    data class Loaded(val state: AccountEditorState.Content) : AccountEditorMutation
    data class LoadFailed(val error: NetworkError) : AccountEditorMutation
    data class NameChanged(val value: String) : AccountEditorMutation
    data class EmojiChanged(val value: String) : AccountEditorMutation
    data class BalanceChanged(val value: String) : AccountEditorMutation
    data class CurrencySelected(val value: CurrencyCode) : AccountEditorMutation
    data class SelectorChanged(val value: AccountEditorSelector?) : AccountEditorMutation
    data object Saving : AccountEditorMutation
    data class SaveFailed(val message: String) : AccountEditorMutation
}

sealed interface AccountEditorEffect : Effect

object AccountEditorReducer : MviReducer<AccountEditorState, AccountEditorMutation> {
    override fun reduce(
        state: AccountEditorState,
        mutation: AccountEditorMutation,
    ): AccountEditorState = when (mutation) {
        is AccountEditorMutation.Loaded -> mutation.state
        is AccountEditorMutation.LoadFailed -> AccountEditorState.Error(mutation.error)
        is AccountEditorMutation.NameChanged -> state.contentOrSame {
            copy(nameInput = mutation.value, saveError = null)
        }
        is AccountEditorMutation.EmojiChanged -> state.contentOrSame {
            copy(emojiInput = mutation.value, saveError = null)
        }
        is AccountEditorMutation.BalanceChanged -> state.contentOrSame {
            copy(balanceInput = mutation.value, saveError = null)
        }
        is AccountEditorMutation.CurrencySelected -> state.contentOrSame {
            copy(currency = mutation.value, activeSelector = null, saveError = null)
        }
        is AccountEditorMutation.SelectorChanged -> state.contentOrSame {
            copy(activeSelector = mutation.value)
        }
        AccountEditorMutation.Saving -> state.contentOrSame {
            copy(isSaving = true, activeSelector = null, saveError = null)
        }
        is AccountEditorMutation.SaveFailed -> state.contentOrSame {
            copy(isSaving = false, saveError = mutation.message)
        }
    }
}

internal fun String.normalizedAmountOrNull() =
    replace(',', '.').toBigDecimalOrNull()

private inline fun AccountEditorState.contentOrSame(
    transform: AccountEditorState.Content.() -> AccountEditorState.Content,
): AccountEditorState = (this as? AccountEditorState.Content)?.transform() ?: this
