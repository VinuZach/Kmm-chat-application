package com.example.chatapplication.ApiConfig.websocketConfig.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupListRequestData(val user:String,val clusterId:Int)

