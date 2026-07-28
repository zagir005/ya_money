package com.zagirlek.ya_money.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zagirlek.finance.api.sync.FinanceSyncStatus
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ya_money.R

internal enum class AppStatusAlertKind {
    Offline,
    UnknownResult,
    Failed,
    Syncing,
    Success,
}

internal data class AppStatusAlert(
    val kind: AppStatusAlertKind,
    val count: Int = 0,
)

internal fun selectAppStatusAlert(
    isOnline: Boolean,
    syncStatus: FinanceSyncStatus,
): AppStatusAlert? = when {
    !isOnline -> AppStatusAlert(AppStatusAlertKind.Offline)
    syncStatus.unknownResultCount > 0 -> AppStatusAlert(
        kind = AppStatusAlertKind.UnknownResult,
        count = syncStatus.unknownResultCount,
    )
    syncStatus.failedCount > 0 -> AppStatusAlert(
        kind = AppStatusAlertKind.Failed,
        count = syncStatus.failedCount,
    )
    syncStatus.pendingCount > 0 -> AppStatusAlert(
        kind = AppStatusAlertKind.Syncing,
        count = syncStatus.pendingCount,
    )
    else -> null
}

@Composable
internal fun AppStatusBanner(
    alert: AppStatusAlert,
    onActionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions
    val colors = alert.colors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.container)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                horizontal = dimensions.space16,
                vertical = dimensions.space8,
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (alert.kind == AppStatusAlertKind.Syncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensions.iconSize),
                color = colors.content,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = when (alert.kind) {
                    AppStatusAlertKind.Offline -> Icons.Outlined.CloudOff
                    AppStatusAlertKind.UnknownResult,
                    AppStatusAlertKind.Failed,
                    -> Icons.Outlined.ErrorOutline
                    AppStatusAlertKind.Success -> Icons.Outlined.CheckCircle
                    AppStatusAlertKind.Syncing -> error("Handled above")
                },
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconSize),
                tint = colors.content,
            )
        }
        Text(
            text = alert.message(),
            modifier = Modifier.weight(1f),
            color = colors.content,
            style = MaterialTheme.typography.bodyMedium,
        )
        alert.actionLabelRes?.let { actionLabelRes ->
            TextButton(onClick = onActionClicked) {
                Text(
                    text = stringResource(actionLabelRes),
                    color = colors.content,
                )
            }
        }
    }
}

@Composable
private fun AppStatusAlert.message(): String = when (kind) {
    AppStatusAlertKind.Offline -> stringResource(R.string.sync_status_offline)
    AppStatusAlertKind.UnknownResult -> stringResource(
        R.string.sync_status_unknown,
        count,
    )
    AppStatusAlertKind.Failed -> stringResource(
        R.string.sync_status_failed,
        count,
    )
    AppStatusAlertKind.Syncing -> stringResource(
        R.string.sync_status_pending,
        count,
    )
    AppStatusAlertKind.Success -> stringResource(R.string.sync_status_success)
}

private val AppStatusAlert.actionLabelRes: Int?
    @StringRes get() = when (kind) {
        AppStatusAlertKind.UnknownResult,
        AppStatusAlertKind.Failed,
        -> R.string.sync_action_ok
        AppStatusAlertKind.Offline,
        AppStatusAlertKind.Syncing,
        AppStatusAlertKind.Success,
        -> null
    }

@Composable
private fun AppStatusAlert.colors(): AppStatusColors = when (kind) {
    AppStatusAlertKind.Offline -> AppStatusColors(
        container = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AppStatusAlertKind.UnknownResult,
    AppStatusAlertKind.Failed,
    -> AppStatusColors(
        container = MaterialTheme.colorScheme.error,
        content = MaterialTheme.colorScheme.onError,
    )
    AppStatusAlertKind.Syncing -> AppStatusColors(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    AppStatusAlertKind.Success -> AppStatusColors(
        container = SuccessContainer,
        content = OnSuccessContainer,
    )
}

private data class AppStatusColors(
    val container: Color,
    val content: Color,
)

private val SuccessContainer = Color(0xFF2E7D32)
private val OnSuccessContainer = Color.White
