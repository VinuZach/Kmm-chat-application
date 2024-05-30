package com.example.chatapplication.android.chat

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatMessageRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ChatScreen(userName: String, viewModel: ChatViewModel)
{

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {

        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    val lifeCycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifeCycleOwner) {

        val observer = LifecycleEventObserver() { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.initSessionForChatRoom("/43/")
            else if (event == Lifecycle.Event.ON_STOP) viewModel.disconnect()
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }

    }
    val state = viewModel.state.value
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp))

    {
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), reverseLayout = true) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            items(items = state.messages) { message ->
                val isOwnMessage = message.user == userName
                Box(contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.width(200.dp).padding(10.dp).drawBehind {
                            val cornerRadius = 10.dp.toPx()
                            val triangleHeight = 20.dp.toPx()
                            val trianggleWidth = 25.dp.toPx()
                            val trianglePath = Path().apply {
                                if (isOwnMessage)
                                {
                                    moveTo(size.width, size.height - cornerRadius)
                                    lineTo(size.width, size.height + triangleHeight)
                                    lineTo(size.width - trianggleWidth, size.height - cornerRadius)
                                    close()
                                }
                                else
                                {
                                    moveTo(0f, size.height - cornerRadius)
                                    lineTo(0f, size.height + triangleHeight)
                                    lineTo(trianggleWidth, size.height - cornerRadius)
                                    close()
                                }

                            }
                            drawPath(path = trianglePath, color = if (isOwnMessage) Color.Green else Color.DarkGray)

                        }.background(color = if (isOwnMessage) Color.Green else Color.DarkGray, shape = RoundedCornerShape(10.dp))
                        .padding(8.dp)) {
                        Text(text = message.user, fontWeight = FontWeight.Bold)
                        Text(text = message.message)

                    }
                }
            }

        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(value = viewModel.messageText.value, onValueChange = viewModel::onMessageChange, placeholder = {
                Text(text = "Enter Message", modifier = Modifier.weight(1f))
            }

                     )

            IconButton(onClick = {

                if (viewModel.messageText.value.isNotEmpty())
                {
                    val sendMessage = ChatMessageRequest(command = "content", user = "ccc@ccc.com", message = viewModel.messageText.value,
                        blocked_user = emptyList(), pageNumber = 1)
                    viewModel.sendMessage(Gson().toJson(sendMessage))
                }
                else Toast.makeText(context, "enter text", Toast.LENGTH_SHORT).show()

            }, modifier = Modifier.padding(end = 10.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "Send")
            }
        }
    }
}