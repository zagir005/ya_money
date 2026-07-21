package com.zagirlek.analytics.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zagirlek.analytics.R
import com.zagirlek.analytics.ui.chart.AnalyticsDonutChart
import com.zagirlek.analytics.ui.summary.AnalyticsCategorySummary
import com.zagirlek.analytics.ui.summary.CategoryProgressItem
import com.zagirlek.analytics.ui.summary.toChartSegment
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.components.elements.BaseBottomSheet
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDetailsBottomSheet(
    summary: AnalyticsDetailsSummaryUi,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions

    BaseBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.analytics_details_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensions.space24),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.space24),
        ) {
            AnalyticsDonutChart(
                segments = summary.categories.map(AnalyticsCategorySummary::toChartSegment),
            ) {
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = dimensions.analyticsDetailsListMaxHeight),
                verticalArrangement = Arrangement.spacedBy(dimensions.space24),
            ) {
                items(
                    items = summary.categories,
                    key = AnalyticsCategorySummary::categoryId,
                ) { category ->
                    CategoryProgressItem(
                        category = category,
                        totalAmount = summary.totalAmount,
                    )
                }
            }
        }
    }
}

data class AnalyticsDetailsSummaryUi(
    val total: String,
    val totalAmount: BigDecimal,
    val categories: List<AnalyticsCategorySummary>,
)
