package com.example.chatapplication.ApiConfig.websocketConfig.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(var message: String, var user: String, var new_page_number: Int, var blocked_user: List<String>?,
    var prevMessages: List<PrevMessage>?, var chat_room_user_list: String) {
    @Serializable
    data class PrevMessage(var primaryId: Int, var message: String, var timestamp: String, var user: String, var blocked_user: String)

}

data class ChatMessageRequest(var command: String, var message: String, var user: String, var pageNumber: Int, var blocked_user: List<String>)

@Serializable
data class GroupDetailsResponseDto(val chatRoomWithTotalMessage: List<ChatRoomWithTotalMessage> = emptyList(),val clusterRoomGroups:List<ChatRoomWithTotalMessage> = emptyList(),val Chat_Type:String="")

@Serializable
data class ChatRoomWithTotalMessage(val  roomID:Int?=null,val roomName:String="",val  clusterGroupId:String="",val totalMessages:Int=0)