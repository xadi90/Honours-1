# Honours Solution — Complete Setup & Deployment Guide

**App Name:** Honours Solution  
**University:** National University (NU), Bangladesh  
**Developer:** Xadi Ahmed (`01878864913` | `xadiahmed23@gmail.com`)  
**Firebase Project:** `gen-lang-client-0015527715`  
**Firestore Named Database:** `ai-studio-android-honourss-67c8ea60-541f-40e2-8b7c-2bec1a85c0e0`  
**Application ID:** `com.aistudio.honourssolution.kqxmzn`

---

## 1. Firebase Console Setup (First-Time Admin Checklist)
1. **Authentication:** Ensure Google Sign-In is enabled in Firebase Console (`Authentication -> Sign-in method -> Google`).
2. **Admin Account:** Sign into the Android app with the bootstrapped admin Google account (`xadiahmed7@gmail.com` or `xadiahmed23@gmail.com`).
3. **Initialize Admin PIN (`2441139`):**
   - Open **Profile -> কনটেন্ট ম্যানেজমেন্ট (Admin Panel)**.
   - Because the password is **never** hardcoded in the APK, on first launch as a bootstrapped admin the screen invites you to initialize the backend PIN.
   - Enter `2441139` and tap **ব্যাকএন্ড পিন সেট ও আনলক করুন**. This stores only the SHA-256 hash in Firestore (`app_settings/admin_security`) and unlocks the full Admin Dashboard.
4. **Seed Starter Catalog:** Inside Admin Dashboard, tap **ডিফল্ট ২২টি বিভাগ ও প্রারম্ভিক সিলেবাস Firestore-এ প্রকাশ করুন** to populate all 22 departments and starter Political Science content into Firestore.
5. **Firebase Storage (for PDF Hosting):**
   - Enable Firebase Storage in Firebase Console (`Storage -> Get Started`).
   - Upload PDFs to Storage or paste any HTTPS PDF download URL in **Admin -> PDF আপলোড ও ব্যবস্থাপনা** (or select a local `.pdf` file via the in-app document picker).
6. **Firebase Remote Config / AI Config:**
   - The app reads live `modelName`, `systemInstructions`, and `maintenanceMode` from `app_settings/ai_config`, editable directly from the Admin Dashboard.
   - Optionally enable Firebase Remote Config in Firebase Console for staged rollouts.

## 2. Cloud Functions Deployment (`/functions`)
```bash
cd functions
npm install
firebase deploy --only functions
```
This deploys `verifyAdminPassword` (server-side rate-limited PIN verification with App Check enforcement) and `grantAdminRole` to your Firebase project.

## 3. Automated APK Build & Direct Download Link (`xadi90/Honours-1`)
This repository includes `.github/workflows/build-apk.yml`, which automatically builds `HonoursSolution.apk` and attaches it to GitHub Releases on every push to `main`/`master` (or manual run in the **Actions** tab).

- **Permanent Direct APK Download URL:**  
  `https://github.com/xadi90/Honours-1/releases/latest/download/HonoursSolution.apk`

For Play Store / custom signed release APKs:
1. Generate an upload keystore (keep outside Git):
   ```bash
   keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
2. Build the signed release APK via Gradle:
   ```bash
   export KEYSTORE_PATH=/path/to/my-upload-key.jks
   export STORE_PASSWORD=your_store_password
   export KEY_PASSWORD=your_key_password
   gradle assembleRelease
   ```
3. Add the release SHA-1 & SHA-256 fingerprints to Firebase Console (`Project Settings -> Android App -> Add fingerprint`) so Google Sign-In and App Check work on release APKs.
4. Upload `app/build/outputs/apk/release/app-release.apk` as `HonoursSolution.apk` to **GitHub Releases** (`v1.0.0`, `v1.0.1`, etc.).
5. Update `app_settings/version_info` in Firestore (or publish a GitHub Release tag) so the in-app **Update Checker** in Settings detects new versions automatically.

## 4. Companion Website on Cloudflare Pages (`/web`)
1. Push the repository (including `/web/`) to GitHub.
2. Open **Cloudflare Dashboard -> Workers & Pages -> Create -> Pages -> Connect to Git**.
3. Configure build settings:
   - **Framework preset:** None (Static)
   - **Build command:** *(leave empty)*
   - **Build output directory:** `web`
4. Deploy. Your landing page & APK download portal will be live on `https://honours-solution.pages.dev` (zero cost, global CDN).
