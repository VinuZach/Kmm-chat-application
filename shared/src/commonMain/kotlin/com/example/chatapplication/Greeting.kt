package com.example.chatapplication

import com.example.chatapplication.ApiConfig.websocketConfig.ChatSocketService
import io.ktor.client.HttpClient


class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {

        return "Hello, ${platform.name}!"
    }


fun getHttpClientForApi1():HttpClient= getHttpClientForApi()

    fun provideChatSocketService(): ChatSocketService {
        return ChatSocketService()


    }
}

