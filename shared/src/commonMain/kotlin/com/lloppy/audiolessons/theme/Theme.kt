package com.lloppy.audiolessons.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Brand = Color(0xFF5B53E0)
private val BrandBright = Color(0xFF7B73FF)
private val Teal = Color(0xFF12B5AC)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E3FF),
    onPrimaryContainer = Color(0xFF17127A),
    secondary = Color(0xFF5C5D72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E0F4),
    onSecondaryContainer = Color(0xFF191A2C),
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8F4F0),
    onTertiaryContainer = Color(0xFF00201E),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF15151B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15151B),
    surfaceVariant = Color(0xFFEDECF4),
    onSurfaceVariant = Color(0xFF6A6A77),
    outline = Color(0xFFCAC9D6),
    outlineVariant = Color(0xFFE2E1EC),
)

private val DarkColors = darkColorScheme(
    primary = BrandBright,
    onPrimary = Color(0xFF11103A),
    primaryContainer = Color(0xFF3A37A8),
    onPrimaryContainer = Color(0xFFE6E3FF),
    secondary = Color(0xFFC4C4DC),
    onSecondary = Color(0xFF1B1C2C),
    secondaryContainer = Color(0xFF34354A),
    onSecondaryContainer = Color(0xFFE2E0F4),
    tertiary = Color(0xFF52DBD2),
    onTertiary = Color(0xFF00201E),
    tertiaryContainer = Color(0xFF005049),
    onTertiaryContainer = Color(0xFFC8F4F0),
    background = Color(0xFF101015),
    onBackground = Color(0xFFE7E6ED),
    surface = Color(0xFF1A1A22),
    onSurface = Color(0xFFE7E6ED),
    surfaceVariant = Color(0xFF2A2A36),
    onSurfaceVariant = Color(0xFF9C9CAB),
    outline = Color(0xFF3C3C4A),
    outlineVariant = Color(0xFF2A2A36),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            shapes = AppShapes,
            content = content,
        )
    }
}
