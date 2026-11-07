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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun AboutDialog(
    onDismiss: () -> Unit,
    onOpenPrivacyPolicy: (() -> Unit)? = null,
    onOpenHelpCenter: (() -> Unit)? = null,
    onReplayTutorial: (() -> Unit)? = null,
    isPersianRtl: Boolean = false
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBgCenter,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TO_OT Player",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (isPersianRtl) "هاب و میکسر هوشمند صوتی چند لاینه" else "Smart Multi-Lane Audio Hub & Mixer",
                        style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent)
                    )
                }
            }
        },
        text = {
            var isChangelogExpanded by remember { mutableStateOf(true) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("about_dialog")
            ) {
                // Prominent Creator / سازنده Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlassSurfaceElevated,
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPersianRtl) "مشخصات سازنده و برنامه" else "Creator & App Specifications",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldPlay.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, EmeraldPlay)
                            ) {
                                Text(
                                    text = if (isPersianRtl) "توسعه‌دهنده" else "Developer",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldPlay,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Creator Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPersianRtl) "سازنده: " else "Creator: ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = AppInfo.DEVELOPER_NAME,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Email with copy affordance
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
                                    tint = VioletAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "email : ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = AppInfo.DEVELOPER_EMAIL,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Telegram ID with copy and open affordance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Telegram ID", AppInfo.DEVELOPER_TELEGRAM)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(
                                        context,
                                        if (isPersianRtl) "شناسه تلگرام کپی شد" else "Telegram ID copied to clipboard",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppInfo.TELEGRAM_URL))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore if no browser or telegram app
                                    }
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "id telegram : ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = AppInfo.DEVELOPER_TELEGRAM,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open Telegram",
                                    tint = CyanAccent.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Telegram",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Version & Build Code
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "نسخه ${AppInfo.VERSION_NAME} (کد ساخت ${AppInfo.VERSION_CODE})" else "Version ${AppInfo.VERSION_NAME} (Build ${AppInfo.VERSION_CODE})",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Medium)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Last Update in Shamsi
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = EmeraldPlay,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "آخرین به‌روزرسانی: ${AppInfo.LAST_UPDATE_SHAMSI}" else "Last Update: ${AppInfo.LAST_UPDATE_SHAMSI} (${AppInfo.LAST_UPDATE_GREGORIAN})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldPlay,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // What's New in Version / تغییرات آخرین نسخه Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPlay.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, EmeraldPlay.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isChangelogExpanded = !isChangelogExpanded }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = null,
                                    tint = EmeraldPlay,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isPersianRtl) "تغییرات نسخه ${AppInfo.VERSION_NAME}" else "What's New in v${AppInfo.VERSION_NAME}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = EmeraldPlay,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = if (isPersianRtl) "${AppInfo.LAST_UPDATE_SHAMSI_VERBOSE} (${AppInfo.LAST_UPDATE_GREGORIAN})" else "${AppInfo.LAST_UPDATE_GREGORIAN} (${AppInfo.LAST_UPDATE_SHAMSI})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isChangelogExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isChangelogExpanded) "Collapse" else "Expand",
                                tint = EmeraldPlay,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isChangelogExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppInfo.LATEST_CHANGELOG.forEachIndexed { index, item ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldPlay,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isPersianRtl) item.titleFa else item.titleEn,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = if (isPersianRtl) item.descFa else item.descEn,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 10.sp,
                                                    lineHeight = 13.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Help Center Card
                if (onOpenHelpCenter != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.08f),
                        border = BorderStroke(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onOpenHelpCenter()
                            }
                            .testTag("about_open_help_center")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPersianRtl) "مرکز راهنمایی و پشتیبانی" else "Help Center & FAQs",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = if (isPersianRtl)
                                        "راهنمای تعاملی، سوالات متداول و آموزش‌ها"
                                    else
                                        "Interactive guides, FAQs & tutorials",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help Center",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Replay Tutorial Card
                if (onReplayTutorial != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CyanAccent.copy(alpha = 0.08f),
                        border = BorderStroke(0.8.dp, CyanAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onReplayTutorial()
                            }
                            .testTag("about_replay_tutorial")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CyanAccent.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPersianRtl) "مرور مجدد راهنمای کاربری" else "Replay Tutorial",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = if (isPersianRtl)
                                        "آموزش گام‌به‌گام امکانات و بخش‌های برنامه"
                                    else
                                        "Interactive step-by-step app tour",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "Replay Tutorial",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Privacy Statement Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPlay.copy(alpha = 0.08f),
                    border = BorderStroke(0.8.dp, EmeraldPlay.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onOpenPrivacyPolicy != null) {
                            onDismiss()
                            onOpenPrivacyPolicy?.invoke()
                        }
                        .testTag("about_open_privacy_policy")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPlay.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = EmeraldPlay,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPersianRtl) "حریم خصوصی و امنیت داده‌ها" else "Privacy Policy & Data Safety",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EmeraldPlay,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = if (isPersianRtl)
                                    "۱۰۰٪ محلی و آفلاین • بدون ارسال داده به سرور • مشاهده جزییات"
                                else
                                    "100% Local • Zero-Data Collection • Tap to view policy",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }
                        if (onOpenPrivacyPolicy != null) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "View Privacy Policy",
                                tint = EmeraldPlay,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (onOpenPrivacyPolicy != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenPrivacyPolicy()
                        },
                        border = BorderStroke(1.dp, EmeraldPlay.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPlay),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPersianRtl) "حریم خصوصی" else "Privacy",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("about_dialog_close")
                ) {
                    Text(
                        text = if (isPersianRtl) "بستن" else "Close",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}
