package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalAppThemePalette
import com.example.util.AppLanguage

/**
 * Mobile-First, Accessible Interactive Help Center Modal for TO_OT Player
 *
 * Implements:
 * - Pure UI documentation layer (never touches, pauses or resets AudioEngine, AudioNodes, or tracks)
 * - Safe area support (status bars + navigation bars)
 * - Full 6-language in-modal switcher (fa, en, es, zh, hi, pt) with instant RTL/LTR realignment
 * - Accordion expand/collapse with preserved expansion state
 * - Instant debounced local client-side search across titles, summaries, and bullet points
 * - Empty state with localized suggestion
 * - Min 44x44dp touch targets & accessibility semantics (dialog, role, contentDescription, testTags)
 */
@Composable
fun HelpModal(
    isOpen: Boolean,
    onDismiss: () -> Unit = {},
    onClose: () -> Unit = onDismiss,
    onStartInteractiveTour: (() -> Unit)? = null,
    initialLanguage: AppLanguage = AppLanguage.FA,
    isPersianRtl: Boolean = false,
    enabledSections: Set<HelpSectionId>? = null,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val themePalette = LocalAppThemePalette.current
    val effectiveInitialLang = if (isPersianRtl) AppLanguage.FA else initialLanguage
    var modalLanguage by remember(effectiveInitialLang) { mutableStateOf(effectiveInitialLang) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedSectionIds by remember { mutableStateOf(setOf(HelpSectionId.MULTI_LANE)) }
    var isLangMenuExpanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val helpStrings = remember(modalLanguage) { HelpTranslations.getTranslations(modalLanguage) }

    // Filtered sections according to enabled feature flags and search query
    val availableSections = remember(helpStrings, enabledSections, searchQuery) {
        val baseSections = if (enabledSections != null) {
            helpStrings.sections.filter { enabledSections.contains(it.id) }
        } else {
            helpStrings.sections
        }

        if (searchQuery.isBlank()) {
            baseSections
        } else {
            val query = searchQuery.trim().lowercase()
            baseSections.filter { section ->
                section.title.lowercase().contains(query) ||
                    section.summary.lowercase().contains(query) ||
                    section.bullets.any { it.lowercase().contains(query) }
            }
        }
    }

    val toggleSection: (HelpSectionId) -> Unit = { id ->
        expandedSectionIds = if (expandedSectionIds.contains(id)) {
            expandedSectionIds - id
        } else {
            expandedSectionIds + id
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides modalLanguage.layoutDirection) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                dismissKeyboard()
                            }
                        )
                    }
                    .semantics {
                        dialog()
                        contentDescription = helpStrings.helpCenterTitle
                    }
                    .testTag("help_modal"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = themePalette.bgCenter,
                    border = BorderStroke(1.dp, themePalette.cardBorder),
                    shadowElevation = 24.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.90f)
                        .widthIn(max = 540.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        dismissKeyboard()
                                    }
                                )
                            }
                            .padding(16.dp)
                    ) {
                        // Header Bar with Icon, Title, Language Switcher, and Close Button
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
                                    color = themePalette.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = null,
                                            tint = themePalette.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = helpStrings.helpCenterTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = themePalette.textPrimary,
                                            fontSize = 16.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.semantics { heading() }
                                    )
                                    Text(
                                        text = "${availableSections.size} ${helpStrings.sectionsCount}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = themePalette.textSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }

                            // Language Selector Dropdown Trigger
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = themePalette.cardBackground,
                                    border = BorderStroke(1.dp, themePalette.cardBorder),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable(role = Role.Button) { isLangMenuExpanded = true }
                                        .testTag("help_language_selector")
                                        .semantics {
                                            contentDescription = helpStrings.selectLanguage
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = modalLanguage.flag,
                                            fontSize = 17.sp
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isLangMenuExpanded,
                                    onDismissRequest = { isLangMenuExpanded = false },
                                    modifier = Modifier.background(themePalette.cardBackground)
                                ) {
                                    AppLanguage.entries.forEach { lang ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(lang.flag, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${lang.nativeName} (${lang.englishName})",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = if (lang == modalLanguage) themePalette.primary else themePalette.textPrimary,
                                                            fontWeight = if (lang == modalLanguage) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    )
                                                    if (lang == modalLanguage) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = themePalette.primary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                modalLanguage = lang
                                                isLangMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Close Button (min 44x44dp hit target)
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("help_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = helpStrings.close,
                                    tint = themePalette.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = helpStrings.searchPlaceholder,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = themePalette.textMuted,
                                        fontSize = 12.5.sp
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = themePalette.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = themePalette.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = themePalette.cardBackground,
                                unfocusedContainerColor = themePalette.cardBackground,
                                focusedBorderColor = themePalette.primary,
                                unfocusedBorderColor = themePalette.cardBorder,
                                focusedTextColor = themePalette.textPrimary,
                                unfocusedTextColor = themePalette.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("help_search_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sections Accordion List
                        if (availableSections.isEmpty()) {
                            // Empty State
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = themePalette.textMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = helpStrings.noResultsFound,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = themePalette.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = helpStrings.noResultsSubtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = themePalette.textSecondary,
                                            fontSize = 12.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableSections, key = { it.id.key }) { section ->
                                    val isExpanded = expandedSectionIds.contains(section.id)
                                    HelpAccordionItem(
                                        section = section,
                                        isExpanded = isExpanded,
                                        onToggle = { toggleSection(section.id) }
                                    )
                                }

                                item {
                                    if (onStartInteractiveTour != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = themePalette.primary.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(role = Role.Button) { onStartInteractiveTour() }
                                                .testTag("help_start_walkthrough_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = themePalette.primary,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Audiotrack,
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (modalLanguage.isRtl) "شروع تور راهنمای تعاملی برنامه" else "Start Interactive App Tour",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            color = themePalette.primary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = if (modalLanguage.isRtl) "مشاهده گام‌به‌گام امکانات و بخش‌های مختلف در بوم برنامه" else "Step-by-step interactive walkthrough of all main app features",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = themePalette.textSecondary,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Helpful tip card at the bottom
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = themePalette.primary.copy(alpha = 0.08f),
                                        border = BorderStroke(0.8.dp, themePalette.primary.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = themePalette.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = helpStrings.tipTitle,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = themePalette.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp
                                                    )
                                                )
                                                Text(
                                                    text = helpStrings.tipDescription,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = themePalette.textSecondary,
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single Accordion Item in Help Center
 */
@Composable
private fun HelpAccordionItem(
    section: HelpSection,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current

    val iconVector: ImageVector = when (section.iconName) {
        "queue" -> Icons.Default.QueueMusic
        "audiotrack" -> Icons.Default.Audiotrack
        "tune" -> Icons.Default.Tune
        "equalizer" -> Icons.Default.Equalizer
        "mic" -> Icons.Default.Mic
        "video" -> Icons.Default.VideoLibrary
        "palette" -> Icons.Default.Palette
        "headphones" -> Icons.Default.Headphones
        "alt_route" -> Icons.Default.AltRoute
        else -> Icons.Default.Info
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = themePalette.cardBackground,
        border = BorderStroke(
            1.dp,
            if (isExpanded) themePalette.primary.copy(alpha = 0.5f) else themePalette.cardBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .animateContentSize(animationSpec = tween(220))
            .testTag("help_accordion_${section.id.key}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row (Clickable, min 48dp height)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .semantics {
                        contentDescription = "${section.title} - ${if (isExpanded) "Collapse" else "Expand"}"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isExpanded) themePalette.primary.copy(alpha = 0.2f) else themePalette.cardBorder.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = if (isExpanded) themePalette.primary else themePalette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) themePalette.primary else themePalette.textPrimary,
                            fontSize = 13.5.sp
                        )
                    )
                    Text(
                        text = section.summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = themePalette.textSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isExpanded) themePalette.primary else themePalette.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded Bullets Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 14.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(themePalette.cardBorder.copy(alpha = 0.6f))
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    section.bullets.forEachIndexed { index, bulletText ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(themePalette.primary)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = bulletText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = themePalette.textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
