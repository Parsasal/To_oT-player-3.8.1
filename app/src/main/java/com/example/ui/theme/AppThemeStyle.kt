package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 11 Unique Visual Design Skins for TO_OT Player
 * 1. COSMIC_AURORA: Flowing neon aurora waves, glowing glass panels, cyan/magenta accents.
 * 2. EARTHY_BIOPHILIC: Sage olive moss marble, parchment cards with organic leaf watermarks, terracotta buttons.
 * 3. MINIMALIST_MONOCHROME: Pure OLED Black, razor-sharp white wireframes, clean vector line art.
 * 4. RETRO_ANALOG: Brushed champagne gold header, dark walnut wood grain, working backlit analog amber VU meters with moving dB needle & brass knobs.
 * 5. RETRO_TERMINAL: CRT matrix green phosphor glow, scanline raster overlay, monospace ASCII syntax [[*]], [CMD:LIST], #########.
 * 6. MODERN_BRUTALISM: Neo-brutalism vibrant yellow & solid black, 2.5dp thick borders, hard offset black drop-shadows.
 * 7. CLAYMORPHIC_PASTEL: Puffy 3D pastel clay cards (Lavender, Mint, Coral), gold embossed sculpted icons, soft pill buttons.
 * 8. HOLOGRAPHIC_JELLY: Translucent obsidian glass with multi-color rainbow iridescent rim glow and shiny jelly artwork.
 * 9. NEUMORPHIC_GLASS: Off-white soft neumorphic extruded surfaces, subtle cyan/turquoise/lavender ambient glow outlines.
 * 10. CYBERPUNK_NEON: Sci-Fi cyber HUD, angular bracketed frames, glowing cyan circuit traces, tech indicators.
 * 11. DARK_LUXURY: Obsidian faceted crystal canvas, matte black panels with double metallic gold borders, satin golden sheen.
 */
enum class AppThemeStyle(
    val themeId: String,
    val titleFa: String,
    val titleEn: String,
    val subtitleFa: String,
    val subtitleEn: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val bgStartDark: Color,
    val bgCenterDark: Color,
    val bgEndDark: Color,
    val bgStartLight: Color,
    val bgCenterLight: Color,
    val bgEndLight: Color,
    val primaryAccentLight: Color,
    val secondaryAccentLight: Color,
    val waveformColors: List<Color>,
    val radiusPanelDp: Float = 20f,
    val radiusControlDp: Float = 12f,
    val hasVUMeter: Boolean = false,
    val hasHardOffsetShadow: Boolean = false,
    val hasScanlineOverlay: Boolean = false,
    val hasTerminalAscii: Boolean = false,
    val hasPuffy3DHighlight: Boolean = false,
    val hasNeonHalo: Boolean = false,
    val hasMetallicSheen: Boolean = false,
    val hasNeumorphicDepth: Boolean = false,
    val hasIridescentRim: Boolean = false,
    val hasWoodTexture: Boolean = false,
    val hasCyberHud: Boolean = false,
    val isWireframeMonochrome: Boolean = false
) {
    // 1. Cosmic Aurora & Waves (شفق قطبی کیهانی) - Image 1
    COSMIC_AURORA(
        themeId = "cosmic-aurora",
        titleFa = "شفق قطبی کیهانی (سایبر ویو)",
        titleEn = "Cosmic Aurora & Waves",
        subtitleFa = "بوم شب کیهانی با امواج شفق قطبی، پنل‌های شیشه‌ای با حاشیه نئونی دوگانه و نشانگرهای درخشان",
        subtitleEn = "Cosmic night canvas with flowing aurora smoke waves, dual neon glow glass panels & vibrant highlights",
        primaryAccent = Color(0xFF00F5D4), // Cyan Aurora
        secondaryAccent = Color(0xFFA78BFA), // Lavender / Purple Aurora
        bgStartDark = Color(0xFF0B0E23),
        bgCenterDark = Color(0xFF141838),
        bgEndDark = Color(0xFF080A1A),
        bgStartLight = Color(0xFFEEF2FF),
        bgCenterLight = Color(0xFFE0E7FF),
        bgEndLight = Color(0xFFC7D2FE),
        primaryAccentLight = Color(0xFF0891B2),
        secondaryAccentLight = Color(0xFF7C3AED),
        waveformColors = listOf(
            Color(0xFF00F5D4),
            Color(0xFF38BDF8),
            Color(0xFF818CF8),
            Color(0xFFA78BFA),
            Color(0xFFC084FC),
            Color(0xFF00F5D4)
        ),
        radiusPanelDp = 22f,
        radiusControlDp = 14f,
        hasNeonHalo = true
    ),

    // 2. Earthy & Biophilic (پوستی و ارگانیک) - Image 2
    EARTHY_BIOPHILIC(
        themeId = "earthy-biophilic",
        titleFa = "پوستی و گیاهی (بیوفیلیک)",
        titleEn = "Earthy & Biophilic",
        subtitleFa = "بوم سنگ مرمر سبز زیتونی، کارت‌های سنگی پوستی با واتر‌مارک برگ‌های طبیعی و دکمه‌های تراکوتا",
        subtitleEn = "Forest olive green marble canvas with warm parchment stone cards & botanical leaf accents",
        primaryAccent = Color(0xFFC86D51), // Warm Terracotta
        secondaryAccent = Color(0xFF7E9F59), // Sage Moss Green
        bgStartDark = Color(0xFF1B261A),
        bgCenterDark = Color(0xFF263625),
        bgEndDark = Color(0xFF141D13),
        bgStartLight = Color(0xFFF7F5EE),
        bgCenterLight = Color(0xFFEDE8DC),
        bgEndLight = Color(0xFFE2DCCE),
        primaryAccentLight = Color(0xFFB45309),
        secondaryAccentLight = Color(0xFF5E7345),
        waveformColors = listOf(
            Color(0xFFC86D51),
            Color(0xFFD97757),
            Color(0xFFE89274),
            Color(0xFF8FA96D),
            Color(0xFF7E9F59),
            Color(0xFFC86D51)
        ),
        radiusPanelDp = 22f,
        radiusControlDp = 14f
    ),

    // 3. Minimalist Monochrome (مونوکروم وایر‌فریم) - Image 3
    MINIMALIST_MONOCHROME(
        themeId = "minimalist-monochrome",
        titleFa = "مونوکروم وایر‌فریم (OLED)",
        titleEn = "Minimalist OLED Wireframe",
        subtitleFa = "بوم مشکی مطلق OLED، خطوط هندسی سفید با کنتراست بالا و آیکون‌های وکتور خطی مینیمال",
        subtitleEn = "Pure OLED black canvas with razor-sharp geometric white wireframes and clean vector lines",
        primaryAccent = Color(0xFFFFFFFF),
        secondaryAccent = Color(0xFFE0E0E0),
        bgStartDark = Color(0xFF000000),
        bgCenterDark = Color(0xFF000000),
        bgEndDark = Color(0xFF000000),
        bgStartLight = Color(0xFFFFFFFF),
        bgCenterLight = Color(0xFFF8F9FA),
        bgEndLight = Color(0xFFE9ECEF),
        primaryAccentLight = Color(0xFF000000),
        secondaryAccentLight = Color(0xFF333333),
        waveformColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE0E0E0),
            Color(0xFFCCCCCC),
            Color(0xFFFFFFFF),
            Color(0xFFE0E0E0),
            Color(0xFFFFFFFF)
        ),
        radiusPanelDp = 18f,
        radiusControlDp = 12f,
        isWireframeMonochrome = true
    ),

    // 4. Vintage Analog Hi-Fi & VU Meters (آنالوگ کلاسیک و گیج VU) - Image 4
    RETRO_ANALOG(
        themeId = "retro-analog",
        titleFa = "آنالوگ کلاسیک و گیج VU",
        titleEn = "Vintage Analog Hi-Fi",
        subtitleFa = "سربرگ طلای شامپاینی، بدنه چوب گردوی تیره، گیج‌های آنالوگ متحرک VU با نور پس‌زمینه کهربایی و ولوم‌های برنجی",
        subtitleEn = "Brushed champagne gold trim, dark walnut wood panels, backlit amber VU meters with moving dB needle & brass knobs",
        primaryAccent = Color(0xFFD4AF37), // Brass Gold
        secondaryAccent = Color(0xFFF39C12), // Warm Amber Glow
        bgStartDark = Color(0xFF1E1710),
        bgCenterDark = Color(0xFF2A2016),
        bgEndDark = Color(0xFF140F0A),
        bgStartLight = Color(0xFFF5EFE0),
        bgCenterLight = Color(0xFFEAE0C8),
        bgEndLight = Color(0xFFDFD2B2),
        primaryAccentLight = Color(0xFFB8860B),
        secondaryAccentLight = Color(0xFFD97706),
        waveformColors = listOf(
            Color(0xFFF39C12),
            Color(0xFFE67E22),
            Color(0xFFD4AF37),
            Color(0xFFF1C40F),
            Color(0xFFE67E22),
            Color(0xFFF39C12)
        ),
        radiusPanelDp = 18f,
        radiusControlDp = 10f,
        hasVUMeter = true,
        hasWoodTexture = true,
        hasMetallicSheen = true
    ),

    // 5. Retro CRT Matrix Terminal (ترمینال واید اسکرین CRT) - Image 5
    RETRO_TERMINAL(
        themeId = "retro-terminal",
        titleFa = "ترمینال واید اسکرین CRT",
        titleEn = "Retro CRT Matrix Terminal",
        subtitleFa = "درخشش فسفری سبز ماتریکس، اسکن‌لاین‌های لامپ تصویر CRT، تایپوگرافی مونو‌اسپیس با دستورات ASCII [[*]] و #########",
        subtitleEn = "Phosphor matrix green CRT glow, raster scanline overlay, monospace ASCII syntax [[*]], [CMD:LIST], #########",
        primaryAccent = Color(0xFF33FF66), // Phosphor CRT Green
        secondaryAccent = Color(0xFF00FF88),
        bgStartDark = Color(0xFF020904),
        bgCenterDark = Color(0xFF051208),
        bgEndDark = Color(0xFF010603),
        bgStartLight = Color(0xFFE6F9EC),
        bgCenterLight = Color(0xFFD0F4DA),
        bgEndLight = Color(0xFFB3ECC2),
        primaryAccentLight = Color(0xFF059669),
        secondaryAccentLight = Color(0xFF10B981),
        waveformColors = listOf(
            Color(0xFF33FF66),
            Color(0xFF00FF88),
            Color(0xFF33FF66),
            Color(0xFF66FF99),
            Color(0xFF00FF88),
            Color(0xFF33FF66)
        ),
        radiusPanelDp = 8f,
        radiusControlDp = 4f,
        hasScanlineOverlay = true,
        hasTerminalAscii = true
    ),

    // 6. Neo-Brutalism Bold (نئو بروتالیسم زرد و مشکی) - Image 6
    MODERN_BRUTALISM(
        themeId = "modern-brutalism",
        titleFa = "نئو بروتالیسم زرد و مشکی",
        titleEn = "Neo-Brutalism Bold",
        subtitleFa = "پس‌زمینه زرد الکتریکی، حاشیه‌های ضخیم مشکی توپر، سایه‌های سخت بدون مات‌شدگی و دکمه‌های کادری بولد",
        subtitleEn = "Vibrant electric yellow canvas, solid black 2.5dp borders, hard offset black drop-shadows & chunky rectangular buttons",
        primaryAccent = Color(0xFF000000), // Solid Black
        secondaryAccent = Color(0xFFFFE500), // Electric Yellow
        bgStartDark = Color(0xFFFFE500),
        bgCenterDark = Color(0xFFFFE500),
        bgEndDark = Color(0xFFFFE500),
        bgStartLight = Color(0xFFFFEE33),
        bgCenterLight = Color(0xFFFFEE33),
        bgEndLight = Color(0xFFFFEE33),
        primaryAccentLight = Color(0xFF000000),
        secondaryAccentLight = Color(0xFFFFE500),
        waveformColors = listOf(
            Color(0xFF000000),
            Color(0xFF1F1F1F),
            Color(0xFF000000),
            Color(0xFF333333),
            Color(0xFF000000),
            Color(0xFF1F1F1F)
        ),
        radiusPanelDp = 0f,
        radiusControlDp = 0f,
        hasHardOffsetShadow = true
    ),

    // 7. Claymorphic Pastel 3D (کلیمورفیک پاستلی سه‌بعدی) - Image 7
    CLAYMORPHIC_PASTEL(
        themeId = "claymorphic-pastel",
        titleFa = "کلیمورفیک پاستلی سه‌بعدی",
        titleEn = "Claymorphic Pastel 3D",
        subtitleFa = "بوم پاستلی اسطوخودوسی، کارت‌های پفکی سه‌بعدی نرم (یاسی، نعنایی و مرجانی) با آیکون‌های طلایی برجسته",
        subtitleEn = "Lavender pastel canvas with soft 3D puffy clay cards (Lavender, Mint, Coral), gold embossed icons & soft pill buttons",
        primaryAccent = Color(0xFFD4AF37), // Gold Accents on Clay
        secondaryAccent = Color(0xFF9D84B7), // Soft Lavender Purple
        bgStartDark = Color(0xFFEAE5F3),
        bgCenterDark = Color(0xFFE3DCED),
        bgEndDark = Color(0xFFD8D0E5),
        bgStartLight = Color(0xFFF6F2FC),
        bgCenterLight = Color(0xFFEFE8F8),
        bgEndLight = Color(0xFFE5DCF2),
        primaryAccentLight = Color(0xFFB8860B),
        secondaryAccentLight = Color(0xFF805AD5),
        waveformColors = listOf(
            Color(0xFFD4AF37),
            Color(0xFFA78BFA),
            Color(0xFF34D399),
            Color(0xFFF472B6),
            Color(0xFFD4AF37),
            Color(0xFFA78BFA)
        ),
        radiusPanelDp = 24f,
        radiusControlDp = 16f,
        hasPuffy3DHighlight = true
    ),

    // 8. Holographic Jelly & Iridescent (شیشه‌ای هولوگرافیک و ژله‌ای) - Image 8
    HOLOGRAPHIC_JELLY(
        themeId = "holographic-jelly",
        titleFa = "شیشه‌ای هولوگرافیک و ژله‌ای",
        titleEn = "Holographic Jelly & Iridescent",
        subtitleFa = "پنل‌های شیشه‌ای ترنسلوسنت آبسیدین با حاشیه رنگین‌کمانی هولوگرافیک، بازتاب آینه‌ای و آیکون‌های ژله‌ای سه‌بعدی",
        subtitleEn = "Translucent obsidian glass with iridescent rainbow rim glow, specular reflection & 3D glossy jelly artwork",
        primaryAccent = Color(0xFF00F5D4), // Neon Electric Cyan
        secondaryAccent = Color(0xFFE056FD), // Neon Magenta
        bgStartDark = Color(0xFF080614),
        bgCenterDark = Color(0xFF160F2B),
        bgEndDark = Color(0xFF060410),
        bgStartLight = Color(0xFFFDF2F8),
        bgCenterLight = Color(0xFFFCE7F3),
        bgEndLight = Color(0xFFFBCFE8),
        primaryAccentLight = Color(0xFF00B4D8),
        secondaryAccentLight = Color(0xFFDB2777),
        waveformColors = listOf(
            Color(0xFF00F5D4),
            Color(0xFF38BDF8),
            Color(0xFFE056FD),
            Color(0xFFA78BFA),
            Color(0xFFF472B6),
            Color(0xFF00F5D4)
        ),
        radiusPanelDp = 24f,
        radiusControlDp = 16f,
        hasIridescentRim = true,
        hasPuffy3DHighlight = true
    ),

    // 9. Neumorphic Soft Light & Glow (نیومورفیک ملایم و های‌لایت) - Image 9
    NEUMORPHIC_GLASS(
        themeId = "neumorphic-glass",
        titleFa = "نیومورفیک ملایم و های‌لایت",
        titleEn = "Neumorphic Soft Light",
        subtitleFa = "بوم نقره‌ای-سفید با سطوح برجسته نرم نیومورفیک، سایه‌های دوگانه ملایم، هاله‌های رنگی فیروزه‌ای/بنفش و دکمه‌های فرورفته",
        subtitleEn = "Soft off-white extruded surfaces with dual-light neumorphic bevels, cyan/lavender ambient glow outlines & embossed controls",
        primaryAccent = Color(0xFF06B6D4), // Cyan Glow
        secondaryAccent = Color(0xFF8B5CF6), // Purple Glow
        bgStartDark = Color(0xFFE8EEF5),
        bgCenterDark = Color(0xFFDFE7F0),
        bgEndDark = Color(0xFFD5E0EB),
        bgStartLight = Color(0xFFF4F7FB),
        bgCenterLight = Color(0xFFEBF1F7),
        bgEndLight = Color(0xFFE2EAF2),
        primaryAccentLight = Color(0xFF0891B2),
        secondaryAccentLight = Color(0xFF7C3AED),
        waveformColors = listOf(
            Color(0xFF06B6D4),
            Color(0xFF10B981),
            Color(0xFF8B5CF6),
            Color(0xFF06B6D4),
            Color(0xFF3B82F6),
            Color(0xFF8B5CF6)
        ),
        radiusPanelDp = 20f,
        radiusControlDp = 12f,
        hasNeumorphicDepth = true
    ),

    // 10. Cyberpunk HUD & Sci-Fi (سایبرپانک HUD و سای‌فای) - Image 10
    CYBERPUNK_NEON(
        themeId = "cyberpunk-neon",
        titleFa = "سایبرپانک HUD و سای‌فای",
        titleEn = "Cyberpunk HUD & Sci-Fi",
        subtitleFa = "بوم تاریک عمیق با براکت‌های هندسی نئونی سای‌فای، خطوط مدار الکترونیکی، ویژوالایزر فیروزه‌ای و نشانگرهای سیستمی",
        subtitleEn = "Deep void black with cyan & magenta angular HUD brackets, glowing circuit traces, cyber indicators & futuristic tech frames",
        primaryAccent = Color(0xFF00F0FF), // Cyber Cyan
        secondaryAccent = Color(0xFFE056FD), // Cyber Magenta
        bgStartDark = Color(0xFF05070E),
        bgCenterDark = Color(0xFF0A0F1D),
        bgEndDark = Color(0xFF030409),
        bgStartLight = Color(0xFFE0F2FE),
        bgCenterLight = Color(0xFFBAE6FD),
        bgEndLight = Color(0xFF7DD3FC),
        primaryAccentLight = Color(0xFF0284C7),
        secondaryAccentLight = Color(0xFFC026D3),
        waveformColors = listOf(
            Color(0xFF00F0FF),
            Color(0xFF38BDF8),
            Color(0xFFE056FD),
            Color(0xFF00F0FF),
            Color(0xFF22D3EE),
            Color(0xFFE056FD)
        ),
        radiusPanelDp = 16f,
        radiusControlDp = 8f,
        hasCyberHud = true,
        hasNeonHalo = true
    ),

    // 11. Dark Luxury Gold & Obsidian (کروماتیک مشکی و طلای لوکس) - Image 11
    DARK_LUXURY(
        themeId = "dark-luxury",
        titleFa = "کروماتیک مشکی و طلای لوکس",
        titleEn = "Dark Luxury Gold & Obsidian",
        subtitleFa = "بوم کریستال آبسیدین، پنل‌های مشکی مات با حاشیه دوبل طلای متالیک، نشان‌های برجسته زرین و بازتاب ساتن",
        subtitleEn = "Obsidian crystal canvas, satin matte black panels with dual metallic gold bevel borders & golden dot visualizer",
        primaryAccent = Color(0xFFD4AF37), // Pure Luxury Gold
        secondaryAccent = Color(0xFFF5C542), // Bright Gold Accent
        bgStartDark = Color(0xFF0C0C0E), // Pure Obsidian
        bgCenterDark = Color(0xFF141418),
        bgEndDark = Color(0xFF08080A),
        bgStartLight = Color(0xFFF9F8F6),
        bgCenterLight = Color(0xFFF0EDE6),
        bgEndLight = Color(0xFFE5E0D5),
        primaryAccentLight = Color(0xFFB8860B),
        secondaryAccentLight = Color(0xFF996515),
        waveformColors = listOf(
            Color(0xFFD4AF37),
            Color(0xFFF3E5AB),
            Color(0xFFE5C158),
            Color(0xFFC5A059),
            Color(0xFFB8860B),
            Color(0xFFE6CA65)
        ),
        radiusPanelDp = 22f,
        radiusControlDp = 12f,
        hasMetallicSheen = true
    ),

    // 12. Crimson Carbon (قرمز، سفید و مشکی) - Red, White & Black High-Contrast
    CRIMSON_CARBON(
        themeId = "crimson-carbon",
        titleFa = "قرمز، سفید و مشکی (کربن قرمز)",
        titleEn = "Crimson Carbon (Red & White)",
        subtitleFa = "ترکیب قدرتمند قرمز آتشین، سفید شفاف و بوم مشکی کربنی عمیق با نهایت کنتراست و پویایی",
        subtitleEn = "High-contrast dynamic palette combining fiery crimson red, crisp pure white & deep carbon black",
        primaryAccent = Color(0xFFFF1E40), // Fiery Crimson Red
        secondaryAccent = Color(0xFFFFFFFF), // Crisp Pure White
        bgStartDark = Color(0xFF0D0D11), // Carbon Black
        bgCenterDark = Color(0xFF14141A), // Deep Black
        bgEndDark = Color(0xFF08080A), // OLED Black
        bgStartLight = Color(0xFFFFFFFF),
        bgCenterLight = Color(0xFFF6F6F9),
        bgEndLight = Color(0xFFE8E8EE),
        primaryAccentLight = Color(0xFFE11D48),
        secondaryAccentLight = Color(0xFF18181B),
        waveformColors = listOf(
            Color(0xFFFF1E40),
            Color(0xFFFFFFFF),
            Color(0xFFFF3B5C),
            Color(0xFFF1F5F9),
            Color(0xFFFF1E40),
            Color(0xFFFF758F)
        ),
        radiusPanelDp = 20f,
        radiusControlDp = 12f,
        hasNeonHalo = true
    ),

    // 13. Scarlet Red Studio (قرمز استودیویی و اسکارلت) - Red & Crimson Audiophile Studio
    SCARLET_STUDIO(
        themeId = "scarlet-studio",
        titleFa = "قرمز استودیویی و اسکارلت",
        titleEn = "Scarlet Red Studio",
        subtitleFa = "بوم قرمز آتشین پرحرارت، کارت‌های زرشکی استودیویی، کنترل‌های مشکی مات و خطوط ویژوالایزر",
        subtitleEn = "Fiery ruby scarlet canvas, rich crimson studio cards with matte black controls & crisp white typography",
        primaryAccent = Color(0xFF1A0206), // Matte Black Controls
        secondaryAccent = Color(0xFFFFFFFF), // Crisp Pure White
        bgStartDark = Color(0xFFD61842), // Saturated Carmine Red
        bgCenterDark = Color(0xFFC01538), // Rich Crimson
        bgEndDark = Color(0xFFA50E2D), // Deep Ruby Red
        bgStartLight = Color(0xFFE51D4B),
        bgCenterLight = Color(0xFFD61842),
        bgEndLight = Color(0xFFBF1438),
        primaryAccentLight = Color(0xFF1A0206),
        secondaryAccentLight = Color(0xFFFFFFFF),
        waveformColors = listOf(
            Color(0xFF1E0207),
            Color(0xFF3B050F),
            Color(0xFF140105),
            Color(0xFF28030A),
            Color(0xFFFFFFFF),
            Color(0xFF1E0207)
        ),
        radiusPanelDp = 20f,
        radiusControlDp = 12f
    );

    companion object {
        /**
         * Safely finds a theme style by ID or enum name.
         * Defaults to COSMIC_AURORA.
         */
        fun fromIdOrDefault(id: String?): AppThemeStyle {
            if (id.isNullOrBlank()) return RETRO_ANALOG
            val trimmed = id.trim()

            // Check by themeId
            values().firstOrNull { it.themeId.equals(trimmed, ignoreCase = true) }?.let { return it }

            // Check by enum name
            values().firstOrNull { it.name.equals(trimmed, ignoreCase = true) }?.let { return it }

            // Map any aliases
            return when (trimmed.uppercase()) {
                "SCARLET_STUDIO", "SCARLET_RED", "GHARMEZ", "GHARMEZ_STUDIO", "RED_STUDIO", "RUBY_RED" -> SCARLET_STUDIO
                "DARK_LUXURY", "SUNSET_GOLD" -> DARK_LUXURY
                "CRIMSON_CARBON", "RED_WHITE_BLACK", "CRIMSON_RED", "RED_BLACK" -> CRIMSON_CARBON
                "HOLOGRAPHIC_JELLY" -> HOLOGRAPHIC_JELLY
                "EARTHY_BIOPHILIC", "EMERALD_STUDIO" -> EARTHY_BIOPHILIC
                "COSMIC_AURORA", "CYBER_NEON" -> COSMIC_AURORA
                "MINIMALIST_MONOCHROME" -> MINIMALIST_MONOCHROME
                "RETRO_ANALOG", "VINTAGE", "VINTAGE_ANALOG", "VINTAGE_ANALOG_HI_FI", "VINTAGE_ANAKOG_HI_FI" -> RETRO_ANALOG
                "RETRO_TERMINAL" -> RETRO_TERMINAL
                "MODERN_BRUTALISM" -> MODERN_BRUTALISM
                "CLAYMORPHIC_PASTEL" -> CLAYMORPHIC_PASTEL
                "NEUMORPHIC_GLASS" -> NEUMORPHIC_GLASS
                "CYBERPUNK_NEON" -> CYBERPUNK_NEON
                else -> EARTHY_BIOPHILIC
            }
        }
    }
}

/**
 * Complete strongly-typed Design Token Palette for TO_OT Player
 */
data class TootThemePalette(
    val themeId: String,
    val style: AppThemeStyle,
    val isDarkMode: Boolean,

    // Design Tokens: Colors
    val colorBackground: Color,
    val colorSurface: Color,
    val colorSurfaceSecondary: Color,
    val colorSurfaceElevated: Color,
    val colorTextPrimary: Color,
    val colorTextSecondary: Color,
    val colorTextMuted: Color,
    val colorAccentPrimary: Color,
    val colorAccentSecondary: Color,
    val colorSuccess: Color = Color(0xFF10B981),
    val colorWarning: Color = Color(0xFFF59E0B),
    val colorDanger: Color = Color(0xFFF43F5E),
    val colorBorder: Color,
    val colorBorderActive: Color,
    val colorFocusRing: Color,
    val colorControlBackground: Color,
    val colorControlHover: Color,
    val colorControlActive: Color,

    // Design Tokens: Deep UI Material Properties
    val cardShape: RoundedCornerShape,
    val controlShape: RoundedCornerShape,
    val buttonShape: RoundedCornerShape,
    val borderStrokeWidth: Dp,
    val cardBorderBrush: Brush,
    val cardBackgroundBrush: Brush,
    val buttonBrush: Brush,
    val buttonBorder: BorderStroke?,
    val buttonTextColor: Color,
    val cardElevation: Dp,
    val hasHardOffsetShadow: Boolean = false,
    val hardShadowOffsetDp: Dp = 4.dp,
    val hardShadowColor: Color = Color.Black,
    val hasScanlineOverlay: Boolean = false,
    val hasTerminalAscii: Boolean = false,
    val hasPuffy3DHighlight: Boolean = false,
    val hasNeonHalo: Boolean = false,
    val hasMetallicSheen: Boolean = false,
    val hasNeumorphicDepth: Boolean = false,
    val hasIridescentRim: Boolean = false,
    val hasWoodTexture: Boolean = false,
    val hasCyberHud: Boolean = false,
    val hasVUMeter: Boolean = false,
    val isWireframeMonochrome: Boolean = false,

    // Design Tokens: Shadows & Glows
    val shadowPanel: Dp = 8.dp,
    val shadowControl: Dp = 4.dp,
    val glowAccent: Color,

    // Design Tokens: Shapes & Radii
    val radiusPanel: Dp,
    val radiusControl: Dp,
    val blurPanel: Dp = 16.dp,

    // Design Tokens: Dynamics & Typography
    val headingWeight: FontWeight = FontWeight.Bold,
    val transitionDurationMs: Int = 300,
    val waveformColors: List<Color>,

    // Primary Gradients
    val gradientPrimary: Brush,
    val gradientSecondary: Brush,

    // Background Gradients
    val backgroundGradient: Brush,
    val surfaceGradient: Brush,
    val headerGradient: Brush,

    // Raw Color Palette
    val bgStart: Color,
    val bgCenter: Color,
    val bgEnd: Color,

    // Semantic Mapping Accessors
    val primary: Color = colorAccentPrimary,
    val secondary: Color = colorAccentSecondary,
    val cardBackground: Color = colorSurface,
    val cardBorder: Color = colorBorder,
    val textPrimary: Color = colorTextPrimary,
    val textSecondary: Color = colorTextSecondary,
    val textMuted: Color = colorTextMuted,
    val bottomNavBackground: Color = colorSurfaceElevated,
    val bottomNavSelectedIndicator: Color = colorControlActive.copy(alpha = 0.28f),
    val glassSurface: Color = colorSurface,
    val glassSurfaceElevated: Color = colorSurfaceElevated,
    val glassBorder: Color = colorBorder,
    val activeGlow: Color = glowAccent
)

/**
 * Creates the complete Theme Design Token Palette based on the 11 supported themes.
 */
fun createTootPalette(style: AppThemeStyle, isDarkMode: Boolean): TootThemePalette {
    val bgStart = if (isDarkMode) style.bgStartDark else style.bgStartLight
    val bgCenter = if (isDarkMode) style.bgCenterDark else style.bgCenterLight
    val bgEnd = if (isDarkMode) style.bgEndDark else style.bgEndLight

    val primary = if (isDarkMode) style.primaryAccent else style.primaryAccentLight
    val secondary = if (isDarkMode) style.secondaryAccent else style.secondaryAccentLight

    val gradPrimary = Brush.linearGradient(listOf(primary, secondary))
    val gradSecondary = Brush.linearGradient(listOf(secondary, primary))

    val bgGradient = Brush.verticalGradient(listOf(bgStart, bgCenter, bgEnd))
    val headerGrad = Brush.horizontalGradient(listOf(primary, secondary))

    // Theme Color Tokens
    val tokens = when (style) {
        AppThemeStyle.COSMIC_AURORA -> ThemeColorTokens(
            cardBg = Color(0xFF131738).copy(alpha = 0.70f),
            surfaceSecondary = Color(0xFF1B214D).copy(alpha = 0.65f),
            surfaceElevated = Color(0xFF0B0E23),
            textPri = Color(0xFFFFFFFF),
            textSec = Color(0xFFC7D2FE),
            textMut = Color(0xFF818CF8),
            border = Color(0xFF818CF8).copy(alpha = 0.40f),
            borderActive = Color(0xFF00F5D4)
        )
        AppThemeStyle.EARTHY_BIOPHILIC -> ThemeColorTokens(
            cardBg = Color(0xFF384635), // Dark moss stone card
            surfaceSecondary = Color(0xFF2E3A2C),
            surfaceElevated = Color(0xFF141D13),
            textPri = Color(0xFFF3EEDF),
            textSec = Color(0xFFC2BAA6),
            textMut = Color(0xFF8FA96D),
            border = Color(0xFF556951),
            borderActive = Color(0xFFC86D51)
        )
        AppThemeStyle.MINIMALIST_MONOCHROME -> ThemeColorTokens(
            cardBg = Color(0xFF000000),
            surfaceSecondary = Color(0xFF0D0D0D),
            surfaceElevated = Color(0xFF000000),
            textPri = Color(0xFFFFFFFF),
            textSec = Color(0xFFCCCCCC),
            textMut = Color(0xFF888888),
            border = Color(0xFFFFFFFF),
            borderActive = Color(0xFFFFFFFF)
        )
        AppThemeStyle.RETRO_ANALOG -> ThemeColorTokens(
            cardBg = Color(0xFF342316), // Dark walnut wood card
            surfaceSecondary = Color(0xFF2A1C12),
            surfaceElevated = Color(0xFFD4AF37), // Champagne brass header/footer
            textPri = Color(0xFFFFF8E7),
            textSec = Color(0xFFD6C2A5),
            textMut = Color(0xFFA89578),
            border = Color(0xFF6B4C30),
            borderActive = Color(0xFFD4AF37)
        )
        AppThemeStyle.RETRO_TERMINAL -> ThemeColorTokens(
            cardBg = Color(0xFF041007),
            surfaceSecondary = Color(0xFF071C0C),
            surfaceElevated = Color(0xFF010603),
            textPri = Color(0xFF33FF66),
            textSec = Color(0xFF00FF88),
            textMut = Color(0xFF1E824C),
            border = Color(0xFF33FF66).copy(alpha = 0.65f),
            borderActive = Color(0xFF33FF66)
        )
        AppThemeStyle.MODERN_BRUTALISM -> ThemeColorTokens(
            cardBg = Color(0xFFFFE500),
            surfaceSecondary = Color(0xFFFFEA33),
            surfaceElevated = Color(0xFFFFE500),
            textPri = Color(0xFF000000),
            textSec = Color(0xFF1A1A1A),
            textMut = Color(0xFF333333),
            border = Color(0xFF000000),
            borderActive = Color(0xFF000000)
        )
        AppThemeStyle.CLAYMORPHIC_PASTEL -> ThemeColorTokens(
            cardBg = Color(0xFFDCD4EC), // Puffy soft lavender clay
            surfaceSecondary = Color(0xFFD1C7E3),
            surfaceElevated = Color(0xFFC7BBDC),
            textPri = Color(0xFF2E1C48),
            textSec = Color(0xFF5E4B77),
            textMut = Color(0xFF8672A0),
            border = Color(0xFFD4AF37).copy(alpha = 0.40f),
            borderActive = Color(0xFFD4AF37)
        )
        AppThemeStyle.HOLOGRAPHIC_JELLY -> ThemeColorTokens(
            cardBg = Color(0xFF160F2E).copy(alpha = 0.65f),
            surfaceSecondary = Color(0xFF221644).copy(alpha = 0.60f),
            surfaceElevated = Color(0xFF0C071C),
            textPri = Color(0xFFFFFFFF),
            textSec = Color(0xFFD8B4FE),
            textMut = Color(0xFF9386B8),
            border = Color(0xFF00F5D4).copy(alpha = 0.45f),
            borderActive = Color(0xFFE056FD)
        )
        AppThemeStyle.NEUMORPHIC_GLASS -> ThemeColorTokens(
            cardBg = Color(0xFFE2EAF2),
            surfaceSecondary = Color(0xFFD8E2EC),
            surfaceElevated = Color(0xFFCDD9E5),
            textPri = Color(0xFF1E293B),
            textSec = Color(0xFF475569),
            textMut = Color(0xFF64748B),
            border = Color(0xFF06B6D4).copy(alpha = 0.35f),
            borderActive = Color(0xFF06B6D4)
        )
        AppThemeStyle.CYBERPUNK_NEON -> ThemeColorTokens(
            cardBg = Color(0xFF080D1A).copy(alpha = 0.85f),
            surfaceSecondary = Color(0xFF0F172A).copy(alpha = 0.80f),
            surfaceElevated = Color(0xFF04060C),
            textPri = Color(0xFFFFFFFF),
            textSec = Color(0xFF7DD3FC),
            textMut = Color(0xFF38BDF8),
            border = Color(0xFF00F0FF).copy(alpha = 0.60f),
            borderActive = Color(0xFF00F0FF)
        )
        AppThemeStyle.DARK_LUXURY -> ThemeColorTokens(
            cardBg = Color(0xFF141418),
            surfaceSecondary = Color(0xFF1A1A22),
            surfaceElevated = Color(0xFF09090C),
            textPri = Color(0xFFFBF8F2),
            textSec = Color(0xFFD6CABA),
            textMut = Color(0xFF8E8679),
            border = Color(0xFFD4AF37).copy(alpha = 0.40f),
            borderActive = Color(0xFFF5C542)
        )
        AppThemeStyle.CRIMSON_CARBON -> ThemeColorTokens(
            cardBg = Color(0xFF14141A).copy(alpha = 0.85f),
            surfaceSecondary = Color(0xFF1E1E26).copy(alpha = 0.80f),
            surfaceElevated = Color(0xFF0A0A0E),
            textPri = Color(0xFFFFFFFF),
            textSec = Color(0xFFE2E2E8),
            textMut = Color(0xFFA0A0AB),
            border = Color(0xFFFF1E40).copy(alpha = 0.45f),
            borderActive = Color(0xFFFF1E40)
        )
        AppThemeStyle.SCARLET_STUDIO -> ThemeColorTokens(
            cardBg = Color(0xFFBE163C),
            surfaceSecondary = Color(0xFFA81033),
            surfaceElevated = Color(0xFFD61842),
            textPri = Color(0xFFFFFFFF),
            textSec = Color(0xFFFFD6E0),
            textMut = Color(0xFFFFAEC0),
            border = Color(0xFF6E071D),
            borderActive = Color(0xFF1A0206)
        )
    }

    val cardShape = RoundedCornerShape(style.radiusPanelDp.dp)
    val controlShape = RoundedCornerShape(style.radiusControlDp.dp)
    val buttonShape = if (style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else RoundedCornerShape(50)

    val strokeWidth = when (style) {
        AppThemeStyle.MODERN_BRUTALISM -> 2.5.dp
        AppThemeStyle.MINIMALIST_MONOCHROME -> 1.4.dp
        AppThemeStyle.CYBERPUNK_NEON -> 1.5.dp
        AppThemeStyle.CRIMSON_CARBON -> 1.3.dp
        AppThemeStyle.SCARLET_STUDIO -> 1.2.dp
        AppThemeStyle.RETRO_TERMINAL -> 1.2.dp
        AppThemeStyle.HOLOGRAPHIC_JELLY -> 1.2.dp
        AppThemeStyle.COSMIC_AURORA -> 1.4.dp
        else -> 1.0.dp
    }

    val cardBorderBrush = when (style) {
        AppThemeStyle.DARK_LUXURY -> Brush.linearGradient(listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37), Color(0xFFB8860B)))
        AppThemeStyle.CRIMSON_CARBON -> Brush.linearGradient(listOf(Color(0xFFFF1E40), Color(0xFFFFFFFF), Color(0xFFFF1E40)))
        AppThemeStyle.SCARLET_STUDIO -> Brush.linearGradient(listOf(Color(0xFF7A0920), Color(0xFF5A0516)))
        AppThemeStyle.COSMIC_AURORA -> Brush.linearGradient(listOf(Color(0xFF00F5D4), Color(0xFF818CF8), Color(0xFFA78BFA)))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Brush.linearGradient(listOf(Color(0xFF00F5D4), Color(0xFF38BDF8), Color(0xFFE056FD), Color(0xFFF472B6)))
        AppThemeStyle.CYBERPUNK_NEON -> Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF38BDF8), Color(0xFFE056FD)))
        AppThemeStyle.RETRO_ANALOG -> Brush.linearGradient(listOf(Color(0xFF8A653E), Color(0xFF53381F)))
        AppThemeStyle.MINIMALIST_MONOCHROME -> Brush.linearGradient(listOf(Color.White, Color.White))
        AppThemeStyle.MODERN_BRUTALISM -> Brush.linearGradient(listOf(Color.Black, Color.Black))
        AppThemeStyle.RETRO_TERMINAL -> Brush.linearGradient(listOf(Color(0xFF33FF66), Color(0xFF00FF88)))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Brush.linearGradient(listOf(Color(0xFFD4AF37).copy(alpha = 0.5f), Color(0xFFD4AF37).copy(alpha = 0.3f)))
        AppThemeStyle.NEUMORPHIC_GLASS -> Brush.linearGradient(listOf(Color(0xFF06B6D4).copy(alpha = 0.4f), Color(0xFF8B5CF6).copy(alpha = 0.4f)))
        AppThemeStyle.EARTHY_BIOPHILIC -> Brush.linearGradient(listOf(Color(0xFF556951), Color(0xFF435340)))
    }

    val cardBackgroundBrush = when (style) {
        AppThemeStyle.EARTHY_BIOPHILIC -> Brush.verticalGradient(listOf(Color(0xFF3B4A38), Color(0xFF303D2E)))
        AppThemeStyle.RETRO_ANALOG -> Brush.verticalGradient(listOf(Color(0xFF3C281A), Color(0xFF281B11)))
        AppThemeStyle.MODERN_BRUTALISM -> Brush.linearGradient(listOf(Color(0xFFFFE500), Color(0xFFFFE500)))
        AppThemeStyle.MINIMALIST_MONOCHROME -> Brush.linearGradient(listOf(Color.Black, Color.Black))
        AppThemeStyle.RETRO_TERMINAL -> Brush.verticalGradient(listOf(Color(0xFF051509), Color(0xFF020A04)))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Brush.verticalGradient(listOf(Color(0xFFE0D8EE), Color(0xFFD5CBDF)))
        AppThemeStyle.NEUMORPHIC_GLASS -> Brush.verticalGradient(listOf(Color(0xFFE5EDF5), Color(0xFFDCE5EE)))
        AppThemeStyle.DARK_LUXURY -> Brush.verticalGradient(listOf(Color(0xFF18181D), Color(0xFF101013)))
        AppThemeStyle.CRIMSON_CARBON -> Brush.verticalGradient(listOf(Color(0xFF191920), Color(0xFF0E0E12)))
        AppThemeStyle.SCARLET_STUDIO -> Brush.verticalGradient(listOf(Color(0xFFBE163C), Color(0xFFB01134)))
        AppThemeStyle.COSMIC_AURORA -> Brush.linearGradient(listOf(Color(0xFF14193C).copy(alpha = 0.75f), Color(0xFF0D102A).copy(alpha = 0.85f)))
        AppThemeStyle.CYBERPUNK_NEON -> Brush.linearGradient(listOf(Color(0xFF0B1124).copy(alpha = 0.90f), Color(0xFF050812).copy(alpha = 0.95f)))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Brush.linearGradient(listOf(Color(0xFF1C133A).copy(alpha = 0.70f), Color(0xFF120C27).copy(alpha = 0.75f)))
    }

    val buttonBrush = when (style) {
        AppThemeStyle.EARTHY_BIOPHILIC -> Brush.verticalGradient(listOf(Color(0xFFD97757), Color(0xFFB85D42)))
        AppThemeStyle.RETRO_ANALOG -> Brush.verticalGradient(listOf(Color(0xFFE8C87A), Color(0xFFC59F4E), Color(0xFF9E7C35)))
        AppThemeStyle.MODERN_BRUTALISM -> Brush.linearGradient(listOf(Color(0xFFFFE500), Color(0xFFFFE500)))
        AppThemeStyle.MINIMALIST_MONOCHROME -> Brush.linearGradient(listOf(Color.Black, Color.Black))
        AppThemeStyle.RETRO_TERMINAL -> Brush.verticalGradient(listOf(Color(0xFF082610), Color(0xFF031407)))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Brush.verticalGradient(listOf(Color(0xFFE2DAEE), Color(0xFFD2C7E2)))
        AppThemeStyle.NEUMORPHIC_GLASS -> Brush.verticalGradient(listOf(Color(0xFFE2EAF2), Color(0xFFD8E2EC)))
        AppThemeStyle.DARK_LUXURY -> Brush.verticalGradient(listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37), Color(0xFFB8860B)))
        AppThemeStyle.CRIMSON_CARBON -> Brush.horizontalGradient(listOf(Color(0xFFFF1E40), Color(0xFFD90429)))
        AppThemeStyle.SCARLET_STUDIO -> Brush.verticalGradient(listOf(Color(0xFFBE163C), Color(0xFFA81033)))
        AppThemeStyle.COSMIC_AURORA -> Brush.linearGradient(listOf(Color(0xFF1A1F45), Color(0xFF101430)))
        AppThemeStyle.CYBERPUNK_NEON -> Brush.linearGradient(listOf(Color(0xFF08182B), Color(0xFF040D17)))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Brush.horizontalGradient(listOf(Color(0xFF00F5D4), Color(0xFF38BDF8), Color(0xFFE056FD)))
    }

    val buttonBorder = when (style) {
        AppThemeStyle.MODERN_BRUTALISM -> BorderStroke(2.dp, Color.Black)
        AppThemeStyle.MINIMALIST_MONOCHROME -> BorderStroke(1.2.dp, Color.White)
        AppThemeStyle.RETRO_TERMINAL -> BorderStroke(1.2.dp, Color(0xFF33FF66))
        AppThemeStyle.CYBERPUNK_NEON -> BorderStroke(1.2.dp, Color(0xFF00F0FF))
        AppThemeStyle.CRIMSON_CARBON -> BorderStroke(1.2.dp, Color.White.copy(alpha = 0.85f))
        AppThemeStyle.SCARLET_STUDIO -> BorderStroke(1.2.dp, Color(0xFF1A0206))
        AppThemeStyle.COSMIC_AURORA -> BorderStroke(1.2.dp, Color(0xFF818CF8).copy(alpha = 0.8f))
        AppThemeStyle.DARK_LUXURY -> BorderStroke(0.8.dp, Color(0xFFF3E5AB))
        AppThemeStyle.RETRO_ANALOG -> BorderStroke(1.dp, Color(0xFFE8C87A))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.8f))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> BorderStroke(1.dp, Color(0xFF00F5D4))
        AppThemeStyle.NEUMORPHIC_GLASS -> BorderStroke(0.8.dp, Color.White.copy(alpha = 0.7f))
        AppThemeStyle.EARTHY_BIOPHILIC -> BorderStroke(1.dp, Color(0xFFE89274).copy(alpha = 0.6f))
    }

    val buttonTextColor = when (style) {
        AppThemeStyle.MODERN_BRUTALISM -> Color.Black
        AppThemeStyle.MINIMALIST_MONOCHROME -> Color.White
        AppThemeStyle.RETRO_TERMINAL -> Color(0xFF33FF66)
        AppThemeStyle.CYBERPUNK_NEON -> Color(0xFF00F0FF)
        AppThemeStyle.CRIMSON_CARBON -> Color.White
        AppThemeStyle.SCARLET_STUDIO -> Color(0xFF1A0206)
        AppThemeStyle.COSMIC_AURORA -> Color.White
        AppThemeStyle.DARK_LUXURY -> Color(0xFF1A1505)
        AppThemeStyle.RETRO_ANALOG -> Color(0xFF24180E)
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Color(0xFF2E1C48)
        AppThemeStyle.NEUMORPHIC_GLASS -> Color(0xFF1E293B)
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Color.Black
        AppThemeStyle.EARTHY_BIOPHILIC -> Color(0xFFFFF7ED)
    }

    val cardElevation = when (style) {
        AppThemeStyle.MODERN_BRUTALISM -> 0.dp
        AppThemeStyle.MINIMALIST_MONOCHROME -> 0.dp
        AppThemeStyle.RETRO_TERMINAL -> 0.dp
        AppThemeStyle.CLAYMORPHIC_PASTEL -> 8.dp
        AppThemeStyle.NEUMORPHIC_GLASS -> 6.dp
        AppThemeStyle.HOLOGRAPHIC_JELLY -> 8.dp
        AppThemeStyle.COSMIC_AURORA -> 6.dp
        AppThemeStyle.CYBERPUNK_NEON -> 6.dp
        AppThemeStyle.DARK_LUXURY -> 6.dp
        AppThemeStyle.CRIMSON_CARBON -> 6.dp
        AppThemeStyle.SCARLET_STUDIO -> 4.dp
        AppThemeStyle.RETRO_ANALOG -> 6.dp
        AppThemeStyle.EARTHY_BIOPHILIC -> 4.dp
    }

    return TootThemePalette(
        themeId = style.themeId,
        style = style,
        isDarkMode = isDarkMode,
        colorBackground = bgStart,
        colorSurface = tokens.cardBg,
        colorSurfaceSecondary = tokens.surfaceSecondary,
        colorSurfaceElevated = tokens.surfaceElevated,
        colorTextPrimary = tokens.textPri,
        colorTextSecondary = tokens.textSec,
        colorTextMuted = tokens.textMut,
        colorAccentPrimary = primary,
        colorAccentSecondary = secondary,
        colorBorder = tokens.border,
        colorBorderActive = tokens.borderActive,
        colorFocusRing = primary.copy(alpha = 0.6f),
        colorControlBackground = primary.copy(alpha = if (isDarkMode) 0.15f else 0.12f),
        colorControlHover = primary.copy(alpha = if (isDarkMode) 0.25f else 0.20f),
        colorControlActive = primary,
        cardShape = cardShape,
        controlShape = controlShape,
        buttonShape = buttonShape,
        borderStrokeWidth = strokeWidth,
        cardBorderBrush = cardBorderBrush,
        cardBackgroundBrush = cardBackgroundBrush,
        buttonBrush = buttonBrush,
        buttonBorder = buttonBorder,
        buttonTextColor = buttonTextColor,
        cardElevation = cardElevation,
        hasHardOffsetShadow = style.hasHardOffsetShadow,
        hardShadowOffsetDp = 4.dp,
        hardShadowColor = Color.Black,
        hasScanlineOverlay = style.hasScanlineOverlay,
        hasTerminalAscii = style.hasTerminalAscii,
        hasPuffy3DHighlight = style.hasPuffy3DHighlight,
        hasNeonHalo = style.hasNeonHalo,
        hasMetallicSheen = style.hasMetallicSheen,
        hasNeumorphicDepth = style.hasNeumorphicDepth,
        hasIridescentRim = style.hasIridescentRim,
        hasWoodTexture = style.hasWoodTexture,
        hasCyberHud = style.hasCyberHud,
        hasVUMeter = style.hasVUMeter,
        isWireframeMonochrome = style.isWireframeMonochrome,
        glowAccent = primary.copy(alpha = if (isDarkMode) 0.35f else 0.20f),
        radiusPanel = style.radiusPanelDp.dp,
        radiusControl = style.radiusControlDp.dp,
        waveformColors = style.waveformColors,
        gradientPrimary = gradPrimary,
        gradientSecondary = gradSecondary,
        backgroundGradient = bgGradient,
        surfaceGradient = cardBackgroundBrush,
        headerGradient = headerGrad,
        bgStart = bgStart,
        bgCenter = bgCenter,
        bgEnd = bgEnd
    )
}

private data class ThemeColorTokens(
    val cardBg: Color,
    val surfaceSecondary: Color,
    val surfaceElevated: Color,
    val textPri: Color,
    val textSec: Color,
    val textMut: Color,
    val border: Color,
    val borderActive: Color
)

val LocalAppThemePalette = staticCompositionLocalOf {
    createTootPalette(AppThemeStyle.EARTHY_BIOPHILIC, isDarkMode = true)
}
