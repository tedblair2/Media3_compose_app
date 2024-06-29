package com.github.tedblair2.muziki3.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.tedblair2.muziki3.R

// Set of Material typography styles to start with
val musicTxtFontFamily= FontFamily(
    Font(R.font.anektelugu_bold, FontWeight.Bold) ,
    Font(R.font.anektelugu_medium,FontWeight.Medium),
    Font(R.font.anektelugu_medium,FontWeight.SemiBold),
    Font(R.font.anektelugu_regular, FontWeight.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = musicTxtFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    bodyMedium = TextStyle(
        fontFamily = musicTxtFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)