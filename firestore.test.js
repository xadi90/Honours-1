const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_student_2";
const ADMIN_EMAIL = "xadiahmed7@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("departments").where("status", "==", "published").get());
  await assertFails(unauthDb.collection("pdf_library").where("status", "==", "published").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      selectedDepartmentId: "political_science",
      selectedYear: 1,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Student profile: owner can create & update; cross-user read/write fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayesha Rahman",
      selectedDepartmentId: "political_science",
      selectedDepartmentName: "রাষ্ট্রবিজ্ঞান",
      selectedYear: 2,
      languagePreference: "bn_en",
      savedAnswersCount: 0,
      bookmarksCount: 0,
      testsCompletedCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      selectedYear: 3,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user access denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      selectedYear: 4,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("User-scoped AI answers & bookmarks: owner succeeds, cross-user fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_answers").doc("ans_1").set({
      userId: ALICE_UID,
      question: "রাষ্ট্রবিজ্ঞানের সংজ্ঞা দাও।",
      answerLength: "সংক্ষিপ্ত উত্তর",
      yearLabel: "অনার্স ১ম বর্ষ",
      subjectName: "রাজনৈতিক তত্ত্ব পরিচিতি",
      language: "bn",
      prashno: "রাষ্ট্রবিজ্ঞানের সংজ্ঞা দাও।",
      bhumika: "রাষ্ট্রবিজ্ঞান সামাজিক বিজ্ঞানের একটি গুরুত্বপূর্ণ শাখা।",
      mulAlochona: "অ্যারিস্টটল রাষ্ট্রবিজ্ঞানকে মাস্টার সায়েন্স বলেছেন।",
      importantPoints: ["রাষ্ট্রের উৎপত্তি ও প্রকৃতি", "সরকার ও শাসনব্যবস্থা"],
      examples: "বাংলাদেশের সংবিধান ও সংসদীয় ব্যবস্থা।",
      uposhonghar: "আধুনিক রাষ্ট্রব্যবস্থা বুঝতে রাষ্ট্রবিজ্ঞান অপরিহার্য।",
      hasImageInput: false,
      bookmarked: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_answers").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("ai_answers").get()
  );
});

test("Shared academic collections: students read published only, cannot write; admin can write", async () => {
  const studentDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "student@example.com",
    email_verified: true,
  }).firestore();
  const adminDb = testEnv.authenticatedContext("admin_1", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const unverifiedSpoofAdminDb = testEnv.authenticatedContext("spoof_1", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  const now = new Date(Date.now() - 1000);

  // Student and unverified email spoof cannot create department or PDF
  await assertFails(
    studentDb.collection("departments").doc("pol_sci").set({
      nameBn: "রাষ্ট্রবিজ্ঞান",
      nameEn: "Political Science",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    unverifiedSpoofAdminDb.collection("departments").doc("pol_sci").set({
      nameBn: "রাষ্ট্রবিজ্ঞান",
      nameEn: "Political Science",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Verified admin creates published & draft PDFs
  await assertSucceeds(
    adminDb.collection("pdf_library").doc("pdf_pub").set({
      departmentId: "political_science",
      departmentName: "রাষ্ট্রবিজ্ঞান",
      yearNumber: 1,
      subjectName: "রাজনৈতিক তত্ত্ব পরিচিতি",
      courseCode: "211901",
      title: "অনার্স ১ম বর্ষ সাজেশন ২০২৫",
      category: "Suggestions",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("pdf_library").doc("pdf_draft").set({
      departmentId: "political_science",
      departmentName: "রাষ্ট্রবিজ্ঞান",
      yearNumber: 1,
      subjectName: "রাজনৈতিক তত্ত্ব পরিচিতি",
      courseCode: "211901",
      title: "খসড়া নোট",
      category: "Class Notes",
      status: "draft",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered published query succeeds for students; unfiltered query fails
  await assertSucceeds(
    studentDb.collection("pdf_library").where("status", "==", "published").get()
  );
  await assertFails(studentDb.collection("pdf_library").get());
  await assertFails(studentDb.collection("pdf_library").doc("pdf_draft").get());
});

test("Admin security settings & user reports isolation", async () => {
  const studentDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const adminDb = testEnv.authenticatedContext("admin_1", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    adminDb.collection("app_settings").doc("admin_security").set({
      pinHash: "dummy_sha256_hash",
      updatedAt: now,
    })
  );
  await assertFails(studentDb.collection("app_settings").doc("admin_security").get());

  await assertSucceeds(
    studentDb.collection("user_reports").doc("rep_1").set({
      userId: ALICE_UID,
      reporterName: "Ayesha",
      targetType: "ai_answer",
      targetId: "ans_1",
      reason: "Inaccurate date",
      status: "open",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    studentDb.collection("user_reports").doc("rep_1").update({
      status: "resolved",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    adminDb.collection("user_reports").doc("rep_1").update({
      status: "resolved",
      adminNote: "Verified and updated",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("AI quota: +1 consumption within 30 free limit, tamper prevention, and purchase approval", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext("admin_1", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  // Student cannot spoof initial credits on profile creation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayesha",
      selectedDepartmentId: "political_science",
      selectedYear: 1,
      freeAiLimit: 999,
      aiUsageCount: 0,
      purchasedAiCredits: 500,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid creation with 30 free limit
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayesha",
      selectedDepartmentId: "political_science",
      selectedYear: 1,
      freeAiLimit: 30,
      aiUsageCount: 0,
      purchasedAiCredits: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid +1 increment succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      aiUsageCount: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Jumping by +2 fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      aiUsageCount: 3,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Student cannot modify own purchasedAiCredits or freeAiLimit
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      purchasedAiCredits: 100,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Simulate user hitting full 30 free limit via security rules bypass
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).update({
      aiUsageCount: 30,
    });
  });

  // 31st request blocked when remaining credits == 0
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      aiUsageCount: 31,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Student submits purchase request (pending only)
  await assertFails(
    aliceDb.collection("ai_purchase_requests").doc("req_bad").set({
      userId: ALICE_UID,
      packageTitle: "Starter Pack",
      creditsAmount: 50,
      priceBdt: 50,
      paymentMethod: "bKash",
      senderNumber: "01711000000",
      transactionId: "TRX12345",
      status: "approved",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("ai_purchase_requests").doc("req_1").set({
      userId: ALICE_UID,
      userName: "Ayesha",
      userEmail: "alice@example.com",
      packageTitle: "Starter Pack (+50)",
      creditsAmount: 50,
      priceBdt: 50,
      paymentMethod: "bKash",
      senderNumber: "01711000000",
      transactionId: "TRX987654",
      status: "pending",
      adminNote: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered own query succeeds; unfiltered list fails for students
  await assertSucceeds(
    aliceDb.collection("ai_purchase_requests").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(aliceDb.collection("ai_purchase_requests").get());
  await assertFails(bobDb.collection("ai_purchase_requests").doc("req_1").get());

  // Student cannot self-approve
  await assertFails(
    aliceDb.collection("ai_purchase_requests").doc("req_1").update({
      status: "approved",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin approves request & grants +50 purchased credits
  await assertSucceeds(
    adminDb.collection("ai_purchase_requests").doc("req_1").update({
      status: "approved",
      adminNote: "Verified bKash Trx",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      purchasedAiCredits: 50,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Double-approval fails
  await assertFails(
    adminDb.collection("ai_purchase_requests").doc("req_1").update({
      status: "approved",
      updatedAt: new Date(Date.now() - 400),
    })
  );

  // Now student can use the 31st AI credit!
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      aiUsageCount: 31,
      updatedAt: new Date(Date.now() - 300),
    })
  );
});

test("Sujat Ali Honours College: admin can arrange sections & settings; students read published only", async () => {
  const studentDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "student@example.com",
    email_verified: true,
  }).firestore();
  const adminDb = testEnv.authenticatedContext("admin_1", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  // Student cannot edit college profile settings or create college sections
  await assertFails(
    studentDb.collection("app_settings").doc("sujat_ali_college").set({
      collegeNameBn: "Hacked College",
      updatedAt: now,
    })
  );
  await assertFails(
    studentDb.collection("college_sections").doc("sec_1").set({
      blockType: "notice",
      title: "Fake Notice",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin updates college profile & creates published + draft sections
  await assertSucceeds(
    adminDb.collection("app_settings").doc("sujat_ali_college").set({
      collegeNameBn: "সুজাত আলী অনার্স কলেজ, পিংনা",
      collegeNameEn: "Sujat Ali Honours College, Pingna",
      heroThemePreset: "emerald_teal",
      layoutMode: "grid",
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("college_sections").doc("sec_pub").set({
      blockType: "notice",
      title: "অনার্স পরীক্ষার ফর্ম ফিলাপ বিজ্ঞপ্তি",
      subtitle: "২০২৫ শিক্ষাবর্ষ",
      body: "নির্ধারিত তারিখের মধ্যে ফর্ম ফিলাপ সম্পন্ন করুন।",
      badgeText: "জরুরি",
      iconKey: "campaign",
      accentStyle: "gold",
      layoutSpan: "full",
      actionLabel: "বিস্তারিত",
      actionUrl: "https://example.com",
      pinned: true,
      sortOrder: 1,
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("college_sections").doc("sec_draft").set({
      blockType: "custom",
      title: "খসড়া রুটিন",
      subtitle: "",
      body: "এখনো চূড়ান্ত হয়নি",
      badgeText: "",
      iconKey: "schedule",
      accentStyle: "primary",
      layoutSpan: "half",
      actionLabel: "",
      actionUrl: "",
      pinned: false,
      sortOrder: 2,
      status: "draft",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin can reorder section (sortOrder update preserving createdAt)
  await assertSucceeds(
    adminDb.collection("college_sections").doc("sec_pub").update({
      sortOrder: 3,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Student reads college profile & published query succeeds; draft & unfiltered list fail
  await assertSucceeds(studentDb.collection("app_settings").doc("sujat_ali_college").get());
  await assertSucceeds(
    studentDb.collection("college_sections").where("status", "==", "published").get()
  );
  await assertFails(studentDb.collection("college_sections").get());
  await assertFails(studentDb.collection("college_sections").doc("sec_draft").get());
  await assertSucceeds(adminDb.collection("college_sections").get());
});

test("User data anti-theft isolation & admin-only usage count listing", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const adminDb = testEnv.authenticatedContext("admin_1", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      selectedDepartmentId: "political_science",
      selectedYear: 1,
      createdAt: now,
      updatedAt: now,
      lastActiveAt: now,
    })
  );

  // Owner can update lastActiveAt heartbeat
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      lastActiveAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Bob cannot list users, read Alice's profile, or write catalog_meta
  await assertFails(bobDb.collection("users").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("bookmarks").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("ai_conversations").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("model_test_results").get());
  await assertFails(
    bobDb.collection("app_settings").doc("catalog_meta").set({
      seeded: true,
      pdfSeedVersion: 2,
    })
  );

  // Verified bootstrapped admin can list users to count total & active users
  const usersSnap = await assertSucceeds(adminDb.collection("users").get());
  if (usersSnap.size !== 1) {
    throw new Error(`Expected 1 user in admin listing, got ${usersSnap.size}`);
  }
});

test("College moderators: scoped solely to Sujat Ali College, denied on /users & academic data; /email_verifications locked", async () => {
  const MOD_EMAIL = "college.mod@example.com";
  const modDb = testEnv.authenticatedContext("mod_uid_1", {
    email: MOD_EMAIL,
    email_verified: true,
  }).firestore();
  const studentDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const adminDb = testEnv.authenticatedContext("admin_1", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  // Student cannot add or list college moderators
  await assertFails(
    studentDb.collection("college_moderators").doc(MOD_EMAIL).set({
      email: MOD_EMAIL,
      displayName: "Unauthorized",
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(studentDb.collection("college_moderators").get());

  // Bootstrapped admin adds an active college moderator
  await assertSucceeds(
    adminDb.collection("college_moderators").doc(MOD_EMAIL).set({
      email: MOD_EMAIL,
      displayName: "Pingna Representative",
      note: "College Coordinator",
      active: true,
      addedByEmail: ADMIN_EMAIL,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Moderator can read own moderator doc, but cannot list all moderators or read other users
  await assertSucceeds(modDb.collection("college_moderators").doc(MOD_EMAIL).get());
  await assertFails(modDb.collection("college_moderators").get());
  await assertFails(studentDb.collection("college_moderators").doc(MOD_EMAIL).get());
  await assertFails(modDb.collection("users").get());
  await assertFails(modDb.collection("users").doc(ALICE_UID).get());

  // Moderator CANNOT write departments, pdf_library, or auth_policy
  await assertFails(
    modDb.collection("departments").doc("pol_sci").set({
      nameBn: "রাষ্ট্রবিজ্ঞান",
      nameEn: "Political Science",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    modDb.collection("app_settings").doc("auth_policy").set({
      requireEmailOtp: false,
      updatedAt: now,
    })
  );

  // Active moderator CAN customize Sujat Ali College banner + upload valid image (data URI or https://)
  await assertSucceeds(
    modDb.collection("app_settings").doc("sujat_ali_college").set({
      collegeNameBn: "সুজাত আলী অনার্স কলেজ, পিংনা",
      heroImageUrl: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ==",
      updatedAt: now,
    })
  );
  await assertSucceeds(
    modDb.collection("college_sections").doc("sec_photo_1").set({
      blockType: "notice",
      title: "ক্যাম্পাস ছবি",
      imageUrl: "https://example.com/campus.jpg",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Invalid image URL (plain http:// or non-image data URI) is rejected even for manager
  await assertFails(
    modDb.collection("college_sections").doc("sec_bad_img").set({
      blockType: "notice",
      title: "Bad Image",
      imageUrl: "http://insecure.example.com/photo.jpg",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Pausing moderator revokes Sujat Ali College write permissions immediately
  await assertSucceeds(
    adminDb.collection("college_moderators").doc(MOD_EMAIL).update({
      active: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    modDb.collection("college_sections").doc("sec_photo_2").set({
      blockType: "notice",
      title: "Paused Mod Attempt",
      status: "published",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bootstrapped admin controls auth_policy (including optional EmailJS config)
  await assertSucceeds(
    adminDb.collection("app_settings").doc("auth_policy").set({
      requireEmailOtp: true,
      emailJsServiceId: "service_demo",
      emailJsTemplateId: "template_demo",
      emailJsPublicKey: "public_key_demo",
      updatedAt: now,
    })
  );

  // /email_verifications is completely blocked from all client reads/writes (even admin)
  await assertFails(studentDb.collection("email_verifications").doc(ALICE_UID).get());
  await assertFails(
    studentDb.collection("email_verifications").doc(ALICE_UID).set({ codeHash: "fake" })
  );
  await assertFails(adminDb.collection("email_verifications").doc(ALICE_UID).get());
  await assertFails(
    adminDb.collection("email_verifications").doc(ALICE_UID).set({ codeHash: "fake" })
  );
});

test("Student chat: PII-free /chat_profiles discovery, participant-only /chats & /messages, read receipts, delete, and blocking", async () => {
  const EVE_UID = "eve_outsider_3";
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@example.com",
    email_verified: true,
  }).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID, {
    email: "eve@example.com",
    email_verified: true,
  }).firestore();

  const now = new Date(Date.now() - 1000);
  const chatId = `dm_${ALICE_UID}_${BOB_UID}`;

  // /chat_profiles forbids leaking email or phone
  await assertFails(
    aliceDb.collection("chat_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayesha",
      email: "alice@example.com",
      yearNumber: 2,
      isOnline: true,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("chat_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayesha",
      phone: "01711000000",
      yearNumber: 2,
      isOnline: true,
      updatedAt: now,
    })
  );

  // Valid PII-free chat profiles succeed and are visible to signed-in students
  await assertSucceeds(
    aliceDb.collection("chat_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayesha",
      departmentName: "রাষ্ট্রবিজ্ঞান",
      yearNumber: 2,
      photoUrl: "",
      isOnline: true,
      lastSeenAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    bobDb.collection("chat_profiles").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Rahim",
      departmentName: "ইতিহাস",
      yearNumber: 3,
      photoUrl: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ==",
      isOnline: true,
      lastSeenAt: now,
      updatedAt: now,
    })
  );

  // Cross-user write on chat_profiles denied; read/list allowed (while /users stays blocked)
  await assertFails(
    bobDb.collection("chat_profiles").doc(ALICE_UID).update({
      displayName: "Hacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(bobDb.collection("chat_profiles").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Alice creates direct chat thread with Bob; outsider Eve cannot create or read it
  await assertFails(
    eveDb.collection("chats").doc(chatId).set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessageText: "",
      blockedBy: [],
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).set({
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Ayesha", [BOB_UID]: "Rahim" },
      lastMessageText: "",
      lastMessageSenderId: "",
      unreadCounts: { [ALICE_UID]: 0, [BOB_UID]: 0 },
      typingUsers: { [ALICE_UID]: 0, [BOB_UID]: 0 },
      blockedBy: [],
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(eveDb.collection("chats").doc(chatId).get());
  await assertSucceeds(bobDb.collection("chats").where("participantIds", "array-contains", BOB_UID).get());

  // Alice sends a text + image message; sender impersonation & Eve denied
  await assertFails(
    bobDb.collection("chats").doc(chatId).collection("messages").doc("spoof_msg").set({
      senderId: ALICE_UID,
      text: "Impersonated",
      createdAt: now,
    })
  );
  await assertFails(
    eveDb.collection("chats").doc(chatId).collection("messages").doc("eve_msg").set({
      senderId: EVE_UID,
      text: "Intruder",
      createdAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").set({
      senderId: ALICE_UID,
      senderName: "Ayesha",
      text: "কেমন আছো? নোটস লাগবে?",
      imageUrl: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ==",
      readBy: [ALICE_UID],
      deletedForAll: false,
      createdAt: now,
    })
  );

  // Outsider Eve cannot read messages
  await assertFails(eveDb.collection("chats").doc(chatId).collection("messages").get());

  // Bob marks msg_1 as read
  await assertSucceeds(
    bobDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").update({
      readBy: [ALICE_UID, BOB_UID],
    })
  );

  // Bob cannot delete Alice's message for everyone, but Alice can delete her own
  await assertFails(
    bobDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").update({
      deletedForAll: true,
      text: "",
      imageUrl: "",
    })
  );
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").update({
      deletedForAll: true,
      text: "",
      imageUrl: "",
    })
  );

  // Alice blocks Bob; Bob cannot send new message or remove Alice's block
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).update({
      blockedBy: [ALICE_UID],
      updatedAt: new Date(Date.now() - 400),
    })
  );
  await assertFails(
    bobDb.collection("chats").doc(chatId).collection("messages").doc("msg_blocked").set({
      senderId: BOB_UID,
      senderName: "Rahim",
      text: "Hello after block",
      readBy: [BOB_UID],
      deletedForAll: false,
      createdAt: new Date(Date.now() - 300),
    })
  );
  await assertFails(
    bobDb.collection("chats").doc(chatId).update({
      blockedBy: [],
      updatedAt: new Date(Date.now() - 300),
    })
  );

  // Alice can unblock Bob
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).update({
      blockedBy: [],
      updatedAt: new Date(Date.now() - 200),
    })
  );
});


