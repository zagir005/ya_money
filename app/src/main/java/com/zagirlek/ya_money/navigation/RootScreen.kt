package com.zagirlek.ya_money.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.zagirlek.analytics.AnalyticsScreen
import com.zagirlek.accounts.AccountEditorScreen
import com.zagirlek.finance.api.sync.FinanceSyncStatus
import com.zagirlek.transactions.TransactionEditorScreen
import kotlinx.coroutines.delay

@Composable
fun RootScreen(component: RootComponent) {
    val isOnline by component.isOnline.collectAsState()
    val syncStatus by component.syncStatus.collectAsState(
        initial = FinanceSyncStatus(),
    )
    val stack = component.childStack.subscribeAsState().value
    val sourceAlert = selectAppStatusAlert(
        isOnline = isOnline,
        syncStatus = syncStatus,
    )
    val bannerState = rememberAppStatusBannerState(sourceAlert)

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = bannerState.isVisible,
            enter = slideInVertically(initialOffsetY = { height -> -height }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { height -> -height }) + fadeOut(),
        ) {
            bannerState.alert?.let { alert ->
                AppStatusBanner(
                    alert = alert,
                    onActionClicked = bannerState::dismiss,
                )
            }
        }
        RootChild(
            active = stack.active.instance,
            backStack = stack.backStack.map { it.instance },
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .then(
                    if (bannerState.isVisible) {
                        Modifier.consumeWindowInsets(WindowInsets.statusBars)
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@Composable
private fun rememberAppStatusBannerState(
    sourceAlert: AppStatusAlert?,
): AppStatusBannerState {
    val state = remember { AppStatusBannerState() }

    LaunchedEffect(sourceAlert) {
        when (sourceAlert?.kind) {
            AppStatusAlertKind.Syncing -> {
                state.wasSyncing = true
                state.dismissedError = null
                state.show(sourceAlert)
            }
            AppStatusAlertKind.Failed,
            AppStatusAlertKind.UnknownResult,
            -> {
                state.wasSyncing = false
                if (state.dismissedError != sourceAlert) {
                    state.show(sourceAlert)
                }
            }
            AppStatusAlertKind.Offline -> {
                state.wasSyncing = false
                state.show(sourceAlert)
            }
            AppStatusAlertKind.Success -> state.show(sourceAlert)
            null -> {
                if (state.wasSyncing) {
                    state.wasSyncing = false
                    state.show(AppStatusAlert(AppStatusAlertKind.Success))
                    delay(SuccessVisibleMillis)
                }
                state.hide()
                delay(BannerExitMillis)
                state.clearIfHidden()
            }
        }
    }

    return state
}

private class AppStatusBannerState {
    var alert by mutableStateOf<AppStatusAlert?>(null)
        private set
    var isVisible by mutableStateOf(false)
        private set
    var wasSyncing = false
    var dismissedError: AppStatusAlert? = null

    fun show(value: AppStatusAlert) {
        alert = value
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    fun clearIfHidden() {
        if (!isVisible) alert = null
    }

    fun dismiss() {
        alert?.takeIf { value ->
            value.kind == AppStatusAlertKind.Failed ||
                value.kind == AppStatusAlertKind.UnknownResult
        }?.let { value ->
            dismissedError = value
            isVisible = false
        }
    }
}

private const val SuccessVisibleMillis = 1_500L
private const val BannerExitMillis = 300L

@Composable
private fun RootChild(
    active: RootComponent.Child,
    backStack: List<RootComponent.Child>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (active) {
            is RootComponent.Child.Main -> MainScreen(active.component)
            is RootComponent.Child.Analytics -> AnalyticsScreen(active.component)
            is RootComponent.Child.TransactionEditor -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainBackground(backStack)
                    TransactionEditorScreen(active.component)
                }
            }
            is RootComponent.Child.AccountEditor -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainBackground(backStack)
                    AccountEditorScreen(active.component)
                }
            }
        }
    }
}

@Composable
private fun MainBackground(backStack: List<RootComponent.Child>) {
    val backgroundMain = backStack
        .lastOrNull { child -> child is RootComponent.Child.Main }
        as? RootComponent.Child.Main
    backgroundMain?.let { MainScreen(it.component) }
}
