package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

enum class UiScaleMode(val displayName: String, val scaleFactor: Float, val description: String) {
    COMPACT("Ringkas (85%)", 0.85f, "Elemen lebih padat, muat lebih banyak informasi"),
    DEFAULT("Standar (100%)", 1.0f, "Ukuran proporsional bawaan sistem"),
    LARGE("Besar (115%)", 1.15f, "Teks dan tombol lebih besar, nyaman dibaca"),
    EXTRA_LARGE("Ekstra Besar (130%)", 1.30f, "Keterbacaan maksimal untuk kemudahan penglihatan")
}

enum class AppThemeMode(val title: String, val description: String) {
    ELEGANT_DARK("Elegant Dark", "Tema bernuansa emerald gelap & aksen emas berkilau"),
    LIGHT_MODE("Light", "Tema terang bersih dengan tipografi jelas & teduh"),
    HIGH_CONTRAST_LIGHT("High Contrast Light", "Kontras maksimal latar putih & teks hitam pekat untuk aksesibilitas"),
    HIGH_CONTRAST_DARK("High Contrast Dark", "Latar hitam murni & teks kontras tinggi untuk keterbacaan tajam")
}

private val IslamicDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLight,
    secondary = GoldAccent,
    onSecondary = Color(0xFF1E1A00),
    secondaryContainer = Color(0xFF3E3600),
    onSecondaryContainer = GoldLight,
    tertiary = EmeraldLight,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = Color(0xFFEF5350),
    onError = Color.White
)

private val IslamicLightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCE8E2),
    onPrimaryContainer = Color(0xFF002B24),
    secondary = GoldRoyal, // Radiant Islamic Gold (Emas Muamalah berkilau dengan kontras tinggi)
    onSecondary = Color.White,
    secondaryContainer = GoldContainerLight,
    onSecondaryContainer = OnGoldContainerLight,
    tertiary = Color(0xFF004D40),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFB5C8C4),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00251A),
    secondary = HighContrastLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF3E2700),
    tertiary = Color(0xFF004D40),
    onTertiary = Color.White,
    background = HighContrastLightBackground,
    onBackground = HighContrastLightTextPrimary,
    surface = HighContrastLightSurface,
    onSurface = HighContrastLightTextPrimary,
    surfaceVariant = HighContrastLightSurfaceVariant,
    onSurfaceVariant = HighContrastLightTextSecondary,
    outline = HighContrastLightBorder,
    error = Color(0xFFB71C1C),
    onError = Color.White
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = HighContrastDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color.White,
    secondary = HighContrastDarkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF665500),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF00E5FF),
    onTertiary = Color.Black,
    background = HighContrastDarkBackground,
    onBackground = HighContrastDarkTextPrimary,
    surface = HighContrastDarkSurface,
    onSurface = HighContrastDarkTextPrimary,
    surfaceVariant = HighContrastDarkSurfaceVariant,
    onSurfaceVariant = HighContrastDarkTextSecondary,
    outline = HighContrastDarkBorder,
    error = Color(0xFFFF1744),
    onError = Color.Black
)

@Composable
fun AmanahLedgerTheme(
    darkTheme: Boolean = true,
    highContrast: Boolean = false,
    uiScaleFactor: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast && !darkTheme -> HighContrastLightColorScheme
        darkTheme -> IslamicDarkColorScheme
        else -> IslamicLightColorScheme
    }

    val currentDensity = LocalDensity.current
    val effectiveScale = uiScaleFactor.coerceIn(0.75f, 1.6f)
    val scaledDensity = remember(currentDensity, effectiveScale) {
        Density(
            density = currentDensity.density * effectiveScale,
            fontScale = currentDensity.fontScale * effectiveScale
        )
    }

    CompositionLocalProvider(
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

