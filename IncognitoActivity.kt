package com.example.telegramwebdesktop

import android.os.Bundle
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class IncognitoActivity : AppCompatActivity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        // This activity runs in :incognito, so its WebView data directory is separate from normal mode.
        WebView.setDataDirectorySuffix("incognito")
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val close = Button(this).apply {
            text = "Exit Incognito & erase temporary data"
            setOnClickListener { clearIncognitoDataAndExit() }
        }
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = false
        web.settings.javaScriptCanOpenWindowsAutomatically = false
        web.settings.setSupportMultipleWindows(false)
        web.settings.useWideViewPort = true
        web.settings.loadWithOverviewMode = true
        web.settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return !(uri.scheme == "https" && (uri.host == "web.telegram.org" || uri.host == "telegram.org" || uri.host?.endsWith(".telegram.org") == true))
            }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        root.addView(close)
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        web.loadUrl("https://web.telegram.org/a/")
    }

    private fun clearIncognitoDataAndExit() {
        web.stopLoading()
        web.loadUrl("about:blank")
        web.clearHistory()
        web.clearCache(true)
        web.clearFormData()
        web.clearSslPreferences()
        CookieManager.getInstance().removeAllCookies {
            CookieManager.getInstance().flush()
            WebStorage.getInstance().deleteAllData()
            web.destroy()
            finishAndRemoveTask()
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() { clearIncognitoDataAndExit() }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.stopLoading()
            web.destroy()
        }
        super.onDestroy()
    }
}