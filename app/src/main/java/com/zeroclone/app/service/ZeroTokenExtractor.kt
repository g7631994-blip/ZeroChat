package com.zeroclone.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class SessionCredentials(
    val cookies: String,
    val localStorage: String,
    val userAgent: String
)

class ZeroTokenExtractor(private val context: Context) {

    @SuppressLint("SetJavaScriptEnabled")
    fun extractSession(providerUrl: String): Flow<SessionCredentials> = callbackFlow {
        val webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

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
                        trySend(creds)
                        close()
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }, "ZeroChatBridge")

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

                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        close(RuntimeException("Load failed: ${error?.description}"))
                    }
                }
            }

            loadUrl(providerUrl)
        }

        awaitClose {
            webView.stopLoading()
            webView.destroy()
        }
    }
}
