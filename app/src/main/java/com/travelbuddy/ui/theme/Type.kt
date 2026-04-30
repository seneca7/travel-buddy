package com.travelbuddy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseline = Typography()

val TravelBuddyTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontWeight = FontWeight.SemiBold),
    displayMedium = baseline.displayMedium.copy(fontWeight = FontWeight.SemiBold),
    displaySmall = baseline.displaySmall.copy(fontWeight = FontWeight.Medium),
    headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.Medium),
    titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.Medium),
    titleSmall = baseline.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = baseline.bodyLarge,
    bodyMedium = baseline.bodyMedium,
    bodySmall = baseline.bodySmall,
    labelLarge = baseline.labelLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),
    labelMedium = baseline.labelMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),
    labelSmall = baseline.labelSmall.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp),
)
