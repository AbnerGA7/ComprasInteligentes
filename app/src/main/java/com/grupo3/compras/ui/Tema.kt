package com.grupo3.compras.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.util.Locale

private val Colores = lightColorScheme(
    primary = Color(0xFF0B5CAD),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4F5),
    onPrimaryContainer = Color(0xFF0B2A4A),
    secondary = Color(0xFF455A74),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6F2),
    onSecondaryContainer = Color(0xFF0B2A4A),
    background = Color(0xFFFAFBFC),
    onBackground = Color(0xFF1B1F24),
    surface = Color.White,
    onSurface = Color(0xFF1B1F24),
    surfaceVariant = Color(0xFFEDF0F4),
    onSurfaceVariant = Color(0xFF4F5967),
    outline = Color(0xFF8A94A3),
    outlineVariant = Color(0xFFD9DEE5),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainer = Color(0xFFF2F4F7),
    surfaceContainerHigh = Color(0xFFECEFF3),
    surfaceContainerHighest = Color(0xFFE6EAEF)
)

@Composable
fun ComprasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, content = content)
}

fun Double.soles(): String = "S/ " + String.format(Locale.US, "%,.2f", this)
