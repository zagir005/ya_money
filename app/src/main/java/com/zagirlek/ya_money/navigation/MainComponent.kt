package com.zagirlek.ya_money.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import com.zagirlek.accounts.AccountsComponent
import com.zagirlek.accounts.DefaultAccountsComponent
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.transactions.DefaultTransactionsComponent
import com.zagirlek.transactions.TransactionType
import com.zagirlek.transactions.TransactionsComponent

enum class MainTab {
    Expenses,
    Income,
    Accounts,
}

interface MainComponent {
    val childPages: Value<ChildPages<MainTab, Child>>

    fun select(tab: MainTab)

    fun openAnalytics()

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
    private val transactionsRepository: TransactionsRepository,
    private val onAnalyticsRequested: () -> Unit,
    private val onCreateTransactionRequested: (TransactionType) -> Unit,
    private val onEditTransactionRequested: (TransactionId) -> Unit,
    private val onCreateAccountRequested: () -> Unit,
    private val onEditAccountRequested: (AccountId) -> Unit,
) : MainComponent, ComponentContext by componentContext {
    private val navigation = PagesNavigation<MainTab>()

    override val childPages: Value<ChildPages<MainTab, MainComponent.Child>> = childPages(
        source = navigation,
        serializer = null,
        initialPages = {
            Pages(
                items = MainTab.entries,
                selectedIndex = MainTab.Expenses.ordinal,
            )
        },
        pageStatus = { index, pages ->
            if (index == pages.selectedIndex) {
                ChildNavState.Status.RESUMED
            } else {
                ChildNavState.Status.CREATED
            }
        },
        handleBackButton = false,
        childFactory = ::createChild,
    )

    override fun select(tab: MainTab) {
        navigation.select(tab.ordinal)
    }

    override fun openAnalytics() {
        onAnalyticsRequested()
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
                transactionsRepository = transactionsRepository,
                onAddRequested = onCreateTransactionRequested,
                onEditRequested = onEditTransactionRequested,
            ),
        )
        MainTab.Income -> MainComponent.Child.Transactions(
            type = TransactionType.Income,
            component = DefaultTransactionsComponent(
                componentContext = componentContext,
                type = TransactionType.Income,
                transactionsRepository = transactionsRepository,
                onAddRequested = onCreateTransactionRequested,
                onEditRequested = onEditTransactionRequested,
            ),
        )
        MainTab.Accounts -> MainComponent.Child.Accounts(
            component = DefaultAccountsComponent(
                componentContext = componentContext,
                accountsRepository = accountsRepository,
                onCreateAccountRequested = onCreateAccountRequested,
                onEditAccountRequested = onEditAccountRequested,
            ),
        )
    }
}
