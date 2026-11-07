package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.audio.AudioLaneState
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.ui.theme.ThemeStyledCard
import com.example.util.getLocalizedSessionName
import java.io.File

/**
 * Universal Theme-Adaptive Session Card for TO_OT Player
 * Implements 11 Distinct Visual Paradigms from User Screenshot Designs
 */
@Composable
fun PersistentSessionCard(
    lane: AudioLaneState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onResumeOrPlay: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenDeviceAudioExplorer: () -> Unit,
    onRenameClick: () -> Unit,
    onColorPickClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleLoop: () -> Unit,
    onTogglePlaylistLoop: () -> Unit = {},
    onToggleShuffle: () -> Unit,
    onFileSelected: (Uri) -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    isPersianRtl: Boolean = false,
    isDragging: Boolean = false,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((Float) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themePalette = LocalAppThemePalette.current

    var showQuickMenu by remember { mutableStateOf(false) }
    var showVolumeSlider by remember { mutableStateOf(false) }
    var volumeInteractionTrigger by remember { mutableStateOf(0L) }

    val toggleVolumeSlider: () -> Unit = {
        showVolumeSlider = !showVolumeSlider
        if (showVolumeSlider) {
            volumeInteractionTrigger = System.currentTimeMillis()
        }
    }

    // Auto-dismiss volume slider after 6 seconds of inactivity
    LaunchedEffect(showVolumeSlider, volumeInteractionTrigger) {
        if (showVolumeSlider) {
            kotlinx.coroutines.delay(6000L)
            showVolumeSlider = false
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onFileSelected(it) }
    }

    val progressFraction = if (lane.durationMs > 0) {
        (lane.currentPositionMs.toFloat() / lane.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val category = SessionCategoryType.fromName(lane.name)
    val categoryIcon: ImageVector = when (category) {
        SessionCategoryType.PODCAST -> Icons.Default.Mic
        SessionCategoryType.WORKOUT -> Icons.Default.FitnessCenter
        SessionCategoryType.AUDIOBOOK -> Icons.Default.AutoStories
        SessionCategoryType.NIGHT_CHILL -> Icons.Default.NightsStay
        SessionCategoryType.FOCUS -> Icons.Default.SelfImprovement
        SessionCategoryType.CUSTOM -> Icons.Default.Audiotrack
    }

    // Dynamic wave animation for playing sessions
    val infiniteTransition = rememberInfiniteTransition(label = "card_wave_anim")
    val waveAnimPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val laneAccentColor = remember(lane.colorHex) {
        lane.colorHex?.let {
            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { null }
        }
    }

    val cardAccent = when (themePalette.style) {
        AppThemeStyle.MODERN_BRUTALISM -> Color.Black
        AppThemeStyle.MINIMALIST_MONOCHROME -> Color.White
        AppThemeStyle.RETRO_TERMINAL -> Color(0xFF33FF66)
        AppThemeStyle.CYBERPUNK_NEON -> Color(0xFF00F0FF)
        AppThemeStyle.COSMIC_AURORA -> Color(0xFF00F5D4)
        AppThemeStyle.EARTHY_BIOPHILIC -> Color(0xFFE89274)
        AppThemeStyle.DARK_LUXURY -> Color(0xFFD4AF37)
        AppThemeStyle.CRIMSON_CARBON -> Color(0xFFFF1E40)
        AppThemeStyle.SCARLET_STUDIO -> Color(0xFF1A0206)
        AppThemeStyle.RETRO_ANALOG -> Color(0xFFE8C87A)
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Color(0xFFD4AF37)
        AppThemeStyle.NEUMORPHIC_GLASS -> Color(0xFF06B6D4)
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Color(0xFF00F5D4)
    }

    val effectiveAccent = laneAccentColor ?: cardAccent

    val cardTextPrimary = themePalette.textPrimary
    val cardTextSecondary = themePalette.textSecondary
    val cardTextMuted = themePalette.textMuted

    val cardBorder = when {
        laneAccentColor != null && (lane.isPlaying || isSelected) -> BorderStroke(2.2.dp, laneAccentColor)
        laneAccentColor != null -> BorderStroke(1.4.dp, laneAccentColor.copy(alpha = 0.85f))
        else -> null
    }

    ThemeStyledCard(
        isPlaying = lane.isPlaying,
        isSelected = isSelected,
        border = cardBorder,
        onClick = { onSelect() },
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_card_${lane.laneId}"),
        contentPadding = 10.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Artwork Thumbnail
                val currentTrackUri = lane.uriString
                    ?: lane.playlist.getOrNull(lane.currentTrackIndex)?.uriString
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onResumeOrPlay() }
                ) {
                    SessionArtwork(
                        sessionName = lane.name,
                        isPlaying = lane.isPlaying,
                        trackUriString = currentTrackUri,
                        categoryType = category,
                        modifier = Modifier
                            .size(64.dp)
                            .then(
                                if (laneAccentColor != null) {
                                    Modifier.border(
                                        1.5.dp,
                                        laneAccentColor.copy(alpha = if (lane.isPlaying) 1f else 0.75f),
                                        if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            ),
                        cornerRadius = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) 0.dp else 12.dp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Middle: Session Info & Waveform/Progress
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Header Row: Category Icon + Title + NOW PLAYING Badge + 3-dots Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            if (!themePalette.hasTerminalAscii) {
                                Surface(
                                    shape = CircleShape,
                                    color = effectiveAccent.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, effectiveAccent.copy(alpha = 0.6f)),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = categoryIcon,
                                            contentDescription = null,
                                            tint = effectiveAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // Interactive Lane Color Accent Dot/Button
                            Surface(
                                shape = CircleShape,
                                color = effectiveAccent,
                                border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onColorPickClick() }
                                    .testTag("color_pick_lane_${lane.laneId}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = if (isPersianRtl) "رنگ اختصاصی لاین (Color Accent)" else "Lane Color Accent",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            val rawTitle = getLocalizedSessionName(lane.name, isPersianRtl)
                            val displayTitle = if (themePalette.hasTerminalAscii) {
                                "[[*]] $rawTitle ★"
                            } else {
                                rawTitle
                            }

                            Text(
                                text = displayTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (themePalette.hasTerminalAscii) FontFamily.Monospace else FontFamily.Default,
                                    color = cardTextPrimary,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (lane.isPlaying) {
                                Surface(
                                    shape = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else RoundedCornerShape(50),
                                    color = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else effectiveAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else effectiveAccent.copy(alpha = 0.6f)),
                                    modifier = Modifier.padding(end = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        if (!themePalette.hasTerminalAscii) {
                                            Canvas(modifier = Modifier.size(width = 9.dp, height = 7.dp)) {
                                                val barW = 1.3.dp.toPx()
                                                val barSp = 1.0.dp.toPx()
                                                val h = size.height
                                                for (b in 0..2) {
                                                    val bx = b * (barW + barSp)
                                                    val barH = (h * (0.35f + 0.65f * kotlin.math.sin(waveAnimPhase + b * 1.5f).coerceIn(0f, 1f))).coerceAtLeast(2.dp.toPx())
                                                    drawRoundRect(
                                                        color = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color(0xFFFFE500) else effectiveAccent,
                                                        topLeft = Offset(bx, h - barH),
                                                        size = Size(barW, barH),
                                                        cornerRadius = CornerRadius(1.dp.toPx())
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(
                                            text = if (themePalette.hasTerminalAscii) "[[NOW PLAYING]]" else if (isPersianRtl) "در حال پخش" else "NOW PLAYING",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color(0xFFFFE500) else effectiveAccent,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = if (themePalette.hasTerminalAscii) FontFamily.Monospace else FontFamily.Default,
                                                fontSize = 7.5.sp,
                                                letterSpacing = 0.4.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Tactile Mobile Reordering Controls (Only explicit Up ▲ / Down ▼ markers)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (canMoveUp) cardAccent.copy(alpha = 0.12f) else Color.Transparent,
                                    border = if (canMoveUp) BorderStroke(0.8.dp, cardAccent.copy(alpha = 0.35f)) else null,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    IconButton(
                                        onClick = onMoveUp,
                                        enabled = canMoveUp,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .testTag("move_up_session_${lane.laneId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = if (isPersianRtl) "انتقال نشست به بالا" else "Move Session Up",
                                            tint = if (canMoveUp) cardAccent else cardTextMuted.copy(alpha = 0.2f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (canMoveDown) cardAccent.copy(alpha = 0.12f) else Color.Transparent,
                                    border = if (canMoveDown) BorderStroke(0.8.dp, cardAccent.copy(alpha = 0.35f)) else null,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    IconButton(
                                        onClick = onMoveDown,
                                        enabled = canMoveDown,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .testTag("move_down_session_${lane.laneId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isPersianRtl) "انتقال نشست به پایین" else "Move Session Down",
                                            tint = if (canMoveDown) cardAccent else cardTextMuted.copy(alpha = 0.2f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            // 3-dots Quick Menu
                            Box {
                                IconButton(
                                    onClick = { showQuickMenu = true },
                                    modifier = Modifier
                                        .size(22.dp)
                                        .testTag("session_menu_${lane.laneId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Session Options",
                                        tint = cardTextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showQuickMenu,
                                    onDismissRequest = { showQuickMenu = false },
                                    modifier = Modifier.background(themePalette.cardBackground)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (isPersianRtl) "اشتراک‌گذاری آهنگ" else "Share Music", color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = themePalette.primary) },
                                        onClick = {
                                            showQuickMenu = false
                                            val uriString = lane.uriString ?: lane.playlist.getOrNull(lane.currentTrackIndex)?.uriString ?: lane.playlist.firstOrNull()?.uriString
                                            if (uriString != null) {
                                                try {
                                                    val uri = if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                                                        Uri.parse(uriString)
                                                    } else {
                                                        val file = File(uriString)
                                                        if (file.exists()) {
                                                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                        } else {
                                                            Uri.parse(uriString)
                                                        }
                                                    }
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "audio/*"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        putExtra(Intent.EXTRA_SUBJECT, lane.fileName ?: lane.name)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, if (isPersianRtl) "اشتراک‌گذاری آهنگ" else "Share Music"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        if (isPersianRtl) "خطا در اشتراک‌گذاری: ${e.localizedMessage}" else "Share error: ${e.localizedMessage}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    if (isPersianRtl) "ابتدا یک فایل صوتی برای این لاین انتخاب کنید" else "No audio file in this lane to share",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isPersianRtl) "تنظیم ولوم لاین" else "Lane Volume Control", color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null, tint = themePalette.primary) },
                                        onClick = {
                                            showQuickMenu = false
                                            toggleVolumeSlider()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (lane.isMuted) (if (isPersianRtl) "لغو بی‌صدا" else "Unmute") else (if (isPersianRtl) "بی‌صدا کردن" else "Mute"), color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(if (lane.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = null, tint = themePalette.textSecondary) },
                                        onClick = {
                                            showQuickMenu = false
                                            onToggleMute()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (lane.isLoopEnabled) (if (isPersianRtl) "تکرار موزیک جاری: فعال 🔁" else "Track Loop: ON 🔁") else (if (isPersianRtl) "تکرار موزیک جاری: غیرفعال" else "Track Loop: OFF"), color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Loop, contentDescription = null, tint = if (lane.isLoopEnabled) themePalette.primary else themePalette.textMuted) },
                                        onClick = {
                                            showQuickMenu = false
                                            onToggleLoop()
                                            val msg = if (!lane.isLoopEnabled) {
                                                if (isPersianRtl) "تکرار موزیک جاری فعال شد 🔁" else "Track loop enabled 🔁"
                                            } else {
                                                if (isPersianRtl) "تکرار موزیک جاری غیرفعال شد" else "Track loop disabled"
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (lane.isPlaylistLoopEnabled) (if (isPersianRtl) "تکرار کل لاین (پلی‌لیست): فعال 🔁" else "Repeat Lane Playlist: ON 🔁") else (if (isPersianRtl) "تکرار کل لاین (پلی‌لیست): غیرفعال" else "Repeat Lane Playlist: OFF"), color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null, tint = if (lane.isPlaylistLoopEnabled) themePalette.primary else themePalette.textMuted) },
                                        onClick = {
                                            showQuickMenu = false
                                            onTogglePlaylistLoop()
                                            val msg = if (!lane.isPlaylistLoopEnabled) {
                                                if (isPersianRtl) "تکرار کل لاین فعال شد: پس از پایان پلی‌لیست از اول شروع می‌شود 🔁" else "Lane playlist repeat enabled: restarts from track 1 upon finish 🔁"
                                            } else {
                                                if (isPersianRtl) "تکرار کل لاین غیرفعال شد" else "Lane playlist repeat disabled"
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (lane.isShuffleEnabled) (if (isPersianRtl) "پخش تصادفی: فعال 🔀" else "Shuffle: ON 🔀") else (if (isPersianRtl) "پخش تصادفی: خاموش" else "Shuffle: OFF"), color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Shuffle, contentDescription = null, tint = if (lane.isShuffleEnabled) themePalette.secondary else themePalette.textMuted) },
                                        onClick = {
                                            showQuickMenu = false
                                            onToggleShuffle()
                                            val msg = if (!lane.isShuffleEnabled) {
                                                if (isPersianRtl) "پخش تصادفی فعال شد 🔀" else "Shuffle enabled 🔀"
                                            } else {
                                                if (isPersianRtl) "پخش تصادفی غیرفعال شد" else "Shuffle disabled"
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isPersianRtl) "تغییر نام نشست" else "Rename Session", color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = themePalette.textSecondary) },
                                        onClick = {
                                            showQuickMenu = false
                                            onRenameClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isPersianRtl) "رنگ اختصاصی لاین (Color Accent)" else "Lane Color Accent", color = themePalette.textPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = themePalette.secondary) },
                                        onClick = {
                                            showQuickMenu = false
                                            onColorPickClick()
                                        }
                                    )
                                    HorizontalDivider(color = themePalette.cardBorder)
                                    DropdownMenuItem(
                                        text = { Text(if (isPersianRtl) "حذف این نشست" else "Delete Session", color = RoseMute) },
                                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = RoseMute) },
                                        onClick = {
                                            showQuickMenu = false
                                            onDeleteClick()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Subtitle
                    val subtitleText = when {
                        !lane.fileName.isNullOrBlank() -> lane.fileName
                        lane.playlist.isNotEmpty() -> lane.playlist.getOrNull(lane.currentTrackIndex)?.title ?: "${lane.playlist.size} ${if (isPersianRtl) "آهنگ" else "tracks"}"
                        else -> if (isPersianRtl) "آماده برای پخش" else "Ready to play"
                    }

                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = cardTextSecondary,
                            fontFamily = if (themePalette.hasTerminalAscii) FontFamily.Monospace else FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Position / Duration / Metadata Row (Strictly zero when initialized)
                    val metadataString = when {
                        lane.durationMs > 0 -> "${formatSessionCardTime(lane.currentPositionMs)} / ${formatSessionCardTime(lane.durationMs)}"
                        lane.playlist.isNotEmpty() -> "${lane.playlist.size} ${if (isPersianRtl) "آهنگ" else "tracks"}"
                        else -> "00:00 / 00:00"
                    }

                    Text(
                        text = metadataString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = effectiveAccent,
                            fontFamily = if (themePalette.hasTerminalAscii) FontFamily.Monospace else FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )

                    // Graceful Non-Intrusive Error Chip
                    if (!lane.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RoseMute.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, RoseMute.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = lane.errorMessage ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RoseMute,
                                        fontSize = 9.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Soundwave Visualizer / ASCII Progress Bar (Strictly 0 on initial launch)
                    if (themePalette.hasTerminalAscii) {
                        val progressBlocks = (progressFraction * 14).toInt().coerceIn(0, 14)
                        val asciiTrack = if (progressBlocks > 0) "#".repeat(progressBlocks).padEnd(14, '.') else ".".repeat(14)
                        Text(
                            text = asciiTrack,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = effectiveAccent,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp
                            )
                        )
                    } else if (lane.isPlaying) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(11.dp)
                        ) {
                            val barCount = 26
                            val totalW = size.width
                            val barWidth = 2.2.dp.toPx()
                            val spacing = (totalW - barCount * barWidth) / (barCount - 1).coerceAtLeast(1)
                            val maxH = size.height

                            for (i in 0 until barCount) {
                                val bx = i * (barWidth + spacing)
                                val normPos = i.toFloat() / barCount
                                val dynamicFactor = (0.25f + 0.75f * kotlin.math.abs(kotlin.math.sin(waveAnimPhase + normPos * 5f))).coerceIn(0.15f, 1.0f)
                                val bh = (maxH * dynamicFactor).coerceAtLeast(2.dp.toPx())
                                val by = (maxH - bh) / 2f

                                drawRoundRect(
                                    color = effectiveAccent,
                                    topLeft = Offset(bx, by),
                                    size = Size(barWidth, bh),
                                    cornerRadius = CornerRadius(1.2.dp.toPx())
                                )
                            }
                        }
                    } else {
                        val effectiveProgress = progressFraction

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val cy = h / 2f
                            val trackH = 2.5.dp.toPx()
                            val knobR = 3.8.dp.toPx()

                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.15f),
                                topLeft = Offset(0f, cy - trackH / 2f),
                                size = Size(w, trackH),
                                cornerRadius = CornerRadius(trackH / 2f)
                            )

                            val activeW = (w * effectiveProgress).coerceIn(0f, w)
                            if (activeW > 0) {
                                drawRoundRect(
                                    color = effectiveAccent,
                                    topLeft = Offset(0f, cy - trackH / 2f),
                                    size = Size(activeW, trackH),
                                    cornerRadius = CornerRadius(trackH / 2f)
                                )
                                drawCircle(
                                    color = effectiveAccent,
                                    radius = knobR,
                                    center = Offset(activeW, cy)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(activeW, cy)
                                )
                            }
                        }
                    }
                }

                // If Vintage Analog Hi-Fi (Image 4), show Analog Backlit VU Meter on the right!
                if (themePalette.hasVUMeter) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AnalogVuMeter(isPlaying = lane.isPlaying)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Controls Row: Resume/Play Action Button + Secondary Actions (or VU Brass Controls)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Resume Action Button
                val resumeLabel = when {
                    themePalette.hasTerminalAscii -> if (lane.isPlaying) "[[PAUSE]]" else "[[PLAY]]"
                    lane.isPlaying -> if (isPersianRtl) "توقف موقت" else "Pause"
                    lane.currentPositionMs > 1000L -> {
                        val pos = formatSessionCardTime(lane.currentPositionMs)
                        if (isPersianRtl) "ادامه از $pos" else "Resume from $pos"
                    }
                    lane.fileName != null || lane.playlist.isNotEmpty() -> if (isPersianRtl) "شروع پخش" else "Start Playing"
                    else -> if (isPersianRtl) "+ افزودن صوت" else "+ Add Audio"
                }

                val buttonShape = when {
                    themePalette.style == AppThemeStyle.MODERN_BRUTALISM -> RoundedCornerShape(0.dp)
                    themePalette.hasTerminalAscii -> RoundedCornerShape(4.dp)
                    else -> RoundedCornerShape(50)
                }

                Surface(
                    shape = buttonShape,
                    color = when {
                        themePalette.style == AppThemeStyle.MODERN_BRUTALISM -> Color(0xFFFFE500)
                        lane.isPlaying -> cardAccent.copy(alpha = 0.25f)
                        else -> cardAccent.copy(alpha = 0.12f)
                    },
                    border = BorderStroke(
                        if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) 2.dp else 1.dp,
                        if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent
                    ),
                    modifier = Modifier
                        .clickable {
                            if (lane.fileName == null && lane.playlist.isEmpty() && category == SessionCategoryType.CUSTOM) {
                                filePickerLauncher.launch(arrayOf("audio/*"))
                            } else {
                                onResumeOrPlay()
                            }
                        }
                        .testTag("session_resume_button_${lane.laneId}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (lane.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = resumeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent,
                                fontWeight = FontWeight.Bold,
                                fontFamily = if (themePalette.hasTerminalAscii) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Right Side Controls: Either Vintage Hi-Fi Brass Knobs or Standard Queue & Info Buttons
                if (themePalette.hasVUMeter) {
                    AnalogBrassControls(
                        onMixClick = { onOpenPlaylist() },
                        onLevelClick = { toggleVolumeSlider() },
                        onLoopClick = {
                            onToggleLoop()
                            val msg = if (!lane.isLoopEnabled) {
                                if (isPersianRtl) "تکرار موزیک جاری فعال شد 🔁" else "Track loop enabled 🔁"
                            } else {
                                if (isPersianRtl) "تکرار موزیک جاری غیرفعال شد" else "Track loop disabled"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        isLoopEnabled = lane.isLoopEnabled,
                        onPlaylistLoopClick = {
                            onTogglePlaylistLoop()
                            val msg = if (!lane.isPlaylistLoopEnabled) {
                                if (isPersianRtl) "تکرار کل لاین فعال شد: شروع مجدد از اولین موزیک 🔁" else "Repeat lane playlist enabled: restarts from first track 🔁"
                            } else {
                                if (isPersianRtl) "تکرار کل لاین غیرفعال شد" else "Lane playlist repeat disabled"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        isPlaylistLoopEnabled = lane.isPlaylistLoopEnabled
                    )
                } else if (themePalette.hasTerminalAscii) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = if (lane.isLoopEnabled) "[TRK:ON]" else "[TRK:LOOP]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (lane.isLoopEnabled) themePalette.primary else cardAccent.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (lane.isLoopEnabled) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier
                                .clickable {
                                    onToggleLoop()
                                    val msg = if (!lane.isLoopEnabled) {
                                        if (isPersianRtl) "تکرار موزیک جاری فعال شد 🔁" else "Track loop enabled 🔁"
                                    } else {
                                        if (isPersianRtl) "تکرار موزیک جاری غیرفعال شد" else "Track loop disabled"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Text(
                            text = if (lane.isPlaylistLoopEnabled) "[PL:ON]" else "[PL:LOOP]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (lane.isPlaylistLoopEnabled) themePalette.primary else cardAccent.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (lane.isPlaylistLoopEnabled) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier
                                .clickable {
                                    onTogglePlaylistLoop()
                                    val msg = if (!lane.isPlaylistLoopEnabled) {
                                        if (isPersianRtl) "تکرار کل لاین فعال شد: شروع مجدد از اولین موزیک 🔁" else "Repeat lane playlist enabled: restarts from first track 🔁"
                                    } else {
                                        if (isPersianRtl) "تکرار کل لاین غیرفعال شد" else "Lane playlist repeat disabled"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "[CMD:LIST]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = cardAccent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp
                            ),
                            modifier = Modifier
                                .clickable { onOpenPlaylist() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "[VOL]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = cardAccent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp
                            ),
                            modifier = Modifier
                                .clickable { toggleVolumeSlider() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Track Loop Toggle Button (repeats the single playing track)
                        val isLoopActive = lane.isLoopEnabled
                        val loopBg = if (isLoopActive) {
                            if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color(0xFFFFE500) else cardAccent
                        } else {
                            cardAccent.copy(alpha = 0.12f)
                        }
                        val loopBorder = if (isLoopActive) {
                            BorderStroke(1.5.dp, if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent)
                        } else {
                            BorderStroke(1.dp, cardAccent.copy(alpha = 0.35f))
                        }
                        val loopIconTint = if (isLoopActive) {
                            Color(0xFF0F172A)
                        } else {
                            cardAccent.copy(alpha = 0.70f)
                        }

                        Surface(
                            onClick = {
                                onToggleLoop()
                                val msg = if (!lane.isLoopEnabled) {
                                    if (isPersianRtl) "تکرار موزیک جاری فعال شد 🔁" else "Track loop enabled 🔁"
                                } else {
                                    if (isPersianRtl) "تکرار موزیک جاری غیرفعال شد" else "Track loop disabled"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            shape = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else CircleShape,
                            color = loopBg,
                            border = loopBorder,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("session_loop_button_${lane.laneId}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Loop,
                                    contentDescription = if (lane.isLoopEnabled) (if (isPersianRtl) "تکرار موزیک در حال پخش فعال است" else "Track Loop Enabled") else (if (isPersianRtl) "تکرار موزیک در حال پخش خاموش است" else "Track Loop Disabled"),
                                    tint = loopIconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Lane Playlist Loop Toggle Button (restarts from track 1 upon playlist finish)
                        val isPlLoopActive = lane.isPlaylistLoopEnabled
                        val plLoopBg = if (isPlLoopActive) {
                            if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color(0xFFFFE500) else cardAccent
                        } else {
                            cardAccent.copy(alpha = 0.12f)
                        }
                        val plLoopBorder = if (isPlLoopActive) {
                            BorderStroke(1.5.dp, if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent)
                        } else {
                            BorderStroke(1.dp, cardAccent.copy(alpha = 0.35f))
                        }
                        val plLoopIconTint = if (isPlLoopActive) {
                            Color(0xFF0F172A)
                        } else {
                            cardAccent.copy(alpha = 0.70f)
                        }

                        Surface(
                            onClick = {
                                onTogglePlaylistLoop()
                                val msg = if (!lane.isPlaylistLoopEnabled) {
                                    if (isPersianRtl) "تکرار کل لاین فعال شد: شروع مجدد از اولین موزیک 🔁" else "Repeat lane playlist enabled: restarts from first track 🔁"
                                } else {
                                    if (isPersianRtl) "تکرار کل لاین غیرفعال شد" else "Lane playlist repeat disabled"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            shape = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else CircleShape,
                            color = plLoopBg,
                            border = plLoopBorder,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("session_playlist_loop_button_${lane.laneId}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = if (lane.isPlaylistLoopEnabled) (if (isPersianRtl) "تکرار کل لاین فعال است" else "Repeat Lane Playlist Enabled") else (if (isPersianRtl) "تکرار کل لاین خاموش است" else "Repeat Lane Playlist Disabled"),
                                    tint = plLoopIconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Queue / Playlist Button
                        Surface(
                            onClick = { onOpenPlaylist() },
                            shape = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else CircleShape,
                            color = cardAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("session_queue_button_${lane.laneId}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "Queue",
                                    tint = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Volume Toggle Button
                        Surface(
                            onClick = { toggleVolumeSlider() },
                            shape = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else CircleShape,
                            color = cardAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("session_volume_button_${lane.laneId}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) Color.Black else cardAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Volume Slider Row
            AnimatedVisibility(
                visible = showVolumeSlider,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = cardAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val volumeFraction = lane.volume.coerceIn(0f, 1f)
                    // Faint/translucent when volume is low, bold/rich/opaque when volume is high
                    val faintVolumeColor = cardAccent.copy(alpha = 0.22f)
                    val boldVolumeColor = cardAccent.copy(alpha = 1.0f)
                    val dynamicActiveColor = androidx.compose.ui.graphics.lerp(faintVolumeColor, boldVolumeColor, volumeFraction)

                    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                    Slider(
                        value = lane.volume,
                        onValueChange = {
                            onVolumeChange(it)
                            volumeInteractionTrigger = System.currentTimeMillis()
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        thumb = {
                            Surface(
                                shape = CircleShape,
                                color = dynamicActiveColor,
                                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.95f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(18.dp)
                            ) {}
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = dynamicActiveColor,
                                    inactiveTrackColor = cardAccent.copy(alpha = 0.15f)
                                ),
                                thumbTrackGapSize = 0.dp,
                                drawStopIndicator = null
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(lane.volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = cardTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

private fun formatSessionCardTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
