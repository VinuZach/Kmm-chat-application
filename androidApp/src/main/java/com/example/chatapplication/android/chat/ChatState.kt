package com.example.chatapplication.android.chat

import com.example.chatapplication.ApiConfig.websocketConfig.MessageDto

data class ChatState(val messages:List<MessageDto> = emptyList(),val isLoading:Boolean=true )
