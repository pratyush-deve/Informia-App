package com.pratyush.infoapp.ui.vault.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pratyush.infoapp.data.local.CardFieldType
import com.pratyush.infoapp.ui.vault.EditableField

@Composable
fun EditableFieldCard(
    field: EditableField,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onTypeChange: (CardFieldType) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Field",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
            OutlinedTextField(
                value = field.label,
                onValueChange = onLabelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Label") },
                singleLine = true
            )
            OutlinedTextField(
                value = field.value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Value") },
                keyboardOptions = keyboardOptionsFor(field.fieldType),
                singleLine = field.fieldType != CardFieldType.MULTILINE,
                minLines = if (field.fieldType == CardFieldType.MULTILINE) 2 else 1,
                maxLines = if (field.fieldType == CardFieldType.MULTILINE) 4 else 1
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardFieldType.entries.forEach { type ->
                    FilterChip(
                        selected = field.fieldType == type,
                        onClick = { onTypeChange(type) },
                        label = {
                            Text(type.name.lowercase().replaceFirstChar { it.titlecase() })
                        }
                    )
                }
            }
        }
    }
}

private fun keyboardOptionsFor(fieldType: CardFieldType): KeyboardOptions {
    return when (fieldType) {
        CardFieldType.EMAIL ->
            KeyboardOptions(keyboardType = KeyboardType.Email)

        CardFieldType.PHONE ->
            KeyboardOptions(keyboardType = KeyboardType.Phone)

        CardFieldType.LINK ->
            KeyboardOptions(keyboardType = KeyboardType.Uri)

        CardFieldType.MULTILINE ->
            KeyboardOptions(keyboardType = KeyboardType.Text)

        CardFieldType.TEXT ->
            KeyboardOptions.Default
    }
}
