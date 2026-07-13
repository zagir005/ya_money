package com.zagirlek.ya_money.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.transactions.expenses.DefaultExpensesComponent
import com.zagirlek.transactions.expenses.ExpensesComponent

enum class MainTab {
    Expenses,
    Income,
    Accounts,
}

interface MainComponent {
    val childStack: Value<ChildStack<MainTab, Child>>

    fun select(tab: MainTab)

    sealed interface Child {
        data class Expenses(val component: ExpensesComponent) : Child
        data object Income : Child
        data object Accounts : Child
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val expensesRepository: ExpensesRepository,
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
        @Suppress("UNUSED_PARAMETER") componentContext: ComponentContext,
    ): MainComponent.Child = when (configuration) {
        MainTab.Expenses -> MainComponent.Child.Expenses(
            component = DefaultExpensesComponent(expensesRepository),
        )
        MainTab.Income -> MainComponent.Child.Income
        MainTab.Accounts -> MainComponent.Child.Accounts
    }
}
