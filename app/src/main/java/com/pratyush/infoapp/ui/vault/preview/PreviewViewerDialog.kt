package com.pratyush.infoapp.ui.vault.preview

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pratyush.infoapp.ui.vault.pdf.PdfViewerScreen
import com.pratyush.infoapp.ui.vault.utils.openFileExternally
import com.pratyush.infoapp.ui.vault.utils.shareFileExternally
import com.pratyush.infoapp.utils.decodeImageUriGroup
import com.pratyush.infoapp.utils.primaryAttachmentUri

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PreviewViewerDialog(
    uriString: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageUriStrings = remember(uriString) { decodeImageUriGroup(uriString) }
    val uri = remember(uriString) { Uri.parse(primaryAttachmentUri(uriString) ?: uriString) }
    val mimeType = context.contentResolver.getType(uri) ?: ""
    val pagerState = rememberPagerState(pageCount = { imageUriStrings.size.coerceAtLeast(1) })
    val currentImageUri = imageUriStrings
        .getOrNull(pagerState.currentPage)
        ?.let(Uri::parse)
        ?: uri

    val isPdf =
        mimeType.contains("pdf", ignoreCase = true) ||
                uri.toString().endsWith(".pdf", ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Log.d("PDF_DEBUG", "mimeType = $mimeType | uri = $uri")

        if (isPdf) {
            PdfViewerScreen(
                uriString = uriString,
                onBack = onDismiss
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Image Viewer") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Back")
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(tonalElevation = 4.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    openFileExternally(context, currentImageUri, "image/*")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Open")
                            }

                            OutlinedButton(
                                onClick = {
                                    shareFileExternally(context, currentImageUri)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUriStrings.size > 1) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                ImagePreview(Uri.parse(imageUriStrings[page]))
                            }
                            SwipeHint(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 18.dp)
                            )
                        }
                    } else {
                        ImagePreview(uri)
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeHint(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "swipeHint")
    val offset by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeOffset"
    )

    Surface(
        modifier = modifier.graphicsLayer { translationX = offset },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 2.dp
    ) {
        Text(
            text = "Swipe for next image",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
