package com.lloppy.akta.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
    secondary = Color(0xFF5C5D72),
    onSecondary = Color.White,
    tertiary = Teal,
    onTertiary = Color.White,
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF15151B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15151B),
)

private val DarkColors = darkColorScheme(
    primary = BrandBright,
    onPrimary = Color(0xFF11103A),
    secondary = Color(0xFFC4C4DC),
    onSecondary = Color(0xFF1B1C2C),
    tertiary = Color(0xFF52DBD2),
    onTertiary = Color(0xFF00201E),
    background = Color(0xFF101015),
    onBackground = Color(0xFFE7E6ED),
    surface = Color(0xFF1A1A22),
    onSurface = Color(0xFFE7E6ED),
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
