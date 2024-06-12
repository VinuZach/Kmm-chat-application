package com.example.chatapplication.ApiConfig.websocketConfig

import kotlinx.serialization.Serializable

@Serializable
data class AssignRoomToGroupRequest(val roomId:Int,val groupId:Int,val userOverride:Boolean)
