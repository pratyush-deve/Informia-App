package com.pratyush.infoapp.ui.vault.utils

import android.content.Context
import android.net.Uri

/**
 * Categorizes files for the preview system.
 */
enum class FileType(val label: String, val chipLabel: String) {
    IMAGE("Image viewer", "Image preview"),
    VIDEO("Video player", "Video preview"),
    AUDIO("Audio player", "Audio file"),
    PDF("Document viewer", "PDF preview"),
    TEXT("Text viewer", "Text preview"),
    UNKNOWN("File viewer", "Tap to open")
}

/**
 * Centralized file type detection.
 */
fun getFileType(context: Context, uri: Uri): FileType {
    val path = uri.toString().lowercase()

    return when {
        path.endsWith(".pdf") -> FileType.PDF
        path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".png") -> FileType.IMAGE
        else -> FileType.UNKNOWN
    }
}
