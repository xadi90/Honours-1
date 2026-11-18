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
