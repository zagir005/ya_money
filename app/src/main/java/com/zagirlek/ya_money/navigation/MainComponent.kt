package com.zagirlek.ya_money.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.zagirlek.accounts.AccountsComponent
import com.zagirlek.accounts.DefaultAccountsComponent
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.api.income.IncomesRepository
import com.zagirlek.transactions.DefaultTransactionsComponent
import com.zagirlek.transactions.TransactionType
import com.zagirlek.transactions.TransactionsComponent

enum class MainTab {
    Expenses,
    Income,
    Accounts,
}

interface MainComponent {
    val childStack: Value<ChildStack<MainTab, Child>>

    fun select(tab: MainTab)

    sealed interface Child {
        data class Transactions(
            val type: TransactionType,
            val component: TransactionsComponent,
        ) : Child
        data class Accounts(val component: AccountsComponent) : Child
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val accountsRepository: AccountsRepository,
    private val expensesRepository: ExpensesRepository,
    private val incomesRepository: IncomesRepository,
) : MainComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<MainTab>()

    override val childStack: Value<ChildStack<MainTab, MainComponent.Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = MainTab.Expenses,
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override fun select(tab: MainTab) {
        navigation.replaceAll(tab)
    }

    private fun createChild(
        configuration: MainTab,
        componentContext: ComponentContext,
    ): MainComponent.Child = when (configuration) {
        MainTab.Expenses -> MainComponent.Child.Transactions(
            type = TransactionType.Expense,
            component = DefaultTransactionsComponent(
                componentContext = componentContext,
                type = TransactionType.Expense,
                expensesRepository = expensesRepository,
                incomesRepository = incomesRepository,
            ),
        )
        MainTab.Income -> MainComponent.Child.Transactions(
            type = TransactionType.Income,
            component = DefaultTransactionsComponent(
                componentContext = componentContext,
                type = TransactionType.Income,
                expensesRepository = expensesRepository,
                incomesRepository = incomesRepository,
            ),
        )
        MainTab.Accounts -> MainComponent.Child.Accounts(
            component = DefaultAccountsComponent(
                componentContext = componentContext,
                accountsRepository = accountsRepository,
            ),
        )
    }
}
