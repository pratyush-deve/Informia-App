package com.pratyush.infoapp.data.settings

enum class LockMethod {
    PIN,
    BIOMETRIC
}

enum class AutoLockTimer(val label: String, val timeoutMillis: Long) {
    IMMEDIATELY("Immediately", 0L),
    THIRTY_SECONDS("30 seconds", 30_000L),
    ONE_MINUTE("1 minute", 60_000L),
    FIVE_MINUTES("5 minutes", 300_000L)
}

data class AppSettings(
    val appLockEnabled: Boolean = false,
    val pinUnlockEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
    val autoLockTimer: AutoLockTimer = AutoLockTimer.THIRTY_SECONDS,
    val hideAppPreview: Boolean = false,
    val hasPin: Boolean = false
) {
    val hasAnyUnlockMethod: Boolean
        get() = (pinUnlockEnabled && hasPin) || biometricUnlockEnabled
}
