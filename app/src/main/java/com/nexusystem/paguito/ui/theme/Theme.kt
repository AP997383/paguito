package com.nexusystem.paguito.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nexusystem.paguito.utils.findActivity

private val LightColors = lightColorScheme(
    primary = PrimaryLight, onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F5E3), onPrimaryContainer = Color(0xFF003D29),
    secondary = SecondaryLight, onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEDE3), onSecondaryContainer = Color(0xFF213E30),
    tertiary = Color(0xFF2866AD), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCEAFF), onTertiaryContainer = Color(0xFF123A67),
    background = BackgroundLight, onBackground = TextPrimaryLight,
    surface = SurfaceLight, onSurface = TextPrimaryLight,
    surfaceVariant = WebsiteSurfaceLight, onSurfaceVariant = WebsiteTextSecondaryLight,
    outline = Color(0xFF718579), outlineVariant = WebsiteBorderLight,
    error = Color(0xFFBA3535), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF6D1414),
    inverseSurface = Color(0xFF20392D), inverseOnSurface = Color(0xFFE9F5ED),
    surfaceTint = PrimaryLight
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark, onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF12543C), onPrimaryContainer = Color(0xFFB8F4D5),
    secondary = SecondaryDark, onSecondary = Color(0xFF123728),
    secondaryContainer = Color(0xFF304B3C), onSecondaryContainer = Color(0xFFD5E9DB),
    tertiary = Color(0xFFA9CCFF), onTertiary = Color(0xFF00305E),
    tertiaryContainer = Color(0xFF214B7D), onTertiaryContainer = Color(0xFFDCEAFF),
    background = BackgroundDark, onBackground = TextPrimaryDark,
    surface = SurfaceDark, onSurface = TextPrimaryDark,
    surfaceVariant = WebsiteSurfaceDark, onSurfaceVariant = WebsiteTextSecondaryDark,
    outline = Color(0xFF819B89), outlineVariant = WebsiteBorderDark,
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF7C2927), onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE2EEE5), inverseOnSurface = Color(0xFF20392D),
    surfaceTint = PrimaryDark
)

private val PaguitoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PaguitoTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val appearance = rememberAppearanceSettings()
    val systemDark = isSystemInDarkTheme()
    val effectiveDark = darkTheme ?: when (appearance.mode) {
        AppearanceMode.System -> systemDark
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
    val baseColors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        effectiveDark -> DarkColors
        else -> LightColors
    }
    val colors = if (dynamicColor || appearance.accent == AccentColor.Paguito) baseColors else {
        val primary = if (effectiveDark) appearance.accent.dark else appearance.accent.light
        baseColors.copy(
            primary = primary, onPrimary = if (effectiveDark) Color(0xFF161616) else Color.White,
            primaryContainer = lerp(baseColors.background, primary, if (effectiveDark) 0.25f else 0.12f),
            onPrimaryContainer = primary,
            secondary = primary, onSecondary = if (effectiveDark) Color(0xFF161616) else Color.White,
            secondaryContainer = lerp(baseColors.background, primary, 0.1f), onSecondaryContainer = primary,
            tertiary = primary, onTertiary = if (effectiveDark) Color(0xFF161616) else Color.White,
            tertiaryContainer = lerp(baseColors.background, primary, 0.12f), onTertiaryContainer = primary,
            surfaceVariant = lerp(baseColors.surface, primary, if (effectiveDark) 0.09f else 0.035f),
            outlineVariant = lerp(baseColors.surface, primary, if (effectiveDark) 0.25f else 0.15f),
            surfaceTint = primary
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            context.findActivity()?.window?.let { window ->
                window.statusBarColor = colors.background.toArgb()
                window.navigationBarColor = colors.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !effectiveDark
                    isAppearanceLightNavigationBars = !effectiveDark
                }
            }
        }
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalAppearance provides appearance, LocalContentColor provides colors.onBackground) {
        MaterialTheme(colorScheme = colors, typography = Typography, shapes = PaguitoShapes, content = content)
    }
}
