package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class CollegeProfileConfig(
    val collegeNameBn: String = "সুজাত আলী অনার্স কলেজ, পিংনা",
    val collegeNameEn: String = "Sujat Ali Honours College, Pingna",
    val locationText: String = "পিংনা, সরিষাবাড়ী, জামালপুর",
    val affiliationText: String = "জাতীয় বিশ্ববিদ্যালয় অধিভুক্ত",
    val collegeCode: String = "Sujat Ali Honours College • Pingna",
    val mottoText: String = "জ্ঞান, শৃঙ্খলা ও মানবিক উৎকর্ষের ঐতিহ্যবাহী বিদ্যাপীঠ",
    val noticeTicker: String = "সুজাত আলী অনার্স কলেজ, পিংনা-এর সকল জরুরি নোটিশ, ক্লাস রুটিন, অনার্স বিভাগ ও ক্যাম্পাস আপডেট এখান থেকে নিয়মিত দেখুন।",
    val heroThemePreset: String = "navy_gold", // navy_gold | emerald_teal | royal_indigo | crimson_gold
    val layoutMode: String = "cards", // cards | compact | grid
    val showQuickInfoStrip: Boolean = true,
    val quickStat1Label: String = "অধিভুক্তি",
    val quickStat1Value: String = "জাতীয় বিশ্ববিদ্যালয়",
    val quickStat2Label: String = "ক্যাম্পাস",
    val quickStat2Value: String = "পিংনা, সরিষাবাড়ী",
    val quickStat3Label: String = "প্রোগ্রাম",
    val quickStat3Value: String = "অনার্স ও ডিগ্রি",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val websiteUrl: String = "",
    val officeHours: String = "রবিবার – বৃহস্পতিবার • সকাল ৯:০০ – বিকাল ৪:০০"
)

@Keep
@IgnoreExtraProperties
data class CollegeSectionBlock(
    @DocumentId val id: String = "",
    val blockType: String = "custom", // notice | principal | department | routine | faculty | admission | link | custom
    val title: String = "",
    val subtitle: String = "",
    val body: String = "",
    val badgeText: String = "",
    val iconKey: String = "school", // school | campaign | schedule | groups | book | info | phone | link | star
    val accentStyle: String = "primary", // primary | gold | emerald | teal | crimson
    val layoutSpan: String = "full", // full | half
    val actionLabel: String = "",
    val actionUrl: String = "",
    val pinned: Boolean = false,
    val sortOrder: Long = 0L,
    val status: String = "published", // published | draft
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
