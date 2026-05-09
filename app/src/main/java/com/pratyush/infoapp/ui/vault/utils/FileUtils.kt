package com.pratyush.infoapp.ui.vault.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File


fun openFileExternally(context: Context, uri: Uri, mimeType: String) {

    val finalUri = if (uri.scheme == "file") {
        val file = File(uri.path!!)
        Log.d("PDF_CARD", "Exists: ${file.exists()} Size: ${file.length()}")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } else {
        uri
    }

    val finalMime = if (mimeType.isBlank()) "application/pdf" else mimeType

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(finalUri, finalMime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val pm = context.packageManager
    val activities = pm.queryIntentActivities(intent, 0)

    Log.d("OPEN_DEBUG", "MIME: $finalMime")
    Log.d("OPEN_DEBUG", "Apps found: ${activities.size}")

    when {
        activities.isEmpty() -> {
            Toast.makeText(context, "No app can open this file", Toast.LENGTH_LONG).show()
        }

        activities.size == 1 -> {
            // 🔥 Direct open (default behavior)
            context.startActivity(intent)
        }

        else -> {
            // 🔥 Let user choose
            context.startActivity(Intent.createChooser(intent, "Open with"))
        }
    }
}
fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.getExternalFilesDir(null), "Pictures").apply {
        mkdirs()
    }
    val file = File(imagesDir, "vault_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Gets the display name of a file from its URI.
 */
fun getFileName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}


fun shareFileExternally(context: Context, uri: Uri) {
    val file = File(uri.path!!)

    val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val mimeType = context.contentResolver.getType(contentUri) ?: "image/*"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
    Log.d("SHARE_DEBUG", "Content URI: $contentUri")
}
