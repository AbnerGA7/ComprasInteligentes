package com.grupo3.compras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grupo3.compras.domain.Categoria

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5FBF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FA),
    onPrimaryContainer = Color(0xFF0B2A57),
    secondary = Color(0xFF138076),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3F1EE),
    onSecondaryContainer = Color(0xFF053A35),
    tertiary = Color(0xFFD9761A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE6CC),
    onTertiaryContainer = Color(0xFF5A2E00),
    background = Color(0xFFF4F7FC),
    onBackground = Color(0xFF12233F),
    surface = Color.White,
    onSurface = Color(0xFF12233F),
    surfaceVariant = Color(0xFFE8EDF6),
    onSurfaceVariant = Color(0xFF4A5A75),
    outline = Color(0xFF9AA8C0),
    outlineVariant = Color(0xFFD5DDEB),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F9FD),
    surfaceContainer = Color(0xFFEEF2F9),
    surfaceContainerHigh = Color(0xFFE7ECF5),
    surfaceContainerHighest = Color(0xFFE1E7F2),
    error = Color(0xFFC0392B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DB0FF),
    onPrimary = Color(0xFF08224A),
    primaryContainer = Color(0xFF1D3E74),
    onPrimaryContainer = Color(0xFFD7E5FF),
    secondary = Color(0xFF5FD3C6),
    onSecondary = Color(0xFF00302B),
    secondaryContainer = Color(0xFF0C4A44),
    onSecondaryContainer = Color(0xFFB6F3EB),
    tertiary = Color(0xFFFFB366),
    onTertiary = Color(0xFF4A2500),
    tertiaryContainer = Color(0xFF6B3A00),
    onTertiaryContainer = Color(0xFFFFDDB8),
    background = Color(0xFF0D1626),
    onBackground = Color(0xFFE6ECF7),
    surface = Color(0xFF121D31),
    onSurface = Color(0xFFE6ECF7),
    surfaceVariant = Color(0xFF22314B),
    onSurfaceVariant = Color(0xFFB7C3D9),
    outline = Color(0xFF73839F),
    outlineVariant = Color(0xFF2C3C58),
    surfaceContainerLowest = Color(0xFF0A1220),
    surfaceContainerLow = Color(0xFF111B2E),
    surfaceContainer = Color(0xFF16233A),
    surfaceContainerHigh = Color(0xFF1C2B45),
    surfaceContainerHighest = Color(0xFF233350),
    error = Color(0xFFFF8A80)
)

private val ComprasShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ComprasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = ComprasShapes,
        content = content
    )
}

val GradienteCabecera: Brush = Brush.linearGradient(listOf(Color(0xFF12233F), Color(0xFF1F5FBF)))

fun Categoria.color(): Color = when (this) {
    Categoria.OFICINA -> Color(0xFF2B6FCB)
    Categoria.ENTRETENIMIENTO -> Color(0xFF8E54E9)
    Categoria.TECNOLOGIA -> Color(0xFF138076)
    Categoria.HOGAR -> Color(0xFFE8872B)
    Categoria.DEPORTE -> Color(0xFFD6455D)
}
