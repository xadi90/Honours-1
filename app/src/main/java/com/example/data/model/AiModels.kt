package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AnswerLength(val labelBn: String, val labelEn: String, val guidance: String) {
    SHORT("সংক্ষিপ্ত উত্তর", "Short Answer", "Concise 2-4 mark exam-ready answer (120-180 words)."),
    MEDIUM("মাঝারি উত্তর", "Medium Answer", "Balanced 4-6 mark answer with clear headings (250-400 words)."),
    DETAILED("বিস্তারিত উত্তর", "Detailed Answer", "Comprehensive Honours-level analytical answer (600+ words)."),
    EXAM_READY("পরীক্ষার উপযোগী উত্তর", "Exam-Ready Answer", "Structured for NU 10-mark broad exam format with intro, numbered points, scholar references, examples, and conclusion.")
}

enum class AiTutorMode(val labelBn: String, val labelEn: String, val promptPrefix: String) {
    CHAT("সাধারণ আলোচনা", "Interactive Chat", "Act as an expert National University Political Science & Honours tutor."),
    CONCEPT("ধারণা ব্যাখ্যা", "Explain Concept", "Explain this academic concept clearly with definitions and examples."),
    SUMMARY("অধ্যায় সারসংক্ষেপ", "Chapter Summary", "Provide a structured, concise chapter summary with key takeaways."),
    QUESTION_GEN("প্রশ্ন তৈরি", "Generate Questions", "Generate likely NU Honours Part-A, Part-B, and Part-C exam questions."),
    MCQ_GEN("MCQ তৈরি", "Generate MCQs", "Generate 5 practice MCQs with 4 options, correct answer, and explanation."),
    EVALUATE("উত্তর মূল্যায়ন", "Evaluate Answer", "Evaluate the student's written answer on accuracy, structure, and NU exam readiness, and suggest improvements."),
    STUDY_PLAN("পড়ার পরিকল্পনা", "Study Plan", "Create a practical study & exam revision plan tailored to the student's year and subject.")
}

@Keep
@IgnoreExtraProperties
data class StructuredAiAnswer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val question: String = "",
    val answerLength: String = AnswerLength.EXAM_READY.labelBn,
    val yearLabel: String = "অনার্স ১ম বর্ষ",
    val subjectName: String = "রাষ্ট্রবিজ্ঞান",
    val language: String = "bn",
    val prashno: String = "",
    val bhumika: String = "",
    val mulAlochona: String = "",
    val importantPoints: List<String> = emptyList(),
    val examples: String = "",
    val uposhonghar: String = "",
    val sourceNote: String = "AI-generated academic explanation (verify with official NU syllabus & textbooks).",
    val hasImageInput: Boolean = false,
    val bookmarked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AiConversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val mode: String = AiTutorMode.CHAT.name,
    val subjectName: String = "",
    val yearNumber: Long = 1L,
    val lastMessage: String = "",
    val messageCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AiMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user", // user | model
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class GeneratedMcqItem(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Keep
@IgnoreExtraProperties
data class AiRemoteConfig(
    val modelName: String = "gemini-3.8-flash",
    val systemInstructions: String = "",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = ""
)
