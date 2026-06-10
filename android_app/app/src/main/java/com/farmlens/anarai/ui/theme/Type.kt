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
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.Black),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = JosefinSans, fontWeight = FontWeight.ExtraBold)
    )
}
