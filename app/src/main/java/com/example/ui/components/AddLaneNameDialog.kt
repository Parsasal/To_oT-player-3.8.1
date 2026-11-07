package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalAppThemePalette

@Composable
fun AddLaneNameDialog(
    defaultName: String,
    isPersianRtl: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themePalette = LocalAppThemePalette.current
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val submit = {
        dismissKeyboard()
        val finalName = text.trim().ifEmpty { defaultName }
        onConfirm(finalName)
    }

    Dialog(
        onDismissRequest = {
            dismissKeyboard()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = themePalette.cardBackground,
            border = BorderStroke(1.dp, themePalette.cardBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { dismissKeyboard() })
                }
                .testTag("add_lane_name_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Row: Compact icon + title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = themePalette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (isPersianRtl) "افزودن لاین صوتی جدید" else "Add New Audio Lane",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.textPrimary,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Prompt description text
                Text(
                    text = if (isPersianRtl)
                        "اسم لاین صوتی را چه انتخاب می‌کنید؟"
                    else
                        "What name do you choose for this audio lane?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = themePalette.textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPersianRtl)
                        "(در صورت عدم انتخاب، نام پیش‌فرض «$defaultName» قرار می‌گیرد)"
                    else
                        "(If left blank, \"$defaultName\" will be used automatically)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = themePalette.textMuted,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input Field: Compact, single line, no bulky label
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = defaultName,
                            color = themePalette.textMuted.copy(alpha = 0.55f),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themePalette.primary,
                        unfocusedBorderColor = themePalette.cardBorder,
                        focusedTextColor = themePalette.textPrimary,
                        unfocusedTextColor = themePalette.textPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_lane_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: Compact, proportional, zero text wrapping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            dismissKeyboard()
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(35.dp)
                            .testTag("btn_cancel_add_lane")
                    ) {
                        Text(
                            text = if (isPersianRtl) "انصراف" else "Cancel",
                            color = themePalette.textMuted,
                            fontSize = 11.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Default Name Button: Quick fallback button
                    OutlinedButton(
                        onClick = {
                            dismissKeyboard()
                            onConfirm(defaultName)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = themePalette.textPrimary
                        ),
                        border = BorderStroke(1.dp, themePalette.cardBorder),
                        modifier = Modifier
                            .height(35.dp)
                            .testTag("btn_use_default_lane_name")
                    ) {
                        Text(
                            text = if (isPersianRtl) "پیش‌فرض" else "Default",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Primary Confirm Button
                    Button(
                        onClick = { submit() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themePalette.primary,
                            contentColor = themePalette.buttonTextColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(35.dp)
                            .testTag("btn_confirm_add_lane")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPersianRtl) "تأیید" else "Confirm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
