package com.example.chatapplication.android.chat

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatapplication.ApiConfig.model.UsersEmailsResponse
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.USER_NAME
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Preview
@Composable
fun RoomCreationOrUpdate(chatCreationUpdate: ChatCreationUpdate = ChatCreationUpdate(-1, null),
    viewModel: ChatViewModel = ChatViewModel(), onBackPressed: () -> Unit = {}) {
    val selectedUserForChat = remember {
        mutableListOf<String>()
    }
    val currentUserName = remember {
        mutableStateOf("")
    }
    val confirmationView = remember {
        mutableStateOf(false)
    }
    BackHandler {
        Log.d("cvbvcb", "RoomCreationOrUpdate: ${confirmationView.value}")
        if (confirmationView.value)
            confirmationView.value = false
        else
            onBackPressed.invoke()
    }

    val allUserList = remember {
        mutableListOf<String>()
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {

        coroutineScope.launch {
            Log.d("asdasda", "ContentView:2222")
            val cacheManager = CacheManager.getManger(context = context)
            currentUserName.value = cacheManager.data.firstOrNull()?.toPreferences()?.get(USER_NAME).toString()

        }.invokeOnCompletion {
            Log.d("asdasda", "ContentView: $currentUserName")
            viewModel.retrieveUserEmailList(currentUserName.value, onResultObtained = object : (Boolean, Any) -> Unit {
                override fun invoke(p1: Boolean, p2: Any) {
                    if (p1) {
                        val userEmailList = p2 as UsersEmailsResponse
                        Log.d("asdsad", "invoke: $userEmailList")
                        allUserList.addAll(userEmailList.userEmailList)
                    }
                }

            })
        }

    }


    TitleWithCurvedEdgeBody(titleView = {
        val title = if (!confirmationView.value) "New Conversation" else "Confirm"
        TitleWithBackButton(title = title, onBackPressed = onBackPressed, PaddingValues(0.dp))
    }, bodyView = {
        if (!confirmationView.value)
            SelectionView(selectedUserForChat, allUserList)
        else
            ConfirmationView(selectedUserForChat)
    }, floatingActionButton = {
        FloatingActionButton(onClick = {
            if (selectedUserForChat.isEmpty()) {
                Toast.makeText(context, "select a user", Toast.LENGTH_SHORT).show()
                return@FloatingActionButton
            }
            if (selectedUserForChat.size == 1)
                callCreateRoomApi(viewModel, roomName = selectedUserForChat[0], selectedUsersList = selectedUserForChat,
                    currentUserName = currentUserName.value, roomId = null)
            else
                confirmationView.value = true


        }, containerColor = MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(50.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "add new chat")
        }

    })

}

@Composable
fun ConfirmationView(selectedUserForChat: MutableList<String>) {
    LazyColumn(Modifier.padding(top = 20.dp)) {
        items(selectedUserForChat.size)
        {
            Column {
            
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()) {

                    Text(text = selectedUserForChat[it], fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(20.dp))
                    Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = "deleted selection",
                        Modifier
                                .padding(10.dp)
                                .clickable {
                                    selectedUserForChat.remove(selectedUserForChat[it])

                                })

                }
                HorizontalDivider(thickness = 1.dp)
            }

        }
    }

}

fun callCreateRoomApi(viewModel: ChatViewModel, roomId: Int? = null, roomName: String,
    selectedUsersList: MutableList<String>, currentUserName: String) {

    selectedUsersList.add(currentUserName)
    viewModel.createOrUpdateChat(roomName, roomId, selectedUsersList)
    { isSuccess, result ->
        Log.d("ashdhsa", "RoomCreationOrUpdate: $isSuccess")

    }
}

@Composable
fun SelectionView(selectedUserForChat: MutableList<String>,
    allUserList: MutableList<String>) {

    LazyColumn(Modifier.padding(top = 20.dp)) {
        items(allUserList.size)
        {
            Column {
                val isCheckedValue = remember {
                    mutableStateOf(false)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()) {

                    Text(text = allUserList[it], fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(20.dp))

                    Checkbox(checked = isCheckedValue.value, onCheckedChange = { isChecked ->
                        isCheckedValue.value = isChecked
                        if (isChecked)
                            selectedUserForChat.add(allUserList[it])
                        else
                            selectedUserForChat.remove(allUserList[it])
                    })
                }
                HorizontalDivider(thickness = 1.dp)
            }

        }
    }

}
