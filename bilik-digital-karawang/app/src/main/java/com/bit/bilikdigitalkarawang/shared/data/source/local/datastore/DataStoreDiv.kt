package com.bit.bilikdigitalkarawang.shared.data.source.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreDiv(private val dataStore: DataStore<Preferences>) {

    // Metode untuk menyimpan data ke DataStore
    suspend fun saveData(key: String, value: String) {
        dataStore.edit { preferences ->
            val pKey = stringPreferencesKey(key)
            preferences[pKey] = value
        }
    }

    // Metode untuk mendapatkan data dari DataStore
    fun getData(key: String): Flow<String?> {
        return dataStore.data.map { preferences ->
            val pKey = stringPreferencesKey(key)
            preferences[pKey]
        }
    }

    suspend fun saveStringList(key: String, list: List<String>) {
        dataStore.edit { preferences ->
            val pKey = stringSetPreferencesKey(key)
            preferences[pKey] = list.toSet()
        }
    }

    fun getStringList(key: String): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            val pKey = stringSetPreferencesKey(key)
            preferences[pKey]?.toList() ?: emptyList()
        }
    }

    suspend fun bulkSaveData(dataMap: Map<String, String>) {
        dataStore.edit { preferences ->
            dataMap.forEach { (key, value) ->
                val pKey = stringPreferencesKey(key)
                preferences[pKey] = value
            }
        }
    }

    suspend fun clearAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}