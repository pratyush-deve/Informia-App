package com.pratyush.infoapp.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "vault_cards")
data class VaultCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val type: String,
    val iconKey: String,
    val imageUri: String? = null,
    val gradientStart: Long,
    val gradientEnd: Long,
    val position: Int = 0
)

@Entity(tableName = "vault_fields")
data class VaultFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardId: Long,
    val label: String,
    val value: String,
    val fieldType: String,
    val position: Int = 0
)

data class CardWithFields(
    @Embedded val card: VaultCardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId"
    )
    val fields: List<VaultFieldEntity>
)
