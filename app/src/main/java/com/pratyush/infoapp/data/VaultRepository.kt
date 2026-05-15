package com.pratyush.infoapp.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.pratyush.infoapp.data.local.CardFieldType
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.data.local.VaultCardEntity
import com.pratyush.infoapp.data.local.VaultDao
import com.pratyush.infoapp.data.local.VaultField
import com.pratyush.infoapp.data.local.VaultFieldEntity
import com.pratyush.infoapp.utils.decodeImageUriGroup
import com.pratyush.infoapp.utils.encodeImageUriGroup
import com.pratyush.infoapp.utils.isImageUriGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

class VaultRepository(
    private val dao: VaultDao,
    private val context: Context
) {
    private val attachmentDirectory by lazy {
        File(context.filesDir, "vault_attachments").apply { mkdirs() }
    }

    fun observeCards(): Flow<List<VaultCard>> {
        return dao.observeCards().map { cards ->
            cards.map { relation ->
                VaultCard(
                    id = relation.card.id,
                    title = relation.card.title,
                    type = enumValueOf(relation.card.type),
                    iconKey = relation.card.iconKey,
                    previousImageUri = relation.card.imageUri,
                    imageUri = relation.card.imageUri,
                    gradientStart = relation.card.gradientStart,
                    gradientEnd = relation.card.gradientEnd,
                    position = relation.card.position,
                    fields = relation.fields
                        .sortedBy { it.position }
                        .map { field ->
                            VaultField(
                                id = field.id,
                                label = field.label,
                                value = field.value,
                                fieldType = enumValueOf(field.fieldType),
                                position = field.position
                            )
                        }
                )
            }
        }
    }

    suspend fun seedIfEmpty() {
        if (dao.cardCount() > 0) return

        sampleCards().forEachIndexed { index, card ->
            saveCard(card.copy(position = index))
        }
    }

    suspend fun getCardById(cardId: Long): VaultCard? {
        val relation = dao.getCardById(cardId) ?: return null
        return VaultCard(
            id = relation.card.id,
            title = relation.card.title,
            type = enumValueOf(relation.card.type),
            iconKey = relation.card.iconKey,
            previousImageUri = relation.card.imageUri,
            imageUri = relation.card.imageUri,
            gradientStart = relation.card.gradientStart,
            gradientEnd = relation.card.gradientEnd,
            position = relation.card.position,
            fields = relation.fields
                .sortedBy { it.position }
                .map { field ->
                    VaultField(
                        id = field.id,
                        label = field.label,
                        value = field.value,
                        fieldType = enumValueOf(field.fieldType),
                        position = field.position
                    )
                }
        )
    }

    suspend fun saveCard(card: VaultCard) {
        val storedAttachment = persistAttachment(
            sourceUri = card.imageUri,
            previousUri = card.previousImageUri
        )
        val position = if (card.id == 0L) dao.lastCardPosition() + 1 else card.position
        val entity = VaultCardEntity(
            id = card.id,
            title = card.title,
            type = card.type.name,
            iconKey = card.iconKey,
            imageUri = storedAttachment,
            gradientStart = card.gradientStart,
            gradientEnd = card.gradientEnd,
            position = position
        )
        val cardId = if (card.id == 0L) {
            dao.insertCard(entity)
        } else {
            dao.updateCard(entity)
            card.id
        }

        dao.deleteFieldsForCard(cardId)
        dao.insertFields(
            card.fields.mapIndexed { index, field ->
                VaultFieldEntity(
                    cardId = cardId,
                    label = field.label,
                    value = field.value,
                    fieldType = field.fieldType.name,
                    position = index
                )
            }
        )
    }

    suspend fun deleteCard(cardId: Long) {
        val card = dao.getCardById(cardId)
        if (card != null) {
            deleteManagedAttachments(card.card.imageUri)
        }
        dao.deleteFieldsForCard(cardId)
        dao.deleteCard(cardId)
    }

    private fun persistAttachment(
        sourceUri: String?,
        previousUri: String?
    ): String? {
        if (sourceUri.isNullOrBlank()) {
            deleteManagedAttachments(previousUri)
            return null
        }

        if (isImageUriGroup(sourceUri)) {
            val persistedUris = decodeImageUriGroup(sourceUri).mapNotNull { uri ->
                persistSingleAttachment(uri, previousUri = null)
            }
            deleteManagedAttachmentsExcept(previousUri, persistedUris.toSet())
            return encodeImageUriGroup(persistedUris)
        }

        deleteManagedAttachmentsExcept(previousUri, setOf(sourceUri))
        return persistSingleAttachment(sourceUri, previousUri)
    }

    private fun persistSingleAttachment(
        sourceUri: String,
        previousUri: String?
    ): String? {
        if (isManagedAttachment(sourceUri)) {
            return sourceUri
        }

        val source = Uri.parse(sourceUri)
        val extension = resolveExtension(source)
        val targetFile = File(attachmentDirectory, "${UUID.randomUUID()}.$extension")

        context.contentResolver.openInputStream(source)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return previousUri

        deleteManagedAttachments(previousUri)
        return Uri.fromFile(targetFile).toString()
    }

    private fun resolveExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        val mimeExtension = mimeType?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
        if (!mimeExtension.isNullOrBlank()) return mimeExtension

        val path = uri.lastPathSegment.orEmpty()
        val dotIndex = path.lastIndexOf('.')
        return if (dotIndex != -1 && dotIndex < path.lastIndex) {
            path.substring(dotIndex + 1)
        } else {
            "bin"
        }
    }

    private fun isManagedAttachment(uriString: String): Boolean {
        val uri = Uri.parse(uriString)
        val path = uri.path ?: return false
        return path.startsWith(attachmentDirectory.absolutePath)
    }

    private fun deleteIfManaged(uriString: String?) {
        if (uriString.isNullOrBlank() || !isManagedAttachment(uriString)) return
        runCatching {
            File(Uri.parse(uriString).path.orEmpty()).delete()
        }
    }

    private fun deleteManagedAttachments(uriString: String?) {
        decodeImageUriGroup(uriString).forEach { uri ->
            deleteIfManaged(uri)
        }
    }

    private fun deleteManagedAttachmentsExcept(uriString: String?, retainedUris: Set<String>) {
        decodeImageUriGroup(uriString)
            .filterNot { it in retainedUris }
            .forEach { uri ->
                deleteIfManaged(uri)
            }
    }

    private fun sampleCards(): List<VaultCard> {
        return listOf(
            VaultCard(
                title = "Identity",
                type = CardType.PROFILE,
                iconKey = "person",
                gradientStart = 0xFF355C7D,
                gradientEnd = 0xFF6C5B7B,
                fields = listOf(
                    VaultField(label = "Full Name", value = "")
                )
            ),
            VaultCard(
                title = "Connect",
                type = CardType.CONTACT,
                iconKey = "contact",
                gradientStart = 0xFF134E5E,
                gradientEnd = 0xFF71B280,
                fields = listOf(
                    VaultField(label = "Phone Number", value = "", fieldType = CardFieldType.PHONE),
                    VaultField(label = "Email Address", value = "", fieldType = CardFieldType.EMAIL),
                    VaultField(label = "Designation", value = ""),
                    VaultField(label = "Company / Institute", value = "")
                )
            )
        )
    }
}
