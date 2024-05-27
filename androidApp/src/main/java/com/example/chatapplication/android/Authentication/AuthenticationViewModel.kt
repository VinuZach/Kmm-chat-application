package com.example.chatapplication.android.Authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.ApiHandler
import com.example.chatapplication.ApiResponseObtained
import kotlinx.coroutines.launch

class AuthenticationViewModel() : ViewModel() {
    fun verifyUserDetails(userName: String, password: String, apiResponseObtained: ApiResponseObtained) {

        viewModelScope.launch {
            ApiHandler().verifyUserDetails(userName, password, onResultObtained = { isSuccess, result ->
                print("${isSuccess} ....${result}")
                apiResponseObtained.onResponseObtained(isSuccess, result)
            })

        }

    }

    fun createNewUser(userName: String, password: String, email: String, apiResponseObtained: ApiResponseObtained) {

        viewModelScope.launch {
            ApiHandler().createNewUser(userName, password, email) { isSuccess, result ->
                apiResponseObtained.onResponseObtained(isSuccess, result)
            }
        }

    }
}

