@file:Suppress("unused")

package com.example.chatapplication.ApiConfig.websocketConfig.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GroupListRequestData(val user:String,val clusterId:Int)




fun GroupListRequestData.groupListToString(): String {

    return Json.encodeToString(this)

}



@Serializable
data class GroupDetailsResponseDto(val chatRoomWithTotalMessage: List<ChatRoomWithTotalMessage> = emptyList(),val clusterRoomGroups:List<ChatRoomWithTotalMessage> = emptyList(),val Chat_Type:String="")

@Serializable
data class ChatRoomWithTotalMessage(val  roomID:Int?=null,val roomName:String="",val  clusterGroupId:String?=null,val totalMessages:Int=0,val roomCountUnderGroup:Int=0)