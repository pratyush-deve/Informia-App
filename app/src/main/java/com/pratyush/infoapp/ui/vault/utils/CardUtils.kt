package com.pratyush.infoapp.ui.vault.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkspacePremium
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.data.local.VaultCard

/**
 * Data class representing color tones for card styling.
 */
data class CardTone(
    val content: Color,
    val secondary: Color,
    val tertiary: Color,
    val fieldContainer: Color,
    val mediaLabelBackground: Color
)

/**
 * Data class representing gradient options for cards.
 */
data class GradientOption(val start: Long, val end: Long)

/**
 * Available gradient options for card backgrounds.
 */
val gradientOptions = listOf(
    GradientOption(0xFF355C7D, 0xFF6C5B7B),
    GradientOption(0xFF134E5E, 0xFF71B280),
    GradientOption(0xFF42275A, 0xFF734B6D),
    GradientOption(0xFF0F2027, 0xFF2C5364),
    GradientOption(0xFF1D4350, 0xFFA43931),
    GradientOption(0xFF3A1C71, 0xFFD76D77),
    GradientOption(0xFF5B86E5, 0xFF36D1DC),
    GradientOption(0xFF667EEA, 0xFF764BA2),
    GradientOption(0xFF00C9A7, 0xFF92FE9D),
    GradientOption(0xFF7F7FD5, 0xFF86A8E7),
    GradientOption(0xFFF857A6, 0xFFFF5858),
    GradientOption(0xFFFF9A9E, 0xFFFAD0C4),
    GradientOption(0xFFA1C4FD, 0xFFC2E9FB),
    GradientOption(0xFFD4FC79, 0xFF96E6A1),
    GradientOption(0xFFFBC2EB, 0xFFA6C1EE),
    GradientOption(0xFFFFE29F, 0xFFFFA99F),
    GradientOption(0xFFB8C6DB, 0xFFF5F7FA)
)

/**
 * Available icon options for cards.
 */
val iconOptions = listOf(
    "person" to Icons.Outlined.Person,
    "contact" to Icons.Outlined.ContactPhone,
    "home" to Icons.Outlined.Home,
    "vehicle" to Icons.Outlined.DirectionsCar,
    "document" to Icons.Outlined.Description,
    "certificate" to Icons.Outlined.WorkspacePremium,
    "badge" to Icons.Outlined.Badge,
    "article" to Icons.AutoMirrored.Outlined.Article
)

/**
 * Determines the appropriate color tone for a card based on its gradient colors.
 */
fun toneFor(start: Long, end: Long): CardTone {
    val startColor = Color(start)
    val endColor = Color(end)
    val averageLuminance = (startColor.luminance() + endColor.luminance()) / 2f
    val isLight = averageLuminance > 0.52f
    return if (isLight) {
        CardTone(
            content = Color(0xFF111827),
            secondary = Color(0xCC111827),
            tertiary = Color(0x99111827),
            fieldContainer = Color.White.copy(alpha = 0.34f),
            mediaLabelBackground = Color.White.copy(alpha = 0.42f)
        )
    } else {
        CardTone(
            content = Color.White,
            secondary = Color.White.copy(alpha = 0.72f),
            tertiary = Color.White.copy(alpha = 0.58f),
            fieldContainer = Color.Black.copy(alpha = 0.14f),
            mediaLabelBackground = Color.Black.copy(alpha = 0.34f)
        )
    }
}

/**
 * Gets the appropriate icon for a given icon key.
 */
fun iconFor(key: String): ImageVector {
    return iconOptions.firstOrNull { it.first == key }?.second ?: Icons.Outlined.Description
}

/**
 * Determines if a card can be shared.
 */
fun canShare(card: VaultCard): Boolean {
    val shareableKinds = setOf(CardType.DOCUMENT, CardType.CERTIFICATE)
    val shareableIcons = setOf("document", "certificate", "article", "badge")
    return (card.type in shareableKinds || card.iconKey in shareableIcons) &&
        (card.imageUri != null || card.fields.any { it.value.isNotBlank() })
}
