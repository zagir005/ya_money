package com.zagirlek.ui.components.elements

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.zagirlek.ui.R
import com.zagirlek.ui.mvi.RetryableErrorEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Composable
fun RetryableErrorSnackbar(
    effects: Flow<RetryableErrorEffect>,
    onRetry: () -> Unit,
): SnackbarHostState {
    val snackbarHostState = remember { SnackbarHostState() }
    val latestOnRetry = rememberUpdatedState(onRetry)
    val retryLabel = stringResource(R.string.retry)

    LaunchedEffect(effects) {
        effects.collect { effect ->
            val result = snackbarHostState.showSnackbar(
                message = effect.message,
                actionLabel = retryLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                latestOnRetry.value()
            }
        }
    }

    return snackbarHostState
}
