package com.example.chatapplication.android.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatMessageRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ChatScreen(userName: String, viewModel: ChatViewModel, roomId: Int, roomName: String, onBackPressed: () -> Unit) {

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
                    ChatMessageRequest(command = "join", user = userName, message = "", blocked_user = emptyList(),
                        pageNumber = 0)
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
            .background(MaterialTheme.colorScheme.background))

    {

        TitleWithBackButton(title = roomName, onBackPressed = onBackPressed, PaddingValues(8.dp))


        LazyColumn(modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), reverseLayout = true) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }


            items(items = state.messages) { message ->
                val messageDisplay: @Composable (sendUserMessage: String, currentUser: String, message: String, blockedUser: List<String>?) -> Unit =
                    { sendUserMessage, currentUser, message1, blockedUserList ->

                        Column {

                            val isOwnMessage = sendUserMessage == currentUser

                            val color =
                                if (isOwnMessage) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary


                            Box(contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart,
                                modifier = Modifier.fillMaxWidth()) {
                                Log.d("zxczxczx", "ChatScreen: ${blockedUserList?.isEmpty()}")
//                                if (isOwnMessage)
                                blockedUserList?.let {
                                    if (it.isNotEmpty()) {
                                        ChatMessageView(MaterialTheme.colorScheme.tertiary, sendUserMessage,
                                            message1,
                                            true, isOwnMessage)
                                    }
                                }
                                ChatMessageView(color, sendUserMessage, message1, false, isOwnMessage)
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
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                                verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isUserChecked.value,
                                    onCheckedChange = {
                                        if (!it) blockedUsers.add(user)
                                        else blockedUsers.remove(user)

                                        isUserChecked.value = it
                                    },
                                )
                                Text(text = user, modifier = Modifier.padding(end = 15.dp),
                                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily)
                            }

                        }
                    }
                }
            }
        }


        Row(modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(modifier = Modifier.weight(0.85f), value = messageText.value, onValueChange = {
                messageText.value = it
                if (it.text.isEmpty()) blockedUsers.clear()

                viewModel.onMessageChange(it.text)

            }, placeholder = {
                Text(text = "Enter Message", modifier = Modifier.weight(1f))
            }, colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.secondary,
                disabledTextColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary))



            Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "send",
                modifier = Modifier
                        .weight(0.15f)

                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                if (viewModel.messageText.value.isNotEmpty()) {

                                    val sendMessage = ChatMessageRequest(command = "content", user = userName,
                                        message = viewModel.messageText.value, blocked_user = blockedUsers,
                                        pageNumber = 1)

                                    viewModel.sendMessage(Gson().toJson(sendMessage))
                                    viewModel.onMessageChange("")




                                    blockedUsers.clear()
                                    newMessageSend.value = true
                                    messageText.value = TextFieldValue(text = "")
                                    viewModel.showUsersInChat(false)
                                } else Toast
                                        .makeText(context, "enter text", Toast.LENGTH_SHORT)
                                        .show()

                            }, onLongPress = {

                                viewModel.showUsersInChat(true)
                            })
                        })

        }
    }
}

@Composable
fun TitleWithBackButton(title: String, onBackPressed: () -> Unit, paddingValues: PaddingValues) {
    Row(modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .padding(paddingValues)
            .clickable {
                onBackPressed.invoke()
            },
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = "back to chat listing",
            tint = Color.White)
        Text(text = title, fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            fontSize = MaterialTheme.typography.titleLarge.fontSize, color = Color.White,
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 10.dp))

    }
}

@Composable
fun ChatMessageView(color: Color, sendUserMessage: String, message: String, isHighLightView: Boolean,
    isOwnMessage: Boolean) {
    val modifier = if (isHighLightView) {
        if (isOwnMessage)
            Modifier.padding(end = 18.dp, bottom = 15.dp)
        else
            Modifier.padding(start = 18.dp, bottom = 15.dp)
    } else Modifier.padding(10.dp)
    Column(modifier = modifier
            .width(200.dp)

//                                        .drawBehind {
//                                            val cornerRadius = 10.dp.toPx()
//                                            val triangleHeight = 20.dp.toPx()
//                                            val triangleWidth = 25.dp.toPx()
//                                            val trianglePath = Path().apply {
//                                                if (isOwnMessage) {
//                                                    moveTo(size.width, size.height - cornerRadius)
//                                                    lineTo(size.width, size.height + triangleHeight)
//                                                    lineTo(size.width - triangleWidth, size.height - cornerRadius)
//                                                    close()
//                                                } else {
//                                                    moveTo(0f, size.height - cornerRadius)
//                                                    lineTo(0f, size.height + triangleHeight)
//                                                    lineTo(triangleWidth, size.height - cornerRadius)
//                                                    close()
//                                                }
//
//                                            }
//
//
//
//
//                                            drawPath(path = trianglePath, color = color)
//
//
//                                        }
            .background(color = color, shape = RoundedCornerShape(10.dp))
            .padding(8.dp)) {
        val fontColor = if (isHighLightView) Color.Transparent else MaterialTheme.colorScheme.background

        Text(text = message, color = fontColor, fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            modifier = Modifier.padding(bottom = 5.dp), fontSize = 20.sp)
        Text(text = sendUserMessage, fontWeight = FontWeight.Light, color = fontColor,
            modifier = Modifier.padding(vertical = 2.dp), fontSize = 12.sp)


    }
}
