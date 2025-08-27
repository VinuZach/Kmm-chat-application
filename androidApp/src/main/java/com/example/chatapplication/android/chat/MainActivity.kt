package com.example.chatapplication.android.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chatapplication.android.Authentication.AuthenticationActivity
import com.example.chatapplication.android.theme.ChatApplicationTheme
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.DataStoreInstance
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            ChatApplicationTheme() {
                val systemUiController = rememberSystemUiController()
                val backgroundColor = MaterialTheme.colorScheme.background
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = backgroundColor,
                        darkIcons = false // false = white icons (light content)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

                ChatGroupAndListingMain(
                    viewModel = chatViewModel,
                    redirectToRoomById = { roomId, roomName ->
                        navController.navigate(NavigationChatRoomId(roomId, roomName))
                    },
                    redirectToRoomDetails = {
                        navController.navigate("room_details_page")
                    },
                    createNewChat =
                        { roomId, roomName ->
                            navController.navigate(ChatCreationUpdate(roomId, roomName))
                        },
                    createNewGroup =
                        { groupId, groupName ->
                            navController.navigate(GroupCreationUpdate(groupId, groupName))
                        }, logoutUser = {
                        chatViewModel.clearCacheData(CacheManager(DataStoreInstance.getManger(this@MainActivity)))
                        startActivity(Intent(this@MainActivity, AuthenticationActivity::class.java))
                        finish()
                    }
                )

            }
            composable<NavigationChatRoomId> {
                val chatRoomId = it.toRoute<NavigationChatRoomId>()
                Log.d("asasdsadsad", "directChat: ")
                Scaffold(content = { paddingValues ->
                    ChatScreen(
                        modifier = Modifier.padding(paddingValues),
                        userName = chatViewModel.userName.value,
                        viewModel = chatViewModel,
                        roomId = chatRoomId.roomId,
                        roomName = chatRoomId.roomName,
                        onBackPressed = {
                            navController.popBackStack()
                        })

                })


            }

            composable<ChatCreationUpdate> {
                val chatCreationUpdate = it.toRoute<ChatCreationUpdate>()
                RoomCreationOrUpdate(chatCreationUpdate, chatViewModel, onBackPressed = {
                    navController.popBackStack()
                })
            }
            composable<GroupCreationUpdate> {
                val groupCreationUpdate = it.toRoute<GroupCreationUpdate>()
                GroupCreationOrUpdate(groupCreationUpdate, chatViewModel) {
                    navController.popBackStack()
                }
            }


        }
    }

}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class NavigationChatRoomId(val roomId: Int, val roomName: String)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatCreationUpdate(val roomId: Int = -1, val roomName: String?)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GroupCreationUpdate(val groupId: Int = -1, val groupName: String?)
