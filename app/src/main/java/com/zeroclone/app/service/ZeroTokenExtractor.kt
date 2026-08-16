package com.zeroclone.app.service

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.zeroclone.app.domain.model.SessionCredentials

class ZeroTokenExtractor(private val webView: WebView) {

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebView(onResult: (SessionCredentials) -> Unit) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            // MÓDULO OMEGA 3: CAMUFLAJE USER-AGENT
            userAgentString = userAgentString
                .replace("; wv", "")
                .replace("Mobile", "eliboM") 
        }

        // Bridge JS -> Kotlin
        webView.addJavascriptInterface(JsBridge(onResult), "AndroidBridge")
    }

    fun injectStealthAndExtract() {
        // Inyección de Mutación de Entorno y Extracción Periódica
        val stealthJs = """
            (function() {
                // Anti-detección básica
                Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
                window.chrome = { runtime: {} };
                
                // Mutación de permisos
                const originalQuery = window.navigator.permissions.query;
                window.navigator.permissions.query = (parameters) => (
                    parameters.name === 'notifications' ?
                    Promise.resolve({ state: Notification.permission }) :
                    originalQuery(parameters)
                );

                // Monitor de inyección de Cookies (Ejecuta cada 2s hasta encontrar sesión)
                const interval = setInterval(() => {
                    const cookies = document.cookie;
                    const ua = navigator.userAgent;
                    // Detectar presencia de tokens de sesión comunes o DOM de chat
                    if (cookies.length > 50 || document.querySelector('[class*="chat"]') || document.querySelector('[id*="chat"]')) {
                        clearInterval(interval);
                        AndroidBridge.postCookies(cookies, ua);
                    }
                }, 2000);
            })();
        """
        webView.evaluateJavascript(stealthJs, null)
    }

    private class JsBridge(private val onResult: (SessionCredentials) -> Unit) {
        @JavascriptInterface
        fun postCookies(cookies: String, ua: String) {
            // Filtrado básico para evitar enviar cookies vacías
            if (cookies.isNotBlank()) {
                onResult(SessionCredentials(cookies, ua, ""))
            }
        }
    }
}
