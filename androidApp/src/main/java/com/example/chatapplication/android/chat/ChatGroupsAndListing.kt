package com.example.chatapplication.android.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatRoomWithTotalMessage
import com.example.chatapplication.ApiConfig.websocketConfig.model.GroupListRequestData
import com.google.gson.Gson


@Preview
@Composable
fun ChatGroupAndListingMain(viewModel: ChatViewModel = ChatViewModel(), redirectToRoomById: (Int, String) -> Unit = { a, b -> },
    redirectToRoomDetails: (Int?) -> Unit = { a -> },
    createNewChat: (Int, String?) -> Unit = { a, b -> }) {

    Scaffold(content = { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val selectedGroupName = remember {
                mutableStateOf<String?>(null)
            }
            val retrieveChatOfGroup: (Int, String?) -> Unit = { groupId, groupName ->
                selectedGroupName.value = groupName
                retrieveChatListBasedOnGroup(viewModel, groupId = groupId)
            }
            val lifeCycleOwner = LocalSavedStateRegistryOwner.current

            DisposableEffect(key1 = lifeCycleOwner) {

                val observer = LifecycleEventObserver() { _, event ->
                    Log.d("awhew", "ChatGroupAndListingMain: $event")
                    if (event == Lifecycle.Event.ON_START) retrieveChatOfGroup.invoke(-1, null)
                    else if (event == Lifecycle.Event.ON_STOP) viewModel.disconnect()
                }
                lifeCycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifeCycleOwner.lifecycle.removeObserver(observer)
                }

            }
            Log.e("awhew", "retrieveChatListBasedOnGroup: ${viewModel.state.value.groupDetailsList}")

            val displayRoomToGroup: @Composable (ChatRoomWithTotalMessage, ChatRoomWithTotalMessage) -> Unit =
                { groupDetails, chatDetails ->
                    Log.d("asdasd", "GroupItemDetail: ${groupDetails.roomName}  ${chatDetails.roomName}")
                    viewModel.assignRoomToGroupMutableState.value = ChatViewModel.AssignRoomToGroup(groupDetails, chatDetails)

                }

            viewModel.assignRoomToGroupMutableState.value?.let {
                AlertDialog(title = {
                    Log.d("adasdas", "ChatGroupAndListingMain: ${it.roomDetails.clusterGroupId}")
                    val title: String = if (it.roomDetails.clusterGroupId != "None") {
                        "Reassign group?"

                    } else
                        "Assign Room To Group"

                    Text(text = title)

                }, dismissButton = {
                    Button(onClick = { viewModel.assignRoomToGroupMutableState.value = null }) {
                        Text(text = "Cancel")
                    }
                }, onDismissRequest = { }, confirmButton = {
                    Button(onClick = {
                        viewModel.assignRoomToSelectedGroup(it.groupDetails.clusterGroupId!!.toInt(), it.roomDetails.roomID!!)
                        viewModel.assignRoomToGroupMutableState.value = null
                    }) {
                        Text(text = "Confirm")
                    }
                }, text = {
                    val title: String = if (it.roomDetails.clusterGroupId != "None") {
                        val count = viewModel.state.value.groupDetailsList.clusterRoomGroups.filter { listItem ->
                            listItem.clusterGroupId == it.roomDetails.clusterGroupId
                        }.size
                        if ((count - 1) == 0)
                            "Group will be deleted ..Reassign room?"
                        else
                            "Reassign group?"

                    } else
                        "Assign Room To Group"

                    Text(text = title)


                }
                )

            }
            LongPressDraggable(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)) {


                    LazyRow(modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 50.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        items(items = viewModel.state.value.groupDetailsList.clusterRoomGroups)
                        {
                            GroupItemDetail(groupDetails = it, retrieveChatOfGroup, displayRoomToGroup)
                        }

                    }

                    selectedGroupName.value?.let {
                        Row {
                            Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = "back", modifier = Modifier.clickable {
                                selectedGroupName.value = null
                                retrieveChatOfGroup.invoke(-1, null)
                            })
                            Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
                        }

                    }

                    Text(
                        text = "Create New Chat",
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .border(2.dp, Color.Black, RectangleShape)
                                .padding(10.dp)
                                .clickable {
                                    createNewChat.invoke(-1, null)

                                },
                        textAlign = TextAlign.Center,
                    )


                    LazyColumn(modifier = Modifier
                            .background(Color.LightGray)
                            .padding(top = 10.dp)
                            .fillMaxHeight()) {
                        Log.d("adasdas", "ChatGroupAndListingMain: ${viewModel.state.value.groupDetailsList.chatRoomWithTotalMessage.size}")
                        items(items = viewModel.state.value.groupDetailsList.chatRoomWithTotalMessage) {
                            ChatItemDetails(chatRoomDetails = it, redirectToRoomById)
                        }
                    }
                }
            }
        }
    }, floatingActionButton = {

    })


}


fun retrieveChatListBasedOnGroup(viewModel: ChatViewModel, groupId: Int) {

    viewModel.initSessionForGroupListing("/chatList")
    {
        Log.e("awhew", "retrieveChatListBasedOnGroup: $groupId")

        val groupListRequestData = GroupListRequestData("aaa@aaa.com", groupId)
        viewModel.sendMessage(Gson().toJson(groupListRequestData))
    }
}

@Preview
@Composable
fun GroupItemDetail(groupDetails: ChatRoomWithTotalMessage = ChatRoomWithTotalMessage(),
    getChatListOfGroupId: (Int, String?) -> Unit = { a, b -> },
    displayRoomToGroup: @Composable (ChatRoomWithTotalMessage, ChatRoomWithTotalMessage) -> Unit = { a, b -> }) {
    val chatToGroupList = remember {
        mutableStateMapOf<Int, Int>()
    }


    DropTarget<ChatRoomWithTotalMessage>(modifier = Modifier.padding(6.dp)) { isInBound, chatGroupItem ->
        val bgColor = if (isInBound) Color.Red else Color.White


        chatGroupItem?.let {
            if (isInBound) {
                chatToGroupList[chatGroupItem.roomID!!] = chatGroupItem.roomID!!
                Log.d("asdasd", "ccc")

                displayRoomToGroup.invoke(groupDetails, it)

            }


        }


        val groupName = groupDetails.roomName
        Card(modifier = Modifier.clickable {
            getChatListOfGroupId.invoke(groupDetails.clusterGroupId!!.toInt(), groupName)
        }) {
            Column(modifier = Modifier
                    .background(bgColor)
                    .fillMaxWidth()
                    .padding(10.dp)) {
                if (chatToGroupList.isNotEmpty()) {

                    Text(text = groupName, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    Text(text = "${chatToGroupList.size} Items", fontSize = 14.sp, color = Color.Black)
                } else Text(text = groupName, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }

    }

}

@Preview
@Composable
fun ChatItemDetails(chatRoomDetails: ChatRoomWithTotalMessage = ChatRoomWithTotalMessage(),
    redirectToRoomById: (Int, String) -> Unit = { a, b -> }) {
    Row(Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(10.dp)) {
        DragTarget(modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    chatRoomDetails.let { roomId ->
                        redirectToRoomById.invoke(roomId.roomID!!, roomId.roomName)
                    }

                }, dataToDrop = chatRoomDetails) {
            Text(text = chatRoomDetails.roomName)
        }
    }

}



