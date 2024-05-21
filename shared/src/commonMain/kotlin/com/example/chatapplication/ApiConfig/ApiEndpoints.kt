package com.example.chatapplication.ApiConfig

const val BASE_URL = "http://192.168.1.38/"

sealed class HttpEndPoints(val url: String) {
    data object userVerification : HttpEndPoints("$BASE_URL/authenticate_user")
    data object forgotPassword : HttpEndPoints("$BASE_URL/forgot_password_user")
    data object registerNewUser : HttpEndPoints("$BASE_URL/register_new_user")
}