package com.pratyush.infoapp.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pratyush.infoapp.data.VaultRepository
import com.pratyush.infoapp.data.local.CardFieldType
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.data.local.VaultField
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
    val cards: List<VaultCard> = emptyList()
)

data class EditableField(
    val id: Long = System.nanoTime(),
    val label: String = "",
    val value: String = "",
    val fieldType: CardFieldType = CardFieldType.TEXT
)

data class CardEditorState(
    val id: Long = 0L,
    val title: String = "",
    val type: CardType = CardType.CUSTOM,
    val iconKey: String = "document",
    val originalImageUri: String? = null,
    val imageUri: String? = null,
    val gradientStart: Long = 0xFF355C7D,
    val gradientEnd: Long = 0xFF6C5B7B,
    val position: Int = 0,
    val fields: List<EditableField> = listOf(EditableField()),
    val isSaving: Boolean = false
)

class VaultViewModel(
    private val repository: VaultRepository
) : ViewModel() {

    val uiState: StateFlow<VaultUiState> = repository.observeCards()
        .map { VaultUiState(cards = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultUiState()
        )

    private val _editorState = MutableStateFlow<CardEditorState?>(null)
    val editorState: StateFlow<CardEditorState?> = _editorState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    fun startCreateCard() {
        _editorState.value = CardEditorState(
            title = "New Card",
            type = CardType.CUSTOM,
            iconKey = "document",
            gradientStart = 0xFF3A1C71,
            gradientEnd = 0xFFD76D77,
            fields = listOf(
                EditableField(label = "Label", value = ""),
                EditableField(label = "Value", value = "")
            )
        )
    }

    fun startEditing(card: VaultCard) {
        _editorState.value = CardEditorState(
            id = card.id,
            title = card.title,
            type = card.type,
            iconKey = card.iconKey,
            originalImageUri = card.imageUri,
            imageUri = card.imageUri,
            gradientStart = card.gradientStart,
            gradientEnd = card.gradientEnd,
            position = card.position,
            fields = card.fields.map { field ->
                EditableField(
                    id = field.id.takeIf { it != 0L } ?: System.nanoTime(),
                    label = field.label,
                    value = field.value,
                    fieldType = field.fieldType
                )
            }.ifEmpty { listOf(EditableField()) }
        )
    }

    fun startEditingById(cardId: Long) {
        viewModelScope.launch {
            val card = repository.getCardById(cardId)
            if (card != null) {
                startEditing(card)
            }
        }
    }

    fun dismissEditor() {
        _editorState.value = null
    }

    fun updateTitle(title: String) = updateEditor { copy(title = title) }

    fun updateIcon(iconKey: String) = updateEditor { copy(iconKey = iconKey) }

    fun updateGradient(start: Long, end: Long) = updateEditor {
        copy(gradientStart = start, gradientEnd = end)
    }

    fun updateImage(uri: String?) = updateEditor { copy(imageUri = uri) }

    fun updateFieldLabel(fieldId: Long, label: String) = updateField(fieldId) { copy(label = label) }

    fun updateFieldValue(fieldId: Long, value: String) = updateField(fieldId) { copy(value = value) }

    fun updateFieldType(fieldId: Long, fieldType: CardFieldType) = updateField(fieldId) { copy(fieldType = fieldType) }

    fun addField() = updateEditor {
        copy(fields = fields + EditableField())
    }

    fun removeField(fieldId: Long) = updateEditor {
        val updated = fields.filterNot { it.id == fieldId }.ifEmpty { listOf(EditableField()) }
        copy(fields = updated)
    }

    fun saveEditor(onSuccess: () -> Unit = {}) {
        val editor = _editorState.value ?: return
        if (editor.isSaving) return // Prevent duplicate saves
        
        _editorState.value = editor.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                repository.saveCard(
                    VaultCard(
                        id = editor.id,
                        title = editor.title.ifBlank { defaultTitle(editor.type) },
                        type = editor.type,
                        iconKey = editor.iconKey,
                        imageUri = editor.imageUri,
                        gradientStart = editor.gradientStart,
                        gradientEnd = editor.gradientEnd,
                        position = editor.position,
                        fields = editor.fields
                            .filter { it.label.isNotBlank() || it.value.isNotBlank() }
                            .mapIndexed { index, field ->
                                VaultField(
                                    label = field.label.ifBlank { "Field ${index + 1}" },
                                    value = field.value,
                                    fieldType = field.fieldType,
                                    position = index
                                )
                            },
                        previousImageUri = editor.originalImageUri
                    )
                )
                // Keep saving state visible for smooth transition
                delay(800)
                // Navigate back FIRST while keeping screen alive
                onSuccess()
                // Small extra delay so NavHost transition settles
                delay(100)
                // THEN clear editor state
                _editorState.value = null
            } catch (e: Exception) {
                // Reset saving state on error
                _editorState.value = editor.copy(isSaving = false)
            }
        }
    }

    fun deleteCard(cardId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
            onSuccess()
        }
    }

    private fun updateEditor(transform: CardEditorState.() -> CardEditorState) {
        _editorState.value = _editorState.value?.transform()
    }

    private fun updateField(fieldId: Long, transform: EditableField.() -> EditableField) {
        updateEditor {
            copy(
                fields = fields.map { field ->
                    if (field.id == fieldId) field.transform() else field
                }
            )
        }
    }

    private fun defaultTitle(type: CardType): String {
        return when (type) {
            CardType.PROFILE -> "Profile"
            CardType.CONTACT -> "Contact"
            CardType.ADDRESS -> "Address"
            CardType.VEHICLE -> "Vehicle Card"
            CardType.DOCUMENT -> "Document Card"
            CardType.CERTIFICATE -> "Certificate Card"
            CardType.CUSTOM -> "Custom Card"
        }
    }

    companion object {
        fun factory(repository: VaultRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VaultViewModel(repository) as T
                }
            }
        }
    }
}
