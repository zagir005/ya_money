package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.systemdesign.foundation.FinanceDesign
import com.zagirlek.transactions.expenses.ExpensesScreen

@Composable
fun MainScreen(component: MainComponent) {
    val stack by component.childStack.subscribeAsState()
    val selectedTab = stack.active.configuration
    val dimensions = FinanceDesign.dimensions

    Scaffold(
        bottomBar = {
            MainNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = component::select,
                modifier = Modifier.height(dimensions.navigationBarHeight),
            )
        },
    ) { contentPadding ->
        Children(
            stack = component.childStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) { child ->
            when (val instance = child.instance) {
                is MainComponent.Child.Expenses -> ExpensesScreen(instance.component)
                MainComponent.Child.Income -> EmptyScreen("Доходы")
                MainComponent.Child.Accounts -> EmptyScreen("Счета")
            }
        }
    }
}

@Composable
private fun MainNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Text(tab.icon) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun EmptyScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

private val MainTab.label: String
    get() = when (this) {
        MainTab.Expenses -> "Расходы"
        MainTab.Income -> "Доходы"
        MainTab.Accounts -> "Счета"
    }

private val MainTab.icon: String
    get() = when (this) {
        MainTab.Expenses -> "↓"
        MainTab.Income -> "↑"
        MainTab.Accounts -> "▣"
    }
