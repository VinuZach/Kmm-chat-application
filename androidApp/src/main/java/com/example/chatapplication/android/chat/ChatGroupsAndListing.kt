package com.example.chatapplication.android.chat

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
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatapplication.ApiConfig.websocketConfig.model.ChatRoomWithTotalMessage
import com.example.chatapplication.ApiConfig.websocketConfig.model.GroupListRequestData
import com.google.gson.Gson


@Preview
@Composable
fun ChatGroupAndListingMain(viewModel: ChatViewModel=ChatViewModel(),redirectToRoomById:(Int)->Unit={})
{


    val retrieveChatOfGroup :(Int)->Unit ={

        retrieveChatListBasedOnGroup(viewModel, groupId = it)
    }
    retrieveChatOfGroup.invoke(-1)
    LongPressDraggable(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()   .background(Color.LightGray)) {


            LazyRow(modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                 ,
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
               items(items = viewModel.state.value.groupDetailsList.clusterRoomGroups)
               {
                    GroupItemDetail(groupDetails = it,retrieveChatOfGroup)
                }

            }
            LazyColumn(modifier = Modifier
                    .background(Color.LightGray)
                    .padding(top = 10.dp)
                    .fillMaxHeight()) {

                items(items = viewModel.state.value.groupDetailsList.chatRoomWithTotalMessage) {
                    ChatItemDetails(chatRoomDetails = it,redirectToRoomById)
                }
            }
        }
    }

}


fun retrieveChatListBasedOnGroup(viewModel:ChatViewModel,groupId:Int): Unit {

    viewModel.initSessionForGroupListing("/chatList")
    {
        val groupListRequestData=GroupListRequestData("aaa@aaa.com",groupId)
        viewModel.sendMessage(Gson().toJson(groupListRequestData))
    }
}
@Preview
@Composable
fun GroupItemDetail(groupDetails: ChatRoomWithTotalMessage = ChatRoomWithTotalMessage(),getChatListOfGroupId:(Int)->Unit={})
{
    val chatToGroupList = remember {
        mutableStateMapOf<String, String>()
    }
    DropTarget<String>(modifier = Modifier.padding(6.dp)) { isInBound, chatGroupItem ->
        val bgColor = if (isInBound) Color.Red else Color.White

        chatGroupItem?.let {
            if (isInBound) chatToGroupList[chatGroupItem] = chatGroupItem
        }
        val groupName=groupDetails.roomName
        Card(modifier = Modifier.clickable {
            getChatListOfGroupId.invoke(groupDetails.clusterGroupId.toInt())
        }) {
            Column(modifier = Modifier
                    .background(bgColor)
                    .fillMaxWidth()
                    .padding(10.dp)) {
                if (chatToGroupList.isNotEmpty())
                {
                    Text(text = groupName, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    Text(text = "${chatToGroupList.size} Items", fontSize = 14.sp, color = Color.Black)
                }
                else Text(text = groupName, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }

    }
}

@Preview
@Composable
fun ChatItemDetails(chatRoomDetails: ChatRoomWithTotalMessage = ChatRoomWithTotalMessage(),redirectToRoomById: (Int) -> Unit={})
{
    Row(Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(10.dp)) {
        DragTarget(modifier = Modifier.fillMaxWidth().clickable {

            chatRoomDetails.roomID?.let {roomId->
                redirectToRoomById.invoke(roomId)
            }

        }, dataToDrop = chatRoomDetails) {
            Text(text = chatRoomDetails.roomName,)
        }
    }

}



