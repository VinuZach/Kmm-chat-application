package com.example.chatapplication.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatapplication.android.chat.ChatScreen
import com.example.chatapplication.android.chat.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //GreetingView(Greeting().greet())
                    MainPage()
                }
            }
        }
    }
}


@Composable
fun GreetingView(text: String) {
    Text(text = "asdsf")

}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}

@Composable
fun MainPage() {

    val chatViewModel: ChatViewModel = viewModel()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "directChat")
    {

        composable(route = "chat_screen/{username}", arguments = listOf(navArgument(name = "username") {
            type = NavType.StringType
            nullable = true
        }))
        {
            val userName = it.arguments?.getString("username")
            userName?.let { userName ->
                ChatScreen(userName = userName, viewModel = chatViewModel)
            }

        }
        composable(route = "User_Authentication")
        {

        }

        composable(route = "directChat")
        {
            ChatScreen(userName = "userName", viewModel = chatViewModel)

        }
    }
}