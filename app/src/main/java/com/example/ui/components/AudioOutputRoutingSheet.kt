package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioOutputDevice
import com.example.audio.AudioOutputType
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.TootThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOutputRoutingSheet(
    isOpen: Boolean,
    availableOutputs: List<AudioOutputDevice>,
    selectedOutput: AudioOutputDevice?,
    isPhoneAudioScoMode: Boolean,
    onSelectOutput: (AudioOutputDevice?) -> Unit,
    onTogglePhoneAudioSco: (Boolean) -> Unit,
    onRefreshOutputs: () -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val themePalette = LocalAppThemePalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isTroubleshootingExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themePalette.cardBackground,
        contentColor = themePalette.textPrimary,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("audio_routing_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = themePalette.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPhoneAudioScoMode) Icons.Default.PhoneInTalk else Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = themePalette.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPersianRtl) "مسیریابی خروجی صدا" else "Audio Output Routing",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = themePalette.textPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = if (isPersianRtl) "اتصال به ضبط خودرو، بلوتوث و هندزفری" else "Car Audio, Bluetooth & External Systems",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = themePalette.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshOutputs,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = themePalette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = themePalette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Audio SCO Mode Card (Specially designed for car audio & telephony systems)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isPhoneAudioScoMode) themePalette.primary.copy(alpha = 0.15f) else themePalette.cardBackground.copy(alpha = 0.4f),
                border = BorderStroke(
                    1.5.dp,
                    if (isPhoneAudioScoMode) themePalette.primary else themePalette.cardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPhoneAudioScoMode) themePalette.primary else themePalette.textMuted.copy(alpha = 0.2f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = if (isPhoneAudioScoMode) Color.Black else themePalette.textPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPersianRtl) "حالت پخش صوتی سیستم‌های تلفنی و خودرو" else "Phone/Car Telephony Audio Mode (SCO)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPhoneAudioScoMode) themePalette.primary else themePalette.textPrimary,
                                        fontSize = 13.5.sp
                                    )
                                )
                                Text(
                                    text = if (isPhoneAudioScoMode) {
                                        if (isPersianRtl) "فعال • صدا از طریق کانال صوتی تلفن ارسال می‌شود" else "Active • Audio routed through telephony channel"
                                    } else {
                                        if (isPersianRtl) "غیرفعال • مناسب برای ضبط‌های خودرو با ورودی تماس" else "Off • For car kits accepting call audio only"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = themePalette.textSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isPhoneAudioScoMode,
                            onCheckedChange = { onTogglePhoneAudioSco(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = themePalette.primary,
                                uncheckedThumbColor = themePalette.textSecondary,
                                uncheckedTrackColor = themePalette.cardBackground
                            ),
                            modifier = Modifier.testTag("toggle_phone_sco_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPersianRtl) {
                            "اگر گوشی را به ضبط ماشین، ترنسمیتر FM یا سیستم صوتی متصل کرده‌اید ولی صدای موزیک از بلندگوهای آن پخش نمی‌شود، این گزینه را روشن کنید تا صدا در قالب کانال صوتی تلفن (SCO) به سیستم ارسال شود."
                        } else {
                            "If your car stereo or audio system connects as a phone call device and doesn't output music, enable this to route audio directly through the telephony/SCO channel."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = themePalette.textMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output Devices Section
            Text(
                text = if (isPersianRtl) "دستگاه‌های خروجی شناخته‌شده" else "Detected Output Devices",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = themePalette.primary,
                    letterSpacing = 0.5.sp,
                    fontSize = 11.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Default Automatic Output option
            OutputDeviceItem(
                title = if (isPersianRtl) "هدایت خودکار سیستم (پیش‌فرض سیستم عامل)" else "System Default (Auto Routing)",
                subtitle = if (isPersianRtl) "اتصال خودکار به هندزفری، بلوتوث یا اسپیکر پیش‌فرض" else "Automatically route to active system device",
                icon = Icons.Default.VolumeUp,
                isSelected = selectedOutput == null,
                onClick = { onSelectOutput(null) },
                themePalette = themePalette,
                isPersianRtl = isPersianRtl
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Discovered Hardware Devices
            availableOutputs.forEach { device ->
                val icon = when (device.type) {
                    AudioOutputType.SPEAKER -> Icons.Default.Speaker
                    AudioOutputType.WIRED_HEADSET -> Icons.Default.Headphones
                    AudioOutputType.BLUETOOTH_A2DP -> Icons.Default.Bluetooth
                    AudioOutputType.BLUETOOTH_SCO -> Icons.Default.PhoneCallback
                    AudioOutputType.USB_AUDIO -> Icons.Default.Usb
                    AudioOutputType.OTHER -> Icons.Default.VolumeUp
                }

                OutputDeviceItem(
                    title = if (isPersianRtl) device.nameFa else device.nameEn,
                    subtitle = when (device.type) {
                        AudioOutputType.SPEAKER -> if (isPersianRtl) "بلندگوی سخت‌افزاری گوشی" else "Built-in hardware speaker"
                        AudioOutputType.WIRED_HEADSET -> if (isPersianRtl) "خروجی جک ۳.۵ میلی‌متری / کابل AUX" else "3.5mm Jack / AUX Cable"
                        AudioOutputType.BLUETOOTH_A2DP -> if (isPersianRtl) "بلوتوث با کیفیت استریو (A2DP)" else "Stereo Bluetooth Audio (A2DP)"
                        AudioOutputType.BLUETOOTH_SCO -> if (isPersianRtl) "سیستم صوتی خودرو و هندزفری (SCO)" else "Car Kit / Telephony Audio (SCO)"
                        AudioOutputType.USB_AUDIO -> if (isPersianRtl) "خروجی دیجیتال پورت USB-C" else "Digital USB-C Audio"
                        AudioOutputType.OTHER -> if (isPersianRtl) "خروجی صوتی متصل" else "Connected Audio Output"
                    },
                    icon = icon,
                    isSelected = selectedOutput?.id == device.id,
                    onClick = { onSelectOutput(device) },
                    themePalette = themePalette,
                    isPersianRtl = isPersianRtl
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Troubleshooting and Tips Collapsible Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themePalette.cardBackground.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, themePalette.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isTroubleshootingExpanded = !isTroubleshootingExpanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = themePalette.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersianRtl) "راهنمای حل مشکل عدم پخش صدا در ضبط خودرو یا اسپیکر" else "Car Stereo & External Audio Troubleshooting",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = themePalette.textPrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Text(
                            text = if (isTroubleshootingExpanded) (if (isPersianRtl) "بستن" else "Hide") else (if (isPersianRtl) "مشاهده" else "Show"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = themePalette.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    AnimatedVisibility(visible = isTroubleshootingExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            TroubleshootStep(
                                stepNumber = "۱",
                                title = if (isPersianRtl) "فعال‌سازی «صدای رسانه (Media Audio)» در بلوتوث گوشی" else "Enable 'Media Audio' in Bluetooth Settings",
                                description = if (isPersianRtl) {
                                    "وارد تنظیمات بلوتوث گوشی شوید، علامت چرخ‌دنده ⚙️ کنار نام ضبط یا سیستم صوتی متصل را لمس کنید و مطمئن شوید گزینه «صدای رسانه» (Media audio) فعال است (گاهی فقط گزینه تماس فعال است)."
                                } else {
                                    "Go to Phone Settings > Bluetooth, tap the gear icon next to your car stereo, and ensure 'Media Audio' is checked."
                                },
                                themePalette = themePalette
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TroubleshootStep(
                                stepNumber = "۲",
                                title = if (isPersianRtl) "تنظیم ورودی ضبط ماشین روی BT Audio یا AUX" else "Switch Stereo Source to BT Audio / AUX",
                                description = if (isPersianRtl) {
                                    "دکمه Source یا Mode روی پنل ضبط ماشین را فشار دهید تا ورودی از رادیو یا Phone به حالت BT Audio یا AUX تغییر کند."
                                } else {
                                    "Press the Mode or Source button on your car stereo to ensure input is set to BT Audio or AUX, not FM radio."
                                },
                                themePalette = themePalette
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TroubleshootStep(
                                stepNumber = "۳",
                                title = if (isPersianRtl) "استفاده از سوئیچ «حالت پخش سیستم‌های تلفنی» بالا" else "Use 'Phone Audio SCO Mode' Above",
                                description = if (isPersianRtl) {
                                    "اگر ضبط خودروی شما قدیمی است یا فقط برای مکالمه طراحی شده، سوییچ بالای همین صفحه را روشن کنید تا موزیک در بستر تماس تلفنی پخش شود."
                                } else {
                                    "If your system only accepts phone call audio, enable the toggle above to route playback over the call stream."
                                },
                                themePalette = themePalette
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OutputDeviceItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    themePalette: TootThemePalette,
    isPersianRtl: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) themePalette.primary.copy(alpha = 0.12f) else themePalette.cardBackground.copy(alpha = 0.3f),
        border = BorderStroke(
            1.dp,
            if (isSelected) themePalette.primary.copy(alpha = 0.8f) else themePalette.cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) themePalette.primary else themePalette.cardBackground,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else themePalette.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) themePalette.primary else themePalette.textPrimary,
                            fontSize = 12.5.sp
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = themePalette.textSecondary,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = themePalette.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TroubleshootStep(
    stepNumber: String,
    title: String,
    description: String,
    themePalette: TootThemePalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = themePalette.secondary.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = themePalette.secondary,
                        fontSize = 10.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = themePalette.textPrimary,
                    fontSize = 11.5.sp
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = themePalette.textSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp
                )
            )
        }
    }
}
