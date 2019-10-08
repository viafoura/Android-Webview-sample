package com.viafoura.webviewexample

import android.app.AlertDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Message
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import androidx.constraintlayout.widget.ConstraintLayout








class MainActivity : AppCompatActivity() {

    private val url = "https://canary.click/eng2/comments.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the web view settings instance
        val settings = webView.settings

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

        // Enable hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Important for Viafoura to work
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        settings.loadsImagesAutomatically = true
        settings.blockNetworkImage = false
        settings.setSupportMultipleWindows(true)

        // Set web view client
        val webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        webView.webViewClient = webViewClient

        webView.webChromeClient = object: WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {


                webView.removeAllViews();
                val newWebView = WebView(this@MainActivity)
                newWebView.settings.domStorageEnabled = true
                newWebView.settings.javaScriptEnabled = true
                newWebView.settings.builtInZoomControls = true
                newWebView.settings.setSupportMultipleWindows(true)
                newWebView.visibility = View.VISIBLE
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        view?.loadUrl(request!!.url.toString())
                        return true
                    }
                }
                newWebView.layoutParams = ConstraintLayout.LayoutParams(200, 200)

//                webView.addView(newWebView, ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

                val builder = AlertDialog.Builder(this@MainActivity).create()
                builder.setTitle("");
                builder.setView(newWebView);
                builder.setButton(
                    "Close"
                ) { dialog, id ->
                    webView.destroy()
                    dialog.dismiss()
                }
                builder.show()
                builder.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.setAcceptThirdPartyCookies(newWebView, true)
                }

                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()

                return true
            }

//            override fun onCloseWindow(window: WebView?) {
//                val parent = window?.parent as WebView
//                parent.removeView(window)
//            }
        }

        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            // If web view have back history, then go to the web view back history
            webView.goBack()
        }
    }
}
