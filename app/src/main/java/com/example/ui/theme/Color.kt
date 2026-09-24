package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// MATERIAL YOU (MATERIAL DESIGN 3) PALETTE
// ==========================================

// Primary (Industrial Sapphire / Cerulean)
val M3PrimaryLight = Color(0xFF006495)
val M3OnPrimaryLight = Color(0xFFFFFFFF)
val M3PrimaryContainerLight = Color(0xFFCBE6FF)
val M3OnPrimaryContainerLight = Color(0xFF001E30)

val M3PrimaryDark = Color(0xFF8ECEFF)
val M3OnPrimaryDark = Color(0xFF00344F)
val M3PrimaryContainerDark = Color(0xFF004B72)
val M3OnPrimaryContainerDark = Color(0xFFCBE6FF)

// Secondary (Slate Blue)
val M3SecondaryLight = Color(0xFF50606E)
val M3OnSecondaryLight = Color(0xFFFFFFFF)
val M3SecondaryContainerLight = Color(0xFFD4E4F5)
val M3OnSecondaryContainerLight = Color(0xFF0D1D29)

val M3SecondaryDark = Color(0xFFB8C8D9)
val M3OnSecondaryDark = Color(0xFF22323F)
val M3SecondaryContainerDark = Color(0xFF394856)
val M3OnSecondaryContainerDark = Color(0xFFD4E4F5)

// Tertiary (Teal / Sage)
val M3TertiaryLight = Color(0xFF246864)
val M3OnTertiaryLight = Color(0xFFFFFFFF)
val M3TertiaryContainerLight = Color(0xFFAEECE5)
val M3OnTertiaryContainerLight = Color(0xFF00201E)

val M3TertiaryDark = Color(0xFF92D0C9)
val M3OnTertiaryDark = Color(0xFF003734)
val M3TertiaryContainerDark = Color(0xFF044E4B)
val M3OnTertiaryContainerDark = Color(0xFFAEECE5)

// Surface & Background (Light)
val M3BackgroundLight = Color(0xFFF7F9FD)
val M3OnBackgroundLight = Color(0xFF181C20)
val M3SurfaceLight = Color(0xFFF7F9FD)
val M3OnSurfaceLight = Color(0xFF181C20)
val M3SurfaceVariantLight = Color(0xFFDEE3EB)
val M3OnSurfaceVariantLight = Color(0xFF42474E)
val M3SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val M3SurfaceContainerLowLight = Color(0xFFF1F4F8)
val M3SurfaceContainerLight = Color(0xFFEBEFF3)
val M3SurfaceContainerHighLight = Color(0xFFE5E9ED)
val M3SurfaceContainerHighestLight = Color(0xFFDFE3E8)
val M3OutlineLight = Color(0xFF72787E)
val M3OutlineVariantLight = Color(0xFFC2C7CE)

// Surface & Background (Dark - M3 Deep Slate)
val M3BackgroundDark = Color(0xFF101418)
val M3OnBackgroundDark = Color(0xFFDFE3E8)
val M3SurfaceDark = Color(0xFF101418)
val M3OnSurfaceDark = Color(0xFFDFE3E8)
val M3SurfaceVariantDark = Color(0xFF42474E)
val M3OnSurfaceVariantDark = Color(0xFFC2C7CE)
val M3SurfaceContainerLowestDark = Color(0xFF0B0E12)
val M3SurfaceContainerLowDark = Color(0xFF181C20)
val M3SurfaceContainerDark = Color(0xFF1C2024)
val M3SurfaceContainerHighDark = Color(0xFF272B2F)
val M3SurfaceContainerHighestDark = Color(0xFF31353A)
val M3OutlineDark = Color(0xFF8C9198)
val M3OutlineVariantDark = Color(0xFF42474E)

// Error
val M3ErrorLight = Color(0xFFBA1A1A)
val M3OnErrorLight = Color(0xFFFFFFFF)
val M3ErrorContainerLight = Color(0xFFFFDAD6)
val M3OnErrorContainerLight = Color(0xFF410002)

val M3ErrorDark = Color(0xFFFFB4AB)
val M3OnErrorDark = Color(0xFF690005)
val M3ErrorContainerDark = Color(0xFF93000A)
val M3OnErrorContainerDark = Color(0xFFFFDAD6)

// Custom semantic status colors (Material 3 harmonized)
val M3StatusSuccess = Color(0xFF2E7D32)
val M3StatusSuccessContainer = Color(0xFFC8E6C9)
val M3StatusWarning = Color(0xFFED6C02)
val M3StatusWarningContainer = Color(0xFFFFE0B2)
val M3StatusInfo = Color(0xFF0288D1)
val M3StatusInfoContainer = Color(0xFFB3E5FC)

// Compatibility Aliases for components
val PdfDarkBackground = M3BackgroundDark
val PdfCardBackground = M3SurfaceContainerDark
val PdfCardBorder = M3OutlineVariantDark
val PdfInputBackground = M3SurfaceContainerHighDark
val PdfInputBorder = M3OutlineVariantDark

val PdfCyanPrimary = M3PrimaryDark
val PdfCyanGlow = M3PrimaryContainerDark
val PdfCyanDark = Color(0xFF004B72)
val PdfTeal = M3TertiaryDark
val PdfTealDark = M3TertiaryContainerDark
val PdfGreen = Color(0xFF81C784)
val PdfGreenDark = Color(0xFF2E7D32)
val PdfAmber = Color(0xFFFFB74D)
val PdfOrange = Color(0xFFFF8A65)
val PdfRed = M3ErrorDark
val PdfPurple = Color(0xFFCE93D8)

val PdfTextWhite = M3OnSurfaceDark
val PdfTextMuted = M3OnSurfaceVariantDark
val PdfTextDark = M3OnPrimaryLight
val PdfTextPlaceholder = M3OutlineDark

val PdfBrandBadgeBg = M3PrimaryContainerDark
val PdfBrandBadgeBorder = M3PrimaryDark.copy(alpha = 0.4f)
val PdfBrandBadgeText = M3OnPrimaryContainerDark

val PdfButtonSelectedBg = M3PrimaryContainerDark
val PdfButtonSelectedBorder = M3PrimaryDark
val PdfButtonUnselectedBg = M3SurfaceContainerHighDark
val PdfButtonUnselectedBorder = M3OutlineVariantDark

val IndustrialNavyDark = M3BackgroundDark
val IndustrialNavySurface = M3SurfaceContainerDark
val IndustrialNavySurfaceVariant = M3SurfaceContainerHighDark
val IndustrialNavyBorder = M3OutlineVariantDark
val IndustrialNavyCard = M3SurfaceContainerDark
val CyanPrimary = M3PrimaryDark
val CyanPrimaryVariant = M3PrimaryContainerDark
val CyanLight = M3OnPrimaryContainerDark
val AmberAccent = PdfAmber
val AmberAccentDark = PdfOrange
val EmeraldSuccess = PdfGreen
val CrimsonError = PdfRed
val TextPrimary = M3OnSurfaceDark
val TextSecondary = M3OnSurfaceVariantDark
val TextTertiary = M3OutlineDark

