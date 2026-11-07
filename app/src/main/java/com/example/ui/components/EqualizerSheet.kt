package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioLaneState
import com.example.audio.EqPreset
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    eqEnabled: Boolean,
    eqBands: List<Float>,
    currentPresetName: String,
    presets: List<EqPreset>,
    masterFrequencies: FloatArray = FloatArray(24) { 0.05f },
    laneFrequencies: Map<Long, FloatArray> = emptyMap(),
    lanes: List<AudioLaneState> = emptyList(),
    isPlaying: Boolean = false,
    onToggleEnabled: (Boolean) -> Unit,
    onBandChange: (Int, Float) -> Unit,
    onSelectPreset: (EqPreset) -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false
) {
    val themePalette = LocalAppThemePalette.current

    val laneWaveformColors = listOf(
        themePalette.primary,
        themePalette.secondary,
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFFF59E0B), // Amber
        Color(0xFFEC4899), // Pink
        Color(0xFFA855F7), // Purple
        Color(0xFF10B981)  // Emerald
    )

    val bandNames = if (isPersianRtl) {
        listOf("بیس (60Hz)", "مید پایین (250Hz)", "مید (1kHz)", "مید بالا (4kHz)", "تریبل (14kHz)")
    } else {
        listOf("Bass (60Hz)", "Low-Mid (250Hz)", "Mid (1kHz)", "High-Mid (4kHz)", "Treble (14kHz)")
    }

    val activePlayingLanes = lanes.filter { it.isPlaying }
    val scrollState = rememberScrollState()

    // Smooth wave oscillation phase animation
    val infiniteTransition = rememberInfiniteTransition(label = "eq_wave_anim")
    val waveAnimPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = themePalette.bgCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(scrollState)
                .testTag("equalizer_sheet")
        ) {
            // Sheet Title & Bypass Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = themePalette.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersianRtl) "اکولایزر ۵ بانده مستر" else "5-Band Master Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (eqEnabled) (if (isPersianRtl) "فعال" else "ON") else (if (isPersianRtl) "غیرفعال" else "BYPASS"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (eqEnabled) themePalette.primary else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = eqEnabled,
                        onCheckedChange = onToggleEnabled,
                        modifier = Modifier.testTag("eq_bypass_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = themePalette.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Waveform & Frequency Analyzer Section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themePalette.cardBackground.copy(alpha = 0.95f),
                border = BorderStroke(1.2.dp, if (isPlaying) themePalette.primary.copy(alpha = 0.6f) else themePalette.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("eq_realtime_waveform_visualizer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Visualizer Header: Label + Active Lanes Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = null,
                                tint = themePalette.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "شکل‌موج و فرکانس‌های زنده لاین‌ها" else "Real-Time Lane Waveforms & Frequency",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isPlaying) themePalette.primary.copy(alpha = 0.2f) else themePalette.glassSurfaceElevated,
                            border = BorderStroke(0.5.dp, if (isPlaying) themePalette.primary.copy(alpha = 0.8f) else themePalette.glassBorder)
                        ) {
                            Text(
                                text = if (isPlaying) {
                                    if (isPersianRtl) "${activePlayingLanes.size} لاین فعال" else "${activePlayingLanes.size} ACTIVE LANES"
                                } else {
                                    if (isPersianRtl) "آماده‌به‌کار" else "STANDBY"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isPlaying) themePalette.primary else TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real-Time Canvas: Multi-Lane Waveforms + EQ Frequency Curve + Live Spectrum
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            // 1. Draw Grid Lines (dB references: +12dB, 0dB, -12dB)
                            val gridColor = Color.White.copy(alpha = 0.07f)
                            drawLine(gridColor, Offset(0f, h * 0.2f), Offset(w, h * 0.2f), strokeWidth = 1.dp.toPx())
                            drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, midY), Offset(w, midY), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(0f, h * 0.8f), Offset(w, h * 0.8f), strokeWidth = 1.dp.toPx())

                            // 5 Frequency Grid Vertical Lines
                            for (f in 0 until 5) {
                                val fx = (f + 0.5f) * (w / 5f)
                                drawLine(gridColor, Offset(fx, 0f), Offset(fx, h), strokeWidth = 1.dp.toPx())
                            }

                            // 2. Draw Real-Time Frequency Spectrum Bars in Background
                            val numBars = masterFrequencies.size
                            if (numBars > 0) {
                                val barWidth = (w / numBars) * 0.65f
                                val barGap = (w / numBars) * 0.35f
                                for (i in 0 until numBars) {
                                    val amp = masterFrequencies[i].coerceIn(0.04f, 1.0f)
                                    val barH = h * 0.85f * amp
                                    val bx = i * (barWidth + barGap)
                                    val by = h - barH

                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                themePalette.primary.copy(alpha = if (isPlaying) 0.55f else 0.15f),
                                                themePalette.secondary.copy(alpha = if (isPlaying) 0.35f else 0.08f)
                                            ),
                                            startY = by,
                                            endY = h
                                        ),
                                        topLeft = Offset(bx, by),
                                        size = Size(barWidth, barH),
                                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                    )
                                }
                            }

                            // 3. Draw Per-Active-Lane Real-Time Waveforms
                            if (activePlayingLanes.isNotEmpty()) {
                                activePlayingLanes.forEachIndexed { laneIndex, lane ->
                                    val laneFreqs = laneFrequencies[lane.laneId] ?: FloatArray(16) { 0.1f }
                                    val laneColor = laneWaveformColors[laneIndex % laneWaveformColors.size]
                                    val laneWavePath = Path()
                                    val lanePhaseOffset = (lane.laneId % 7) * 0.9f + waveAnimPhase

                                    val points = 40
                                    for (p in 0..points) {
                                        val progress = p.toFloat() / points
                                        val x = progress * w
                                        val freqIdx = (progress * (laneFreqs.size - 1)).toInt().coerceIn(0, laneFreqs.size - 1)
                                        val level = laneFreqs[freqIdx]

                                        val sine1 = sin(progress * 12f + lanePhaseOffset)
                                        val sine2 = sin(progress * 24f - lanePhaseOffset * 0.8f) * 0.5f
                                        val displacement = (sine1 + sine2) * (h * 0.32f) * level

                                        val y = midY + displacement

                                        if (p == 0) {
                                            laneWavePath.moveTo(x, y)
                                        } else {
                                            laneWavePath.lineTo(x, y)
                                        }
                                    }

                                    // Draw glowing waveform for this lane
                                    drawPath(
                                        path = laneWavePath,
                                        color = laneColor.copy(alpha = 0.8f),
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            } else {
                                // Idle / Standby subtle breathing sine wave
                                val idlePath = Path()
                                val points = 40
                                for (p in 0..points) {
                                    val progress = p.toFloat() / points
                                    val x = progress * w
                                    val y = midY + sin(progress * 8f + waveAnimPhase) * (h * 0.08f)
                                    if (p == 0) idlePath.moveTo(x, y) else idlePath.lineTo(x, y)
                                }
                                drawPath(
                                    path = idlePath,
                                    color = themePalette.primary.copy(alpha = 0.35f),
                                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // 4. Draw EQ Frequency Response Curve (Smooth Bezier reflecting 5 EQ sliders)
                            val eqCurvePath = Path()
                            val eqPoints = mutableListOf<Offset>()
                            for (i in 0 until 5) {
                                val gain = eqBands.getOrElse(i) { 0f }
                                val x = (i + 0.5f) * (w / 5f)
                                // Gain range -12dB to +12dB mapped to Y
                                val normalizedGain = if (eqEnabled) (gain / 12f) else 0f
                                val y = midY - (normalizedGain * (h * 0.38f))
                                eqPoints.add(Offset(x, y))
                            }

                            eqCurvePath.moveTo(0f, eqPoints.first().y)
                            for (i in 0 until eqPoints.size - 1) {
                                val p0 = if (i == 0) eqPoints[i] else eqPoints[i - 1]
                                val p1 = eqPoints[i]
                                val p2 = eqPoints[i + 1]
                                val p3 = if (i + 2 < eqPoints.size) eqPoints[i + 2] else p2

                                val control1 = Offset(
                                    p1.x + (p2.x - p0.x) * 0.25f,
                                    p1.y + (p2.y - p0.y) * 0.25f
                                )
                                val control2 = Offset(
                                    p2.x - (p3.x - p1.x) * 0.25f,
                                    p2.y - (p3.y - p1.y) * 0.25f
                                )
                                eqCurvePath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                            }
                            eqCurvePath.lineTo(w, eqPoints.last().y)

                            // Stroke the EQ response curve
                            drawPath(
                                path = eqCurvePath,
                                brush = Brush.horizontalGradient(
                                    listOf(themePalette.primary, themePalette.secondary, themePalette.primary.copy(alpha = 0.8f))
                                ),
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Draw glowing control nodes on the 5 bands
                            eqPoints.forEachIndexed { idx, point ->
                                drawCircle(
                                    color = themePalette.secondary.copy(alpha = 0.4f),
                                    radius = 6.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = if (eqEnabled) themePalette.primary else TextMuted,
                                    radius = 3.5.dp.toPx(),
                                    center = point
                                )
                            }
                        }
                    }

                    // Active Lane Legend Tags
                    if (activePlayingLanes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activePlayingLanes.forEachIndexed { laneIndex, lane ->
                                val laneColor = laneWaveformColors[laneIndex % laneWaveformColors.size]
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = laneColor.copy(alpha = 0.18f),
                                    border = BorderStroke(0.5.dp, laneColor.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(laneColor, CircleShape)
                                        )
                                        Text(
                                            text = lane.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = laneColor,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Section
            Text(
                text = if (isPersianRtl) "پریست‌های آماده اکولایزر:" else "Equalizer Presets:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = currentPresetName == preset.name
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) themePalette.primary else themePalette.glassSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) themePalette.primary else themePalette.glassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectPreset(preset) }
                            .testTag("preset_${preset.name}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5 Band Sliders with Live Band Levels
            bandNames.forEachIndexed { index, name ->
                val gain = eqBands.getOrElse(index) { 0f }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = "${if (gain > 0) "+" else ""}${gain.toInt()} dB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (gain != 0f) themePalette.primary else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Slider(
                        value = gain,
                        onValueChange = { onBandChange(index, it) },
                        valueRange = -12f..12f,
                        steps = 23,
                        enabled = eqEnabled,
                        modifier = Modifier.fillMaxWidth().testTag("eq_slider_$index"),
                        colors = SliderDefaults.colors(
                            thumbColor = themePalette.secondary,
                            activeTrackColor = themePalette.secondary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                            disabledThumbColor = TextMuted,
                            disabledActiveTrackColor = TextMuted.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
