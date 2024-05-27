package com.example.chatapplication

import com.example.chatapplication.ApiConfig.HttpEndPoints
import com.example.chatapplication.ApiConfig.NewUserRegistrationRequest
import com.example.chatapplication.ApiConfig.NewUserRegistrationResponse
import com.example.chatapplication.ApiConfig.UserAuthenticationResponse
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ApiHandler(val apiCallManager: HttpClient = getHttpClientForApi()) {


    suspend fun verifyUserDetails(userName: String, password: String, onResultObtained: (Boolean, Any) -> Unit) {
        var result = UserAuthenticationResponse(success = false, message = "Server response not obtained")
        val httpResponse: HttpResponse? = try {
            apiCallManager.request {
                url(HttpEndPoints.UserVerification.url)
                method = HttpMethod.Post
                basicAuth(username = userName, password = password)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized)
            result = httpResponse.body<UserAuthenticationResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            onResultObtained.invoke(result.success, result)
        }


    }

    suspend fun createNewUser(userName: String, password: String, email: String, onResultObtained: (Boolean, Any) -> Unit) {
        var result = NewUserRegistrationResponse(success = false, message = "Server response not obtained")
        val httpResponse: HttpResponse? = try {
            apiCallManager.request {
                contentType(ContentType.Application.Json)
                url(HttpEndPoints.RegisterNewUser.url)
                method = HttpMethod.Post
                setBody(NewUserRegistrationRequest(userName, password, email))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized) result =
            httpResponse.body<NewUserRegistrationResponse>()

        CoroutineScope(Dispatchers.Main).launch {
            onResultObtained.invoke(result.success, result)
        }
    }
}