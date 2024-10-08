package com.example.chatapplication.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chatapplication.android.chat.ChatGroupAndListingMain
import com.example.chatapplication.android.chat.ChatScreen
import com.example.chatapplication.android.chat.ChatViewModel
import com.example.chatapplication.android.chat.RoomCreationOrUpdate
import com.example.chatapplication.android.theme.ChatApplicationTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatApplicationTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainChatPageView()
                }
            }
        }
    }

    @Preview
    @Composable
    fun MainChatPageView() {

        val chatViewModel: ChatViewModel = viewModel()
        val navController = rememberNavController()
//        val userNameFlow = chatViewModel.retrieveUserNameFromCache(CacheManager.getManger(applicationContext))
//        userNameFlow.invokeOnCompletion {
//            userNameFlow.getCompleted()?.let {
//
//                chatViewModel.userName.value=it
//            }
//        }
        NavHost(navController = navController, startDestination = "group_and_chat_listing") {
            composable("group_and_chat_listing") {
                ChatGroupAndListingMain(viewModel = chatViewModel, redirectToRoomById = { roomId, roomName ->
                    navController.navigate(NavigationChatRoomId(roomId, roomName))
                }, redirectToRoomDetails = {
                    navController.navigate("room_details_page")
                }, createNewChat =
                { roomId, roomName ->
                    navController.navigate(ChatCreationUpdate(roomId, roomName))
                })

            }
            composable<NavigationChatRoomId> {
                val chatRoomId = it.toRoute<NavigationChatRoomId>()
                Log.d("asasdsadsad", "directChat: ")
                ChatScreen(userName = chatViewModel.userName.value, viewModel = chatViewModel, roomId = chatRoomId.roomId, roomName = chatRoomId.roomName)

            }

            composable<ChatCreationUpdate> {
                val ChatCreationUpdate = it.toRoute<ChatCreationUpdate>()
                RoomCreationOrUpdate(ChatCreationUpdate, chatViewModel)
            }


        }
    }

}

@Serializable
data class NavigationChatRoomId(val roomId: Int, val roomName: String)

@Serializable
data class ChatCreationUpdate(val roomId: Int = -1, val roomName: String?)