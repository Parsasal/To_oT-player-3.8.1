package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.LocalAppThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorSheet(
    currentTheme: AppThemeStyle,
    isDarkMode: Boolean,
    onSelectTheme: (AppThemeStyle) -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false
) {
    val themePalette = LocalAppThemePalette.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = themePalette.bgCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("theme_selector_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = themePalette.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = themePalette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isPersianRtl) "تنظیمات تم و حالت نمایش" else "Theme & Display Mode",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.textPrimary
                        )
                    )
                    Text(
                        text = if (isPersianRtl) "تغییر حالت شب/روز برای راحتی چشم و انتخاب پالت رنگی" else "Toggle Dark/Light mode and select acoustic color palette",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = themePalette.textSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Light / Dark Mode Segmented Switch
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = themePalette.cardBackground,
                border = BorderStroke(1.dp, themePalette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Dark Mode Option
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDarkMode) themePalette.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetDarkMode(true) }
                            .testTag("theme_mode_dark")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (isDarkMode) (if (isDarkMode) Color.Black else Color.White) else themePalette.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "حالت شب (Dark)" else "Dark Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDarkMode) (if (isDarkMode) Color.Black else Color.White) else themePalette.textPrimary,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }

                    // Light Mode Option
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isDarkMode) themePalette.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetDarkMode(false) }
                            .testTag("theme_mode_light")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (!isDarkMode) Color.White else themePalette.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "حالت روز (Light)" else "Light Mode",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isDarkMode) Color.White else themePalette.textPrimary,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPersianRtl) "پالت‌های رنگی استودیو" else "STUDIO COLOR PALETTES",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = themePalette.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(AppThemeStyle.values()) { themeStyle ->
                    val isSelected = currentTheme == themeStyle
                    val previewPrimary = if (isDarkMode) themeStyle.primaryAccent else themeStyle.primaryAccentLight
                    val previewSecondary = if (isDarkMode) themeStyle.secondaryAccent else themeStyle.secondaryAccentLight
                    val previewBg = if (isDarkMode) themeStyle.bgCenterDark else themeStyle.bgCenterLight

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) themePalette.primary.copy(alpha = if (isDarkMode) 0.15f else 0.10f) else themePalette.cardBackground,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.8.dp,
                            if (isSelected) themePalette.primary else themePalette.cardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTheme(themeStyle) }
                            .testTag("theme_option_${themeStyle.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Theme Visual Preview Badge (mini gradient swatch)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    previewBg,
                                                    previewPrimary,
                                                    previewSecondary
                                                )
                                            )
                                        )
                                        .testTag("theme_swatch_${themeStyle.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isPersianRtl) themeStyle.titleFa else themeStyle.titleEn,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) themePalette.primary else themePalette.textPrimary,
                                                fontSize = 13.5.sp
                                            )
                                        )

                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = themePalette.primary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (isPersianRtl) "فعال" else "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = themePalette.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (isPersianRtl) themeStyle.subtitleFa else themeStyle.subtitleEn,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = themePalette.textSecondary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 2
                                    )
                                }
                            }

                            // Palette dot indicators
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(previewPrimary, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(previewSecondary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
