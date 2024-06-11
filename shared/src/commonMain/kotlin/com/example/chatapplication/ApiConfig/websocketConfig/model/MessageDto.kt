package com.example.chatapplication.ApiConfig.websocketConfig.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MessageDto(var message: String, var user: String, var new_page_number: Int, var blocked_user: List<String>?,
    var prevMessages: List<PrevMessage>?, var chat_room_user_list: String) {
    @Serializable
    data class PrevMessage(var primaryId: Int, var message: String, var timestamp: String, var user: String, var blocked_user: String)

}
@Serializable
data class ChatMessageRequest(var command: String, var message: String, var user: String, var pageNumber: Int, var blocked_user: List<String>)

@Suppress("unused")
fun ChatMessageRequest.getStringData(): String {

    return Json.encodeToString(this)

}