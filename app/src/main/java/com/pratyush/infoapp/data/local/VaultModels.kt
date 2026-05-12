package com.pratyush.infoapp.data.local

enum class CardType {
    PROFILE,
    CONTACT,
    ADDRESS,
    VEHICLE,
    DOCUMENT,
    CERTIFICATE,
    CUSTOM
}

enum class CardFieldType {
    TEXT,
    PHONE,
    EMAIL,
    LINK,
    MULTILINE
}

data class VaultField(
    val id: Long = 0L,
    val label: String,
    val value: String,
    val fieldType: CardFieldType = CardFieldType.TEXT,
    val position: Int = 0
)

data class VaultCard(
    val id: Long = 0L,
    val title: String,
    val type: CardType,
    val iconKey: String,
    val previousImageUri: String? = null,
    val imageUri: String? = null,
    val gradientStart: Long,
    val gradientEnd: Long,
    val position: Int = 0,
    val fields: List<VaultField> = emptyList()
)
