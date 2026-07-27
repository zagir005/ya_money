package com.zagirlek.ya_money.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.zagirlek.analytics.AnalyticsComponent
import com.zagirlek.analytics.DefaultAnalyticsComponent
import com.zagirlek.accounts.AccountEditorComponent
import com.zagirlek.accounts.AccountEditorMode
import com.zagirlek.accounts.DefaultAccountEditorComponent
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.category.CategoriesRepository
import com.zagirlek.finance.api.sync.FinanceSyncStatus
import com.zagirlek.finance.api.sync.FinanceSyncStatusRepository
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.transactions.DefaultTransactionEditorComponent
import com.zagirlek.transactions.TransactionEditorComponent
import com.zagirlek.transactions.TransactionEditorMode
import com.zagirlek.transactions.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {
    val childStack: Value<ChildStack<Configuration, Child>>
    val isOnline: StateFlow<Boolean>
    val syncStatus: Flow<FinanceSyncStatus>

    fun retrySync()

    sealed interface Configuration {
        data object Main : Configuration
        data object Analytics : Configuration
        data class CreateTransaction(val type: TransactionType) : Configuration
        data class EditTransaction(val transactionId: TransactionId) : Configuration
        data object CreateAccount : Configuration
        data class EditAccount(val accountId: AccountId) : Configuration
    }

    sealed interface Child {
        data class Main(val component: MainComponent) : Child
        data class Analytics(val component: AnalyticsComponent) : Child
        data class TransactionEditor(val component: TransactionEditorComponent) : Child
        data class AccountEditor(val component: AccountEditorComponent) : Child
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val accountsRepository: AccountsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val transactionsRepository: TransactionsRepository,
    override val isOnline: StateFlow<Boolean>,
    syncStatusRepository: FinanceSyncStatusRepository,
    private val onRetrySyncRequested: () -> Unit,
) : RootComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<RootComponent.Configuration>()

    override val syncStatus: Flow<FinanceSyncStatus> =
        syncStatusRepository.observeStatus()

    override val childStack: Value<ChildStack<RootComponent.Configuration, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = RootComponent.Configuration.Main,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    override fun retrySync() {
        onRetrySyncRequested()
    }

    private fun createChild(
        configuration: RootComponent.Configuration,
        componentContext: ComponentContext,
    ): RootComponent.Child = when (configuration) {
        RootComponent.Configuration.Main -> RootComponent.Child.Main(
            component = DefaultMainComponent(
                componentContext = componentContext,
                accountsRepository = accountsRepository,
                transactionsRepository = transactionsRepository,
                onAnalyticsRequested = {
                    navigation.pushNew(RootComponent.Configuration.Analytics)
                },
                onCreateTransactionRequested = { type ->
                    navigation.pushNew(RootComponent.Configuration.CreateTransaction(type))
                },
                onEditTransactionRequested = { transactionId ->
                    navigation.pushNew(RootComponent.Configuration.EditTransaction(transactionId))
                },
                onCreateAccountRequested = {
                    navigation.pushNew(RootComponent.Configuration.CreateAccount)
                },
                onEditAccountRequested = { accountId ->
                    navigation.pushNew(RootComponent.Configuration.EditAccount(accountId))
                },
            ),
        )
        RootComponent.Configuration.Analytics -> RootComponent.Child.Analytics(
            component = DefaultAnalyticsComponent(
                componentContext = componentContext,
                transactionsRepository = transactionsRepository,
                accountsRepository = accountsRepository,
                onBackRequested = { navigation.pop() },
            ),
        )
        is RootComponent.Configuration.CreateTransaction ->
            RootComponent.Child.TransactionEditor(
                component = createTransactionEditor(
                    componentContext = componentContext,
                    mode = TransactionEditorMode.Create(configuration.type),
                ),
            )
        is RootComponent.Configuration.EditTransaction ->
            RootComponent.Child.TransactionEditor(
                component = createTransactionEditor(
                    componentContext = componentContext,
                    mode = TransactionEditorMode.Edit(configuration.transactionId),
                ),
            )
        RootComponent.Configuration.CreateAccount ->
            RootComponent.Child.AccountEditor(
                component = createAccountEditor(
                    componentContext = componentContext,
                    mode = AccountEditorMode.Create,
                ),
            )
        is RootComponent.Configuration.EditAccount ->
            RootComponent.Child.AccountEditor(
                component = createAccountEditor(
                    componentContext = componentContext,
                    mode = AccountEditorMode.Edit(configuration.accountId),
                ),
            )
    }

    private fun createTransactionEditor(
        componentContext: ComponentContext,
        mode: TransactionEditorMode,
    ): TransactionEditorComponent = DefaultTransactionEditorComponent(
        componentContext = componentContext,
        mode = mode,
        transactionsRepository = transactionsRepository,
        categoriesRepository = categoriesRepository,
        accountsRepository = accountsRepository,
        onDismissRequested = { navigation.pop() },
    )

    private fun createAccountEditor(
        componentContext: ComponentContext,
        mode: AccountEditorMode,
    ): AccountEditorComponent = DefaultAccountEditorComponent(
        componentContext = componentContext,
        mode = mode,
        accountsRepository = accountsRepository,
        onDismissRequested = { navigation.pop() },
    )
}
