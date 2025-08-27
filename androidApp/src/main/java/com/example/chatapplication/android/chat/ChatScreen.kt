package com.example.chatapplication.android.chat

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.chatapplication.ApiConfig.model.AttachmentUploadResponse
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatAttachment
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatMessageRequest
import com.example.chatapplication.ApiConfig.websocketConfig.model.VoiceAttachment
import com.example.chatapplication.android.AudioRecorderManager
import com.example.chatapplication.android.AudioRecordingPermissionRequester
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import java.io.File


@Composable
fun ChatScreen(modifier: Modifier, userName: String, viewModel: ChatViewModel, roomId: Int, roomName: String,
               onBackPressed: () -> Unit
) {
    val blockedUsers = remember {
        mutableListOf<String>()
    }
    val newMessageSend = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {

        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }
    val lifeCycleOwner = LocalSavedStateRegistryOwner.current

    DisposableEffect(key1 = lifeCycleOwner) {

        val observer = LifecycleEventObserver() { _, event ->

            if (event == Lifecycle.Event.ON_START) viewModel.initSessionForChatRoom(
                "/$roomId/",
                onConnected = {
                    val sendMessage =
                        ChatMessageRequest(
                            command = "join",
                            user = userName,
                            message = "",
                            blocked_user = emptyList(),
                            pageNumber = 0
                        )
                    viewModel.sendMessage(Gson().toJson(sendMessage))
                })
            else if (event == Lifecycle.Event.ON_STOP) viewModel.disconnect()
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }

    }
    val state = viewModel.state.value

    Column(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.primaryContainer))
    {

        TitleWithBackButton(title = roomName, onBackPressed = onBackPressed, PaddingValues(8.dp))


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), reverseLayout = true
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }


            items(items = state.messages) { message ->
                ChatItemView(message, viewModel, userName)
            }

        }

        val messageText = remember {
            mutableStateOf(TextFieldValue())
        }
        if (viewModel.showUsersInChat.value) {

            if (viewModel.state.value.messages.isNotEmpty()) {

                viewModel.state.value.messages.last().chat_room_user_list?.let { chatUserList ->
                    LazyRow {
                        items(chatUserList) { user ->

                            val isUserChecked = remember {
                                mutableStateOf(true)
                            }

                            if (blockedUsers.contains(user)) isUserChecked.value = false
                            else isUserChecked.value = true


                            Row(
                                Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(10.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isUserChecked.value,
                                    onCheckedChange = {
                                        if (!it) blockedUsers.add(user)
                                        else blockedUsers.remove(user)

                                        isUserChecked.value = it
                                    },
                                )
                                Text(
                                    text = user, modifier = Modifier.padding(end = 15.dp),
                                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily
                                )
                            }

                        }
                    }
                }
            }
        }


        InputElementSpace(messageText, viewModel, blockedUsers, newMessageSend, userName, context)
    }
}


@Preview
@Composable
fun InputElementSpace(messageText: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue()),
                      viewModel: ChatViewModel = ChatViewModel(),
                      blockedUsers: MutableList<String> = mutableListOf<String>(),
                      newMessageSend: MutableState<Boolean> = mutableStateOf(false),
                      userName: String = "",
                      context: Context = LocalContext.current
) {


    var recordedFile by remember { mutableStateOf<File?>(null) }

    val audioRecorderManager = remember { AudioRecorderManager() }

    Column {
        recordedFile?.let { file ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)) {


                AttachmentView(filename = file.name, filePath = file.absolutePath, audioRecorderManager = audioRecorderManager, onCancel = {
                    recordedFile=null
                })


            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            TextField(
                modifier = Modifier.weight(0.85f),
                value = messageText.value,
                onValueChange = {
                    messageText.value = it
                    if (it.text.isEmpty()) blockedUsers.clear()
                    viewModel.onMessageChange(it.text)

                },
                placeholder = {
                    Text(text = "Enter Message", modifier = Modifier.weight(1f))
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    disabledTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary
                )
            )

            if (messageText.value.text.isNotEmpty() || recordedFile != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "send",
                    modifier = Modifier
                        .weight(0.15f)

                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                val sendMessageToServer: (String, ChatAttachment?) -> Unit =
                                    { message, chatAttachment ->
                                        val sendMessage = ChatMessageRequest(
                                            command = "content",
                                            user = userName,
                                            message = message,
                                            blocked_user = blockedUsers,
                                            pageNumber = 1,
                                            chatAttachment = chatAttachment
                                        )

                                        viewModel.sendMessage(Gson().toJson(sendMessage))
                                        viewModel.onMessageChange("")
                                        blockedUsers.clear()
                                        newMessageSend.value = true
                                        messageText.value = TextFieldValue(text = "")
                                        viewModel.showUsersInChat(false)

                                    }
                                if (viewModel.messageText.value.isNotEmpty()) {
                                    sendMessageToServer.invoke(viewModel.messageText.value, null)
//                                    val sendMessage = ChatMessageRequest(
//                                        command = "content",
//                                        user = userName,
//                                        message = viewModel.messageText.value,
//                                        blocked_user = blockedUsers,
//                                        pageNumber = 1
//                                    )
//
//                                    viewModel.sendMessage(Gson().toJson(sendMessage))
//                                    viewModel.onMessageChange("")
//
//
//
//
//                                    blockedUsers.clear()
//                                    newMessageSend.value = true
//                                    messageText.value = TextFieldValue(text = "")
//                                    viewModel.showUsersInChat(false)
                                } else if (recordedFile != null) {
                                    recordedFile?.let {
                                        viewModel.uploadFile(
                                            it,
                                            onResultObtained = { isSuccess, result ->
                                                val voiceNoteAttachment =
                                                    result as AttachmentUploadResponse
                                                sendMessageToServer.invoke(
                                                    viewModel.messageText.value,
                                                    VoiceAttachment(voiceNoteAttachment)
                                                )
                                                Toast.makeText(context,
                                                    "$isSuccess",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()

                                            })
                                    }


                                } else Toast
                                    .makeText(context, "enter text", Toast.LENGTH_SHORT)
                                    .show()

                            }, onLongPress = {

                                viewModel.showUsersInChat(true)
                            })
                        })

            } else {
                val isAudioPermissionGranted = remember {
                    mutableStateOf(false)
                }

                AudioRecordingPermissionRequester { hasPermission, requestPermission ->
                    isAudioPermissionGranted.value = hasPermission
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice note",
                        modifier = Modifier
                            .padding(10.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        if (isAudioPermissionGranted.value) {

                                            val audioFile = File(
                                                context.cacheDir,
                                                "audio_record_${System.currentTimeMillis()}.3gp"
                                            )
                                            recordedFile = null
                                            audioRecorderManager.startRecording(
                                                context,
                                                audioFile.absolutePath
                                            )
                                            Toast.makeText(context, "aaa", Toast.LENGTH_SHORT).show()
                                            tryAwaitRelease()
                                            audioRecorderManager.stopRecording()
                                            audioRecorderManager.getRecordedFilePath()?.let { resultFile ->
                                                recordedFile = resultFile
                                            }

                                            Toast.makeText(context, "sss", Toast.LENGTH_SHORT).show()
                                        } else
                                            requestPermission.invoke()

                                    })
                            },
                    )
                }
            }


        }
    }

}


@Preview
@Composable
fun TitleWithBackButton(
    title: String = "",
    onBackPressed: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(paddingValues)
            .clickable {
                onBackPressed.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = "back to chat listing",
            tint = Color.White
        )
        Text(
            text = title, fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            fontSize = MaterialTheme.typography.titleMedium.fontSize, color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp, horizontal = 10.dp)
        )

    }
}

@Preview
@Composable
fun ChatMessageView(
    color: Color = Color.Transparent, sendUserMessage: String = "user name", message: String = "message",
    isHighLightView: Boolean = false, isOwnMessage: Boolean = true, onClickAction: () -> Unit = {},
    chatAttachment: ChatAttachment? = null
) {
    val modifier = if (isHighLightView) {
        if (isOwnMessage)
            Modifier.padding(end = 18.dp, bottom = 15.dp)
        else
            Modifier.padding(start = 18.dp, bottom = 15.dp)
    } else Modifier.padding(10.dp)
    Column(
        modifier = modifier
            .clickable {
                onClickAction.invoke()
            }
            .defaultMinSize(200.dp)
            .background(color = color, shape = RoundedCornerShape(10.dp))
            .padding(8.dp)) {
        val fontColor =
            if (isHighLightView) Color.Transparent else MaterialTheme.colorScheme.background
        chatAttachment?.let {
            when (it) {
                is VoiceAttachment -> {
                    val voiceAttachment = (chatAttachment as VoiceAttachment).voiceAttachment
                    val audioRecorderManager = remember { AudioRecorderManager() }
                    Card(shape = RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp)) {

                        AttachmentView(Modifier.background(MaterialTheme.colorScheme.primaryContainer), audioRecorderManager, filename = voiceAttachment.fileName!!, filePath = voiceAttachment.getUploadFile()!!)

                    }
                }
            }


        }


        Text(text = message, color = fontColor, fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            modifier = Modifier.padding(bottom = 5.dp), fontSize = 20.sp
        )
        Text(text = sendUserMessage, fontWeight = FontWeight.Light, color = fontColor,
            modifier = Modifier.padding(vertical = 2.dp), fontSize = 12.sp
        )
    }
}
