package com.example.chatapplication.android.chat

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.Greeting
import com.example.chatapplication.ApiConfig.websocketConfig.ChatSocketService
import com.example.chatapplication.ApiConfig.websocketConfig.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private lateinit var chatSocketService: ChatSocketService


    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun initSessionForGroupListing(groupId:String,onConnected:(()->Unit)?=null)
    {

        initSession(groupId,false,onConnected)
    }
    fun initSessionForChatRoom(roomId: String,onConnected:(()->Unit)?=null)
    {
        initSession(roomId,true,onConnected)
    }

   private fun initSession(roomId: String,isForChat:Boolean=true,onConnected:(()->Unit)?) {
        this.chatSocketService = Greeting().provideChatSocketService()
        viewModelScope.launch {
            val result = chatSocketService.initSession(roomId = roomId)

            when (result) {
                is Resource.Success -> {
                    onConnected?.invoke()
                    if (isForChat)
                    {
                        chatSocketService.observeMessages().onEach { message ->

                                val newList = state.value.messages.toMutableList().apply {
                                    add(0, message)
                                }
                                _state.value = state.value.copy(messages = newList)
                            }.launchIn(viewModelScope)
                    }
                    else
                    {
                        chatSocketService.observeGroupList().onEach { message ->
                            Log.d("awheghhqw", "initSession: $message")

                            _state.value = state.value.copy(groupDetailsList = message)
                        }.launchIn(viewModelScope)
                    }
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

    fun sendMessage(message:String) {
        viewModelScope.launch {
                chatSocketService.sendMessage(message)
                _messageText.value=""

        }
    }


    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}