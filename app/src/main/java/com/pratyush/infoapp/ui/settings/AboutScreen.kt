package com.pratyush.infoapp.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
private fun aboutBackgroundBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.background
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember(context) { context.versionName() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "About",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Informia at a glance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(aboutBackgroundBrush())
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                AboutHero(versionName = versionName)
            }
            item {
                AboutSection(
                    title = "What Informia is used for",
                    icon = Icons.Outlined.Archive,
                    body = "Informia gives important personal information a calm, organized home. Keep identity details, contacts, documents, certificates, links, and custom records as editable cards that are easy to scan and update."
                )
            }
            item {
                AboutSection(
                    title = "Privacy-focused document storage",
                    icon = Icons.Outlined.Security,
                    body = "Your card previews and attachments stay inside the app experience, with security controls for app locking and hiding previews from screenshots, recording, and recent apps."
                )
            }
            item {
                AboutSection(
                    title = "Organize files and cards",
                    icon = Icons.Outlined.Folder,
                    body = "Use flexible fields, icons, color themes, and media previews to keep everyday records tidy without turning simple details into heavy paperwork."
                )
            }
            item {
                AboutSection(
                    title = "Security features",
                    icon = Icons.Outlined.Lock,
                    body = "App Lock supports PIN and biometric unlock choices, auto-lock timing, and secure preview protection. PIN verification is backed by Android Keystore-assisted hashing."
                )
            }
            item {
                AboutSection(
                    title = "Offline and local-first",
                    icon = Icons.Outlined.OfflineBolt,
                    body = "Informia is designed around quick local access to your saved cards. Your important details remain available when you need them, without depending on a remote account flow."
                )
            }
            item {
                AboutSection(
                    title = "Simplicity and productivity",
                    icon = Icons.Outlined.TaskAlt,
                    body = "The app is intentionally focused: capture the information, attach what matters, preview it cleanly, and get back to the real thing you were doing."
                )
            }
            item {
                InfoCard(
                    title = "Developer : Pratyush Nishank",
                    lines = listOf("Built for Ease", "Designed with a focus on privacy, clarity, and practical daily use.")
                )
            }
            item {
                Text(
                    text = "Informia keeps essential information close, quiet, and well organized.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                )
            }
        }
    }
}

@Composable
private fun AboutHero(versionName: String) {
    val context = LocalContext.current
    val appIcon = remember(context) {
        runCatching {
            context.packageManager.getApplicationIcon(context.packageName).toBitmap()
        }.getOrNull()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.asImageBitmap(),
                        contentDescription = "Informia app icon",
                        modifier = Modifier.size(72.dp)
                    )
                } else {
                    Icon(
                        Icons.Outlined.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = "Informia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "A private, beautiful vault for important cards, identity details, documents, and everyday records.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            InfoCard(
                title = "App Version",
                lines = listOf(versionName)
            )
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    icon: ImageVector,
    body: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    lines: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (lines.size == 1) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun Context.versionName(): String {
    return runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        packageInfo.versionName ?: "Unknown"
    }.getOrDefault("Unknown")
}
