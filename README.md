# Telegram Web Desktop (Android Studio starter project)

An unofficial Android WebView wrapper for the official Telegram Web app (`https://web.telegram.org/a/`). It is not affiliated with Telegram.

## Features in this starter
- Desktop user-agent / viewport mode and a mobile-mode switch.
- App gate using Android system `BiometricPrompt`: strong biometric or the phone's device PIN/password/pattern. This does **not** create a separate app-specific PIN.
- Screen content protected from screenshots/recents via `FLAG_SECURE`.
- Incognito Activity in a separate Android process with a separate WebView data directory; its temporary cookies/cache/storage are cleared on exit. This does not delete Telegram messages from Telegram servers.
- HTTPS-only navigation allowlist for Telegram domains; JavaScript is enabled because Telegram Web requires it; file/content access and JavaScript pop-up windows are disabled.
- Android backup disabled to reduce risk of copying app data.

## Build
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder in Android Studio and let Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Debug APK should be under `app/build/outputs/apk/debug/app-debug.apk`.

This environment did not include the Android SDK or Gradle executable, so no APK binary has been built or tested here. Build and test on an Android device before relying on it.

## Important limitations and security notes
- Normal-mode WebView session cookies are stored in the app's private WebView profile. Android Keystore is not used to encrypt WebView's internal cookie database directly; WebView manages that storage. Do not add a feature that exports cookies or asks users to paste Telegram session tokens.
- Incognito runs in a separate process/profile. Android WebView process/profile behavior varies by Android System WebView version; test on target devices. The cleanup clears the incognito profile's cookies, cache, and WebStorage on exit.
- App lock uses the phone's existing screen-lock credential as fallback, not a separately configured app password. Add an app-specific PIN only with a proper salted KDF and rate limiting if needed.
- Telegram may change its web app or user-agent requirements, so Desktop mode may not always exactly match the desktop browser UI.
- This is a starter project, not a security-audited release. Review dependencies and test before distributing.

## Optional: build APK in GitHub Actions (no Android SDK on your phone)
1. Upload this project folder to a GitHub repository (the repository can be private).
2. Open the repository's **Actions** tab and select **Build Android APK**.
3. Run the workflow using **Run workflow** (or push to `main`/`master`).
4. When the run finishes, open the run page and download the `TelegramWebDesktop-debug-apk` artifact. Extract the ZIP to get `app-debug.apk`.

This cloud workflow is provided as a build convenience; the APK is not built until the workflow actually runs successfully. Review the source and test security behavior before using it with a real Telegram account.