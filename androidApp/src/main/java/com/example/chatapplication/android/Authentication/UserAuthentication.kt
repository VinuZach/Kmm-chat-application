package com.example.chatapplication.android.Authentication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.chatapplication.ApiConfig.UserAuthenticationResponse
import com.example.chatapplication.ApiResponseObtained
import com.example.chatapplication.android.MainActivity
import kotlin.coroutines.coroutineContext
import kotlin.math.sin

interface onNavigate
{
    fun onTaskPerformed(navigate: String)
}

@Preview("FullPage")
@Composable
fun UserAuthenticationFullPage(authenticateViewModel: AuthenticationViewModel = AuthenticationViewModel(), onNavigate: onNavigate? = null)
{

    val context = LocalContext.current

    ConstraintLayout(modifier = Modifier.fillMaxSize().imePadding()) {
        val (userDetailsFieldsRef, buttonRef, newAccountRef) = createRefs()
        val userNameState = remember {
            mutableStateOf("aaa@aaa.com")
        }
        val passWordState = remember {
            mutableStateOf("aaa")
        }
        val emailState = remember {
            mutableStateOf("")
        }
        val isLoginPageState = remember {
            mutableStateOf(true)
        }

        Column(modifier = Modifier.constrainAs(userDetailsFieldsRef) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom, margin = 100.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)

        }.padding(top = 20.dp)) {
            if (!isLoginPageState.value) TextField(value = emailState.value, onValueChange = {
                emailState.value = it
            }, modifier = Modifier.padding(bottom = 20.dp), placeholder = {
                Text(text = "Email")
            }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

            TextField(value = userNameState.value, onValueChange = {
                userNameState.value = it
            }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true, placeholder = {
                Text(text = "User Name")
            })

            val isPasswordVisible = remember {
                mutableStateOf(false)
            }

            TextField(value = passWordState.value, onValueChange = {
                passWordState.value = it
            }, modifier = Modifier.padding(top = 10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), singleLine = true,
                visualTransformation = if (isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val trailingIcon = if (isPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { isPasswordVisible.value = !isPasswordVisible.value }) {
                        Icon(trailingIcon, contentDescription = "password")
                    }
                }, placeholder = {
                    Text(text = "Password")
                })


            if (isLoginPageState.value) Text(text = "Forgot password", modifier = Modifier.padding(top = 20.dp).align(Alignment.End))


        }

        Button(onClick = {
            if (isLoginPageState.value)
            {
                if (userNameState.value.isEmpty() && passWordState.value.isEmpty())
                {
                    Toast.makeText(context, "enter values", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                validateUser(context, authenticateViewModel, onNavigate, userNameState.value, passWordState.value)
            }
            else
            {
                if (userNameState.value.isEmpty() && passWordState.value.isEmpty() && emailState.value.isEmpty())
                {
                    Toast.makeText(context, "enter values", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                createNewUser(context, authenticateViewModel, onNavigate, userNameState.value, passWordState.value, emailState.value)

            }
        }, modifier = Modifier.fillMaxWidth(.6f).constrainAs(buttonRef) {
            top.linkTo(userDetailsFieldsRef.bottom, margin = 100.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(top = 10.dp)) {

            Text(text = if (isLoginPageState.value) "Login" else "Register")

        }

        Text(text = if (isLoginPageState.value) "Create new account ." else "Account already exist",
            modifier = Modifier.constrainAs(newAccountRef) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.padding(10.dp).clickable {
                isLoginPageState.value = !isLoginPageState.value
            }, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = Color.Yellow)
    }


}

fun createNewUser(context: Context, authenticateViewModel: AuthenticationViewModel, onNavigate: onNavigate?, userName: String,
    password: String, email: String)
{
    authenticateViewModel.createNewUser(userName, password, email, object : ApiResponseObtained
    {
        override fun onResponseObtained(isSuccess: Boolean, response: Any?)
        {
            Log.d("asdasdwe", "verifyUserDetails: asdasd $isSuccess   ... $response")
        }

    })

}

fun validateUser(context: Context, authenticateViewModel: AuthenticationViewModel, onNavigate: onNavigate?, username: String,
    password: String)
{
    authenticateViewModel.verifyUserDetails(username, password, object : ApiResponseObtained
    {
        override fun onResponseObtained(isSuccess: Boolean, response: Any?)
        {
            Log.d("asdasdwe", "verifyUserDetails: asdasd $isSuccess   ... $response")
            if (isSuccess)
            {
               onNavigate?.onTaskPerformed("complete")
            }
            else
            {
                val authenticationResponse = response as UserAuthenticationResponse?
                val errorMessage = authenticationResponse?.message ?: run {
                    "server not reachable"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

    })
}
