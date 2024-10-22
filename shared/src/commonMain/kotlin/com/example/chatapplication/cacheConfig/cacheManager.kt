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



class CacheManager(private val dataStoreInstance:DataStore<Preferences>)
{

    val USER_NAME = stringPreferencesKey("user_name")


    suspend fun <T>saveDataToCache( key:Preferences.Key<T>,value :T)
    {
        dataStoreInstance.edit {
            it[key]=value
        }
    }
    suspend fun <T>retrieveDataFromCache( key:Preferences.Key<T>):T?
    {
       return dataStoreInstance.data.first().toPreferences()[key]
    }

    suspend fun saveStringDataToCache( key:Preferences.Key<String>,value :String)
    {
        dataStoreInstance.edit {
            it[key]=value
        }
    }
    suspend fun retrieveStringDataFromCache( key:Preferences.Key<String>):String?
    {
        return dataStoreInstance.data.first().toPreferences()[key]
    }

}


