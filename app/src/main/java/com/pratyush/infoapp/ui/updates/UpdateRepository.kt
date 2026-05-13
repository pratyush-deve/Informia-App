package com.pratyush.infoapp.ui.updates

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UpdateRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val latestUpdate = currentInformiaUpdate()
    private val _state = MutableStateFlow(preferences.readGateState())

    val state: StateFlow<UpdateGateState> = _state.asStateFlow()

    fun markCurrentVersionSeen() {
        val current = currentVersionInfo()
        preferences.edit()
            .putLong(KEY_LAST_SEEN_UPDATE_VERSION, current.versionCode)
            .putLong(KEY_LAST_KNOWN_APP_VERSION, current.versionCode)
            .applyAndRefresh()
    }

    private fun SharedPreferences.Editor.applyAndRefresh() {
        apply()
        _state.value = preferences.readGateState()
    }

    private fun SharedPreferences.readGateState(): UpdateGateState {
        val current = currentVersionInfo()
        val lastSeen = getLong(KEY_LAST_SEEN_UPDATE_VERSION, NO_VERSION_SEEN)
        val lastKnown = getLong(KEY_LAST_KNOWN_APP_VERSION, NO_VERSION_SEEN)
        val hasStoredVersion = lastSeen != NO_VERSION_SEEN || lastKnown != NO_VERSION_SEEN
        val isFirstLaunchAfterRealUpdate = !hasStoredVersion && current.isUpdateInstall
        val isStoredVersionUpgrade = hasStoredVersion && current.versionCode > lastKnown
        val hasUnseenUpdateContent = latestUpdate.versionCode <= current.versionCode &&
            latestUpdate.versionCode > lastSeen

        return UpdateGateState(
            currentVersion = current,
            latestUpdate = latestUpdate,
            lastSeenVersionCode = lastSeen,
            shouldShowAutomatically = FORCE_SHOW_UPDATES || hasUnseenUpdateContent &&
                (isFirstLaunchAfterRealUpdate || isStoredVersionUpgrade)
        )
    }

    private fun currentVersionInfo(): AppVersionInfo {
        val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        return AppVersionInfo(
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
            versionName = packageInfo.versionName.orEmpty(),
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime
        )
    }

    companion object {
        //toggle to enable onboarding screen
        //true to keep onboarding on always
        const val FORCE_SHOW_UPDATES = false

        private const val PREFS_NAME = "informia_update_onboarding"
        private const val KEY_LAST_SEEN_UPDATE_VERSION = "last_seen_update_version"
        private const val KEY_LAST_KNOWN_APP_VERSION = "last_known_app_version"
        private const val NO_VERSION_SEEN = -1L

        @Volatile
        private var instance: UpdateRepository? = null

        fun getInstance(context: Context): UpdateRepository {
            return instance ?: synchronized(this) {
                instance ?: UpdateRepository(context).also { instance = it }
            }
        }
    }
}

private fun currentInformiaUpdate(): UpdateVersion {
    return UpdateVersion(
        versionCode = 3L,
        versionName = "1.2",
        title = "What's New in Informia",
        subtitle = "Your documents are now more private and secure than ever.",
        pages = listOf(
            UpdatePage(
                title = "What's New in Informia",
                subtitle = "Your documents are now more private and secure than ever.",
                icon = Icons.Outlined.WorkspacePremium,
                accentColor = Color(0xFF7DD3FC),
                kind = UpdatePageKind.Welcome
            ),
            UpdatePage(
                title = "App Lock",
                subtitle = "Protect your private documents with PIN or biometric unlock.",
                icon = Icons.Outlined.Lock,
                accentColor = Color(0xFF6EE7B7),
                kind = UpdatePageKind.AppLock,
                features = listOf(
                    UpdateFeature("PIN protection", "Keep Informia behind a private app PIN.", Icons.Outlined.Password),
                    UpdateFeature("Biometric unlock", "Use fingerprint or face unlock when your device supports it.", Icons.Outlined.Fingerprint),
                    UpdateFeature("Flexible access", "Choose one unlock method or keep both enabled.", Icons.Outlined.Security)
                )
            ),
            UpdatePage(
                title = "Smart Auto Lock",
                subtitle = "Informia can lock itself after you leave the app.",
                icon = Icons.Outlined.Timer,
                accentColor = Color(0xFFFFC857),
                kind = UpdatePageKind.AutoLock,
                features = listOf(
                    UpdateFeature("Auto-lock timer", "Pick the timeout that fits how you work.", Icons.Outlined.Timer),
                    UpdateFeature("Safer multitasking", "Return from other apps without leaving documents exposed.", Icons.Outlined.Shield),
                    UpdateFeature("Foreground protection", "Lock checks run when Informia comes back into view.", Icons.Outlined.Lock)
                )
            ),
            UpdatePage(
                title = "Hidden Recents Preview",
                subtitle = "Keep sensitive content out of recent apps and screenshots.",
                icon = Icons.Outlined.VisibilityOff,
                accentColor = Color(0xFFFF8A80),
                kind = UpdatePageKind.PrivacyPreview,
                features = listOf(
                    UpdateFeature("Hidden previews", "Your vault content stays covered in the app switcher.", Icons.Outlined.VisibilityOff),
                    UpdateFeature("Screenshot protection", "Block screenshots and screen recording when enabled.", Icons.Outlined.Shield),
                    UpdateFeature("Privacy-first behavior", "A discreet privacy view appears as the app leaves foreground.", Icons.Outlined.Security)
                )
            ),
            UpdatePage(
                title = "Your privacy matters.",
                subtitle = "Informia is ready with stronger protection for your documents.",
                icon = Icons.Outlined.Shield,
                accentColor = Color(0xFFB794F4),
                kind = UpdatePageKind.Final
            )
        )
    )
}
