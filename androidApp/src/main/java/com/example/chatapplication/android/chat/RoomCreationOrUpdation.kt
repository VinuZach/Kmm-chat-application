package com.example.chatapplication.android.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.chatapplication.ApiConfig.model.UsersEmailsResponse
import com.example.chatapplication.android.ChatCreationUpdate

@Composable
fun RoomCreationOrUpdate(chatCreationUpdate: ChatCreationUpdate, viewModel: ChatViewModel = ChatViewModel()) {

    val roomName = remember {
        mutableStateOf("")
    }
    val allUserList= remember {
        mutableListOf<String>()
    }
    val selectedUserForChat= remember {
        mutableListOf<String>("ccc@ccc.com")
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(value = roomName.value, onValueChange = {
            roomName.value = it
        })
      Button(onClick = {
          viewModel.createOrUpdateChat(roomName.value,chatCreationUpdate.roomId,selectedUserForChat.toList())
          {isSuccess,result->
              Log.d("ashdhsa", "RoomCreationOrUpdate: $isSuccess")

          }
      }) {
          Text(text = "Submit")
      }
        viewModel.retrieveUserEmailList(onResultObtained = object : (Boolean, Any) -> Unit {
            override fun invoke(p1: Boolean, p2: Any) {
                if (p1) {
                    val userEmailList = p2 as UsersEmailsResponse
                    Log.d("asdsad", "invoke: $userEmailList")
                    allUserList.addAll(userEmailList.userEmailList);
                }
            }

        })
    }
}