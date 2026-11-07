package com.example.util

/**
 * Centralized application metadata, semantic versioning, release dates (Shamsi & Gregorian),
 * changelog history, and creator specifications for TO_OT Player.
 */
object AppInfo {
    const val APP_NAME = "TO_OT Player"
    
    // Semantic Versioning: Major.Minor.Patch (Bumped with each feature iteration)
    const val VERSION_NAME = "3.8.0"
    const val VERSION_CODE = 46
    const val MIN_SUPPORTED_VERSION = "1.0.0"
    
    // Store Intent & Web URLs
    const val GOOGLE_PLAY_URL = "market://details?id=com.tootplayer.audiohub"
    const val BAZAAR_URL = "bazaar://details?id=com.tootplayer.audiohub"
    const val MYKET_URL = "myket://details?id=com.tootplayer.audiohub"
    const val PWA_FALLBACK_URL = "https://tootplayer.app/update"
    
    // Last Update Release Dates in strict yyyy/mm/dd Shamsi and Gregorian formats
    const val LAST_UPDATE_SHAMSI = "1405/08/17"
    const val LAST_UPDATE_SHAMSI_VERBOSE = "۱۷ آبان ۱۴۰۵"
    const val LAST_UPDATE_GREGORIAN = "2026/11/07"
    
    // Creator & Developer Information
    const val DEVELOPER_NAME = "gholamreza bahrami"
    const val DEVELOPER_EMAIL = "parsa.sal@gmail.com"
    const val DEVELOPER_TELEGRAM = "@TO_OTOT"
    const val TELEGRAM_URL = "https://t.me/TO_OTOT"

    data class ChangelogItem(
        val titleFa: String,
        val titleEn: String,
        val descFa: String,
        val descEn: String
    )

    val LATEST_CHANGELOG = listOf(
        ChangelogItem(
            titleFa = "نسخه ۳.۸.۰: بازطراحی کاوشگر دانلودها، حل اسکرول کیبورد و ارتقای معماری و عملکرد برنامه",
            titleEn = "v3.8.0: Redesigned Audio Explorer, Seamless Keyboard Dismissal & Architecture Overhaul",
            descFa = "۱. بازسازی کامل پنجره Device Audio & Downloads با استانداردهای مدرن؛ ۲. حل قطعی پنهان‌سازی کیبورد با اسکرول (onDrag) و لمس خارج از کادر؛ ۳. بهینه‌سازی لایه‌ها با collectAsStateWithLifecycle جهت کاهش مصرف باتری و CPU؛ ۴. لود تنبل (Lazy) سرویس‌های سنگین و جلوگیری از فریز؛ ۵. افزودن سقف ایمنی کوئری و بهینه‌سازی MediaStore برای عدم بروز خطای حافظه.",
            descEn = "1. Complete ground-up redesign of Device Audio & Downloads explorer; 2. Rock-solid soft keyboard dismissal on list scroll (on-drag) and outside touch; 3. Transition to collectAsStateWithLifecycle across all audio flows for zero background battery/CPU drain; 4. Lazy-loading for heavy background services for instant cold boot; 5. Optimized MediaStore scanner with memory bounds protection."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۷.۰: افزودن فیلد تنظیم دستی کلید Gemini API در تنظیمات",
            titleEn = "v3.7.0: Manual Gemini API Key Input & Management in Settings",
            descFa = "۱. افزودن فیلد اختصاصی ورود دستی کلید API در منوی تنظیمات همراه با قابلیت مشاهده/پنهان‌سازی رمز، الصاق مستقیم از کلیپ‌بورد، ذخیره و پاکسازی؛ ۲. اولویت‌دهی خودکار به کلید شخصی بر روی کلید پیش‌فرض در جستجوی هوشمند متنی و تصویری با Gemini 3.5 Flash؛ ۳. نمایش وضعیت زنده کلید (شخصی/سیستم/آفلاین).",
            descEn = "1. Dedicated manual API Key input card in Settings with visibility toggle, one-tap clipboard paste, save & remove actions; 2. Automatic prioritization of personal custom key for Gemini 3.5 Flash semantic & image search; 3. Live status indicator badge showing current key state (Custom / System / Offline)."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۶.۰: هوش مصنوعی جستجوی کتابخانه، تثبیت رنگ لاین‌ها و بهینه‌سازی پلی‌لیست",
            titleEn = "v3.6.0: AI Smart Library Search, Lane Color Fix & Streamlined Playlist",
            descFa = "۱. هوش مصنوعی جستجوی دوطرفه (فارسی/انگلیسی، حس‌و‌حال و تصویری با Gemini) بدون کادرهای مزاحم؛ ۲. فعال‌سازی و تثبیت کامل رنگ اختصاصی لاین‌ها با اعمال بلادرنگ بر روی کارت، حاشیه، کاور و امواج صوتی؛ ۳. ساده‌سازی و خلوت‌سازی پنجره پلی‌لیست لاین با حذف کلیدهای اضافی.",
            descEn = "1. AI Smart Search with bilingual transliteration, mood detection, and image-based track discovery without intrusive explanation blocks; 2. Real-time Lane Color Accent with immediate visual feedback across cards, borders, artwork, and soundwaves; 3. Streamlined playlist sheet layout with clean minimal controls."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۵.۰: کدگذاری رنگی لاین‌ها (Color Accent) و ویجت اختصاصی صفحه اصلی اندروید",
            titleEn = "v3.5.0: Lane Color Accent & Android Homescreen Widget",
            descFa = "۱. کدگذاری رنگی لاین‌ها (Lane Color Accent) با پالت رنگ‌های مجزا (قرمز، آبی، سبز و...) برای تشخیص چشمی فوری؛ ۲. ویجت صفحه اصلی اندروید (Homescreen Widget) جهت کنترل پخش/توقف و مدیریت نشست‌ها بدون باز کردن کامل اپلیکیشن.",
            descEn = "1. Lane Color Accent with distinct color palette for immediate visual recognition; 2. Android Homescreen Widget for playback controls directly from the home screen."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۴.۲: افزودن حالت تکرار کل لاین (شروع مجدد از اولین موزیک پس از اتمام پلی‌لیست)",
            titleEn = "v3.4.2: Repeat Lane Playlist Mode (Restart From First Track Upon Finish)",
            descFa = "افزودن کلید اختصاصی تکرار کل پلی‌لیست لاین در منو و کنترل‌های کارت؛ هرگاه تمام موزیک‌های پلی‌لیست لاین جاری تمام شود، پخش به طور خودکار دوباره از اولین موزیک همان لاین شروع می‌گردد.",
            descEn = "Added a dedicated Repeat Lane Playlist option in track controls and menus: whenever the playlist finishes its final track, playback automatically restarts from the first music track of the lane."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۴.۱: بهبود عملکرد، بهینه‌سازی چیدمان و ثبات پخش",
            titleEn = "v3.4.1: Performance boost, clean layout organization & high-fidelity playback",
            descFa = "بهبود ساختار و نظم جدول و لیست‌ها، روان‌تر شدن کنترل پخش و صدای مستقل لاین‌ها، و طراحی تمیز و استاندارد به زبان انگلیسی.",
            descEn = "Enhanced layout structure for list items and tables, seamless independent multi-lane playback, and clean user experience."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۳.۶: افزودن لاین اختصاصی «ریمیکس» به ردیف اول صفحه اصلی",
            titleEn = "v3.3.6: Dedicated 'Remix' Audio Lane in Top Row of Sessions Screen",
            descFa = "افزودن لاین جدید «ریمیکس» در بالاترین ردیف صفحه اول برنامه برای میکس و پلی‌بک اختصاصی فایل‌های ریمیکس با دسترسی سریع و امکان مرتب‌سازی آزادانه.",
            descEn = "Added a dedicated 'Remix' audio lane in the top row of the home screen for quick mixing and independent playback with full reordering support."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۳.۵: تکرار اختصاصی همان موزیک در حال پخش (Single-Track Loop) به جای کل پلی‌لیست",
            titleEn = "v3.3.5: Exact Active Track Looping (Single-Track Loop Mode)",
            descFa = "عملکرد کلید Loop دقیقاً محدود به همان موزیک در حال پخش شد؛ موزیک جاری به صورت بدون وقفه و مداوم تکرار می‌شود بدون اینکه به ترک‌های بعدی پلی‌لیست پرش کند.",
            descEn = "The Loop option now repeats the exact currently playing track endlessly and gaplessly without advancing through the rest of the playlist."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۳.۴: درج لاین جدید در ردیف اول با حفظ کامل تنظیمات لوپ و قابلیت جابه‌جایی دستی",
            titleEn = "v3.3.4: Top-Row Insertion for New Audio Lanes with Protected Loop State",
            descFa = "قرارگیری خودکار لاین‌های جدید در ردیف اول (بالای فهرست) با دسترسی سریع، حفظ ۱۰۰٪ تنظیمات لوپ و صدای لاین‌های موجود، همراه با امکان جابه‌جایی و مرتب‌سازی آزادانه لاین‌ها به بالا و پایین توسط کاربر.",
            descEn = "New audio lanes are automatically placed at the top (first row) for immediate accessibility, with zero impact on existing loop settings or playback, while full manual reordering remains available."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۳.۳: تثبیت کامل و محافظت از وضعیت حلقه تکرار (Loop) در تمام لاین‌ها",
            titleEn = "v3.3.3: Bulletproof Loop State Preservation & Playlist Synchronization",
            descFa = "محافظت کامل از وضعیت Loop در برابر بازنویسی هنگام افزودن لاین جدید یا تعویض ترک‌های پلی‌لیست، همگام‌سازی فوری دیتابیس با موتور صوتی، جلوگیری از توقف ناخواسته لاین‌های بدون پلی‌لیست و حفظ کامل پایداری پخش.",
            descEn = "Guaranteed loop state preservation against overwrites when creating new lanes or selecting playlist tracks, immediate database synchronization, safe playlist synchronization without stopping direct files, and rock-solid playback stability."
        ),
        ChangelogItem(
            titleFa = "نسخه ۳.۳.۲: رفع قطعی تداخل لاین جدید با لوپ و بهبود تفکیک نوار وضعیت",
            titleEn = "v3.3.2: Complete Loop Stability on New Lane Addition & Status Bar Refinement",
            descFa = "حل کامل مشکل از کار افتادن یا به هم ریختن تنظیمات لوپ (Loop) هنگام ایجاد لاین جدید با تثبیت ترتیب اندیس‌ها در انتهای لیست و همگام‌سازی بلادرنگ پایگاه داده، حفظ دائمی وضعیت فعال بودن لوپ بین اجرای مجدد نرم‌افزار، تکرار بدون وقفه سیستمی، و تفکیک استاندارد لبه‌به‌لبه نوار وضعیت برای خوانایی کامل ساعت و آیکون‌های موبایل.",
            descEn = "Completely fixed loop reset issue when creating new audio lanes by preserving bottom-append order and real-time Room database synchronization, persistent loop state across sessions, gapless hardware playback looping, and edge-to-edge system status bar boundary for enhanced visibility of clock and system indicators."
        ),
        ChangelogItem(
            titleFa = "اصلاح اساسی سیستم تکرار موزیک (Loop)، پایداری لاین‌های جدید و تفکیک نوار وضعیت سیستم",
            titleEn = "Seamless Gapless Loop, Safe Lane Addition & Status Bar Boundary",
            descFa = "رفع مشکل عدم تکرار موزیک با فعال‌سازی تکرار بدون وقفه سیستمی، جلوگیری از به هم ریختن و جابه‌جایی تنظیمات تکرار با افزودن لاین‌ها به انتهای لیست، حفظ پایدار اطلاعات در دیتابیس، و ترسیم خط تفکیک‌کننده نوار وضعیت موبایل برای خوانایی ساعت و نشانگرها در تمام تم‌ها.",
            descEn = "Resolved music loop functionality with gapless hardware looping, stabilized new lane addition by appending to bottom to prevent card shifts and settings reset, ensured persistent DB state preservation, and added an edge-to-edge status bar separator boundary line for clear visibility of system clock and indicators across all themes."
        ),
        ChangelogItem(
            titleFa = "سیستم هوشمند مسیریابی صدا و اتصال به سیستم‌های صوتی و ضبط خودرو (حالت SCO)",
            titleEn = "Smart Audio Output Routing & Car Stereo Telephony (SCO) Mode",
            descFa = "رفع مشکل عدم پخش موزیک در سیستم‌های صوتی و ضبط خودرو هنگام اتصال، افزودن سوئیچ اختصاصی پخش صوتی تلفنی (SCO)، پنجره انتخاب خروجی صدا برای AUX، بلوتوث و بلندگو با تشخیص خودکار لحظه‌ای و راهنمای عیب‌یابی.",
            descEn = "Resolved external car stereo and audio system playback failure over phone connection, added dedicated Phone Audio (SCO) telephony mode switch, dynamic audio output selector for AUX, Headphones & Bluetooth, and troubleshooting guide."
        ),
        ChangelogItem(
            titleFa = "اصلاح قطعی الصاق فایل اکسترکت‌شده به لاین صوتی و به‌روزرسانی آموزش",
            titleEn = "Target Lane Assignment Fix & Tutorial Refresh",
            descFa = "اصلاح فرآیند انتخاب لاین هدف و ایجاد لاین جدید، همگام‌سازی فوری دیتابیس و پلی‌لیست، اضافه شدن نشانگر تایید بصری و دکمه انتقال مستقیم به پلیر صوتی به همراه آموزش جامع.",
            descEn = "Completely resolved target lane assignment with immediate DB & playlist sync, added visual confirmation badge with direct 'Go to Player' navigation, and refreshed tutorials."
        ),
        ChangelogItem(
            titleFa = "به‌روزرسانی جامع آموزش نرم‌افزار و راهنمای تور تعاملی",
            titleEn = "Interactive Onboarding Tour & Knowledge Base Update",
            descFa = "افزایش مراحل آموزش تعاملی به ۵ مرحله شامل استخراج صدا از ویدیو، اسلایدر دوطرفه برش و کنترل دقیق لاین‌ها به همراه به‌روزرسانی مرکز راهنما.",
            descEn = "Expanded interactive tour to 5 steps covering video extraction, dual-slider trimming, and lane controls with refreshed Help Center."
        ),
        ChangelogItem(
            titleFa = "اصلاح و ارتقای ماژول استخراج صوتی و اسلایدر دوطرفه برش",
            titleEn = "Video-to-Audio Extractor & Range Trim Slider Fix",
            descFa = "بهبود الصاق مستقیم صوت استخراج‌شده به لاین هدف یا لاین جدید و تجهیز پنجره به اسلایدر برش دوطرفه برای تعیین سریع بازه.",
            descEn = "Enhanced direct target lane attachment and interactive dual-slider waveform trimming with live preview."
        ),
        ChangelogItem(
            titleFa = "یکپارچه‌سازی و رفع تداخل کلاس‌های تکراری و ارتقای امنیت",
            titleEn = "Code Duplication Cleanup & Security Hardening",
            descFa = "تثبیت ماژول اصلی استخراج صوت، افزودن مجوز صوتی در مانیفست و ارتقای کد ساخت به نسخه ۳.۲.۱ (بیلد ۳۳).",
            descEn = "Consolidated audio extractor classes, declared required audio permission, and bumped build to 3.2.1 (build 33)."
        ),
        ChangelogItem(
            titleFa = "تایمر خواب هوشمند سراسری با نشانگر زنده در هدر",
            titleEn = "Global Resilient Sleep Timer with Live Header Pill",
            descFa = "شمارش معکوس مستقل بدون توقف در هنگام تعویض صفحات، دکمه‌های تمدید سریع ۵ و ۱۵ دقیقه و فید صدای آرام.",
            descEn = "Wall-clock precision countdown with top header indicator, quick extensions (+5m/+15m), and gentle fade-out."
        ),
        ChangelogItem(
            titleFa = "مدیریت یکپارچه پوسته‌ها و رابط کاربری ارگونومیک",
            titleEn = "Unified Theme Engine & Ergonomic Interface",
            descFa = "بهینه‌سازی تنظیمات ظاهر برنامه و دسترسی مستقیم و روان به پالت‌های رنگی و تم‌های مدرن.",
            descEn = "Streamlined appearance settings with direct, seamless access to modern color palettes and themes."
        )
    )
}
