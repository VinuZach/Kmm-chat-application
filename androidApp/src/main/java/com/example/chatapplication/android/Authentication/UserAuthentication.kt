package com.example.chatapplication.android.Authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.constraintlayout.compose.Dimension
import com.example.chatapplication.ApiConfig.model.UserAuthenticationResponse
import com.example.chatapplication.ApiResponseObtained
import com.example.chatapplication.cacheConfig.CacheManager

interface onNavigate {
    fun onTaskPerformed(navigate: String)
}

@Preview("FullPage")
@Composable
fun UserAuthenticationFullPage(authenticateViewModel: AuthenticationViewModel = AuthenticationViewModel(), onNavigate: onNavigate? = null) {


    ConstraintLayout(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)) {
        val (logoSection, detailsSection) = createRefs()
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .constrainAs(logoSection)
                {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(detailsSection.top)
                },
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "asdad")
        }
        Column(modifier = Modifier
                .background(MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .fillMaxWidth()
                .padding(5.dp)
                .constrainAs(detailsSection)
                {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                }, verticalArrangement = Arrangement.Top) {

            UserLoginSection(authenticateViewModel, onNavigate)
        }

    }
}

@Composable
fun UserLoginSection(authenticateViewModel: AuthenticationViewModel, onNavigate: onNavigate?) {
    val context = LocalContext.current

    ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(top = 15.dp, bottom = 25.dp)) {

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
        Column(modifier = Modifier
                .fillMaxWidth()
                .constrainAs(userDetailsFieldsRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(buttonRef.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = 30.dp)
        ) {
            Text(text = "Welcome", modifier = Modifier
                    .padding(vertical = 20.dp)
                    .fillMaxWidth(),
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily, fontWeight = FontWeight.Normal,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                color = MaterialTheme.colorScheme.secondary
            )

            if (!isLoginPageState.value)
                TextField(colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    disabledTextColor =MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary),
                    label = { Text(text = "Email", fontFamily = MaterialTheme.typography.displayMedium.fontFamily) },

                    value = emailState.value, onValueChange = {
                        emailState.value = it
                    }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))

            TextField(colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.secondary,
                disabledTextColor =MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary), label =
            {
                Text(text = "User Name",fontFamily = MaterialTheme.typography.displayMedium.fontFamily)
            },
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp), value = userNameState.value, onValueChange = {
                    userNameState.value = it
                }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true, placeholder = {

                })

            val isPasswordVisible = remember {
                mutableStateOf(false)
            }

            TextField(colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.secondary,
                disabledTextColor =MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,), value = passWordState.value, onValueChange = {
                passWordState.value = it
            }, modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), singleLine = true,
                visualTransformation = if (isPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                label = {
                    Text(text = "Password",fontFamily = MaterialTheme.typography.displayMedium.fontFamily)
                },
                trailingIcon = {
                    val trailingIcon = if (isPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { isPasswordVisible.value = !isPasswordVisible.value }) {
                        Icon(trailingIcon, contentDescription = "password")
                    }
                })


            if (isLoginPageState.value) Text(text = "Forgot password", modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.End), fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                color = MaterialTheme.colorScheme.secondary,
                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                fontSize = MaterialTheme.typography.labelSmall.fontSize)


        }

        Button(onClick = {
            if (isLoginPageState.value) {
                if (userNameState.value.isEmpty() && passWordState.value.isEmpty()) {
                    Toast.makeText(context, "enter values", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                validateUser(context, authenticateViewModel, onNavigate, userNameState.value, passWordState.value)
            } else {
                if (userNameState.value.isEmpty() && passWordState.value.isEmpty() && emailState.value.isEmpty()) {
                    Toast.makeText(context, "enter values", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                createNewUser(context, authenticateViewModel, onNavigate, userNameState.value, passWordState.value, emailState.value)

            }
        }, modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 40.dp, top = 80.dp)
                .constrainAs(buttonRef) {

                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(newAccountRef.top)
                }
        ) {

            Text(text = if (isLoginPageState.value) "Login" else "Register",
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily, fontSize = 18.sp, color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(vertical = 5.dp))

        }
        Text(text = if (isLoginPageState.value) "Register a new account " else "Login with existing account",
            fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
            modifier = Modifier
                    .constrainAs(newAccountRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                    }

                    .clickable {
                        isLoginPageState.value = !isLoginPageState.value
                    }, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.tertiary)
    }

}

fun createNewUser(context: Context, authenticateViewModel: AuthenticationViewModel, onNavigate: onNavigate?, userName: String,
    password: String, email: String) {
    authenticateViewModel.createNewUser(userName, password, email, object : ApiResponseObtained {
        override fun onResponseObtained(isSuccess: Boolean, response: Any?) {
            Log.d("asdasdwe", "verifyUserDetails: asdasd $isSuccess   ... $response")
        }

    })

}


fun validateUser(context: Context, authenticateViewModel: AuthenticationViewModel, onNavigate: onNavigate?, username: String,
    password: String) {


    authenticateViewModel.verifyUserDetails(username, password, object : ApiResponseObtained {
        override fun onResponseObtained(isSuccess: Boolean, response: Any?) {
            Log.d("asdasdwe", "verifyUserDetails: asdasd $isSuccess   ... $response")
            if (isSuccess) {
                val cacheManager = CacheManager.getManger(context)
                authenticateViewModel.saveUserNameToCache(cacheManager, username)

                onNavigate?.onTaskPerformed("complete")
            } else {
                val authenticationResponse = response as UserAuthenticationResponse?
                val errorMessage = authenticationResponse?.message ?: run {
                    "server not reachable"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }


    })

}


