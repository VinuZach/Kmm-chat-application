package com.example.chatapplication.android.Authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapplication.android.theme.ChatApplicationTheme
import com.example.chatapplication.android.MainActivity
import com.example.chatapplication.cacheConfig.CacheManager
import com.example.chatapplication.cacheConfig.USER_NAME
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AuthenticationActivity : ComponentActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            ChatApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current


                    LaunchedEffect(key1 = true) {
                        coroutineScope.launch {

//                            val cacheManager = CacheManager.getManger(context = context)
//                            val userNameFlow = cacheManager.data.firstOrNull()?.toPreferences()?.get(USER_NAME)
//
//                          if (userNameFlow!=null)
//                          {
//                              finish()
//                              startActivity(Intent(this@AuthenticationActivity, MainActivity::class.java))
//                          }
                        }
                    }


                    NavHost(navController = navController, startDestination = "user_authentication") {

                        composable(route = "user_authentication") {
                            UserAuthenticationFullPage(onNavigate = object : onNavigate
                            {
                                override fun onTaskPerformed(navigate: String)
                                {
                                    if (navigate == "complete")
                                    {
                                        finish()
                                        startActivity(Intent(this@AuthenticationActivity, MainActivity::class.java))
                                    }
                                    else navController.navigate(navigate)
                                }

                            })
                        }
                    }
                }
            }
        }
    }

}
