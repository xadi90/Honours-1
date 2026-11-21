package com.example.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import kotlinx.coroutines.tasks.await

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

@Volatile
var appCheckInitSummary: String = "uninitialized"
    private set

@Volatile
var firstAppCheckError: String? = null
    private set

@Volatile
var firstAppCheckTokenReady: Boolean = false
    private set

fun ensureFirebaseReady(context: Context): Boolean {
    val appContext = context.applicationContext
    return try {
        if (FirebaseApp.getApps(appContext).isNotEmpty()) {
            true
        } else {
            FirebaseApp.initializeApp(appContext) != null || FirebaseApp.getApps(appContext).isNotEmpty()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Firebase initialization check failed", e)
        false
    }
}

fun configureAppCheck(context: Context, intent: Intent?) {
    val appContext = context.applicationContext
    try {
        if (!ensureFirebaseReady(appContext)) {
            appCheckInitSummary = "firebase_not_ready"
            Log.e(TAG, "FirebaseApp is not initialized (missing or invalid google-services.json). Skipping App Check.")
            return
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = appContext.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Synchronous commit() is required before DebugAppCheckProviderFactory reads it
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            appCheckInitSummary = "debug_provider(hasIntentToken=${!debugToken.isNullOrEmpty()})"
            warmUpAndCaptureFirstError()
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val siteKey = runCatching {
                appContext.getString(R.string.recaptcha_site_key)
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: run {
                val resId = appContext.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", appContext.packageName)
                if (resId != 0) appContext.getString(resId) else ""
            }
            if (siteKey.isNotBlank()) {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    RecaptchaAppCheckProviderFactory.getInstance(siteKey),
                    true
                )
                appCheckInitSummary = "recaptcha_installed(keyPrefix=${siteKey.take(6)})"
                warmUpAndCaptureFirstError()
                Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
            } else {
                appCheckInitSummary = "recaptcha_site_key_missing"
                Log.e(TAG, "recaptcha_site_key resource string not found or blank.")
            }
        }
    } catch (e: Exception) {
        appCheckInitSummary = "init_error(${e.localizedMessage ?: e.javaClass.simpleName})"
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

private fun warmUpAndCaptureFirstError() {
    try {
        FirebaseAppCheck.getInstance().getAppCheckToken(false)
            .addOnSuccessListener { token ->
                firstAppCheckTokenReady = token.token.isNotBlank()
                firstAppCheckError = null
                Log.d(TAG, "Initial App Check token fetched successfully (len=${token.token.length}).")
            }
            .addOnFailureListener { err ->
                if (firstAppCheckError == null) {
                    val causeMsg = err.cause?.localizedMessage?.takeIf { it.isNotBlank() }?.let { " | cause: $it" }.orEmpty()
                    firstAppCheckError = "${err.javaClass.simpleName}: ${err.localizedMessage ?: "unknown"}$causeMsg"
                }
                Log.e(TAG, "Initial App Check token fetch failed", err)
            }
    } catch (e: Exception) {
        if (firstAppCheckError == null) {
            firstAppCheckError = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "unknown"}"
        }
        Log.e(TAG, "Failed to trigger App Check warm-up", e)
    }
}

suspend fun diagnoseAppCheckFailure(): String {
    val capturedFirst = firstAppCheckError
    return try {
        val token = FirebaseAppCheck.getInstance().getAppCheckToken(false).await()
        "Token fetched (len=${token.token.length}, $appCheckInitSummary, firstErr=${capturedFirst ?: "none"})"
    } catch (e: Exception) {
        val currentMsg = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "unknown"}"
        if (capturedFirst == null) {
            firstAppCheckError = currentMsg
        }
        val firstPart = if (!capturedFirst.isNullOrBlank() && capturedFirst != currentMsg) {
            " | firstError=$capturedFirst"
        } else {
            ""
        }
        "Token fetch failed: $currentMsg$firstPart ($appCheckInitSummary)"
    }
}
