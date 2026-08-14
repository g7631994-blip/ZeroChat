package com.zeroclone.app.presentation.ui.screens

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zeroclone.app.data.local.CredentialStore
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.service.SessionCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    credentialStore: CredentialStore = koinInject()
) {
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("ZeroChat", color = Color(0xFFE3E3E3), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (selectedProvider == null) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(Provider.entries.toList()) { provider ->
                        Card(
                            modifier = Modifier.height(80.dp).clickable { selectedProvider = provider },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = provider.displayName,
                                    color = Color(0xFFE3E3E3),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        text = "Inicia sesión en ${selectedProvider!!.displayName}",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp
                    )
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        val script = """
                                            (function() {
                                                try {
                                                    var data = {
                                                        cookies: document.cookie,
                                                        localStorage: JSON.parse(JSON.stringify(localStorage)),
                                                        userAgent: navigator.userAgent
                                                    };
                                                    ZeroChatBridge.onCredentialsExtracted(JSON.stringify(data));
                                                } catch(e) {}
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(script, null)
                                    }
                                }
                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun onCredentialsExtracted(json: String) {
                                        try {
                                            val obj = JSONObject(json)
                                            val creds = SessionCredentials(
                                                cookies = obj.optString("cookies", ""),
                                                localStorage = obj.optJSONObject("localStorage")?.toString() ?: "{}",
                                                userAgent = obj.optString("userAgent", "")
                                            )
                                            CoroutineScope(Dispatchers.IO).launch {
                                                credentialStore.save(selectedProvider!!, creds)
                                                onAuthenticated()
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }, "ZeroChatBridge")
                                loadUrl(selectedProvider!!.baseUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
