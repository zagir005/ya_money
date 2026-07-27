package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.analytics.AnalyticsScreen
import com.zagirlek.accounts.AccountEditorScreen
import com.zagirlek.finance.api.sync.FinanceSyncStatus
import com.zagirlek.transactions.TransactionEditorScreen

@Composable
fun RootScreen(component: RootComponent) {
    val isOnline by component.isOnline.collectAsState()
    val syncStatus by component.syncStatus.collectAsState(
        initial = FinanceSyncStatus(),
    )
    val stack = component.childStack.subscribeAsState().value
    val alert = selectAppStatusAlert(
        isOnline = isOnline,
        syncStatus = syncStatus,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        alert?.let { value ->
            AppStatusBanner(
                alert = value,
                onActionClicked = component::retrySync,
            )
        }
        RootChild(
            active = stack.active.instance,
            backStack = stack.backStack.map { it.instance },
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .then(
                    if (alert != null) {
                        Modifier.consumeWindowInsets(WindowInsets.statusBars)
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@Composable
private fun RootChild(
    active: RootComponent.Child,
    backStack: List<RootComponent.Child>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (active) {
            is RootComponent.Child.Main -> MainScreen(active.component)
            is RootComponent.Child.Analytics -> AnalyticsScreen(active.component)
            is RootComponent.Child.TransactionEditor -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainBackground(backStack)
                    TransactionEditorScreen(active.component)
                }
            }
            is RootComponent.Child.AccountEditor -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainBackground(backStack)
                    AccountEditorScreen(active.component)
                }
            }
        }
    }
}

@Composable
private fun MainBackground(backStack: List<RootComponent.Child>) {
    val backgroundMain = backStack
        .lastOrNull { child -> child is RootComponent.Child.Main }
        as? RootComponent.Child.Main
    backgroundMain?.let { MainScreen(it.component) }
}
