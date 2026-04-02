package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton

@Composable
fun CConfirmationDialog(
    title: String = "Konfirmasi",
    message: String = "Apakah Anda yakin ingin melanjutkan?",
    confirmText: String = "Ya",
    cancelText: String = "Batal",
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Background hitam semi-transparan tanpa blur
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
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
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                CButton(
                    label = confirmText,
                    onClick = { onConfirm() }
                )
            },
            dismissButton = {
                CButton(
                    backgroundColor = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.onError,
                    label = cancelText,
                    onClick = { onCancel() }
                )
            },
            modifier = modifier
        )
    }
}