package com.example.daily_expenses.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.model.CategoryHelper

@Composable
fun AddExpenseSheet(
    editingExpense: Expense?,
    initialBucket: String = "Expense", // "Expense", "Needs", or "Wants"
    onDismiss: () -> Unit,
    onSave: (amount: Double, category: String, description: String, bucket: String) -> Unit
) {
    val activeBucket = editingExpense?.bucket ?: initialBucket
    val isPlannedItem = activeBucket == "Needs" || activeBucket == "Wants"

    var rawAmount by remember(editingExpense) {
        mutableStateOf(
            editingExpense?.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }

    var selectedCategory by remember(editingExpense) {
        mutableStateOf(
            if (editingExpense != null && !isPlannedItem && CategoryHelper.predefinedCategories.contains(editingExpense.category))
                editingExpense.category
            else "Food"
        )
    }

    var itemTitle by remember(editingExpense) {
        mutableStateOf(if (editingExpense != null && isPlannedItem) editingExpense.category else "")
    }

    var noteText by remember(editingExpense) {
        mutableStateOf(editingExpense?.description ?: "")
    }

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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Bar: Close 'X' on left, Centered title ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                val headerTitle = when {
                    editingExpense != null -> "Edit ${activeBucket.lowercase()}"
                    activeBucket == "Needs" -> "Add a need"
                    activeBucket == "Wants" -> "Add a want"
                    else -> "Add expense"
                }

                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Formatted Amount Display ────────────────────────────────────
            val displayNumber = formatInputAmount(rawAmount)
            Text(
                text = "₦$displayNumber",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Category Pills (for Daily Expense) OR Item Title (for Needs/Wants) ──
            if (!isPlannedItem) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    CategoryHelper.predefinedCategories.forEach { cat ->
                        val chosen = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(100.dp),
                            color = if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                            border = BorderStroke(
                                1.dp,
                                if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (chosen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            } else {
                // Item Title Card for Needs/Wants
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = if (activeBucket == "Needs") "Need item" else "Want item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicTextField(
                            value = itemTitle,
                            onValueChange = { itemTitle = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (itemTitle.isEmpty()) {
                                        Text(
                                            text = if (activeBucket == "Needs") "e.g. Rent top-up, Groceries" else "e.g. New sneakers, Hair appointment",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Note (optional) Box ─────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Note (optional)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (noteText.isEmpty()) {
                                    Text(
                                        text = if (isPlannedItem) "e.g. Market on Saturday" else "Lunch at the canteen",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Custom 3x4 Keypad ───────────────────────────────────────────
            NumericKeypad(
                onDigit = { key ->
                    if (key == ".") {
                        if (!rawAmount.contains(".")) {
                            rawAmount = if (rawAmount.isEmpty()) "0." else "$rawAmount."
                        }
                    } else {
                        if (rawAmount == "0") {
                            rawAmount = key
                        } else if (rawAmount.length < 10) {
                            rawAmount += key
                        }
                    }
                },
                onBackspace = {
                    if (rawAmount.isNotEmpty()) {
                        rawAmount = rawAmount.dropLast(1)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Save Button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    val amt = rawAmount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        val finalCategory = if (isPlannedItem) {
                            itemTitle.ifBlank { activeBucket }
                        } else {
                            selectedCategory
                        }
                        onSave(amt, finalCategory, noteText, activeBucket)
                    }
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
                val buttonText = when {
                    editingExpense != null -> "Save changes"
                    activeBucket == "Needs" -> "Save need"
                    activeBucket == "Wants" -> "Save want"
                    else -> "Save expense"
                }
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "BACK")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(width = 88.dp, height = 56.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (key == "BACK") onBackspace() else onDigit(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "BACK") {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatInputAmount(raw: String): String {
    if (raw.isEmpty()) return "0"
    val parts = raw.split(".")
    val intPart = parts[0].toLongOrNull() ?: 0L
    val formattedInt = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(intPart)
    return if (parts.size > 1) {
        "$formattedInt.${parts[1]}"
    } else {
        formattedInt
    }
}
