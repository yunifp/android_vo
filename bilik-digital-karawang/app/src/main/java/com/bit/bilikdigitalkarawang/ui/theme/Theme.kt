package com.bit.bilikdigitalkarawang.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary           = Color(0xFF0D83FD),   // Biru utama (tombol)
    onPrimary         = Color(0xFFFFFFFF),   // Putih untuk kontras yang baik
    primaryContainer  = Color(0xFF004C99),   // Biru gelap untuk container
    onPrimaryContainer = Color(0xFFB3D7FF),  // Biru muda untuk teks di container

    secondary         = Color(0xFF5A9BFF),   // Biru medium (outline field)
    onSecondary       = Color(0xFF001F3D),   // Biru sangat gelap
    secondaryContainer = Color(0xFF003366),  // Biru gelap untuk container
    onSecondaryContainer = Color(0xFFCCE5FF), // Biru sangat muda

    tertiary          = Color(0xFF66B3FF),   // Biru accent untuk link
    onTertiary        = Color(0xFF001A33),   // Biru gelap untuk kontras

    background        = Color(0xFF0A0E1A),   // Background gelap dengan hint biru
    onBackground      = Color(0xFFE1E8F5),   // Putih kebiruan

    surface           = Color(0xFF0F1419),   // Surface gelap dengan hint biru
    onSurface         = Color(0xFFE1E8F5),   // Putih kebiruan
    surfaceVariant    = Color(0xFF1A2332),   // Variant dengan biru gelap
    onSurfaceVariant  = Color(0xFFB8C8D9),   // Abu-abu kebiruan

    error             = Color(0xFFFF6B6B),   // Merah lembut
    onError           = Color(0xFF330000)    // Merah gelap
)

private val LightColorScheme = lightColorScheme(
    primary           = Color(0xFF0D83FD),   // Biru utama (tombol Sign-In)
    onPrimary         = Color(0xFFFFFFFF),   // Putih untuk teks tombol
    primaryContainer  = Color(0xFFB3D7FF),   // Biru muda untuk hover/ripple
    onPrimaryContainer = Color(0xFF001A33),  // Biru gelap untuk teks container

    secondary         = Color(0xFF4080E6),   // Biru medium (outline field)
    onSecondary       = Color(0xFFFFFFFF),   // Putih
    secondaryContainer = Color(0xFFE6F2FF),  // Biru sangat muda untuk container
    onSecondaryContainer = Color(0xFF001F3D), // Biru gelap

    tertiary          = Color(0xFF0066CC),   // Biru gelap untuk accent/link
    onTertiary        = Color(0xFFFFFFFF),   // Putih

    background        = Color(0xFFF8FAFF),   // Background putih dengan hint biru
    onBackground      = Color(0xFF0A1420),   // Hampir hitam dengan hint biru

    surface           = Color(0xFFFFFFFF),   // Surface putih bersih
    onSurface         = Color(0xFF0A1420),   // Hampir hitam dengan hint biru
    surfaceVariant    = Color(0xFFE6F0FF),   // Variant biru sangat muda
    onSurfaceVariant  = Color(0xFF1A2332),   // Biru gelap

    error             = Color(0xFFE53E3E),   // Merah untuk error
    onError           = Color(0xFFFFFFFF)    // Putih
)
@Composable
fun BilikDigitalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}