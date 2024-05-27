package com.example.chatapplication.ApiConfig

const val BASE_URL = "http://192.168.1.35:8000/chatApp"

sealed class HttpEndPoints(val url: String) {
    data object UserVerification : HttpEndPoints("$BASE_URL/authenticate_user")
    data object ForgotPassword : HttpEndPoints("$BASE_URL/forgot_password_user")
    data object RegisterNewUser : HttpEndPoints("$BASE_URL/register_new_user")
}