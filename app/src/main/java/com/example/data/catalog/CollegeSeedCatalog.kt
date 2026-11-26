package com.example.data.catalog

import com.example.data.model.CollegeProfileConfig
import com.example.data.model.CollegeSectionBlock

object CollegeSeedCatalog {

    val defaultProfile = CollegeProfileConfig()

    val blockTypeOptions = listOf(
        "notice" to "কলেজ নোটিশ",
        "principal" to "অধ্যক্ষের বাণী",
        "department" to "অনার্স বিভাগ",
        "routine" to "ক্লাস / পরীক্ষার রুটিন",
        "faculty" to "শিক্ষকমণ্ডলী",
        "admission" to "ভর্তি ও ফর্ম ফিলাপ",
        "link" to "ওয়েবসাইট / লিংক",
        "custom" to "কাস্টম কার্ড"
    )

    val iconOptions = listOf(
        "school" to "কলেজ",
        "campaign" to "নোটিশ",
        "schedule" to "রুটিন",
        "groups" to "শিক্ষক/শিক্ষার্থী",
        "book" to "পাঠ্যক্রম",
        "info" to "তথ্য",
        "phone" to "যোগাযোগ",
        "link" to "লিংক",
        "star" to "বিশেষ"
    )

    val accentOptions = listOf(
        "primary" to "নেভি ব্লু",
        "gold" to "গোল্ডেন",
        "emerald" to "সবুজ",
        "teal" to "টিল",
        "crimson" to "লাল (জরুরি)"
    )

    val heroThemePresets = listOf(
        "navy_gold" to "নেভি ও গোল্ড",
        "emerald_teal" to "সবুজ ও টিল",
        "royal_indigo" to "রয়্যাল ইন্ডিগো",
        "crimson_gold" to "ক্রিমসন ও গোল্ড"
    )

    val layoutModes = listOf(
        "cards" to "বড় কার্ড (Full)",
        "compact" to "কম্প্যাক্ট তালিকা",
        "grid" to "গ্রিড (২ কলাম)"
    )

    val starterSections: List<CollegeSectionBlock> = listOf(
        CollegeSectionBlock(
            id = "sahc_notice_board",
            blockType = "notice",
            title = "কলেজ নোটিশ বোর্ড",
            subtitle = "সর্বশেষ বিজ্ঞপ্তি ও একাডেমিক নির্দেশনা",
            body = "অনার্স ১ম, ২য়, ৩য় ও ৪র্থ বর্ষের ইনকোর্স, টেস্ট পরীক্ষা, ফর্ম ফিলাপ এবং জাতীয় বিশ্ববিদ্যালয়ের সকল আপডেট নোটিশ এখান থেকে দেখে নিন। অ্যাডমিন প্যানেল থেকে যেকোনো সময় নতুন নোটিশ যুক্ত বা সাজানো যাবে।",
            badgeText = "জরুরি নোটিশ",
            iconKey = "campaign",
            accentStyle = "gold",
            layoutSpan = "full",
            pinned = true,
            sortOrder = 1L,
            status = "published"
        ),
        CollegeSectionBlock(
            id = "sahc_principal_desk",
            blockType = "principal",
            title = "অধ্যক্ষের বাণী ও প্রশাসন",
            subtitle = "সুজাত আলী অনার্স কলেজ, পিংনা",
            body = "শিক্ষার্থীদের সুশৃঙ্খল পঠন-পাঠন, নিয়মিত উপস্থিতি এবং নৈতিক ও মানবিক মূল্যবোধ বিকাশে আমাদের কলেজ পরিবার সর্বদা প্রতিশ্রুতিবদ্ধ।",
            badgeText = "প্রশাসন",
            iconKey = "school",
            accentStyle = "primary",
            layoutSpan = "full",
            pinned = false,
            sortOrder = 2L,
            status = "published"
        ),
        CollegeSectionBlock(
            id = "sahc_class_routine",
            blockType = "routine",
            title = "ক্লাস ও ইনকোর্স রুটিন",
            subtitle = "বর্ষভিত্তিক সময়সূচি",
            body = "রবিবার থেকে বৃহস্পতিবার সকাল ৯:০০টা থেকে নির্ধারিত কক্ষে নিয়মিত অনার্স ক্লাস পরিচালিত হয়। বিভাগীয় নোটিশ বোর্ড অনুযায়ী রুটিন অনুসরণ করুন।",
            badgeText = "রুটিন",
            iconKey = "schedule",
            accentStyle = "teal",
            layoutSpan = "half",
            pinned = false,
            sortOrder = 3L,
            status = "published"
        ),
        CollegeSectionBlock(
            id = "sahc_faculty_info",
            blockType = "faculty",
            title = "বিভাগীয় শিক্ষকমণ্ডলী",
            subtitle = "অভিজ্ঞ শিক্ষক প্যানেল",
            body = "সকল অনার্স বিভাগে অভিজ্ঞ ও নিবেদিতপ্রাণ শিক্ষকবৃন্দ নিয়মিত পাঠদান ও একাডেমিক পরামর্শ প্রদান করছেন।",
            badgeText = "শিক্ষক",
            iconKey = "groups",
            accentStyle = "emerald",
            layoutSpan = "half",
            pinned = false,
            sortOrder = 4L,
            status = "published"
        ),
        CollegeSectionBlock(
            id = "sahc_honours_departments",
            blockType = "department",
            title = "অনার্স বিভাগসমূহ ও একাডেমিক তথ্য",
            subtitle = "জাতীয় বিশ্ববিদ্যালয় কারিকুলাম",
            body = "সুজাত আলী অনার্স কলেজ, পিংনা-তে পরিচালিত সকল অনার্স বিভাগের সেমিনার তথ্য, সিলেবাস সহায়তা ও পরীক্ষার প্রস্তুতি রিসোর্স অ্যাডমিন নিজের মতো করে সাজাতে পারবেন।",
            badgeText = "অনার্স",
            iconKey = "book",
            accentStyle = "primary",
            layoutSpan = "full",
            pinned = false,
            sortOrder = 5L,
            status = "published"
        ),
        CollegeSectionBlock(
            id = "sahc_admission_help",
            blockType = "admission",
            title = "ভর্তি ও ফর্ম ফিলাপ সহায়তা",
            subtitle = "শিক্ষার্থী সেবা ডেস্ক",
            body = "ভর্তি সংক্রান্ত কাগজপত্র, রেজিস্ট্রেশন কার্ড সংগ্রহ, প্রবেশপত্র বিতরণ ও ফর্ম ফিলাপের সময়সূচি কলেজের অফিস কক্ষ ও এই পেজে প্রকাশ করা হয়।",
            badgeText = "হেল্প ডেস্ক",
            iconKey = "info",
            accentStyle = "crimson",
            layoutSpan = "full",
            pinned = false,
            sortOrder = 6L,
            status = "published"
        )
    )
}
