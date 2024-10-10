package com.example.chatapplication.ApiConfig.websocketConfig

import com.example.chatapplication.ApiConfig.HOST_NAME


const val BASE_URL = "ws://$HOST_NAME"


sealed class WebSocketEndpoint(val url: String) {
    data object ChatSocket : WebSocketEndpoint("$BASE_URL/ws/chat")

}

annotation class ChatType {
    companion object {
        val REFRESH_CHAT = "refresh"

    }

}