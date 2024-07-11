package com.example.chatapplication.cacheConfig

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


fun createDataStore(context: Context): DataStore<Preferences> =
    createDataStore(producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath })

object CacheManager
{
    private lateinit var cacheManager: DataStore<Preferences>
    fun getManger(context: Context): DataStore<Preferences>
    {

        if (!this::cacheManager.isInitialized) cacheManager = createDataStore(context)
        return cacheManager
    }
}


