# Privacy Policy & Data Safety — TO_OT Player

**Application ID:** `com.tootplayer.audiohub`  
**Version:** `3.0.0`  
**Developer:** Parsa Salami (`parsa.sal@gmail.com` | Telegram: `@parsa_sal`)  
**Architecture:** 100% Local-First / Zero Cloud Telemetry  
**Last Updated:** August 2026 / شهریور ۱۴۰۵

---

## 🛡️ 1. Zero-Data Collection Declaration (Google Play & Cafe Bazaar Compliant)

**TO_OT Player** operates on a strictly client-side, local-first architecture:
- **Zero Analytics & Telemetry:** No analytics libraries, crash loggers, trackers, or telemetry beacons are embedded in this application.
- **Zero Cloud Storage:** Audio tracks, synthesized waveform caches, equalizer settings, session configurations, and recordings reside solely within the device sandbox and are never transmitted to external cloud servers.
- **No User Account Requirement:** The application functions fully offline without login, registration, or device fingerprinting.

---

## 📱 2. Minimalist Android Permissions & Declarations

| Android Permission | Purpose & Scope |
| :--- | :--- |
| `android.permission.READ_MEDIA_AUDIO` | Required to browse and load user-selected audio tracks from local device storage for multi-lane playback. |
| `android.permission.READ_EXTERNAL_STORAGE` (`maxSdkVersion="32"`) | Required for legacy Android versions (API <= 32) to read local audio files. |
| `android.permission.FOREGROUND_SERVICE` | Required to maintain uninterrupted multi-track audio playback when the app is minimized. |
| `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Required on Android 14+ (API 34+) for foreground media player services. |
| `android.permission.WAKE_LOCK` | Keeps the audio rendering thread active when the screen turns off to prevent buffer underruns. |
| `android.permission.POST_NOTIFICATIONS` | Displays playback status and media controls (Play/Pause/Skip/Close) in the Android notification center. |

> **Excluded Dangerous Permissions:**  
> High-risk permissions such as `RECORD_AUDIO`, `INTERNET`, `ACCESS_FINE_LOCATION`, and `READ_CONTACTS` are excluded to guarantee total data privacy and avoid false-positive security warnings.

---

## 🇮🇷 ۳. بیانیه حفظ حریم خصوصی کاربران (ویژه کافه‌بازار و مایکت)

نرم‌افزار **TO_OT Player** با احترام کامل به حریم خصوصی کاربران گرامی، به صورت کاملاً آفلاین و درون‌دستگاهی (Local-First) عمل می‌کند:
1. **عدم جمع‌آوری و ارسال اطلاعات:** هیچ‌گونه داده شخصی، آماری، لاگ یا فایل صوتی از گوشی شما به سرور خارجی ارسال نمی‌گردد.
2. **دسترسی به فایل‌ها:** دسترسی حافظه صرفاً جهت انتخاب و پخش آهنگ‌های مورد نظر شما در باندهای میکسر استفاده می‌شود.
3. **سازگاری کامل با قوانین کافه‌بازار و گوگل‌پلی.**

---

## 📬 4. Developer Contact

For inquiries regarding this privacy policy or the app architecture:
- **Developer:** Parsa Salami
- **Email:** [parsa.sal@gmail.com](mailto:parsa.sal@gmail.com)
- **Telegram:** [@parsa_sal](https://t.me/parsa_sal)
