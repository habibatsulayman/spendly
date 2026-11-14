package com.example.daily_expenses.ui.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.model.MonthlyAggregate
import com.example.daily_expenses.ui.home.HomeViewModel
import com.example.daily_expenses.ui.home.MonthlyBudgetPlan
import com.example.daily_expenses.ui.home.calculateWantsAndNeedsAllocation
import com.example.daily_expenses.ui.home.formatAmount
import com.example.daily_expenses.ui.theme.SpendlyThemeExtras
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun SummaryScreen(
    summaryViewModel: SummaryViewModel,
    homeViewModel: HomeViewModel
) {
    val selectedMonth by homeViewModel.selectedMonth.collectAsState()
    val budgetPlan by homeViewModel.budgetPlan.collectAsState()
    val budgetVersion by homeViewModel.budgetVersion.collectAsState()
    val needsItems by homeViewModel.needsItems.collectAsState()
    val wantsItems by homeViewModel.wantsItems.collectAsState()
    val dailyNeedsSpent by homeViewModel.dailyNeedsSpent.collectAsState()
    val dailyWantsSpent by homeViewModel.dailyWantsSpent.collectAsState()

    val needsRaw = needsItems.filter { it.isSorted }.sumOf { it.amount } + dailyNeedsSpent
    val wantsRaw = wantsItems.filter { it.isSorted }.sumOf { it.amount } + dailyWantsSpent
    val allocation = calculateWantsAndNeedsAllocation(
        needsBudget = budgetPlan.needsBudget,
        wantsBudget = budgetPlan.wantsBudget,
        needsSortedRaw = needsRaw,
        wantsSortedRaw = wantsRaw
    )
    val needsSorted = allocation.effectiveNeedsSorted
    val wantsSorted = allocation.effectiveWantsCapped
    val investSorted = if (budgetPlan.investmentSorted) budgetPlan.investmentBudget else 0.0

    LaunchedEffect(selectedMonth, investSorted, budgetPlan.investmentSortedAt, budgetVersion) {
        summaryViewModel.selectMonth(selectedMonth.year, selectedMonth.month)
        summaryViewModel.setInvestmentSortedAmount(investSorted, budgetPlan.investmentSortedAt)
        summaryViewModel.setPastMonthInvestmentProvider(budgetVersion) { year, month, loggedIncome ->
            homeViewModel.getSortedInvestmentForMonth(year, month, loggedIncome)
        }
    }

    val summary by summaryViewModel.monthlySummary.collectAsState()
    val pastMonths by summaryViewModel.pastMonths.collectAsState()
    val extras = SpendlyThemeExtras.colors

    var showTransactionsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar: "Insights" ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ── Month Selector: < October 2026 > ────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { homeViewModel.selectPreviousMonth() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = selectedMonth.label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { homeViewModel.selectNextMonth() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Spent this month Card (Tap to view monthly transactions) ─
            item {
                Surface(
                    onClick = { showTransactionsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spent this month",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View transactions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₦${formatAmount(summary.totalAmount)}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${summary.transactionCount} transaction${if (summary.transactionCount != 1) "s" else ""} · Tap to view all",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── 2. By category Card with Donut Chart ────────────────────────
            item {
                ByCategoryDonutCard(
                    categoryTotals = summary.categoryTotals,
                    palette = extras.categories
                )
            }

            // ── 3. Budget Card (Needs / Wants / Investment) ─────────────────
            item {
                InsightsBudgetCard(
                    budgetPlan = budgetPlan,
                    needsSorted = needsSorted,
                    wantsSorted = wantsSorted,
                    investSorted = investSorted
                )
            }

            // ── 4. Past months Section ──────────────────────────────────────
            if (pastMonths.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Past months",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(pastMonths.size) { index ->
                    val aggregate = pastMonths[index]
                    val prevAmount = pastMonths.getOrNull(index + 1)?.totalAmount
                    PastMonthCard(
                        aggregate = aggregate,
                        previousAmount = prevAmount
                    )
                }
            }
        }
    }

    if (showTransactionsDialog) {
        MonthlyTransactionsScreenDialog(
            monthName = selectedMonth.shortMonthName,
            totalSpent = summary.totalAmount,
            transactions = summary.transactions,
            onDismiss = { showTransactionsDialog = false },
            onEditExpense = { expense ->
                if (expense.bucket != "Investment") {
                    showTransactionsDialog = false
                    homeViewModel.setEditingExpense(expense)
                }
            },
            onDeleteExpense = { expense ->
                if (expense.bucket == "Investment") {
                    homeViewModel.toggleInvestmentSorted()
                } else {
                    homeViewModel.deleteExpense(expense)
                }
            }
        )
    }
}

@Composable
private fun MonthlyTransactionsScreenDialog(
    monthName: String,
    totalSpent: Double,
    transactions: List<Expense>,
    onDismiss: () -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Header: < November transactions ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$monthName transactions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Summary Header Card ─────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Spent in $monthName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₦${formatAmount(totalSpent)}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${transactions.size} transaction${if (transactions.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Transactions List Card ──────────────────────────────────────
            if (transactions.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "No transactions logged for $monthName yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                transactions.forEachIndexed { index, item ->
                                    val dateStr = if (item.date > 0L) dateFormat.format(Date(item.date)) else ""
                                    val bucketLabel = when (item.bucket) {
                                        "Expense" -> "Daily"
                                        "Needs" -> "Need"
                                        "Wants" -> "Want"
                                        "Investment" -> "Monthly plan"
                                        else -> item.bucket
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = item.bucket != "Investment") {
                                                onEditExpense(item)
                                            }
                                            .padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(100.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = bucketLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            if (item.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = item.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (dateStr.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Text(
                                            text = "₦${formatAmount(item.amount)}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        IconButton(
                                            onClick = { onDeleteExpense(item) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteOutline,
                                                contentDescription = "Delete ${item.category}",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }

                                    if (index < transactions.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline,
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ByCategoryDonutCard(
    categoryTotals: List<CategorySpend>,
    palette: List<Color>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "By category",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Donut Ring Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.outline
                Canvas(modifier = Modifier.size(136.dp)) {
                    val strokeWidth = 26.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    if (categoryTotals.isEmpty()) {
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    } else {
                        var startAngle = -90f
                        categoryTotals.forEachIndexed { idx, item ->
                            val sweep = item.percentage * 360f
                            val color = palette.getOrElse(idx % palette.size) { trackColor }
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (categoryTotals.isEmpty()) {
                Text(
                    text = "No category breakdown for this month yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categoryTotals.forEachIndexed { idx, item ->
                        val dotColor = palette.getOrElse(idx % palette.size) { MaterialTheme.colorScheme.primary }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(dotColor)
                                )
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₦${formatAmount(item.amount)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " · ${(item.percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsBudgetCard(
    budgetPlan: MonthlyBudgetPlan,
    needsSorted: Double,
    wantsSorted: Double,
    investSorted: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Budget",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            BudgetProgressRow(
                label = "Needs",
                sortedSpent = needsSorted,
                budget = budgetPlan.needsBudget
            )

            BudgetProgressRow(
                label = "Investment",
                sortedSpent = investSorted,
                budget = budgetPlan.investmentBudget
            )

            BudgetProgressRow(
                label = "Wants",
                sortedSpent = wantsSorted,
                budget = budgetPlan.wantsBudget
            )
        }
    }
}

@Composable
private fun BudgetProgressRow(
    label: String,
    sortedSpent: Double,
    budget: Double
) {
    val left = (budget - sortedSpent).coerceAtLeast(0.0)
    val progress = if (budget > 0) (sortedSpent / budget).toFloat().coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "₦${formatAmount(left)} left of ₦${formatAmount(budget)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun PastMonthCard(
    aggregate: MonthlyAggregate,
    previousAmount: Double?
) {
    val extras = SpendlyThemeExtras.colors
    val changePercent = if (previousAmount != null && previousAmount > 0) {
        ((aggregate.totalAmount - previousAmount) / previousAmount * 100)
    } else null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = aggregate.month,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (changePercent != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val isUp = changePercent > 0
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isUp) extras.badgeUpBg else extras.badgeDownBg
                    ) {
                        Text(
                            text = "${if (isUp) "+" else ""}${String.format("%.1f", changePercent)}% vs prev",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUp) extras.badgeUpText else extras.badgeDownText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "₦${formatAmount(aggregate.totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
