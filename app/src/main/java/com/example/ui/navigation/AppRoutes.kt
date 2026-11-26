package com.example.ui.navigation

object AppRoutes {
    // Bottom Navigation Tabs
    const val HOME = "home"
    const val AI_SOLVER = "ai_solver"
    const val PDF_LIBRARY = "pdf_library"
    const val BOOKMARKS = "bookmarks"
    const val PROFILE = "profile"

    // Academic & Political Science Screens
    const val POLITICAL_SCIENCE = "political_science"
    const val HONOURS_YEAR = "honours_year/{year}"
    fun honoursYear(year: Int) = "honours_year/$year"

    const val SUBJECT_DETAILS = "subject_details/{subjectId}"
    fun subjectDetails(subjectId: String) = "subject_details/$subjectId"

    const val CHAPTER_DETAILS = "chapter_details/{subjectId}/{chapterId}"
    fun chapterDetails(subjectId: String, chapterId: String) = "chapter_details/$subjectId/$chapterId"

    // AI Screens
    const val AI_CHAT = "ai_chat"
    const val QUESTION_IMAGE_UPLOAD = "question_image_upload"
    const val ANSWER_DETAILS = "answer_details"

    // PDF Library & Reader
    const val DEPARTMENT_PDFS = "department_pdfs/{departmentId}"
    fun departmentPdfs(departmentId: String) = "department_pdfs/$departmentId"

    const val PDF_READER = "pdf_reader/{pdfId}"
    fun pdfReader(pdfId: String) = "pdf_reader/$pdfId"

    const val DOWNLOAD_MANAGER = "download_manager"

    // Exam Preparation & Search
    const val GLOBAL_SEARCH = "global_search"
    const val PREVIOUS_QUESTIONS = "previous_questions"
    const val SUGGESTIONS = "suggestions"
    const val MCQ_PRACTICE = "mcq_practice"
    const val MODEL_TEST = "model_test"

    // Profile, Settings, Info & College Portal
    const val SUJAT_ALI_COLLEGE = "sujat_ali_college"
    const val SETTINGS = "settings"
    const val ABOUT_DEVELOPER = "about_developer"

    // Admin Content Management
    const val ADMIN_PASSWORD = "admin_password"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_PDF_UPLOAD = "admin_pdf_upload"
    const val ADMIN_CONTENT_EDITOR = "admin_content_editor"
    const val ADMIN_ANNOUNCEMENTS = "admin_announcements"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_LIMIT_MANAGEMENT = "admin_limit_management"
    const val ADMIN_COLLEGE_EDITOR = "admin_college_editor"
}
