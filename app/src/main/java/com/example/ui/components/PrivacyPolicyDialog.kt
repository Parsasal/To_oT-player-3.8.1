package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBgCenter
import com.example.ui.theme.EmeraldPlay
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.util.AppInfo

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBgCenter,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldPlay.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldPlay.copy(alpha = 0.5f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldPlay,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isPersianRtl) "حریم خصوصی و امنیت داده‌ها" else "Privacy Policy & Data Safety",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = if (isPersianRtl) "تطابق با استانداردهای گوگل‌پلی و کافه‌بازار" else "Google Play & Cafe Bazaar Store Compliant",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldPlay,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .testTag("privacy_policy_dialog")
            ) {
                // Zero-Data Collection Badge Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GlassSurfaceElevated,
                    border = BorderStroke(1.dp, EmeraldPlay.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = EmeraldPlay,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl) "بیانیه عدم جمع‌آوری داده‌ها" else "Zero-Data Collection Guarantee",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = EmeraldPlay,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPlay.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, EmeraldPlay)
                            ) {
                                Text(
                                    text = "100% LOCAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldPlay,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isPersianRtl)
                                "نرم‌افزار TO_OT Player با معماری ۱۰۰٪ محلی (Local-First) توسعه یافته است. هیچ‌گونه اطلاعات شخصی، شناسه‌های دستگاه، فایل‌های صوتی یا داده‌های تحلیلی (Telemetry/Analytics) هرگز ذخیره، ردیابی یا به سرورهای خارجی ارسال نمی‌گردد."
                            else
                                "TO_OT Player operates 100% locally on your device (Local-First Architecture). No telemetry, analytics, personal identifiers, device telemetry, or audio files are ever collected, transmitted, or stored on external cloud servers.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Permissions Breakdown
                Text(
                    text = if (isPersianRtl) "دسترسی‌های شفاف و حداقلی برنامه" else "TRANSPARENT APP PERMISSIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    icon = Icons.Default.FolderOpen,
                    iconTint = CyanAccent,
                    name = "READ_MEDIA_AUDIO",
                    description = if (isPersianRtl)
                        "صرفاً برای انتخاب و پخش فایل‌های صوتی از حافظه دستگاه شما و پخش در باندهای میکسر توسط خود شما."
                    else
                        "Exclusively used for client-side scanning and playback of user-selected audio tracks inside the multi-lane mixer."
                )

                Spacer(modifier = Modifier.height(6.dp))

                PermissionItem(
                    icon = Icons.Default.PowerSettingsNew,
                    iconTint = VioletAccent,
                    name = "FOREGROUND_SERVICE (MEDIA_PLAYBACK)",
                    description = if (isPersianRtl)
                        "جهت پخش مداوم صدا در پس‌زمینه، کارکرد بدون قطعی در زمان خاموش بودن صفحه و کنترل از طریق پنل اعلانات."
                    else
                        "Required for uninterrupted background playback, lock screen audio controls, and notification media session integration."
                )

                Spacer(modifier = Modifier.height(6.dp))

                PermissionItem(
                    icon = Icons.Default.Notifications,
                    iconTint = Color(0xFFF59E0B),
                    name = "POST_NOTIFICATIONS",
                    description = if (isPersianRtl)
                        "نمایش کنترل‌های استاندارد پخش موسیقی و نام آهنگ فعال در نوار اعلانات سیستم اندروید."
                    else
                        "Allows displaying media player controls (Play/Pause/Next/Prev) in the Android notification bar."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Store Compliance Notice
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(0.5.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPlay,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersianRtl)
                                "سازگار با دستورالعمل‌های امنیت داده گوگل‌پلی و مقررات انتشار کافه‌بازار و مایکت: بدون تبلیغات مخفی، بدون ردیابی موقعیت مکانی و بدون دسترسی به اطلاعات شخصی."
                            else
                                "Fully compliant with Google Play Data Safety specifications & Cafe Bazaar developer guidelines: No ads, no location tracking, and no external data sharing.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Developer & Contact Info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlassSurfaceElevated,
                    border = BorderStroke(0.5.dp, CyanAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isPersianRtl) "اطلاعات سازنده و تماس:" else "Developer & Contact:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Email", AppInfo.DEVELOPER_EMAIL)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(
                                        context,
                                        if (isPersianRtl) "ایمیل کپی شد" else "Email copied to clipboard",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${AppInfo.DEVELOPER_NAME} (${AppInfo.DEVELOPER_EMAIL})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPlay),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("privacy_policy_dialog_close")
            ) {
                Text(
                    text = if (isPersianRtl) "متوجه شدم و قبول دارم" else "Understood",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    iconTint: Color,
    name: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = GlassSurfaceElevated,
        border = BorderStroke(0.5.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = iconTint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}
