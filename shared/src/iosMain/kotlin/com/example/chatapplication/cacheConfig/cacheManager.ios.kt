package com.example.chatapplication.cacheConfig

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal fun createDataStoreForIos(): DataStore<Preferences> {
    return createDataStore(producePath = {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(directory).path + "/$dataStoreFileName"
    })
}
object DataStoreInstance
{
    private lateinit var cacheManager: DataStore<Preferences>
    fun getManger(): DataStore<Preferences>
    {

        if (!this::cacheManager.isInitialized) cacheManager = createDataStoreForIos()
        return cacheManager
    }
}