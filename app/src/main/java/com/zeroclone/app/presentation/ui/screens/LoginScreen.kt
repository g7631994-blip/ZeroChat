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
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var extractor by remember { mutableStateOf<ZeroTokenExtractor?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Iniciando sesión en ${provider.name}...", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Completa el login manualmente. La app extraerá la sesión automáticamente.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewRef = this
                        val ext = ZeroTokenExtractor(this)
                        extractor = ext
                        
                        ext.configureWebView { creds ->
                            viewModel.updateSession(creds.cookies, creds.userAgent)
                            onAuthenticated()
                        }
                        
                        loadUrl(provider.loginUrl)
                        // Inyectar tras un pequeño delay para asegurar carga del DOM
                        postDelayed({ ext.injectStealthAndExtract() }, 1500)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { 
                extractor?.injectStealthAndExtract() // Forzar re-intento de extracción
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forzar Extracción de Sesión")
        }
    }
}
