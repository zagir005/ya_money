package com.zagirlek.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.formatter.Money
import com.zagirlek.ui.formatter.MoneyFormatter

private val titleTextColor = Color(0xFFA39EA7)

@Composable
fun BalanceCard(
    title: String,
    balance: Money,
    moneyFormatter: MoneyFormatter,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.screenHorizontalPadding,
                vertical = dimensions.space24,
            ),
        verticalArrangement = Arrangement.spacedBy(dimensions.space12),
    ) {
        Text(
            text = title,
            color = titleTextColor,
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = moneyFormatter.format(balance),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displayLarge,
        )
    }
}
