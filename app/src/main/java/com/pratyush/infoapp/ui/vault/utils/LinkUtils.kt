package com.pratyush.infoapp.ui.vault.utils

import androidx.compose.ui.res.stringResource
import com.pratyush.infoapp.R

data class LinkMeta(
    val title: String,
    val displayUrl: String,
    val iconRes: Int
)

fun extractLinkMeta(url: String): LinkMeta {
    if (url.isBlank()) {
        return LinkMeta(
            title = "Link",
            displayUrl = url,
            iconRes = R.drawable.ic_link_generic_brand
        )
    }

    val cleanUrl = cleanUrl(url)
    val domain = extractDomain(cleanUrl)
    
    return when {
        domain.contains("google.com") && !domain.contains("drive.google.com") -> {
            LinkMeta(
                title = "Google",
                displayUrl = formatDisplayUrl(cleanUrl, "google.com"),
                iconRes = R.drawable.ic_link_google_brand
            )
        }
        domain.contains("youtube.com") || domain.contains("youtu.be") -> {
            LinkMeta(
                title = "YouTube",
                displayUrl = formatDisplayUrl(cleanUrl, "youtube.com"),
                iconRes = R.drawable.ic_link_youtube_brand
            )
        }
        domain.contains("github.com") -> {
            LinkMeta(
                title = "GitHub",
                displayUrl = formatDisplayUrl(cleanUrl, "github.com"),
                iconRes = R.drawable.ic_link_github_brand
            )
        }
        domain.contains("linkedin.com") -> {
            LinkMeta(
                title = "LinkedIn",
                displayUrl = formatDisplayUrl(cleanUrl, "linkedin.com"),
                iconRes = R.drawable.ic_link_linkedin_brand
            )
        }
        domain.contains("drive.google.com") -> {
            LinkMeta(
                title = "Google Drive",
                displayUrl = formatDisplayUrl(cleanUrl, "drive.google.com"),
                iconRes = R.drawable.googe_drive
            )
        }
        else -> {
            val genericTitle = extractGenericTitle(domain)
            LinkMeta(
                title = genericTitle,
                displayUrl = formatGenericDisplayUrl(cleanUrl, domain),
                iconRes = R.drawable.ic_link_generic_brand
            )
        }
    }
}

private fun cleanUrl(url: String): String {
    return url.trim()
        .removePrefix("http://")
        .removePrefix("https://")
        .removePrefix("www.")
        .removeSuffix("/")
}

private fun extractDomain(cleanUrl: String): String {
    return cleanUrl.substringBefore("/").lowercase()
}

private fun formatDisplayUrl(cleanUrl: String, domain: String): String {
    return if (cleanUrl == domain) {
        domain
    } else {
        val path = cleanUrl.substringAfter(domain, "")
        if (path.isNotEmpty()) {
            "$domain$path"
        } else {
            domain
        }
    }
}

private fun formatGenericDisplayUrl(cleanUrl: String, domain: String): String {
    return if (cleanUrl == domain) {
        domain
    } else {
        val path = cleanUrl.substringAfter(domain, "")
        if (path.isNotEmpty()) {
            "$domain$path"
        } else {
            domain
        }
    }
}

private fun extractGenericTitle(domain: String): String {
    return try {
        val parts = domain.split(".")
        when {
            parts.size >= 2 && parts[parts.size - 2].length > 3 -> {
                parts[parts.size - 2].replaceFirstChar { it.uppercase() }
            }
            parts.isNotEmpty() -> {
                parts[0].replaceFirstChar { it.uppercase() }
            }
            else -> "Link"
        }
    } catch (e: Exception) {
        "Link"
    }
}
