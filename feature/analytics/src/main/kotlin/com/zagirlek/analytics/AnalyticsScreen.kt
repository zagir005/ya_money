@file:OptIn(ExperimentalMaterial3Api::class)

package com.zagirlek.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zagirlek.analytics.ui.calendar.CalendarBottomSheet
import com.zagirlek.analytics.ui.chart.AnalyticsDonutChart
import com.zagirlek.analytics.ui.details.AnalyticsDetailsBottomSheet
import com.zagirlek.analytics.ui.details.AnalyticsDetailsSummaryUi
import com.zagirlek.analytics.ui.summary.AnalyticsCategorySummary
import com.zagirlek.analytics.ui.summary.color
import com.zagirlek.analytics.ui.summary.toChartSegment
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.components.elements.BaseBottomSheet
import com.zagirlek.ui.components.elements.LoadingContent
import com.zagirlek.ui.components.elements.NetworkErrorAlert
import com.zagirlek.ui.components.elements.SelectionListItem
import com.zagirlek.ui.components.elements.SelectionListItemControl
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList

@Composable
fun AnalyticsScreen(component: AnalyticsComponent) {
    val state by component.state.collectAsState()
    var activeSheet by remember { mutableStateOf<AnalyticsFilterSheet?>(null) }
    var showChartDetails by remember { mutableStateOf(false) }
    LaunchedEffect(component) {
        component.effects
            .collect { effect ->
                when (effect) {
                    is AnalyticsEffect.ShowFilterSheet -> activeSheet = effect.sheet
                    AnalyticsEffect.ShowChartDetails -> showChartDetails = true
                }
            }
    }

    AnalyticsContent(
        state = state,
        onIntent = component::accept,
    )

    when (val currentState = state) {
        is AnalyticsState.Content -> AnalyticsFilterSheets(
            activeSheet = activeSheet,
            period = currentState.period,
            filters = currentState.filters,
            options = currentState.filterOptions,
            onIntent = component::accept,
            onDismissRequest = { activeSheet = null },
        )
        is AnalyticsState.Empty -> AnalyticsFilterSheets(
            activeSheet = activeSheet,
            period = currentState.period,
            filters = currentState.filters,
            options = currentState.filterOptions,
            onIntent = component::accept,
            onDismissRequest = { activeSheet = null },
        )
        is AnalyticsState.Error -> AnalyticsFilterSheets(
            activeSheet = activeSheet,
            period = currentState.period,
            filters = currentState.filters,
            options = AnalyticsFilterOptions(
                categories = emptyList(),
                accounts = emptyList(),
            ),
            onIntent = component::accept,
            onDismissRequest = { activeSheet = null },
        )
        else -> Unit
    }

    when (val currentState = state) {
        is AnalyticsState.Content -> if (showChartDetails) {
            AnalyticsDetailsBottomSheet(
                summary = currentState.summary.toDetailsSummary(),
                onDismissRequest = { showChartDetails = false },
            )
        }
        else -> Unit
    }
}

@Composable
fun AnalyticsContent(
    state: AnalyticsState,
    onIntent: (AnalyticsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            AnalyticsTopBar(onBackClicked = { onIntent(AnalyticsIntent.BackClicked) })

            when (state) {
                is AnalyticsState.Loading -> AnalyticsStateContent { LoadingContent() }
                is AnalyticsState.Error -> AnalyticsOverview(
                    period = state.period,
                    filters = state.filters,
                    summary = AnalyticsSummaryUi(total = "", categories = emptyList()),
                    filterOptions = AnalyticsFilterOptions(
                        categories = emptyList(),
                        accounts = emptyList(),
                    ),
                    transactionItems = emptyList(),
                    isRefreshing = false,
                    historyError = state.error,
                    onIntent = onIntent,
                )
                is AnalyticsState.Empty -> AnalyticsOverview(
                    period = state.period,
                    filters = state.filters,
                    summary = state.summary,
                    filterOptions = state.filterOptions,
                    transactionItems = emptyList(),
                    isRefreshing = state.isRefreshing,
                    historyError = state.historyError,
                    onIntent = onIntent,
                )
                is AnalyticsState.Content -> AnalyticsOverview(
                    period = state.period,
                    filters = state.filters,
                    summary = state.summary,
                    filterOptions = state.filterOptions,
                    transactionItems = state.transactionItems,
                    isRefreshing = state.isRefreshing,
                    historyError = state.historyError,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun AnalyticsTopBar(onBackClicked: () -> Unit) {
    val dimensions = YaMoneyDesign.dimensions

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.topBarHeight)
            .padding(horizontal = dimensions.screenHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.space8),
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.analytics_back),
            )
        }
        Text(
            text = stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun AnalyticsOverview(
    period: TransactionPeriod,
    filters: AnalyticsFilters,
    summary: AnalyticsSummaryUi,
    filterOptions: AnalyticsFilterOptions,
    transactionItems: List<AnalyticsTransactionItemUi>,
    isRefreshing: Boolean,
    historyError: com.zagirlek.finance.api.error.NetworkError?,
    onIntent: (AnalyticsIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onIntent(AnalyticsIntent.RefreshRequested) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = dimensions.space32),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensions.screenHorizontalPadding,
                            end = dimensions.screenHorizontalPadding,
                            top = dimensions.space24,
                            bottom = dimensions.space24,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensions.space20),
                ) {
                AnalyticsDonutChart(
                    segments = summary.categories.map(AnalyticsCategorySummary::toChartSegment),
                    modifier = Modifier.clickable(
                        enabled = summary.categories.isNotEmpty(),
                        onClick = { onIntent(AnalyticsIntent.ChartClicked) },
                    ),
                ) {
                    if (summary.categories.isEmpty()) {
                        Text(
                            text = stringResource(R.string.analytics_no_operations),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.analytics_total_for_period),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = summary.total,
                                style = MaterialTheme.typography.displayLarge,
                            )
                        }
                    }
                }

                    AnalyticsLegend(categories = summary.categories)
                }
            }

            item {
                AnalyticsFiltersSection(
                    period = period,
                    filters = filters,
                    options = filterOptions,
                    onIntent = onIntent,
                )
            }

            if (historyError != null) {
                item {
                    NetworkErrorAlert(
                        title = historyError.title,
                        message = historyError.message.orEmpty(),
                        onRetryClicked = { onIntent(AnalyticsIntent.RetryClicked) },
                    )
                }
            }
            if (transactionItems.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.analytics_transactions),
                        modifier = Modifier.padding(
                            start = dimensions.screenHorizontalPadding,
                            end = dimensions.screenHorizontalPadding,
                            top = dimensions.space32,
                            bottom = dimensions.space12,
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                items(transactionItems, key = AnalyticsTransactionItemUi::id) { item ->
                    AnalyticsTransactionItem(item = item)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsLegend(categories: List<AnalyticsCategorySummary>) {
    val dimensions = YaMoneyDesign.dimensions

    if (categories.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensions.space24,
                Alignment.CenterHorizontally,
            ),
        ) {
            categories.take(3).forEach { category ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.analyticsCategoryColorSize)
                            .clip(CircleShape)
                            .background(category.color),
                    )
                    Text(
                        text = category.categoryName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsFiltersSection(
    period: TransactionPeriod,
    filters: AnalyticsFilters,
    options: AnalyticsFilterOptions,
    onIntent: (AnalyticsIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    Column(modifier = Modifier.fillMaxWidth()) {
        AnalyticsFilterRow(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            label = stringResource(R.string.analytics_filter_type),
            value = filters.type.toLabel(),
            onClick = { onIntent(AnalyticsIntent.TypeFilterClicked) },
        )
        HorizontalDivider()
        AnalyticsFilterRow(
            icon = Icons.Filled.CalendarMonth,
            label = stringResource(R.string.analytics_filter_period),
            value = period.format(),
            onClick = { onIntent(AnalyticsIntent.PeriodFilterClicked) },
        )
        HorizontalDivider()
        AnalyticsFilterRow(
            icon = Icons.Filled.Sell,
            label = stringResource(R.string.analytics_filter_categories),
            value = filters.categoryIds.toCategoriesLabel(options.categories),
            onClick = { onIntent(AnalyticsIntent.CategoryFilterClicked) },
        )
        HorizontalDivider()
        AnalyticsFilterRow(
            icon = Icons.Filled.CreditCard,
            label = stringResource(R.string.analytics_filter_account),
            value = filters.accountId.toAccountLabel(options.accounts),
            onClick = { onIntent(AnalyticsIntent.AccountFilterClicked) },
        )
        HorizontalDivider()
    }
}

@Composable
private fun AnalyticsFilterRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.listItemMinHeight)
            .padding(horizontal = dimensions.screenHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.space16),
    ) {
        Surface(
            modifier = Modifier.size(dimensions.listLeadingSize),
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null)
            }
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        AnalyticsFilterValue(value = value, onClick = onClick)
    }
}

@Composable
private fun AnalyticsFilterValue(value: String, onClick: () -> Unit) {
    val dimensions = YaMoneyDesign.dimensions

    Surface(
        onClick = onClick,
        modifier = Modifier.widthIn(max = 230.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.background,
    ) {
        Text(
            text = value,
            modifier = Modifier.padding(
                horizontal = dimensions.space12,
                vertical = dimensions.space4,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AnalyticsTransactionItem(item: AnalyticsTransactionItemUi) {
    val dimensions = YaMoneyDesign.dimensions

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.screenHorizontalPadding,
                    vertical = dimensions.space12,
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensions.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.listLeadingSize)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.emoji)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = item.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = item.amount,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = dimensions.screenHorizontalPadding))
    }
}

@Composable
private fun AnalyticsFilterSheets(
    activeSheet: AnalyticsFilterSheet?,
    period: TransactionPeriod,
    filters: AnalyticsFilters,
    options: AnalyticsFilterOptions,
    onIntent: (AnalyticsIntent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (activeSheet) {
        AnalyticsFilterSheet.Type -> TypeFilterBottomSheet(
            selectedType = filters.type,
            onApply = {
                onIntent(AnalyticsIntent.TypeApplied(it))
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest,
        )
        AnalyticsFilterSheet.Period -> PeriodFilterBottomSheet(
            period = period,
            onCustomPeriodSelected = { onIntent(AnalyticsIntent.CustomPeriodClicked) },
            onPresetSelected = {
                onIntent(AnalyticsIntent.PeriodPresetApplied(it))
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest,
        )
        AnalyticsFilterSheet.Calendar -> CalendarBottomSheet(
            initialStartDate = period.startDate,
            initialEndDate = period.endDate,
            onApply = { startDate, endDate ->
                onIntent(AnalyticsIntent.PeriodApplied(TransactionPeriod(startDate, endDate)))
            },
            onDismissRequest = onDismissRequest,
        )
        AnalyticsFilterSheet.Categories -> CategoriesFilterBottomSheet(
            options = options.categories,
            selectedCategoryIds = filters.categoryIds,
            onApply = {
                onIntent(AnalyticsIntent.CategoriesApplied(it))
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest,
        )
        AnalyticsFilterSheet.Account -> AccountFilterBottomSheet(
            options = options.accounts,
            selectedAccountId = filters.accountId,
            onAccountSelected = {
                onIntent(AnalyticsIntent.AccountApplied(it))
                onDismissRequest()
            },
            onDismissRequest = onDismissRequest,
        )
        null -> Unit
    }
}

@Composable
private fun PeriodFilterBottomSheet(
    period: TransactionPeriod,
    onCustomPeriodSelected: () -> Unit,
    onPresetSelected: (AnalyticsPeriodPreset) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val today = LocalDate.now()
    val selectedPreset = AnalyticsPeriodPreset.entries.firstOrNull { it.toPeriod(today) == period }

    BaseBottomSheet(
        title = stringResource(R.string.analytics_period_title),
        onDismissRequest = onDismissRequest,
    ) {
        SelectionListItem(
            title = stringResource(R.string.analytics_period_custom),
            subtitle = period.format(),
            control = SelectionListItemControl.Checkmark(selectedPreset == null),
            onClick = onCustomPeriodSelected,
        )
        HorizontalDivider()

        AnalyticsPeriodPreset.entries.forEach { preset ->
            SelectionListItem(
                title = preset.label(),
                control = SelectionListItemControl.Checkmark(selectedPreset == preset),
                onClick = { onPresetSelected(preset) },
            )
            if (preset != AnalyticsPeriodPreset.Year) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun TypeFilterBottomSheet(
    selectedType: TransactionType?,
    onApply: (TransactionType?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var draftType by remember(selectedType) { mutableStateOf(selectedType) }
    val dimensions = YaMoneyDesign.dimensions

    BaseBottomSheet(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.analytics_filter_type),
    ) {
        SelectionListItem(
            title = stringResource(R.string.analytics_expenses),
            control = SelectionListItemControl.CircularCheckmark(draftType == TransactionType.Expense),
            controlSize = dimensions.compactSelectionControlSize,
            onClick = { draftType = TransactionType.Expense },
        )
        HorizontalDivider()
        SelectionListItem(
            title = stringResource(R.string.analytics_income),
            control = SelectionListItemControl.CircularCheckmark(draftType == TransactionType.Income),
            controlSize = dimensions.compactSelectionControlSize,
            onClick = { draftType = TransactionType.Income },
        )
        HorizontalDivider()
        SelectionListItem(
            title = stringResource(R.string.analytics_all),
            control = SelectionListItemControl.CircularCheckmark(draftType == null),
            controlSize = dimensions.compactSelectionControlSize,
            onClick = { draftType = null },
        )
        HorizontalDivider()
        Button(
            onClick = { onApply(draftType) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.screenHorizontalPadding,
                    vertical = dimensions.space16,
                ),
        ) {
            Text(stringResource(R.string.analytics_type_done))
        }
    }
}

@Composable
private fun CategoriesFilterBottomSheet(
    options: List<AnalyticsCategoryOptionUi>,
    selectedCategoryIds: Set<Int>?,
    onApply: (Set<Int>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var draftCategoryIds by remember(options, selectedCategoryIds) {
        mutableStateOf<Set<Int>>(selectedCategoryIds ?: options.map { it.id }.toSet())
    }

    BaseBottomSheet(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.analytics_categories_sheet_title),
    ) {
        if (options.isEmpty()) {
            Text(
                text = stringResource(R.string.analytics_categories_empty),
                modifier = Modifier.padding(
                    start = YaMoneyDesign.dimensions.screenHorizontalPadding,
                    end = YaMoneyDesign.dimensions.screenHorizontalPadding,
                    bottom = YaMoneyDesign.dimensions.space24,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            options.forEachIndexed { index, option ->
                SelectionListItem(
                    title = option.name,
                    leadingEmoji = option.emoji,
                    control = SelectionListItemControl.Checkbox(option.id in draftCategoryIds),
                    onClick = {
                        draftCategoryIds = draftCategoryIds.toggle(option.id)
                    },
                )
                if (index != options.lastIndex) {
                    HorizontalDivider()
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = YaMoneyDesign.dimensions.screenHorizontalPadding,
                        vertical = YaMoneyDesign.dimensions.space16,
                    ),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(onClick = { onApply(draftCategoryIds) }) {
                    Text(stringResource(R.string.analytics_categories_apply))
                }
            }
        }
    }
}

@Composable
private fun AccountFilterBottomSheet(
    options: List<AnalyticsAccountOptionUi>,
    selectedAccountId: AccountId?,
    onAccountSelected: (AccountId?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BaseBottomSheet(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.analytics_accounts_sheet_title),
        fillContentHeight = true,
    ) {
        SelectionListItem(
            title = stringResource(R.string.analytics_all_accounts),
            leadingEmoji = "💳",
            control = SelectionListItemControl.Checkmark(selectedAccountId == null),
            onClick = { onAccountSelected(null) },
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(
                items = options,
                key = { it.id.value },
            ) { option ->
                SelectionListItem(
                    title = option.name,
                    leadingEmoji = option.emoji,
                    control = SelectionListItemControl.Checkmark(option.id == selectedAccountId),
                    onClick = { onAccountSelected(option.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AnalyticsStateContent(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun TransactionType?.toLabel(): String = when (this) {
    TransactionType.Expense -> stringResource(R.string.analytics_expenses)
    TransactionType.Income -> stringResource(R.string.analytics_income)
    null -> stringResource(R.string.analytics_all)
}

@Composable
private fun AnalyticsPeriodPreset.label(): String = when (this) {
    AnalyticsPeriodPreset.Week -> stringResource(R.string.analytics_period_week)
    AnalyticsPeriodPreset.Month -> stringResource(R.string.analytics_period_month)
    AnalyticsPeriodPreset.Quarter -> stringResource(R.string.analytics_period_quarter)
    AnalyticsPeriodPreset.Year -> stringResource(R.string.analytics_period_year)
}

@Composable
private fun Set<Int>?.toCategoriesLabel(categories: List<AnalyticsCategoryOptionUi>): String {
    if (this == null) return stringResource(R.string.analytics_all_categories)
    if (isEmpty()) return stringResource(R.string.analytics_categories_none)
    val selectedNames = categories.filter { it.id in this }.map { it.name }
    return if (selectedNames.isNotEmpty()) {
        selectedNames.joinToString()
    } else {
        stringResource(R.string.analytics_all_categories)
    }
}

@Composable
private fun AccountId?.toAccountLabel(
    accounts: List<AnalyticsAccountOptionUi>,
): String = accounts.firstOrNull { it.id == this }?.name
    ?: stringResource(R.string.analytics_all_accounts)

private fun TransactionPeriod.format(): String = "${startDate.format(filterDateFormatter)} – ${endDate.format(filterDateFormatter)}"

private fun Set<Int>.toggle(categoryId: Int): Set<Int> =
    if (categoryId in this) this - categoryId else this + categoryId

private val filterDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun AnalyticsSummaryUi.toDetailsSummary(): AnalyticsDetailsSummaryUi = AnalyticsDetailsSummaryUi(
    total = total,
    totalAmount = categories.sumOf(AnalyticsCategorySummary::amount),
    categories = categories,
)
