package com.pratyush.infoapp.ui.updates

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class UpdateFeature(
    val title: String,
    val description: String,
    val icon: ImageVector
)

data class UpdatePage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val features: List<UpdateFeature> = emptyList(),
    val accentColor: Color,
    val kind: UpdatePageKind
)

data class UpdateVersion(
    val versionCode: Long,
    val versionName: String,
    val title: String,
    val subtitle: String,
    val pages: List<UpdatePage>
)

data class AppVersionInfo(
    val versionCode: Long,
    val versionName: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
) {
    val isUpdateInstall: Boolean
        get() = lastUpdateTime > firstInstallTime
}

data class UpdateGateState(
    val currentVersion: AppVersionInfo,
    val latestUpdate: UpdateVersion,
    val lastSeenVersionCode: Long,
    val shouldShowAutomatically: Boolean
)

enum class UpdatePageKind {
    Welcome,
    AppLock,
    AutoLock,
    PrivacyPreview,
    Final
}
