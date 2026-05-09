package com.pratyush.infoapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Transaction
    @Query("SELECT * FROM vault_cards ORDER BY position ASC, id ASC")
    fun observeCards(): Flow<List<CardWithFields>>

    @Transaction
    @Query("SELECT * FROM vault_cards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): CardWithFields?

    @Query("SELECT COUNT(*) FROM vault_cards")
    suspend fun cardCount(): Int

    @Insert
    suspend fun insertCard(card: VaultCardEntity): Long

    @Update
    suspend fun updateCard(card: VaultCardEntity)

    @Insert
    suspend fun insertFields(fields: List<VaultFieldEntity>)

    @Query("DELETE FROM vault_fields WHERE cardId = :cardId")
    suspend fun deleteFieldsForCard(cardId: Long)

    @Query("DELETE FROM vault_cards WHERE id = :cardId")
    suspend fun deleteCard(cardId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) FROM vault_cards")
    suspend fun lastCardPosition(): Int
}
