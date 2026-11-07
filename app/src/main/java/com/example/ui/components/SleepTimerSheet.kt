package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cancel
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBgCenter
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.RoseMute
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    remainingSeconds: Int?,
    onStartTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit,
    onAddMinutes: (Int) -> Unit = {},
    isPersianRtl: Boolean = false
) {
    var customMinutes by remember { mutableStateOf(20) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkBgCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("sleep_timer_sheet")
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersianRtl) "تایمر خواب هوشمند" else "Smart Sleep Timer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Countdown Display if active
            if (remainingSeconds != null && remainingSeconds > 0) {
                val min = remainingSeconds / 60
                val sec = remainingSeconds % 60
                val formattedTime = String.format("%02d:%02d", min, sec)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPersianRtl) "زمان باقیمانده تا خاموشی:" else "Remaining until sleep:",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (remainingSeconds <= 30) {
                            Text(
                                text = if (isPersianRtl) "در حال کاهش تدریجی صدا..." else "Smoothly fading out volume...",
                                style = MaterialTheme.typography.labelSmall.copy(color = RoseMute, fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Extend timer chips (+5 min, +15 min)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GlassSurfaceElevated,
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier
                                    .clickable { onAddMinutes(5) }
                                    .testTag("sleep_timer_add_5m")
                            ) {
                                Text(
                                    text = if (isPersianRtl) "+۵ دقیقه" else "+5 min",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GlassSurfaceElevated,
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier
                                    .clickable { onAddMinutes(15) }
                                    .testTag("sleep_timer_add_15m")
                            ) {
                                Text(
                                    text = if (isPersianRtl) "+۱۵ دقیقه" else "+15 min",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onCancelTimer,
                            colors = ButtonDefaults.buttonColors(containerColor = RoseMute),
                            modifier = Modifier.testTag("cancel_sleep_timer")
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "لغو تایمر" else "Cancel Sleep Timer",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            } else {
                // Preset buttons: 15, 30, 45, 60 min
                Text(
                    text = if (isPersianRtl) "انتخاب مدت زمان:" else "Select Duration:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val presets = listOf(15, 30, 45, 60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { minutes ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GlassSurfaceElevated,
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStartTimer(minutes) }
                                .testTag("timer_preset_${minutes}m")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$minutes",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (isPersianRtl) "دقیقه" else "min",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Slider
                Text(
                    text = if (isPersianRtl) "مدت زمان سفارشی: $customMinutes دقیقه" else "Custom Duration: $customMinutes min",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )

                Slider(
                    value = customMinutes.toFloat(),
                    onValueChange = { customMinutes = it.toInt() },
                    valueRange = 1f..120f,
                    steps = 118,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = VioletAccent,
                        activeTrackColor = VioletAccent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onStartTimer(customMinutes) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_custom_timer"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text(
                        text = if (isPersianRtl) "شروع تایمر سفارشی ($customMinutes دقیقه)" else "Start Custom Timer ($customMinutes min)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isPersianRtl)
                    "• در ۳۰ ثانیه پایانی، صدای خروجی به نرمی محو و متوقف می‌شود."
                else
                    "• In the final 30 seconds, master volume smoothly fades out before stopping.",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
