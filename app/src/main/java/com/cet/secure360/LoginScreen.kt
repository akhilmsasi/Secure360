package com.cet.secure360

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Color Palette (Matching Dashcam Dashboard) ─────────────────────────────

private val BackgroundDark   = Color(0xFF1A1D22)
private val SurfaceDark      = Color(0xFF22262E)
private val SurfaceVariant   = Color(0xFF2A2F39)
private val AccentTeal       = Color(0xFF00C9A7)
private val AccentRed        = Color(0xFFE53935)
private val TextPrimary      = Color(0xFFECEFF4)
private val TextSecondary    = Color(0xFF8D93A1)

@Composable
fun LoginMainScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Background Decorative Elements
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(Brush.radialGradient(listOf(AccentTeal.copy(alpha = 0.1f), Color.Transparent)))
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .background(Brush.radialGradient(listOf(AccentRed.copy(alpha = 0.05f), Color.Transparent)))
                .blur(50.dp)
        )

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

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Section
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Secure360 Pro",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Intelligent Vehicle Monitoring",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(32.dp))

                // Error Message
                if (viewModel.loginError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentRed.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = viewModel.loginError ?: "",
                            color = AccentRed,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Input Fields
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Enter your username") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = SurfaceVariant,
                        focusedLabelColor = AccentTeal,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentTeal,
                        focusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f)
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.AlternateEmail, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = SurfaceVariant,
                        focusedLabelColor = AccentTeal,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentTeal,
                        focusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f)
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.LockOpen, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Forgot Password?",
                    color = AccentTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { /* Reset logic */ }
                )

                Spacer(Modifier.height(32.dp))

                // Login Button
                Button(
                    onClick = {
                        viewModel.loginUser(username, password, context) { success ->
                            if (success) onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !viewModel.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        disabledContainerColor = AccentTeal.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "SIGN IN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "New user?", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = " Create Account",
                        color = AccentTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
