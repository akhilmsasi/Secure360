package com.cet.secure360

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cet.secure360.ui.theme.Secure360Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Secure360Theme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {


    val navController = rememberNavController()


    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE) }
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
//    val viewModel: MainViewModel = viewModel()

    val startDestination = if (isLoggedIn) "home_screen" else "login_screen"



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A021A))
    ) {

        NavHost(navController = navController, startDestination = startDestination) {
            composable("login_screen") {
                LoginMainScreen(
                    onNavigateToRegister = {
//                        navController.navigate("register_screen")
                                           },
                    onLoginSuccess = {
                        navController.navigate("home_screen")
                    }
                )
            }
            composable("home_screen") {
                DashcamScreen()
            }
        }

    }

}