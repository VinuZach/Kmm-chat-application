package com.example.chatapplication.ApiConfig

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

fun getHttpClientForWebSocket(): HttpClient = HttpClient(CIO)
{
    install(WebSockets)
    install(ContentNegotiation)
    {
        Json
    }
}

fun getHttpClientForApiCall(): HttpClient = HttpClient(CIO) {
    install(ContentNegotiation)
    //Timeout plugin to set up timeout milliseconds for client
    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
        logger = object : Logger {
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



