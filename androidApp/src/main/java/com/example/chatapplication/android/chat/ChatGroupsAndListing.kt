package com.example.chatapplication.android.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun ChatGroupAndListingMain() {
    Column() {


        LazyRow(modifier = Modifier.fillMaxWidth().background(Color.White), horizontalArrangement = Arrangement.Center) {
            items(count = 2) {
                GroupItemDetail(groupName = "group name $it")
            }
        }
        LazyColumn(modifier = Modifier.padding(top = 10.dp).background(Color.LightGray )) {
            items(count = 10)
            {
                ChatItemDetails(chatTitle = "chat item $it")
            }
        }
    }
}
@Preview
@Composable
fun GroupItemDetail(groupName: String = "sample Group ") {
    Card(modifier = Modifier.background(Color.White).padding(2.dp), shape = RoundedCornerShape(12)) {
        Text(text = groupName, modifier = Modifier.padding(10.dp))
    }
}

@Preview
@Composable
fun ChatItemDetails(chatTitle: String = "sample chat item") {
    Text(text = chatTitle, modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(), fontSize = 20.sp)
}



