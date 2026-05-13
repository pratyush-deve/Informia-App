package com.pratyush.infoapp.ui.vault

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.data.local.CardFieldType
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.data.local.VaultField
import com.pratyush.infoapp.ui.theme.InfoAppTheme
import com.pratyush.infoapp.ui.vault.components.VaultCardItem
import com.pratyush.infoapp.ui.vault.components.HeroSection
import com.pratyush.infoapp.ui.vault.preview.PreviewViewerDialog
import com.pratyush.infoapp.ui.vault.editor.AttachmentOption
import com.pratyush.infoapp.ui.components.ActionDialog
import com.pratyush.infoapp.ui.components.ConfirmationDialog
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.gradientOptions
import com.pratyush.infoapp.ui.vault.utils.iconOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class DialogState {
    NONE,
    ACTIONS,
    DELETE_CONFIRMATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    uiState: VaultUiState,
    onCreateCard: () -> Unit,
    onEditCard: (VaultCard) -> Unit,
    onDeleteCard: (VaultCard) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var viewerCard by remember { mutableStateOf<VaultCard?>(null) }
    var dialogState by remember { mutableStateOf(DialogState.NONE) }
    var selectedCard by remember { mutableStateOf<VaultCard?>(null) }
    val listBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090B13),
            Color(0xFF101826),
            Color(0xFF0B101A)
        )
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF101826).copy(alpha = 0.98f),
                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Column {
                            Text("Informia")
                            Text(
                                text = "Private cards for identity, contacts, documents, and more",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Open settings",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateCard,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add custom card")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(listBackground)
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    HeroSection(cardCount = uiState.cards.size)
                }
                items(
                    items = uiState.cards,
                    key = { it.id }
                ) { card ->
                    VaultCardItem(
                        card = card,
                        onEditCard = {
                            selectedCard = card
                            dialogState = DialogState.ACTIONS
                        },
                        onFieldCopied = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard")
                            }
                        },
                        onOpenPreview = {
                            viewerCard = card
                        }
                    )
                }
            }
        }
    }

    if (viewerCard != null) {
        val imageUri = viewerCard?.imageUri

        if (!imageUri.isNullOrEmpty()) {
            PreviewViewerDialog(
                uriString = imageUri,
                onDismiss = { viewerCard = null }
            )
        } else {
            // If there's no image/file attached, reset the state
            viewerCard = null
        }
    }

    when (dialogState) {
        DialogState.ACTIONS -> {
            selectedCard?.let { card ->
                ActionDialog(
                    onDismiss = {
                        dialogState = DialogState.NONE
                        selectedCard = null
                    },
                    onEdit = {
                        dialogState = DialogState.NONE
                        selectedCard = null
                        onEditCard(card)
                    },
                    onDelete = {
                        dialogState = DialogState.DELETE_CONFIRMATION
                    }
                )
            }
        }
        DialogState.DELETE_CONFIRMATION -> {
            selectedCard?.let { card ->
                ConfirmationDialog(
                    message = "Are you sure you want to delete this card?",
                    onDismiss = {
                        dialogState = DialogState.NONE
                        selectedCard = null
                    },
                    onConfirm = {
                        onDeleteCard(card)
                        dialogState = DialogState.NONE
                        selectedCard = null
                    }
                )
            }
        }
        DialogState.NONE -> {}
    }
}

@Composable
private fun HeroSection(cardCount: Int) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Your identity, organized beautifully",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Store personal details, addresses, vehicles, documents, and custom records as editable smart cards.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("$cardCount cards") }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Tap to Copy") }
                )
            }
        }
    }
}

@Composable
private fun VaultCardItem(
    card: VaultCard,
    onEditCard: () -> Unit,
    onFieldCopied: () -> Unit,
    onOpenPreview: () -> Unit
) {
    val context = LocalContext.current
    val tone = remember(card.gradientStart, card.gradientEnd) {
        toneFor(card.gradientStart, card.gradientEnd)
    }
    var expanded by rememberSaveable(card.id) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "cardExpandArrow"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { expanded = !expanded }),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(card.gradientStart), Color(card.gradientEnd))
                    )
                )
                .padding(18.dp)
                .animateContentSize()
        ) {
            if (card.type == CardType.PROFILE) {
                ProfileCardHeader(
                    card = card,
                    expanded = expanded,
                    onEditCard = onEditCard,
                    tone = tone
                )
            } else {
                StandardCardHeader(
                    card = card,
                    expanded = expanded,
                    rotation = rotation,
                    onEditCard = onEditCard,
                    onShare = { shareCard(context, card) },
                    tone = tone
                )
                CardMedia(
                    card = card,
                    expanded = expanded,
                    editablePreview = false,
                    onOpenPreview = onOpenPreview,
                    tone = tone
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    card.fields.forEach { field ->
                        FieldRow(
                            field = field,
                            onFieldCopied = onFieldCopied,
                            tone = tone
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandardCardHeader(
    card: VaultCard,
    expanded: Boolean,
    rotation: Float,
    onEditCard: () -> Unit,
    onShare: () -> Unit,
    tone: CardTone
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        CardIconBubble(icon = iconFor(card.iconKey), size = 48.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = tone.content
            )
            Text(
                text = card.type.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodySmall,
                color = tone.secondary
            )
        }
        IconButton(onClick = onEditCard) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit card",
                tint = tone.content
            )
        }
        if (canShare(card)) {
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share card",
                    tint = tone.content
                )
            }
        }
        Icon(
            imageVector = if (expanded) Icons.Outlined.Remove else Icons.Outlined.Add,
            contentDescription = "Expand or collapse card",
            modifier = Modifier
                .padding(top = 8.dp)
                .rotate(rotation),
            tint = tone.content.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun ProfileCardHeader(
    card: VaultCard,
    expanded: Boolean,
    onEditCard: () -> Unit,
    tone: CardTone
) {
    val avatarSize by animateDpAsState(
        targetValue = if (expanded) 112.dp else 58.dp,
        animationSpec = tween(260),
        label = "profileAvatarSize"
    )
    val name = card.fields.firstOrNull()?.value?.ifBlank { null }
        ?: card.fields.firstOrNull()?.label
        ?: card.title

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEditCard) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit card",
                    tint = tone.content
                )
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(
                    imageUri = card.imageUri,
                    size = avatarSize
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = tone.content
                )
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = tone.secondary
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    imageUri = card.imageUri,
                    size = avatarSize
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = tone.content
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    imageUri: String?,
    size: androidx.compose.ui.unit.Dp
) {
    val uri = imageUri?.toUri()
    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = "Profile image",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(size * 0.46f),
                tint = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun CardIconBubble(
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun CardMedia(
    card: VaultCard,
    expanded: Boolean,
    editablePreview: Boolean,
    onOpenPreview: (() -> Unit)? = null,
    tone: CardTone
) {
    val context = LocalContext.current
    val uri = card.imageUri?.toUri()
    val fileType = remember(uri) { uri?.let { getFileType(context, it) } ?: FileType.UNKNOWN }

    when {
        card.type == CardType.PROFILE && editablePreview -> {
            Spacer(modifier = Modifier.height(10.dp))
            TinyProfilePreviewCard(card = card)
            Spacer(modifier = Modifier.height(6.dp))
        }

        card.type == CardType.PROFILE -> Unit
        uri != null -> {
            Spacer(modifier = Modifier.height(if (editablePreview) 10.dp else 16.dp))
            if (!expanded && !editablePreview) {
                CollapsedPreviewButton(
                    icon = previewIconFor(fileType),
                    label = fileType.chipLabel,
                    tone = tone,
                    onClick = onOpenPreview
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onOpenPreview != null) {
                            onOpenPreview?.invoke()
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.18f)
                    )
                ) {
                    when (fileType) {
                        FileType.IMAGE -> {
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "${card.title} preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            if (editablePreview) {
                                                78.dp
                                            } else if (expanded) {
                                                220.dp
                                            } else {
                                                132.dp
                                            }
                                        ),
                                    contentScale = if (editablePreview) {
                                        ContentScale.Crop
                                    } else {
                                        ContentScale.Fit
                                    }
                                )
                                if (!editablePreview) {
                                    Text(
                                        text = "Preview",
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp)
                                            .background(
                                                tone.mediaLabelBackground,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = tone.content,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        FileType.PDF -> {
                            FileIconRow(
                                icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                                label = if (editablePreview) "Selected PDF" else "PDF attached",
                                subtitle = if (editablePreview) "" else "Tap to view",
                                tone = tone
                            )
                        }

                        FileType.VIDEO -> {
                            FileIconRow(
                                icon = Icons.Outlined.Slideshow,
                                label = if (editablePreview) "Selected Video" else "Video attached",
                                subtitle = if (editablePreview) "" else "Tap to play",
                                tone = tone
                            )
                        }

                        FileType.AUDIO -> {
                            FileIconRow(
                                icon = Icons.Outlined.Description,
                                label = if (editablePreview) "Selected Audio" else "Audio attached",
                                subtitle = if (editablePreview) "" else "Tap to play",
                                tone = tone
                            )
                        }

                        FileType.TEXT -> {
                            FileIconRow(
                                icon = Icons.Outlined.Notes,
                                label = if (editablePreview) "Selected Text" else "Text file attached",
                                subtitle = if (editablePreview) "" else "Tap to read",
                                tone = tone
                            )
                        }

                        FileType.UNKNOWN -> {
                            FileIconRow(
                                icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                                label = if (editablePreview) "Selected File" else "File attached",
                                subtitle = if (editablePreview) "" else "Tap to open",
                                tone = tone
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(if (editablePreview) 6.dp else 14.dp))
        }
    }
}

@Composable
private fun CollapsedPreviewButton(
    icon: ImageVector,
    label: String,
    tone: CardTone,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tone.mediaLabelBackground, RoundedCornerShape(14.dp))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = tone.content.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = tone.content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun previewIconFor(fileType: FileType): ImageVector {
    return when (fileType) {
        FileType.IMAGE -> Icons.Outlined.Image
        FileType.VIDEO -> Icons.Outlined.Slideshow
        FileType.AUDIO -> Icons.Outlined.Description
        FileType.PDF,
        FileType.UNKNOWN -> Icons.AutoMirrored.Outlined.InsertDriveFile
        FileType.TEXT -> Icons.Outlined.Notes
    }
}

@Composable
private fun FileIconRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    tone: CardTone
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tone.content
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = tone.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tone.secondary
                )
            }
        }
    }
}

@Composable
private fun TinyProfilePreviewCard(card: VaultCard) {
    val tone = remember(card.gradientStart, card.gradientEnd) {
        toneFor(card.gradientStart, card.gradientEnd)
    }
    val name = card.fields.firstOrNull()?.value?.ifBlank { null }
        ?: card.fields.firstOrNull()?.label
        ?: "Profile"

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.38f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                imageUri = card.imageUri,
                size = 42.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = tone.content
                )
                Text(
                    text = if (card.imageUri == null) "Avatar placeholder active" else "Selected profile image",
                    style = MaterialTheme.typography.bodySmall,
                    color = tone.secondary
                )
            }
        }
    }
}

@Composable
private fun FieldRow(
    field: VaultField,
    onFieldCopied: () -> Unit,
    tone: CardTone
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var copyJob by remember(field.id, field.value) { mutableStateOf<Job?>(null) }

    Surface(
        color = tone.fieldContainer,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInteropFilter { event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            copyJob?.cancel()
                            copyJob = scope.launch {
                                delay(3_000L)
                                if (field.value.isNotBlank()) {
                                    copyValue(clipboard as ClipboardManager, field.value)
                                    onFieldCopied()
                                }
                            }
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (event.historySize > 0) {
                                val dx = kotlin.math.abs(event.x - event.getHistoricalX(0))
                                val dy = kotlin.math.abs(event.y - event.getHistoricalY(0))
                                if (dx > 24f || dy > 24f) {
                                    copyJob?.cancel()
                                    copyJob = null
                                }
                            }
                        }

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            copyJob?.cancel()
                            copyJob = null
                        }
                    }
                    false
                }
                .combinedClickable(onClick = {})
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = tone.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = field.value.ifBlank { "No value" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = tone.content
                )
            }
            if (field.fieldType == CardFieldType.EMAIL || field.fieldType == CardFieldType.PHONE) {
                IconButton(onClick = { openValue(context, field) }) {
                    Icon(
                        imageVector = if (field.fieldType == CardFieldType.EMAIL) {
                            Icons.Outlined.Email
                        } else {
                            Icons.Outlined.Phone
                        },
                        contentDescription = "Open value",
                        tint = tone.content
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardEditorSheet(
    state: CardEditorState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onGradientChange: (Long, Long) -> Unit,
    onImageChange: (String?) -> Unit,
    onAddField: () -> Unit,
    onRemoveField: (Long) -> Unit,
    onFieldLabelChange: (Long, String) -> Unit,
    onFieldValueChange: (Long, String) -> Unit,
    onFieldTypeChange: (Long, CardFieldType) -> Unit
) {
    val context = LocalContext.current
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onImageChange(uri?.toString())
    }
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
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
        onImageChange(if (success) pendingCameraUri?.toString() else null)
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                FilledIconButton(onClick = onSave) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Save card")
                }
            }

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

            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
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
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Field")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAttachmentOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentOptions = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Attachment Source",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pick the source that feels right for this card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AttachmentOptionCard(
                    option = AttachmentOption.GALLERY,
                    onClick = {
                        showAttachmentOptions = false
                        if (state.type == CardType.PROFILE) {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            galleryPicker.launch("image/*")
                        }
                    }
                )
                AttachmentOptionCard(
                    option = AttachmentOption.CAMERA,
                    onClick = {
                        showAttachmentOptions = false
                        val cameraUri = createTempImageUri(context)
                        pendingCameraUri = cameraUri
                        cameraPicker.launch(cameraUri)
                    }
                )
                AttachmentOptionCard(
                    option = AttachmentOption.FILES,
                    onClick = {
                        showAttachmentOptions = false
                        if (state.type == CardType.PROFILE) {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            galleryPicker.launch("image/*")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachmentChooserButton(
    isProfile: Boolean,
    hasPreview: Boolean,
    onChoose: () -> Unit,
    onRemove: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onChoose) {
            Icon(Icons.Outlined.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isProfile) "Choose Photo" else "Choose Preview")
        }
        if (hasPreview) {
            OutlinedButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun AttachmentOptionCard(
    option: AttachmentOption,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardIconBubble(icon = option.icon, size = 48.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EditableFieldCard(
    field: EditableField,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onTypeChange: (CardFieldType) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Field",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
            OutlinedTextField(
                value = field.label,
                onValueChange = onLabelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Label") },
                singleLine = true
            )
            OutlinedTextField(
                value = field.value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Value") },
                keyboardOptions = keyboardOptionsFor(field.fieldType),
                minLines = if (field.fieldType == CardFieldType.MULTILINE) 3 else 1
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardFieldType.entries.forEach { type ->
                    FilterChip(
                        selected = field.fieldType == type,
                        onClick = { onTypeChange(type) },
                        label = {
                            Text(type.name.lowercase().replaceFirstChar { it.titlecase() })
                        }
                    )
                }
            }
        }
    }
}

fun openFileExternally(context: Context, uri: Uri, mimeType: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Categorizes files for the preview system.
 */
enum class FileType(val label: String, val chipLabel: String) {
    IMAGE("Image viewer", "Image preview"),
    VIDEO("Video player", "Video preview"),
    AUDIO("Audio player", "Audio file"),
    PDF("Document viewer", "PDF preview"),
    TEXT("Text viewer", "Text preview"),
    UNKNOWN("File viewer", "Tap to open")
}

/**
 * Centralized file type detection.
 */
fun getFileType(context: Context, uri: Uri): FileType {
    val mimeType = context.contentResolver.getType(uri) ?: ""
    return when {
        mimeType.startsWith("image/") -> FileType.IMAGE
        mimeType.startsWith("video/") -> FileType.VIDEO
        mimeType.startsWith("audio/") -> FileType.AUDIO
        mimeType == "application/pdf" -> FileType.PDF
        mimeType.startsWith("text/") || mimeType == "application/json" || 
        mimeType in setOf("text/plain", "text/html", "text/css", "text/javascript", "text/xml") -> FileType.TEXT
        else -> FileType.UNKNOWN
    }
}

/**
 * Reusable external file opener.
 */
fun openFileExternally(context: Context, uri: Uri) {
    val mimeType = context.contentResolver.getType(uri)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        // Fallback or toast if needed
    }
}

@Composable
fun PreviewViewerDialog(
    uriString: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uri = remember(uriString) { Uri.parse(uriString) }
    val fileType = remember(uri) { getFileType(context, uri) }
    val mimeType = remember(uri) { context.contentResolver.getType(uri) ?: "" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "File Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fileType.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (fileType) {
                        FileType.IMAGE -> ImagePreview(uri)
                        FileType.PDF -> PdfPreview(uri)
                        FileType.VIDEO -> VideoPreview(uri)
                        FileType.AUDIO -> AudioPreview(uri)
                        FileType.TEXT -> TextPreview(uri)
                        FileType.UNKNOWN -> FallbackPreview(uri, mimeType)
                    }
                }

                // Footer Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { openFileExternally(context, uri, mimeType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open in other app")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(uri: Uri) {
    AsyncImage(
        model = uri,
        contentDescription = "Image Preview",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun PdfPreview(uri: Uri) {
    val context = LocalContext.current
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    val renderer = PdfRenderer(pfd)
                    val pageCount = renderer.pageCount
                    val list = mutableListOf<Bitmap>()
                    for (i in 0 until pageCount.coerceAtMost(10)) { // Limit to 10 pages for preview performance
                        renderer.openPage(i).use { page ->
                            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            list.add(bitmap)
                        }
                    }
                    renderer.close()
                    bitmaps = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (bitmaps.isEmpty()) {
        CircularProgressIndicator()
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bitmaps) { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "PDF Page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

@Composable
private fun VideoPreview(uri: Uri) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val controller = MediaController(context)
                controller.setAnchorView(this)
                setMediaController(controller)
                setVideoURI(uri)
                requestFocus()
                start()
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AudioPreview(uri: Uri) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    // Store MediaPlayer reference for play/pause control
    val mediaPlayer = remember {
        MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                prepareAsync()
                setOnCompletionListener { isPlaying = false }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    DisposableEffect(uri) {
        onDispose {
            try {
                mediaPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Description, // Placeholder for audio icon
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        FilledIconButton(
            onClick = {
                try {
                    if (isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                    isPlaying = !isPlaying
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun TextPreview(uri: Uri) {
    val context = LocalContext.current
    var content by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    content = stream.bufferedReader().readText()
                }
            } catch (e: Exception) {
                content = "Error loading text content."
            }
        }
    }

    if (content == null) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = content!!,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
        }
    }
}

@Composable
private fun FallbackPreview(uri: Uri, mimeType: String) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "This file can't be previewed here",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Try opening it with another application on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { openFileExternally(context, uri, mimeType) }
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Open in other app")
        }
    }
}

@Composable
private fun rememberPdfPages(uri: Uri): androidx.compose.runtime.State<List<Bitmap>> {
    val context = LocalContext.current
    return produceState(initialValue = emptyList(), uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    renderPdfPages(descriptor)
                } ?: emptyList()
            }.getOrDefault(emptyList())
        }
    }
}

private fun renderPdfPages(descriptor: ParcelFileDescriptor): List<Bitmap> {
    val pages = mutableListOf<Bitmap>()
    PdfRenderer(descriptor).use { renderer ->
        repeat(renderer.pageCount) { index ->
            renderer.openPage(index).use { page ->
                val scale = 2
                val bitmap = Bitmap.createBitmap(
                    page.width * scale,
                    page.height * scale,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pages += bitmap
            }
        }
    }
    return pages
}

private fun keyboardOptionsFor(fieldType: CardFieldType): KeyboardOptions {
    return when (fieldType) {
        CardFieldType.EMAIL ->
            KeyboardOptions(keyboardType = KeyboardType.Email)

        CardFieldType.PHONE ->
            KeyboardOptions(keyboardType = KeyboardType.Phone)

        CardFieldType.LINK ->
            KeyboardOptions(keyboardType = KeyboardType.Uri)

        CardFieldType.MULTILINE ->
            KeyboardOptions(keyboardType = KeyboardType.Text)

        CardFieldType.TEXT ->
            KeyboardOptions.Default
    }
}

private fun iconFor(key: String): ImageVector {
    return iconOptions.firstOrNull { it.first == key }?.second ?: Icons.Outlined.Description
}

private fun copyValue(clipboard: ClipboardManager, value: String) {
    clipboard.setText(AnnotatedString(value))
}

private fun openValue(context: Context, field: VaultField) {
    val intent = when (field.fieldType) {
        CardFieldType.EMAIL -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${field.value}"))
        CardFieldType.PHONE -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:${field.value}"))
        else -> null
    } ?: return

    runCatching {
        context.startActivity(intent)
    }
}

private fun canShare(card: VaultCard): Boolean {
    val shareableKinds = setOf(CardType.DOCUMENT, CardType.CERTIFICATE)
    val shareableIcons = setOf("document", "certificate", "article", "badge")
    return (card.type in shareableKinds || card.iconKey in shareableIcons) &&
        card.imageUri != null
}

private fun shareCard(context: Context, card: VaultCard) {
    val summary = buildString {
        append(card.title)
        val usableFields = card.fields.filter { it.value.isNotBlank() }
        if (usableFields.isNotEmpty()) {
            append("\n\n")
            append(
                usableFields.joinToString("\n") { field ->
                    "${field.label}: ${field.value}"
                }
            )
        }
    }

    val attachmentUri = card.imageUri?.toUri()
    val intent = if (attachmentUri != null) {
        val mimeType = context.contentResolver.getType(attachmentUri) ?: "*/*"
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, attachmentUri)
            putExtra(Intent.EXTRA_TEXT, summary)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
    }

    runCatching {
        context.startActivity(Intent.createChooser(intent, "Share card"))
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.getExternalFilesDir(null), "Pictures").apply {
        mkdirs()
    }
    val file = File(imagesDir, "vault_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun toneFor(start: Long, end: Long): CardTone {
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

@Preview(showBackground = true)
@Composable
private fun VaultScreenPreview() {
    InfoAppTheme {
        VaultScreen(
            uiState = VaultUiState(
                cards = listOf(
                    VaultCard(
                        id = 1L,
                        title = "Identity",
                        type = CardType.PROFILE,
                        iconKey = "person",
                        gradientStart = 0xFF355C7D,
                        gradientEnd = 0xFF6C5B7B,
                        fields = listOf(
                            VaultField(label = "Full Name", value = "Your Name Here")
                        )
                    ),
                    VaultCard(
                        id = 2L,
                        title = "Connect",
                        type = CardType.CONTACT,
                        iconKey = "contact",
                        gradientStart = 0xFF134E5E,
                        gradientEnd = 0xFF71B280,
                        fields = listOf(
                            VaultField(
                                label = "Phone Number",
                                value = "",
                                fieldType = CardFieldType.PHONE
                            ),
                            VaultField(
                                label = "Email Address",
                                value = "",
                                fieldType = CardFieldType.EMAIL
                            )
                        )
                    )
                )
            ),
            onCreateCard = {},
            onEditCard = {},
            onDeleteCard = {},
            onOpenSettings = {}
        )
    }
}
