package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LocalAppThemePalette
import com.example.widget.TootAppWidgetProvider

@Composable
fun HomescreenWidgetDialog(
    isPersianRtl: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themePalette = LocalAppThemePalette.current

    val canPinWidget = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            AppWidgetManager.getInstance(context)?.isRequestPinAppWidgetSupported == true

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = themePalette.cardBackground,
            border = BorderStroke(1.5.dp, themePalette.glassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("homescreen_widget_dialog")
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
                            color = themePalette.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPersianRtl) "ویجت صفحه اصلی گوشی" else "Homescreen Widget",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = themePalette.textPrimary
                            )
                        )
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

                // Widget Mockup Card Preview
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF141923),
                    border = BorderStroke(1.dp, Color(0xFF2E384D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TO_OT • Remix",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (isPersianRtl) "موزیک در حال پخش" else "Now Playing Audio",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF242C3D),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF242C3D),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPersianRtl)
                        "با افزودن این ویجت به صفحه اصلی اندروید، می‌توانید لاین‌های صوتی را بدون نیاز به باز کردن اپلیکیشن، مستقیماً پخش، متوقف یا سولو کنید و به قطعه بعدی و قبلی بروید."
                    else
                        "Add this mini widget to your Android home screen to control playback, pause, and switch tracks instantly without opening the app.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = themePalette.textSecondary,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (canPinWidget) {
                    Button(
                        onClick = {
                            try {
                                val appWidgetManager = AppWidgetManager.getInstance(context)
                                val provider = ComponentName(context, TootAppWidgetProvider::class.java)
                                appWidgetManager.requestPinAppWidget(provider, null, null)
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error requesting widget: $e", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = themePalette.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersianRtl) "افزودن فوری به صفحه اصلی" else "Pin to Home Screen",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themePalette.glassSurface,
                        border = BorderStroke(1.dp, themePalette.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isPersianRtl) "راهنمای افزودن دستی:" else "Manual Step-by-Step:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = themePalette.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPersianRtl)
                                    "۱. انگشت خود را روی فضای خالی صفحه اصلی گوشی نگه دارید.\n۲. گزینه «Widgets / ویجت‌ها» را بزنید.\n۳. «TO_OT Player» را انتخاب کرده و به صفحه بکشید."
                                else
                                    "1. Long press on an empty area of your phone's home screen.\n2. Tap 'Widgets'.\n3. Locate 'TO_OT Player' and drag it to your screen.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = themePalette.textSecondary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
