# blockit · Paywall-Free Digital Detox (Tablet & Phone Edition)

A native Android digital-detox and full-phone session blocker app modeled after **blockit** (`com.hypenet.focused`), built with:
- **100% Free & Paywall-Free**: Zero in-app purchases, zero ads, all 3 Suitcase bags unlocked.
- **Earned Parachute Economy**: Parachutes regenerate through 7-day focus streaks (never bought with money).
- **Tablet Optimization**: Dedicated `sw600dp` two-pane responsive layout for tablets and foldable devices.
- **Doze-Proof Monotonic Countdown**: Uses `SystemClock.elapsedRealtime()` delta so screen-off sleep never slows down the timer.
- **Anti-Bypass Protection**: Intercepts unwhitelisted packages via `AccessibilityService` and blocks quick-settings exploitation.

---

## How to Build the APK (2 Beginner Methods)

### Method A: Cloud Build via GitHub Actions (Zero Local Setup)
1. Push this project folder to your GitHub account (public or private repository).
2. Go to the **Actions** tab on your GitHub repository.
3. The `Build blockit Android APK` workflow will run automatically (or tap **Run workflow**).
4. Once completed (approx. 2 minutes), download the **`blockit-detox-debug-apk`** artifact zip containing your installable `.apk`!

### Method B: Build Locally with Android Studio
1. Download and install [Android Studio](https://developer.android.com/studio).
2. Open Android Studio and choose **File > Open**, then select this unzipped project folder.
3. Wait for Gradle sync to complete.
4. In the top menu, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Once built, click the **locate** popup link to find `app-debug.apk`.
6. Transfer `app-debug.apk` to your Android phone or tablet via USB/Google Drive and tap to install!
