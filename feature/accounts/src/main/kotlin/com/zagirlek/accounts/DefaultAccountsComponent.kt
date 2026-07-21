package com.zagirlek.accounts

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.Currency
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.Money
import com.zagirlek.ui.formatter.MoneyFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class DefaultAccountsComponent(
    componentContext: ComponentContext,
    private val accountsRepository: AccountsRepository,
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<AccountsState, AccountsMutation, AccountsIntent, AccountsReducer>(
    reducer = AccountsReducer,
    componentContext = componentContext,
), AccountsComponent {

    private val mutableState = MutableStateFlow<AccountsState>(AccountsState.Loading)

    override val state: StateFlow<AccountsState> = mutableState.asStateFlow()
    private val mutableEffects = MutableSharedFlow<AccountsEffect>(extraBufferCapacity = 1)

    override val effects: Flow<AccountsEffect> = mutableEffects.asSharedFlow()

    private var loadJob: Job? = null

    init {
        loadAccounts()
    }

    override fun accept(intent: AccountsIntent) {
        when (intent) {
            is AccountsIntent.AccountClicked -> Unit
            AccountsIntent.DateClicked -> Unit
            AccountsIntent.AnalyticsClicked -> Unit
            AccountsIntent.SettingsClicked -> Unit
            AccountsIntent.AddClicked -> Unit
            AccountsIntent.RetryClicked -> loadAccounts()
            AccountsIntent.RefreshRequested -> loadAccounts(isRefresh = true)
        }
    }

    private fun loadAccounts(isRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        val isContentRefresh = isRefresh && mutableState.value is AccountsState.Content
        if (isContentRefresh) {
            AccountsMutation.Refreshing.reduce(mutableState)
        } else {
            AccountsMutation.Loading.reduce(mutableState)
        }

        loadJob = ioScope.launch {
            val mutation = try {
                accountsRepository.getAccounts().toMutation()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isContentRefresh) {
                    AccountsMutation.RefreshFailed(error.toErrorMessage())
                } else {
                    AccountsMutation.Error(error.toErrorMessage())
                }
            }

            componentScope.launch {
                if (mutation is AccountsMutation.RefreshFailed) {
                    mutableEffects.tryEmit(AccountsEffect.ShowRetryableError(mutation.message))
                }
                mutation.reduce(mutableState)
            }
        }
    }

    private fun List<Account>.toMutation(): AccountsMutation = when {
        isEmpty() -> AccountsMutation.Empty
        else -> AccountsMutation.Content(
            total = Money(sumOf(Account::balance), Currency.Ruble).format(moneyFormatter),
            items = map { account ->
                AccountItemUi(
                    id = account.id.value,
                    lead = account.emoji,
                    content = account.name,
                    trail = Money(account.balance, Currency.Ruble).format(moneyFormatter),
                )
            },
        )
    }

    private fun Exception.toErrorMessage(): String = message ?: DEFAULT_ERROR_MESSAGE

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "Не удалось загрузить счета."
    }
}
