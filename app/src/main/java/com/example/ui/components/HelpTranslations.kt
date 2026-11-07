package com.example.ui.components

import com.example.util.AppInfo
import com.example.util.AppLanguage

enum class HelpSectionId(val key: String) {
    MULTI_LANE("multi_lane"),
    LOCAL_FILES_LANE_CONTROLS("local_files_lane_controls"),
    SIMULTANEOUS_SOLO_MUTE("simultaneous_solo_mute"),
    EQUALIZER_FREQUENCIES("equalizer_frequencies"),
    RECORDER_CROSSFADER_SLEEP("recorder_crossfader_sleep"),
    VIDEO_AUDIO_EXTRACTOR("video_audio_extractor"),
    THEMES_APPEARANCE("themes_appearance"),
    MEDIA_SESSION_HEADSET("media_session_headset"),
    AUDIO_ROUTING_CAR_SYSTEMS("audio_routing_car_systems")
}

data class HelpSection(
    val id: HelpSectionId,
    val title: String,
    val summary: String,
    val iconName: String,
    val bullets: List<String>
)

data class HelpStrings(
    val helpCenterTitle: String,
    val helpSubtitle: String,
    val searchPlaceholder: String,
    val noResultsFound: String,
    val noResultsSubtitle: String,
    val close: String,
    val selectLanguage: String,
    val sectionsCount: String,
    val tipTitle: String,
    val tipDescription: String,
    val sections: List<HelpSection>
)

object HelpTranslations {

    fun getTranslations(language: AppLanguage): HelpStrings {
        return when (language) {
            AppLanguage.FA -> faStrings
            AppLanguage.EN -> enStrings
            AppLanguage.ES -> esStrings
            AppLanguage.ZH -> zhStrings
            AppLanguage.HI -> hiStrings
            AppLanguage.PT -> ptStrings
        }
    }

    private val faStrings = HelpStrings(
        helpCenterTitle = "مرکز راهنما و آموزش",
        helpSubtitle = "راهنمای جامع امکانات، ابزارها و قابلیت‌های TO_OT Player (نسخه ${AppInfo.VERSION_NAME})",
        searchPlaceholder = "جستجو در راهنما (مثلاً: استخراج ویدیو، تم، تایمر خواب، اکولایزر)...",
        noResultsFound = "نتیجه‌ای یافت نشد",
        noResultsSubtitle = "لطفاً عبارت دیگری را جستجو کنید یا زبان راهنما را تغییر دهید.",
        close = "بستن",
        selectLanguage = "زبان راهنما",
        sectionsCount = "بخش راهنما",
        tipTitle = "نکتهٔ کاربردی",
        tipDescription = "پخش صدا حین مطالعه راهنما متوقف نمی‌شود و می‌توانید همزمان بخش‌های مختلف برنامه را امتحان کنید.",
        sections = listOf(
            HelpSection(
                id = HelpSectionId.MULTI_LANE,
                title = "۱. مدیریت چند لاین (Multi-Lane)",
                summary = "نحوه ایجاد، ترتیب‌بندی و مدیریت چندین لاین صوتی مستقل در یک پروژه",
                iconName = "queue",
                bullets = listOf(
                    "افزودن لاین جدید: با کلیک روی دکمه «+» در نوار بالای صفحه، یک لاین صوتی جدید بلافاصله در بالای فهرست اضافه می‌شود.",
                    "جابه‌جایی با کشیدن (Drag & Drop): با نگه‌داشتن انگشت روی آیکون جابه‌جایی هر کارت، می‌توانید موقعیت لاین را به بالا یا پایین بکشید.",
                    "جابه‌جایی با دکمه‌ها: با استفاده از دکمه‌های فلش بالا و پایین در منوی هر لاین، ترتیب لاین‌ها را به سادگی تغییر دهید.",
                    "انتخاب فایل صوتی: هر لاین می‌تواند فایل محلی مجزا از حافظه داخلی، فایل‌های تولیدشده نمونه یا پوشه دانلودها بارگذاری کند."
                )
            ),
            HelpSection(
                id = HelpSectionId.LOCAL_FILES_LANE_CONTROLS,
                title = "۲. فایل‌های محلی و کنترل‌های لاین",
                summary = "تغییر نام، تنظیم مستقل ولوم، جلو/عقب بردن و مدیریت صف پخش هر لاین",
                iconName = "audiotrack",
                bullets = listOf(
                    "انتخاب فایل از دستگاه: با لمس هر لاین یا استفاده از تب کاوشگر دستگاه، فایل‌های صوتی موجود در گوشی را جستجو و بارگذاری نمایید.",
                    "تغییر نام لاین: نام هر لاین را می‌توانید به صورت اختصاصی ویرایش کنید تا پروژه‌های چندلایه مرتب باقی بمانند.",
                    "تنظیم ولوم اختصاصی: هر لاین دارای اسلایدر صدای جداگانه از ۰٪ تا ۱۰۰٪ است که تداخلی با ولوم کلی سیستم ندارد.",
                    "تکرار کل لاین (Repeat Lane): با روشن کردن دکمه تکرار لاین 🔁، وقتی موزیک‌های پلی‌لیست لاین جاری تمام شد، پخش دوباره از ابتدا و از اولین موزیک همان لاین شروع می‌شود.",
                    "پخش، توقف و پیمایش مستقل: امکان Play/Pause اختصاصی و اسلایدر Seek برای حرکت دقیق در بازه زمانی هر قطعه فراهم است."
                )
            ),
            HelpSection(
                id = HelpSectionId.SIMULTANEOUS_SOLO_MUTE,
                title = "۳. پخش همزمان، سولو (Solo) و میوت (Mute)",
                summary = "میکسر حرفه‌ای برای جداسازی یا خاموش‌کردن ترک‌ها حین اجرای همزمان",
                iconName = "tune",
                bullets = listOf(
                    "پخش چند لاین با هم: همه لاین‌های فعال می‌توانند به شکل همزمان و بی‌درنگ بدون افت کیفیت یا نویز پخش شوند.",
                    "ویژگی سولو (Solo): با فعال‌سازی دکمه Solo روی یک یا چند لاین، بقیه لاین‌ها به طور خودکار بی‌صدا شده و فقط لاین‌های انتخابی شنیده می‌شوند.",
                    "ویژگی میوت (Mute): با زدن دکمه Mute، صدای همان لاین بدون توقف زمان پخش قطع می‌گردد.",
                    "دکمه پخش/توقف همه: در نوار پایینی می‌توانید با یک کلیک تمام لاین‌های موجود را همزمان شروع یا متوقف کنید."
                )
            ),
            HelpSection(
                id = HelpSectionId.EQUALIZER_FREQUENCIES,
                title = "۴. اکولایزر، افکت‌های صوتی و DSP",
                summary = "اکولایزر چندبانده، تقویت فرکانس‌ها، صدای سه‌بعدی و ریورب محیطی",
                iconName = "equalizer",
                bullets = listOf(
                    "اکولایزر گرافیکی: کنترل دقیق فرکانس‌های زیر، میانی و بم از ۶۰ هرتز تا ۱۴ کیلوهرتز به صورت بلادرنگ.",
                    "پریست‌های صوتی آماده: انتخاب سریع حالت‌های از پیش تنظیم‌شده نظیر Rock، Pop، Jazz، Classical، Bass Boost و Flat.",
                    "افکت‌های فضایی و DSP: تقویت بیس (Bass Boost)، شبیه‌ساز مجازی صوتی (Virtualizer) و ریورب محیطی.",
                    "تنظیم و ریست سریع: با زدن دکمه Reset می‌توانید منحنی فرکانسی را فوراً به حالت تراز استاندارد بازگردانید."
                )
            ),
            HelpSection(
                id = HelpSectionId.RECORDER_CROSSFADER_SLEEP,
                title = "۵. ضبط‌کننده، کراس‌فیدر و تایمر خواب هوشمند",
                summary = "ضبط باکیفیت، میکس انتقالی دی‌جی و تایمر خواب پایدار با کنترل از هدر",
                iconName = "mic",
                bullets = listOf(
                    "ضبط‌کننده داخلی (Recorder): ضبط صدا با فرمت استاندارد، همراه با نمایش تایمر، حجم ضبط و امکان افزودن مستقیم به عنوان لاین جدید.",
                    "کراس‌فیدر (Crossfader): اسلایدر انتقالی حرفه‌ای برای تنظیم توازن بلندی صدا بین دو لاین دلخواه به سبک دی‌جی.",
                    "تایمر خواب پایدار سراسری (Sleep Timer): شمارش معکوس دقیق بر پایه ساعت واقعی بدون توقف حین خروج از پنجره یا تعویض تب‌ها.",
                    "کنترل از هدر و تمدید سریع: مشاهده زمان باقی‌مانده در نوار بالای برنامه و امکان تمدید سریع با دکمه‌های +5m و +15m به همراه فید نرم ۳۰ ثانیه‌ای."
                )
            ),
            HelpSection(
                id = HelpSectionId.VIDEO_AUDIO_EXTRACTOR,
                title = "۶. استخراج صدا از ویدیو و برش دقیق دوطرفه",
                summary = "استخراج صدای ویدیوهای گالری، برش بازه با اسلایدر دوطرفه و افزودن مستقیم به لاین هدف یا لاین جدید",
                iconName = "video",
                bullets = listOf(
                    "انتخاب هر ویدیوی محلی: پشتیبانی از فرمت‌های ویدیویی رایج در دستگاه با دسترسی امن و مستقیم از گالری.",
                    "اسلایدر دوطرفه برش (Range Trim Slider): تنظیم آسان و دقیق نشانگرهای آغاز و پایان با کشیدن دستگیره‌ها روی تایم‌لاین.",
                    "پیش‌نمایش بلادرنگ: شنیدن بخش برش‌خورده پیش از ذخیره‌سازی برای اطمینان از صحت بازه انتخابی.",
                    "افزودن مستقیم به پروژه: الصاق قطعه صوتی استخراج‌شده به لاین هدف دلخواه یا ساخت لاین جدید با یک کلیک، همگام‌سازی بلادرنگ دیتابیس و امکان انتقال مستقیم به پلیر صوتی با کلید میانبر."
                )
            ),
            HelpSection(
                id = HelpSectionId.THEMES_APPEARANCE,
                title = "۷. موتور پوسته‌ها و سفارشی‌سازی ظاهر",
                summary = "انتخاب تم‌های رنگی مدرن، استایل‌های ارگانیک، دارک و بیوفیلیک",
                iconName = "palette",
                bullets = listOf(
                    "موتور پوسته‌ها (Theme Engine): دسترسی به مجموعه‌ای از تم‌های هماهنگ با طراحی مدرن Material 3.",
                    "پوسته پیش‌فرض بیوفیلیک (Biophilic Green): طراحی چشم‌نواز گیاهی با رنگ‌های زیتونی و سنگی آرامش‌بخش.",
                    "تنوع رنگی: تم‌های اختصاصی شامل سایان سایبرپانک، زمردی نئونی، امواج اقیانوسی و ارغوانی عمیق.",
                    "ذخیره دائمی تنظیمات: پوسته انتخابی شما در حافظه پایدار دستگاه ذخیره شده و پس از بستن برنامه حفظ می‌شود."
                )
            ),
            HelpSection(
                id = HelpSectionId.MEDIA_SESSION_HEADSET,
                title = "۸. کنترل‌های مدیا سشن و هندزفری",
                summary = "مدیریت پخش از طریق اعلان سیستم اندروید و کلیدهای استاندارد هندزفری",
                iconName = "headphones",
                bullets = listOf(
                    "اعلان پس‌زمینه (Media Notification): کنترل پخش، مکث و جابه‌جایی بین ترک‌ها مستقیماً از نوار نوتیفیکیشن یا لاک‌اسکرین اندروید.",
                    "کلیدهای فیزیکی هدفون: پشتیبانی از دستورات استاندارد Play/Pause، قطعه بعدی و قبلی بر اساس پروتکل استاندارد اندروید.",
                    "توجه سازگاری: عملکرد رفتارهای اختصاصی هندزفری‌های بلوتوثی بسته به نسخه اندروید و مدل سخت‌افزار دستگاه ممکن است متغیر باشد."
                )
            ),
            HelpSection(
                id = HelpSectionId.AUDIO_ROUTING_CAR_SYSTEMS,
                title = "۹. مسیریابی خروجی صدا، ضبط خودرو و حالت تلفنی (SCO)",
                summary = "حل مشکل عدم پخش صدا در ضبط خودرو، سیستم‌های صوتی و جابه‌جایی بین خروجی‌ها",
                iconName = "alt_route",
                bullets = listOf(
                    "مشکل عدم پخش در ضبط ماشین: برخی سیستم‌های صوتی و ضبط‌های بلوتوثی خودرو فقط تماس تلفنی دریافت می‌کنند و صدای موسیقی (A2DP) را پخش نمی‌کنند.",
                    "حالت صوتی تلفن (SCO Telephony Mode): با فعال‌سازی این کلید در پنجره مسیریابی خروجی صدا، موزیک از طریق کانال صوتی مکالمه تلفنی به سیستم خودرو ارسال می‌شود و بدون وقفه پخش می‌گردد.",
                    "انتخاب دستی خروجی صدا: جابه‌جایی سریع بین خروجی‌های فعال شامل بلوتوث استریو، کابل AUX، هدفون، خروجی USB و بلندگوی داخلی گوشی.",
                    "دسترسی سریع: آیکون اختصاصی مسیریابی خروجی صدا در نوار بالای صفحه (کنار دکمه تم) و همچنین در تب تنظیمات در دسترس است."
                )
            )
        )
    )

    private val enStrings = HelpStrings(
        helpCenterTitle = "Help Center & Guide",
        helpSubtitle = "Comprehensive guide to all features, tools, and workflows in TO_OT Player (v${AppInfo.VERSION_NAME})",
        searchPlaceholder = "Search guide (e.g. Video Extractor, Theme, Sleep Timer, EQ)...",
        noResultsFound = "No matching topics found",
        noResultsSubtitle = "Try searching with a different term or select another guide language.",
        close = "Close",
        selectLanguage = "Guide Language",
        sectionsCount = "Help Topics",
        tipTitle = "Helpful Tip",
        tipDescription = "Audio playback continues smoothly while browsing the Help Center.",
        sections = listOf(
            HelpSection(
                id = HelpSectionId.MULTI_LANE,
                title = "1. Multi-Lane Management",
                summary = "Create, organize, and manage independent audio lanes in real time",
                iconName = "queue",
                bullets = listOf(
                    "Add New Lane: Tap the '+' button in the top bar to immediately insert a fresh audio lane at the top of your session list.",
                    "Drag & Drop Reorder: Long-press and drag any lane handle up or down to restructure your track arrangement.",
                    "Button Reorder: Use the up and down arrow buttons inside each lane's options to move tracks quickly.",
                    "Audio Selection: Each lane can load independent local audio files from your device storage or sample generators."
                )
            ),
            HelpSection(
                id = HelpSectionId.LOCAL_FILES_LANE_CONTROLS,
                title = "2. Local File & Lane Controls",
                summary = "Custom naming, individual volume, seek bar, and independent playback",
                iconName = "audiotrack",
                bullets = listOf(
                    "Device File Explorer: Search and load audio tracks directly from your internal storage or SD card.",
                    "Lane Renaming: Custom name each lane to keep complex multi-track projects clearly organized.",
                    "Dedicated Volume: Every lane has an independent volume slider (0%–100%) that respects your master output.",
                    "Repeat Lane Playlist: Turn on Repeat Lane 🔁 to automatically restart playback from the very first track of this lane as soon as its playlist finishes.",
                    "Independent Transport: Play, pause, and scrub through tracks independently without affecting other lanes."
                )
            ),
            HelpSection(
                id = HelpSectionId.SIMULTANEOUS_SOLO_MUTE,
                title = "3. Simultaneous Playback, Solo & Mute",
                summary = "Professional multi-track mixing with instantaneous track isolation and silencing",
                iconName = "tune",
                bullets = listOf(
                    "Concurrence: All active tracks play simultaneously in phase without audio degradation or distortion.",
                    "Solo Feature: Isolates one or more selected lanes while automatically muting all other lanes.",
                    "Mute Feature: Silences a specific lane instantly without interrupting its playback timeline.",
                    "Master Transport: The bottom controls allow you to play or pause all active lanes together in a single tap."
                )
            ),
            HelpSection(
                id = HelpSectionId.EQUALIZER_FREQUENCIES,
                title = "4. Equalizer, Audio Effects & DSP",
                summary = "Multi-band graphic EQ, audio enhancement, spatial virtualizer, and reverb",
                iconName = "equalizer",
                bullets = listOf(
                    "Graphic Equalizer: Adjust low, mid, and high frequency bands in real-time across your soundstage.",
                    "Built-in Presets: Instant switching between Rock, Pop, Jazz, Classical, Bass Boost, Vocal Boost, and Flat profiles.",
                    "DSP & Spatial Effects: Dynamic bass boost, 3D audio virtualizer, and environmental reverb presets.",
                    "Quick Reset: Reset all frequency bands back to a flat neutral curve with a single touch."
                )
            ),
            HelpSection(
                id = HelpSectionId.RECORDER_CROSSFADER_SLEEP,
                title = "5. Recorder, Crossfader & Smart Sleep Timer",
                summary = "High-fidelity recording, DJ-style crossfading, and wall-clock resilient sleep timer",
                iconName = "mic",
                bullets = listOf(
                    "Internal Recorder: Record clear audio with elapsed timers, live levels, and immediate one-tap import into a new lane.",
                    "DJ Crossfader: Smoothly blend volume balance between two chosen audio lanes with linear crossfade curves.",
                    "Resilient Sleep Timer: Engine-level countdown persisting across tab switches and app navigation.",
                    "Header Pill & Extensions: Live countdown pill in the top bar, +5m / +15m quick extensions, and gentle 30s volume fade-out."
                )
            ),
            HelpSection(
                id = HelpSectionId.VIDEO_AUDIO_EXTRACTOR,
                title = "6. Video-to-Audio Extractor & Range Trimmer",
                summary = "Extract high-quality audio from gallery videos with interactive dual-slider range trimming",
                iconName = "video",
                bullets = listOf(
                    "Pick Any Video: Choose video files from your device gallery or downloads with zero permission overhead.",
                    "Interactive Range Trim Slider: Smoothly drag start and end handles directly on the timeline for millisecond-precision trimming.",
                    "Live Audio Audition: Audition and preview your extracted snippet prior to exporting.",
                    "Direct Target Lane Import: Save extracted clips directly into your selected target lane or instantly create a new top lane."
                )
            ),
            HelpSection(
                id = HelpSectionId.THEMES_APPEARANCE,
                title = "7. Theme Engine & Visual Customization",
                summary = "Modern palettes, biophilic green styling, and persistent theme preferences",
                iconName = "palette",
                bullets = listOf(
                    "Theme Engine: Switch seamlessly between curated Material 3 themes designed for clarity and contrast.",
                    "Biophilic Default Theme: Soothing botanical green and organic stone styling optimized for eye comfort.",
                    "Vibrant Palettes: Expressive choices including Cyberpunk Cyan, Neon Emerald, Deep Violet, and Oceanic Blue.",
                    "Persistent Storage: Your preferred theme is stored locally and restored automatically on app launch."
                )
            ),
            HelpSection(
                id = HelpSectionId.MEDIA_SESSION_HEADSET,
                title = "8. Media Session & Headset Controls",
                summary = "System notification controls and physical headphone button integration",
                iconName = "headphones",
                bullets = listOf(
                    "Media Notification: Full lock-screen and notification drawer playback controls (play, pause, next, previous).",
                    "Headset Key Events: Standard single, double, and triple clicks on wired or Bluetooth earbuds trigger transport actions.",
                    "Hardware Compatibility: Specific Bluetooth headset behaviors depend on OS version and headset vendor specs."
                )
            ),
            HelpSection(
                id = HelpSectionId.AUDIO_ROUTING_CAR_SYSTEMS,
                title = "9. Audio Routing, Car Systems & Telephony (SCO) Mode",
                summary = "Solve playback issues on car stereos, Bluetooth audio systems, and switch active outputs",
                iconName = "alt_route",
                bullets = listOf(
                    "Car Stereo Playback Issue: Certain car head units and older Bluetooth receivers only accept phone calls and reject A2DP media streams.",
                    "Phone Audio (SCO Telephony Mode): Toggle this switch in the Audio Routing panel to route your music through the telephone call audio channel, ensuring immediate audio through your car speakers.",
                    "Manual Output Selection: Easily switch between Stereo Bluetooth, AUX Cable / Headphones, USB Audio, and Phone Speaker.",
                    "Quick Access: Tap the Audio Routing icon in the top header bar (next to the theme palette button) or access it from Settings."
                )
            )
        )
    )

    private val esStrings = HelpStrings(
        helpCenterTitle = "Centro de Ayuda y Guía",
        helpSubtitle = "Guía completa de funciones y herramientas profesionales de TO_OT Player (v${AppInfo.VERSION_NAME})",
        searchPlaceholder = "Buscar en la guía (ej. Extractor de video, Temas, Temporizador, EQ)...",
        noResultsFound = "No se encontraron resultados",
        noResultsSubtitle = "Intente buscar con otro término o cambie de idioma.",
        close = "Cerrar",
        selectLanguage = "Idioma de la Guía",
        sectionsCount = "Temas de Ayuda",
        tipTitle = "Consejo Útil",
        tipDescription = "La reproducción de audio continuará normalmente mientras consulta esta guía.",
        sections = listOf(
            HelpSection(
                id = HelpSectionId.MULTI_LANE,
                title = "1. Gestión Multi-Pista",
                summary = "Creación, reordenamiento y control de pistas de audio independientes",
                iconName = "queue",
                bullets = listOf(
                    "Agregar nueva pista: Toque el botón '+' en la barra superior para insertar una nueva pista arriba de la lista.",
                    "Reordenar por arrastre: Mantenga presionado el tirador de cualquier pista para desplazarla arriba o abajo.",
                    "Reordenar por botones: Utilice las flechas arriba/abajo dentro de las opciones de la pista.",
                    "Selección de archivos: Cada pista puede cargar archivos de audio locales de forma independiente."
                )
            ),
            HelpSection(
                id = HelpSectionId.LOCAL_FILES_LANE_CONTROLS,
                title = "2. Archivos Locales y Controles",
                summary = "Nombres personalizados, volumen individual y reproducción independiente",
                iconName = "audiotrack",
                bullets = listOf(
                    "Explorador de archivos: Busque y cargue pistas desde su almacenamiento interno.",
                    "Renombrar pistas: Personalice el nombre de cada pista para mantener organizados sus proyectos.",
                    "Volumen dedicado: Cada pista tiene control deslizante de volumen de 0% a 100%.",
                    "Controles de transporte: Reproduzca, pause y avance de forma independiente en cada pista."
                )
            ),
            HelpSection(
                id = HelpSectionId.SIMULTANEOUS_SOLO_MUTE,
                title = "3. Reproducción Simultánea, Solo y Mute",
                summary = "Mezcla multipista con aislamiento y silenciamiento instantáneo",
                iconName = "tune",
                bullets = listOf(
                    "Simultaneidad: Todas las pistas activas suenan al mismo tiempo sin distorsión.",
                    "Función Solo: Aísla las pistas seleccionadas y silencia las demás automáticamente.",
                    "Función Mute: Silencia una pista sin detener su línea de tiempo.",
                    "Controles maestros: Controle todas las pistas a la vez desde el panel inferior."
                )
            ),
            HelpSection(
                id = HelpSectionId.EQUALIZER_FREQUENCIES,
                title = "4. Ecualizador, Efectos de Audio y DSP",
                summary = "Ecualizador gráfico multibanda, refuerzo de graves y efectos espaciales",
                iconName = "equalizer",
                bullets = listOf(
                    "Ecualizador gráfico: Ajuste frecuencias bajas, medias y agudas en tiempo real.",
                    "Perfiles listos: Perfiles para Rock, Pop, Jazz, Clásica, Refuerzo de graves y Plano.",
                    "Efectos DSP: Refuerzo de graves (Bass Boost), virtualizador 3D y reverberación ambiental.",
                    "Reinicio rápido: Restablezca todas las bandas a la posición plana con un solo toque."
                )
            ),
            HelpSection(
                id = HelpSectionId.RECORDER_CROSSFADER_SLEEP,
                title = "5. Grabador, Crossfader y Temporizador de Sueño",
                summary = "Grabación de alta fidelidad, mezcla DJ y temporizador persistente",
                iconName = "mic",
                bullets = listOf(
                    "Grabador interno: Grabe audio con temporizador y añádalo directamente como nueva pista.",
                    "Crossfader DJ: Mezcle suavemente el balance de volumen entre dos pistas elegidas.",
                    "Temporizador de sueño: Cuenta regresiva resistente a nivel de motor de audio.",
                    "Acceso desde el encabezado: Indicador en la barra superior, botones +5m / +15m y desvanecimiento suave de 30s."
                )
            ),
            HelpSection(
                id = HelpSectionId.VIDEO_AUDIO_EXTRACTOR,
                title = "6. Extractor de Audio de Video y Recorte Preciso",
                summary = "Extraiga audio de videos de la galería con recorte visual de onda",
                iconName = "video",
                bullets = listOf(
                    "Seleccione cualquier video: Cargue videos locales sin necesidad de permisos complejos.",
                    "Recorte de precisión: Ajuste puntos de inicio y fin con precisión de milisegundos.",
                    "Audición en vivo: Escuche el fragmento recortado antes de exportar.",
                    "Importación directa: Guarde el clip y agréguelo de inmediato a una nueva pista."
                )
            ),
            HelpSection(
                id = HelpSectionId.THEMES_APPEARANCE,
                title = "7. Motor de Temas y Personalización Visual",
                summary = "Paletas modernas, estilo biofílico y almacenamiento persistente de tema",
                iconName = "palette",
                bullets = listOf(
                    "Motor de temas: Cambie entre temas basados en Material Design 3.",
                    "Tema Biofílico por defecto: Estilo botánico verde y tonos piedra para máxima comodidad visual.",
                    "Paletas variadas: Incluye Cyberpunk Cyan, Esmeralda Neón, Violeta Profundo y Azul Océano.",
                    "Persistencia: Su tema preferido se guarda y se restaura al volver a abrir la aplicación."
                )
            ),
            HelpSection(
                id = HelpSectionId.MEDIA_SESSION_HEADSET,
                title = "8. Notificaciones y Auriculares",
                summary = "Controles en pantalla de bloqueo y botones de auriculares",
                iconName = "headphones",
                bullets = listOf(
                    "Notificación multimedia: Controles de reproducción en la barra de notificaciones y bloqueo.",
                    "Botones de auriculares: Soporte para acciones estándar de reproducción, pausa y avance.",
                    "Compatibilidad: El soporte de gestos específicos de Bluetooth varía según el dispositivo."
                )
            ),
            HelpSection(
                id = HelpSectionId.AUDIO_ROUTING_CAR_SYSTEMS,
                title = "9. Enrutamiento de Audio y Modo Telefonía (SCO)",
                summary = "Solución a problemas de reproducción en autos y sistemas de audio Bluetooth",
                iconName = "alt_route",
                bullets = listOf(
                    "Audio en estéreo del automóvil: Ciertos sistemas solo aceptan audio de llamadas telefónicas.",
                    "Modo de Audio Telefónico (SCO): Active esta opción para transmitir la música a través del canal de llamada del auto.",
                    "Selección de Salida: Cambie rápidamente entre Bluetooth, cable AUX, USB o el altavoz del teléfono.",
                    "Acceso rápido: Disponible en la barra superior y en la pantalla de Ajustes."
                )
            )
        )
    )

    private val zhStrings = HelpStrings(
        helpCenterTitle = "帮助中心与指南",
        helpSubtitle = "TO_OT 播放器各项功能与专业工具完整使用指南 (v${AppInfo.VERSION_NAME})",
        searchPlaceholder = "搜索帮助（例如：视频提取、主题、睡眠定时、均衡器）...",
        noResultsFound = "未找到相关主题",
        noResultsSubtitle = "请尝试输入其他关键词或切换语言。",
        close = "关闭",
        selectLanguage = "指南语言",
        sectionsCount = "项帮助主题",
        tipTitle = "实用提示",
        tipDescription = "阅读帮助指南时，背景音乐播放将平稳保持，不会中断。",
        sections = listOf(
            HelpSection(
                id = HelpSectionId.MULTI_LANE,
                title = "1. 多音轨管理 (Multi-Lane)",
                summary = "创建、排序并独立管理多个音频轨道",
                iconName = "queue",
                bullets = listOf(
                    "添加新轨道：点击顶部导航栏中的“+”按钮，新轨道将立即插入到列表顶部。",
                    "长按拖拽排序：长按轨道拖动图标，即可上下自如调整轨道顺序。",
                    "箭头按钮排序：在每个音轨的操作选项中，可使用向上/向下箭头快速移动。",
                    "音频选择：每个轨道均可独立加载本地存储的音频文件或示例生成音频。"
                )
            ),
            HelpSection(
                id = HelpSectionId.LOCAL_FILES_LANE_CONTROLS,
                title = "2. 本地文件与独立轨道控制",
                summary = "自定义轨道名称、独立音量调节与精准播放进度条",
                iconName = "audiotrack",
                bullets = listOf(
                    "设备文件浏览器：轻松扫描并加载内部存储中的各类音频格式文件。",
                    "音轨重命名：为每个音轨设定清晰的名称，便于管理复杂的多轨混音项目。",
                    "独立音量控制：每条轨道拥有 0% 至 100% 独立音量滑块。",
                    "独立传输控制：各轨道支持独立的播放、暂停与进度拖动调节。"
                )
            ),
            HelpSection(
                id = HelpSectionId.SIMULTANEOUS_SOLO_MUTE,
                title = "3. 同步播放、独奏 (Solo) 与静音 (Mute)",
                summary = "多轨混音器，支持一键隔离目标轨道或瞬间消音",
                iconName = "tune",
                bullets = listOf(
                    "同步并发播放：所有激活轨道实时同相播放，无杂音与音频损失。",
                    "独奏功能 (Solo)：激活 Solo 将使选定轨道保持发声，其他轨道自动静音。",
                    "静音功能 (Mute)：瞬时切断指定音轨的声音，而不停止其播放时间轴。",
                    "全局播放控制：底部控制面板支持一键同时播放或暂停所有音轨。"
                )
            ),
            HelpSection(
                id = HelpSectionId.EQUALIZER_FREQUENCIES,
                title = "4. 均衡器、音效与 DSP",
                summary = "多频段图形均衡器、低音增强与 3D 环绕音效",
                iconName = "equalizer",
                bullets = listOf(
                    "图形均衡器：实时精准调节低频、中频与高频各频段参数。",
                    "内置音效预设：一键应用摇滚、流行、爵士、古典、低音增强与人声强化预设。",
                    "DSP 空间音效：动态低音增强、虚拟环绕立体声及环境混响效果。",
                    "一键重置：轻按重置按钮即可迅速将各频段曲线归零平衡。"
                )
            ),
            HelpSection(
                id = HelpSectionId.RECORDER_CROSSFADER_SLEEP,
                title = "5. 录音机、交叉混音与智能睡眠定时",
                summary = "高保真内录、DJ 交叉渐变及顶部常驻休眠倒计时",
                iconName = "mic",
                bullets = listOf(
                    "内置录音机：实时录制音频，配有计时器与电平显示，录制完成后可直接插入新音轨。",
                    "双轨交叉渐变 (Crossfader)：DJ 风格平滑调节两条选定音轨之间的音量平衡。",
                    "智能睡眠定时器：引擎级倒计时，切换页面不中断，并在结束前 30 秒平滑淡出。",
                    "顶部控制与快速延时：顶部栏常驻显示剩余倒计时，支持一键 +5 分钟或 +15 分钟。"
                )
            ),
            HelpSection(
                id = HelpSectionId.VIDEO_AUDIO_EXTRACTOR,
                title = "6. 视频提取音频与精准剪辑",
                summary = "从相册视频中提取无损音频，波形精准裁剪并导入项目",
                iconName = "video",
                bullets = listOf(
                    "选择本地视频：直接浏览相册中的视频文件进行快速音频提取。",
                    "毫秒级精准裁剪：通过可视波形拖动起点与终点，精确截取目标片段。",
                    "实时试听：在导出前可实时预览裁剪音频片段。",
                    "一键导入项目：保存提取的音频片段并直接插入为新音轨。"
                )
            ),
            HelpSection(
                id = HelpSectionId.THEMES_APPEARANCE,
                title = "7. 主题引擎与视觉个性化",
                summary = "现代配色、自然绿意风格与持久化主题设置",
                iconName = "palette",
                bullets = listOf(
                    "主题引擎：在多种现代 Material 3 色彩风格之间自由切换。",
                    "默认自然绿意主题 (Biophilic Green)：温润的植物绿与岩石质感，提供舒适视觉体验。",
                    "丰富配色方案：包含赛博青蓝、霓虹翡翠、深邃幽紫和深海湛蓝。",
                    "持久保存：所选主题自动保存在设备本地，下次启动保持不变。"
                )
            ),
            HelpSection(
                id = HelpSectionId.MEDIA_SESSION_HEADSET,
                title = "8. 系统媒体通知与耳机控制",
                summary = "锁屏通知栏控制以及线控耳机与蓝牙按键集成",
                iconName = "headphones",
                bullets = listOf(
                    "锁屏媒体通知：在通知抽屉和锁屏界面直接控制播放、暂停与切歌。",
                    "耳机物理按键：支持标准单按（播放/暂停）、双击（下一曲）及三击操作。",
                    "硬件兼容性说明：不同品牌蓝牙耳机的按键协议因系统和固件可能存在差异。"
                )
            ),
            HelpSection(
                id = HelpSectionId.AUDIO_ROUTING_CAR_SYSTEMS,
                title = "9. 音频输出路由与车载电话 (SCO) 模式",
                summary = "解决车载音响或蓝牙设备无法发声的问题并自由切换输出端",
                iconName = "alt_route",
                bullets = listOf(
                    "车载音响无声问题：部分车载音响仅接收通话通道音频，不接受普通 A2DP 音乐流。",
                    "电话音频 (SCO 模式)：在音频路由面板中开启此开关，即可将音乐伪装为通话音频传输至车机并顺畅播放。",
                    "手动切换输出：在蓝牙、AUX 有线耳机、USB 音频及手机内置扬声器之间一键无缝切换。",
                    "快捷访问：顶部栏主题按钮旁及设置页面均设有音频路由入口。"
                )
            )
        )
    )

    private val hiStrings = HelpStrings(
        helpCenterTitle = "सहायता केंद्र और गाइड",
        helpSubtitle = "TO_OT प्लेयर के सभी फीचर्स, टूल्स और सेटिंग्स की सम्पूर्ण जानकारी (v${AppInfo.VERSION_NAME})",
        searchPlaceholder = "गाइड में खोजें (जैसे: वीडियो एक्सट्रैक्टर, थीम, स्लीप टाइमर, EQ)...",
        noResultsFound = "कोई परिणाम नहीं मिला",
        noResultsSubtitle = "कृपया किसी अन्य शब्द से खोजें या भाषा बदलें।",
        close = "बंद करें",
        selectLanguage = "गाइड की भाषा",
        sectionsCount = "सहायता विषय",
        tipTitle = "उपयोगी सुझाव",
        tipDescription = "गाइड पढ़ते समय बैकग्राउंड में ऑडियो प्लेबैक सामान्य रूप से जारी रहेगा।",
        sections = listOf(
            HelpSection(
                id = HelpSectionId.MULTI_LANE,
                title = "1. मल्टी-लेन प्रबंधन (Multi-Lane)",
                summary = "स्वतंत्र ऑडियो ट्रैक्स जोड़ें, व्यवस्थित करें और चलाएं",
                iconName = "queue",
                bullets = listOf(
                    "नया लेन जोड़ें: शीर्ष बार में '+' बटन दबाकर सूची के शीर्ष पर एक नया ट्रैक जोड़ें।",
                    "ड्रैग एंड ड्रॉप: ट्रैक को ऊपर या नीचे ले जाने के लिए हैंडल को दबाकर खींचें।",
                    "बटन द्वारा क्रम बदलें: लेन के विकल्पों में ऊपर/नीचे तीर बटनों का उपयोग करें।",
                    "ऑडियो चयन: प्रत्येक लेन अपने डिवाइस स्टोरेज से अलग ऑडियो फाइल लोड कर सकता है।"
                )
            ),
            HelpSection(
                id = HelpSectionId.LOCAL_FILES_LANE_CONTROLS,
                title = "2. स्थानीय फाइलें और लेन नियंत्रण",
                summary = "कस्टम नाम, स्वतंत्र वॉल्यूम और अलग प्लेबैक नियंत्रण",
                iconName = "audiotrack",
                bullets = listOf(
                    "डिवाइस एक्सप्लोरर: आंतरिक स्टोरेज से ऑडियो फाइलें खोजें और लोड करें।",
                    "लेन का नाम बदलें: ट्रैक्स को व्यवस्थित रखने के लिए अपनी पसंद का नाम दें।",
                    "स्वतंत्र वॉल्यूम: प्रत्येक लेन के लिए 0% से 100% तक अलग वॉल्यूम स्लाइडर।",
                    "स्वतंत्र नियंत्रण: हर ट्रैक को बिना किसी रुकावट के अलग से चलाएं या पॉज़ करें।"
                )
            ),
            HelpSection(
                id = HelpSectionId.SIMULTANEOUS_SOLO_MUTE,
                title = "3. एक साथ प्लेबैक, सोलो और म्यूट",
                summary = "पेशेवर मिक्सर के साथ एक साथ चलाएं या ट्रैक म्यूट करें",
                iconName = "tune",
                bullets = listOf(
                    "एक साथ प्लेबैक: सभी सक्रिय ट्रैक्स बिना किसी ध्वनि हानि के एक साथ बजते हैं।",
                    "सोलो फीचर (Solo): केवल चुने हुए ट्रैक्स की आवाज सुनें, बाकी स्वतः म्यूट हो जाते हैं।",
                    "म्यूट फीचर (Mute): प्लेबैक रोके बिना किसी भी ट्रैक की आवाज बंद करें।",
                    "मास्टर कंट्रोल: नीचे दिए गए कंट्रोल पैनल से सभी ट्रैक्स को एक साथ चलाएं या रोकें।"
                )
            ),
            HelpSection(
                id = HelpSectionId.EQUALIZER_FREQUENCIES,
                title = "4. इक्वलाइज़र, ऑडियो इफेक्ट्स और DSP",
                summary = "मल्टी-बैंड इक्वलाइज़र, बास बूस्ट और 3D स्थानिक ध्वनि",
                iconName = "equalizer",
                bullets = listOf(
                    "ग्राफिक इक्वलाइज़र: कम, मध्यम और उच्च आवृत्तियों को रीयल-टाइम में समायोजित करें।",
                    "रेडी प्रीसेट्स: रॉक, पॉप, जैज़, क्लासिकल और बास बूस्ट जैसे रेडीमेड मोड्स।",
                    "DSP इफेक्ट्स: बास बूस्ट, 3D ऑडियो वर्चुअलाइज़र और पर्यावरण रिवर्ब।",
                    "त्वरित रीसेट: एक टच में सभी सेटिंग्स को सामान्य स्तर पर वापस लाएं।"
                )
            ),
            HelpSection(
                id = HelpSectionId.RECORDER_CROSSFADER_SLEEP,
                title = "5. रिकॉर्डर, क्रॉसफेडर और स्लीप टाइमर",
                summary = "उच्च गुणवत्ता रिकॉर्डिंग, दो-लेन मिक्सिंग और स्वचालित शटडाउन",
                iconName = "mic",
                bullets = listOf(
                    "आंतरिक रिकॉर्डर: टाइमर के साथ ऑडियो रिकॉर्ड करें और तुरंत नए लेन के रूप में जोड़ें।",
                    "डीजे क्रॉसफेडर: दो ट्रैक्स के बीच वॉल्यूम बैलेंस को आसानी से मिश्रित करें।",
                    "स्थिर स्लीप टाइमर: स्क्रीन बदलने पर भी टाइमर जारी रहता है और 30 सेकंड का स्मूथ फेड-आउट प्रदान करता है।",
                    "हेडर कंट्रोल और एक्सटेंशन: शीर्ष बार में लाइव पिल और +5m / +15m क्विक बटन।"
                )
            ),
            HelpSection(
                id = HelpSectionId.VIDEO_AUDIO_EXTRACTOR,
                title = "6. वीडियो से ऑडियो निष्कर्षण और सटीक ट्रिमिंग",
                summary = "गैलरी वीडियो से ऑडियो निकालें, वेवफॉर्म ट्रिम करें और प्रोजेक्ट में जोड़ें",
                iconName = "video",
                bullets = listOf(
                    "वीडियो चुनें: अपनी डिवाइस गैलरी से वीडियो फाइल्स लोड करें।",
                    "सटीक ट्रिमिंग: मिलीसेकंड स्तर पर शुरुआती और अंतिम बिंदु सेट करें।",
                    "लाइव प्रीव्यू: एक्सपोर्ट करने से पहले ट्रिम किए गए हिस्से को सुनें।",
                    "सीधा इम्पोर्ट: एक्सट्रैक्ट किए गए ऑडियो को सीधे नए ट्रैक के रूप में जोड़ें।"
                )
            ),
            HelpSection(
                id = HelpSectionId.THEMES_APPEARANCE,
                title = "7. थीम इंजन और विज़ुअल कस्टमाइज़ेशन",
                summary = "आधुनिक कलर पैलेट्स और स्थायी थीम सेटिंग्स",
                iconName = "palette",
                bullets = listOf(
                    "थीम इंजन: मटीरियल डिज़ाइन 3 पर आधारित आकर्षक थीम्स चुनें।",
                    "बायोफिलिक डिफ़ॉल्ट थीम: सुखदायक हरे और प्राकृतिक रंग आंखों के आराम के लिए।",
                    "विविध पैलेट्स: साइबरपंक सियान, नियॉन एमराल्ड, डीप वायलेट और ओशन ब्लू।",
                    "स्थायी स्टोरेज: आपकी चुनी हुई थीम ऐप बंद होने के बाद भी सुरक्षित रहती है।"
                )
            ),
            HelpSection(
                id = HelpSectionId.MEDIA_SESSION_HEADSET,
                title = "8. नोटिफिकेशन और हेडसेट नियंत्रण",
                summary = "सिस्टम नोटिफिकेशन और हेडफोन बटन से नियंत्रण",
                iconName = "headphones",
                bullets = listOf(
                    "मीडिया नोटिफिकेशन: लॉक स्क्रीन और नोटिफिकेशन बार से प्ले, पॉज़ और ट्रैक बदलें।",
                    "हेडसेट बटन्स: वायर्ड या ब्लूटूथ ईयरबड्स से प्ले/पॉज़ और नेक्स्ट ट्रैक कमांड।",
                    "अनुकूलता: विभिन्न ब्लूटूथ डिवाइसों के अनुसार प्रतिक्रिया भिन्न हो सकती है।"
                )
            ),
            HelpSection(
                id = HelpSectionId.AUDIO_ROUTING_CAR_SYSTEMS,
                title = "9. ऑडियो रूटिंग और कार मोड (SCO)",
                summary = "कार स्टीरियो और ब्लूटूथ सिस्टम में प्लेबैक समस्या का समाधान",
                iconName = "alt_route",
                bullets = listOf(
                    "कार स्टीरियो प्लेबैक समस्या: कुछ कार स्टीरियो केवल फ़ोन कॉल ऑडियो स्वीकार करते हैं।",
                    "फ़ोन ऑडियो (SCO मोड): इसे चालू करके संगीत को कॉल चैनल के माध्यम से कार स्पीकर्स पर बजाएं।",
                    "आउटपुट चयन: ब्लूटूथ, AUX केबल, USB और फ़ोन स्पीकर में आसानी से बदलें।",
                    "शीर्ष बार और सेटिंग्स दोनों से तुरंत एक्सेस करें।"
                )
            )
        )
    )

    private val ptStrings = HelpStrings(
        helpCenterTitle = "Central de Ajuda e Guia",
        helpSubtitle = "Guia completo de recursos, ferramentas e fluxo de trabalho do TO_OT Player (v${AppInfo.VERSION_NAME})",
        searchPlaceholder = "Pesquisar no guia (ex.: Extrator de vídeo, Tema, Timer, EQ)...",
        noResultsFound = "Nenhum tópico encontrado",
        noResultsSubtitle = "Tente pesquisar com outro termo ou selecione outro idioma.",
        close = "Fechar",
        selectLanguage = "Idioma do Guia",
        sectionsCount = "Tópicos de Ajuda",
        tipTitle = "Dica Útil",
        tipDescription = "A reprodução de áudio continuará normalmente enquanto consulta este guia.",
        sections = listOf(
            HelpSection(
                id = HelpSectionId.MULTI_LANE,
                title = "1. Gerenciamento Multi-Faixas",
                summary = "Crie, organize e gerencie faixas de áudio independentes",
                iconName = "queue",
                bullets = listOf(
                    "Adicionar nova faixa: Toque no botão '+' na barra superior para inserir uma nova faixa no topo da lista.",
                    "Reordenar por arrasto: Pressione e arraste o identificador de qualquer faixa para reposicioná-la.",
                    "Reordenar por botões: Use as setas para cima/para baixo dentro das opções da faixa.",
                    "Seleção de arquivos: Cada faixa pode carregar arquivos de áudio locais de forma independente."
                )
            ),
            HelpSection(
                id = HelpSectionId.LOCAL_FILES_LANE_CONTROLS,
                title = "2. Arquivos Locais e Controles",
                summary = "Nomes personalizados, volume individual e reprodução independente",
                iconName = "audiotrack",
                bullets = listOf(
                    "Explorador de arquivos: Pesquise e carregue faixas diretamente do armazenamento interno.",
                    "Renombrar faixas: Personalize o nome de cada faixa para manter seus projetos organizados.",
                    "Volume dedicado: Cada faixa possui controle deslizante de volume de 0% a 100%.",
                    "Controles de transporte: Reproduza, pause e navegue pelo áudio de forma independente."
                )
            ),
            HelpSection(
                id = HelpSectionId.SIMULTANEOUS_SOLO_MUTE,
                title = "3. Reprodução Simultânea, Solo e Mute",
                summary = "Mixagem multipista com isolamento e silenciamento instantâneo",
                iconName = "tune",
                bullets = listOf(
                    "Simultaneidade: Todas as faixas ativas tocam ao mesmo tempo sem distorção.",
                    "Função Solo: Isola as faixas selecionadas e silencia as demais automaticamente.",
                    "Função Mute: Silencia uma faixa sem interromper sua linha do tempo.",
                    "Controles mestres: Controle todas as faixas juntas a partir do painel inferior."
                )
            ),
            HelpSection(
                id = HelpSectionId.EQUALIZER_FREQUENCIES,
                title = "4. Equalizador, Efeitos de Áudio e DSP",
                summary = "Equalizador gráfico multibanda, reforço de graves e virtualizador 3D",
                iconName = "equalizer",
                bullets = listOf(
                    "Equalizador gráfico: Ajuste frequências graves, médias e agudas em tempo real.",
                    "Perfis prontos: Perfis para Rock, Pop, Jazz, Clássico, Reforço de Graves e Plano.",
                    "Efeitos DSP: Reforço dinâmico de graves (Bass Boost), áudio 3D e reverberação.",
                    "Redefinição rápida: Restaure todas as bandas para a curva plana com um único toque."
                )
            ),
            HelpSection(
                id = HelpSectionId.RECORDER_CROSSFADER_SLEEP,
                title = "5. Gravador, Crossfader e Timer de Sono",
                summary = "Gravação de alta fidelidade, mixagem DJ e temporizador resiliente",
                iconName = "mic",
                bullets = listOf(
                    "Gravador interno: Grave áudio com cronômetro e adicione diretamente como nova faixa.",
                    "Crossfader DJ: Misture suavemente o equilíbrio de volume entre duas faixas escolhidas.",
                    "Timer de sono resiliente: Contagem contínua no motor de áudio com fade-out suave de 30 segundos.",
                    "Acesso no cabeçalho e extensão: Indicador na barra superior e botões rápidos de +5m / +15m."
                )
            ),
            HelpSection(
                id = HelpSectionId.VIDEO_AUDIO_EXTRACTOR,
                title = "6. Extrator de Áudio de Vídeo e Corte Preciso",
                summary = "Extraia áudio de vídeos da galeria com corte de forma de onda",
                iconName = "video",
                bullets = listOf(
                    "Escolha qualquer vídeo: Selecione vídeos da galeria com acesso rápido e seguro.",
                    "Corte preciso: Ajuste marcadores de início e fim com precisão milimétrica.",
                    "Prévia em tempo real: Ouça o trecho antes de salvar.",
                    "Importação direta: Salve o áudio extraído e carregue-o imediatamente em uma nova faixa."
                )
            ),
            HelpSection(
                id = HelpSectionId.THEMES_APPEARANCE,
                title = "7. Motor de Temas e Personalização Visual",
                summary = "Paletas modernas, estilo biofílico e temas persistentes",
                iconName = "palette",
                bullets = listOf(
                    "Motor de temas: Alterne livremente entre temas inspirados no Material 3.",
                    "Tema Biofílico padrão: Tons botânicos e texturas naturais para descanso visual.",
                    "Paletas expressivas: Inclui Cyberpunk Cyan, Esmeralda Neon, Violeta Profundo e Azul Oceano.",
                    "Salvamento persistente: O tema escolhido fica salvo no dispositivo para próximas sessões."
                )
            ),
            HelpSection(
                id = HelpSectionId.MEDIA_SESSION_HEADSET,
                title = "8. Notificações e Fones de Ouvido",
                summary = "Controles na tela de bloqueio e botões de fones de ouvido",
                iconName = "headphones",
                bullets = listOf(
                    "Notificação multimídia: Controles na barra de notificações e tela de bloqueio.",
                    "Botones de fones: Suporte para ações padrão de reproduzir, pausar e avançar faixa.",
                    "Compatibilidade: O comportamento de fones Bluetooth pode variar conforme o modelo e versão do sistema."
                )
            ),
            HelpSection(
                id = HelpSectionId.AUDIO_ROUTING_CAR_SYSTEMS,
                title = "9. Roteamento de Áudio e Modo Telefonia (SCO)",
                summary = "Solução para reprodução em som automotivo e seleção de saídas de áudio",
                iconName = "alt_route",
                bullets = listOf(
                    "Som automotivo sem áudio: Alguns aparelhos de som só aceitam áudio no canal de chamada telefônica.",
                    "Modo de Áudio Telefônico (SCO): Ative esta opção para direcionar o som pelo canal de voz do som automotivo.",
                    "Seleção rápida de saída: Alterne facilmente entre Bluetooth, cabo AUX, USB e alto-falante.",
                    "Acesso prático: Atalho na barra superior ao lado do tema e na tela de Ajustes."
                )
            )
        )
    )
}
