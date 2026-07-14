package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ya_money.R
import com.zagirlek.transactions.expenses.ExpensesScreen

@Composable
fun MainScreen(component: MainComponent) {
    val stack by component.childStack.subscribeAsState()
    val selectedTab = stack.active.configuration
    val dimensions = YaMoneyDesign.dimensions

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
                MainComponent.Child.Income -> EmptyScreen(R.string.income_placeholder)
                MainComponent.Child.Accounts -> EmptyScreen(R.string.accounts_placeholder)
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
                icon = { MainTabIcon(tab) },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}

@Composable
private fun EmptyScreen(@StringRes titleRes: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
    }
}

private val MainTab.labelRes: Int
    get() = when (this) {
        MainTab.Expenses -> R.string.tab_expenses
        MainTab.Income -> R.string.tab_income
        MainTab.Accounts -> R.string.tab_accounts
    }

@Composable
private fun MainTabIcon(tab: MainTab) {
    when (tab) {
        MainTab.Expenses -> Icon(Icons.AutoMirrored.Outlined.TrendingDown, contentDescription = null)
        MainTab.Income -> Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null)
        MainTab.Accounts -> Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null)
    }
}