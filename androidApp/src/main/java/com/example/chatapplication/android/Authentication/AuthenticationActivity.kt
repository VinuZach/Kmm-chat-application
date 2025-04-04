package com.example.chatapplication.android.Authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapplication.android.chat.MainActivity
import com.example.chatapplication.android.theme.ChatApplicationTheme
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.DataStoreInstance


import kotlinx.coroutines.launch

class AuthenticationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            ChatApplicationTheme() {

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current


                    LaunchedEffect(key1 = true) {
                        coroutineScope.launch {
                      val cacheManager = CacheManager(DataStoreInstance.getManger(context))
                           val userName=cacheManager.retrieveDataFromCache(cacheManager.USER_NAME)
                            if (userName != null) {
                                finish()
                                startActivity(Intent(this@AuthenticationActivity, MainActivity::class.java))
                            }
                        }
                    }


                    NavHost(navController = navController, startDestination = "user_authentication") {

                        composable(route = "user_authentication") {
                            UserAuthenticationFullPage(onNavigate = object : onNavigate {
                                override fun onTaskPerformed(navigate: String) {
                                    if (navigate == "complete") {
                                        finish()
                                        startActivity(Intent(this@AuthenticationActivity, MainActivity::class.java))
                                    } else navController.navigate(navigate)
                                }

                            })
                        }
                    }
                }
            }
        }
    }

}
