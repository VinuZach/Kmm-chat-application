package com.example.chatapplication.android.Authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.ApiConfig.HttpEndPoints
import com.example.chatapplication.ApiConfig.UserAuthenticationResponse
import com.example.chatapplication.ApiResponseObtained
import com.example.chatapplication.getHttpClientForApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.utils.io.printStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val apiCallManager: HttpClient = getHttpClientForApi()) : ViewModel()
{

    fun verifyUserDetails(userName: String, password: String, apiResponseObtained: ApiResponseObtained): Unit
    {
        var result: UserAuthenticationResponse? = null
        viewModelScope.launch {
            val httpResponse: HttpResponse? = try
            {
                apiCallManager.request {
                    url(HttpEndPoints.UserVerification.url)
                    method = HttpMethod.Post
                    basicAuth(username = userName, password = password)
                }
            } catch (e: Exception)
            {
                e.printStack()
                null
            }
            if (httpResponse?.status == HttpStatusCode.OK) result = httpResponse.body<UserAuthenticationResponse>()

        }.invokeOnCompletion {

            apiResponseObtained.onResponseObtained((result != null), result)
        }

    }
}