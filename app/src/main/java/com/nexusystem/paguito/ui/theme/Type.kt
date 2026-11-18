package com.nexusystem.paguito.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun text(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.SansSerif, fontWeight = weight,
    fontSize = size.sp, lineHeight = height.sp, letterSpacing = 0.sp
)

val Typography = Typography(
    displayLarge = text(48, 56, FontWeight.Bold),
    displayMedium = text(40, 48, FontWeight.Bold),
    displaySmall = text(34, 42, FontWeight.Bold),
    headlineLarge = text(30, 38, FontWeight.Bold),
    headlineMedium = text(26, 34, FontWeight.Bold),
    headlineSmall = text(24, 32, FontWeight.Bold),
    titleLarge = text(22, 28, FontWeight.Bold),
    titleMedium = text(16, 24, FontWeight.SemiBold),
    titleSmall = text(14, 20, FontWeight.SemiBold),
    bodyLarge = text(16, 24), bodyMedium = text(14, 22), bodySmall = text(12, 18),
    labelLarge = text(14, 20, FontWeight.SemiBold),
    labelMedium = text(12, 18, FontWeight.SemiBold),
    labelSmall = text(11, 16, FontWeight.Medium)
)
