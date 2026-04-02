package com.bit.bilikdigitalkarawang.features.auth.presentation.pin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinInput(
    pinValue: String,
    onPinValueChange: (String) -> Unit,
    isError: Boolean = false
) {
    val pinLength = 6
    val focusRequester = remember { FocusRequester() }

    // Auto focus ketika composable pertama kali dimuat
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box {
        // Hidden TextField untuk input - posisi di atas visual boxes
        BasicTextField(
            value = pinValue,
            onValueChange = { newValue ->
                if (newValue.length <= pinLength && newValue.all { it.isDigit() }) {
                    onPinValueChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .focusRequester(focusRequester)
                .alpha(0f), // Transparan tapi tetap bisa di-focus
            decorationBox = { innerTextField ->
                // Box kosong untuk menghilangkan visual TextField
                Box(modifier = Modifier.fillMaxSize())
            }
        )

        // Visual PIN boxes
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pinLength) { index ->
                    PinBox(
                        value = if (index < pinValue.length) "•" else "",
                        isFilled = index < pinValue.length,
                        isError = isError,
                        isActive = index == pinValue.length,
                    )
                }
            }
        }

    }
}

@Composable
private fun PinBox(
    value: String,
    isFilled: Boolean,
    isError: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isActive -> MaterialTheme.colorScheme.primary
        isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline
    }

    val backgroundColor = when {
        isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )
    }
}