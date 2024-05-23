package com.example.chatapplication

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AndroidPlatform : Platform
{
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

interface ApiResponseObtained
{
    fun onResponseObtained(isSuccess:Boolean,response:Any?)
}
actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getHttpClientForWebSocket(): HttpClient = HttpClient(CIO) {
    install(WebSockets)
    install(ContentNegotiation) {
        Json
    }
}

actual fun getHttpClientForApi():HttpClient = HttpClient(CIO) {

    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
        logger = object : Logger
        {
            override fun log(message: String) {
                println("message :${message}")
            }
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            useAlternativeNames = false
        })
    }
}
