package com.zeroclone.app.presentation.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zeroclone.app.data.remote.Provider
import com.zeroclone.app.presentation.viewmodel.ChatViewModel
import com.zeroclone.app.service.ZeroTokenExtractor
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoginScreen(
    provider: Provider,
    onAuthenticated: () -> Unit,
    viewModel: ChatViewModel = koinViewModel { parametersOf(provider) }
) {
    var captured by remember { mutableStateOf(false) }
    var extractor by remember { mutableStateOf<ZeroTokenExtractor?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Iniciando sesión en ${provider.name}...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Completa el login manualmente. La app extraerá la sesión automáticamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        val ext = ZeroTokenExtractor(this)
                        extractor = ext

                        ext.configureWebView(provider) { creds ->
                            if (!captured) {
                                captured = true
                                viewModel.updateSession(creds)
                                onAuthenticated()
                            }
                        }

                        loadUrl(provider.loginUrl)

                        postDelayed({
                            ext.injectStealthAndExtract(provider)
                        }, 1500)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                extractor?.injectStealthAndExtract(provider)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forzar extracción de sesión")
        }
    }
}
