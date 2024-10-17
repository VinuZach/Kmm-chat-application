package com.example.chatapplication.cacheConfig

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences


internal fun createDataStore(context: Context): DataStore<Preferences> =
    createDataStore(producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath })

object DataStoreInstance
{
    lateinit var cacheManager: DataStore<Preferences>

    fun getManger(context: Context): DataStore<Preferences>
    {

        if (!this::cacheManager.isInitialized) cacheManager = createDataStore(context)
        return cacheManager
    }
}




