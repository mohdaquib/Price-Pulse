package com.realtimepricetracker.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.realtimepricetracker.domain.entities.OrderBook
import com.realtimepricetracker.domain.entities.OrderBookEntry
import com.realtimepricetracker.presentation.ui.theme.ErrorRed
import com.realtimepricetracker.presentation.ui.theme.RealtimePriceTrackerTheme
import com.realtimepricetracker.presentation.ui.theme.SuccessGreen

@Composable
fun OrderBookPanel(
    orderBook: OrderBook,
    modifier: Modifier = Modifier,
) {
    val maxQty = remember(orderBook) {
        (orderBook.bids + orderBook.asks).maxOfOrNull { it.quantity } ?: 1.0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "ORDER BOOK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            ) {
                BookColumnHeader(
                    label = "BID",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                BookColumnHeader(
                    label = "ASK",
                    color = ErrorRed,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
            }

            // Sub-headers: qty / price | price / qty
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                SubHeader(leftLabel = "QTY", rightLabel = "PRICE", modifier = Modifier.weight(1f))
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                SubHeader(leftLabel = "PRICE", rightLabel = "QTY", modifier = Modifier.weight(1f))
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Book rows — zip bids and asks so they render side-by-side
            val rowCount = maxOf(orderBook.bids.size, orderBook.asks.size)
            repeat(rowCount) { i ->
                val bid = orderBook.bids.getOrNull(i)
                val ask = orderBook.asks.getOrNull(i)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                ) {
                    BidRow(
                        entry = bid,
                        maxQty = maxQty,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 6.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    AskRow(
                        entry = ask,
                        maxQty = maxQty,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Spread
            val spreadText = "Spread  ${"%.2f".format(orderBook.spread)}" +
                "  (${"%.4f".format(orderBook.spreadPercent)}%)"
            Text(
                text = spreadText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BookColumnHeader(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontFamily = FontFamily.Monospace,
        modifier = modifier,
        textAlign = textAlign,
    )
}

@Composable
private fun SubHeader(leftLabel: String, rightLabel: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = leftLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = rightLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** Bid row: depth bar grows from the right; text layout is [qty] ... [price]. */
@Composable
private fun BidRow(entry: OrderBookEntry?, maxQty: Double, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(22.dp)) {
        if (entry != null) {
            val fraction = (entry.quantity / maxQty).toFloat().coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(SuccessGreen.copy(alpha = 0.12f))
                    .align(Alignment.CenterEnd),
            )
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatQty(entry.quantity),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = "%.2f".format(entry.price),
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

/** Ask row: depth bar grows from the left; text layout is [price] ... [qty]. */
@Composable
private fun AskRow(entry: OrderBookEntry?, maxQty: Double, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(22.dp)) {
        if (entry != null) {
            val fraction = (entry.quantity / maxQty).toFloat().coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(ErrorRed.copy(alpha = 0.12f))
                    .align(Alignment.CenterStart),
            )
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "%.2f".format(entry.price),
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = formatQty(entry.quantity),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

private fun formatQty(qty: Double): String = when {
    qty >= 1_000 -> "${"%.1f".format(qty / 1_000)}K"
    else         -> qty.toInt().toString()
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun OrderBookPanelPreview() {
    val book = OrderBook(
        symbol = "AAPL",
        bids = listOf(
            OrderBookEntry(182.45, 500.0),
            OrderBookEntry(182.44, 1200.0),
            OrderBookEntry(182.43, 800.0),
            OrderBookEntry(182.42, 2100.0),
            OrderBookEntry(182.41, 350.0),
            OrderBookEntry(182.40, 670.0),
            OrderBookEntry(182.39, 990.0),
            OrderBookEntry(182.38, 430.0),
        ),
        asks = listOf(
            OrderBookEntry(182.46, 300.0),
            OrderBookEntry(182.47, 750.0),
            OrderBookEntry(182.48, 2900.0),
            OrderBookEntry(182.49, 610.0),
            OrderBookEntry(182.50, 1400.0),
            OrderBookEntry(182.51, 220.0),
            OrderBookEntry(182.52, 880.0),
            OrderBookEntry(182.53, 1750.0),
        ),
    )
    RealtimePriceTrackerTheme {
        OrderBookPanel(orderBook = book)
    }
}
