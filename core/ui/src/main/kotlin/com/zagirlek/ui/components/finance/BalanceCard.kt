package com.zagirlek.ui.components.finance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.zagirlek.systemdesign.theme.YaMoneyDesign

private val titleTextColor = Color(0xFFA39EA7)

@Composable
fun BalanceCard(
    title: String,
    balance: String,
    modifier: Modifier = Modifier
) {
    val dimensions = YaMoneyDesign.dimensions

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.space20
            )
            .padding(top = dimensions.space12, bottom = dimensions.space32)
    ) {
        Text(
            text = title,
            color = titleTextColor,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = balance,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displayLarge,
        )
    }
}
