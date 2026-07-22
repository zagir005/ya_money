package com.zagirlek.ui.components.finance

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.R

@Composable
fun FinanceScaffold(
    date: String,
    onDateClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FinanceTopAppBar(
                date = date,
                onDateClick = onDateClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.size(dimensions.fabSize),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.finance_add_content_description),
                )
            }
        },
    ) { contentPadding -> content(contentPadding) }
}
