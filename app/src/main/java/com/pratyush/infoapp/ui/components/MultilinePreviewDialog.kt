package com.pratyush.infoapp.ui.vault.components

import android.R.attr.visible
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.iconFor
import com.pratyush.infoapp.ui.vault.utils.toneFor
import kotlinx.coroutines.delay

@Composable
fun MultilinePreviewDialog(
    title: String,
    content: String,
    clipboardManager: ClipboardManager,
    onDismiss: () -> Unit,
    onCopied: () -> Unit,
    tone: CardTone,
    startColor: Long,
    endColor: Long,
    iconKey: String
) {

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(startColor).copy(alpha = 0.42f),
            Color(endColor).copy(alpha = 0.24f),
            Color(0xFF0E0E0E)
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),

            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(startColor).copy(alpha = 0.92f),
                                Color(endColor).copy(alpha = 0.78f),
                                Color(0xFF121212)
                            )
                        )
                    )
            ) {

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable(onClick = onDismiss)
                        .padding(8.dp),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {

                            Icon(
                                imageVector = iconFor(iconKey),
                                contentDescription = null,
                                tint = tone.content,
                                modifier = Modifier.padding(9.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = title,
                            color = tone.content,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .heightIn(max = 360.dp),

                        shape = RoundedCornerShape(24.dp),

                        color = Color.Black.copy(alpha = 0.32f)
                    ) {
                        val scrollState = rememberScrollState()

                        Box {

                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .padding(
                                        start = 20.dp,
                                        top = 20.dp,
                                        end = 28.dp,
                                        bottom = 20.dp
                                    )
                            ) {

                                Text(
                                    text = content.ifBlank { "No content" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(28.dp))
                            }

                            // tiny scrollbar track
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 10.dp)
                                    .width(3.dp)
                                    .fillMaxWidth(0f)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                            ) {

                                val indicatorHeight = 36.dp

                                val scrollProgress =
                                    if (scrollState.maxValue == 0) {
                                        0f
                                    } else {
                                        scrollState.value.toFloat() /
                                                scrollState.maxValue.toFloat()
                                    }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(
                                            top = (84.dp * scrollProgress)
                                        )
                                        .width(3.dp)
                                        .height(indicatorHeight)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.White.copy(alpha = 0.55f))
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 18.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),

                        horizontalArrangement = Arrangement.Center
                    ) {

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF2563EB))
                                .clickable {

                                    clipboardManager.setText(
                                        AnnotatedString(content)
                                    )

                                    onCopied()
                                }
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                ),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Copy Text",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 420,
    heightDp = 700
)
@Composable
private fun MultilinePreviewDialogPreview() {

    val clipboard = LocalClipboardManager.current

    val start = 0xFF3A1C71
    val end = 0xFFD76D77

    MaterialTheme {

        Surface(
            color = Color.Black
        ) {

            MultilinePreviewDialog(
                title = "Notes",

                content = """
                    This is a multiline preview dialog.

                    • Gradient tinted backdrop
                    • Dynamic card theming
                    • Scrollable content
                    • Rounded corners
                    • Better action buttons
                    • Dynamic icon support

                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

                    Ut enim ad minim veniam,
                    quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

                    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
                """.trimIndent(),

                clipboardManager = clipboard,

                onDismiss = {},

                onCopied = {},

                tone = toneFor(start, end),

                startColor = start,

                endColor = end,

                iconKey = "article"
            )
        }
    }
}