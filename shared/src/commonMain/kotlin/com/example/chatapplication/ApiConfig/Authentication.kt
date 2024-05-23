package com.example.chatapplication.ApiConfig

import kotlinx.serialization.Serializable

@Serializable
data class UserAuthenticationResponse(val token:String?=null,val success:Boolean,val message:String?=null)