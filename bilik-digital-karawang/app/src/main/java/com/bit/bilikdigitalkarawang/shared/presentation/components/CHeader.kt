package com.bit.bilikdigitalkarawang.shared.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        BackButton(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Logo(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}