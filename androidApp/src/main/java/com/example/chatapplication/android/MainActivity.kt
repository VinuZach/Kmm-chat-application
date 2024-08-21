package com.example.chatapplication.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chatapplication.android.Authentication.ui.theme.ChatApplicationTheme
import com.example.chatapplication.android.chat.ChatGroupAndListingMain
import com.example.chatapplication.android.chat.ChatScreen
import com.example.chatapplication.android.chat.ChatViewModel
import com.example.chatapplication.cacheConfig.CacheManager
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatApplicationTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainPage()
                }
            }
        }
    }

    @Preview
    @Composable
    fun MainPage() {
        val userName = remember {
            mutableStateOf("")
        }
        val chatViewModel: ChatViewModel = viewModel()
        val navController = rememberNavController()
        val userNameFlow = chatViewModel.retrieveUserNameFromCache(CacheManager.getManger(applicationContext))
        userNameFlow.invokeOnCompletion {
            userNameFlow.getCompleted()?.let {
                userName.value = it
            }
        }
        NavHost(navController = navController, startDestination = "group_and_chat_listing") {

            composable<NavigationChatRoomId> {
                val chatRoomId = it.toRoute<NavigationChatRoomId>()
                Log.d("asasdsadsad", "directChat: ")
                ChatScreen(userName = userName.value, viewModel = chatViewModel, roomId = chatRoomId.roomId, roomName = chatRoomId.roomName)

            }
            composable("group_and_chat_listing") {
                ChatGroupAndListingMain(viewModel = chatViewModel, redirectToRoomById = { roomId, roomName ->
                    navController.navigate(NavigationChatRoomId(roomId, roomName))
                }, redirectToRoomDetails = {
                    navController.navigate("room_details_page")
                }, createNewChat =
                {
                    navController.navigate("room_details_page")
                })

            }


        }
    }

}

@Serializable
data class NavigationChatRoomId(val roomId: Int, val roomName: String)

@Serializable
data class ChatCreationUpdate(val roomId: Int?, val roomName: String?)