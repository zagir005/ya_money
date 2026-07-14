package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ya_money.R
import com.zagirlek.ui.R as CoreUiR
import com.zagirlek.accounts.AccountsScreen
import com.zagirlek.transactions.TransactionsScreen

@Composable
fun MainScreen(component: MainComponent) {
    val stack by component.childStack.subscribeAsState()
    val selectedTab = stack.active.configuration
    val dimensions = YaMoneyDesign.dimensions

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                is MainComponent.Child.Transactions -> TransactionsScreen(
                    type = instance.type,
                    component = instance.component,
                )
                is MainComponent.Child.Accounts -> AccountsScreen(instance.component)
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
    Column(modifier = modifier) {
        HorizontalDivider(
            thickness = 1.dp,
            color = YaMoneyDesign.colors.navigationDivider,
        )

        NavigationBar(
            modifier = Modifier.weight(1f),
            containerColor = YaMoneyDesign.colors.navigationBarContainer,
        ) {
            MainTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = { MainTabIcon(tab) },
                    label = { Text(stringResource(tab.labelRes)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
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
        MainTab.Expenses -> Icon(
            painter = painterResource(CoreUiR.drawable.ic_nav_expense),
            contentDescription = null,
        )
        MainTab.Income -> Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null)
        MainTab.Accounts -> Icon(
            painter = painterResource(CoreUiR.drawable.ic_nav_account),
            contentDescription = null,
        )
    }
}
