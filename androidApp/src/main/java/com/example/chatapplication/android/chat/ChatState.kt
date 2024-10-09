package com.example.chatapplication.android.chat

import com.example.chatapplication.ApiConfig.websocketConfig.model.GroupDetailsResponseDto
import com.example.chatapplication.ApiConfig.websocketConfig.model.MessageDto

data class ChatState(val messages: List<MessageDto> = emptyList(), val isLoading: Boolean = true,
    val groupDetailsList: GroupDetailsResponseDto = GroupDetailsResponseDto())

