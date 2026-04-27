package com.realtimepricetracker.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.realtimepricetracker.domain.entities.AlertCondition
import com.realtimepricetracker.presentation.ui.theme.AccentGold
import com.realtimepricetracker.presentation.ui.theme.BullGreen
import com.realtimepricetracker.presentation.ui.theme.BearRed
import com.realtimepricetracker.presentation.ui.theme.RealtimePriceTrackerTheme

@Composable
fun SetAlertDialog(
    symbol: String,
    currentPrice: Double,
    onConfirm: (targetPrice: Double, condition: AlertCondition) -> Unit,
    onDismiss: () -> Unit,
) {
    var priceText by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(AlertCondition.ABOVE) }
    val targetPrice = priceText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Price Alert · $symbol",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = "Current price: ${"%.2f".format(currentPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = condition == AlertCondition.ABOVE,
                        onClick = { condition = AlertCondition.ABOVE },
                        label = { Text("Above") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BullGreen.copy(alpha = 0.18f),
                            selectedLabelColor = BullGreen,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = condition == AlertCondition.ABOVE,
                            selectedBorderColor = BullGreen.copy(alpha = 0.4f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    FilterChip(
                        selected = condition == AlertCondition.BELOW,
                        onClick = { condition = AlertCondition.BELOW },
                        label = { Text("Below") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BearRed.copy(alpha = 0.18f),
                            selectedLabelColor = BearRed,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = condition == AlertCondition.BELOW,
                            selectedBorderColor = BearRed.copy(alpha = 0.4f),
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) priceText = filtered
                    },
                    label = { Text("Target price") },
                    placeholder = { Text("e.g. 150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = priceText.isNotEmpty() && targetPrice == null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        focusedLabelColor = AccentGold,
                        cursorColor = AccentGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(targetPrice!!, condition) },
                enabled = targetPrice != null && targetPrice > 0,
            ) {
                Text(
                    "Set Alert",
                    color = if (targetPrice != null && targetPrice > 0) AccentGold
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

@Preview
@Composable
private fun SetAlertDialogPreview() {
    RealtimePriceTrackerTheme {
        SetAlertDialog(
            symbol = "AAPL",
            currentPrice = 182.50,
            onConfirm = { _, _ -> },
            onDismiss = {},
        )
    }
}
