## This Project

Using Viafoura's tools in a webview may present some issues. A sample implementation can be found  in this repository that addresses certain issues you may experience when working with Android `WebView`s.

### Social Login

Viafoura Social Login utilizes a popup window in order to complete the oAuth redirect flow. By default `WebView` 
does not enable popups.

[Please examine MainActivity.kt](app/src/main/java/com/viafoura/webviewexample/MainActivity.kt)
for a sample implementation which enables Viafoura social login to work as expected.

The `WebView`'s controller must:

1. In the main `WebView`'s settings
   1. Set: `javaScriptCanOpenWindowsAutomatically = true`
   1. Set: `domStorageEnabled = true`
   1. Set: `javaScriptEnabled = true`
   1. Set: `loadsImagesAutomatically = true`
   1. Set: `blockNetworkImage = false`
   1. Set: `setSupportMultipleWindows(true)`
   1. To enable social login with Google, set: `userAgentString = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36"`
1. A custom `WebViewWebChromeClient` for the `WebView` displaying the page
   1. Implement the `onCreateWindow` handler for that `WebViewWebChromeClient` which opens a `dialog` (or similar subview structure) and child `WebView`
   1. Implement `onCloseWindow` to allow the webview to close
1. In the child `WebView`
   1. Implement `onCheckIsTextEditor` to enable the soft keyboard
   2. Implement an `onPageFinished` handler in its `WebViewClient`

### Other Issues

There may be other issues that appear when attempting to implement Viafoura's tools in a `WebView`.

[Contact our support team at support@viafoura.com for more information and assistance](mailto:support@viafoura.com).
