package com.zeroclone.app.presentation.ui.screens

import androidx.compose.runtime.*
import com.zeroclone.app.data.remote.Provider

@Composable
fun ZeroChatNavHost() {
    var isAuthenticated by remember { mutableStateOf(false) }
    val selectedProvider = Provider.DEEPSEEK // Cambiar dinámicamente en el futuro

    if (isAuthenticated) {
        ChatScreen(provider = selectedProvider)
    } else {
        LoginScreen(
            provider = selectedProvider,
            onAuthenticated = { isAuthenticated = true }
        )
    }
}
