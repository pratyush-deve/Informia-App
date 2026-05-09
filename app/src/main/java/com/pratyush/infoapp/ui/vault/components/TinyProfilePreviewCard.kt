package com.pratyush.infoapp.ui.vault.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.toneFor

@Composable
fun TinyProfilePreviewCard(card: VaultCard) {
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
