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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatapplication.ApiConfig.model.ChatListResponse
import com.example.chatapplication.ApiConfig.model.ChatListResponseData
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.DataStoreInstance
import kotlinx.coroutines.launch

@Composable
fun GroupCreationOrUpdate(
    groupCreationUpdate: GroupCreationUpdate, viewModel: ChatViewModel,
    onBackPressed: () -> Unit,
) {
    val selectedChatForGroup = remember {
        mutableStateListOf<ChatListResponseData>()
    }
    val currentUserName = remember {
        mutableStateOf("")
    }
    val confirmationView = remember {
        mutableStateOf(false)
    }
    val createGroupName = remember {
        mutableStateOf("")
    }
    val isTransmitting = remember {
        mutableStateOf(false)
    }
    BackHandler {

        if (confirmationView.value)
            confirmationView.value = false
        else
            onBackPressed.invoke()
    }

    val allChatList = remember {
        mutableListOf<ChatListResponseData>()
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {

        coroutineScope.launch {
            Log.d("bdfgert", "ContentView:2222")
//            val cacheManager = CacheManager.getManger(context = context)
//            currentUserName.value = cacheManager.data.firstOrNull()?.toPreferences()?.get(USER_NAME).toString()
            val cacheManager=CacheManager(DataStoreInstance.getManger(context))
            currentUserName.value=cacheManager.retrieveDataFromCache(cacheManager.USER_NAME)!!
        }.invokeOnCompletion {
            Log.d("bdfgert", "ContentView: $currentUserName")
            viewModel.retrieveChatList(currentUserName.value, true, onResultObtained = object : (Boolean, Any) -> Unit {
                override fun invoke(p1: Boolean, p2: Any) {
                    Log.d("bdfgert", "invoke: $p1")
                    if (p1) {
                        val chatListResponse = p2 as ChatListResponse

                        allChatList.addAll(chatListResponse.chatListData)
                    }
                }

            })
        }

    }


    TitleWithCurvedEdgeBody(titleView = {
        val title = if (!confirmationView.value) "New Group" else "Confirm"
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
                SelectionViewForGroup(selectedChatForGroup, allChatList)
            else
                ConfirmationViewForGroup(selectedChatForGroup, createGroupName)
    }, floatingActionButton = {

        if (!isTransmitting.value)
            if (allChatList.isNotEmpty())
                FloatingActionButton(onClick = {
                    if (selectedChatForGroup.isEmpty()) {
                        Toast.makeText(context, "select users", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }

                    val onApiResult: (Boolean) -> Unit = {
                        isTransmitting.value = false
                        if (it)
                            onBackPressed.invoke()
                    }

                    if (selectedChatForGroup.size == 1) {
                        if (createGroupName.value.isEmpty())
                            createGroupName.value = selectedChatForGroup[0].roomName
                        callCreateGroupApi(viewModel, groupName = createGroupName.value,
                            selectedChatList = selectedChatForGroup,
                            groupId = null, isTransmitting = isTransmitting)
                        {
                            onApiResult.invoke(it)
                        }
                    } else {
                        if (confirmationView.value) {
                            if (createGroupName.value.isEmpty()) {
                                Toast.makeText(context, "Group Name required", Toast.LENGTH_SHORT).show()
                                return@FloatingActionButton
                            } else
                                callCreateGroupApi(viewModel, groupName = createGroupName.value,
                                    selectedChatList = selectedChatForGroup,
                                    groupId = null, isTransmitting = isTransmitting)
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
fun ConfirmationViewForGroup(
    selectedUserForChat: MutableList<ChatListResponseData>,
    createdRoomName: MutableState<String>,
) {

    Column(Modifier.padding(horizontal = 10.dp, vertical = 30.dp)) {

        TextField(colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.secondary,
            disabledTextColor = MaterialTheme.colorScheme.secondary,
            unfocusedTextColor = MaterialTheme.colorScheme.secondary),
            label = {
                Text(text = "Group Name", fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
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

                        Text(text = selectedUserForChat[it].roomName,
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

fun callCreateGroupApi(
    viewModel: ChatViewModel, groupId: Int? = null, groupName: String,
    selectedChatList: MutableList<ChatListResponseData>, isTransmitting: MutableState<Boolean>,
    onDone: (Boolean) -> Unit,
) {

    isTransmitting.value = true
    viewModel.createOrUpdateGroup(groupName, groupId, selectedChatList)
    { isSuccess, result ->
        Log.d("ashdhsa", "RoomCreationOrUpdate: $isSuccess")
        onDone.invoke(isSuccess)

    }
}

@Composable
fun SelectionViewForGroup(
    selectedChatForGroup: MutableList<ChatListResponseData>,
    allChatList: MutableList<ChatListResponseData>,
) {

    LazyColumn(Modifier.padding(top = 20.dp)) {
        item {
            if (allChatList.isEmpty())
                Text(text = "No Unassigned chats", modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center)
        }
        items(allChatList.size)
        {
            Column {
                val isCheckedValue = remember {
                    mutableStateOf(selectedChatForGroup.contains(allChatList[it]))
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()) {

                    Text(text = allChatList[it].roomName, fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(20.dp))

                    Checkbox(checked = isCheckedValue.value, onCheckedChange = { isChecked ->
                        isCheckedValue.value = isChecked
                        if (isChecked)
                            selectedChatForGroup.add(allChatList[it])
                        else
                            selectedChatForGroup.remove(allChatList[it])
                    })
                }
                HorizontalDivider(thickness = 1.dp)
            }

        }
    }

}