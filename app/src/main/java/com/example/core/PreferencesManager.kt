package com.example.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "honours_solution_prefs")

data class AdminLockoutState(
    val failedAttempts: Int = 0,
    val lockoutUntilMs: Long = 0L
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_PREF = stringPreferencesKey("language_pref")
        val SEARCH_HISTORY = stringSetPreferencesKey("search_history")
        val RECENT_PDF_IDS = stringPreferencesKey("recent_pdf_ids")
        val RECENT_SUBJECT_IDS = stringPreferencesKey("recent_subject_ids")
        val ADMIN_FAILED_ATTEMPTS = intPreferencesKey("admin_failed_attempts")
        val ADMIN_LOCKOUT_UNTIL_MS = longPreferencesKey("admin_lockout_until_ms")
        val ADMIN_UNLOCKED_UNTIL_MS = longPreferencesKey("admin_unlocked_until_ms")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        AppThemeMode.fromKey(prefs[Keys.THEME_MODE])
    }

    val languagePref: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE_PREF] ?: "bn_en"
    }

    val searchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        (prefs[Keys.SEARCH_HISTORY] ?: emptySet()).toList().sorted()
    }

    val recentPdfIds: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.RECENT_PDF_IDS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    val recentSubjectIds: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.RECENT_SUBJECT_IDS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    val adminLockoutState: Flow<AdminLockoutState> = context.dataStore.data.map { prefs ->
        AdminLockoutState(
            failedAttempts = prefs[Keys.ADMIN_FAILED_ATTEMPTS] ?: 0,
            lockoutUntilMs = prefs[Keys.ADMIN_LOCKOUT_UNTIL_MS] ?: 0L
        )
    }

    fun getPdfReadingProgress(pdfId: String): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[intPreferencesKey("pdf_page_$pdfId")] ?: 0
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.key }
    }

    suspend fun setLanguagePref(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_PREF] = lang }
    }

    suspend fun addSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.SEARCH_HISTORY] ?: emptySet()).toMutableSet()
            current.add(trimmed)
            if (current.size > 15) {
                current.remove(current.first())
            }
            prefs[Keys.SEARCH_HISTORY] = current
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it.remove(Keys.SEARCH_HISTORY) }
    }

    suspend fun recordRecentPdf(pdfId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_PDF_IDS]?.split(",")?.filter { it.isNotBlank() }?.toMutableList()
                ?: mutableListOf()
            current.remove(pdfId)
            current.add(0, pdfId)
            prefs[Keys.RECENT_PDF_IDS] = current.take(10).joinToString(",")
        }
    }

    suspend fun recordRecentSubject(subjectId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SUBJECT_IDS]?.split(",")?.filter { it.isNotBlank() }?.toMutableList()
                ?: mutableListOf()
            current.remove(subjectId)
            current.add(0, subjectId)
            prefs[Keys.RECENT_SUBJECT_IDS] = current.take(8).joinToString(",")
        }
    }

    suspend fun savePdfReadingProgress(pdfId: String, pageIndex: Int) {
        context.dataStore.edit { it[intPreferencesKey("pdf_page_$pdfId")] = pageIndex }
    }

    suspend fun recordAdminFailedAttempt(maxAttempts: Int = 5, lockoutDurationMs: Long = 5 * 60_000L) {
        context.dataStore.edit { prefs ->
            val nextAttempts = (prefs[Keys.ADMIN_FAILED_ATTEMPTS] ?: 0) + 1
            prefs[Keys.ADMIN_FAILED_ATTEMPTS] = nextAttempts
            if (nextAttempts >= maxAttempts) {
                prefs[Keys.ADMIN_LOCKOUT_UNTIL_MS] = System.currentTimeMillis() + lockoutDurationMs
            }
        }
    }

    suspend fun resetAdminFailedAttempts() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ADMIN_FAILED_ATTEMPTS] = 0
            prefs[Keys.ADMIN_LOCKOUT_UNTIL_MS] = 0L
        }
    }

    suspend fun clearUserSessionData() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SEARCH_HISTORY)
            prefs.remove(Keys.RECENT_PDF_IDS)
            prefs.remove(Keys.RECENT_SUBJECT_IDS)
        }
    }
}
