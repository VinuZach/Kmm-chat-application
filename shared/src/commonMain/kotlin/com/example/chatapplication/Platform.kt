package com.example.chatapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform