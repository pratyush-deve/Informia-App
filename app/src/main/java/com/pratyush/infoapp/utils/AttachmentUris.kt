package com.pratyush.infoapp.utils

import org.json.JSONArray

private const val IMAGE_GROUP_PREFIX = "images:"

fun isImageUriGroup(uriString: String?): Boolean {
    return uriString?.startsWith(IMAGE_GROUP_PREFIX) == true
}

fun encodeImageUriGroup(uriStrings: List<String>): String? {
    val cleaned = uriStrings
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    return when (cleaned.size) {
        0 -> null
        1 -> cleaned.first()
        else -> IMAGE_GROUP_PREFIX + JSONArray(cleaned).toString()
    }
}

fun decodeImageUriGroup(uriString: String?): List<String> {
    if (uriString.isNullOrBlank()) return emptyList()
    if (!isImageUriGroup(uriString)) return listOf(uriString)

    return runCatching {
        val json = uriString.removePrefix(IMAGE_GROUP_PREFIX)
        val array = JSONArray(json)
        List(array.length()) { index -> array.getString(index) }
            .filter { it.isNotBlank() }
    }.getOrDefault(emptyList())
}

fun primaryAttachmentUri(uriString: String?): String? {
    return decodeImageUriGroup(uriString).firstOrNull()
}
