package com.zagirlek.analytics.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.analytics.R
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import com.zagirlek.ui.components.elements.BaseBottomSheet
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarBottomSheet(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onApply: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    key(initialStartDate, initialEndDate) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialStartDate.toUtcEpochMillis(),
            initialSelectedEndDateMillis = initialEndDate.toUtcEpochMillis(),
        )
        val selectedStartDate = pickerState.selectedStartDateMillis?.toUtcLocalDate()
        val selectedEndDate = pickerState.selectedEndDateMillis?.toUtcLocalDate()
        val canApply = selectedStartDate != null && selectedEndDate != null
        val dimensions = YaMoneyDesign.dimensions

        BaseBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            title = stringResource(R.string.analytics_calendar_title),
        ) {
            DateRangePicker(
                state = pickerState,
                title = {},
                showModeToggle = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensions.screenHorizontalPadding,
                        end = dimensions.screenHorizontalPadding,
                        top = dimensions.space16,
                        bottom = dimensions.space24,
                    ),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.analytics_calendar_cancel))
                }

                Button(
                    onClick = {
                        onApply(requireNotNull(selectedStartDate), requireNotNull(selectedEndDate))
                        onDismissRequest()
                    },
                    enabled = canApply,
                ) {
                    Text(stringResource(R.string.analytics_calendar_apply))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CalendarBottomSheetPreview() {
    YaMoneyTheme {
        CalendarBottomSheet(
            initialStartDate = LocalDate.of(2026, 1, 20),
            initialEndDate = LocalDate.of(2026, 2, 5),
            onApply = { _, _ -> },
            onDismissRequest = {},
        )
    }
}

private fun LocalDate.toUtcEpochMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
