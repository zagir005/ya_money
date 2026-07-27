package com.zagirlek.accounts

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.error.toNetworkError
import com.zagirlek.finance.api.money.Money
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.MoneyFormatter
import com.zagirlek.ui.formatter.format
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class DefaultAccountsComponent(
    componentContext: ComponentContext,
    private val accountsRepository: AccountsRepository,
    private val onCreateAccountRequested: () -> Unit,
    private val onEditAccountRequested: (AccountId) -> Unit,
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<AccountsState, AccountsMutation, AccountsIntent, AccountsReducer>(
    reducer = AccountsReducer,
    componentContext = componentContext,
), AccountsComponent {

    private val mutableState = MutableStateFlow<AccountsState>(AccountsState.Loading)

    override val state: StateFlow<AccountsState> = mutableState.asStateFlow()
    override val effects: Flow<AccountsEffect> = emptyFlow()

    private var refreshJob: Job? = null

    init {
        observeAccounts()
        refreshAccounts()
    }

    override fun accept(intent: AccountsIntent) {
        when (intent) {
            is AccountsIntent.AccountClicked ->
                onEditAccountRequested(AccountId(intent.id))
            AccountsIntent.DateClicked -> Unit
            AccountsIntent.AnalyticsClicked -> Unit
            AccountsIntent.SettingsClicked -> Unit
            AccountsIntent.AddClicked -> onCreateAccountRequested()
            AccountsIntent.RetryClicked,
            AccountsIntent.RefreshRequested,
            -> refreshAccounts()
        }
    }

    private fun observeAccounts() {
        componentScope.launch {
            accountsRepository.observeAccounts().collect { accounts ->
                accounts.toMutation().reduce(mutableState)
            }
        }
    }

    private fun refreshAccounts() {
        if (refreshJob?.isActive == true) return

        if (mutableState.value is AccountsState.Content) {
            AccountsMutation.Refreshing.reduce(mutableState)
        }

        refreshJob = ioScope.launch {
            try {
                accountsRepository.refreshAccounts()
                componentScope.launch {
                    AccountsMutation.RefreshCompleted.reduce(mutableState)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                componentScope.launch {
                    val mutation = if (mutableState.value is AccountsState.Content) {
                        AccountsMutation.RefreshFailed(error.toNetworkError())
                    } else {
                        AccountsMutation.Error(error.toNetworkError())
                    }
                    mutation.reduce(mutableState)
                }
            }
        }
    }

    private fun List<Account>.toMutation(): AccountsMutation = when {
        isEmpty() -> AccountsMutation.Empty
        else -> AccountsMutation.Content(
            total = groupBy { account -> account.money.currency }
                .values
                .map { accounts ->
                    Money(
                        amount = accounts.sumOf { account -> account.money.amount },
                        currency = accounts.first().money.currency,
                    ).format(moneyFormatter)
                }
                .joinToString(separator = " · "),
            items = map { account ->
                AccountItemUi(
                    id = account.id.value,
                    lead = account.emoji,
                    content = account.name,
                    trail = account.money.format(moneyFormatter),
                )
            },
        )
    }

}
