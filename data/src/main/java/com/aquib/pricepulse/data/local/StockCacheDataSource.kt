package com.aquib.pricepulse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.aquib.pricepulse.domain.entities.Stock
import kotlinx.coroutines.flow.first

private val Context.stockCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "stock_cache")

private data class CachedStock(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double,
)

class StockCacheDataSource(private val context: Context, private val gson: Gson) {
    private val stocksKey = stringPreferencesKey("stocks_json")
    private val timestampKey = longPreferencesKey("cached_at")
    private val listType = object : TypeToken<List<CachedStock>>() {}.type

    suspend fun save(stocks: List<Stock>) {
        val toSave = stocks.map { CachedStock(it.symbol, it.price, it.change, it.changePercentage) }
        context.stockCacheDataStore.edit { prefs ->
            prefs[stocksKey] = gson.toJson(toSave)
            prefs[timestampKey] = System.currentTimeMillis()
        }
    }

    suspend fun load(): Pair<List<Stock>, Long?> {
        val prefs = context.stockCacheDataStore.data.first()
        val json = prefs[stocksKey]
        val timestamp = prefs[timestampKey]
        val stocks = if (json != null) {
            try {
                val cached: List<CachedStock> = gson.fromJson(json, listType)
                cached.map { Stock(it.symbol, it.price, it.change, it.changePercentage) }
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()
        return Pair(stocks, timestamp)
    }
}
