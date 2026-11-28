package com.example.core

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class OtpSendResult {
    data class Sent(val expiresInSeconds: Int = 300) : OtpSendResult()
    data class Cooldown(val message: String, val cooldownSeconds: Int) : OtpSendResult()
    data class ServiceUnavailable(val message: String, val isFunctionNotDeployed: Boolean) : OtpSendResult()
    data class Error(val message: String) : OtpSendResult()
}

sealed class OtpVerifyResult {
    data object Verified : OtpVerifyResult()
    data class InvalidCode(val message: String) : OtpVerifyResult()
    data class ServiceUnavailable(val message: String, val isFunctionNotDeployed: Boolean) : OtpVerifyResult()
    data class Error(val message: String) : OtpVerifyResult()
}

object EmailOtpService {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun resolveFunctionsBaseUrl(): String {
        val projectId = runCatching { FirebaseApp.getInstance().options.projectId }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "gen-lang-client-0015527715"
        return "https://us-central1-$projectId.cloudfunctions.net"
    }

    fun maskEmail(email: String): String {
        val trimmed = email.trim()
        val atIdx = trimmed.indexOf('@')
        if (atIdx <= 1) return trimmed
        val local = trimmed.substring(0, atIdx)
        val domain = trimmed.substring(atIdx)
        val visiblePrefix = local.take(2)
        return "$visiblePrefix***$domain"
    }

    suspend fun sendLoginOtp(): OtpSendResult {
        val user = runCatching { Firebase.auth.currentUser }.getOrNull()
            ?: return OtpSendResult.Error("অনুগ্রহ করে আগে Google দিয়ে সাইন-ইন করুন।")

        val idToken = runCatching { user.getIdToken(false).await().token }.getOrNull()
            ?: return OtpSendResult.Error("লগইন সেশন পাওয়া যায়নি। অনুগ্রহ করে আবার সাইন-ইন করুন।")

        val appCheckToken = runCatching {
            FirebaseAppCheck.getInstance().getAppCheckToken(false).await().token
        }.getOrNull()

        return withContext(Dispatchers.IO) {
            try {
                val requestBuilder = Request.Builder()
                    .url("${resolveFunctionsBaseUrl()}/sendLoginOtp")
                    .post("{}".toRequestBody(jsonMediaType))
                    .header("Authorization", "Bearer $idToken")

                if (!appCheckToken.isNullOrBlank()) {
                    requestBuilder.header("X-Firebase-AppCheck", appCheckToken)
                }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    val json = runCatching { JSONObject(bodyStr) }.getOrNull()
                    val serverMsg = json?.optString("error")?.takeIf { it.isNotBlank() }

                    when (response.code) {
                        in 200..299 -> {
                            val expiresIn = json?.optInt("expiresInSeconds", 300) ?: 300
                            OtpSendResult.Sent(expiresIn)
                        }
                        404 -> OtpSendResult.ServiceUnavailable(
                            message = "ইমেইল OTP ক্লাউড ফাংশন (sendLoginOtp) এখনো ডিপ্লয় করা হয়নি।",
                            isFunctionNotDeployed = true
                        )
                        429 -> OtpSendResult.Cooldown(
                            message = serverMsg ?: "অনুগ্রহ করে কিছুক্ষণ অপেক্ষা করে আবার কোড পাঠান।",
                            cooldownSeconds = (json?.optInt("cooldownSeconds", 60) ?: 60).coerceIn(5, 300)
                        )
                        503 -> OtpSendResult.ServiceUnavailable(
                            message = serverMsg ?: "সার্ভারে SMTP ইমেইল কনফিগারেশন (SMTP_USER / SMTP_PASS) সেট করা হয়নি।",
                            isFunctionNotDeployed = false
                        )
                        else -> OtpSendResult.Error(
                            serverMsg ?: "ভেরিফিকেশন কোড পাঠানো যায়নি (HTTP ${response.code})।"
                        )
                    }
                }
            } catch (e: Exception) {
                OtpSendResult.Error(
                    e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "নেটওয়ার্ক সমস্যার কারণে ইমেইল কোড পাঠানো যায়নি।"
                )
            }
        }
    }

    suspend fun verifyLoginOtp(code: String): OtpVerifyResult {
        val cleanCode = code.trim()
        if (cleanCode.length != 5 || !cleanCode.all { it.isDigit() }) {
            return OtpVerifyResult.InvalidCode("সঠিক ৫ সংখ্যার কোড লিখুন।")
        }

        val user = runCatching { Firebase.auth.currentUser }.getOrNull()
            ?: return OtpVerifyResult.Error("অনুগ্রহ করে আগে Google দিয়ে সাইন-ইন করুন।")

        val idToken = runCatching { user.getIdToken(false).await().token }.getOrNull()
            ?: return OtpVerifyResult.Error("লগইন সেশন পাওয়া যায়নি। অনুগ্রহ করে আবার সাইন-ইন করুন।")

        val appCheckToken = runCatching {
            FirebaseAppCheck.getInstance().getAppCheckToken(false).await().token
        }.getOrNull()

        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().put("code", cleanCode).toString()
                val requestBuilder = Request.Builder()
                    .url("${resolveFunctionsBaseUrl()}/verifyLoginOtp")
                    .post(payload.toRequestBody(jsonMediaType))
                    .header("Authorization", "Bearer $idToken")

                if (!appCheckToken.isNullOrBlank()) {
                    requestBuilder.header("X-Firebase-AppCheck", appCheckToken)
                }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    val json = runCatching { JSONObject(bodyStr) }.getOrNull()
                    val serverMsg = json?.optString("error")?.takeIf { it.isNotBlank() }

                    when (response.code) {
                        in 200..299 -> {
                            if (json?.optBoolean("verified", false) == true) {
                                OtpVerifyResult.Verified
                            } else {
                                OtpVerifyResult.InvalidCode(serverMsg ?: "কোড যাচাই করা যায়নি।")
                            }
                        }
                        404 -> OtpVerifyResult.ServiceUnavailable(
                            message = "ইমেইল OTP ক্লাউড ফাংশন (verifyLoginOtp) এখনো ডিপ্লয় করা হয়নি।",
                            isFunctionNotDeployed = true
                        )
                        400, 401, 429 -> OtpVerifyResult.InvalidCode(
                            serverMsg ?: "ভুল বা মেয়াদোত্তীর্ণ কোড।"
                        )
                        503 -> OtpVerifyResult.ServiceUnavailable(
                            message = serverMsg ?: "সার্ভারে SMTP ইমেইল কনফিগারেশন সেট করা হয়নি।",
                            isFunctionNotDeployed = false
                        )
                        else -> OtpVerifyResult.Error(
                            serverMsg ?: "কোড যাচাই করা যায়নি (HTTP ${response.code})।"
                        )
                    }
                }
            } catch (e: Exception) {
                OtpVerifyResult.Error(
                    e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "নেটওয়ার্ক সমস্যার কারণে কোড যাচাই করা যায়নি।"
                )
            }
        }
    }
}
