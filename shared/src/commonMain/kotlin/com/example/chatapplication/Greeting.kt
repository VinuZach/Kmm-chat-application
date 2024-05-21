package com.example.chatapplication

import com.example.chatapplication.ApiConfig.websocketConfig.ChatSocketService


class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }


    fun provideChatSocketService(): ChatSocketService {
        return ChatSocketService()

    }
}