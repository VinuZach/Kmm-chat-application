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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chatapplication.ApiConfig.model.UsersEmailsResponse
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.DataStoreInstance

import kotlinx.coroutines.launch

@Preview
@Composable
fun RoomCreationOrUpdate(chatCreationUpdate: ChatCreationUpdate = ChatCreationUpdate(-1, null),
    viewModel: ChatViewModel = ChatViewModel(), onBackPressed: () -> Unit = {}) {
    val selectedUserForChat = remember {
        mutableStateListOf<String>()
    }
    val currentUserName = remember {
        mutableStateOf("")
    }
    val confirmationView = remember {
        mutableStateOf(false)
    }
    val createdRoomName = remember {
        mutableStateOf("")
    }
    val isTransmitting = remember {
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

            val cacheManager=CacheManager(DataStoreInstance.getManger(context))
            currentUserName.value=cacheManager.retrieveDataFromCache(cacheManager.USER_NAME)!!

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
        if (isTransmitting.value) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)) {

                CircularProgressIndicator()
                Text(text = "Creating conversation", fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                    color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(10.dp))
            }
        } else
            if (!confirmationView.value)
                SelectionView(selectedUserForChat, allUserList)
            else
                ConfirmationView(selectedUserForChat, createdRoomName)
    }, floatingActionButton = {
        if (!isTransmitting.value)
            FloatingActionButton(onClick = {
                if (selectedUserForChat.isEmpty()) {
                    Toast.makeText(context, "select users", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }

                val onApiResult: (Boolean) -> Unit = {
                    isTransmitting.value = false
                    if (it)
                        onBackPressed.invoke()
                }

                if (selectedUserForChat.size == 1) {
                    if (createdRoomName.value.isEmpty())
                        createdRoomName.value = selectedUserForChat[0]
                    callCreateRoomApi(viewModel, roomName = createdRoomName.value,
                        selectedUsersList = selectedUserForChat,
                        currentUserName = currentUserName.value, roomId = null, isTransmitting = isTransmitting)
                    {
                        onApiResult.invoke(it)
                    }
                } else {
                    if (confirmationView.value) {
                        if (createdRoomName.value.isEmpty()) {
                            Toast.makeText(context, "Conversation Name required", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        } else
                            callCreateRoomApi(viewModel, roomName = createdRoomName.value,
                                selectedUsersList = selectedUserForChat,
                                currentUserName = currentUserName.value, roomId = null, isTransmitting = isTransmitting)
                            {
                                onApiResult.invoke(it)
                            }
                    } else
                        confirmationView.value = true
                }


            }, containerColor = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(50.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "add new chat")
            }

    })

}

@Composable
fun ConfirmationView(selectedUserForChat: MutableList<String>, createdRoomName: MutableState<String>) {

    Column(Modifier.padding(horizontal = 10.dp, vertical = 30.dp)) {

        TextField(colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.secondary,
            disabledTextColor = MaterialTheme.colorScheme.secondary,
            unfocusedTextColor = MaterialTheme.colorScheme.secondary),
            label = {
                Text(text = "Conversation Name", fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                    color = MaterialTheme.colorScheme.secondary)
            },
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp), value = createdRoomName.value, onValueChange = {
                createdRoomName.value = it
            }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), singleLine = true)
        LazyColumn(Modifier.padding(top = 20.dp)) {
            items(selectedUserForChat.size)
            {
                Column {

                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .fillMaxWidth()) {

                        Text(text = selectedUserForChat[it],
                            fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
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
}

fun callCreateRoomApi(viewModel: ChatViewModel, roomId: Int? = null, roomName: String,
    selectedUsersList: MutableList<String>, currentUserName: String, isTransmitting: MutableState<Boolean>,
    onDone: (Boolean) -> Unit) {
    selectedUsersList.add(currentUserName)
    isTransmitting.value = true
    viewModel.createOrUpdateChat(roomName, roomId, selectedUsersList)
    { isSuccess, result ->
        Log.d("ashdhsa", "RoomCreationOrUpdate: $isSuccess")
        onDone.invoke(isSuccess)

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
                    mutableStateOf(selectedUserForChat.contains(allUserList[it]))
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
