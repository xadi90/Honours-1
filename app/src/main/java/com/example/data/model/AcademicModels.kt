package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val selectedDepartmentId: String = "political_science",
    val selectedDepartmentName: String = "রাষ্ট্রবিজ্ঞান (Political Science)",
    val selectedYear: Long = 1L,
    val languagePreference: String = "bn_en",
    val savedAnswersCount: Long = 0L,
    val bookmarksCount: Long = 0L,
    val testsCompletedCount: Long = 0L,
    val freeAiLimit: Long = 30L,
    val aiUsageCount: Long = 0L,
    val purchasedAiCredits: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    @ServerTimestamp val lastActiveAt: Timestamp? = null
) {
    @get:Exclude
    val totalAiLimit: Long
        get() = (freeAiLimit.coerceAtLeast(0L) + purchasedAiCredits.coerceAtLeast(0L))

    @get:Exclude
    val remainingAiCredits: Long
        get() = (totalAiLimit - aiUsageCount.coerceAtLeast(0L)).coerceAtLeast(0L)
}

@Keep
@IgnoreExtraProperties
data class Department(
    @DocumentId val id: String = "",
    val nameBn: String = "",
    val nameEn: String = "",
    val code: String = "",
    val description: String = "",
    val hasStructuredSolution: Boolean = false,
    val sortOrder: Long = 0L,
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Subject(
    @DocumentId val id: String = "",
    val departmentId: String = "political_science",
    val yearNumber: Long = 1L,
    val courseCode: String = "",
    val titleBn: String = "",
    val titleEn: String = "",
    val credits: Long = 4L,
    val syllabusNote: String = "",
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Chapter(
    @DocumentId val id: String = "",
    val subjectId: String = "",
    val yearNumber: Long = 1L,
    val chapterNumber: Long = 1L,
    val titleBn: String = "",
    val titleEn: String = "",
    val topics: List<String> = emptyList(),
    val summary: String = "",
    val definitions: String = "",
    val shortQuestions: String = "",
    val broadQuestions: String = "",
    val creativeQuestions: String = "",
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PdfResource(
    @DocumentId val id: String = "",
    val departmentId: String = "",
    val departmentName: String = "",
    val yearNumber: Long = 1L,
    val subjectName: String = "",
    val courseCode: String = "",
    val title: String = "",
    val category: String = "Book PDFs",
    val description: String = "",
    val pdfUrl: String = "",
    val localFileName: String = "",
    val coverImageUrl: String = "",
    val pageCount: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val uploadedBy: String = "",
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudyQuestion(
    @DocumentId val id: String = "",
    val departmentId: String = "political_science",
    val yearNumber: Long = 1L,
    val subjectId: String = "",
    val subjectName: String = "",
    val courseCode: String = "",
    val questionType: String = "previous_year", // previous_year | suggestion | mcq
    val examYear: String = "",
    val partLabel: String = "",
    val prompt: String = "",
    val answerText: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Long = 0L,
    val explanation: String = "",
    val priority: String = "Important",
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ModelTest(
    @DocumentId val id: String = "",
    val departmentId: String = "political_science",
    val yearNumber: Long = 1L,
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val durationMinutes: Long = 15L,
    val questionCount: Long = 10L,
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ModelTestResult(
    @DocumentId val id: String = "",
    val userId: String = "",
    val testId: String = "",
    val testTitle: String = "",
    val yearNumber: Long = 1L,
    val subjectName: String = "",
    val totalQuestions: Long = 0L,
    val correctCount: Long = 0L,
    val scorePercent: Long = 0L,
    val durationSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val tag: String = "NU Notice",
    val pinned: Boolean = false,
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val reporterName: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val reason: String = "",
    val details: String = "",
    val status: String = "open",
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminLog(
    @DocumentId val id: String = "",
    val adminUid: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookmarkItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val itemType: String = "pdf", // answer | pdf | chapter | pdf_page
    val targetId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val pageNumber: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DeveloperProfileConfig(
    val name: String = "Xadi Ahmed",
    val tagline: String = "জাতীয় বিশ্ববিদ্যালয়",
    val departmentFocus: String = "Political Science (অনার্স ১ম–৪র্থ বর্ষ)",
    val bio: String = "জাতীয় বিশ্ববিদ্যালয়ের রাষ্ট্রবিজ্ঞান অনার্স ১ম থেকে ৪র্থ বর্ষের শিক্ষার্থীদের জন্য আধুনিক একাডেমিক সহায়িকা, AI প্রশ্ন সমাধান এবং সকল বিভাগের PDF লাইব্রেরি।",
    val phone: String = "01878864913",
    val email: String = "xadiahmed23@gmail.com",
    val websiteUrl: String = "https://honours-solution.pages.dev",
    val websiteLabel: String = "Official Website (Cloudflare Pages)"
)

data class AiQuotaState(
    val freeLimit: Long = 30L,
    val purchasedCredits: Long = 0L,
    val usedCount: Long = 0L
) {
    val totalCredits: Long get() = freeLimit.coerceAtLeast(0L) + purchasedCredits.coerceAtLeast(0L)
    val remainingCredits: Long get() = (totalCredits - usedCount.coerceAtLeast(0L)).coerceAtLeast(0L)
}

data class AiLimitPackage(
    val id: String,
    val titleBn: String,
    val credits: Long,
    val priceBdt: Long,
    val badge: String = ""
)

@Keep
@IgnoreExtraProperties
data class AiLimitConfig(
    val freeLimit: Long = 30L,
    val paymentPhone: String = "01878864913",
    val paymentMethods: String = "bKash / Nagad / Rocket (Personal)",
    val paymentNote: String = "উপরের নম্বরে Send Money করে নিচে আপনার পেমেন্ট নম্বর ও Transaction ID (TrxID) জমা দিন। অ্যাডমিন যাচাই করে সাথে সাথেই আপনার একাউন্টে লিমিট যোগ করে দেবেন।",
    val pack1Credits: Long = 50L,
    val pack1PriceBdt: Long = 50L,
    val pack2Credits: Long = 150L,
    val pack2PriceBdt: Long = 120L,
    val pack3Credits: Long = 500L,
    val pack3PriceBdt: Long = 300L
) {
    fun packages(): List<AiLimitPackage> = listOf(
        AiLimitPackage("pack_1", "স্টার্টার প্যাক (+${pack1Credits} AI লিমিট)", pack1Credits, pack1PriceBdt, "জনপ্রিয়"),
        AiLimitPackage("pack_2", "এক্সাম প্রো প্যাক (+${pack2Credits} AI লিমিট)", pack2Credits, pack2PriceBdt, "সাশ্রয়ী"),
        AiLimitPackage("pack_3", "অনার্স মাস্টার প্যাক (+${pack3Credits} AI লিমিট)", pack3Credits, pack3PriceBdt, "সেরা ভ্যালু")
    )
}

@Keep
@IgnoreExtraProperties
data class AiLimitPurchaseRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val packageTitle: String = "",
    val creditsAmount: Long = 0L,
    val priceBdt: Long = 0L,
    val paymentMethod: String = "bKash",
    val senderNumber: String = "",
    val transactionId: String = "",
    val status: String = "pending", // pending | approved | rejected
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CatalogMetaConfig(
    val seeded: Boolean = false,
    val pdfSeedVersion: Long = 0L,
    val suppressedIds: List<String> = emptyList(),
    val deletedIds: List<String> = emptyList(),
    val draftIds: List<String> = emptyList()
)

data class AdminUserDirectoryEntry(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val selectedDepartmentName: String = "",
    val selectedYear: Long = 1L,
    val aiUsageCount: Long = 0L,
    val lastActiveAt: Timestamp? = null,
    val createdAt: Timestamp? = null
)

data class AdminUserUsageSummary(
    val totalUsers: Int = 0,
    val activeTodayCount: Int = 0,
    val activeLast7DaysCount: Int = 0,
    val newUsersLast7DaysCount: Int = 0,
    val totalAiQueriesUsed: Long = 0L,
    val departmentCounts: List<Pair<String, Int>> = emptyList(),
    val yearCounts: List<Pair<Long, Int>> = emptyList(),
    val users: List<AdminUserDirectoryEntry> = emptyList()
)

@Keep
@IgnoreExtraProperties
data class AuthPolicyConfig(
    val requireEmailOtp: Boolean = false,
    val emailJsServiceId: String = "",
    val emailJsTemplateId: String = "",
    val emailJsPublicKey: String = ""
) {
    val hasEmailJsConfigured: Boolean
        get() = emailJsServiceId.isNotBlank() &&
            emailJsTemplateId.isNotBlank() &&
            emailJsPublicKey.isNotBlank()
}
