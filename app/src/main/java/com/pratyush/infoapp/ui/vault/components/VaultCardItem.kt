package com.pratyush.infoapp.ui.vault.components

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.pratyush.infoapp.data.local.VaultCard
import com.pratyush.infoapp.data.local.VaultField
import com.pratyush.infoapp.data.local.CardType
import com.pratyush.infoapp.ui.vault.utils.CardTone
import com.pratyush.infoapp.ui.vault.utils.toneFor
import com.pratyush.infoapp.ui.vault.utils.canShare
import com.pratyush.infoapp.ui.vault.utils.iconFor
import com.pratyush.infoapp.ui.vault.utils.shareFileExternally

@Composable
fun VaultCardItem(
    card: VaultCard,
    onEditCard: () -> Unit,
    onFieldCopied: () -> Unit,
    onOpenPreview: () -> Unit
) {
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
                val context = LocalContext.current
                StandardCardHeader(
                    card = card,
                    expanded = expanded,
                    rotation = rotation,
                    onEditCard = onEditCard,
                    onShare = {
                        card.imageUri?.let {
                            shareFileExternally(context, Uri.parse(it))
                        }
                    },
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
                            tone = tone,
                            startColor = card.gradientStart,
                            endColor = card.gradientEnd,
                            iconKey = card.iconKey
                        )
                    }
                }
            }
        }
    }
}
