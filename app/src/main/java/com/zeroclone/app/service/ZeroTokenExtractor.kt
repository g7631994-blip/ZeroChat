package com.zeroclone.app.service

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.zeroclone.app.domain.model.Provider
import com.zeroclone.app.domain.model.SessionCredentials
import kotlinx.serialization.json.Json

class ZeroTokenExtractor(private val webView: WebView) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    fun configureWebView(
        provider: Provider,
        onResult: (SessionCredentials) -> Unit
    ) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true

            val ua = userAgentString ?: ""
            userAgentString = ua
                .replace("; wv", "")
                .replace("Mobile", "eliboM")
        }

        webView.addJavascriptInterface(
            JsBridge(provider, onResult),
            "AndroidBridge"
        )
    }

    fun injectStealthAndExtract(provider: Provider) {
        val js = buildStealthJs(provider)
        webView.evaluateJavascript(js, null)
    }

    private fun buildStealthJs(provider: Provider): String {
        val stealth = """
            (function() {
                if (window.__zeroChatStealth) return;
                window.__zeroChatStealth = true;

                Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
                window.chrome = { runtime: {} };

                const originalQuery = window.navigator.permissions.query;
                window.navigator.permissions.query = (parameters) => (
                    parameters.name === 'notifications' ?
                    Promise.resolve({ state: Notification.permission }) :
                    originalQuery(parameters)
                );
            })();
        """.trimIndent()

        val capture = when (provider) {
            Provider.CHATGPT -> """
                (function() {
                    if (window.__zeroChatCapture) return;
                    window.__zeroChatCapture = true;

                    async function zeroCapture() {
                        try {
                            const res = await fetch('/api/auth/session', {credentials: 'include'});
                            if (!res.ok) return;

                            const ct = res.headers.get('content-type') || '';
                            if (!ct.includes('application/json')) return;

                            const data = await res.json();

                            if (data && data.accessToken) {
                                clearInterval(window.__zeroChatTimer);

                                AndroidBridge.postCredentials(JSON.stringify({
                                    cookies: document.cookie,
                                    userAgent: navigator.userAgent,
                                    provider: '${provider.name}',
                                    accessToken: data.accessToken
                                }));
                            }
                        } catch (e) {}
                    }

                    window.__zeroChatTimer = setInterval(zeroCapture, 2500);
                    setTimeout(zeroCapture, 1000);
                })();
            """.trimIndent()

            Provider.CLAUDE -> """
                (function() {
                    if (window.__zeroChatCapture) return;
                    window.__zeroChatCapture = true;

                    async function zeroCapture() {
                        try {
                            const res = await fetch('/api/organizations', {credentials: 'include'});
                            if (!res.ok) return;

                            const text = await res.text();
                            const match = text.match(/"uuid"\s*:\s*"([a-f0-9-]+)"/i);
                            const org = match ? match[1] : null;

                            const path = window.location.pathname || '';
                            const convoMatch = path.match(/chat\/([a-f0-9-]+)/i);
                            const conversationId = convoMatch ? convoMatch[1] : null;

                            if (org) {
                                clearInterval(window.__zeroChatTimer);

                                AndroidBridge.postCredentials(JSON.stringify({
                                    cookies: document.cookie,
                                    userAgent: navigator.userAgent,
                                    provider: '${provider.name}',
                                    organizationId: org,
                                    conversationId: conversationId,
                                    rawSession: text.substring(0, Math.min(text.length, 4000))
                                }));
                            }
                        } catch (e) {}
                    }

                    window.__zeroChatTimer = setInterval(zeroCapture, 3000);
                    setTimeout(zeroCapture, 1500);
                })();
            """.trimIndent()

            else -> """
                (function() {
                    if (window.__zeroChatCapture) return;
                    window.__zeroChatCapture = true;

                    function zeroCapture() {
                        try {
                            const cookies = document.cookie;

                            const ready = cookies.length > 50 ||
                                document.querySelector('[class*="chat"]') ||
                                document.querySelector('[id*="chat"]');

                            if (ready) {
                                clearInterval(window.__zeroChatTimer);
                                AndroidBridge.postCookies(document.cookie, navigator.userAgent);
                            }
                        } catch (e) {}
                    }

                    window.__zeroChatTimer = setInterval(zeroCapture, 2000);
                    setTimeout(zeroCapture, 1000);
                })();
            """.trimIndent()
        }

        return stealth + "\n" + capture
    }

    private inner class JsBridge(
        private val provider: Provider,
        private val onResult: (SessionCredentials) -> Unit
    ) {

        @JavascriptInterface
        fun postCredentials(payload: String) {
            try {
                val creds = json.decodeFromString<SessionCredentials>(payload)
                val safe = creds.copy(provider = provider.name)

                mainHandler.post {
                    onResult(safe)
                }
            } catch (e: Exception) {
                Log.e("ZeroTokenExtractor", "Failed to parse credentials", e)
            }
        }

        @JavascriptInterface
        fun postCookies(cookies: String, userAgent: String) {
            val creds = SessionCredentials(
                cookies = cookies,
                userAgent = userAgent,
                provider = provider.name
            )

            mainHandler.post {
                onResult(creds)
            }
        }
    }
}
