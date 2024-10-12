package com.example.chatapplication.ApiConfig.model

import kotlinx.serialization.Serializable

@Serializable
data class UsersEmailsResponse(val success: Boolean = false, val userEmailList: List<String> = emptyList())


@Serializable
data class ChatCreationOrUpdate(val room_name: String, val room_id: Int?, val chat_user_list: List<String>)

@Serializable
data class GroupCreationOrUpdate(val group_name: String, val roomIds: List<Int>)


@Serializable
data class UserDetails(val userName: String)

@Serializable
data class ChatListRequest(val currentUserName:String,val block_assigned_chats: Boolean)

@Serializable
data class ChatListResponse(val success: Boolean = false, val chatListData: List<ChatListResponseData> = emptyList())

@Serializable
data class ChatListResponseData(val id: Int, val roomName: String, val clusterGroupId_id: Int?)