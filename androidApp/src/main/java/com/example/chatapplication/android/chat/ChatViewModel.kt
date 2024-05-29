package com.example.chatapplication.android.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.Greeting
import com.example.chatapplication.ApiConfig.websocketConfig.ChatSocketService
import com.example.chatapplication.ApiConfig.websocketConfig.MessageFormat
import com.example.chatapplication.ApiConfig.websocketConfig.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    lateinit var chatSocketService: ChatSocketService


    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()


    fun initSession(roomId: String) {
        this.chatSocketService = Greeting().provideChatSocketService()
        viewModelScope.launch {
            val result = chatSocketService.initSession(roomId = roomId)

            when (result) {
                is Resource.Success -> {
                    chatSocketService.observeMessages()
                            .onEach { message ->

                                val newList = state.value.messages.toMutableList().apply {
                                    add(0, message)
                                }
                                _state.value = state.value.copy(messages = newList)
                            }.launchIn(viewModelScope)

                }

                is Resource.Error   -> {
                    _toastEvent.emit(result.message ?: "Unknown Error")
                }


            }
        }

    }

    fun onMessageChange(message: String) {
        _messageText.value = message
    }

    fun disconnect() {
        viewModelScope.launch {
            chatSocketService.closeSession()
        }
    }

    fun sendMessage() {
        viewModelScope.launch {


                val sendMessage =
                    MessageFormat(command = "content", user = "ccc@ccc.com", message = messageText.value, blocked_user = emptyList(),
                        pageNumber = 1)
                chatSocketService.sendMessage(Gson().toJson(sendMessage))
                _messageText.value=""

        }
    }


    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}