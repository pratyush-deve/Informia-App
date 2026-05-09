package com.pratyush.infoapp.ui.vault.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.ui.graphics.vector.ImageVector

enum class AttachmentOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    GALLERY("Gallery", "Use a photo already on your device", Icons.Outlined.Image),
    CAMERA("Camera", "Capture a fresh image right now", Icons.Outlined.PhotoCamera),
    FILES("Drive / Files", "Browse image files or PDF documents", Icons.AutoMirrored.Outlined.InsertDriveFile)
}
