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
class GGGG{
    interface onDOne
    {
        fun aaaa()
    }
    fun cakk(a:String,b:String,c:Float,d:onDOne): String {
        d.aaaa()
        return "asdsa"
    }
    fun aaa(c:(Boolean,Any)->Unit): Unit {
        c.invoke(true,"122")
    }
}
