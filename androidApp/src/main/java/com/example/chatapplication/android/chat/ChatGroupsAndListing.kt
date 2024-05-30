package com.example.chatapplication.android.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun ChatGroupAndListingMain() {
    LongPressDraggable(modifier = Modifier.fillMaxSize()) {
        Column() {


            LazyRow(modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White), horizontalArrangement = Arrangement.Center) {
                items(count = 2) {
                    GroupItemDetail(groupName = "group name $it")
                }
            }
            LazyColumn(modifier = Modifier
                    .padding(top = 10.dp)
                    .background(Color.LightGray)) {
                items(count = 10)
                {
                    ChatItemDetails(chatTitle = "chat item $it")
                }
            }
        }
}

}
@Preview
@Composable
fun GroupItemDetail(groupName: String = "sample Group ") {
    val chatToGroupList = remember {
        mutableStateMapOf<String, String>()
    }
    DropTarget<String>(
        modifier = Modifier
                .padding(6.dp)

    ) { isInBound, foodItem ->
        val bgColor = if (isInBound) Color.Red else  Color.White

        foodItem?.let {
            if (isInBound)
                chatToGroupList[foodItem] = foodItem
        }

        Column(
            modifier = Modifier.background( bgColor, RoundedCornerShape(16.dp)),
        ) {
            // ...User Image and name Text views

            if (chatToGroupList.isNotEmpty()) {
                Text(
                    text = groupName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${chatToGroupList.size} Items",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            else
                Text(
                    text = groupName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
        }
    }
}

@Preview
@Composable
fun ChatItemDetails(chatTitle: String = "sample chat item") {
    Row(Modifier.fillMaxWidth().background(Color.Transparent).padding(10.dp)) {
        DragTarget(modifier = Modifier.fillMaxWidth(), dataToDrop = chatTitle) {
            Text(text = chatTitle)
        }
    }

}



