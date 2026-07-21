package com.zagirlek.ui.components.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.R

@Composable
fun ErrorContent(
    message: String,
    onRetryClicked: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(YaMoneyDesign.dimensions.space12),
        ) {
            Text(text = message)
            Button(onClick = onRetryClicked) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}
