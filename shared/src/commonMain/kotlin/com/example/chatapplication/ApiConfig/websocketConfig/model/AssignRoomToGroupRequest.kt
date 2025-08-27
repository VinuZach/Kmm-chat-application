package com.example.chatapplication.ApiConfig.websocketConfig.model

import com.example.chatapplication.ApiConfig.model.AttachmentUploadResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class AssignRoomToGroupRequest(val roomId: Int, val groupId: Int, val userOverride: Boolean)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("attachmentType")
sealed class ChatAttachment {
    abstract val attachmentType: String
}

@Serializable
@SerialName("VoiceNote")
class VoiceAttachment(val voiceAttachment: AttachmentUploadResponse) : ChatAttachment() {
    override var attachmentType: String = "VoiceNote"

}