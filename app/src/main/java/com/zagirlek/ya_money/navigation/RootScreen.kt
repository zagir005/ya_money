package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.analytics.AnalyticsScreen
import com.zagirlek.accounts.AccountEditorScreen
import com.zagirlek.transactions.TransactionEditorScreen

@Composable
fun RootScreen(component: RootComponent) {
    val stack = component.childStack.subscribeAsState().value
    when (val active = stack.active.instance) {
        is RootComponent.Child.Main -> MainScreen(active.component)
        is RootComponent.Child.Analytics -> AnalyticsScreen(active.component)
        is RootComponent.Child.TransactionEditor -> {
            Box(modifier = Modifier.fillMaxSize()) {
                MainBackground(stack.backStack.map { it.instance })
                TransactionEditorScreen(active.component)
            }
        }
        is RootComponent.Child.AccountEditor -> {
            Box(modifier = Modifier.fillMaxSize()) {
                MainBackground(stack.backStack.map { it.instance })
                AccountEditorScreen(active.component)
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
