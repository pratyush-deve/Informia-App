package com.pratyush.infoapp.ui.vault.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Share
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.iconFor
import com.pratyush.infoapp.ui.vault.utils.canShare

@Composable
fun StandardCardHeader(
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
        CardIconBubble(
            icon = iconFor(card.iconKey),
            size = 48.dp,
            iconTint = tone.content
        )
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
