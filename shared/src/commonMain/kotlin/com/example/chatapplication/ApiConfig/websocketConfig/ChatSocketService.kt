package com.example.chatapplication.ApiConfig.websocketConfig

import com.example.chatapplication.ApiConfig.getHttpClientForWebSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.Url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

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

    fun observeMessages(): Flow<MessageDto> {
        return try {
            socket?.incoming?.receiveAsFlow()?.filter {
                it is Frame.Text
            }?.map {
                val json = (it as Frame.Text).readText()

                val messageDto = Json.decodeFromString<MessageDto>(json)
                messageDto

            } ?: flow { }

        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    suspend fun closeSession() {
        socket?.close()
    }
}