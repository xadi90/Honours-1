package com.example.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest

private const val AUTH_TAG = "Auth"

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Suppress("DEPRECATION")
fun getSigningSha1(context: Context): String {
    return try {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
        }
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.signingInfo?.apkContentsSigners ?: pkgInfo.signingInfo?.signingCertificateHistory
        } else {
            pkgInfo.signatures
        }
        val certBytes = signatures?.firstOrNull()?.toByteArray() ?: return "unknown"
        val digest = MessageDigest.getInstance("SHA-1").digest(certBytes)
        digest.joinToString(":") { "%02X".format(it) }
    } catch (_: Throwable) {
        "unknown"
    }
}

private fun getDefaultWebClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    if (resId == 0) return null
    return try {
        context.getString(resId).takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager?,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (!ensureFirebaseReady(context)) {
        onUnauthenticated()
        return
    }
    val existingUser = runCatching { Firebase.auth.currentUser }.getOrNull()
    if (existingUser != null) {
        onAuthSuccess()
        return
    }
    if (credentialManager == null) {
        onUnauthenticated()
        return
    }
    val activity = context.findActivity()
    if (activity == null) {
        onUnauthenticated()
        return
    }
    val clientId = getDefaultWebClientId(context) ?: run {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager?,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    if (!ensureFirebaseReady(context)) {
        onAuthError("Firebase is not configured in this build. Please check google-services.json.")
        return
    }
    if (credentialManager == null) {
        onAuthError("Google Credential Manager is unavailable on this device. Please update Google Play Services.")
        return
    }
    val clientId = getDefaultWebClientId(context) ?: run {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }
    val activity = context.findActivity()
    if (activity == null) {
        onAuthError("Unable to launch Google Sign-In from current context.")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val primaryRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val cred = try {
                credentialManager.getCredential(activity, primaryRequest).credential
            } catch (primaryErr: GetCredentialCancellationException) {
                throw primaryErr
            } catch (primaryErr: GetCredentialException) {
                Log.w(AUTH_TAG, "GetSignInWithGoogleOption failed (${primaryErr.type}): ${primaryErr.message}", primaryErr)
                val fallbackOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(fallbackOption)
                    .build()
                credentialManager.getCredential(activity, fallbackRequest).credential
            }

            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type received (${cred.type}).")
            }
        } catch (e: GetCredentialCancellationException) {
            val detail = e.errorMessage?.toString().orEmpty()
            Log.w(AUTH_TAG, "Google Sign-In cancelled or dismissed: $detail", e)
            val isUserCancel = detail.isBlank() ||
                detail.contains("cancelled by the user", ignoreCase = true) ||
                detail.contains("canceled by the user", ignoreCase = true)
            if (isUserCancel) {
                onAuthCancelled()
            } else {
                onAuthError("Google Sign-In সম্পন্ন হয়নি ($detail)\nAPK SHA-1: ${getSigningSha1(context)}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(AUTH_TAG, "Google Sign-In failed", e)
            val rawMsg = e.localizedMessage ?: e.message ?: "Unknown sign-in error"
            val sha1 = getSigningSha1(context)
            val friendlyMsg = if (
                rawMsg.contains("28444") ||
                rawMsg.contains("DEVELOPER_ERROR", ignoreCase = true) ||
                rawMsg.contains("10:") ||
                rawMsg.contains("Caller not verified", ignoreCase = true) ||
                rawMsg.contains("Developer console", ignoreCase = true)
            ) {
                "Google Sign-In SHA-1 কনফিগারেশন অমিল ($rawMsg)।\nএই APK-এর SHA-1: $sha1\n(সঠিক debug.keystore.base64 দিয়ে পুনরায় বিল্ড প্রয়োজন।)"
            } else {
                "Sign-In সম্পন্ন হয়নি: $rawMsg\n(APK SHA-1: $sha1)"
            }
            onAuthError(friendlyMsg)
        }
    }
}

fun signOut(
    credentialManager: CredentialManager?,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager?.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Throwable) {
                Log.w(AUTH_TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                runCatching { Firebase.auth.signOut() }
                onSignOutComplete()
            }
        }
    }
}
