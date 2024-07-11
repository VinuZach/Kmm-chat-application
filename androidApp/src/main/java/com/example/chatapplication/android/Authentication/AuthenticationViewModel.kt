package com.example.chatapplication.android.Authentication


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.ApiHandler
import com.example.chatapplication.ApiResponseObtained
import com.example.chatapplication.cacheConfig.USER_NAME
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class AuthenticationViewModel() : ViewModel()
{
    fun verifyUserDetails(userName: String, password: String, apiResponseObtained: ApiResponseObtained)
    {

        viewModelScope.launch {
            ApiHandler().verifyUserDetails(userName, password, onResultObtained = { isSuccess, result ->
                print("${isSuccess} ....${result}")

                apiResponseObtained.onResponseObtained(isSuccess, result)
            })

        }

    }

    fun saveUserNameToCache(cacheManager: DataStore<Preferences>, userName: String)
    {
        viewModelScope.launch {
            cacheManager.edit { preference ->
                preference[USER_NAME] = userName
            }
        }

    }




    fun createNewUser(userName: String, password: String, email: String, apiResponseObtained: ApiResponseObtained)
    {

        viewModelScope.launch {
            ApiHandler().createNewUser(userName, password, email) { isSuccess, result ->
                apiResponseObtained.onResponseObtained(isSuccess, result)
            }
        }

    }
}

