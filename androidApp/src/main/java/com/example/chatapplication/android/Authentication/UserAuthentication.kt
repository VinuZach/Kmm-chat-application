package com.example.chatapplication.android.Authentication

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.chatapplication.ApiResponseObtained

@Preview("FullPage")
@Composable
fun UserAuthenticationFullPage(authenticateViewModel: AuthenticationViewModel = AuthenticationViewModel())
{

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (userDetailsFieldsRef, buttonRef, newAccountRef) = createRefs()

        Column(modifier = Modifier.constrainAs(userDetailsFieldsRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom, margin = 100.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)

            }.padding(top = 20.dp)) {
            val userNameState = remember {
                mutableStateOf("")
            }
            val passWordState = remember {
                mutableStateOf("")
            }
            TextField(value = userNameState.value, onValueChange = {
                userNameState.value = it
            })
            TextField(value = passWordState.value, onValueChange = {
                passWordState.value = it
            }, modifier = Modifier.padding(top = 15.dp))
            Text(text = "Forgot password", modifier = Modifier.padding(top = 20.dp).align(Alignment.End))


        }

        Button(onClick = {
            authenticateViewModel.verifyUserDetails("aaa@aaa.com", "aaa", object : ApiResponseObtained
            {
                override fun onResponseObtained(isSuccess: Boolean, response: Any?)
                {
                    Log.d("asdasdwe", "verifyUserDetails: asdasd $isSuccess   ... $response")
                }

            })
        }, modifier = Modifier.fillMaxWidth(.6f).constrainAs(buttonRef) {
                top.linkTo(userDetailsFieldsRef.bottom, margin = 100.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(top = 10.dp)) {
            Text(text = "Login")
        }

        Text(text = "Create new account .", modifier = Modifier.constrainAs(newAccountRef) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(10.dp), fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = Color.Yellow)
    }


}