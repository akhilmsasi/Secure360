package com.cet.secure360

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Left Side: Car Image and Branding
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Glow
            Box(
                modifier = Modifier
                    .size(500.dp)
                    .background(Brush.radialGradient(listOf(AccentTeal.copy(alpha = 0.15f), Color.Transparent)))
                    .blur(60.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Secure360 Pro",
                    color = AccentTeal,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "NEXT-GEN VEHICLE SECURITY",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(Modifier.height(40.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.car_image),
                    contentDescription = "Car Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
                
                Spacer(Modifier.height(40.dp))
                
                // Feature List
                FeatureItem(Icons.Default.CloudSync, "Real-time Cloud backup & synchronization")
                FeatureItem(Icons.Default.PrecisionManufacturing, "AI-Powered Incident Detection")
                FeatureItem(Icons.Default.Security, "24/7 Intelligent Sentry Monitoring")
            }
        }

        // Right Side: Login Form
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(SurfaceDark.copy(alpha = 0.5f))
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            LoginScreen(onNavigateToRegister = onNavigateToRegister, onLoginSuccess = onLoginSuccess)
        }
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, null, tint = AccentTeal, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { 40 })
    ) {
        Column(
            modifier = Modifier
                .width(400.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Welcome Back",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sign in to access your vehicle dashboard",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            // Error Message
            if (viewModel.loginError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentRed.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = viewModel.loginError ?: "",
                        color = AccentRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // Input Fields
            Text("Username", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("admin_secure", color = TextSecondary.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = SurfaceVariant,
                    cursorColor = AccentTeal,
                    focusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Text("Password", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••", color = TextSecondary.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = SurfaceVariant,
                    cursorColor = AccentTeal,
                    focusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f)
                ),
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

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Forgot password?",
                color = AccentTeal,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { }
            )

            Spacer(Modifier.height(40.dp))

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
                shape = RoundedCornerShape(12.dp)
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

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Don't have an account?", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = " Register Vehicle",
                    color = AccentTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
