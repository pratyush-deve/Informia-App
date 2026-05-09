package com.pratyush.infoapp.ui.vault.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import com.pratyush.infoapp.ui.vault.components.CardIconBubble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentOptionsSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (AttachmentOption) -> Unit,
    isProfile: Boolean
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss
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
                    onClick = { onOptionSelected(AttachmentOption.GALLERY) }
                )
                AttachmentOptionCard(
                    option = AttachmentOption.CAMERA,
                    onClick = { onOptionSelected(AttachmentOption.CAMERA) }
                )
                AttachmentOptionCard(
                    option = AttachmentOption.FILES,
                    onClick = { onOptionSelected(AttachmentOption.FILES) },
                    subtitle = if (isProfile) {
                        "Browse image files"
                    } else {
                        "Browse image files or PDF documents"
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachmentOptionCard(
    option: AttachmentOption,
    onClick: () -> Unit,
    subtitle: String = option.subtitle
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
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
