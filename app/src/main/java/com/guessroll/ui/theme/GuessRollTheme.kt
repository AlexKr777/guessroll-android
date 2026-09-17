package com.guessroll.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guessroll.R

val Ink = Color(0xFF0E1019)
val InkRaised = Color(0xFF151622)
val NightPlum = Color(0xFF1B1728)
val DeepBlue = Color(0xFF151B27)
val Midnight = Color(0xFF201D2B)

val Glass = Color(0xF21A1824)
val GlassStrong = Color(0xFC221F2F)
val GlassSoft = Color(0xEA151722)

val Panel = Color(0xFF211F2D)
val PanelSoft = Color(0xFF2E2A3A)
val PanelDeep = Color(0xFF11131D)
val PanelElevated = Color(0xFF343047)
val PanelStroke = Color(0x38D8D0F0)
val PanelStrokeStrong = Color(0x56E3D8F5)

val Violet = Color(0xFF7D78B7)
val WarmViolet = Color(0xFF9589D0)
val VioletBright = Color(0xFFE4DBFF)
val ElectricBlue = Color(0xFFB8CBEF)
val Sky = Color(0xFFC9D9F3)
val Pink = Color(0xFFE3B2A4)
val Berry = Color(0xFFB788A8)
val Coral = Color(0xFFE38B76)
val Amber = Color(0xFFECCA82)
val Mint = Color(0xFF9ACFB6)

val GlowViolet = Color(0xFF706BB1)
val GlowBlue = Color(0xFF617FAC)
val GlowPink = Color(0xFFD79D88)
val GlowMint = Color(0xFF6FA68E)

val Danger = Coral
val Warning = Amber
val Success = Mint

val TextPrimary = Color(0xFFFFF7EC)
val TextSecondary = Color(0xFFE9E2F4)
val TextMuted = Color(0xFFCFC7DC)
val TextFaint = Color(0xFFA09AB2)
val TextDisabled = Color(0xFF777288)

val Space4 = 4.dp
val Space8 = 8.dp
val Space12 = 12.dp
val Space16 = 16.dp
val Space20 = 20.dp
val Space24 = 24.dp
val Space32 = 32.dp

val RadiusMedium = 20.dp
val RadiusLarge = 24.dp
val RadiusXL = 28.dp

val BackgroundBase = Ink
val BackgroundDeep = Midnight
val BackgroundGlowCyan = GlowBlue
val BackgroundGlowViolet = GlowViolet
val BackgroundGlowPink = GlowPink
val SurfaceQuiet = GlassSoft
val SurfacePrimary = Glass
val SurfaceSecondary = PanelDeep
val SurfaceGlass = Glass
val SurfaceGlassStrong = GlassStrong
val SurfaceHero = PanelElevated
val BorderSubtle = PanelStroke
val BorderMedium = PanelStrokeStrong
val BorderFocus = VioletBright
val BorderGlow = ElectricBlue
val TextOnAccent = TextPrimary
val AccentViolet = Violet
val AccentBlue = ElectricBlue
val AccentCyan = Sky
val AccentPink = Berry
val SuccessSoft = Mint.copy(alpha = 0.14f)
val WarningSoft = Amber.copy(alpha = 0.14f)
val ErrorSoft = Coral.copy(alpha = 0.14f)

private val GuessRollColorScheme: ColorScheme = darkColorScheme(
    primary = VioletBright,
    secondary = Amber,
    tertiary = Mint,
    background = Ink,
    surface = Panel,
    surfaceVariant = PanelSoft,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    onError = TextPrimary,
)

private val GuessRollFontFamily = FontFamily(
    Font(R.font.manrope_variable, FontWeight.Normal),
    Font(R.font.manrope_variable, FontWeight.Medium),
    Font(R.font.manrope_variable, FontWeight.SemiBold),
    Font(R.font.manrope_variable, FontWeight.Bold),
)

private val GuessRollTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 39.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 35.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = GuessRollFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
    ),
)

@Composable
fun GuessRollTheme(content: @Composable () -> Unit) {
    val motionSettings = rememberGuessRollMotionSettings()
    CompositionLocalProvider(LocalGuessRollMotion provides motionSettings) {
        MaterialTheme(
            colorScheme = GuessRollColorScheme,
            typography = GuessRollTypography,
            content = content,
        )
    }
}
