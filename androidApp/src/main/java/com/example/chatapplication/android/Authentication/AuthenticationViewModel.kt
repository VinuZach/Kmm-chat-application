package com.example.chatapplication.android.Authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.ApiConfig.HttpEndPoints
import com.example.chatapplication.ApiConfig.NewUserRegistrationRequest
import com.example.chatapplication.ApiConfig.NewUserRegistrationResponse
import com.example.chatapplication.ApiConfig.UserAuthenticationResponse
import com.example.chatapplication.ApiResponseObtained
import com.example.chatapplication.getHttpClientForApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.printStack
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val apiCallManager: HttpClient = getHttpClientForApi()) : ViewModel()
{
    fun verifyUserDetails(userName: String, password: String, apiResponseObtained: ApiResponseObtained): Unit
    {
        var result: UserAuthenticationResponse? = null
        viewModelScope.launch {

            result = UserAuthenticationResponse(success = false, message = "Server response not obtained")
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

            if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized) result =
                httpResponse.body<UserAuthenticationResponse>()

        }.invokeOnCompletion {
            val isSuccess = result?.success ?: run {
                false
            }

            apiResponseObtained.onResponseObtained(isSuccess, result)
        }

    }

    fun createNewUser(userName: String, password: String, email: String, apiResponseObtained: ApiResponseObtained): Unit
    {
        var result: NewUserRegistrationResponse? = null
        viewModelScope.launch {
            result = NewUserRegistrationResponse(success = false, message = "Server response not obtained")
            val httpResponse: HttpResponse? = try
            {
                apiCallManager.request {
                    contentType(ContentType.Application.Json)
                    url(HttpEndPoints.RegisterNewUser.url)
                    method = HttpMethod.Post
                    setBody(NewUserRegistrationRequest(userName, password, email))
                }
            } catch (e: Exception)
            {
                e.printStack()
                null
            }

            if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized) result =
                httpResponse.body<NewUserRegistrationResponse>()

        }.invokeOnCompletion {
            val isSuccess = result?.success ?: run {
                false
            }

            apiResponseObtained.onResponseObtained(isSuccess, result)
        }

    }
}

