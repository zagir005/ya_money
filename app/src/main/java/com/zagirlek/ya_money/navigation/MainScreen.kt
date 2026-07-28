package com.zagirlek.ya_money.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ya_money.R
import com.zagirlek.ui.R as CoreUiR
import com.zagirlek.accounts.AccountsScreen
import com.zagirlek.transactions.TransactionsScreen

@Composable
fun MainScreen(component: MainComponent) {
    val pages by component.childPages.subscribeAsState()
    val selectedTab = pages.items[pages.selectedIndex].configuration

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MainNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = component::select,
            )
        },
    ) { contentPadding ->
        pages.items
            .getOrNull(pages.selectedIndex)
            ?.instance
            ?.let { child ->
                MainPage(
                    child = child,
                    component = component,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                )
            }
    }
}

@Composable
private fun MainPage(
    child: MainComponent.Child,
    component: MainComponent,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (child) {
            is MainComponent.Child.Transactions -> TransactionsScreen(
                type = child.type,
                component = child.component,
                onAnalyticsClick = component::openAnalytics,
            )
            is MainComponent.Child.Accounts -> AccountsScreen(
                component = child.component,
                onAnalyticsClick = component::openAnalytics,
            )
        }
    }
}

@Composable
private fun MainNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    Column(
        modifier = modifier.background(YaMoneyDesign.colors.navigationBarContainer),
    ) {
        HorizontalDivider(
            thickness = dimensions.navigationDividerThickness,
            color = YaMoneyDesign.colors.navigationDivider,
        )

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimensions.navigationBarHeight),
            containerColor = YaMoneyDesign.colors.navigationBarContainer,
        ) {
            MainTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    alwaysShowLabel = true,
                    icon = {
                        MainTabIcon(
                            tab = tab,
                            modifier = Modifier.size(dimensions.navigationIconSize),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
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
private fun MainTabIcon(
    tab: MainTab,
    modifier: Modifier = Modifier,
) {
    when (tab) {
        MainTab.Expenses -> Icon(
            painter = painterResource(CoreUiR.drawable.ic_nav_expense),
            contentDescription = null,
            modifier = modifier,
        )
        MainTab.Income -> Icon(
            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
            contentDescription = null,
            modifier = modifier,
        )
        MainTab.Accounts -> Icon(
            painter = painterResource(CoreUiR.drawable.ic_nav_account),
            contentDescription = null,
            modifier = modifier,
        )
    }
}
