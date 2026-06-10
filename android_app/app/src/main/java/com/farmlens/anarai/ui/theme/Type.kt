package com.farmlens.anarai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.farmlens.anarai.R

val JosefinSans = FontFamily(
    Font(R.font.josefinsans)
)

val Typography = Typography().let { defaultTypography ->
    Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold)
    )
}
