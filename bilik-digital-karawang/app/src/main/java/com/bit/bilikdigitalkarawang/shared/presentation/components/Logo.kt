package com.bit.bilikdigitalkarawang.shared.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bit.bilikdigitalkarawang.R
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.ui.theme.Typography

@Composable
fun Logo(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    val logoResource = if (isDarkTheme) {
        R.drawable.logo_dark
    } else {
        R.drawable.logo
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.width(144.dp),
            painter = painterResource(logoResource),
            contentDescription = "Logo",
        )
        Text(
            text = Constant.VERSI,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}