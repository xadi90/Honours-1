const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const crypto = require("crypto");

admin.initializeApp();

const DATABASE_ID = "ai-studio-android-honourss-67c8ea60-541f-40e2-8b7c-2bec1a85c0e0";
const db = admin.firestore();
db.settings({ databaseId: DATABASE_ID });

const BOOTSTRAPPED_ADMIN_EMAILS = new Set([
  "xadiahmed7@gmail.com",
  "xadiahmed23@gmail.com",
]);

function sha256Hex(value) {
  return crypto.createHash("sha256").update(value, "utf8").digest("hex");
}

async function assertAdmin(request) {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Google Sign-In required.");
  }
  const email = (request.auth.token.email || "").toLowerCase();
  const verified = request.auth.token.email_verified === true;
  if (verified && BOOTSTRAPPED_ADMIN_EMAILS.has(email)) {
    return;
  }
  const adminSnap = await db.collection("admins").doc(request.auth.uid).get();
  if (!adminSnap.exists) {
    throw new HttpsError("permission-denied", "Admin privileges required.");
  }
}

/**
 * Verifies the Content Management PIN against app_settings/admin_security
 * with server-side rate limiting.
 */
exports.verifyAdminPassword = onCall({ enforceAppCheck: true }, async (request) => {
  await assertAdmin(request);
  const entered = String(request.data?.pin || "").trim();
  if (!entered) {
    throw new HttpsError("invalid-argument", "Password is required.");
  }

  const uid = request.auth.uid;
  const rateRef = db.collection("app_settings").doc(`admin_rate_${uid}`);
  const rateSnap = await rateRef.get();
  const now = Date.now();
  if (rateSnap.exists) {
    const lockoutUntil = rateSnap.get("lockoutUntilMs") || 0;
    if (lockoutUntil > now) {
      throw new HttpsError("resource-exhausted", "Too many attempts. Please try again later.");
    }
  }

  const secSnap = await db.collection("app_settings").doc("admin_security").get();
  const storedHash = secSnap.get("pinHash");
  if (!storedHash) {
    throw new HttpsError("failed-precondition", "Admin PIN hash is not configured yet.");
  }

  const matched = sha256Hex(entered) === storedHash;
  if (!matched) {
    const attempts = (rateSnap.exists ? rateSnap.get("attempts") || 0 : 0) + 1;
    await rateRef.set({
      attempts,
      lockoutUntilMs: attempts >= 5 ? now + 5 * 60 * 1000 : 0,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return { verified: false, remainingAttempts: Math.max(0, 5 - attempts) };
  }

  await rateRef.set({
    attempts: 0,
    lockoutUntilMs: 0,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await db.collection("admin_logs").add({
    adminUid: uid,
    adminEmail: request.auth.token.email || "",
    action: "CLOUD_FN_ADMIN_UNLOCK",
    summary: "Verified admin panel session via Cloud Function",
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { verified: true };
});

/**
 * Grants admin privileges to another verified Google account UID.
 */
exports.grantAdminRole = onCall({ enforceAppCheck: true }, async (request) => {
  await assertAdmin(request);
  const targetUid = String(request.data?.targetUid || "").trim();
  const targetEmail = String(request.data?.targetEmail || "").trim();
  if (!targetUid) {
    throw new HttpsError("invalid-argument", "targetUid is required.");
  }

  await db.collection("admins").doc(targetUid).set({
    email: targetEmail,
    grantedBy: request.auth.uid,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await db.collection("admin_logs").add({
    adminUid: request.auth.uid,
    adminEmail: request.auth.token.email || "",
    action: "GRANT_ADMIN_ROLE",
    summary: `Granted admin access to ${targetEmail || targetUid}`,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  return { success: true };
});
