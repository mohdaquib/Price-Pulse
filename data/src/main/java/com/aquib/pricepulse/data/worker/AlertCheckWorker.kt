package com.aquib.pricepulse.data.worker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aquib.pricepulse.core.network.datasource.FinnhubRestDataSource
import com.aquib.pricepulse.data.local.AlertDataSource
import com.aquib.pricepulse.data.notification.NotificationHelper
import com.aquib.pricepulse.domain.entities.AlertCondition
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Runs periodically (every 15 min) to check alerts even when the app is closed.
 * Uses REST API since the WebSocket is not available in the background.
 */
@HiltWorker
class AlertCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val alertDataSource: AlertDataSource,
    private val restDataSource: FinnhubRestDataSource,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val active = alertDataSource.observeAlerts().first().filter { it.isActive }
        if (active.isEmpty()) return Result.success()

        val symbols = active.map { it.symbol }.distinct()
        val quotes = restDataSource.getQuotes(symbols).getOrNull() ?: return Result.success()
        val priceMap = quotes.associate { (symbol, dto) -> symbol to dto.currentPrice }

        active.forEach { alert ->
            val price = priceMap[alert.symbol] ?: return@forEach
            val triggered = when (alert.condition) {
                AlertCondition.ABOVE -> price >= alert.targetPrice
                AlertCondition.BELOW -> price <= alert.targetPrice
            }
            if (triggered) {
                // Check notification permission for Android 13+ (API 33)
                val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                if (hasPermission) {
                    notificationHelper.notify(alert, price)
                }

                alertDataSource.remove(alert.id)
            }
        }

        return Result.success()
    }
}
