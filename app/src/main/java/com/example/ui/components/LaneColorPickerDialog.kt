package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.AudioLaneState
import com.example.ui.theme.LocalAppThemePalette

data class LaneStudioColor(
    val hex: String,
    val nameEn: String,
    val nameFa: String,
    val color: Color
)

val STUDIO_LANE_COLORS = listOf(
    LaneStudioColor("#EF4444", "Crimson Red", "قرمز استودیویی", Color(0xFFEF4444)),
    LaneStudioColor("#F43F5E", "Neon Rose", "رز نئون", Color(0xFFF43F5E)),
    LaneStudioColor("#F97316", "Sunset Orange", "نارنجی غروب", Color(0xFFF97316)),
    LaneStudioColor("#F59E0B", "Amber Gold", "کهربایی طلایی", Color(0xFFF59E0B)),
    LaneStudioColor("#84CC16", "Acid Lime", "سبز فسفری", Color(0xFF84CC16)),
    LaneStudioColor("#10B981", "Emerald Green", "سبز زمردی", Color(0xFF10B981)),
    LaneStudioColor("#06B6D4", "Cyber Cyan", "آبی سایبر", Color(0xFF06B6D4)),
    LaneStudioColor("#3B82F6", "Cobalt Blue", "آبی کبالت", Color(0xFF3B82F6)),
    LaneStudioColor("#6366F1", "Royal Indigo", "نیلی شاهانه", Color(0xFF6366F1)),
    LaneStudioColor("#8B5CF6", "Neon Violet", "بنفش نئونی", Color(0xFF8B5CF6)),
    LaneStudioColor("#D946EF", "Magenta Pink", "سرخابی فوشیا", Color(0xFFD946EF)),
    LaneStudioColor("#64748B", "Studio Slate", "خاکستری مدرن", Color(0xFF64748B))
)

@Composable
fun LaneColorPickerDialog(
    lane: AudioLaneState,
    isPersianRtl: Boolean,
    onColorSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val themePalette = LocalAppThemePalette.current
    var selectedHex by remember { mutableStateOf(lane.colorHex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themePalette.cardBackground,
            border = BorderStroke(1.5.dp, themePalette.glassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("lane_color_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = selectedHex?.let {
                                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { themePalette.primary }
                            } ?: themePalette.primary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPersianRtl) "رنگ اختصاصی لاین" else "Lane Color Accent",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = themePalette.textPrimary
                                )
                            )
                            Text(
                                text = lane.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = themePalette.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themePalette.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPersianRtl)
                        "یک رنگ برای تفکیک چشمی و جذابیت ظاهری این لاین انتخاب کنید:"
                    else
                        "Select a distinct visual accent color for this audio lane:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = themePalette.textSecondary,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Studio Colors Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(STUDIO_LANE_COLORS, key = { it.hex }) { studioColor ->
                        val isSelected = selectedHex?.equals(studioColor.hex, ignoreCase = true) == true
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedHex = studioColor.hex
                                    onColorSelected(studioColor.hex)
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(studioColor.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPersianRtl) studioColor.nameFa else studioColor.nameEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isSelected) themePalette.textPrimary else themePalette.textMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Reset to default theme & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedHex = null
                            onColorSelected(null)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themePalette.glassBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorReset,
                            contentDescription = null,
                            tint = themePalette.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersianRtl) "حذف رنگ" else "Default",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = themePalette.textSecondary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onColorSelected(selectedHex)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedHex?.let {
                                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { themePalette.primary }
                            } ?: themePalette.primary
                        ),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = if (isPersianRtl) "اعمال رنگ" else "Apply Color",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
