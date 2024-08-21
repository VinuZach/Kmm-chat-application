package com.example.chatapplication.android.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatMessageRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ChatScreen(userName: String, viewModel: ChatViewModel, roomId: Int, roomName: String) {

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
            Log.d("awhew", "cccc : $event")
            if (event == Lifecycle.Event.ON_START) viewModel.initSessionForChatRoom("/$roomId/", onConnected = {
                val sendMessage =
                    ChatMessageRequest(command = "join", user = userName, message = "", blocked_user = emptyList(), pageNumber = 0)
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

    Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp))

    {

        Text(text = roomName)


        LazyColumn(modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), reverseLayout = true) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }


            items(items = state.messages) { message ->
                val messageDisplay: @Composable (sendUserMessage: String, currentUser: String, message: String, blocked_user: List<String>?) -> Unit =
                    { sendUserMessage, currentUser, message, blockedUserList ->

                        Column {

//                            blockedUserList?.let {
//
//                                if (displayBlockedUserSeperator) {
//                                    if (it.isNotEmpty()) {
//
//                                        Spacer(modifier = Modifier.padding(10.dp))
//                                        Row(Modifier.fillMaxWidth()) {
//                                            val blockedUserText = if (it.size == 1) it.first()
//                                            else "${it.first()} and ${(it.size - 1)} others"
//                                            Text(text = "blocked for $blockedUserText", modifier = Modifier.fillMaxWidth(),
//                                                textAlign = TextAlign.Center)
//                                        }
//                                        Spacer(modifier = Modifier.padding(10.dp))
//                                    } else {
//                                        Spacer(modifier = Modifier.padding(10.dp))
//                                        Row(Modifier.fillMaxWidth()) {
//
//                                            Text(text = "----------------------", modifier = Modifier.fillMaxWidth(),
//                                                textAlign = TextAlign.Center)
//                                        }
//                                        Spacer(modifier = Modifier.padding(10.dp))
//                                    }
//                                }
//                            }


                            val isOwnMessage = sendUserMessage == currentUser

                            var color = if (isOwnMessage) Color.Green else Color.LightGray

                            blockedUserList?.let {
                                if (it.isNotEmpty())
                                    color = Color.Gray
                            }
                            Box(contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart,
                                modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier
                                        .width(200.dp)
                                        .padding(10.dp)
                                        .drawBehind {
                                            val cornerRadius = 10.dp.toPx()
                                            val triangleHeight = 20.dp.toPx()
                                            val triangleWidth = 25.dp.toPx()
                                            val trianglePath = Path().apply {
                                                if (isOwnMessage) {
                                                    moveTo(size.width, size.height - cornerRadius)
                                                    lineTo(size.width, size.height + triangleHeight)
                                                    lineTo(size.width - triangleWidth, size.height - cornerRadius)
                                                    close()
                                                } else {
                                                    moveTo(0f, size.height - cornerRadius)
                                                    lineTo(0f, size.height + triangleHeight)
                                                    lineTo(triangleWidth, size.height - cornerRadius)
                                                    close()
                                                }

                                            }




                                            drawPath(path = trianglePath, color = color)


                                        }
                                        .background(color = color,
                                            shape = RoundedCornerShape(10.dp))
                                        .padding(8.dp)) {
                                    Text(text = sendUserMessage, fontWeight = FontWeight.Bold)
                                    Text(text = message)


                                }
                            }
                        }
                    }





                message.prevMessages?.forEach { prevMessage ->
                    if (prevMessage.message.isNotEmpty()) {


                        messageDisplay(prevMessage.user, userName, prevMessage.message, prevMessage.blocked_user)


                    }
                }
                if (message.message.isNotEmpty()) {


                    messageDisplay(message.user, userName, message.message, message.blocked_user)
                    Log.w("3456456", "Res   ")


                }
            }

        }
        Log.e("asdsadsa", "ChatScreen: ${viewModel.showUsersInChat.value}")
        val messageText = remember {
            mutableStateOf(TextFieldValue())
        }
        if (viewModel.showUsersInChat.value) {
            Log.e("asdsadsa", "ChatScreen: ${viewModel.state.value.messages}")
            if (viewModel.state.value.messages.isNotEmpty()) {

                viewModel.state.value.messages.last().chat_room_user_list?.let { chatUserList ->
                    LazyRow {
                        items(chatUserList) { user ->

                            val isUserChecked = remember {
                                mutableStateOf(true)
                            }

                            if (blockedUsers.contains(user)) isUserChecked.value = false
                            else isUserChecked.value = true


                            Row(Modifier
                                    .padding(2.dp)
                                    .border(1.dp, Color.Black, RectangleShape),
                                verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isUserChecked.value, onCheckedChange = {
                                    if (!it) blockedUsers.add(user)
                                    else blockedUsers.remove(user)

                                    isUserChecked.value = it
                                }, modifier = Modifier.padding(horizontal = 10.dp))
                                Text(text = user, modifier = Modifier.padding(horizontal = 2.dp))
                            }

                        }
                    }
                }
            }
        }


        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(value = messageText.value, onValueChange = {
                messageText.value = it
                if (it.text.isEmpty()) blockedUsers.clear()

                viewModel.onMessageChange(it.text)

            }, placeholder = {
                Text(text = "Enter Message", modifier = Modifier.weight(1f))
            })



            Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "send", modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (viewModel.messageText.value.isNotEmpty()) {

                        val sendMessage = ChatMessageRequest(command = "content", user = userName, message = viewModel.messageText.value,
                            blocked_user = blockedUsers, pageNumber = 1)

                        viewModel.sendMessage(Gson().toJson(sendMessage))
                        viewModel.onMessageChange("")




                        blockedUsers.clear()
                        newMessageSend.value = true
                        messageText.value = TextFieldValue(text = "")
                        viewModel.showUsersInChat(false)
                    } else Toast.makeText(context, "enter text", Toast.LENGTH_SHORT).show()

                }, onLongPress = {

                    viewModel.showUsersInChat(true)
                })
            })

        }
    }
}