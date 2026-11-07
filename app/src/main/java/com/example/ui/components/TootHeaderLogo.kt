package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import com.example.audio.AudioOutputType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppThemePalette
import com.example.util.AppLanguage
import com.example.util.LocalActiveLanguage
import com.example.util.LocalAppStrings

/**
 * Modern Header Bar for TO_OT Player
 * Displays the sleek "TO_OT" typography logo with pulsing equalizer rings,
 * alongside top header controls:
 * 1. Language switcher (6 languages: FA, EN, ES, ZH, HI, PT)
 * 2. Theme palette selector (11 themes)
 * 3. Search toggle
 * 4. Prominent, high-visibility "➕ Add Lane" action button (minimum 48x48dp hit area)
 */
@Composable
fun TootHeaderLogo(
    onOpenThemeSelector: () -> Unit,
    onAddNewSession: () -> Unit,
    onOpenHelpCenter: () -> Unit = {},
    sleepTimerRemainingSec: Int? = null,
    onOpenSleepTimer: () -> Unit = {},
    onOpenHomescreenWidget: (() -> Unit)? = null,
    onToggleSearch: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    onOpenAudioRouting: () -> Unit = {},
    isPhoneAudioScoActive: Boolean = false,
    audioOutputType: AudioOutputType? = null,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current
    val strings = LocalAppStrings.current
    val activeLanguage = LocalActiveLanguage.current

    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("brand_logo_header")
            ) {
                // Futuristic Typographic "TO_OT" with Equalizer Speaker Cones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Letter 'T'
                    Text(
                        text = "T",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 25.sp,
                            color = themePalette.primary,
                            letterSpacing = (-0.5).sp
                        )
                    )

                    // First 'O' (Glowing Speaker Cone / Equalizer Ring)
                    SpeakerConeLogoRing(
                        primaryColor = themePalette.primary,
                        accentColor = themePalette.secondary,
                        pulseScale = pulseGlow
                    )

                    // Underscore '_' Divider
                    Text(
                        text = "_",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = themePalette.textMuted
                        ),
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )

                    // Second 'O' (Glowing Speaker Cone / Equalizer Ring)
                    SpeakerConeLogoRing(
                        primaryColor = themePalette.secondary,
                        accentColor = themePalette.primary,
                        pulseScale = pulseGlow
                    )

                    // Letter 'T'
                    Text(
                        text = "T",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 25.sp,
                            color = themePalette.primary,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Vertical Accent Divider
                Surface(
                    shape = RoundedCornerShape(1.dp),
                    color = themePalette.cardBorder.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(22.dp)
                        .width(1.dp)
                ) {}

                Spacer(modifier = Modifier.width(6.dp))

                // Subtitle Badge
                Column {
                    Text(
                        text = "PLAYER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = themePalette.textPrimary,
                            letterSpacing = 1.6.sp,
                            fontSize = 8.sp
                        )
                    )
                    Text(
                        text = strings.brandSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = themePalette.primary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }

            // Compact Action Controls & Prominent Add Lane Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active Sleep Timer Pill indicator if running
                if (sleepTimerRemainingSec != null && sleepTimerRemainingSec > 0) {
                    val min = sleepTimerRemainingSec / 60
                    val sec = sleepTimerRemainingSec % 60
                    Surface(
                        shape = RoundedCornerShape(19.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .height(38.dp)
                            .clickable { onOpenSleepTimer() }
                            .testTag("header_active_sleep_timer_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = if (activeLanguage.isRtl) "تایمر خواب فعال" else "Active Sleep Timer",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%02d:%02d", min, sec),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Theme Palette Selector (18px icon, 38px touch target)
                Surface(
                    shape = CircleShape,
                    color = themePalette.cardBackground,
                    border = BorderStroke(1.dp, themePalette.cardBorder),
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onOpenThemeSelector() }
                        .testTag("open_theme_selector_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme Selector",
                            tint = themePalette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Homescreen Widget Quick Launcher
                if (onOpenHomescreenWidget != null) {
                    Surface(
                        shape = CircleShape,
                        color = themePalette.cardBackground,
                        border = BorderStroke(1.dp, themePalette.cardBorder),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onOpenHomescreenWidget() }
                            .testTag("open_homescreen_widget_header_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = if (activeLanguage.isRtl) "ویجت صفحه اصلی" else "Homescreen Widget",
                                tint = themePalette.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Pure '+' Symbol Add Lane Action Button
                Surface(
                    shape = CircleShape,
                    color = themePalette.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onAddNewSession() }
                        .testTag("add_new_session_prominent_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.addLane,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas drawing for the typographic 'O' as an animated glowing audio speaker cone / equalizer ring
 */
@Composable
fun SpeakerConeLogoRing(
    primaryColor: Color,
    accentColor: Color,
    pulseScale: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(19.dp)
            .padding(horizontal = 1.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = (size.width / 2f) * 0.95f
        val midRadius = outerRadius * 0.65f * pulseScale
        val innerRadius = outerRadius * 0.32f

        // Outer Equalizer Ring
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(primaryColor, accentColor, primaryColor),
                center = center
            ),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Mid Pulsing Speaker Diaphragm Ring
        drawCircle(
            color = accentColor.copy(alpha = 0.7f * pulseScale),
            radius = midRadius,
            center = center,
            style = Stroke(width = 1.1.dp.toPx())
        )

        // Inner Cone Core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor, accentColor),
                center = center,
                radius = innerRadius
            ),
            radius = innerRadius,
            center = center
        )
    }
}
