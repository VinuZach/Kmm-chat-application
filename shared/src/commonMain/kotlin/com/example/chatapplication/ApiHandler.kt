package com.example.chatapplication

import com.example.chatapplication.ApiConfig.model.BaseResponse
import com.example.chatapplication.ApiConfig.HttpEndPoints
import com.example.chatapplication.ApiConfig.model.ChatCreationOrUpdate
import com.example.chatapplication.ApiConfig.model.GroupCreationOrUpdate
import com.example.chatapplication.ApiConfig.model.NewUserRegistrationRequest
import com.example.chatapplication.ApiConfig.model.NewUserRegistrationResponse
import com.example.chatapplication.ApiConfig.model.UserAuthenticationResponse
import com.example.chatapplication.ApiConfig.model.UserDetails
import com.example.chatapplication.ApiConfig.model.UsersEmailsResponse
import com.example.chatapplication.ApiConfig.websocketConfig.AssignRoomToGroupRequest
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
import kotlinx.coroutines.launch

class ApiHandler(val apiCallManager: HttpClient = getHttpClientForApi()) {


    suspend fun assignRoomToSelectedGroup(roomId: Int, groupId: Int, userOverride: Boolean,
        onResultObtained: (Boolean, Any) -> Unit) {
        var result = BaseResponse(success = false, message = "Server response not obtained")
        val httpResponse: HttpResponse? = try {

            apiCallManager.request {
                contentType(ContentType.Application.Json)
                url(HttpEndPoints.AssignRoomToSelectedGroup.url)
                method = HttpMethod.Post

                setBody(AssignRoomToGroupRequest(roomId, groupId, userOverride))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized)
            result = httpResponse.body<BaseResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            onResultObtained.invoke(result.success, result)
        }
    }

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

    suspend fun retrieveAllUserEmails(currentUserName: String, onResultObtained: (Boolean, Any) -> Unit) {
        var result = UsersEmailsResponse(false)

        val httpResponse: HttpResponse? = try {
            apiCallManager.request {
                contentType(ContentType.Application.Json)
                setBody(UserDetails(currentUserName))
                url(HttpEndPoints.RetrieveAllUsersEmail.url)
                method = HttpMethod.Post

            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized) result =
            UsersEmailsResponse(true, httpResponse.body<List<String>>())

        CoroutineScope(Dispatchers.Main).launch {
            onResultObtained.invoke(result.success, result)
        }
    }


    suspend fun createNewUser(userName: String, password: String, email: String,
        onResultObtained: (Boolean, Any) -> Unit) {
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


    suspend fun createOrUpdateChat(roomName: String, roomId: Int?, selectedUserForChat: List<String>,
        onResultObtained: (Boolean, Any) -> Unit) {
        var result = BaseResponse(success = false, message = "Server response not obtained")
        val httpResponse: HttpResponse? = try {

            apiCallManager.request {
                contentType(ContentType.Application.Json)
                url(HttpEndPoints.CreateOrUpdateChat.url)
                method = HttpMethod.Post

                setBody(ChatCreationOrUpdate(roomName, roomId, selectedUserForChat))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized)
            result = httpResponse.body<BaseResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            onResultObtained.invoke(result.success, result)
        }

    }


    suspend fun createOrUpdateGroup(groupName: String, roomIds: List<Int>, onResultObtained: (Boolean, Any) -> Unit) {
        var result = BaseResponse(success = false, message = "Server response not obtained")
        val httpResponse: HttpResponse? = try {

            apiCallManager.request {
                contentType(ContentType.Application.Json)
                url(HttpEndPoints.CreateOrUpdateChat.url)
                method = HttpMethod.Post

                setBody(GroupCreationOrUpdate(groupName, roomIds))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (httpResponse?.status == HttpStatusCode.OK || httpResponse?.status == HttpStatusCode.Unauthorized)
            result = httpResponse.body<BaseResponse>()
        CoroutineScope(Dispatchers.Main).launch {
            onResultObtained.invoke(result.success, result)
        }

    }
}