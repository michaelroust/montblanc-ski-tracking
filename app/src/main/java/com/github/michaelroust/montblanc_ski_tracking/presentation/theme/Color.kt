package com.github.michaelroust.montblanc_ski_tracking.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Red400 = Color(0xFFCF6679)

// Colors that are used in the Montblanc fitness app
val GreenMontblanc = Color(0xFFA2CD34)
val BlueMontblanc = Color(0xFF38B2E3)
val OrangeMontblanc = Color(0xFFE8B003)
val RedMontblanc = Color(0xFFA80D16)

// Colors variants from the ones that are used in the Montblanc fitness app
val GreenVariantMontblanc = Color(0xFFA2CFA5)
val BlueVariantMontblanc = Color(0xFF38B2C4)
val OrangeVariantMontblanc = Color(0xFFE8B074)
val RedVariantMontblanc = Color(0xFFA85A11)

internal val wearColorPalette: Colors = Colors(
    primary = OrangeMontblanc,
    primaryVariant = OrangeVariantMontblanc,
    secondary = BlueMontblanc,
    secondaryVariant = BlueVariantMontblanc,
    error = Red400,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.Black
)