package com.alonso.explorersaga.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.alonso.explorersaga.R

// Playfair Display para títulos (carácter editorial) y Montserrat para el texto
val playfairDisplayFamily = FontFamily(
    Font(R.font.playfairdisplay_bold, FontWeight.Bold)
)

val montserratFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = playfairDisplayFamily, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = playfairDisplayFamily, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = playfairDisplayFamily, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = playfairDisplayFamily, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = playfairDisplayFamily, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = playfairDisplayFamily, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = montserratFamily, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = montserratFamily, fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.copy(fontFamily = montserratFamily, fontWeight = FontWeight.Bold),
    bodyLarge = base.bodyLarge.copy(fontFamily = montserratFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = montserratFamily),
    bodySmall = base.bodySmall.copy(fontFamily = montserratFamily),
    labelLarge = base.labelLarge.copy(fontFamily = montserratFamily, fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.copy(fontFamily = montserratFamily),
    labelSmall = base.labelSmall.copy(fontFamily = montserratFamily)
)
