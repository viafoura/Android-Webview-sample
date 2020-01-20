## Objective

The objective of this repository is to illustrate how to use a WebView
component to access a page integrating with Viafoura in order to use the social
login capability Viafoura provides.

When developing an app in Android that includes a WebView component, a few
additional steps are needed to ensure that the social login functionality that
Viafoura provides functions properly.

This README and repository describes what to do, why it needs to be done, and
includes source code examples.

## Why

The Android WebView component provides a subset of the functionality of the
Android Webkit browser. The WebView's behavior deviates from regular browsers
in the way that it manages popup windows. When a popup window is opened from a
link in a page within a WebView, it is opened separately in the Android browser,
and not within the same WebView. This breaks the continuity of the web page, and
makes it impossible to return control to the page hosted with a WebView in the
app.

Viafoura's social login process requires a popup window to manage the
authentication process with the appropriate social network. Once the social
network has authorized a user's credentials, the popup window is closed, and the
originating site will now be logged into Viafoura.

## Implementation

Please examine
[MainActivity.kt](app/src/main/java/com/viafoura/webviewexample/MainActivity.kt)
for a sample implementation which enables Viafoura social login to work as
expected.

The `WebView`'s controller must:

1. In the main `WebView`'s settings
   1. Set: `javaScriptCanOpenWindowsAutomatically = true`
   2. Set: `domStorageEnabled = true`
   3. Set: `javaScriptEnabled = true`
   4. Set: `loadsImagesAutomatically = true`
   5. Set: `blockNetworkImage = false`
   6. Set: `setSupportMultipleWindows(true)`
   7. To enable social login with Google, set:
   `userAgentString = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M)
   AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537
   .36"`
2. A custom `WebViewWebChromeClient` for the `WebView` displaying the page
   1. Implement the `onCreateWindow` handler for that `WebViewWebChromeClient`
   which opens a `dialog` (or similar subview structure) and child `WebView`
   2. Implement `onCloseWindow` to allow the webview to close
3. In the child `WebView`
   1. Implement `onCheckIsTextEditor` to enable the soft keyboard
   2. Implement an `onPageFinished` handler in its `WebViewClient`

### Other Issues

There potentially be other issues that appear when attempting to implement
Viafoura's tools in a WebView.

[Contact our support team at support@viafoura.com for more information and
assistance](mailto:support@viafoura.com).
