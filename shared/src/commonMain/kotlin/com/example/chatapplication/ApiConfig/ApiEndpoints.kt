package com.example.chatapplication.ApiConfig

const val HOST_NAME ="192.168.1.39:8000"

const val BASE_URL = "http://$HOST_NAME/chatApp"
sealed class HttpEndPoints(val url: String) {
    data object UserVerification : HttpEndPoints("$BASE_URL/authenticate_user")
    data object ForgotPassword : HttpEndPoints("$BASE_URL/forgot_password_user")
    data object RegisterNewUser : HttpEndPoints("$BASE_URL/register_new_user")
}