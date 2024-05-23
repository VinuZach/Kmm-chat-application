package com.example.chatapplication.ApiConfig.websocketConfig


const val BASE_URL = "ws://192.168.1.39:8000"

sealed class WebSocketEndpoint(val url: String) {
    data object ChatSocket : WebSocketEndpoint("$BASE_URL/ws/chat")
}