package com.pratyush.infoapp.ui.vault.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image

@Composable
fun AttachmentChooserButton(
    isProfile: Boolean,
    hasPreview: Boolean,
    onChoose: () -> Unit,
    onRemove: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onChoose) {
            Icon(Icons.Outlined.Image, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (isProfile) "Choose Photo" else "Choose Preview")
        }
        if (hasPreview) {
            OutlinedButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}
