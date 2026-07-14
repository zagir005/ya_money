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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

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
        }
    }

    private fun loadAccounts() {
        AccountsMutation.Loading.reduce(mutableState)

        componentScope.launch {
            val mutation = runCatching {
                accountsRepository.getAccounts().toMutation()
            }.getOrElse {
                AccountsMutation.Error
            }

            mutation.reduce(mutableState)
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
}
