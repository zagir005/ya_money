package com.zagirlek.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import com.zagirlek.ui.R

private val CalendarButtonContainerColor = Color(0xFFF4EFF8)

@Composable
fun FinanceTopAppBar(
    date: String,
    analyticsContentDescription: String,
    settingsContentDescription: String,
    onDateClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.topBarHeight)
            .padding(horizontal = dimensions.screenHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FinanceCalendarButton(
            date = date,
            onClick = onDateClick,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAnalyticsClick) {
                Icon(
                    painter = painterResource(R.drawable.analytics),
                    contentDescription = analyticsContentDescription,
                    modifier = Modifier.size(dimensions.iconSize),
                )
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(R.drawable.settings),
                    contentDescription = settingsContentDescription,
                    modifier = Modifier.size(dimensions.iconSize),
                )
            }
        }
    }
}

@Composable
fun FinanceCalendarButton(
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    Button(
        onClick = onClick,
        modifier = modifier.wrapContentWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = CalendarButtonContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensions.space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.calendar),
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconSize)
            )

            Text(
                text = date,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview
@Composable
private fun FinanceCalendarButtonPreview() {
    YaMoneyTheme { 
        FinanceTopAppBar(
            date = "12 июня",
            analyticsContentDescription = "Аналитика",
            settingsContentDescription = "Настройки",
            onDateClick = {},
            onAnalyticsClick = {},
            onSettingsClick = {},
        )
    }
}
