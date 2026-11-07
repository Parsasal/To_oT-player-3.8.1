package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioLaneState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBgCenter
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossfaderSheet(
    lanes: List<AudioLaneState>,
    leftLaneId: Long?,
    rightLaneId: Long?,
    crossfadePosition: Float,
    durationSec: Int,
    onSelectLeftLane: (Long?) -> Unit,
    onSelectRightLane: (Long?) -> Unit,
    onPositionChange: (Float) -> Unit,
    onDurationChange: (Int) -> Unit,
    onAutoCrossfade: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false
) {
    val playableLanes = lanes.filter { it.fileName != null }
    val isCrossfaderEnabled = playableLanes.size >= 2

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkBgCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("crossfader_sheet")
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AltRoute,
                    contentDescription = null,
                    tint = VioletAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersianRtl) "کراس‌فیدر دوطرفه" else "2-Lane Dual Crossfader",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isCrossfaderEnabled) {
                // Warning / Info banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPersianRtl)
                                "برای استفاده از کراس‌فیدر، حداقل دو لاین با فایل‌های صوتی بارگذاری شده نیاز است."
                            else
                                "Crossfader requires at least two lanes with loaded audio files.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                // Lane Selector Row: Left vs Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Lane Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersianRtl) "لاین چپ (A):" else "Left Lane (A):",
                            style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        playableLanes.forEach { lane ->
                            val isLeftSelected = lane.laneId == leftLaneId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLeftSelected) CyanAccent else GlassSurfaceElevated,
                                border = BorderStroke(1.dp, if (isLeftSelected) CyanAccent else GlassBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onSelectLeftLane(lane.laneId) }
                            ) {
                                Text(
                                    text = lane.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isLeftSelected) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Right Lane Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersianRtl) "لاین راست (B):" else "Right Lane (B):",
                            style = MaterialTheme.typography.labelSmall.copy(color = VioletAccent, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        playableLanes.forEach { lane ->
                            val isRightSelected = lane.laneId == rightLaneId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isRightSelected) VioletAccent else GlassSurfaceElevated,
                                border = BorderStroke(1.dp, if (isRightSelected) VioletAccent else GlassBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onSelectRightLane(lane.laneId) }
                            ) {
                                Text(
                                    text = lane.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isRightSelected) Color.White else TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Crossfader Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "A (100% Left)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Center (50/50)",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Text(
                        text = "B (100% Right)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = VioletAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Slider(
                    value = crossfadePosition,
                    onValueChange = onPositionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("crossfader_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = VioletAccent,
                        inactiveTrackColor = CyanAccent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Transition Duration Slider & Trigger Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPersianRtl) "مدت انتقال خودکار:" else "Auto-Transition Duration:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                    Text(
                        text = "${durationSec}s",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Slider(
                    value = durationSec.toFloat(),
                    onValueChange = { onDurationChange(it.toInt()) },
                    valueRange = 2f..8f,
                    steps = 5,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onAutoCrossfade(false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("auto_fade_left"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text(
                            text = if (isPersianRtl) "انتقال به چپ (A)" else "Fade to Left (A)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { onAutoCrossfade(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("auto_fade_right"),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent)
                    ) {
                        Text(
                            text = if (isPersianRtl) "انتقال به راست (B)" else "Fade to Right (B)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
