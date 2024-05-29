package com.example.chatapplication.ApiConfig.websocketConfig

import com.example.chatapplication.getHttpClientForWebSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.Url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
class ChatSocketService {

    private var socket: WebSocketSession? = null
    private var client = getHttpClientForWebSocket()

    suspend fun initSession(roomId: String): Resource<Unit> {

        return try {

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

                val messageDto = Json.decodeFromString<MessageDto>(json)
                messageDto

            }?.asCommonFlow() ?: flow<MessageDto> { }.asCommonFlow()

        } catch (e: Exception) {
            e.printStackTrace()
            flow<MessageDto> { }.asCommonFlow()
        }
    }

    suspend fun closeSession() {
        socket?.close()
    }
}