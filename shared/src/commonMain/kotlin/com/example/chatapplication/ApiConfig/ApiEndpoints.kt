package com.example.chatapplication.ApiConfig

const val HOST_NAME ="192.168.1.34:8080"

const val BASE_URL = "http://$HOST_NAME/chatApp"
sealed class HttpEndPoints(val url: String) {
    data object UserVerification : HttpEndPoints("$BASE_URL/authenticate_user")
    data object ForgotPassword : HttpEndPoints("$BASE_URL/forgot_password_user")
    data object AssignRoomToSelectedGroup : HttpEndPoints("$BASE_URL/assign_room_to_group")
    data object RegisterNewUser : HttpEndPoints("$BASE_URL/register_new_user")
    data object RetrieveAllUsersEmail : HttpEndPoints("$BASE_URL/retrieve_all_users")
    data object RetrieveAllChats : HttpEndPoints("$BASE_URL/retrieve_all_chats")
    data object CreateOrUpdateChat :HttpEndPoints("$BASE_URL/create_update_chat")
    data object CreateOrUpdateGroup :HttpEndPoints("$BASE_URL/create_update_group")
}