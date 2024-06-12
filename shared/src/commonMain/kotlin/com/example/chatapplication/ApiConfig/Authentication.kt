package com.example.chatapplication.ApiConfig

import kotlinx.serialization.Serializable


@Serializable
data class BaseResponse(val success: Boolean, val message: String? = null )

@Serializable
data class UserAuthenticationResponse(val token: String? = null, val success: Boolean, val message: String? = null,
    val statusCode: String? = null)


@Serializable
data class NewUserRegistrationRequest(val username: String, val password: String, val email: String)

@Serializable
data class NewUserRegistrationResponse(var message:String?=null,var success: Boolean=false,var token:String?=null)