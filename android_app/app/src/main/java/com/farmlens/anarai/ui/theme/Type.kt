package com.farmlens.anarai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.farmlens.anarai.R

val JosefinSans = FontFamily(
    Font(R.font.josefinsans, FontWeight.Normal),
    Font(R.font.josefinsans, FontWeight.Bold),
    Font(R.font.josefinsans, FontWeight.ExtraBold),
    Font(R.font.josefinsans, FontWeight.Light),
    Font(R.font.josefinsans, FontWeight.Medium),
    Font(R.font.josefinsans, FontWeight.SemiBold),
    Font(R.font.josefinsans, FontWeight.Thin)
)

val Typography = Typography().let { defaultTypography ->
    Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = JosefinSans),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = JosefinSans),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = JosefinSans),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = JosefinSans),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = JosefinSans),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = JosefinSans),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Medium),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Medium),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Medium)
    )
}
