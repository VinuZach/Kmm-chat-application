package com.example.chatapplication.ApiConfig.model

import kotlinx.serialization.Serializable

@Serializable
data class UsersEmailsResponse(val success: Boolean=false,val userEmailList: List<String> = emptyList())

@Serializable
data class ChatCreationOrUpdate(val room_name:String,val room_id:Int?,val chat_user_list:List<String>)