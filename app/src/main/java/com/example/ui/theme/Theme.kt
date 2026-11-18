package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class AppThemeMode(val key: String, val label: String) {
    SYSTEM("system", "System Default"),
    LIGHT("light", "Light Mode"),
    DARK("dark", "Dark Mode");

    companion object {
        fun fromKey(key: String?): AppThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

private val HonoursLightColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = Color.White,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Gold600,
    onSecondary = Color.White,
    secondaryContainer = Gold50,
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = Teal600,
    onTertiary = Color.White,
    tertiaryContainer = Teal100,
    onTertiaryContainer = Color(0xFF042F2E),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val HonoursDarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Navy900,
    primaryContainer = Navy700,
    onPrimaryContainer = Navy50,
    secondary = Gold500,
    onSecondary = Navy900,
    secondaryContainer = Color(0xFF422006),
    onSecondaryContainer = Gold200,
    tertiary = Color(0xFF5EEAD4),
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = Color(0xFF115E59),
    onTertiaryContainer = Teal100,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Color(0xFFFECACA)
)

val HonoursShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HonoursSolutionTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> HonoursDarkColorScheme
        else -> HonoursLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HonoursTypography,
        shapes = HonoursShapes,
        content = content
    )
}
