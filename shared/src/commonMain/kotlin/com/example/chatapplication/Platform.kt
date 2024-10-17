package com.example.chatapplication

import io.ktor.client.HttpClient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getHttpClientForWebSocket(): HttpClient
expect fun getHttpClientForApi():HttpClient
