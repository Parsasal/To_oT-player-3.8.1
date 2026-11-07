package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.LocalAppThemePalette
import kotlin.math.abs

/**
 * Tour Step Definition Data Class
 */
data class TourStepItem(
    val id: String,
    val titleFa: String,
    val titleEn: String,
    val descriptionFa: String,
    val descriptionEn: String,
    val stepIndex: Int,
    val totalSteps: Int
)

/**
 * Onboarding Tour Overlay Component for TO_OT Player
 *
 * Implements a non-intrusive, mobile-first guided tour:
 * - Independent visual layer (never pauses, touches or disrupts audio playback)
 * - Safe persistence key: "to_ot_tour_completed" -> true in SharedPreferences
 * - Touch-friendly (minimum 44-48dp interactive touch targets)
 * - Swipe navigation support on the tooltip card
 * - Persian RTL and English LTR alignment
 * - Fully responsive on 320px, 360px and 412px widths
 */
@Composable
fun OnboardingTour(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val themePalette = LocalAppThemePalette.current
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val tourSteps = remember {
        listOf(
            TourStepItem(
                id = "header-add-lane",
                titleFa = "خوش آمدید به TO_OT Player",
                titleEn = "Welcome to TO_OT Player",
                descriptionFa = "با دکمهٔ افزودن لاین می‌توانید یک لاین صوتی جدید ایجاد کنید.\nلاین جدید بلافاصله در بالای فهرست قرار می‌گیرد.",
                descriptionEn = "Use the Add Lane button in the top bar to create a new audio lane.\nNew lanes are immediately added to the top of your sessions.",
                stepIndex = 1,
                totalSteps = 5
            ),
            TourStepItem(
                id = "audio-lane",
                titleFa = "مدیریت مستقل لاین‌ها",
                titleEn = "Independent Lane Management",
                descriptionFa = "هر لاین می‌تواند فایل صوتی جداگانه‌ای داشته باشد.\nمی‌توانید نام لاین را تغییر دهید، آن را با دکمه‌های بالا و پایین یا کشیدن جابه‌جا کنید و پخش، صدا و وضعیت آن را مستقل کنترل نمایید.",
                descriptionEn = "Each lane holds its own independent audio file and queue.\nYou can rename it, reorder with up/down arrows or drag & drop, and control playback and volume independently.",
                stepIndex = 2,
                totalSteps = 5
            ),
            TourStepItem(
                id = "lane-solo-mute",
                titleFa = "پخش همزمان، Solo و Mute",
                titleEn = "Simultaneous Play, Solo & Mute",
                descriptionFa = "چند لاین می‌توانند همزمان پخش شوند.\nبا Solo فقط لاین‌های انتخاب‌شده شنیده می‌شوند و با Mute صدای یک لاین را بدون توقف قطع می‌کنید.",
                descriptionEn = "Multiple audio lanes play together concurrently.\nUse Solo to isolate specific tracks or Mute to silence any track instantly without stopping playback.",
                stepIndex = 3,
                totalSteps = 5
            ),
            TourStepItem(
                id = "master-control-panel",
                titleFa = "پنل کنترل اصلی و ابزارهای صوتی",
                titleEn = "Master Control Panel & DSP Tools",
                descriptionFa = "کنترل لاین فعال با ابزارهای حرفه‌ای: Seek، Volume، اکولایزر چندبانده، ضبط باکیفیت صدا، کراس‌فیدر انتقالی DJ و تایمر خواب هوشمند در هدر.",
                descriptionEn = "Control your active lane directly and access powerful tools: Seek, Volume, Multi-band EQ, High-Res Recorder, DJ Crossfader, and Header Sleep Timer.",
                stepIndex = 4,
                totalSteps = 5
            ),
            TourStepItem(
                id = "video-extractor-trim",
                titleFa = "استخراج صدا از ویدیو و الصاق مستقیم به لاین",
                titleEn = "Video Audio Extraction & Target Lane Assignment",
                descriptionFa = "صدای ویدیوهای گالری را استخراج کنید، با اسلایدر دوطرفه بازه دلخواه را برش دهید و با انتخاب لاین هدف، صوت را مستقیماً به لاین دلخواه یا لاین جدید پروژه منتقل نمایید.",
                descriptionEn = "Extract pristine audio from device videos, fine-tune cues with the dual-slider trimmer, and assign audio directly to any target lane or create a new lane.",
                stepIndex = 5,
                totalSteps = 5
            )
        )
    }

    val currentStep = tourSteps.getOrElse(currentStepIndex) { tourSteps.first() }

    // Swipe gesture distance accumulator
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val handleNext: () -> Unit = {
        if (currentStepIndex < tourSteps.size - 1) {
            currentStepIndex++
        } else {
            onDismiss()
        }
    }

    val handleBack: () -> Unit = {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = 0.72f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .semantics {
                dialog()
                contentDescription = if (isPersianRtl) "راهنمای تعاملی برنامه" else "Interactive Onboarding Tour"
            }
            .testTag("onboarding_tour_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tooltip Card with gentle shadow and theme accent border
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = themePalette.cardBackground,
                border = BorderStroke(1.5.dp, themePalette.primary.copy(alpha = 0.85f)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .pointerInput(currentStepIndex) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(dragAccumulator) > 50f) {
                                    if (isPersianRtl) {
                                        if (dragAccumulator > 0) handleNext() else handleBack()
                                    } else {
                                        if (dragAccumulator < 0) handleNext() else handleBack()
                                    }
                                }
                                dragAccumulator = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccumulator += dragAmount
                            }
                        )
                    }
                    .testTag("onboarding_tour_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Top Row: Step Badge & Skip Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = themePalette.primary.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl)
                                        "مرحله ${currentStep.stepIndex} از ${currentStep.totalSteps}"
                                    else
                                        "Step ${currentStep.stepIndex} of ${currentStep.totalSteps}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = themePalette.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("onboarding_skip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isPersianRtl) "رد کردن راهنما" else "Skip Tutorial",
                                tint = themePalette.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = if (isPersianRtl) currentStep.titleFa else currentStep.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = themePalette.textPrimary,
                            fontSize = 17.5.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (isPersianRtl) TextAlign.Right else TextAlign.Left
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = if (isPersianRtl) currentStep.descriptionFa else currentStep.descriptionEn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = themePalette.textSecondary,
                            fontSize = 13.5.sp,
                            lineHeight = 21.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (isPersianRtl) TextAlign.Right else TextAlign.Left
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Step Indicator Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until tourSteps.size) {
                            val isActive = i == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.5.dp)
                                    .height(7.dp)
                                    .width(if (isActive) 18.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) themePalette.primary else themePalette.textMuted.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Navigation Action Buttons (Min 44x44dp hit area)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = handleBack,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, themePalette.cardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = themePalette.textSecondary
                                ),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("onboarding_back_button")
                            ) {
                                Icon(
                                    imageVector = if (isPersianRtl) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersianRtl) "قبلی" else "Back",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, themePalette.cardBorder.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = themePalette.textMuted
                                ),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("onboarding_skip_bottom_button")
                            ) {
                                Text(
                                    text = if (isPersianRtl) "رد کردن" else "Skip",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Next or Finish Button
                        val isLastStep = currentStepIndex == tourSteps.size - 1
                        Button(
                            onClick = handleNext,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themePalette.primary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Text(
                                text = if (isLastStep) {
                                    if (isPersianRtl) "شروع استفاده" else "Get Started"
                                } else {
                                    if (isPersianRtl) "بعدی" else "Next"
                                },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isLastStep) Icons.Default.Check else (if (isPersianRtl) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
