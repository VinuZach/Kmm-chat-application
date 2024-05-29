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
import com.example.chatapplication.android.Authentication.ui.theme.ChatApplicationTheme
import com.example.chatapplication.android.chat.ChatGroupAndListingMain
import com.example.chatapplication.android.chat.ChatScreen
import com.example.chatapplication.android.chat.ChatViewModel

class MainActivity : ComponentActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
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
    fun MainPage()
    {

        val chatViewModel: ChatViewModel = viewModel()
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "directChat") {

            composable(route = "directChat") {
                Log.d("asasdsadsad", "directChat: ")
              //  ChatScreen(userName = "userName", viewModel = chatViewModel)
                ChatGroupAndListingMain()
            }
        }
    }
}