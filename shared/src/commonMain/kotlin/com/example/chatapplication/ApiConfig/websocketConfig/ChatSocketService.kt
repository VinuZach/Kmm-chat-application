package com.example.chatapplication.ApiConfig.websocketConfig

import com.example.chatapplication.ApiConfig.websocketConfig.model.GroupDetailsResponseDto
import com.example.chatapplication.ApiConfig.websocketConfig.model.MessageDto
import com.example.chatapplication.getHttpClientForWebSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.Url
import io.ktor.utils.io.core.Closeable
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class ChatSocketService {

    private var socket: WebSocketSession? = null
    private var client = getHttpClientForWebSocket()
    private var currentUserName: String = ""
    suspend fun initSession(roomId: String, currentUserName: String): Resource<Unit> {

        this.currentUserName = currentUserName
        return try {
            socket?.close()
            socket = client.webSocketSession {
                url(url = Url(WebSocketEndpoint.ChatSocket.url + roomId))
            }

            if (socket?.isActive == true) {
                Resource.Success(Unit)
            } else
                Resource.Error(message = "connection failed")

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(message = e.message.toString())
        }
    }

    suspend fun sendMessage(message: String) {
        try {
            socket?.send(Frame.Text(message))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun <T> Flow<T>.asCommonFlow(): CommonFlow<T> = CommonFlow(this)
    class CommonFlow<T>(private val origin: Flow<T>) : Flow<T> by origin {
        fun watch(block: (T) -> Unit): Closeable {
            val job = Job()

            onEach {
                block(it)
            }.launchIn(CoroutineScope(Dispatchers.Main + job))

            return object : Closeable {
                override fun close() {
                    job.cancel()
                }
            }
        }
    }

    fun observeMessages(): CommonFlow<MessageDto> {
        return try {
            socket?.incoming?.receiveAsFlow()?.filter {
                it is Frame.Text
            }?.map {
                val json = (it as Frame.Text).readText()
                val JsonDecorder = Json { ignoreUnknownKeys = true }
                val messageDto = JsonDecorder.decodeFromString<MessageDto>(json)

                messageDto

            }?.asCommonFlow() ?: flow<MessageDto> { }.asCommonFlow()

        } catch (e: Exception) {
            print("observeMessages  : $e")
            e.printStackTrace()
            flow<MessageDto> { }.asCommonFlow()
        }
    }

    var previousGroupMessageDTO = GroupDetailsResponseDto()
    fun observeGroupList(): CommonFlow<GroupDetailsResponseDto> {
        return try {
            socket?.incoming?.receiveAsFlow()?.filter {
                it is Frame.Text
            }?.map {
                val json = (it as Frame.Text).readText()

                val messageDto = Json.decodeFromString<GroupDetailsResponseDto>(json)
                print("aaa current user :${currentUserName}  requested : ${messageDto.requested_user}")
                if (messageDto.Chat_Type == ChatType.REFRESH_CHAT || (currentUserName.isEmpty() || currentUserName == messageDto.requested_user)) {
                    previousGroupMessageDTO = messageDto
                    messageDto
                } else
                    previousGroupMessageDTO

            }?.asCommonFlow() ?: flow<GroupDetailsResponseDto> { }.asCommonFlow()

        } catch (e: Exception) {
            e.printStackTrace()
            flow<GroupDetailsResponseDto> { }.asCommonFlow()
        }
    }

    suspend fun closeSession() {
        socket?.close()

    }
}