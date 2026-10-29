package com.example.daily_expenses.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.ui.theme.SpendlyThemeExtras
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun WantsAndNeedsScreen(
    viewModel: HomeViewModel,
    onAddNeed: () -> Unit,
    onAddWant: () -> Unit,
    onOpenBudgetAndIncome: () -> Unit = {}
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val budgetPlan by viewModel.budgetPlan.collectAsState()
    val needsItems by viewModel.needsItems.collectAsState()
    val wantsItems by viewModel.wantsItems.collectAsState()
    val dailyNeedsSpent by viewModel.dailyNeedsSpent.collectAsState()
    val dailyWantsSpent by viewModel.dailyWantsSpent.collectAsState()
    val prevUnsorted by viewModel.previousMonthUnsortedItems.collectAsState()

    val isEmptyState = !budgetPlan.hasBudget &&
        needsItems.isEmpty() &&
        wantsItems.isEmpty() &&
        dailyNeedsSpent == 0.0 &&
        dailyWantsSpent == 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar: "Wants & Needs" ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wants & Needs",
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
            IconButton(onClick = { viewModel.selectPreviousMonth() }) {
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
            IconButton(onClick = { viewModel.selectNextMonth() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (isEmptyState) {
            // ── Empty State (Pages 9 & 12) ──────────────────────────────────
            val prevMonthName = selectedMonth.previous().shortMonthName
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Nothing planned for ${selectedMonth.shortMonthName}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "List what you need and what you want this month. Tick each one off when it is sorted.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onAddNeed,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "+ Add a need",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onAddWant,
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        text = "+ Add a want",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Carry over unsorted from $prevMonthName",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        if (prevUnsorted.isNotEmpty()) {
                            viewModel.carryOverUnsortedFromPreviousMonth()
                        } else {
                            onAddNeed()
                        }
                    }
                )
            }
        } else {
            // ── Populated State (Pages 3 & 7) ───────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // NEEDS Section
                item {
                    BucketChecklistSection(
                        headerLabel = "NEEDS",
                        budget = budgetPlan.needsBudget,
                        extraDailySpent = dailyNeedsSpent,
                        items = needsItems,
                        addLabel = "+ Add a need",
                        onToggleItem = { viewModel.toggleExpenseSorted(it) },
                        onEditItem = { viewModel.setEditingExpense(it) },
                        onDeleteItem = { viewModel.deleteExpense(it) },
                        onAddClick = onAddNeed
                    )
                }

                // WANTS Section
                item {
                    BucketChecklistSection(
                        headerLabel = "WANTS",
                        budget = budgetPlan.wantsBudget,
                        extraDailySpent = dailyWantsSpent,
                        items = wantsItems,
                        addLabel = "+ Add a want",
                        onToggleItem = { viewModel.toggleExpenseSorted(it) },
                        onEditItem = { viewModel.setEditingExpense(it) },
                        onDeleteItem = { viewModel.deleteExpense(it) },
                        onAddClick = onAddWant
                    )
                }

                // INVESTMENT Card
                item {
                    InvestmentCard(
                        amount = budgetPlan.investmentBudget,
                        ratio = budgetPlan.investmentRatio,
                        isSorted = budgetPlan.investmentSorted,
                        onToggle = { viewModel.toggleInvestmentSorted() },
                        onEditBudget = onOpenBudgetAndIncome
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BucketChecklistSection(
    headerLabel: String,
    budget: Double,
    extraDailySpent: Double = 0.0,
    items: List<Expense>,
    addLabel: String,
    onToggleItem: (Expense) -> Unit,
    onEditItem: (Expense) -> Unit,
    onDeleteItem: (Expense) -> Unit,
    onAddClick: () -> Unit
) {
    val sortedTotal = items.filter { it.isSorted }.sumOf { it.amount } + extraDailySpent
    val left = (budget - sortedTotal)
    val progress = if (budget > 0) (sortedTotal / budget).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "bucket_progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerLabel,
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (budget > 0) {
                    "${if (left < 0) "-" else ""}₦${formatAmount(abs(left))} left"
                } else {
                    "₦${formatAmount(sortedTotal)} sorted"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
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
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "₦${formatAmount(sortedTotal)} sorted of ₦${formatAmount(budget)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Checklist Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onToggleItem(item) },
                                onLongClick = { onDeleteItem(item) }
                            )
                            .padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpendlyCheckbox(
                            checked = item.isSorted,
                            onCheckedChange = { onToggleItem(item) }
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditItem(item) }
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "₦${formatAmount(item.amount)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { onDeleteItem(item) },
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

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                }

                // "+ Add a need" / "+ Add a want" row at the bottom of the card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddClick() }
                        .padding(horizontal = 18.dp, vertical = 15.dp)
                ) {
                    Text(
                        text = addLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InvestmentCard(
    amount: Double,
    ratio: Int,
    isSorted: Boolean,
    onToggle: () -> Unit,
    onEditBudget: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (amount > 0) onToggle() else onEditBudget() }
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpendlyCheckbox(
                checked = isSorted,
                onCheckedChange = { if (amount > 0) onToggle() else onEditBudget() }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Investment",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$ratio% · set aside when pay lands",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "₦${formatAmount(amount)}",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SpendlyCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Surface(
        onClick = onCheckedChange,
        modifier = Modifier.size(26.dp),
        shape = RoundedCornerShape(6.dp),
        color = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        border = BorderStroke(
            1.5.dp,
            if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        if (checked) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

// ─── Full-screen Budget Editor (Pages 11 & 14) ───────────────────────────────
@Composable
fun MonthlyBudgetScreenDialog(
    monthName: String,
    prevMonthName: String,
    currentPlan: MonthlyBudgetPlan,
    previousPlan: MonthlyBudgetPlan,
    incomeItems: List<Expense> = emptyList(),
    onAddIncome: () -> Unit = {},
    onDeleteIncome: (Expense) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (
        income: Double,
        needs: Double,
        wants: Double,
        investment: Double,
        needsRatio: Int,
        investmentRatio: Int,
        wantsRatio: Int
    ) -> Unit
) {
    var incomeText by remember(currentPlan.income) {
        mutableStateOf(if (currentPlan.income > 0) currentPlan.income.toLong().toString() else "")
    }
    var needsRatioText by remember {
        mutableStateOf(currentPlan.needsRatio.toString())
    }
    var investRatioText by remember {
        mutableStateOf(currentPlan.investmentRatio.toString())
    }
    var wantsRatioText by remember {
        mutableStateOf(currentPlan.wantsRatio.toString())
    }
    var needsText by remember(currentPlan.needsBudget) {
        mutableStateOf(if (currentPlan.needsBudget > 0) currentPlan.needsBudget.toLong().toString() else "")
    }
    var wantsText by remember(currentPlan.wantsBudget) {
        mutableStateOf(if (currentPlan.wantsBudget > 0) currentPlan.wantsBudget.toLong().toString() else "")
    }
    var investText by remember(currentPlan.investmentBudget) {
        mutableStateOf(if (currentPlan.investmentBudget > 0) currentPlan.investmentBudget.toLong().toString() else "")
    }
    var showCustomRatioRow by remember {
        mutableStateOf(
            currentPlan.needsRatio != 50 || currentPlan.investmentRatio != 30 || currentPlan.wantsRatio != 20
        )
    }

    val nRatio = needsRatioText.toIntOrNull() ?: 50
    val iRatio = investRatioText.toIntOrNull() ?: 30
    val wRatio = wantsRatioText.toIntOrNull() ?: 20

    val incomeVal = incomeText.toDoubleOrNull() ?: 0.0
    val needsVal = needsText.toDoubleOrNull() ?: 0.0
    val wantsVal = wantsText.toDoubleOrNull() ?: 0.0
    val investVal = investText.toDoubleOrNull() ?: 0.0
    val totalAllocated = needsVal + wantsVal + investVal
    val allocatedPct = if (incomeVal > 0) ((totalAllocated / incomeVal) * 100).toInt() else if (totalAllocated > 0) 100 else 0

    fun recalculateFromIncomeAndRatios(inc: Double, nr: Int, ir: Int, wr: Int) {
        if (inc > 0) {
            needsText = (inc * (nr / 100.0)).toLong().toString()
            investText = (inc * (ir / 100.0)).toLong().toString()
            wantsText = (inc * (wr / 100.0)).toLong().toString()
        }
    }

    val extras = SpendlyThemeExtras.colors

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── Header: < October budget ────────────────────────────────────
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
                    text = "$monthName budget",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Monthly income card ─────────────────────────────────────────
            Surface(
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
                            text = if (incomeItems.isNotEmpty()) "Total monthly income (${incomeItems.size} logged)" else "Monthly income (optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (showCustomRatioRow) "Use 50/30/20" else "Custom ratio",
                            style = MaterialTheme.typography.labelMedium.copy(
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                if (showCustomRatioRow) {
                                    needsRatioText = "50"
                                    investRatioText = "30"
                                    wantsRatioText = "20"
                                    recalculateFromIncomeAndRatios(incomeVal, 50, 30, 20)
                                    showCustomRatioRow = false
                                } else {
                                    showCustomRatioRow = true
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "₦",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            BasicTextField(
                                value = incomeText,
                                onValueChange = { value ->
                                    val clean = value.filter { it.isDigit() }
                                    incomeText = clean
                                    val inc = clean.toDoubleOrNull() ?: 0.0
                                    recalculateFromIncomeAndRatios(inc, nRatio, iRatio, wRatio)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (incomeText.isEmpty()) {
                                            Text(
                                                text = "0",
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontSize = 34.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Surface(
                            onClick = onAddIncome,
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "+ Add income",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (incomeItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        incomeItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.description.isNotBlank()) "${item.category} · ${item.description}" else item.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "+₦${formatAmount(item.amount)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { onDeleteIncome(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete income",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (showCustomRatioRow) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RatioPillField(
                                label = "Needs %",
                                value = needsRatioText,
                                onValueChange = { v ->
                                    val clean = v.filter { it.isDigit() }.take(3)
                                    needsRatioText = clean
                                    recalculateFromIncomeAndRatios(incomeVal, clean.toIntOrNull() ?: 0, iRatio, wRatio)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            RatioPillField(
                                label = "Invest %",
                                value = investRatioText,
                                onValueChange = { v ->
                                    val clean = v.filter { it.isDigit() }.take(3)
                                    investRatioText = clean
                                    recalculateFromIncomeAndRatios(incomeVal, nRatio, clean.toIntOrNull() ?: 0, wRatio)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            RatioPillField(
                                label = "Wants %",
                                value = wantsRatioText,
                                onValueChange = { v ->
                                    val clean = v.filter { it.isDigit() }.take(3)
                                    wantsRatioText = clean
                                    recalculateFromIncomeAndRatios(incomeVal, nRatio, iRatio, clean.toIntOrNull() ?: 0)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Fills the split below using $nRatio / $iRatio / $wRatio. You can edit any percentage or amount.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 3-segment proportional bar (Needs / Investment / Wants) ─────
            val nWeight = if (totalAllocated > 0) (needsVal / totalAllocated).toFloat().coerceAtLeast(0.05f) else (nRatio / 100f).coerceAtLeast(0.05f)
            val iWeight = if (totalAllocated > 0) (investVal / totalAllocated).toFloat().coerceAtLeast(0.05f) else (iRatio / 100f).coerceAtLeast(0.05f)
            val wWeight = if (totalAllocated > 0) (wantsVal / totalAllocated).toFloat().coerceAtLeast(0.05f) else (wRatio / 100f).coerceAtLeast(0.05f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .weight(nWeight)
                        .fillMaxHeight()
                        .background(extras.categories.getOrElse(0) { MaterialTheme.colorScheme.primary })
                )
                Box(
                    modifier = Modifier
                        .weight(iWeight)
                        .fillMaxHeight()
                        .background(extras.categories.getOrElse(1) { MaterialTheme.colorScheme.secondary })
                )
                Box(
                    modifier = Modifier
                        .weight(wWeight)
                        .fillMaxHeight()
                        .background(extras.categories.getOrElse(2) { MaterialTheme.colorScheme.tertiary })
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Needs / Investment / Wants Split Card ───────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BudgetBucketInputRow(
                        dotColor = extras.categories.getOrElse(0) { MaterialTheme.colorScheme.primary },
                        title = "Needs",
                        ratioValue = needsRatioText,
                        onRatioChange = { v ->
                            val clean = v.filter { it.isDigit() }.take(3)
                            needsRatioText = clean
                            recalculateFromIncomeAndRatios(incomeVal, clean.toIntOrNull() ?: 0, iRatio, wRatio)
                        },
                        value = needsText,
                        onValueChange = { needsText = it.filter { c -> c.isDigit() } }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    BudgetBucketInputRow(
                        dotColor = extras.categories.getOrElse(1) { MaterialTheme.colorScheme.secondary },
                        title = "Investment",
                        ratioValue = investRatioText,
                        onRatioChange = { v ->
                            val clean = v.filter { it.isDigit() }.take(3)
                            investRatioText = clean
                            recalculateFromIncomeAndRatios(incomeVal, nRatio, clean.toIntOrNull() ?: 0, wRatio)
                        },
                        value = investText,
                        onValueChange = { investText = it.filter { c -> c.isDigit() } }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    BudgetBucketInputRow(
                        dotColor = extras.categories.getOrElse(2) { MaterialTheme.colorScheme.tertiary },
                        title = "Wants",
                        ratioValue = wantsRatioText,
                        onRatioChange = { v ->
                            val clean = v.filter { it.isDigit() }.take(3)
                            wantsRatioText = clean
                            recalculateFromIncomeAndRatios(incomeVal, nRatio, iRatio, clean.toIntOrNull() ?: 0)
                        },
                        value = wantsText,
                        onValueChange = { wantsText = it.filter { c -> c.isDigit() } }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Allocated summary row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Allocated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₦${formatAmount(totalAllocated)} · $allocatedPct%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Copy previous month budget link ─────────────────────────────
            Text(
                text = "Copy $prevMonthName budget",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable {
                        if (previousPlan.hasBudget) {
                            incomeText = if (previousPlan.income > 0) previousPlan.income.toLong().toString() else ""
                            needsRatioText = previousPlan.needsRatio.toString()
                            investRatioText = previousPlan.investmentRatio.toString()
                            wantsRatioText = previousPlan.wantsRatio.toString()
                            needsText = previousPlan.needsBudget.toLong().toString()
                            wantsText = previousPlan.wantsBudget.toLong().toString()
                            investText = previousPlan.investmentBudget.toLong().toString()
                        } else {
                            incomeText = "120000"
                            needsText = (120000 * (nRatio / 100.0)).toLong().toString()
                            investText = (120000 * (iRatio / 100.0)).toLong().toString()
                            wantsText = (120000 * (wRatio / 100.0)).toLong().toString()
                        }
                    }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Save budget button ──────────────────────────────────────────
            Button(
                onClick = {
                    val finalNeeds = if (needsVal > 0) needsVal else incomeVal * (nRatio / 100.0)
                    val finalInvest = if (investVal > 0) investVal else incomeVal * (iRatio / 100.0)
                    val finalWants = if (wantsVal > 0) wantsVal else incomeVal * (wRatio / 100.0)
                    onSave(incomeVal, finalNeeds, finalWants, finalInvest, nRatio, iRatio, wRatio)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RatioPillField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BudgetBucketInputRow(
    dotColor: Color,
    title: String,
    ratioValue: String,
    onRatioChange: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dotColor)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = ratioValue,
                        onValueChange = onRatioChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.width(IntrinsicSize.Min)
                    )
                    Text(
                        text = "% of income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(130.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₦",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )
        }
    }
}
