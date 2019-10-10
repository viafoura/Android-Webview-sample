package com.viafoura.webviewexample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Message
import android.webkit.*

class MainActivity : AppCompatActivity() {

    private val url = "https://canary.click/eng2/comments.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setWebViewSettings(webView.settings)

        // This helps with testing, should be removed when we push a final version
        CookieManager.getInstance().removeAllCookies {
            // Enable hardware acceleration
            webView.clearCache(false)
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            webView.webChromeClient = WebViewWebChromeClient()
            webView.loadUrl(url)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            // If web view have back history, then go to the web view back history
            webView.goBack()
        }
    }

    inner class WebViewWebChromeClient : WebChromeClient() {
        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            val dialogWebView = WebView(this@MainActivity)
            val dialog = AlertDialog.Builder(this@MainActivity).create()

            setWebViewSettings(dialogWebView.settings)
            dialogWebView.webViewClient = DialogViewWebViewClient(dialog)

            dialog.setView(dialogWebView)
            dialog.show()
            dialog.setOnDismissListener { dialogWebView.destroy() }
            // Destroy the web view when a user clicks the OS back button which closes the dialog and dispatches an event handled here
            dialog.setOnCancelListener { dialogWebView.destroy() }

            val transport = resultMsg?.obj as WebView.WebViewTransport
            transport.webView = dialogWebView
            resultMsg.sendToTarget()

            return true
        }

        override fun onCloseWindow(window: WebView?) {
            val parent = window?.parent as WebView
            parent.removeView(window)
        }
    }

    inner class DialogViewWebViewClient(private val dialog: AlertDialog) : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            if (url?.contains("/callback.php") == true) {
                dialog.dismiss()
            }

            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            when {
                // User clicked Facebook cancel button
                request?.url?.getQueryParameter("error_reason")?.equals("user_denied") == true -> {
                    dialog.dismiss()
                    return true
                }
                // User clicked Linked In cancel button
                request?.url?.getQueryParameter("error")?.equals("user_cancelled_login") == true -> {
                    dialog.dismiss()
                    return true
                }
                // User clicked Twitter cancel button
                request?.url?.getQueryParameter("denied") != null -> {
                    dialog.dismiss()
                    return true
                }
                else -> return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebViewSettings(settings: WebSettings) {
        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)

        // Enable zooming in web view
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = true

        // Enable safe browsing if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true  // api 26
        }

        // Important for Viafoura to work
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        settings.loadsImagesAutomatically = true
        settings.blockNetworkImage = false
        settings.setSupportMultipleWindows(true)

        // Important for Google social login to work as it gives an error page otherwise (Chrome mobile UA)
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36"
    }
}
