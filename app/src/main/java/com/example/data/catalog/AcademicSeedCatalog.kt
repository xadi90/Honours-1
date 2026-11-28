package com.example.data.catalog

import com.example.data.model.Announcement
import com.example.data.model.Chapter
import com.example.data.model.Department
import com.example.data.model.ModelTest
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject

object AcademicSeedCatalog {

    val honoursYears = listOf(
        1 to "রাষ্ট্রবিজ্ঞান – অনার্স ১ম বর্ষ",
        2 to "রাষ্ট্রবিজ্ঞান – অনার্স ২য় বর্ষ",
        3 to "রাষ্ট্রবিজ্ঞান – অনার্স ৩য় বর্ষ",
        4 to "রাষ্ট্রবিজ্ঞান – অনার্স ৪র্থ বর্ষ"
    )

    val pdfCategories = listOf(
        "Book PDFs",
        "Class Notes",
        "Lecture Sheets",
        "Previous-Year Question Papers",
        "Suggestions",
        "Reference Materials",
        "Exam Preparation PDFs"
    )

    val defaultDepartments: List<Department> = listOf(
        Department("political_science", "রাষ্ট্রবিজ্ঞান", "Political Science", "POL", "Primary structured 1st–4th Year Honours solutions, syllabus, & PDF library", true, 1),
        Department("bangla", "বাংলা", "Bangla", "BNG", "বাংলা সাহিত্য ও ব্যাকরণ PDF লাইব্রেরি", false, 2),
        Department("english", "ইংরেজি", "English", "ENG", "English Literature & Linguistics PDF Library", false, 3),
        Department("history", "ইতিহাস", "History", "HIS", "ইতিহাস বিভাগের PDF বই ও নোট", false, 4),
        Department("islamic_history", "ইসলামের ইতিহাস ও সংস্কৃতি", "Islamic History & Culture", "IHC", "ইসলামের ইতিহাস ও সংস্কৃতি PDF সংগ্রহ", false, 5),
        Department("philosophy", "দর্শন", "Philosophy", "PHI", "দর্শন বিভাগের বই ও লেকচার শিট", false, 6),
        Department("sociology", "সমাজবিজ্ঞান", "Sociology", "SOC", "সমাজবিজ্ঞান বিভাগের PDF রিসোর্স", false, 7),
        Department("social_work", "সমাজকর্ম", "Social Work", "SWK", "সমাজকর্ম বিভাগের নোট ও সাজেশন", false, 8),
        Department("economics", "অর্থনীতি", "Economics", "ECO", "ব্যষ্টিক ও সামষ্টিক অর্থনীতি PDF লাইব্রেরি", false, 9),
        Department("psychology", "মনোবিজ্ঞান", "Psychology", "PSY", "মনোবিজ্ঞান বিভাগের বই ও শিট", false, 10),
        Department("geography", "ভূগোল ও পরিবেশ", "Geography & Environment", "GEO", "ভূগোল ও পরিবেশ বিজ্ঞান PDF", false, 11),
        Department("mathematics", "গণিত", "Mathematics", "MAT", "ক্যালকুলাস, অ্যালজেব্রা ও ম্যাথমেটিক্স PDF", false, 12),
        Department("physics", "পদার্থবিজ্ঞান", "Physics", "PHY", "পদার্থবিজ্ঞান বই, ল্যাব নোট ও প্রশ্নপত্র", false, 13),
        Department("chemistry", "রসায়ন", "Chemistry", "CHE", "জৈব ও অজৈব রসায়ন PDF লাইব্রেরি", false, 14),
        Department("botany", "উদ্ভিদবিজ্ঞান", "Botany", "BOT", "উদ্ভিদবিজ্ঞান নোট ও রেফারেন্স বই", false, 15),
        Department("zoology", "প্রাণিবিজ্ঞান", "Zoology", "ZOO", "প্রাণিবিজ্ঞান PDF বই ও নোট", false, 16),
        Department("accounting", "হিসাববিজ্ঞান", "Accounting", "ACC", "অ্যাকাউন্টিং ও ফিন্যান্সিয়াল রিপোর্টিং PDF", false, 17),
        Department("management", "ব্যবস্থাপনা", "Management", "MGT", "ম্যানেজমেন্ট বই ও এক্সাম সাজেশন", false, 18),
        Department("marketing", "মার্কেটিং", "Marketing", "MKT", "মার্কেটিং প্রিন্সিপালস ও কেস স্টাডি PDF", false, 19),
        Department("finance_banking", "ফিন্যান্স ও ব্যাংকিং", "Finance & Banking", "FIN", "ফিন্যান্স, ব্যাংকিং ও ইনভেস্টমেন্ট PDF", false, 20),
        Department("statistics", "পরিসংখ্যান", "Statistics", "STA", "পরিসংখ্যান ও ডাটা অ্যানালাইসিস PDF", false, 21),
        Department("other_honours", "অন্যান্য উপলব্ধ অনার্স বিভাগ", "Other Honours Departments", "OTH", "জাতীয় বিশ্ববিদ্যালয়ের অন্যান্য সকল অনার্স বিভাগ", false, 22)
    )

    val starterSubjects: List<Subject> = listOf(
        Subject("pol_y1_1", "political_science", 1, "PS-101 (Editable)", "রাজনৈতিক তত্ত্ব পরিচিতি", "Introduction to Political Theory", 4, "Starter template — verify & update exact NU course code via Content Management."),
        Subject("pol_y1_2", "political_science", 1, "PS-102 (Editable)", "পাশ্চাত্য রাষ্ট্রচিন্তা", "Western Political Thought", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y1_3", "political_science", 1, "PS-103 (Editable)", "প্রধান প্রধান বৈদেশিক সরকার", "Major Foreign Governments", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y2_1", "political_science", 2, "PS-201 (Editable)", "বাংলাদেশের সরকার ও রাজনীতি", "Government and Politics of Bangladesh", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y2_2", "political_science", 2, "PS-202 (Editable)", "প্রাচ্যের রাষ্ট্রচিন্তা", "Oriental Political Thought", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y3_1", "political_science", 3, "PS-301 (Editable)", "আন্তর্জাতিক রাজনীতি ও সংগঠন", "International Politics & Organization", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y3_2", "political_science", 3, "PS-302 (Editable)", "লোক প্রশাসন পরিচিতি", "Introduction to Public Administration", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y4_1", "political_science", 4, "PS-401 (Editable)", "আধুনিক রাজনৈতিক তত্ত্ব ও বিশ্লেষণ", "Modern Political Theory & Analysis", 4, "Starter template — editable from Admin Panel."),
        Subject("pol_y4_2", "political_science", 4, "PS-402 (Editable)", "বাংলাদেশের বৈদেশিক সম্পর্ক ও পররাষ্ট্রনীতি", "Foreign Relations & Foreign Policy of Bangladesh", 4, "Starter template — editable from Admin Panel.")
    )

    val starterChapters: List<Chapter> = listOf(
        Chapter(
            id = "ch_pol_y1_1_1",
            subjectId = "pol_y1_1",
            yearNumber = 1,
            chapterNumber = 1,
            titleBn = "রাষ্ট্রবিজ্ঞানের প্রকৃতি, পরিধি ও পদ্ধতি",
            titleEn = "Nature, Scope & Methods of Political Science",
            topics = listOf("রাষ্ট্রবিজ্ঞানের সংজ্ঞা", "প্রকৃতি ও পরিধি", "অধ্যয়ন পদ্ধতিসমূহ", "সামাজিক বিজ্ঞানের অন্যান্য শাখার সাথে সম্পর্ক"),
            summary = "রাষ্ট্রবিজ্ঞান হলো সমাজের সেই শাখা যা রাষ্ট্র, সরকার, ক্ষমতা, কর্তৃত্ব এবং রাজনৈতিক আচরণ নিয়ে বৈজ্ঞানিক ও দার্শনিক বিশ্লেষণ করে। অ্যারিস্টটল রাষ্ট্রবিজ্ঞানকে 'Master Science' হিসেবে অভিহিত করেছেন।",
            definitions = "• রাষ্ট্র (State): নির্দিষ্ট ভূখণ্ড, জনসমষ্টি, সরকার ও সার্বভৌমত্বের সমন্বয়ে গঠিত রাজনৈতিক সংগঠন।\n• সার্বভৌমত্ব (Sovereignty): রাষ্ট্রের সর্বোচ্চ ও চূড়ান্ত আইনগত ক্ষমতা।",
            shortQuestions = "১. রাষ্ট্রবিজ্ঞানকে কেন 'মাস্টার সায়েন্স' বলা হয়?\nউত্তর: অ্যারিস্টটলের মতে, রাষ্ট্রবিজ্ঞান সমাজের সর্বোচ্চ কল্যাণ ও অন্যান্য সকল বিজ্ঞানকে নিয়ন্ত্রণ করে বলেই একে মাস্টার সায়েন্স বলা হয়।\n\n২. আধুনিক রাষ্ট্রবিজ্ঞানের জনক কে?\nউত্তর: নিকোলো ম্যাকিয়াভেলি।",
            broadQuestions = "প্রশ্ন: রাষ্ট্রবিজ্ঞানের সংজ্ঞা দাও এবং এর পরিধি ও বিষয়বস্তু আলোচনা করো।\n(বিস্তারিত কাঠামোগত উত্তর দেখতে AI প্রশ্ন সমাধান অথবা নোট PDF ব্যবহার করুন।)",
            creativeQuestions = "উদ্দীপক বিশ্লেষণ: সাম্প্রতিক সময়ে ঐতিহ্যবাহী প্রাতিষ্ঠানিক পদ্ধতির পাশাপাশি আচরণবাদী (Behavioural) ও উত্তর-আচরণবাদী পদ্ধতি রাষ্ট্রবিজ্ঞান গবেষণায় কী ধরনের পরিবর্তন এনেছে?"
        ),
        Chapter(
            id = "ch_pol_y1_1_2",
            subjectId = "pol_y1_1",
            yearNumber = 1,
            chapterNumber = 2,
            titleBn = "রাষ্ট্রের উৎপত্তি ও সার্বভৌমত্ব তত্ত্ব",
            titleEn = "Origin of State & Theory of Sovereignty",
            topics = listOf("সামাজিক চুক্তি মতবাদ", "ঐতিহাসিক ও বিবর্তনমূলক মতবাদ", "অস্টিনের একত্ববাদী সার্বভৌমত্ব", "বহুত্ববাদী সার্বভৌমত্ব"),
            summary = "রাষ্ট্রের উৎপত্তি সম্পর্কে ঐশ্বরিক, বলপ্রয়োগ, সামাজিক চুক্তি (হবস, লক, রুশো) এবং ঐতিহাসিক মতবাদ প্রচলিত রয়েছে। এর মধ্যে ঐতিহাসিক বা বিবর্তনমূলক মতবাদকে সর্বাধিক বিজ্ঞানসম্মত মনে করা হয়।",
            definitions = "• সামাজিক চুক্তি: হবস, লক ও রুশোর মতে, প্রাকৃতিক অবস্থা থেকে বের হয়ে মানুষ চুক্তির মাধ্যমে রাষ্ট্র গঠন করে।",
            shortQuestions = "১. 'The Leviathan' গ্রন্থের রচয়িতা কে?\nউত্তর: টমাস হবস (১৬৫১)।\n\n২. রুশোর সাধারণ ইচ্ছা (General Will) কী?\nউত্তর: সমগ্র সমাজের কল্যাণকামী সামষ্টিক ইচ্ছাকেই রুশো সাধারণ ইচ্ছা বলেছেন।",
            broadQuestions = "প্রশ্ন: জন অস্টিনের সার্বভৌমত্ব তত্ত্ব সমালোচনাসহ আলোচনা করো।",
            creativeQuestions = "বিশ্বায়নের যুগে জাতি-রাষ্ট্রের সার্বভৌমত্বের সীমাবদ্ধতা বিশ্লেষণ করো।"
        ),
        Chapter(
            id = "ch_pol_y2_1_1",
            subjectId = "pol_y2_1",
            yearNumber = 2,
            chapterNumber = 1,
            titleBn = "১৯৭২ সালের বাংলাদেশের সংবিধানের মূল বৈশিষ্ট্য",
            titleEn = "Salient Features of the 1972 Constitution of Bangladesh",
            topics = listOf("সংবিধান প্রণয়নের পটভূমি", "রাষ্ট্র পরিচালনার মূলনীতি", "মৌলিক অধিকার", "সংসদীয় শাসনব্যবস্থা"),
            summary = "১৯৭২ সালের ৪ নভেম্বর গণপরিষদে গৃহীত এবং ১৬ ডিসেম্বর কার্যকর বাংলাদেশের সংবিধান বিশ্বের অন্যতম লিখিত ও দুষ্পরিবর্তনীয় সংবিধানগুলোর একটি।",
            definitions = "• মৌলিক অধিকার: সংবিধানের তৃতীয় ভাগে (অনুচ্ছেদ ২৭–৪৪) নাগরিকদের মৌলিক অধিকারসমূহ বর্ণিত আছে।",
            shortQuestions = "১. বাংলাদেশের সংবিধান কবে কার্যকর হয়?\nউত্তর: ১৯৭২ সালের ১৬ ডিসেম্বর।",
            broadQuestions = "প্রশ্ন: ১৯৭২ সালের বাংলাদেশের সংবিধানের প্রধান বৈশিষ্ট্যসমূহ আলোচনা করো।",
            creativeQuestions = "মৌলিক অধিকার ও রাষ্ট্র পরিচালনার মূলনীতির মধ্যে পার্থক্য ও সম্পর্ক মূল্যায়ন করো।"
        )
    )

    val starterQuestions: List<StudyQuestion> = listOf(
        StudyQuestion(
            id = "q_prev_1",
            departmentId = "political_science",
            yearNumber = 1,
            subjectId = "pol_y1_1",
            subjectName = "রাজনৈতিক তত্ত্ব পরিচিতি",
            courseCode = "PS-101",
            questionType = "previous_year",
            examYear = "2023",
            partLabel = "খ-বিভাগ (সংক্ষিপ্ত)",
            prompt = "সার্বভৌমত্ব কী? জন অস্টিনের সার্বভৌমত্বের সংজ্ঞা ও বৈশিষ্ট্য লিখ।",
            answerText = "সার্বভৌমত্ব হলো রাষ্ট্রের সর্বোচ্চ, অবিভাজ্য ও চূড়ান্ত ক্ষমতা। জন অস্টিনের মতে, 'কোনো ঊর্ধ্বতন কর্তৃপক্ষের প্রতি অনুগত নয় এমন কোনো নির্দিষ্ট ব্যক্তি বা ব্যক্তিসমষ্টির প্রতি সমাজের অধিকাংশ মানুষ অভ্যস্ত আনুগত্য প্রদর্শন করলে সেই ব্যক্তি বা ব্যক্তিসমষ্টি উক্ত সমাজে সার্বভৌম।' বৈশিষ্ট্য: চূড়ান্ত, স্থায়ী, অবিভাজ্য, অহস্তান্তরযোগ্য ও সর্বজনীন।",
            priority = "High Frequency"
        ),
        StudyQuestion(
            id = "q_prev_2",
            departmentId = "political_science",
            yearNumber = 1,
            subjectId = "pol_y1_2",
            subjectName = "পাশ্চাত্য রাষ্ট্রচিন্তা",
            courseCode = "PS-102",
            questionType = "previous_year",
            examYear = "2023",
            partLabel = "গ-বিভাগ (রচনামূলক)",
            prompt = "প্লেটোর আদর্শ রাষ্ট্র (Ideal State) ও ন্যায়বিচার তত্ত্ব আলোচনা করো।",
            answerText = "প্লেটো তাঁর 'The Republic' গ্রন্থে আদর্শ রাষ্ট্র ও ন্যায়বিচারের ধারণা তুলে ধরেন। তাঁর মতে, সমাজের তিনটি শ্রেণি—শাসক (দার্শনিক রাজা), সৈনিক ও উৎপাদক—প্রত্যেকে নিজ নিজ দায়িত্ব সুষ্ঠুভাবে পালন করলে রাষ্ট্রে ন্যায়বিচার প্রতিষ্ঠিত হয়।",
            priority = "Very Important"
        ),
        StudyQuestion(
            id = "q_sug_1",
            departmentId = "political_science",
            yearNumber = 2,
            subjectId = "pol_y2_1",
            subjectName = "বাংলাদেশের সরকার ও রাজনীতি",
            courseCode = "PS-201",
            questionType = "suggestion",
            examYear = "Target Exam",
            partLabel = "গ-বিভাগ সাজেশন",
            prompt = "বাংলাদেশের ১৯৭২ সালের সংবিধানের মূল বৈশিষ্ট্যসমূহ এবং মৌলিক অধিকারসমূহ আলোচনা করো।",
            answerText = "ভূমিকা, ঐতিহাসিক পটভূমি, লিখিত ও দুষ্পরিবর্তনীয় প্রকৃতি, সংসদীয় গণতন্ত্র, রাষ্ট্র পরিচালনার ৪ মূলনীতি, ২৭–৪৪ অনুচ্ছেদে মৌলিক অধিকার, বিচার বিভাগের স্বাধীনতা ও উপসংহার আকারে লিখুন।",
            priority = "100% Important"
        ),
        StudyQuestion(
            id = "q_mcq_1",
            departmentId = "political_science",
            yearNumber = 1,
            subjectId = "pol_y1_1",
            subjectName = "রাজনৈতিক তত্ত্ব পরিচিতি",
            courseCode = "PS-101",
            questionType = "mcq",
            prompt = "'Politics' গ্রন্থটির রচয়িতা কে?",
            options = listOf("প্লেটো", "অ্যারিস্টটল", "সক্রেটিস", "ম্যাকিয়াভেলি"),
            correctOptionIndex = 1L,
            explanation = "গ্রিক দার্শনিক অ্যারিস্টটল 'Politics' গ্রন্থটি রচনা করেন; এজন্য তাঁকে রাষ্ট্রবিজ্ঞানের জনক বলা হয়।",
            priority = "Core"
        ),
        StudyQuestion(
            id = "q_mcq_2",
            departmentId = "political_science",
            yearNumber = 1,
            subjectId = "pol_y1_2",
            subjectName = "পাশ্চাত্য রাষ্ট্রচিন্তা",
            courseCode = "PS-102",
            questionType = "mcq",
            prompt = "মন্টেস্কু কোন তত্ত্বের জন্য বিখ্যাত?",
            options = listOf("সামাজিক চুক্তি তত্ত্ব", "ক্ষমতা স্বতন্ত্রীকরণ নীতি", "সার্বভৌমত্ব তত্ত্ব", "উপযোগবাদ"),
            correctOptionIndex = 1L,
            explanation = "ফরাসি দার্শনিক মন্টেস্কু তাঁর 'The Spirit of the Laws' গ্রন্থে ক্ষমতা স্বতন্ত্রীকরণ নীতি (Separation of Powers) উপস্থাপন করেন।",
            priority = "Core"
        ),
        StudyQuestion(
            id = "q_mcq_3",
            departmentId = "political_science",
            yearNumber = 2,
            subjectId = "pol_y2_1",
            subjectName = "বাংলাদেশের সরকার ও রাজনীতি",
            courseCode = "PS-201",
            questionType = "mcq",
            prompt = "বাংলাদেশের সংবিধানের কত নং অনুচ্ছেদে আইনের দৃষ্টিতে সমতার কথা বলা হয়েছে?",
            options = listOf("২৬ নং অনুচ্ছেদ", "২৭ নং অনুচ্ছেদ", "৩১ নং অনুচ্ছেদ", "৩৯ নং অনুচ্ছেদ"),
            correctOptionIndex = 1L,
            explanation = "বাংলাদেশের সংবিধানের ২৭ নং অনুচ্ছেদে বলা হয়েছে, 'সকল নাগরিক আইনের দৃষ্টিতে সমান এবং আইনের সমান আশ্রয় লাভের অধিকারী।'",
            priority = "Core"
        )
    )

    val starterModelTests: List<ModelTest> = listOf(
        ModelTest(
            id = "mt_pol_1",
            departmentId = "political_science",
            yearNumber = 1,
            subjectId = "pol_y1_1",
            subjectName = "রাজনৈতিক তত্ত্ব ও রাষ্ট্রচিন্তা",
            title = "অনার্স ১ম বর্ষ মডেল টেস্ট — ০১",
            durationMinutes = 10,
            questionCount = 3
        )
    )

    const val PDF_SEED_VERSION = 2L

    private fun honoursPdf(
        id: String,
        deptId: String,
        deptName: String,
        year: Long,
        subject: String,
        code: String,
        title: String,
        category: String,
        desc: String
    ) = PdfResource(
        id = id,
        departmentId = deptId,
        departmentName = deptName,
        yearNumber = year,
        subjectName = subject,
        courseCode = code,
        title = title,
        category = category,
        description = desc,
        localFileName = "${id}.pdf",
        pageCount = 2L,
        fileSizeBytes = 14800L,
        uploadedBy = "Admin",
        status = "published"
    )

    val starterPdfs: List<PdfResource> = listOf(
        // ১. রাষ্ট্রবিজ্ঞান (Political Science) - ১ম থেকে ৪র্থ বর্ষের সকল মূল বিষয়
        honoursPdf("pdf_starter_pol1", "political_science", "রাষ্ট্রবিজ্ঞান", 1, "রাজনৈতিক তত্ত্ব পরিচিতি", "211901", "রাজনৈতিক তত্ত্ব পরিচিতি — সম্পূর্ণ স্টাডি গাইড ও প্রশ্ন কাঠামো", "Suggestions", "রাষ্ট্রের উৎপত্তি, সার্বভৌমত্ব, আইন, স্বাধীনতা, সাম্য, অধিকার ও গণতন্ত্র বিষয়ক জাতীয় বিশ্ববিদ্যালয় ১ম বর্ষ গাইড।"),
        honoursPdf("pdf_pol_102", "political_science", "রাষ্ট্রবিজ্ঞান", 1, "পাশ্চাত্য রাষ্ট্রচিন্তা", "211903", "পাশ্চাত্য রাষ্ট্রচিন্তা (প্লেটো, অ্যারিস্টটল, হবস, লক, রুশো) নোট", "Class Notes", "প্রাচীন ও আধুনিক পাশ্চাত্য রাষ্ট্রদার্শনিকদের মূল তত্ত্ব ও বিগত সালের সংক্ষিপ্ত-রচনামূলক প্রশ্নের গাইড।"),
        honoursPdf("pdf_pol_103", "political_science", "রাষ্ট্রবিজ্ঞান", 1, "প্রধান প্রধান বৈদেশিক সরকার", "211905", "বৈদেশিক সরকার (যুক্তরাজ্য, যুক্তরাষ্ট্র ও ফ্রান্স) লেকচার শিট", "Lecture Sheets", "তুলনামূলক শাসনব্যবস্থা, ব্রিটিশ পার্লামেন্ট, মার্কিন রাষ্ট্রপতি ও ফরাসি শাসনকাঠামো।"),
        honoursPdf("pdf_starter_pol2", "political_science", "রাষ্ট্রবিজ্ঞান", 2, "বাংলাদেশের সরকার ও রাজনীতি", "221901", "বাংলাদেশের সংবিধান ও শাসনব্যবস্থা — পূর্ণাঙ্গ নোট PDF", "Class Notes", "১৯৭২ সালের সংবিধান, মৌলিক অধিকার, সংসদ, নির্বাহী ও বিচার বিভাগ সম্পর্কিত ২য় বর্ষ গাইড।"),
        honoursPdf("pdf_pol_202", "political_science", "রাষ্ট্রবিজ্ঞান", 2, "প্রাচ্যের রাষ্ট্রচিন্তা", "221903", "প্রাচ্যের রাষ্ট্রচিন্তা (ইবনে খালদুন, আল-ফারাবি, কৌটিল্য)", "Book PDFs", "মুসলিম ও প্রাচ্য রাষ্ট্রচিন্তার মৌলিক ধারণা, আসাবিয়্যাহ তত্ত্ব ও রাষ্ট্রদর্শন।"),
        honoursPdf("pdf_pol_301", "political_science", "রাষ্ট্রবিজ্ঞান", 3, "আন্তর্জাতিক রাজনীতি ও সংগঠন", "231901", "আন্তর্জাতিক রাজনীতি ও জাতিসংঘ — ৩য় বর্ষ সাজেশন ও নোট", "Suggestions", "জাতীয় শক্তি, পররাষ্ট্রনীতি, স্নায়ুযুদ্ধ, ন্যাম, জাতিসংঘ ও আঞ্চলিক সংস্থা।"),
        honoursPdf("pdf_pol_302", "political_science", "রাষ্ট্রবিজ্ঞান", 3, "লোক প্রশাসন পরিচিতি", "231903", "লোক প্রশাসন ও আমলাতন্ত্র — ৩য় বর্ষ হ্যান্ডনোট", "Lecture Sheets", "প্রশাসনিক সংগঠন, ওয়েবারের আমলাতন্ত্র, নেতৃত্ব, সিদ্ধান্ত গ্রহণ ও বাজেট ব্যবস্থা।"),
        honoursPdf("pdf_pol_303", "political_science", "রাষ্ট্রবিজ্ঞান", 3, "গবেষণা পদ্ধতি ও পরিসংখ্যান", "231905", "সামাজিক গবেষণা পদ্ধতি ও পরিসংখ্যান গাইড", "Exam Preparation PDFs", "হাইপোথিসিস, স্যাম্পলিং, প্রশ্নমালা তৈরি ও মৌলিক পরিসংখ্যান সূত্রাবলি।"),
        honoursPdf("pdf_pol_401", "political_science", "রাষ্ট্রবিজ্ঞান", 4, "আধুনিক রাজনৈতিক তত্ত্ব ও বিশ্লেষণ", "241901", "আধুনিক রাজনৈতিক তত্ত্ব — ৪র্থ বর্ষ চূড়ান্ত প্রস্তুতি", "Exam Preparation PDFs", "সিস্টেম তত্ত্ব, স্ট্রাকচারাল ফাংশনালিজম, রাজনৈতিক সংস্কৃতি ও আধুনিকায়ন।"),
        honoursPdf("pdf_pol_402", "political_science", "রাষ্ট্রবিজ্ঞান", 4, "বাংলাদেশের বৈদেশিক সম্পর্ক ও পররাষ্ট্রনীতি", "241903", "বাংলাদেশের পররাষ্ট্রনীতি ও কূটনীতি — ৪র্থ বর্ষ নোট", "Previous-Year Question Papers", "সার্ক, বিমসটেক, রোহিঙ্গা সংকট, অর্থনৈতিক কূটনীতি ও প্রতিবেশী দেশের সাথে সম্পর্ক।"),

        // ২. বাংলা (Bangla)
        honoursPdf("pdf_bng_101", "bangla", "বাংলা", 1, "বাংলা ভাষা ও সাহিত্যের ইতিহাস (প্রাচীন ও মধ্যযুগ)", "211001", "চর্যাপদ ও মধ্যযুগের বাংলা সাহিত্য — ১ম বর্ষ গাইড", "Book PDFs", "চর্যাপদ, শ্রীকৃষ্ণকীর্তন, বৈষ্ণব পদাবলী, মঙ্গলকাব্য ও অনুবাদ সাহিত্যের মূল বিষয়বস্তু।"),
        honoursPdf("pdf_bng_201", "bangla", "বাংলা", 2, "বাংলা কবিতা ও কাব্য", "221001", "মাইকেল, রবীন্দ্র ও নজরুল কাব্য — ২য় বর্ষ লেকচার শিট", "Lecture Sheets", "মেঘনাদবধ কাব্য, সোনার তরী ও অগ্নিবীণা কাব্যের ব্যাখ্যা ও প্রশ্নোত্তর।"),
        honoursPdf("pdf_bng_301", "bangla", "বাংলা", 3, "বাংলা উপন্যাস ও নাটক", "231001", "বাংলা উপন্যাস ও নাট্যসাহিত্য — ৩য় বর্ষ নোট", "Class Notes", "বিষাদ-সিন্ধু, পদ্মা নদীর মাঝি, লালসালু ও রক্তকরবী বিষয়ক বিশ্লেষণ।"),
        honoursPdf("pdf_bng_401", "bangla", "বাংলা", 4, "বাংলা সাহিত্যের আধুনিক যুগ ও সমালোচনা", "241001", "আধুনিক বাংলা প্রবন্ধ, ছোটগল্প ও ছন্দ-অলংকার সাজেশন", "Suggestions", "বাংলা ছন্দ, অলংকার, ধ্বনিতত্ত্ব ও সমালোচনা সাহিত্যের চূড়ান্ত প্রস্তুতি।"),

        // ৩. ইংরেজি (English)
        honoursPdf("pdf_eng_101", "english", "ইংরেজি", 1, "English Reading & Writing Skills", "211101", "Academic Reading, Writing & Intro to Poetry — Year 1", "Book PDFs", "Critical reading strategies, essay structure, and foundational English poetry analysis."),
        honoursPdf("pdf_eng_201", "english", "ইংরেজি", 2, "Romantic Poetry & History of English Literature", "221101", "Wordsworth, Coleridge, Shelley & Keats — Year 2 Notes", "Class Notes", "Key themes, poetic devices, and literary eras from Old English to the Romantic period."),
        honoursPdf("pdf_eng_301", "english", "ইংরেজি", 3, "Elizabethan Drama & Victorian Literature", "231101", "Shakespeare, Marlowe & Victorian Poets — Year 3 Guide", "Lecture Sheets", "Macbeth, Hamlet, Doctor Faustus, Tennyson, and Browning critical summaries."),
        honoursPdf("pdf_eng_401", "english", "ইংরেজি", 4, "Twentieth Century Novel & Literary Criticism", "241101", "Modernism, Literary Theory & Exam Suggestions — Year 4", "Suggestions", "Aristotle, Eliot, Structuralism, Post-Colonialism, and 20th-century novels."),

        // ৪. ইতিহাস (History)
        honoursPdf("pdf_his_101", "history", "ইতিহাস", 1, "প্রাচীন বাংলার ইতিহাস ও বিশ্বসভ্যতা", "211501", "প্রাচীন বাংলা (পাল, সেন) ও বিশ্বসভ্যতার ইতিহাস গাইড", "Book PDFs", "মৌর্য, গুপ্ত, পাল ও সেন আমল এবং প্রাচীন মিসর, মেসোপটেমিয়া ও গ্রিক সভ্যতা।"),
        honoursPdf("pdf_his_201", "history", "ইতিহাস", 2, "মধ্যযুগের বাংলার ইতিহাস ও ইউরোপ", "221501", "সুলতানি ও মুঘল বাংলা এবং রেনেসাঁস — ২য় বর্ষ নোট", "Class Notes", "স্বাধীন সুলতানি আমল, মুঘল সুবা বাংলা এবং ইউরোপীয় নবজাগরণ ও সংস্কার আন্দোলন।"),
        honoursPdf("pdf_his_301", "history", "ইতিহাস", 3, "আধুনিক ভারতের ও দক্ষিণ এশিয়ার ইতিহাস", "231501", "ব্রিটিশ শাসন, ১৮৫৭ বিপ্লব ও জাতীয়তাবাদ — ৩য় বর্ষ শিট", "Lecture Sheets", "পলাশী যুদ্ধ থেকে ১৯৪৭ সালের দেশভাগ পর্যন্ত ভারতের রাজনৈতিক ও সামাজিক ইতিহাস।"),
        honoursPdf("pdf_his_401", "history", "ইতিহাস", 4, "বাংলাদেশের মুক্তিযুদ্ধের ইতিহাস ও আধুনিক বিশ্ব", "241501", "১৯৫২–১৯৭১ মুক্তিযুদ্ধ ও সমকালীন বিশ্ব ইতিহাস সাজেশন", "Suggestions", "ভাষা আন্দোলন, ছয় দফা, ১৯৭১-এর মুক্তিযুদ্ধ, বিশ্বযুদ্ধ ও স্নায়ুযুদ্ধ পরবর্তী বিশ্ব।"),

        // ৫. ইসলামের ইতিহাস ও সংস্কৃতি (Islamic History & Culture)
        honoursPdf("pdf_ihc_101", "islamic_history", "ইসলামের ইতিহাস ও সংস্কৃতি", 1, "প্রাক-ইসলামী আরব ও মহানবী (সা.)-এর যুগ", "211601", "আইয়ামে জাহেলিয়া ও মদীনা সনদ — ১ম বর্ষ গাইড", "Book PDFs", "প্রাক-ইসলামী সমাজ, নবুওয়াত, হিজরত, মদীনা সনদ ও বিদায় হজের ভাষণ।"),
        honoursPdf("pdf_ihc_201", "islamic_history", "ইসলামের ইতিহাস ও সংস্কৃতি", 2, "খোলাফায়ে রাশেদীন ও উমাইয়া খিলাফত", "221601", "রাশেদীন ও উমাইয়া শাসনব্যবস্থা — ২য় বর্ষ নোট", "Class Notes", "চার খলিফার শাসনকাল, প্রশাসনিক সংস্কার ও উমাইয়া যুগের জ্ঞান-বিজ্ঞান চর্চা।"),
        honoursPdf("pdf_ihc_301", "islamic_history", "ইসলামের ইতিহাস ও সংস্কৃতি", 3, "আব্বাসীয় খিলাফত ও মুসলিম স্পেন", "231601", "আব্বাসীয় স্বর্ণযুগ ও আন্দালুসিয়া সভ্যতা — ৩য় বর্ষ শিট", "Lecture Sheets", "বায়তুল হিকমাহ, হারুন-অর-রশিদ, মামুন এবং স্পেনে মুসলিম সভ্যতার অবদান।"),
        honoursPdf("pdf_ihc_401", "islamic_history", "ইসলামের ইতিহাস ও সংস্কৃতি", 4, "মুসলিম স্থাপত্য ও বাংলায় মুসলিম শাসন", "241601", "ইসলামী শিল্পকলা ও বাংলার সুলতানি-মুঘল সংস্কৃতি সাজেশন", "Suggestions", "ষাটগম্বুজ মসজিদ, তাজমহল, ইখতিয়ার উদ্দিন বখতিয়ার খলজি ও বাংলা সংস্কৃতি।"),

        // ৬. দর্শন (Philosophy)
        honoursPdf("pdf_phi_101", "philosophy", "দর্শন", 1, "সাধারণ দর্শন ও যুক্তিবিদ্যা (Logic)", "211701", "দর্শনের মূল সমস্যা ও অবরোহ-আরোহ যুক্তিবিদ্যা গাইড", "Book PDFs", "জ্ঞানবিদ্যা, অধিবিদ্যা, পদের ব্যাপ্ততা, সহানুমান ও প্রতীকী যুক্তিবিদ্যার ভিত্তি।"),
        honoursPdf("pdf_phi_201", "philosophy", "দর্শন", 2, "প্রাচীন ও মধ্যযুগীয় পাশ্চাত্য দর্শন", "221701", "থেলিস, সক্রেটিস, প্লেটো, অ্যারিস্টটল ও অ্যাকুইনাস নোট", "Class Notes", "প্রাক-সক্রেটিক দর্শন, ভাববাদ, বাস্তববাদ এবং মধ্যযুগীয় খ্রিষ্টীয় ও মুসলিম দর্শন।"),
        honoursPdf("pdf_phi_301", "philosophy", "দর্শন", 3, "আধুনিক পাশ্চাত্য দর্শন ও নীতিবিদ্যা", "231701", "বুদ্ধিবাদ, অভিজ্ঞতাবাদ ও কান্টের বিচারবাদ — ৩য় বর্ষ", "Lecture Sheets", "দেকার্ত, স্পিনোজা, লক, বার্কলে, হিউম, কান্ট ও নৈতিক মানদণ্ড তত্ত্ব।"),
        honoursPdf("pdf_phi_401", "philosophy", "দর্শন", 4, "বাঙালি দর্শন ও সমকালীন দর্শন", "241701", "বাঙালির দর্শন, অস্তিত্ববাদ ও বিশ্লেষণী দর্শন সাজেশন", "Suggestions", "লালন, রবীন্দ্রনাথ, আরজ আলী মাতুব্বর, সার্ত্রে ও রাসেলের দর্শন।"),

        // ৭. সমাজবিজ্ঞান (Sociology)
        honoursPdf("pdf_soc_101", "sociology", "সমাজবিজ্ঞান", 1, "প্রারম্ভিক সমাজবিজ্ঞান ও সামাজিক ইতিহাস", "212001", "সমাজবিজ্ঞান পরিচিতি, সংস্কৃতি ও সামাজিকীকরণ গাইড", "Book PDFs", "সমাজবিজ্ঞানের উদ্ভব, সমাজ, সংস্কৃতি, সামাজিক স্তরবিন্যাস ও প্রতিষ্ঠান।"),
        honoursPdf("pdf_soc_201", "sociology", "সমাজবিজ্ঞান", 2, "ধ্রুপদী সামাজিক তত্ত্ব ও বাংলাদেশের সমাজ", "222001", "কোম্ত, ডুর্খেইম, মার্ক্স ও ওয়েবার — ২য় বর্ষ নোট", "Class Notes", "দৃশ্যবাদ, আত্মহত্যা তত্ত্ব, শ্রেণিসংগ্রাম, আমলাতন্ত্র ও বাংলাদেশের গ্রামীণ সমাজ।"),
        honoursPdf("pdf_soc_301", "sociology", "সমাজবিজ্ঞান", 3, "সামাজিক গবেষণা পদ্ধতি ও পরিসংখ্যান", "232001", "গবেষণা ডিজাইন, সার্ভে ও ডাটা বিশ্লেষণ শিট", "Lecture Sheets", "গুণগত ও পরিমাণগত গবেষণা, জরিপ পদ্ধতি, কোরিলেশন ও কাই-স্কয়ার টেস্ট।"),
        honoursPdf("pdf_soc_401", "sociology", "সমাজবিজ্ঞান", 4, "সমসাময়িক সমাজতাত্ত্বিক তত্ত্ব ও উন্নয়ন", "242001", "আধুনিক সামাজিক তত্ত্ব, নগরায়ণ ও জেন্ডার স্টাডিজ সাজেশন", "Suggestions", "পারসন্স, মার্টন, ফুকো, বিশ্বায়ন ও বাংলাদেশের সামাজিক উন্নয়ন।"),

        // ৮. সমাজকর্ম (Social Work)
        honoursPdf("pdf_swk_101", "social_work", "সমাজকর্ম", 1, "সমাজকর্ম পরিচিতি ও মানবীয় আচরণ", "212101", "সমাজকর্মের ইতিহাস, দর্শন ও নীতিমালা — ১ম বর্ষ গাইড", "Book PDFs", "পেশাদার সমাজকর্ম, সমাজকল্যাণ বনাম সমাজকর্ম ও মানব আচরণের মনস্তাত্ত্বিক ভিত্তি।"),
        honoursPdf("pdf_swk_201", "social_work", "সমাজকর্ম", 2, "সামাজিক নীতি, পরিকল্পনা ও সমস্যা", "222101", "বাংলাদেশের সামাজিক সমস্যা ও সমাজকল্যাণ নীতি নোট", "Class Notes", "দারিদ্র্য, বেকারত্ব, জনসংখ্যা সমস্যা, সামাজিক নিরাপত্তা ও উন্নয়ন পরিকল্পনা।"),
        honoursPdf("pdf_swk_301", "social_work", "সমাজকর্ম", 3, "ব্যক্তি, দলীয় ও সমষ্টি সমাজকর্ম পদ্ধতি", "232101", "Case Work, Group Work ও Community Organization শিট", "Lecture Sheets", "মৌলিক সমাজকর্ম পদ্ধতির ধাপসমূহ, র‍্যাপো স্থাপন ও ইন্টারভেনশন কৌশল।"),
        honoursPdf("pdf_swk_401", "social_work", "সমাজকর্ম", 4, "সমাজকল্যাণ প্রশাসন ও ফিল্ড ওয়ার্ক গাইড", "242101", "সামাজিক গবেষণা, ফিল্ড প্র্যাকটিকাম ও রিপোর্ট রাইটিং সাজেশন", "Exam Preparation PDFs", "ব্যবহারিক ফিল্ড ওয়ার্ক নির্দেশিকা, গবেষণা প্রস্তাবনা ও মৌখিক পরীক্ষার প্রস্তুতি।"),

        // ৯. অর্থনীতি (Economics)
        honoursPdf("pdf_eco_101", "economics", "অর্থনীতি", 1, "ব্যষ্টিক অর্থনীতি (Microeconomics) ও মৌলিক গণিত", "212201", "চাহিদা, জোগান, ভোক্তা আচরণ ও বাজার — ১ম বর্ষ গাইড", "Book PDFs", "উপযোগ তত্ত্ব, নিরপেক্ষ রেখা, উৎপাদন অপেক্ষক, পূর্ণ প্রতিযোগিতা ও একচেটিয়া বাজার।"),
        honoursPdf("pdf_eco_201", "economics", "অর্থনীতি", 2, "সামষ্টিক অর্থনীতি (Macroeconomics) ও পরিসংখ্যান", "222201", "জাতীয় আয়, IS-LM মডেল, মুদ্রাস্ফীতি ও কর্মসংস্থান নোট", "Class Notes", "GDP/GNP হিসাব, কেইনসীয় তত্ত্ব, মুদ্রানীতি, রাজস্বনীতি ও ফ্লিপস কার্ভ।"),
        honoursPdf("pdf_eco_301", "economics", "অর্থনীতি", 3, "উন্নয়ন অর্থনীতি ও আন্তর্জাতিক বাণিজ্য", "232201", "অর্থনৈতিক প্রবৃদ্ধি, দারিদ্র্য ও আন্তর্জাতিক বাণিজ্য শিট", "Lecture Sheets", "রিকার্ডো ও হেকশার-ওহলিন বাণিজ্য তত্ত্ব, বিনিময় হার, IMF ও বিশ্বব্যাংক।"),
        honoursPdf("pdf_eco_401", "economics", "অর্থনীতি", 4, "বাংলাদেশের অর্থনীতি ও অর্থমিতি (Econometrics)", "242201", "বাংলাদেশের কৃষি, শিল্প, বাজেট ও ইকোনমেট্রিক্স সাজেশন", "Suggestions", "বাজেট বিশ্লেষণ, রেমিট্যান্স, পোশাক শিল্প, রিগ্রেশন মডেল ও প্রকল্প মূল্যায়ন।"),

        // ১০. মনোবিজ্ঞান (Psychology)
        honoursPdf("pdf_psy_101", "psychology", "মনোবিজ্ঞান", 1, "সাধারণ মনোবিজ্ঞান ও পরীক্ষণ মনোবিজ্ঞান", "213401", "সংবেদন, প্রত্যক্ষণ, শিখন, স্মৃতি ও প্রেষণা — ১ম বর্ষ", "Book PDFs", "মনোবিজ্ঞানের পদ্ধতি, স্নায়ুতন্ত্র, প্যাভলভ ও স্কিনারের শিখন তত্ত্ব।"),
        honoursPdf("pdf_psy_201", "psychology", "মনোবিজ্ঞান", 2, "বিকাশ মনোবিজ্ঞান ও সামাজিক মনোবিজ্ঞান", "223401", "শিশু-কিশোর বিকাশ, মনোভাব ও নেতৃত্ব — ২য় বর্ষ নোট", "Class Notes", "পিয়াজেঁ ও এরিকসনের বিকাশ স্তর, সামাজিক প্রভাব ও দলীয় আচরণ।"),
        honoursPdf("pdf_psy_301", "psychology", "মনোবিজ্ঞান", 3, "অস্বাভাবিক মনোবিজ্ঞান ও মনস্তাত্ত্বিক পরিসংখ্যান", "233401", "Abnormal Psychology & Research Methods — Year 3", "Lecture Sheets", "উদ্বেগ, বিষণ্ণতা, সিজোফ্রেনিয়া, মানসিক চাপ ও মনস্তাত্ত্বিক পরীক্ষা।"),
        honoursPdf("pdf_psy_401", "psychology", "মনোবিজ্ঞান", 4, "ক্লিনিক্যাল ও কাউন্সেলিং মনোবিজ্ঞান", "243401", "সাইকোথেরাপি, কাউন্সেলিং ও অর্গানাইজেশনাল সাইকোলজি সাজেশন", "Suggestions", "CBT, মনঃসমীক্ষণ, মানসিক স্বাস্থ্য মূল্যায়ন ও কর্মক্ষেত্রের মনোবিজ্ঞান।"),

        // ১১. ভূগোল ও পরিবেশ (Geography & Environment)
        honoursPdf("pdf_geo_101", "geography", "ভূগোল ও পরিবেশ", 1, "প্রাকৃতিক ভূগোল ও মানচিত্রাঙ্কন", "213201", "ভূমিরূপবিদ্যা, শিলা, প্লেট টেকটোনিক ও মানচিত্র — ১ম বর্ষ", "Book PDFs", "নদী, হিমবাহ ও বায়ুর কার্যাবলি, মানচিত্র অভিক্ষেপ ও স্কেল অঙ্কন।"),
        honoursPdf("pdf_geo_201", "geography", "ভূগোল ও পরিবেশ", 2, "মানবীয় ভূগোল ও জলবায়ুবিজ্ঞান", "223201", "জনসংখ্যা ভূগোল, বসতি ও আবহাওয়া-জলবায়ু নোট", "Class Notes", "বায়ুচাপ বলয়, মৌসুমি জলবায়ু, গ্রিনহাউস প্রভাব ও জনমিতিক রূপান্তর।"),
        honoursPdf("pdf_geo_301", "geography", "ভূগোল ও পরিবেশ", 3, "বাংলাদেশের ভূগোল ও পরিবেশ ব্যবস্থাপনা", "233201", "বাংলাদেশের নদ-নদী, ভূ-প্রকৃতি ও পরিবেশ আইন শিট", "Lecture Sheets", "বঙ্গীয় ব-দ্বীপ, সুন্দরবন, কৃষি-জলবায়ু অঞ্চল ও জলবায়ু পরিবর্তন ঝুঁকি।"),
        honoursPdf("pdf_geo_401", "geography", "ভূগোল ও পরিবেশ", 4, "GIS, রিমোট সেন্সিং ও দুর্যোগ ব্যবস্থাপনা", "243201", "জিআইএস (GIS), উপগ্রহ চিত্র ও দুর্যোগ প্রশমন গাইড", "Exam Preparation PDFs", "GIS ডাটা মডেল, রিমোট সেন্সিং সেন্সর, ঘূর্ণিঝড় ও বন্যা ব্যবস্থাপনা।"),

        // ১২. গণিত (Mathematics)
        honoursPdf("pdf_mat_101", "mathematics", "গণিত", 1, "Calculus-I, Linear Algebra & Analytic Geometry", "213701", "অনার্স ১ম বর্ষ গণিত: ডিফারেনশিয়াল ক্যালকুলাস ও ম্যাট্রিক্স", "Book PDFs", "Limits, continuity, derivatives, Taylor series, matrices, determinants & conics."),
        honoursPdf("pdf_mat_201", "mathematics", "গণিত", 2, "Calculus-II & Ordinary Differential Equations", "223701", "অনার্স ২য় বর্ষ গণিত: ভেক্টর ক্যালকুলাস ও ODE নোট", "Class Notes", "Multiple integrals, Green/Stokes/Gauss theorems, 1st & higher order ODEs."),
        honoursPdf("pdf_mat_301", "mathematics", "গণিত", 3, "Real Analysis, Complex Analysis & Numerical Analysis", "233701", "অনার্স ৩য় বর্ষ গণিত: রিয়্যাল, কমপ্লেক্স ও নিউমেরিক্যাল অ্যানালাইসিস", "Lecture Sheets", "Metric spaces, Cauchy-Riemann equations, residue theorem, Newton-Raphson & Runge-Kutta."),
        honoursPdf("pdf_mat_401", "mathematics", "গণিত", 4, "Partial Differential Equations, Topology & Tensor", "243701", "অনার্স ৪র্থ বর্ষ গণিত: PDE, ফ্লুইড মেকানিক্স ও টপোলজি সাজেশন", "Suggestions", "Laplace, wave & heat equations, topological spaces, tensor calculus & applied math."),

        // ১৩. পদার্থবিজ্ঞান (Physics)
        honoursPdf("pdf_phy_101", "physics", "পদার্থবিজ্ঞান", 1, "Mechanics, Properties of Matter & Waves", "212701", "বলবিদ্যা, তরঙ্গ ও তাপগতিবিদ্যা — ১ম বর্ষ গাইড", "Book PDFs", "Newtonian mechanics, moment of inertia, elasticity, SHM, wave equation & thermodynamics."),
        honoursPdf("pdf_phy_201", "physics", "পদার্থবিজ্ঞান", 2, "Electricity, Magnetism & Optics", "222701", "তড়িৎচুম্বকত্ব ও আলোকবিজ্ঞান — ২য় বর্ষ নোট", "Class Notes", "Gauss's law, Maxwell's equations, interference, diffraction & polarization of light."),
        honoursPdf("pdf_phy_301", "physics", "পদার্থবিজ্ঞান", 3, "Quantum Mechanics-I & Electronics", "232701", "কোয়ান্টাম বলবিদ্যা-১ ও ইলেকট্রনিক্স — ৩য় বর্ষ শিট", "Lecture Sheets", "Schrodinger wave equation, potential well, harmonic oscillator, diodes, BJT & Op-Amp."),
        honoursPdf("pdf_phy_401", "physics", "পদার্থবিজ্ঞান", 4, "Nuclear Physics, Solid State & Quantum Mechanics-II", "242701", "নিউক্লিয়ার, সলিড স্টেট ও রিলেটিভিটি — ৪র্থ বর্ষ সাজেশন", "Suggestions", "Crystal structure, band theory, nuclear decay, fission/fusion & special relativity."),

        // ১৪. রসায়ন (Chemistry)
        honoursPdf("pdf_che_101", "chemistry", "রসায়ন", 1, "Inorganic Chemistry-I & Physical Chemistry-I", "212801", "অজৈব ও ভৌত রসায়ন-১ — ১ম বর্ষ স্টাডি গাইড", "Book PDFs", "Atomic structure, chemical bonding, VSEPR, gases, chemical kinetics & equilibrium."),
        honoursPdf("pdf_che_201", "chemistry", "রসায়ন", 2, "Organic Chemistry-I & Analytical Chemistry", "222801", "জৈব রসায়ন-১ ও বিশ্লেষণী রসায়ন — ২য় বর্ষ নোট", "Class Notes", "Reaction mechanisms (SN1, SN2, E1, E2), aromaticity, titrimetry & chromatography."),
        honoursPdf("pdf_che_301", "chemistry", "রসায়ন", 3, "Physical Chemistry-II & Coordination Chemistry", "232801", "তড়িৎ রসায়ন, তাপগতিবিদ্যা ও কো-অর্ডিনেশন রসায়ন শিট", "Lecture Sheets", "Electrochemistry, phase rule, crystal field theory (CFT) & transition metals."),
        honoursPdf("pdf_che_401", "chemistry", "রসায়ন", 4, "Spectroscopy, Stereochemistry & Industrial Chemistry", "242801", "স্পেকট্রোস্কোপি ও স্টেরিওকেমিস্ট্রি — ৪র্থ বর্ষ সাজেশন", "Suggestions", "UV-Vis, IR, NMR spectroscopy, chirality, polymers & industrial chemical processes."),

        // ১৫. উদ্ভিদবিজ্ঞান (Botany)
        honoursPdf("pdf_bot_101", "botany", "উদ্ভিদবিজ্ঞান", 1, "Microbiology, Mycology & Phycology", "213001", "অণুজীববিজ্ঞান, ছত্রাক ও শৈবালতত্ত্ব — ১ম বর্ষ গাইড", "Book PDFs", "Virus, bacteria, fungi life cycles, algal classification & plant pathology basics."),
        honoursPdf("pdf_bot_201", "botany", "উদ্ভিদবিজ্ঞান", 2, "Bryophyta, Pteridophyta & Gymnosperms", "223001", "ব্রায়োফাইটা, টেরিডোফাইটা ও নগ্নবীজী উদ্ভিদ নোট", "Class Notes", "Riccia, Marchantia, Pteris, Cycas, Pinus morphology and evolutionary trends."),
        honoursPdf("pdf_bot_301", "botany", "উদ্ভিদবিজ্ঞান", 3, "Plant Physiology, Anatomy & Genetics", "233001", "উদ্ভিদ শরীরতত্ত্ব, টিস্যু ও জিনতত্ত্ব — ৩য় বর্ষ শিট", "Lecture Sheets", "Photosynthesis, respiration, transpiration, Mendelism, linkage & cytogenetics."),
        honoursPdf("pdf_bot_401", "botany", "উদ্ভিদবিজ্ঞান", 4, "Plant Ecology, Biotechnology & Biostatistics", "243001", "উদ্ভিদ বাস্তুসংস্থান ও জৈবপ্রযুক্তি — ৪র্থ বর্ষ সাজেশন", "Suggestions", "Tissue culture, recombinant DNA technology, ecosystems of Bangladesh & biostatistics."),

        // ১৬. প্রাণিবিজ্ঞান (Zoology)
        honoursPdf("pdf_zoo_101", "zoology", "প্রাণিবিজ্ঞান", 1, "Non-Chordata & Cell Biology", "213101", "অমেরুদণ্ডী প্রাণিবিজ্ঞান ও কোষবিদ্যা — ১ম বর্ষ গাইড", "Book PDFs", "Protozoa to Echinodermata classification, cell organelles, mitosis & meiosis."),
        honoursPdf("pdf_zoo_201", "zoology", "প্রাণিবিজ্ঞান", 2, "Chordata, Comparative Anatomy & Embryology", "223101", "কর্ডাটা, তুলনামূলক অঙ্গসংস্থান ও ভ্রূণতত্ত্ব নোট", "Class Notes", "Fishes, amphibians, reptiles, birds, mammals comparative anatomy & embryonic development."),
        honoursPdf("pdf_zoo_301", "zoology", "প্রাণিবিজ্ঞান", 3, "Genetics, Evolution & Animal Physiology", "233101", "জিনতত্ত্ব, বিবর্তন ও প্রাণীর শরীরতত্ত্ব — ৩য় বর্ষ শিট", "Lecture Sheets", "DNA replication, Darwinism, Neo-Darwinism, digestion, circulation & neurophysiology."),
        honoursPdf("pdf_zoo_401", "zoology", "প্রাণিবিজ্ঞান", 4, "Entomology, Fisheries & Wildlife Conservation", "243101", "কীটতত্ত্ব, মৎস্যবিজ্ঞান ও বন্যপ্রাণী সংরক্ষণ সাজেশন", "Suggestions", "Integrated pest management, aquaculture in Bangladesh & wildlife conservation."),

        // ১৭. হিসাববিজ্ঞান (Accounting)
        honoursPdf("pdf_acc_101", "accounting", "হিসাববিজ্ঞান", 1, "Principles of Accounting & Business Mathematics", "212501", "অনার্স ১ম বর্ষ হিসাববিজ্ঞান: মৌলিক হিসাব ও ব্যবসায় গণিত", "Book PDFs", "Accounting cycle, adjusting entries, financial statements, matrices & time value of money."),
        honoursPdf("pdf_acc_201", "accounting", "হিসাববিজ্ঞান", 2, "Intermediate Accounting & Business Communication", "222501", "ইন্টারমিডিয়েট অ্যাকাউন্টিং ও কর্পোরেট রিপোর্টিং নোট", "Class Notes", "Receivables, inventory valuation, depreciation, intangible assets & cash flow statements."),
        honoursPdf("pdf_acc_301", "accounting", "হিসাববিজ্ঞান", 3, "Cost Accounting, Auditing & Taxation", "232501", "কস্ট অ্যাকাউন্টিং, অডিটিং ও বাংলাদেশ আয়কর গাইড", "Lecture Sheets", "Job/process costing, variance analysis, audit report & Bangladesh Income Tax computation."),
        honoursPdf("pdf_acc_401", "accounting", "হিসাববিজ্ঞান", 4, "Advanced Accounting & Financial Statement Analysis", "242501", "অ্যাডভান্সড অ্যাকাউন্টিং ও আইএফআরএস (IFRS) সাজেশন", "Suggestions", "Consolidation, partnership liquidation, IAS/IFRS standards & ratio analysis."),

        // ১৮. ব্যবস্থাপনা (Management)
        honoursPdf("pdf_mgt_101", "management", "ব্যবস্থাপনা", 1, "Principles of Management & Introduction to Business", "212601", "ব্যবস্থাপনার নীতিমালা ও ব্যবসায় পরিচিতি — ১ম বর্ষ", "Book PDFs", "Planning, organizing, staffing, leading, controlling & business ownership forms."),
        honoursPdf("pdf_mgt_201", "management", "ব্যবস্থাপনা", 2, "Organizational Behavior & Business Statistics", "222601", "সাংগঠনিক আচরণ ও ব্যবসায় পরিসংখ্যান — ২য় বর্ষ নোট", "Class Notes", "Motivation theories, leadership styles, group dynamics, probability & hypothesis testing."),
        honoursPdf("pdf_mgt_301", "management", "ব্যবস্থাপনা", 3, "Human Resource Management (HRM) & Operations", "232601", "মানবসম্পদ ব্যবস্থাপনা ও উৎপাদন ব্যবস্থাপনা শিট", "Lecture Sheets", "Recruitment, training, performance appraisal, compensation & quality control."),
        honoursPdf("pdf_mgt_401", "management", "ব্যবস্থাপনা", 4, "Strategic Management & Project Management", "242601", "কৌশলগত ব্যবস্থাপনা ও প্রজেক্ট ম্যানেজমেন্ট সাজেশন", "Suggestions", "SWOT analysis, Porter's Five Forces, corporate strategy & PERT/CPM network."),

        // ১৯. মার্কেটিং (Marketing)
        honoursPdf("pdf_mkt_101", "marketing", "মার্কেটিং", 1, "Principles of Marketing & Microeconomics", "212301", "বিপণনের নীতিমালা (4Ps) ও অর্থনীতি — ১ম বর্ষ গাইড", "Book PDFs", "Segmentation, targeting, positioning, product life cycle, pricing & promotion mix."),
        honoursPdf("pdf_mkt_201", "marketing", "মার্কেটিং", 2, "Consumer Behavior & Business Communication", "222301", "ভোক্তা আচরণ ও ব্যবসায়িক যোগাযোগ — ২য় বর্ষ নোট", "Class Notes", "Consumer decision-making process, perception, attitude & IMC strategy."),
        honoursPdf("pdf_mkt_301", "marketing", "মার্কেটিং", 3, "Marketing Research & Brand Management", "232301", "মার্কেটিং রিসার্চ ও ব্র্যান্ড ম্যানেজমেন্ট শিট", "Lecture Sheets", "Survey design, SPSS data interpretation, brand equity & positioning models."),
        honoursPdf("pdf_mkt_401", "marketing", "মার্কেটিং", 4, "International & Digital Marketing Strategy", "242301", "আন্তর্জাতিক ও ডিজিটাল মার্কেটিং — ৪র্থ বর্ষ সাজেশন", "Suggestions", "Export marketing, global entry modes, SEO, social media & e-commerce analytics."),

        // ২০. ফিন্যান্স ও ব্যাংকিং (Finance & Banking)
        honoursPdf("pdf_fin_101", "finance_banking", "ফিন্যান্স ও ব্যাংকিং", 1, "Principles of Finance & Accounting", "212401", "অর্থায়নের নীতিমালা ও টাইম ভ্যালু অব মানি গাইড", "Book PDFs", "Time value of money, bond & stock valuation, risk-return & financial markets."),
        honoursPdf("pdf_fin_201", "finance_banking", "ফিন্যান্স ও ব্যাংকিং", 2, "Commercial Banking & Insurance", "222401", "বাণিজ্যিক ব্যাংক ব্যবস্থাপনা ও বিমা — ২য় বর্ষ নোট", "Class Notes", "Credit creation, Bangladesh Bank regulation, CAMELS rating, life & general insurance."),
        honoursPdf("pdf_fin_301", "finance_banking", "ফিন্যান্স ও ব্যাংকিং", 3, "Corporate Finance & Capital Budgeting", "232401", "কর্পোরেট ফিন্যান্স, NPV/IRR ও মূলধন ব্যয় শিট", "Lecture Sheets", "Capital budgeting techniques, WACC, leverage, capital structure & dividend policy."),
        honoursPdf("pdf_fin_401", "finance_banking", "ফিন্যান্স ও ব্যাংকিং", 4, "Investment Analysis, Portfolio & Risk Management", "242401", "পোর্টফোলিও ম্যানেজমেন্ট ও ঝুঁকি বিশ্লেষণ সাজেশন", "Suggestions", "CAPM, Markowitz portfolio theory, derivatives & Dhaka Stock Exchange analysis."),

        // ২১. পরিসংখ্যান (Statistics)
        honoursPdf("pdf_sta_101", "statistics", "পরিসংখ্যান", 1, "Introductory Statistics & Probability", "213601", "মৌলিক পরিসংখ্যান ও সম্ভাব্যতা — ১ম বর্ষ গাইড", "Book PDFs", "Central tendency, dispersion, skewness, Bayes' theorem & random variables."),
        honoursPdf("pdf_sta_201", "statistics", "পরিসংখ্যান", 2, "Probability Distributions & Sampling", "223601", "বিন্যাস তত্ত্ব ও স্যাম্পলিং কৌশল — ২য় বর্ষ নোট", "Class Notes", "Binomial, Poisson, Normal distributions, SRS, stratified & cluster sampling."),
        honoursPdf("pdf_sta_301", "statistics", "পরিসংখ্যান", 3, "Statistical Inference & Regression Analysis", "233601", "এস্টিমেশন, হাইপোথিসিস টেস্টিং ও রিগ্রেশন শিট", "Lecture Sheets", "MLE, t/F/chi-square tests, ANOVA & multiple linear regression analysis."),
        honoursPdf("pdf_sta_401", "statistics", "পরিসংখ্যান", 4, "Econometrics, Multivariate & Experimental Design", "243601", "মাল্টিভ্যারিয়েট অ্যানালাইসিস ও ডিজাইন অব এক্সপেরিমেন্ট সাজেশন", "Suggestions", "CRD, RBD, Latin square design, PCA, factor analysis & time series forecasting."),

        // ২২. অন্যান্য উপলব্ধ অনার্স বিভাগ (Other Honours Departments - Common Compulsory & Exam Prep)
        honoursPdf("pdf_oth_101", "other_honours", "অন্যান্য উপলব্ধ অনার্স বিভাগ", 1, "স্বাধীন বাংলাদেশের অভ্যুদয়ের ইতিহাস (আবশ্যিক কোর্স)", "211501", "স্বাধীন বাংলাদেশের অভ্যুদয়ের ইতিহাস — সকল বিভাগের ১ম বর্ষ গাইড", "Book PDFs", "সকল অনার্স ১ম বর্ষের আবশ্যিক কোর্স: ১৯৪৭–১৯৭১ মুক্তিযুদ্ধ ও বাংলাদেশের অভ্যুদয়।"),
        honoursPdf("pdf_oth_201", "other_honours", "অন্যান্য উপলব্ধ অনার্স বিভাগ", 2, "English (Compulsory) — All Honours Departments", "221109", "জাতীয় বিশ্ববিদ্যালয় অনার্স ২য় বর্ষ Compulsory English নোট", "Exam Preparation PDFs", "Reading comprehension, grammar, paragraph, essay, report & letter writing complete guide."),
        honoursPdf("pdf_oth_301", "other_honours", "অন্যান্য উপলব্ধ অনার্স বিভাগ", 3, "বাংলা ভাষা ও সাহিত্য (ঐচ্ছিক/কমন গাইড)", "231009", "অনার্স ৩য় বর্ষ প্রশ্নব্যাংক ও উত্তর লেখার কৌশল PDF", "Previous-Year Question Papers", "ক, খ ও গ বিভাগের নম্বর বণ্টন, ভালো ফলাফলের কৌশল ও পূর্ববর্তী প্রশ্ন কাঠামো।"),
        honoursPdf("pdf_oth_401", "other_honours", "অন্যান্য উপলব্ধ অনার্স বিভাগ", 4, "অনার্স ৪র্থ বর্ষ প্রজেক্ট, ভাইভা ও চূড়ান্ত প্রস্তুতি", "240001", "অনার্স ৪র্থ বর্ষ মৌখিক (Viva-Voce) ও চূড়ান্ত পরীক্ষার গাইড", "Reference Materials", "রিসার্চ মনোগ্রাফ/টার্ম পেপার, ভাইভা প্রস্তুতি ও শেষ মুহূর্তের রিভিশন চেকলিস্ট।")
    )

    val starterAnnouncements: List<Announcement> = listOf(
        Announcement(
            id = "ann_welcome",
            title = "Honours Solution-এ স্বাগতম!",
            body = "জাতীয় বিশ্ববিদ্যালয়ের রাষ্ট্রবিজ্ঞান অনার্স ১ম–৪র্থ বর্ষের সমাধান, AI প্রশ্ন সমাধান এবং সকল বিভাগের PDF লাইব্রেরি এখন হাতের মুঠোয়। এডমিন প্যানেল থেকে যাচাইকৃত সিলেবাস ও PDF আপলোড করুন।",
            tag = "Welcome",
            pinned = true
        )
    )
}
