package com.example.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.audio.AudioLaneState
import com.example.ui.theme.LocalAppThemePalette

@Composable
fun EditLaneNameDialog(
    lane: AudioLaneState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false
) {
    val themePalette = LocalAppThemePalette.current
    var text by remember { mutableStateOf(lane.name) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val isValidName = text.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = {
            dismissKeyboard()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        containerColor = themePalette.cardBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    dismissKeyboard()
                }
            )
        },
        title = {
            Text(
                text = if (isPersianRtl) "ویرایش نام لاین" else "Rename Audio Lane",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = themePalette.textPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                dismissKeyboard()
                            }
                        )
                    }
                    .testTag("rename_dialog")
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(if (isPersianRtl) "نام لاین" else "Lane Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValidName) {
                                dismissKeyboard()
                                onConfirm(text.trim())
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = themePalette.textPrimary,
                        unfocusedTextColor = themePalette.textPrimary,
                        focusedBorderColor = themePalette.primary,
                        unfocusedBorderColor = themePalette.cardBorder,
                        focusedLabelColor = themePalette.primary,
                        unfocusedLabelColor = themePalette.textSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_text_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidName) {
                        dismissKeyboard()
                        onConfirm(text.trim())
                    }
                },
                enabled = isValidName,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themePalette.primary,
                    disabledContainerColor = themePalette.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("confirm_rename_button")
            ) {
                Text(
                    text = if (isPersianRtl) "ذخیره" else "Save",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dismissKeyboard()
                    onDismiss()
                },
                modifier = Modifier.testTag("cancel_rename_button")
            ) {
                Text(
                    text = if (isPersianRtl) "انصراف" else "Cancel",
                    color = themePalette.textSecondary
                )
            }
        }
    )
}
