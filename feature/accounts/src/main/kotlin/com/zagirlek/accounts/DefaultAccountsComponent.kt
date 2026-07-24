package com.zagirlek.accounts

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.Account
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
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<AccountsState, AccountsMutation, AccountsIntent, AccountsReducer>(
    reducer = AccountsReducer,
    componentContext = componentContext,
), AccountsComponent {

    private val mutableState = MutableStateFlow<AccountsState>(AccountsState.Loading)

    override val state: StateFlow<AccountsState> = mutableState.asStateFlow()
    override val effects: Flow<AccountsEffect> = emptyFlow()

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
            AccountsIntent.RetryClicked -> loadAccounts(isRefresh = true)
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
                    AccountsMutation.RefreshFailed(error.toNetworkError())
                } else {
                    AccountsMutation.Error(error.toNetworkError())
                }
            }

            componentScope.launch {
                mutation.reduce(mutableState)
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
