package com.aquib.pricepulse.data.notification

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aquib.pricepulse.domain.entities.AlertCondition
import com.aquib.pricepulse.domain.entities.PriceAlert
import com.aquib.pricepulse.domain.repositories.Notifier

class NotificationHelper(private val context: Context) : Notifier {

    companion object {
        const val CHANNEL_ID = "price_alerts"
        private const val CHANNEL_NAME = "Price Alerts"
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggered when a stock hits your target price"
                enableLights(true)
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun notify(alert: PriceAlert, currentPrice: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val conditionWord = when (alert.condition) {
            AlertCondition.ABOVE -> "above"
            AlertCondition.BELOW -> "below"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("${alert.symbol} alert triggered")
            .setContentText(
                "${alert.symbol} is $conditionWord ${"%.2f".format(alert.targetPrice)} — " +
                "current price: ${"%.2f".format(currentPrice)}"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(alert.id.hashCode(), notification)
    }
}
