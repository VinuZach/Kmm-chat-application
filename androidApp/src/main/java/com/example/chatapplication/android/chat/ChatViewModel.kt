package com.example.chatapplication.android.chat


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.ApiConfig.websocketConfig.ChatSocketService
import com.example.chatapplication.ApiConfig.websocketConfig.ChatType
import com.example.chatapplication.ApiConfig.websocketConfig.Resource
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatRoomWithTotalMessage
import com.example.chatapplication.ApiConfig.websocketConfig.model.GroupListRequestData
import com.example.chatapplication.ApiHandler
import com.example.chatapplication.Greeting
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    var groupListRequestData: GroupListRequestData? = null
    private lateinit var chatSocketService: ChatSocketService


    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val _showUsersInChat = mutableStateOf(false)
    val showUsersInChat: State<Boolean> = _showUsersInChat

    data class AssignRoomToGroup(var groupDetails: ChatRoomWithTotalMessage, var roomDetails: ChatRoomWithTotalMessage)

    var assignRoomToGroupMutableState: MutableState<AssignRoomToGroup?> = mutableStateOf(null)

    var userName: MutableState<String> = mutableStateOf("")
    val apiHandler by lazy {
        ApiHandler()
    }


    fun showUsersInChat(showUsers: Boolean) {
        _showUsersInChat.value = showUsers
    }

    fun initSessionForGroupListing(groupId: String, onConnected: (() -> Unit)? = null) {

        initSession(groupId, false, onConnected)
    }

    fun initSessionForChatRoom(roomId: String, onConnected: (() -> Unit)? = null) {
        _state.value = ChatState()
        initSession(roomId, true, onConnected)
    }

    private fun initSession(roomId: String, isForChat: Boolean = true, onConnected: (() -> Unit)?) {
        this.chatSocketService = Greeting().provideChatSocketService()
        viewModelScope.launch {
            Log.e("asdasd", "MainChatPageView: 111 ${userName.value}", )
            val result = chatSocketService.initSession(roomId = roomId, currentUserName = userName.value)

            when (result) {
                is Resource.Success -> {
                    onConnected?.invoke()
                    if (isForChat) {
                        chatSocketService.observeMessages().onEach { message ->

                            if (message.message.trim().isEmpty()) {
                                if (state.value.messages.isNotEmpty()) {
                                    state.value.messages.first().prevMessages?.let { existingPrevMessage ->
                                        if (existingPrevMessage.isNotEmpty()) _state.value = ChatState()
                                    }
                                    try {
                                        state.value.messages.last().prevMessages?.let { existingPrevMessage ->
                                            if (existingPrevMessage.isNotEmpty()) _state.value = ChatState()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            val newList = state.value.messages.toMutableList().apply {

                                add(0, message)
                            }

                            _state.value = state.value.copy(messages = newList)
                        }.launchIn(viewModelScope)
                    } else {
                        chatSocketService.observeGroupList().onEach { message ->
                            Log.d("asasdsad", "initSession: $message")
                            if (message.Chat_Type == ChatType.REFRESH_CHAT) {
                                sendMessage(Gson().toJson(groupListRequestData))
                            } else
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

    fun sendMessage(message: String) {
        viewModelScope.launch {
            chatSocketService.sendMessage(message)
            _messageText.value = ""

        }
    }


    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun assignRoomToSelectedGroup(groupId: Int, roomID: Int, userOverride: Boolean = true) {
        viewModelScope.launch {
            apiHandler.assignRoomToSelectedGroup(roomID, groupId, userOverride,
                onResultObtained = { isSuccess, result ->

                })
        }
    }



    fun retrieveUserEmailList(currentUserName:String,onResultObtained: (Boolean, Any) -> Unit) {
        viewModelScope.launch {
            apiHandler.retrieveAllUserEmails(currentUserName,object : (Boolean, Any) -> Unit {
                override fun invoke(p1: Boolean, p2: Any) {

                    onResultObtained.invoke(p1, p2)

                }

            })
        }
    }

    fun createOrUpdateChat(roomName: String, roomID: Int?, selectedUserForChat: List<String>,
        onResultObtained: (Boolean, Any) -> Unit) {
        viewModelScope.launch {
            apiHandler.createOrUpdateChat(roomName, roomID, selectedUserForChat, onResultObtained)
        }

    }
}