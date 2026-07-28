package com.zagirlek.accounts

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.account.CreateAccount
import com.zagirlek.finance.api.account.UpdateAccount
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.ui.cmp.MviComponent
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class DefaultAccountEditorComponent(
    componentContext: ComponentContext,
    private val mode: AccountEditorMode,
    private val accountsRepository: AccountsRepository,
    private val onDismissRequested: () -> Unit,
) : MviComponent<
    AccountEditorState,
    AccountEditorMutation,
    AccountEditorIntent,
    AccountEditorReducer
>(
    reducer = AccountEditorReducer,
    componentContext = componentContext,
), AccountEditorComponent {
    private val mutableState = MutableStateFlow<AccountEditorState>(
        when (mode) {
            AccountEditorMode.Create -> AccountEditorState.Content(
                isCreating = true,
                nameInput = "",
                emojiInput = "",
                balanceInput = "",
                currency = CurrencyCode.RUB,
            )
            is AccountEditorMode.Edit -> AccountEditorState.Loading
        },
    )

    override val state: StateFlow<AccountEditorState> = mutableState.asStateFlow()
    override val effects: Flow<AccountEditorEffect> = emptyFlow()

    init {
        if (mode is AccountEditorMode.Edit) {
            observeAccount(mode)
            refreshAccount()
        }
    }

    override fun accept(intent: AccountEditorIntent) {
        when (intent) {
            is AccountEditorIntent.NameChanged ->
                AccountEditorMutation.NameChanged(intent.value).reduce(mutableState)
            is AccountEditorIntent.EmojiChanged ->
                AccountEditorMutation.EmojiChanged(intent.value).reduce(mutableState)
            is AccountEditorIntent.BalanceChanged ->
                AccountEditorMutation.BalanceChanged(intent.value).reduce(mutableState)
            is AccountEditorIntent.CurrencySelected ->
                AccountEditorMutation.CurrencySelected(intent.value).reduce(mutableState)
            is AccountEditorIntent.SelectorOpened ->
                AccountEditorMutation.SelectorChanged(intent.value).reduce(mutableState)
            AccountEditorIntent.SelectorDismissed ->
                AccountEditorMutation.SelectorChanged(null).reduce(mutableState)
            AccountEditorIntent.SaveClicked -> save()
            AccountEditorIntent.Dismissed -> onDismissRequested()
        }
    }

    private fun observeAccount(editMode: AccountEditorMode.Edit) {
        componentScope.launch {
            accountsRepository.observeAccount(editMode.accountId).collect { account ->
                if (account != null) {
                    val current = mutableState.value as? AccountEditorState.Content
                    val content = current?.copy(
                        balanceInput = account.money.amount.toPlainString(),
                    ) ?: account.toEditorState()
                    AccountEditorMutation.Loaded(content).reduce(mutableState)
                }
            }
        }
    }

    private fun refreshAccount() {
        ioScope.launch {
            try {
                accountsRepository.refreshAccounts()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
            }
        }
    }

    private fun save() {
        val state = mutableState.value as? AccountEditorState.Content ?: return
        if (!state.isSaveEnabled) return
        val balance = state.balanceInput.normalizedAmountOrNull()

        AccountEditorMutation.Saving.reduce(mutableState)
        ioScope.launch {
            try {
                when (val value = mode) {
                    AccountEditorMode.Create -> accountsRepository.createAccount(
                        CreateAccount(
                            name = state.nameInput,
                            emoji = state.emojiInput,
                            initialBalance = Money(
                                amount = requireNotNull(balance),
                                currency = state.currency,
                            ),
                        ),
                    )
                    is AccountEditorMode.Edit -> accountsRepository.updateAccount(
                        UpdateAccount(
                            accountId = value.accountId,
                            name = state.nameInput,
                            emoji = state.emojiInput,
                            balance = Money(
                                amount = requireNotNull(balance),
                                currency = state.currency,
                            ),
                        ),
                    )
                }
                componentScope.launch {
                    onDismissRequested()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                componentScope.launch {
                    AccountEditorMutation.SaveFailed(
                        error.message ?: "Не удалось сохранить счёт.",
                    ).reduce(mutableState)
                }
            }
        }
    }
}

private fun Account.toEditorState() = AccountEditorState.Content(
    isCreating = false,
    nameInput = name,
    emojiInput = emoji,
    balanceInput = money.amount.toPlainString(),
    currency = money.currency,
)
