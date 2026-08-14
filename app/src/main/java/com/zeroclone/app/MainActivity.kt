package com.zeroclone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zeroclone.app.presentation.ui.screens.ChatScreen
import com.zeroclone.app.presentation.ui.screens.LoginScreen
import com.zeroclone.app.presentation.ui.theme.ZeroChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZeroChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZeroChatNavGraph()
                }
            }
        }
    }
}

@Composable
fun ZeroChatNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onAuthenticated = { navController.navigate("chat") })
        }
        composable("chat") {
            ChatScreen(onSessionExpired = {
                navController.popBackStack("login", inclusive = false)
            })
        }
    }
}
