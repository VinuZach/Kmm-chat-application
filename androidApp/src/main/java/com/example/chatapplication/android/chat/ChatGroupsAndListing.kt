package com.example.chatapplication.android.chat


import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatRoomWithTotalMessage
import com.example.chatapplication.ApiConfig.websocketConfig.model.GroupListRequestData
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.USER_NAME
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


@Preview
@Composable
fun ChatGroupAndListingMain(viewModel: ChatViewModel = ChatViewModel(),
    redirectToRoomById: (Int, String) -> Unit = { _, _ -> },
    redirectToRoomDetails: (Int?) -> Unit = { _ -> },
    createNewChat: (Int, String?) -> Unit = { _, _ -> },
    createNewGroup:(Int,String?) ->Unit ={_,_ ->}) {

    TitleWithCurvedEdgeBody(titleView = {
        Text(text = "side menu")
        Text(text = "Icon")

    }, bodyView = {
        GroupListingAndChatView(viewModel, redirectToRoomById,createNewGroup)
    }, floatingActionButton = {
        FloatingActionButton(onClick = {
            createNewChat.invoke(-1, null)
        }, containerColor = MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(50.dp)) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "add new chat")
        }

    })


}

@Composable
fun GroupListingAndChatView(viewModel: ChatViewModel = ChatViewModel(),
    redirectToRoomById: (Int, String) -> Unit = { _, _ -> },
    createNewGroup:(Int,String?) ->Unit ={_,_ ->}) {
    Column(modifier = Modifier.fillMaxHeight(0.9f)) {

        val selectedGroupName = remember {
            mutableStateOf<String?>(null)
        }
        val retrieveChatOfGroup: (Int, String?) -> Unit = { groupId, groupName ->
            selectedGroupName.value = groupName
            retrieveChatListBasedOnGroup(viewModel, groupId = groupId)
        }
        val lifeCycleOwner = LocalSavedStateRegistryOwner.current
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        DisposableEffect(key1 = true) {
            coroutineScope.launch {
                val cacheManager = CacheManager.getManger(context = context)
                val userNameFlow = cacheManager.data.firstOrNull()?.toPreferences()?.get(USER_NAME)
                viewModel.userName.value = userNameFlow!!
                retrieveChatOfGroup.invoke(-1, null)
                Log.e("asdasd", "GroupListingAndChatView: $userNameFlow")
            }.invokeOnCompletion {
                Log.e("asdasd", "comp")
            }

            val observer = LifecycleEventObserver() { _, event ->
                if (event == Lifecycle.Event.ON_STOP) viewModel.disconnect()
            }
            lifeCycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifeCycleOwner.lifecycle.removeObserver(observer)
            }

        }


        val displayRoomToGroup: @Composable (ChatRoomWithTotalMessage, ChatRoomWithTotalMessage) -> Unit =
            { groupDetails, chatDetails ->

                viewModel.assignRoomToGroupMutableState.value =
                    ChatViewModel.AssignRoomToGroup(groupDetails, chatDetails)

            }

        viewModel.assignRoomToGroupMutableState.value?.let {
            AlertDialog(title = {

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
                    viewModel.assignRoomToSelectedGroup(it.groupDetails.clusterGroupId!!.toInt(),
                        it.roomDetails.roomID!!)
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
            ) {
                LazyRow(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .defaultMinSize(minHeight = 50.dp),
                    horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                    item {
                        Button(modifier = Modifier.padding(end = 5.dp), onClick = {
                            createNewGroup.invoke(-1,null)
                        }) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "add new group")
                        }
                    }
                    items(items = viewModel.state.value.groupDetailsList.clusterRoomGroups)
                    {
                        GroupItemDetail(groupDetails = it, retrieveChatOfGroup, displayRoomToGroup)
                    }

                }

                selectedGroupName.value?.let {
                    Row(verticalAlignment = Alignment.Top,
                        modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGroupName.value = null
                                    retrieveChatOfGroup.invoke(-1, null)
                                }
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                )
                                .padding(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = "back",
                            modifier = Modifier

                                    .padding(5.dp), tint = Color.White)
                        Text(text = it, modifier = Modifier.padding(start = 5.dp),
                            fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                            fontSize = 25.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }

                }
                LazyColumn(modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxHeight()) {

                    items(items = viewModel.state.value.groupDetailsList.chatRoomWithTotalMessage) {
                        ChatItemDetails(chatRoomDetails = it, redirectToRoomById)
                    }
                }
            }
        }

    }
}


fun retrieveChatListBasedOnGroup(viewModel: ChatViewModel, groupId: Int) {

    viewModel.initSessionForGroupListing("/chatList")
    {
        Log.d("asdasd", "retrieveChatListBasedOnGroup: groupid :${viewModel.userName.value}")
        val groupListRequestData = GroupListRequestData(viewModel.userName.value, groupId)
        viewModel.groupListRequestData = groupListRequestData
        viewModel.sendMessage(Gson().toJson(groupListRequestData))
    }
}

@Preview
@Composable
fun GroupItemDetail(groupDetails: ChatRoomWithTotalMessage = ChatRoomWithTotalMessage(),
    getChatListOfGroupId: (Int, String?) -> Unit = { _, _ -> },
    displayRoomToGroup: @Composable (ChatRoomWithTotalMessage, ChatRoomWithTotalMessage) -> Unit = { _, _ -> }) {

    val chatToGroupList = remember {
        mutableStateMapOf<Int, Int>()
    }


    DropTarget<ChatRoomWithTotalMessage>(modifier = Modifier) { isInBound, chatGroupItem ->
        val bgColor = if (isInBound) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary


        chatGroupItem?.let {
            if (isInBound) {
                chatToGroupList[chatGroupItem.roomID!!] = chatGroupItem.roomID!!

                displayRoomToGroup.invoke(groupDetails, it)
            }
        }


        val groupName = groupDetails.roomName

        Card(modifier = Modifier
                .clickable {
                    getChatListOfGroupId.invoke(groupDetails.clusterGroupId!!.toInt(), groupName)
                }
                .padding(3.dp),
            border = BorderStroke(width = if (isInBound) 2.dp else 1.5.dp, color = bgColor),
            colors = CardColors(contentColor = Color.Black, containerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledContentColor = MaterialTheme.colorScheme.background)) {
            Column() {
                Text(text = groupName, modifier = Modifier.padding(vertical = 8.dp, horizontal = 30.dp),
                    fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                    fontWeight = if (isInBound) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isInBound) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)

            }
        }

    }

}

@Preview
@Composable
fun ChatItemDetails(chatRoomDetails: ChatRoomWithTotalMessage = ChatRoomWithTotalMessage(),
    redirectToRoomById: (Int, String) -> Unit = { _, _ -> }) {
    Column {

        DragTarget(modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    chatRoomDetails.let { roomId ->
                        redirectToRoomById.invoke(roomId.roomID!!, roomId.roomName)
                    }
                }, dataToDrop = chatRoomDetails) {
            Text(text = chatRoomDetails.roomName, fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(20.dp))

        }



        HorizontalDivider(thickness = 1.dp)
    }

}

@Composable
fun TitleWithCurvedEdgeBody(titleView: @Composable () -> Unit, bodyView: @Composable () -> Unit,
    floatingActionButton: (@Composable () -> Unit)? = null) {
    Scaffold(content = { paddingValues ->

        ConstraintLayout(modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)) {
            val (logoSection, detailsSection) = createRefs()
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .constrainAs(logoSection)
                    {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(detailsSection.top)
                    },
                horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                        .fillMaxWidth()
                ) {
                    titleView.invoke()
                }
            }
            Column(modifier = Modifier
                    .background(MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .constrainAs(detailsSection)
                    {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                    }, verticalArrangement = Arrangement.Top) {

                bodyView.invoke()

            }


        }
    }, floatingActionButton = {
        floatingActionButton?.invoke()

    })
}



