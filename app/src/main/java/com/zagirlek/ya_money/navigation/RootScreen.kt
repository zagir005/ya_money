package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.zagirlek.analytics.AnalyticsScreen

@Composable
fun RootScreen(component: RootComponent) {
    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
    ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Main -> MainScreen(instance.component)
            is RootComponent.Child.Analytics -> AnalyticsScreen(instance.component)
        }
    }
}
