package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.util.AppInfo
import com.example.util.AppLanguage
import com.example.util.LocalActiveLanguage
import com.example.util.LocalAppStrings

@Composable
fun SettingsTabScreen(
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenRecorder: () -> Unit,
    onOpenAudioExtractor: () -> Unit = {},
    onOpenAudioRouting: () -> Unit = {},
    onOpenThemeSelector: () -> Unit,
    onOpenLanguageSelector: () -> Unit,
    onOpenHomescreenWidgetDialog: () -> Unit = {},
    onOpenAboutDialog: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenHelpCenter: () -> Unit = {},
    onReplayTutorial: () -> Unit = {},
    onTogglePersianRtl: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onPlayAllLanes: () -> Unit,
    onPauseAllLanes: () -> Unit,
    onStopAllLanes: () -> Unit,
    onClearAllMutes: () -> Unit,
    onClearAllSolos: () -> Unit,
    isPersianRtl: Boolean = false,
    isDarkMode: Boolean = true,
    currentThemeName: String,
    eqEnabled: Boolean,
    sleepTimerSec: Int,
    isRecording: Boolean,
    autoResumeAfterCall: Boolean = true,
    onToggleAutoResumeAfterCall: () -> Unit = {},
    activeAiProvider: String = "gemini",
    customGeminiApiKey: String = "",
    customOpenAiApiKey: String = "",
    customDeepSeekApiKey: String = "",
    customGroqApiKey: String = "",
    hasValidAiApiKey: Boolean = false,
    onSelectAiProvider: (String) -> Unit = {},
    onSaveAiProviderKey: (String, String) -> Unit = { _, _ -> },
    onClearAiProviderKey: (String) -> Unit = {},
    customApiKey: String = "",
    hasValidApiKey: Boolean = false,
    onSaveCustomApiKey: (String) -> Unit = {},
    onClearCustomApiKey: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current
    val strings = LocalAppStrings.current
    val activeLang = LocalActiveLanguage.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = themePalette.textPrimary,
                fontSize = 22.sp
            )
        )
        Text(
            text = "${AppInfo.APP_NAME} v${AppInfo.VERSION_NAME} • ${AppInfo.LAST_UPDATE_SHAMSI}",
            style = MaterialTheme.typography.bodySmall.copy(color = themePalette.textMuted)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Audio Processing & Studio Tools
        Text(
            text = strings.audioEngineSection.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = themePalette.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsItemCard(
            title = strings.sleepTimer,
            subtitle = if (sleepTimerSec > 0) "${sleepTimerSec / 60}m ${sleepTimerSec % 60}s remaining" else "Off",
            icon = Icons.Default.Bedtime,
            iconTint = Color(0xFFF59E0B),
            onClick = onOpenSleepTimer,
            testTag = "settings_open_sleep_timer"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItemCard(
            title = if (activeLang == AppLanguage.FA) "مسیریابی صدا و سیستم‌های صوتی (ضبط ماشین)" else "Audio Output & Car Systems",
            subtitle = if (activeLang == AppLanguage.FA) "اتصال به ضبط ماشین، بلوتوث، کابل AUX و حالت صوتی تلفن (SCO)" else "Car Audio, Bluetooth, AUX & Telephony Mode",
            icon = Icons.Default.AltRoute,
            iconTint = Color(0xFF06B6D4),
            onClick = onOpenAudioRouting,
            testTag = "settings_open_audio_routing"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItemCard(
            title = strings.videoAudioExtractor,
            subtitle = if (activeLang == AppLanguage.FA) "استخراج صوتی MP3 از ویدیوهای طولانی و حجیم" else "Extract MP3 audio from long & large videos",
            icon = Icons.Default.Movie,
            iconTint = Color(0xFF6366F1),
            onClick = onOpenAudioExtractor,
            testTag = "settings_open_audio_extractor"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItemCard(
            title = strings.equalizer,
            subtitle = if (eqEnabled) "ON • Live Visualizer" else "Bypassed",
            icon = Icons.Default.GraphicEq,
            iconTint = themePalette.primary,
            onClick = onOpenEqualizer,
            testTag = "settings_open_eq"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItemCard(
            title = strings.masterRecorder,
            subtitle = if (isRecording) "Recording..." else "Ready",
            icon = Icons.Default.Mic,
            iconTint = if (isRecording) RoseMute else Color(0xFF10B981),
            onClick = onOpenRecorder,
            testTag = "settings_open_recorder"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItemCard(
            title = if (activeLang == AppLanguage.FA) "ویجت صفحه اصلی (Homescreen Widget)" else "Homescreen Widget & Controls",
            subtitle = if (activeLang == AppLanguage.FA) "کنترلر دسکتاپ برای پخش/توقف و سولو بدون باز کردن کامل اپلیکیشن" else "Desktop controller for play/pause & track solo without opening app",
            icon = Icons.Default.Widgets,
            iconTint = Color(0xFFF43F5E),
            onClick = onOpenHomescreenWidgetDialog,
            testTag = "settings_open_homescreen_widget"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Simultaneous Mixer & Multi-Play Controls
        Text(
            text = "MULTI-SESSION MIXER",
            style = MaterialTheme.typography.labelSmall.copy(
                color = themePalette.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = themePalette.cardBackground,
            border = BorderStroke(1.dp, themePalette.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onPlayAllLanes,
                        modifier = Modifier
                            .weight(1f)
                            .height(35.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themePalette.primary,
                            contentColor = if (isDarkMode) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPersianRtl) "پخش همه" else "Play All",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = onPauseAllLanes,
                        modifier = Modifier
                            .weight(1f)
                            .height(35.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0),
                            contentColor = themePalette.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPersianRtl) "توقف موقت" else "Pause All",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = onStopAllLanes,
                        modifier = Modifier
                            .weight(1f)
                            .height(35.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFFFEE2E2),
                            contentColor = RoseMute
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPersianRtl) "قطع کامل" else "Stop All",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: Call Interruptions & Playback Control
        Text(
            text = if (isPersianRtl) "مدیریت تماس‌های تلفنی و پخش" else "PLAYBACK & PHONE CALLS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = themePalette.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Auto-Resume After Call Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = themePalette.cardBackground,
            border = BorderStroke(1.dp, themePalette.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleAutoResumeAfterCall() }
                .testTag("settings_toggle_auto_resume_after_call")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (autoResumeAfterCall) themePalette.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhoneCallback,
                                contentDescription = null,
                                tint = if (autoResumeAfterCall) themePalette.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = strings.autoResumeAfterCall,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = themePalette.textPrimary
                            )
                        )
                        Text(
                            text = strings.autoResumeAfterCallDesc,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = themePalette.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = autoResumeAfterCall,
                    onCheckedChange = { onToggleAutoResumeAfterCall() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = themePalette.primary,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("switch_auto_resume_after_call")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: App Appearance & Multi-Language
        Text(
            text = "APPEARANCE & LANGUAGE",
            style = MaterialTheme.typography.labelSmall.copy(
                color = themePalette.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 6-Language Selector Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = themePalette.cardBackground,
            border = BorderStroke(1.dp, themePalette.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenLanguageSelector() }
                .testTag("settings_open_language_selector")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = themePalette.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = activeLang.flag,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = strings.languageSection,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = themePalette.textPrimary
                            )
                        )
                        Text(
                            text = "${activeLang.nativeName} (${activeLang.englishName}) • 6 Languages Supported",
                            style = MaterialTheme.typography.labelSmall.copy(color = themePalette.primary)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = themePalette.textMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Theme Engine
        SettingsItemCard(
            title = strings.themeEngine,
            subtitle = "$currentThemeName • ${strings.elevenThemesSubtitle}",
            icon = Icons.Default.Palette,
            iconTint = themePalette.primary,
            onClick = onOpenThemeSelector,
            testTag = "settings_open_theme"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: AI Multi-Provider Engine & API Keys
        Text(
            text = if (isPersianRtl) "موتور هوش مصنوعی و کلیدهای دستی API" else "AI ENGINE & API KEYS",
            style = MaterialTheme.typography.labelSmall.copy(
                color = themePalette.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MultiAiProviderSettingsCard(
            activeProviderId = activeAiProvider,
            customGeminiApiKey = customGeminiApiKey.ifEmpty { customApiKey },
            customOpenAiApiKey = customOpenAiApiKey,
            customDeepSeekApiKey = customDeepSeekApiKey,
            customGroqApiKey = customGroqApiKey,
            hasValidAiApiKey = hasValidAiApiKey || hasValidApiKey,
            onSelectProvider = onSelectAiProvider,
            onSaveProviderKey = onSaveAiProviderKey,
            onClearProviderKey = onClearAiProviderKey,
            isPersianRtl = isPersianRtl
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 5: Support, Guides & About
        Text(
            text = if (isPersianRtl) "پشتیبانی، راهنما و درباره برنامه" else "SUPPORT & ABOUT",
            style = MaterialTheme.typography.labelSmall.copy(
                color = themePalette.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Help Center & Knowledge Base
        SettingsItemCard(
            title = if (isPersianRtl) "مرکز راهنمایی و سوالات متداول" else "Help Center & FAQ",
            subtitle = if (isPersianRtl) "راهنمای جامع، آموزش‌ها و رفع اشکال کامل" else "Interactive guides, troubleshooting & tips",
            icon = Icons.Default.HelpOutline,
            iconTint = Color(0xFF38BDF8),
            onClick = onOpenHelpCenter,
            testTag = "settings_open_help"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Replay Tutorial / Onboarding Tour
        SettingsItemCard(
            title = if (isPersianRtl) "راهنمای تعاملی برنامه" else "Replay Tutorial",
            subtitle = if (isPersianRtl) "مرور مجدد راهنمای کار با بخش‌های مختلف" else "Review step-by-step interactive walkthrough",
            icon = Icons.Default.AutoStories,
            iconTint = themePalette.primary,
            onClick = onReplayTutorial,
            testTag = "settings_replay_tutorial"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // About TO_OT Player
        SettingsItemCard(
            title = strings.aboutApp,
            subtitle = if (isPersianRtl) "v${AppInfo.VERSION_NAME} • ${AppInfo.LAST_UPDATE_SHAMSI} (${AppInfo.LAST_UPDATE_GREGORIAN})" else "v${AppInfo.VERSION_NAME} • Release: ${AppInfo.LAST_UPDATE_GREGORIAN}",
            icon = Icons.Default.Info,
            iconTint = themePalette.textMuted,
            onClick = onOpenAboutDialog,
            testTag = "settings_open_about"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Privacy Policy & Data Safety
        SettingsItemCard(
            title = if (isPersianRtl) "حریم خصوصی و امنیت داده‌ها" else "Privacy Policy & Data Safety",
            subtitle = if (isPersianRtl) "۱۰۰٪ محلی • بدون جمع‌آوری داده • استاندارد گوگل‌پلی و بازار" else "100% Local • Zero-Data Collection • Store Compliant",
            icon = Icons.Default.Shield,
            iconTint = Color(0xFF10B981),
            onClick = onOpenPrivacyPolicy,
            testTag = "settings_open_privacy_policy"
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SettingsItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val themePalette = LocalAppThemePalette.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themePalette.cardBackground,
        border = BorderStroke(1.dp, themePalette.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = themePalette.textPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = themePalette.textMuted,
                        fontSize = 11.5.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = themePalette.textMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MultiAiProviderSettingsCard(
    activeProviderId: String,
    customGeminiApiKey: String,
    customOpenAiApiKey: String,
    customDeepSeekApiKey: String,
    customGroqApiKey: String,
    hasValidAiApiKey: Boolean,
    onSelectProvider: (String) -> Unit,
    onSaveProviderKey: (String, String) -> Unit,
    onClearProviderKey: (String) -> Unit,
    isPersianRtl: Boolean
) {
    val themePalette = LocalAppThemePalette.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val providers = remember { com.example.ai.AiProvider.values().toList() }
    var selectedProvider by remember(activeProviderId) {
        mutableStateOf(com.example.ai.AiProvider.fromId(activeProviderId))
    }

    // Current saved key for the currently selected provider
    val currentSavedKey = when (selectedProvider) {
        com.example.ai.AiProvider.GEMINI -> customGeminiApiKey
        com.example.ai.AiProvider.OPENAI -> customOpenAiApiKey
        com.example.ai.AiProvider.DEEPSEEK -> customDeepSeekApiKey
        com.example.ai.AiProvider.GROQ -> customGroqApiKey
    }

    // Input state updates whenever the selected provider or its saved key changes
    var enteredKey by remember(selectedProvider, currentSavedKey) {
        mutableStateOf(currentSavedKey)
    }
    var isKeyVisible by remember(selectedProvider) { mutableStateOf(false) }

    val hasCustomKey = currentSavedKey.isNotBlank()
    val isProviderConfigured = when (selectedProvider) {
        com.example.ai.AiProvider.GEMINI -> hasCustomKey || hasValidAiApiKey
        com.example.ai.AiProvider.OPENAI -> hasCustomKey
        com.example.ai.AiProvider.DEEPSEEK -> hasCustomKey
        com.example.ai.AiProvider.GROQ -> hasCustomKey
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themePalette.cardBackground,
        border = BorderStroke(1.dp, themePalette.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_ai_provider_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Icon + Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = (if (isProviderConfigured) Color(0xFF10B981) else themePalette.primary).copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isProviderConfigured) Icons.Default.Key else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isProviderConfigured) Color(0xFF10B981) else themePalette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPersianRtl) "مدیریت ارائه‌دهندگان و کلیدهای هوش مصنوعی" else "AI Search & Multi-Provider Keys",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.textPrimary
                        )
                    )
                    Text(
                        text = if (isPersianRtl) "انتخاب مدل و ثبت کلید دستی بدون چندردیفه شدن" else "Select AI engine & manage API keys",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = themePalette.textMuted,
                            fontSize = 11.sp
                        )
                    )
                }

                // Status Badge for current provider
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        hasCustomKey -> Color(0xFF10B981).copy(alpha = 0.18f)
                        selectedProvider == com.example.ai.AiProvider.GEMINI && hasValidAiApiKey -> Color(0xFF3B82F6).copy(alpha = 0.18f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                hasCustomKey -> Icons.Default.CheckCircle
                                selectedProvider == com.example.ai.AiProvider.GEMINI && hasValidAiApiKey -> Icons.Default.AutoAwesome
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when {
                                hasCustomKey -> Color(0xFF10B981)
                                selectedProvider == com.example.ai.AiProvider.GEMINI && hasValidAiApiKey -> Color(0xFF3B82F6)
                                else -> Color(0xFFF59E0B)
                            },
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                hasCustomKey -> if (isPersianRtl) "شخصی فعال" else "Custom Key"
                                selectedProvider == com.example.ai.AiProvider.GEMINI && hasValidAiApiKey -> if (isPersianRtl) "سیستم پیش‌فرض" else "Default"
                                else -> if (isPersianRtl) "نیاز به کلید" else "Key Required"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                color = when {
                                    hasCustomKey -> Color(0xFF10B981)
                                    selectedProvider == com.example.ai.AiProvider.GEMINI && hasValidAiApiKey -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Selector Chips (Gemini | OpenAI | DeepSeek | Groq)
            // Tapping any chip immediately switches the active provider and updates the single input/paste row below
            Text(
                text = if (isPersianRtl) "انتخاب هوش مصنوعی (کلیک برای ویرایش و الصاق کلید):" else "Select AI Provider (tap to switch & paste key):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = themePalette.textPrimary,
                    fontSize = 11.5.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                providers.forEach { provider ->
                    val isSelected = provider == selectedProvider
                    val hasKeyForThis = when (provider) {
                        com.example.ai.AiProvider.GEMINI -> customGeminiApiKey.isNotBlank() || hasValidAiApiKey
                        com.example.ai.AiProvider.OPENAI -> customOpenAiApiKey.isNotBlank()
                        com.example.ai.AiProvider.DEEPSEEK -> customDeepSeekApiKey.isNotBlank()
                        com.example.ai.AiProvider.GROQ -> customGroqApiKey.isNotBlank()
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isSelected -> themePalette.primary
                            hasKeyForThis -> themePalette.primary.copy(alpha = 0.12f)
                            else -> themePalette.cardBorder.copy(alpha = 0.3f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) themePalette.primary else themePalette.cardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedProvider = provider
                                onSelectProvider(provider.id)
                            }
                            .testTag("btn_select_ai_provider_${provider.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isPersianRtl) provider.displayNameFa else provider.displayNameEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (isSelected) themePalette.buttonTextColor else themePalette.textPrimary,
                                    fontSize = 10.5.sp
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (hasKeyForThis) (if (isPersianRtl) "● فعال" else "● Active") else (if (isPersianRtl) "○ بی‌کلید" else "○ No key"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) themePalette.buttonTextColor.copy(alpha = 0.85f)
                                           else if (hasKeyForThis) Color(0xFF10B981)
                                           else themePalette.textMuted.copy(alpha = 0.6f)
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current provider description & active model name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPersianRtl)
                        "ارائه‌دهنده انتخابی: ${selectedProvider.displayNameFa}"
                    else
                        "Selected Engine: ${selectedProvider.displayNameEn}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = themePalette.primary,
                        fontSize = 12.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = themePalette.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = selectedProvider.defaultModel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = themePalette.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SINGLE Unified Text Input Field & Paste Area for the selected provider
            OutlinedTextField(
                value = enteredKey,
                onValueChange = { enteredKey = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_ai_provider_api_key"),
                label = {
                    Text(
                        text = if (isPersianRtl)
                            "کلید API (${selectedProvider.displayNameFa})"
                        else
                            "${selectedProvider.displayNameEn} API Key",
                        fontSize = 12.sp
                    )
                },
                placeholder = {
                    Text(
                        text = selectedProvider.placeholderKey,
                        color = themePalette.textMuted.copy(alpha = 0.5f)
                    )
                },
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (enteredKey.isNotEmpty()) {
                            IconButton(
                                onClick = { enteredKey = "" },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text",
                                    tint = themePalette.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Prominent PASTE Button (📋)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = themePalette.primary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        enteredKey = clip.trim()
                                        Toast.makeText(
                                            context,
                                            if (isPersianRtl) "کلید در فیلد ${selectedProvider.displayNameFa} الصاق شد 📋"
                                            else "Pasted into ${selectedProvider.displayNameEn} 📋",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isPersianRtl) "حافظه کلیپ‌بورد خالی است" else "Clipboard is empty",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .testTag("btn_paste_ai_api_key")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste from clipboard",
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersianRtl) "الصاق" else "PASTE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = themePalette.primary
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = { isKeyVisible = !isKeyVisible },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle key visibility",
                                tint = themePalette.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themePalette.primary,
                    unfocusedBorderColor = themePalette.cardBorder,
                    focusedLabelColor = themePalette.primary,
                    unfocusedLabelColor = themePalette.textMuted,
                    focusedTextColor = themePalette.textPrimary,
                    unfocusedTextColor = themePalette.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Save Key & Clear Key for the current provider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val trimmed = enteredKey.trim()
                        if (trimmed.isEmpty()) {
                            Toast.makeText(
                                context,
                                if (isPersianRtl) "لطفاً ابتدا کلید را وارد یا الصاق کنید" else "Please enter or paste an API key",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onSaveProviderKey(selectedProvider.id, trimmed)
                            Toast.makeText(
                                context,
                                if (isPersianRtl) "کلید اختصاصی ${selectedProvider.displayNameFa} ذخیره و فعال شد" else "${selectedProvider.displayNameEn} API key saved & activated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themePalette.primary,
                        contentColor = themePalette.buttonTextColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier
                        .then(if (hasCustomKey || enteredKey.isNotBlank()) Modifier.weight(1.3f) else Modifier.fillMaxWidth())
                        .height(35.dp)
                        .testTag("btn_save_active_ai_key")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPersianRtl) "ذخیره و فعال‌سازی" else "Save & Activate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (hasCustomKey || enteredKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            enteredKey = ""
                            onClearProviderKey(selectedProvider.id)
                            Toast.makeText(
                                context,
                                if (isPersianRtl) "کلید ${selectedProvider.displayNameFa} حذف شد" else "${selectedProvider.displayNameEn} key removed",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RoseMute
                        ),
                        border = BorderStroke(1.dp, RoseMute.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(35.dp)
                            .testTag("btn_clear_active_ai_key")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = RoseMute,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPersianRtl) "حذف کلید" else "Remove",
                            color = RoseMute,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Helpful footer link/hint specific to selected provider
            Text(
                text = if (isPersianRtl)
                    "💡 دریافت کلید: وارد کنسول ${selectedProvider.helpUrl} شوید."
                else
                    "💡 Get your ${selectedProvider.displayNameEn} key at: ${selectedProvider.helpUrl}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = themePalette.textMuted.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun GeminiApiKeySettingsCard(
    customApiKey: String,
    hasValidApiKey: Boolean,
    onSaveCustomApiKey: (String) -> Unit,
    onClearCustomApiKey: () -> Unit,
    isPersianRtl: Boolean
) {
    MultiAiProviderSettingsCard(
        activeProviderId = com.example.ai.AiProvider.GEMINI.id,
        customGeminiApiKey = customApiKey,
        customOpenAiApiKey = "",
        customDeepSeekApiKey = "",
        customGroqApiKey = "",
        hasValidAiApiKey = hasValidApiKey,
        onSelectProvider = { },
        onSaveProviderKey = { _, key -> onSaveCustomApiKey(key) },
        onClearProviderKey = { _ -> onClearCustomApiKey() },
        isPersianRtl = isPersianRtl
    )
}
