package com.example.chatapplication.android.Authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapplication.android.Authentication.ui.theme.ChatApplicationTheme
import com.example.chatapplication.android.MainActivity

class AuthenticationActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContent {
            ChatApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "user_authentication") {
                        composable(route = "user_authentication") {
                            UserAuthenticationFullPage(onNavigate = object : onNavigate
                            {
                                override fun onTaskPerformed(navigate: String)
                                {
                                    navController.navigate(navigate)
                                }

                            })
                        }
                        composable("logincomplete") {
                            startActivity(Intent(LocalContext.current, MainActivity::class.java))
                        }
                        composable("new_account") {

                        }
                    }
                }
            }
        }
    }

}
