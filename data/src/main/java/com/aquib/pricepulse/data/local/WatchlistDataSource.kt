package com.aquib.pricepulse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.watchlistDataStore: DataStore<Preferences> by preferencesDataStore(name = "watchlist")

class WatchlistDataSource(private val context: Context) {
    private val symbolsKey = stringSetPreferencesKey("symbols")

    fun observeSymbols(): Flow<Set<String>> = context.watchlistDataStore.data
        .map { prefs -> prefs[symbolsKey] ?: emptySet() }

    suspend fun add(symbol: String) {
        context.watchlistDataStore.edit { prefs ->
            prefs[symbolsKey] = (prefs[symbolsKey] ?: emptySet()) + symbol
        }
    }

    suspend fun remove(symbol: String) {
        context.watchlistDataStore.edit { prefs ->
            prefs[symbolsKey] = (prefs[symbolsKey] ?: emptySet()) - symbol
        }
    }
}
