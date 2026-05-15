package com.pratyush.infoapp.ui.vault.editor

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.data.local.CardFieldType
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.ui.vault.CardEditorState
import com.pratyush.infoapp.ui.vault.EditableField
import com.pratyush.infoapp.ui.vault.components.CardMedia
import com.pratyush.infoapp.ui.vault.utils.toneFor
import com.pratyush.infoapp.ui.vault.utils.gradientOptions
import com.pratyush.infoapp.ui.vault.utils.iconOptions
import com.pratyush.infoapp.ui.vault.utils.createTempImageUri
import com.pratyush.infoapp.ui.vault.components.TinyProfilePreviewCard
import com.pratyush.infoapp.utils.decodeImageUriGroup
import com.pratyush.infoapp.utils.encodeImageUriGroup
import com.pratyush.infoapp.utils.isImageUriGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    navController: NavController,
    state: CardEditorState,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onGradientChange: (Long, Long) -> Unit,
    onImageChange: (String?) -> Unit,
    onAddField: () -> Unit,
    onRemoveField: (Long) -> Unit,
    onFieldLabelChange: (Long, String) -> Unit,
    onFieldValueChange: (Long, String) -> Unit,
    onFieldTypeChange: (Long, CardFieldType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showAddImageDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val selectedImageUris = remember(context, state.imageUri) {
        val attachment = state.imageUri
        val attachmentMimeType = attachment
            ?.takeUnless(::isImageUriGroup)
            ?.let { uriString ->
                runCatching { context.contentResolver.getType(Uri.parse(uriString)) }.getOrNull()
            }
            .orEmpty()
        when {
            attachment.isNullOrBlank() -> emptyList()
            isImageUriGroup(attachment) -> decodeImageUriGroup(attachment)
            attachmentMimeType.contains("pdf", ignoreCase = true) -> emptyList()
            attachment.endsWith(".pdf", ignoreCase = true) -> emptyList()
            else -> decodeImageUriGroup(attachment)
        }
    }

    fun appendImageUris(newUris: List<Uri>) {
        if (newUris.isEmpty()) return
        val newUriStrings = newUris.map { it.toString() }
        onImageChange(
            if (state.type == CardType.PROFILE) {
                newUriStrings.firstOrNull()
            } else {
                encodeImageUriGroup(selectedImageUris + newUriStrings)
            }
        )
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        appendImageUris(uris)
    }
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        onImageChange(uri?.toString())
    }
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        onImageChange(uri?.toString())
    }
    val cameraPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            if (state.type == CardType.PROFILE) {
                onImageChange(pendingCameraUri.toString())
            } else {
                appendImageUris(listOf(pendingCameraUri) as List<Uri>)
            }
        }
        if (!success) pendingCameraUri = null
    }

    val previewCard = remember(state.imageUri, state.type, state.title, state.iconKey) {
        VaultCard(
            id = state.id,
            title = state.title.ifBlank { "Preview" },
            type = state.type,
            iconKey = state.iconKey,
            imageUri = state.imageUri,
            gradientStart = state.gradientStart,
            gradientEnd = state.gradientEnd,
            fields = emptyList()
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.id == 0L) "Create Card" else "Edit Card",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Update fields, colors, icon, and preview content",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (state.isSaving) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Saving...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        FilledIconButton(
                            onClick = onSave,
                            enabled = !state.isSaving
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = "Save card")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Card Title") },
                singleLine = true
            )

            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                iconOptions.forEach { (key, icon) ->
                    FilterChip(
                        selected = state.iconKey == key,
                        onClick = { onIconChange(key) },
                        label = {
                            Text(key.replaceFirstChar { it.titlecase() })
                        },
                        leadingIcon = {
                            Icon(icon, contentDescription = null)
                        }
                    )
                }
            }

            Text(
                text = "Card Colors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                gradientOptions.forEach { option ->
                    val selected = state.gradientStart == option.start && state.gradientEnd == option.end
                    Box(
                        modifier = Modifier
                            .size(width = 72.dp, height = 42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(option.start), Color(option.end))
                                )
                            )
                            .clickable { onGradientChange(option.start, option.end) }
                    ) {
                        if (selected) {
                            Text(
                                text = "Selected",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Preview / Attachments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (state.type != CardType.PROFILE) {
                    IconButton(onClick = { showAddImageDialog = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add images")
                    }
                }
            }
            AttachmentChooserButton(
                isProfile = state.type == CardType.PROFILE,
                hasPreview = state.imageUri != null,
                onChoose = { showAttachmentOptions = true },
                onRemove = { onImageChange(null) }
            )
            CardMedia(
                card = previewCard,
                expanded = true,
                editablePreview = true,
                onAddImages = if (state.type == CardType.PROFILE) null else {
                    { showAddImageDialog = true }
                },
                onRemoveImage = { uriString ->
                    onImageChange(encodeImageUriGroup(selectedImageUris.filterNot { it == uriString }))
                },
                tone = toneFor(previewCard.gradientStart, previewCard.gradientEnd)
            )

            Text(
                text = "Fields",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            state.fields.forEach { field ->
                EditableFieldCard(
                    field = field,
                    onLabelChange = { onFieldLabelChange(field.id, it) },
                    onValueChange = { onFieldValueChange(field.id, it) },
                    onTypeChange = { onFieldTypeChange(field.id, it) },
                    onRemove = { onRemoveField(field.id) }
                )
            }

            OutlinedButton(
                onClick = onAddField,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Field")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAttachmentOptions) {
        AttachmentOptionsSheet(
            isVisible = showAttachmentOptions,
            onDismiss = { showAttachmentOptions = false },
            onOptionSelected = { option ->
                showAttachmentOptions = false
                when (option) {
                    AttachmentOption.GALLERY -> {
                        imagePicker.launch("image/*")
                    }
                    AttachmentOption.CAMERA -> {
                        val cameraUri = createTempImageUri(context)
                        pendingCameraUri = cameraUri
                        cameraPicker.launch(cameraUri)
                    }
                    AttachmentOption.FILES -> {
                        attachmentPicker.launch(
                            if (state.type == CardType.PROFILE) {
                                arrayOf("image/*")
                            } else {
                                arrayOf("image/*", "application/pdf")
                            }
                        )
                    }
                }
            },
            isProfile = state.type == CardType.PROFILE,
            hasImages = selectedImageUris.isNotEmpty()
        )
    }

    if (showAddImageDialog) {
        AddImageDialog(
            isVisible = showAddImageDialog,
            onDismiss = { showAddImageDialog = false },
            onGallerySelected = {
                showAddImageDialog = false
                imagePicker.launch("image/*")
            },
            onCameraSelected = {
                showAddImageDialog = false
                val cameraUri = createTempImageUri(context)
                pendingCameraUri = cameraUri
                cameraPicker.launch(cameraUri)
            }
        )
    }
}
