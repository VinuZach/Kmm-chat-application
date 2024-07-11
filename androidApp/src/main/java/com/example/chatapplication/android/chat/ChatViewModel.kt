package com.example.chatapplication.android.chat

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.Greeting
import com.example.chatapplication.ApiConfig.websocketConfig.ChatSocketService
import com.example.chatapplication.ApiConfig.websocketConfig.Resource
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatRoomWithTotalMessage
import com.example.chatapplication.ApiHandler
import com.example.chatapplication.cacheConfig.USER_NAME
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel()
{

    private lateinit var chatSocketService: ChatSocketService


    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    data class AssignRoomToGroup(var groupDetails: ChatRoomWithTotalMessage, var roomDetails: ChatRoomWithTotalMessage)

    var assignRoomToGroupMutableState: MutableState<AssignRoomToGroup?> = mutableStateOf(null)


    fun initSessionForGroupListing(groupId: String, onConnected: (() -> Unit)? = null)
    {

        initSession(groupId, false, onConnected)
    }

    fun initSessionForChatRoom(roomId: String, onConnected: (() -> Unit)? = null)
    {
        _state.value = ChatState()
        initSession(roomId, true, onConnected)
    }

    private fun initSession(roomId: String, isForChat: Boolean = true, onConnected: (() -> Unit)?)
    {
        this.chatSocketService = Greeting().provideChatSocketService()
        viewModelScope.launch {
            val result = chatSocketService.initSession(roomId = roomId)

            when (result)
            {
                is Resource.Success ->
                {
                    onConnected?.invoke()
                    if (isForChat)
                    {
                        chatSocketService.observeMessages().onEach { message ->

                          if (message.message.trim().isEmpty())
                          {
                              if (state.value.messages.isNotEmpty())
                              {
                                  state.value.messages.first().prevMessages?.let {
                                      existingPrevMessage->
                                      if (existingPrevMessage.isNotEmpty())
                                          _state.value = ChatState()
                                  }
                                  try
                                  {
                                      state.value.messages.last().prevMessages?.let {
                                              existingPrevMessage->
                                          if (existingPrevMessage.isNotEmpty())
                                              _state.value = ChatState()
                                      }
                                  } catch (e: Exception)
                                  {
                                    e.printStackTrace()
                                  }
                              }
                          }
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

                is Resource.Error   ->
                {
                    _toastEvent.emit(result.message ?: "Unknown Error")
                }


            }
        }

    }

    fun onMessageChange(message: String)
    {
        _messageText.value = message
    }

    fun disconnect()
    {
        viewModelScope.launch {
            chatSocketService.closeSession()
        }
    }

    fun sendMessage(message: String)
    {
        viewModelScope.launch {
            chatSocketService.sendMessage(message)
            _messageText.value = ""

        }
    }


    override fun onCleared()
    {
        super.onCleared()
        disconnect()
    }

    fun assignRoomToSelectedGroup(groupId: Int, roomID: Int, userOverride: Boolean = true)
    {
        viewModelScope.launch {
            ApiHandler().assignRoomToSelectedGroup(roomID, groupId, userOverride, onResultObtained = { isSuccess, result ->
                Log.d("asdasdasd", "assignRoomToSelectedGroup: $isSuccess , $result")
            })
        }
    }

    fun retrieveUserNameFromCache(cacheManager: DataStore<Preferences>) = viewModelScope.async {
        cacheManager.data.firstOrNull()?.toPreferences()?.get(USER_NAME)
    }
}