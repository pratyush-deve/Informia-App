package com.pratyush.infoapp.ui.vault.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.FileType
import com.pratyush.infoapp.ui.vault.utils.getFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CardMedia(
    card: VaultCard,
    expanded: Boolean,
    editablePreview: Boolean,
    onOpenPreview: (() -> Unit)? = null,
    tone: CardTone
) {
    var aspectRatio by remember { mutableStateOf(1f) }

    val context = LocalContext.current
    val uriString = card.imageUri
    val uri = if (uriString?.isNotEmpty() == true) {
        try {
            uriString.toUri()
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
    val fileType = remember(uri) { uri?.let { getFileType(context, it) } ?: FileType.UNKNOWN }

    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfLoading by remember { mutableStateOf(false) }
    var pdfError by remember { mutableStateOf(false) }
    // Load PDF first page when fileType is PDF
    LaunchedEffect(uri) {
        if (fileType == FileType.PDF && uri != null) {
            pdfLoading = true
            pdfError = false

            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val pfd = if (uri.scheme == "file") {
                        ParcelFileDescriptor.open(
                            java.io.File(uri.path!!),
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
                    } else {
                        context.contentResolver.openFileDescriptor(uri, "r")
                    }

                    pfd?.use {
                        PdfRenderer(it).use { renderer ->
                            if (renderer.pageCount > 0) {
                                val page = renderer.openPage(0)
                                aspectRatio = page.width.toFloat() / page.height.toFloat()

                                val bmp = Bitmap.createBitmap(
                                    page.width,
                                    page.height,
                                    Bitmap.Config.ARGB_8888
                                )

                                page.render(
                                    bmp,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )

                                page.close()

                                Bitmap.createScaledBitmap(
                                    bmp,
                                    (bmp.width * 0.5f).toInt(),
                                    (bmp.height * 0.5f).toInt(),
                                    true
                                )
                            } else null
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            pdfBitmap = bitmap
            pdfError = bitmap == null
            pdfLoading = false
        }
    }
    Log.d("PDF_CARD", "Loading PDF thumbnail")
    Log.d("PDF_CARD", "Bitmap = $pdfBitmap")
    Log.d("PDF_CARD", "URI: $uri")
    Log.d("PDF_CARD", "FileType: $fileType")
    Log.d("PDF_CARD", "Path: ${uri?.path}")

    val isRealPdf = try {
        val file = java.io.File(uri?.path!!)
        val header = file.inputStream().buffered().use {
            val bytes = ByteArray(5)
            it.read(bytes)
            String(bytes)
        }
        header.startsWith("%PDF")
    } catch (e: Exception) {
        false
    }

    Log.d("PDF_CARD", "Is real PDF: $isRealPdf")

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
                    uri = uri,
                    title = card.title,
                    fileType = fileType,
                    pdfBitmap = pdfBitmap,
                    pdfLoading = pdfLoading,
                    pdfError = pdfError,
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
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    if (uri != null) {
                        Box {


                            if (fileType == FileType.PDF && !editablePreview) {
                                Text(
                                    text = "PDF",
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            if (uri.toString().endsWith(".pdf", true)) {

                                when {
                                    pdfLoading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(if (aspectRatio > 0f) aspectRatio else 1f)
                                                .heightIn(max = 300.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    pdfError -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(if (aspectRatio > 0f) aspectRatio else 1f)
                                                .heightIn(max = 300.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("PDF preview unavailable")
                                        }
                                    }

                                    pdfBitmap != null -> {
                                        Image(
                                            bitmap = pdfBitmap!!.asImageBitmap(),
                                            contentDescription = "PDF preview",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(if (aspectRatio > 0f) aspectRatio else 1f)
                                                .heightIn(max = 300.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(if (aspectRatio > 0f) aspectRatio else 1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Loading PDF...")
                                        }
                                    }
                                }

                            } else {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "${card.title} preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(if (aspectRatio > 0f) aspectRatio else 1f)
                                        .heightIn(max = 300.dp),
                                    contentScale = if (editablePreview) {
                                        ContentScale.Crop
                                    } else {
                                        ContentScale.Fit
                                    },
                                    onSuccess = {
                                        val drawable = it.result.drawable
                                        val w = drawable.intrinsicWidth
                                        val h = drawable.intrinsicHeight
                                        if (w > 0 && h > 0) {
                                            aspectRatio = w.toFloat() / h.toFloat()
                                        }
                                    }
                                )
                            }

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
    uri: Uri,
    title: String,
    fileType: FileType,
    pdfBitmap: Bitmap?,
    pdfLoading: Boolean,
    pdfError: Boolean,
    tone: CardTone,
    onClick: (() -> Unit)?
) {
    val previewShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tone.mediaLabelBackground, RoundedCornerShape(14.dp))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(116.dp)
                .height(64.dp)
                .clip(previewShape)
                .background(tone.fieldContainer),
            contentAlignment = Alignment.Center
        ) {
            when {
                fileType == FileType.PDF && pdfBitmap != null -> {
                    Image(
                        bitmap = pdfBitmap.asImageBitmap(),
                        contentDescription = "$title preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                fileType == FileType.PDF && pdfLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = tone.content.copy(alpha = 0.85f)
                    )
                }

                fileType == FileType.IMAGE -> {
                    AsyncImage(
                        model = uri,
                        contentDescription = "$title preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(if (pdfError) 22.dp else 24.dp),
                        tint = tone.content.copy(alpha = 0.9f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
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
