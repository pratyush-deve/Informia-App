package com.pratyush.infoapp.ui.vault.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.toneFor

@Composable
fun ProfileCardHeader(
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
