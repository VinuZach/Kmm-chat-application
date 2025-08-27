package com.example.chatapplication.android.chat

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatapplication.ApiConfig.model.AttachmentUploadResponse
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatAttachment
import com.example.chatapplication.ApiConfig.websocketConfig.model.MessageDto
import com.example.chatapplication.android.AudioRecorderManager
import java.io.File

@Composable
fun ChatItemView(message: MessageDto, viewModel: ChatViewModel, userName: String) {
    val messageDisplay: @Composable (sendUserMessage: String, currentUser: String, message: String,
                                     blockedUser: List<String>?, chatAttachment: ChatAttachment?) -> Unit =
        { sendUserMessage, currentUser, message1, blockedUserList, chatAttachment ->


            Column {

                val isOwnMessage = sendUserMessage == currentUser

                val color =
                    if (!isOwnMessage) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

                val displayBlockedUser = remember {
                    mutableStateOf(false)
                }
                val messageClickEvent: () -> Unit = {
                    blockedUserList?.let {
                        if (it.isNotEmpty()) {
                            displayBlockedUser.value = !displayBlockedUser.value
                        }
                    }
                }
                Box(
                    contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    blockedUserList?.let {
                        if (it.isNotEmpty()) {
                            ChatMessageView(
                                MaterialTheme.colorScheme.tertiary, sendUserMessage,
                                message1,
                                true, isOwnMessage, messageClickEvent, chatAttachment)
                        }
                    }
                    ChatMessageView(
                        color, sendUserMessage, message1, false, isOwnMessage,
                        messageClickEvent, chatAttachment
                    )


                }
                if (displayBlockedUser.value) {
                    var unblockedUserList: MutableList<String> = mutableListOf()
                    blockedUserList?.let {
                        viewModel.state.value.messages.last().chat_room_user_list?.let { fullUserList ->
                            unblockedUserList = fullUserList.toMutableList()
                            unblockedUserList.removeAll(it)
                        }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            reverseLayout = true
                        ) {

                            items(unblockedUserList) { userName ->
                                Text(
                                    text = userName,
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .fillMaxWidth()
                                        .align(Alignment.End),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

    message.prevMessages?.forEach { prevMessage ->
        if (prevMessage.message.isNotEmpty() || prevMessage.chatAttachment != null) {

            messageDisplay(
                prevMessage.user,
                userName,
                prevMessage.message,
                prevMessage.blocked_user,
                prevMessage.chatAttachment
            )


        }
    }


    Log.d("asdweqw", "ChatScreen: $message")
    if (message.message.isNotEmpty() || message.chatAttachment != null) {
        messageDisplay(
            message.user,
            userName,
            message.message,
            message.blocked_user,
            message.chatAttachment
        )
    }
}

@Preview
@Composable
fun AttachmentView(
    modifier: Modifier = Modifier, audioRecorderManager: AudioRecorderManager = AudioRecorderManager(),
    filename: String = "", filePath: String = "", onCancel: (() -> Unit)? = null
) {
    val isMediaPlaying = remember {
        mutableStateOf(false)
    }
    Column {
        onCancel?.let {
            Icon(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 20.dp)
                    .align(Alignment.End)
                    .clickable {
                        it.invoke()
                    },
                imageVector = Icons.Default.Close, contentDescription = "close",
            )
        }

        Row(modifier = modifier
            .fillMaxWidth()
            .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val toggleMediaPlay: () -> Unit = {
                isMediaPlaying.value = !isMediaPlaying.value
                if (isMediaPlaying.value)
                    audioRecorderManager.playAudioFile(filename, filePath) {
                        Log.d("uertuer", "------------: ")
                        isMediaPlaying.value = false
                    }
                else

                    audioRecorderManager.pauseAudioFile()


            }


            Icon(
                imageVector = if (isMediaPlaying.value)
                    Icons.Default.Pause
                else
                    Icons.Default.PlayArrow,
                contentDescription = "Play media",
                modifier = Modifier.clickable {
                    toggleMediaPlay.invoke()
                })
            val isScrolling = remember {
                mutableStateOf(false)
            }

            Log.d("uertuer", "AttachmentView: ${audioRecorderManager.getTotalAudioDuration().value.toFloat()}")
            Column(Modifier.fillMaxWidth(0.9f)) {

                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = audioRecorderManager.getCurrentAudioPosition().value,
                    onValueChangeFinished = {
                        isScrolling.value = false
                    },
                    onValueChange = { newValue ->
                        isScrolling.value = true
                        audioRecorderManager.audioScrollToPosition(
                            newValue.toInt()
                        )
                    },
                    colors = SliderColors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        activeTickColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface,
                        inactiveTickColor = MaterialTheme.colorScheme.secondary,
                        disabledThumbColor = MaterialTheme.colorScheme.secondary,
                        disabledActiveTrackColor = MaterialTheme.colorScheme.secondary,
                        disabledActiveTickColor = MaterialTheme.colorScheme.secondary,
                        disabledInactiveTrackColor = MaterialTheme.colorScheme.secondary,
                        disabledInactiveTickColor = MaterialTheme.colorScheme.secondary,
                    ),
                    valueRange = 0f..audioRecorderManager.getTotalAudioDuration().value.toFloat(),


                    )
                // if (isScrolling.value)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = audioRecorderManager.getFormattedCurrentTime(),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = audioRecorderManager.getFormattedDuration(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


        }
    }


}
