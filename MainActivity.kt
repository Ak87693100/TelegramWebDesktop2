package com.example.telegramwebdesktop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private var unlocked = false
    private val home = "https://web.telegram.org/a/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        showLockScreen()
    }

    private fun showLockScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(36, 36, 36, 36)
            setBackgroundColor(android.graphics.Color.rgb(23, 33, 43))
        }
        val title = android.widget.TextView(this).apply {
            text = "Telegram Web Desktop\n\nUnlock to continue"
            textSize = 22f
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
            setPadding(12, 12, 12, 28)
        }
        val unlock = Button(this).apply { text = "Unlock with biometrics or phone PIN"; setOnClickListener { authenticate() } }
        layout.addView(title)
        layout.addView(unlock)
        setContentView(layout)
        authenticate()
    }

    private fun authenticate() {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Set up a screen lock or supported biometric in Android Settings first.", Toast.LENGTH_LONG).show()
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                unlocked = true
                showWebView()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(this@MainActivity, "App remains locked", Toast.LENGTH_SHORT).show()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Telegram Web Desktop")
            .setSubtitle("Use fingerprint, supported face unlock, or your phone screen-lock PIN/password")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    private fun showWebView() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val desktop = Button(this).apply { text = "Desktop: ON"; setOnClickListener {
            val s = web.settings
            s.useWideViewPort = true
            s.loadWithOverviewMode = true
            s.userAgentString = DESKTOP_UA
            web.reload()
        } }
        val mobile = Button(this).apply { text = "Mobile"; setOnClickListener {
            web.settings.userAgentString = null
            web.settings.useWideViewPort = true
            web.settings.loadWithOverviewMode = true
            web.reload()
        } }
        val incognito = Button(this).apply { text = "Incognito"; setOnClickListener {
            startActivity(Intent(this@MainActivity, IncognitoActivity::class.java))
        } }
        val lock = Button(this).apply { text = "Lock"; setOnClickListener { unlocked = false; showLockScreen() } }
        listOf(desktop, mobile, incognito, lock).forEach { bar.addView(it, LinearLayout.LayoutParams(0, -2, 1f)) }
        web = WebView(this)
        web.settings.javaScriptEnabled = true // Telegram Web requires JavaScript; navigation is allowlisted below.
        web.settings.domStorageEnabled = true
        web.settings.databaseEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = false
        web.settings.javaScriptCanOpenWindowsAutomatically = false
        web.settings.setSupportMultipleWindows(false)
        web.settings.useWideViewPort = true
        web.settings.loadWithOverviewMode = true
        web.settings.userAgentString = DESKTOP_UA
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return !(uri.scheme == "https" && (uri.host == "web.telegram.org" || uri.host == "telegram.org" || uri.host?.endsWith(".telegram.org") == true))
            }
        }
        CookieManager.getInstance().setAcceptCookie(true)
        web.loadUrl(home)
        root.addView(bar)
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    override fun onPause() {
        super.onPause()
        if (unlocked) {
            // Require re-authentication whenever the app returns to foreground.
            unlocked = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (unlocked) return
        // First launch already shows the lock screen; on later resumes require authentication.
        if (::web.isInitialized) showLockScreen()
    }

    override fun onBackPressed() {
        if (::web.isInitialized && unlocked && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    companion object {
        private const val DESKTOP_UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}