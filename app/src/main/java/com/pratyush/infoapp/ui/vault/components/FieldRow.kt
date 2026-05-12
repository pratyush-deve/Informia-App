package com.pratyush.infoapp.ui.vault.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.ui.text.AnnotatedString
import com.pratyush.infoapp.data.local.VaultField
import com.pratyush.infoapp.data.local.CardFieldType
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.extractLinkMeta
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FieldRow(
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
                                    copyValue(clipboard, field.value)
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
                
                if (field.fieldType == CardFieldType.LINK && field.value.isNotBlank()) {
                    val linkMeta = extractLinkMeta(field.value)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.width(28.dp).height(28.dp),
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 1.dp,
                            tonalElevation = 0.5.dp
                        ) {
                            Icon(
                                painter = painterResource(linkMeta.iconRes),
                                contentDescription = null,
                                modifier = Modifier.width(20.dp).height(20.dp),
                                tint = androidx.compose.ui.graphics.Color.Unspecified
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = linkMeta.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = tone.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = linkMeta.displayUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = tone.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        text = field.value.ifBlank { "No value" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = tone.content
                    )
                }
            }
            if (
                field.fieldType == CardFieldType.EMAIL ||
                field.fieldType == CardFieldType.PHONE ||
                field.fieldType == CardFieldType.LINK
                ) {
                IconButton(
                    onClick = {
                        copyJob?.cancel()
                        copyJob = null
                        openValue(context, field)
                    }
                ){
                    Icon(
                        imageVector = if (field.fieldType == CardFieldType.EMAIL) {
                            Icons.Outlined.Email
                        }
                        else if (field.fieldType == CardFieldType.LINK){
                            Icons.Outlined.Link
                        }
                        else {
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

private fun copyValue(clipboard: ClipboardManager, value: String) {
    clipboard.setText(AnnotatedString(value))
}

private fun openValue(context: Context, field: VaultField) {
    val intent = when (field.fieldType) {
        CardFieldType.EMAIL -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${field.value}"))
        CardFieldType.PHONE -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:${field.value}"))
        CardFieldType.LINK -> {
            val url = if (
                field.value.startsWith("http://") ||
                field.value.startsWith("https://")
            ) {
                field.value
            } else {
                "https://${field.value}"
            }

            val linkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            linkIntent.setPackage("com.android.chrome")
            linkIntent
        }
        else -> null
    } ?: return

    runCatching {
        context.startActivity(intent)
    }.onFailure {
        // Fallback if Chrome is not installed
        if (field.fieldType == CardFieldType.LINK) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(field.value))
            runCatching {
                context.startActivity(fallbackIntent)
            }
        }
    }
}
