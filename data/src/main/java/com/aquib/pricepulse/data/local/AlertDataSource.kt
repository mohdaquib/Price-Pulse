package com.aquib.pricepulse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.aquib.pricepulse.domain.entities.PriceAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.alertDataStore: DataStore<Preferences> by preferencesDataStore(name = "alerts")

class AlertDataSource(private val context: Context, private val gson: Gson) {
    private val alertsKey = stringPreferencesKey("alerts_json")
    private val listType = object : TypeToken<List<PriceAlert>>() {}.type

    fun observeAlerts(): Flow<List<PriceAlert>> = context.alertDataStore.data
        .map { prefs -> parseAlerts(prefs) }

    suspend fun add(alert: PriceAlert) {
        context.alertDataStore.edit { prefs ->
            val current = parseAlerts(prefs)
            prefs[alertsKey] = gson.toJson(current + alert)
        }
    }

    suspend fun remove(id: String) {
        context.alertDataStore.edit { prefs ->
            val current = parseAlerts(prefs)
            prefs[alertsKey] = gson.toJson(current.filter { it.id != id })
        }
    }

    private fun parseAlerts(prefs: Preferences): List<PriceAlert> {
        val json = prefs[alertsKey] ?: return emptyList()
        return try {
            gson.fromJson(json, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
