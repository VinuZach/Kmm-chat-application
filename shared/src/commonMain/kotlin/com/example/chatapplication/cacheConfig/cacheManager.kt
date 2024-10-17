package com.example.chatapplication.cacheConfig

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

internal const val dataStoreFileName = "dice.preferences_pb"

annotation class DataStoreKeys
{
   companion object
   {
       val USER_NAME = stringPreferencesKey("user_name")
   }

}

class CacheManager(private val dataStoreInstance:DataStore<Preferences>)
{
    suspend fun <T>saveDataToCache(@DataStoreKeys key:Preferences.Key<T>,value :T)
    {
        dataStoreInstance.edit {
            it[key]=value
        }
    }
    suspend fun <T>retrieveDataFromCache(@DataStoreKeys key:Preferences.Key<T>):T?
    {
       return dataStoreInstance.data.first().toPreferences()[key]
    }

}


