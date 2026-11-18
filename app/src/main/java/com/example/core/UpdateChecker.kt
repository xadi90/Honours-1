package com.example.core

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val currentVersionCode: Int,
    val currentVersionName: String,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val updateAvailable: Boolean,
    val sourceLabel: String
)

object UpdateChecker {
    private const val GITHUB_RELEASES_API =
        "https://api.github.com/repos/xadi90/Honours-1/releases/latest"
    private const val DIRECT_GITHUB_APK_URL =
        "https://github.com/xadi90/Honours-1/releases/latest/download/HonoursSolution.apk"
    private const val OFFICIAL_WEBSITE_URL = "https://honours-solution.pages.dev"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkForUpdates(
        remoteVersionCode: Int? = null,
        remoteVersionName: String? = null,
        remoteNotes: String? = null,
        remoteDownloadUrl: String? = null
    ): AppUpdateInfo = withContext(Dispatchers.IO) {
        val currentCode = BuildConfig.VERSION_CODE
        val currentName = BuildConfig.VERSION_NAME

        // 1. Prefer Firestore-managed version_info when populated by Admin
        if (remoteVersionCode != null && remoteVersionCode > 0 && !remoteVersionName.isNullOrBlank()) {
            val url = remoteDownloadUrl?.takeIf { it.startsWith("https://") } ?: DIRECT_GITHUB_APK_URL
            return@withContext AppUpdateInfo(
                currentVersionCode = currentCode,
                currentVersionName = currentName,
                latestVersionCode = remoteVersionCode,
                latestVersionName = remoteVersionName,
                releaseNotes = remoteNotes.orEmpty(),
                downloadUrl = url,
                updateAvailable = remoteVersionCode > currentCode,
                sourceLabel = "Official Honours Solution Server"
            )
        }

        // 2. Fallback to GitHub Releases API check (prefers direct .apk asset URL)
        try {
            val request = Request.Builder()
                .url(GITHUB_RELEASES_API)
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val tagName = json.optString("tag_name", "v$currentName").removePrefix("v")
                    val htmlUrl = json.optString("html_url", OFFICIAL_WEBSITE_URL)
                    val assets = json.optJSONArray("assets")
                    var directApkUrl = ""
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.optJSONObject(i) ?: continue
                            val assetUrl = asset.optString("browser_download_url", "")
                            if (assetUrl.endsWith(".apk", ignoreCase = true)) {
                                directApkUrl = assetUrl
                                break
                            }
                        }
                    }
                    val notes = json.optString("body", "Latest stable build from GitHub Releases.")
                    val isNewer = compareSemanticVersions(tagName, currentName) > 0
                    return@withContext AppUpdateInfo(
                        currentVersionCode = currentCode,
                        currentVersionName = currentName,
                        latestVersionCode = if (isNewer) currentCode + 1 else currentCode,
                        latestVersionName = tagName,
                        releaseNotes = notes,
                        downloadUrl = directApkUrl.ifBlank { htmlUrl },
                        updateAvailable = isNewer,
                        sourceLabel = "GitHub Releases"
                    )
                }
            }
        } catch (_: Exception) {
        }

        AppUpdateInfo(
            currentVersionCode = currentCode,
            currentVersionName = currentName,
            latestVersionCode = currentCode,
            latestVersionName = currentName,
            releaseNotes = "You are running the latest installed version.",
            downloadUrl = OFFICIAL_WEBSITE_URL,
            updateAvailable = false,
            sourceLabel = "Installed Build"
        )
    }

    private fun compareSemanticVersions(remote: String, local: String): Int {
        val rParts = remote.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val lParts = local.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val maxLen = maxOf(rParts.size, lParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (r != l) return r.compareTo(l)
        }
        return 0
    }
}
