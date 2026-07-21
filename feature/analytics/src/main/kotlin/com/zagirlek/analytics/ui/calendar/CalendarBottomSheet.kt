package com.zagirlek.analytics.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = dimensions.calendarContentMaxHeight)
                    .fillMaxHeight(),
            ) {
                DateRangePicker(
                    state = pickerState,
                    title = {},
                    headline = {
                        CalendarRangeHeadline(
                            startDate = selectedStartDate,
                            endDate = selectedEndDate,
                        )
                    },
                    showModeToggle = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                        modifier = Modifier.widthIn(min = dimensions.calendarActionButtonMinWidth),
                    ) {
                        Text(stringResource(R.string.analytics_calendar_apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarRangeHeadline(
    startDate: LocalDate?,
    endDate: LocalDate?,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensions.screenHorizontalPadding,
                end = dimensions.screenHorizontalPadding,
                bottom = dimensions.space16,
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarDateField(
            value = startDate?.format(calendarDateFormatter).orEmpty(),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "–",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
        )
        CalendarDateField(
            value = endDate?.format(calendarDateFormatter).orEmpty(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CalendarDateField(
    value: String,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    Surface(
        modifier = modifier.heightIn(min = dimensions.calendarDateFieldMinHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = dimensions.calendarDateFieldBorder,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = dimensions.space12),
                style = MaterialTheme.typography.titleMedium,
            )
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

private val calendarDateFormatter = DateTimeFormatter.ofPattern("d MMM uuuu", Locale("ru"))
