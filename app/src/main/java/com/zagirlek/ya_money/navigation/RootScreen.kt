package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.analytics.AnalyticsScreen
import com.zagirlek.transactions.TransactionEditorScreen

@Composable
fun RootScreen(component: RootComponent) {
    val stack = component.childStack.subscribeAsState().value
    when (val active = stack.active.instance) {
        is RootComponent.Child.Main -> MainScreen(active.component)
        is RootComponent.Child.Analytics -> AnalyticsScreen(active.component)
        is RootComponent.Child.TransactionEditor -> {
            Box(modifier = Modifier.fillMaxSize()) {
                val backgroundMain = stack.backStack
                    .lastOrNull()
                    ?.instance as? RootComponent.Child.Main
                backgroundMain?.let { MainScreen(it.component) }
                TransactionEditorScreen(active.component)
            }
        }
    }
}
