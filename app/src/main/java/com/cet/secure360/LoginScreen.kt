package com.cet.secure360

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun LoginMainScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A021A)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){
        LoginScreen(onNavigateToRegister = onNavigateToRegister, onLoginSuccess = onLoginSuccess)
    }
}


@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    val SuccessGreen = Color(0xFF4CAF50)
    val ErrorRed = Color(0xFFFF6B6B)
    val DefaultGray = Color.Gray
    val DefaultWhite = Color.White
    val IndicatorActiveColor = Color(0xFFF5B81E)

    Column(
        modifier = Modifier
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Secure",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Welcome Back",
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            modifier = Modifier
                .padding(top = 40.dp)
        )

        Text(
            text = "Let's Get You In To Secure",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 15.dp)
        )

        if (viewModel.loginError != null) {
            Text(
                text = viewModel.loginError ?: "",
                color = ErrorRed,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter your Username") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            ) ,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Username Icon",
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter your Password") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DefaultWhite,
                unfocusedTextColor = DefaultWhite,
                cursorColor = DefaultWhite,
                focusedBorderColor = DefaultWhite,
                unfocusedBorderColor = DefaultGray,
                focusedLabelColor = DefaultWhite,
                unfocusedLabelColor = DefaultGray
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = DefaultGray
                )
            },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = DefaultGray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )


        Text(
            text = "Forgot Password?",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp)
        )

        Card(
            modifier = Modifier
                .padding(top = 30.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clickable(enabled = !viewModel.isLoading) {
                    viewModel.loginUser(username, password, context) { success ->
                        if (success) {
                            onLoginSuccess()
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(IndicatorActiveColor),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text(
                        text = "Login",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't Have An Account..?",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Text(
                text = "  Create Account",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    onNavigateToRegister()
                }
            )
        }
    }
}