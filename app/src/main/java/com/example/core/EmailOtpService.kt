package com.example.core

import com.example.data.model.AuthPolicyConfig
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
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
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

private data class PendingClientOtp(
    val salt: String,
    val codeHash: String,
    val lastSentAtMs: Long,
    val expiresAtMs: Long,
    val attempts: Int = 0
)

object EmailOtpService {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val secureRandom = SecureRandom()
    private val pendingEmailJsOtps = ConcurrentHashMap<String, PendingClientOtp>()

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

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun sendLoginOtp(authPolicy: AuthPolicyConfig = AuthPolicyConfig()): OtpSendResult {
        val user = runCatching { Firebase.auth.currentUser }.getOrNull()
            ?: return OtpSendResult.Error("অনুগ্রহ করে আগে Google দিয়ে সাইন-ইন করুন।")

        val email = user.email?.trim().orEmpty()
        if (email.isBlank()) {
            return OtpSendResult.Error("এই Google অ্যাকাউন্টে কোনো ইমেইল ঠিকানা পাওয়া যায়নি।")
        }

        if (authPolicy.hasEmailJsConfigured) {
            return sendViaEmailJs(uid = user.uid, email = email, policy = authPolicy)
        }

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
                            message = "ইমেইল OTP ব্যাকএন্ড (Cloud Functions / EmailJS) এখনো কনফিগার করা হয়নি।",
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

    suspend fun verifyLoginOtp(
        code: String,
        authPolicy: AuthPolicyConfig = AuthPolicyConfig()
    ): OtpVerifyResult {
        val cleanCode = code.trim()
        if (cleanCode.length != 5 || !cleanCode.all { it.isDigit() }) {
            return OtpVerifyResult.InvalidCode("সঠিক ৫ সংখ্যার কোড লিখুন।")
        }

        val user = runCatching { Firebase.auth.currentUser }.getOrNull()
            ?: return OtpVerifyResult.Error("অনুগ্রহ করে আগে Google দিয়ে সাইন-ইন করুন।")

        if (authPolicy.hasEmailJsConfigured) {
            return verifyEmailJsOtp(uid = user.uid, enteredCode = cleanCode)
        }

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
                            message = "ইমেইল OTP ব্যাকএন্ড (Cloud Functions / EmailJS) এখনো কনফিগার করা হয়নি।",
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

    private suspend fun sendViaEmailJs(
        uid: String,
        email: String,
        policy: AuthPolicyConfig
    ): OtpSendResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = pendingEmailJsOtps[uid]
        if (existing != null) {
            val remainingSec = ((existing.lastSentAtMs + 60_000L - now + 999L) / 1000L).toInt()
            if (remainingSec > 0) {
                return@withContext OtpSendResult.Cooldown(
                    message = "অনুগ্রহ করে $remainingSec সেকেন্ড পর আবার কোড পাঠান।",
                    cooldownSeconds = remainingSec
                )
            }
        }

        val otpCode = (10000 + secureRandom.nextInt(90000)).toString()
        val salt = (0..15).joinToString("") { "%02x".format(secureRandom.nextInt(256)) }
        val hash = sha256Hex("$uid:$otpCode:$salt")

        try {
            val templateParams = JSONObject()
                .put("to_email", email)
                .put("email", email)
                .put("otp_code", otpCode)
                .put("passcode", otpCode)
                .put("code", otpCode)
                .put("app_name", "Honours Solution")
                .put("expires_minutes", "5")

            val payload = JSONObject()
                .put("service_id", policy.emailJsServiceId.trim())
                .put("template_id", policy.emailJsTemplateId.trim())
                .put("user_id", policy.emailJsPublicKey.trim())
                .put("template_params", templateParams)
                .toString()

            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(payload.toRequestBody(jsonMediaType))
                .header("Origin", "https://honours-solution.pages.dev")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    pendingEmailJsOtps[uid] = PendingClientOtp(
                        salt = salt,
                        codeHash = hash,
                        lastSentAtMs = now,
                        expiresAtMs = now + 5 * 60_000L,
                        attempts = 0
                    )
                    OtpSendResult.Sent(expiresInSeconds = 300)
                } else {
                    OtpSendResult.Error(
                        "EmailJS দিয়ে কোড পাঠানো যায়নি (HTTP ${response.code}): ${body.take(120)}"
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

    private fun verifyEmailJsOtp(uid: String, enteredCode: String): OtpVerifyResult {
        val entry = pendingEmailJsOtps[uid]
            ?: return OtpVerifyResult.InvalidCode("কোনো সক্রিয় ভেরিফিকেশন কোড পাওয়া যায়নি। নতুন কোড পাঠান।")

        val now = System.currentTimeMillis()
        if (entry.attempts >= 5) {
            return OtpVerifyResult.InvalidCode("অনেকবার ভুল কোড দেওয়া হয়েছে। ৬০ সেকেন্ড পর নতুন কোড পাঠান।")
        }
        if (now > entry.expiresAtMs) {
            pendingEmailJsOtps.remove(uid)
            return OtpVerifyResult.InvalidCode("কোডটির মেয়াদ শেষ হয়ে গেছে। নতুন কোড পাঠান।")
        }

        val candidate = sha256Hex("$uid:$enteredCode:${entry.salt}")
        return if (MessageDigest.isEqual(candidate.toByteArray(), entry.codeHash.toByteArray())) {
            pendingEmailJsOtps.remove(uid)
            OtpVerifyResult.Verified
        } else {
            val nextAttempts = entry.attempts + 1
            pendingEmailJsOtps[uid] = entry.copy(attempts = nextAttempts)
            OtpVerifyResult.InvalidCode("ভুল কোড! বাকি চেষ্টা: ${(5 - nextAttempts).coerceAtLeast(0)} বার।")
        }
    }
}
