const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const crypto = require("crypto");
const nodemailer = require("nodemailer");

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

function timingSafeHexEqual(a, b) {
  if (typeof a !== "string" || typeof b !== "string" || a.length !== b.length) {
    return false;
  }
  return crypto.timingSafeEqual(Buffer.from(a, "utf8"), Buffer.from(b, "utf8"));
}

async function authenticateHttpRequest(req) {
  const authHeader = req.headers.authorization || "";
  const idToken = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
  if (!idToken) {
    return { errorStatus: 401, errorMessage: "Google Sign-In authorization token is required." };
  }
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    const appCheckToken = req.headers["x-firebase-appcheck"];
    if (appCheckToken && typeof appCheckToken === "string") {
      try {
        await admin.appCheck().verifyToken(appCheckToken);
      } catch (_err) {
        return { errorStatus: 403, errorMessage: "Invalid Firebase App Check token." };
      }
    }
    return { decodedToken };
  } catch (_err) {
    return { errorStatus: 401, errorMessage: "Invalid or expired Firebase ID token." };
  }
}

function createEmailTransporter() {
  const user = (process.env.SMTP_USER || "").trim();
  const pass = (process.env.SMTP_PASS || "").trim();
  const host = (process.env.SMTP_HOST || "smtp.gmail.com").trim();
  const port = Number(process.env.SMTP_PORT || 465);
  if (!user || !pass) {
    return null;
  }
  return nodemailer.createTransport({
    host,
    port,
    secure: port === 465,
    auth: { user, pass },
  });
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

/**
 * Sends a 5-digit OTP verification code to the signed-in user's email address.
 * Stores only a salted SHA-256 hash in email_verifications/{uid}.
 */
exports.sendLoginOtp = onRequest({ cors: false }, async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }
  const authResult = await authenticateHttpRequest(req);
  if (authResult.errorStatus) {
    return res.status(authResult.errorStatus).json({ error: authResult.errorMessage });
  }

  const uid = authResult.decodedToken.uid;
  const email = (authResult.decodedToken.email || "").trim();
  if (!email) {
    return res.status(400).json({ error: "No email address associated with this Google account." });
  }

  const transporter = createEmailTransporter();
  if (!transporter) {
    return res.status(503).json({
      error: "SMTP email service is not configured on the server (set SMTP_USER and SMTP_PASS).",
    });
  }

  const docRef = db.collection("email_verifications").doc(uid);
  const existing = await docRef.get();
  const now = Date.now();
  if (existing.exists) {
    const lastSentAtMs = existing.get("lastSentAtMs") || 0;
    const remainingCooldownSec = Math.ceil((lastSentAtMs + 60 * 1000 - now) / 1000);
    if (remainingCooldownSec > 0) {
      return res.status(429).json({
        error: `অনুগ্রহ করে ${remainingCooldownSec} সেকেন্ড পর আবার কোড পাঠান।`,
        cooldownSeconds: remainingCooldownSec,
      });
    }
  }

  const otpCode = String(crypto.randomInt(10000, 100000));
  const salt = crypto.randomBytes(16).toString("hex");
  const codeHash = sha256Hex(`${uid}:${otpCode}:${salt}`);
  const expiresAtMs = now + 5 * 60 * 1000;

  await docRef.set({
    uid,
    email,
    salt,
    codeHash,
    attempts: 0,
    lastSentAtMs: now,
    expiresAtMs,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  const fromAddress = (process.env.SMTP_FROM || process.env.SMTP_USER || "").trim();
  await transporter.sendMail({
    from: `"Honours Solution" <${fromAddress}>`,
    to: email,
    subject: `Honours Solution লগইন ভেরিফিকেশন কোড: ${otpCode}`,
    text: `আপনার Honours Solution অ্যাপে লগইন ভেরিফিকেশন কোড (৫ সংখ্যা): ${otpCode}\n\nএই কোডটি ৫ মিনিটের মধ্যে মেয়াদোত্তীর্ণ হবে। এটি কারো সাথে শেয়ার করবেন না।`,
    html: `
      <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px;">
        <h2 style="color: #0f172a; margin-top: 0;">Honours Solution</h2>
        <p style="color: #334155;">আপনার অ্যাকাউন্টে লগইন সম্পন্ন করতে নিচের ৫ সংখ্যার ভেরিফিকেশন কোডটি ব্যবহার করুন:</p>
        <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px; text-align: center; padding: 16px; background: #f1f5f9; border-radius: 10px; color: #0f172a; margin: 20px 0;">
          ${otpCode}
        </div>
        <p style="color: #64748b; font-size: 13px;">এই কোডটি আগামী ৫ মিনিটের জন্য বৈধ। আপনি লগইন করার চেষ্টা না করলে এই ইমেইলটি উপেক্ষা করুন।</p>
      </div>
    `,
  });

  return res.status(200).json({ sent: true, expiresInSeconds: 300 });
});

/**
 * Verifies the 5-digit OTP entered by the signed-in user.
 */
exports.verifyLoginOtp = onRequest({ cors: false }, async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }
  const authResult = await authenticateHttpRequest(req);
  if (authResult.errorStatus) {
    return res.status(authResult.errorStatus).json({ error: authResult.errorMessage });
  }

  const uid = authResult.decodedToken.uid;
  const enteredCode = String(req.body?.code || "").trim();
  if (!/^\d{5}$/.test(enteredCode)) {
    return res.status(400).json({ error: "সঠিক ৫ সংখ্যার কোড লিখুন।" });
  }

  const docRef = db.collection("email_verifications").doc(uid);
  const snap = await docRef.get();
  if (!snap.exists) {
    return res.status(400).json({ error: "কোনো সক্রিয় ভেরিফিকেশন কোড পাওয়া যায়নি। নতুন কোড পাঠান।" });
  }

  const now = Date.now();
  const expiresAtMs = snap.get("expiresAtMs") || 0;
  const attempts = snap.get("attempts") || 0;
  if (attempts >= 5) {
    return res.status(429).json({ error: "অনেকবার ভুল কোড দেওয়া হয়েছে। ৬০ সেকেন্ড পর নতুন কোড পাঠান।" });
  }
  if (now > expiresAtMs) {
    await docRef.delete();
    return res.status(400).json({ error: "কোডটির মেয়াদ শেষ হয়ে গেছে। নতুন কোড পাঠান।" });
  }

  const salt = snap.get("salt") || "";
  const storedHash = snap.get("codeHash") || "";
  const candidateHash = sha256Hex(`${uid}:${enteredCode}:${salt}`);
  if (!timingSafeHexEqual(candidateHash, storedHash)) {
    await docRef.update({ attempts: attempts + 1 });
    return res.status(401).json({
      verified: false,
      error: `ভুল কোড! বাকি চেষ্টা: ${Math.max(0, 4 - attempts)} বার।`,
    });
  }

  await docRef.delete();
  await db.collection("users").doc(uid).set(
    {
      lastEmailOtpVerifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    },
    { merge: true }
  );

  return res.status(200).json({ verified: true });
});
