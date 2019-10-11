package com.viafoura.webviewexample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View.FOCUSABLE
import android.view.View.NOT_FOCUSABLE
import android.webkit.*



class MainActivity : AppCompatActivity() {

    private val url = "https://canary.click/eng2/comments.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setWebViewSettings(webView.settings)

        // Enable hardware acceleration
        webView.clearCache(false)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.webChromeClient = WebViewWebChromeClient()
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            // If web view have back history, then go to the web view back history
            webView.goBack()
        }
    }

    inner class WebViewWebChromeClient : WebChromeClient() {
        // Viafoura's social login uses  popup windows. By default Android webview will not handle them
        // so some extra code is needed to handle this case
        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            // For the sake of convenience it is easy to load the popup window that handles social
            // login in a webview in a dialog window
            val dialogWebView = DialogWebView(this@MainActivity)
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

        // When the webview closes, it should remove itself from the dialog
        override fun onCloseWindow(window: WebView?) {
            val parent = window?.parent as WebView
            parent.removeView(window)
        }
    }

    // This is needed to ensure that the soft keyboard appears in the dialog window
    // so the user can enter login credentials
    open inner class DialogWebView(context: Context?) : WebView(context) {
        override fun onCheckIsTextEditor(): Boolean {
            return true
        }
    }

    inner class DialogViewWebViewClient(private val dialog: AlertDialog) : WebViewClient() {
        // The dialog should be dismissed after the login process has finished
        override fun onPageFinished(view: WebView?, url: String?) {
            if (url?.contains("/callback.php") == true) {
                dialog.dismiss()
            }

            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (isUrlACancelRequest(request?.url)) {
                dialog.dismiss()
                return true
            }

            return super.shouldOverrideUrlLoading(view, request)
        }

        // Required for older versions of Android
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (isUrlACancelRequest(Uri.parse(url))) {
                dialog.dismiss()
                return true
            }

            return super.shouldOverrideUrlLoading(view, url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebViewSettings(settings: WebSettings) {
        // Enable and set up web view cache
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

    private fun isUrlACancelRequest(url: Uri?): Boolean {
        return url?.getQueryParameter("error_reason")?.equals("user_denied") == true
                || url?.getQueryParameter("error")?.equals("user_cancelled_login") == true
                || url?.getQueryParameter("denied") != null
    }
}
