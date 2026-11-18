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

    val starterPdfs: List<PdfResource> = listOf(
        PdfResource(
            id = "pdf_starter_pol1",
            departmentId = "political_science",
            departmentName = "রাষ্ট্রবিজ্ঞান",
            yearNumber = 1,
            subjectName = "রাজনৈতিক তত্ত্ব পরিচিতি",
            courseCode = "PS-101",
            title = "রাষ্ট্রবিজ্ঞান ১ম বর্ষ স্টাডি গাইড ও প্রশ্ন কাঠামো",
            category = "Suggestions",
            description = "Offline-ready introductory Political Science Honours study guide (replaceable via Admin PDF Upload).",
            localFileName = "starter_pol_y1_guide.pdf",
            pageCount = 1,
            fileSizeBytes = 12400L,
            uploadedBy = "Admin"
        ),
        PdfResource(
            id = "pdf_starter_pol2",
            departmentId = "political_science",
            departmentName = "রাষ্ট্রবিজ্ঞান",
            yearNumber = 2,
            subjectName = "বাংলাদেশের সরকার ও রাজনীতি",
            courseCode = "PS-201",
            title = "বাংলাদেশের সংবিধান ও শাসনব্যবস্থা সংক্ষিপ্ত নোট",
            category = "Class Notes",
            description = "Offline-ready starter note on 1972 Constitution of Bangladesh.",
            localFileName = "starter_pol_y2_constitution.pdf",
            pageCount = 1,
            fileSizeBytes = 11800L,
            uploadedBy = "Admin"
        )
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
