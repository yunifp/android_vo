package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton
import kotlin.text.isNotBlank

@Composable
fun CPinConfirmationDialog(
    title: String = "Konfirmasi PIN",
    onConfirm: (String, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinPanitia by remember { mutableStateOf("") }
    var pinPengembang by remember { mutableStateOf("") }

    // Biar bisa cek kosong
    val isValid = pinPanitia.isNotBlank() && pinPengembang.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCancel() }
    ) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { onCancel() },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    OutlinedTextField(
                        value = pinPanitia,
                        onValueChange = { pinPanitia = it },
                        label = { Text("PIN Panitia") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                    )

                    OutlinedTextField(
                        value = pinPengembang,
                        onValueChange = { pinPengembang = it },
                        label = { Text("PIN Buka Rekap") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                    )
                }
            },
            confirmButton = {
                CButton(
                    label = "Konfirmasi",
                    onClick = {
                        if (isValid) {
                            onConfirm(pinPanitia, pinPengembang)
                        }
                    }
                )
            },
            dismissButton = {
                CButton(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    label = "Batal",
                    onClick = { onCancel() }
                )
            },
            modifier = modifier
        )
    }
}
