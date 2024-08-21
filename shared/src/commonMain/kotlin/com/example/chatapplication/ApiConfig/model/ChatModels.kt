package com.example.chatapplication.ApiConfig.model

import kotlinx.serialization.Serializable

@Serializable
data class UsersEmailsResponse(val success: Boolean=false,val userEmailList: List<String> = emptyList())