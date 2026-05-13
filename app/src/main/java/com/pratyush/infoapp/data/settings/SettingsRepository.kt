package com.pratyush.infoapp.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

class SettingsRepository private constructor(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(preferences.readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setAppLockEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_APP_LOCK_ENABLED, enabled)
            .applyAndRefresh()
    }

    fun configureAppLock(
        enabled: Boolean,
        pinUnlockEnabled: Boolean,
        biometricUnlockEnabled: Boolean,
        pin: String? = null
    ) {
        val editor = preferences.edit()
            .putBoolean(KEY_APP_LOCK_ENABLED, enabled)
            .putBoolean(KEY_PIN_UNLOCK_ENABLED, pinUnlockEnabled)
            .putBoolean(KEY_BIOMETRIC_UNLOCK_ENABLED, biometricUnlockEnabled)
        if (pin != null) {
            val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
            val hash = hashPin(pin, salt)
            editor
                .putString(KEY_PIN_SALT, salt.toBase64())
                .putString(KEY_PIN_HASH, hash.toBase64())
        }
        editor.applyAndRefresh()
    }

    fun setPinUnlockEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_PIN_UNLOCK_ENABLED, enabled)
            .applyAndRefresh()
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_BIOMETRIC_UNLOCK_ENABLED, enabled)
            .applyAndRefresh()
    }

    fun setAutoLockTimer(timer: AutoLockTimer) {
        preferences.edit()
            .putString(KEY_AUTO_LOCK_TIMER, timer.name)
            .applyAndRefresh()
    }

    fun setHideAppPreview(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_HIDE_APP_PREVIEW, enabled)
            .applyAndRefresh()
    }

    fun savePin(pin: String) {
        val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
        val hash = hashPin(pin, salt)
        preferences.edit()
            .putString(KEY_PIN_SALT, salt.toBase64())
            .putString(KEY_PIN_HASH, hash.toBase64())
            .putBoolean(KEY_PIN_UNLOCK_ENABLED, true)
            .applyAndRefresh()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = preferences.getString(KEY_PIN_SALT, null)?.fromBase64() ?: return false
        val expected = preferences.getString(KEY_PIN_HASH, null)?.fromBase64() ?: return false
        val actual = hashPin(pin, salt)
        return actual.contentEquals(expected)
    }

    fun markBackgroundedAt(timestampMillis: Long) {
        preferences.edit()
            .putLong(KEY_LAST_BACKGROUNDED_AT, timestampMillis)
            .apply()
    }

    fun shouldLockOnForeground(nowMillis: Long): Boolean {
        val settings = settings.value
        if (!settings.appLockEnabled || !settings.hasAnyUnlockMethod) return false

        val lastBackgroundedAt = preferences.getLong(KEY_LAST_BACKGROUNDED_AT, 0L)
        if (lastBackgroundedAt <= 0L) return false

        return nowMillis - lastBackgroundedAt >= settings.autoLockTimer.timeoutMillis
    }

    private fun SharedPreferences.Editor.applyAndRefresh() {
        apply()
        _settings.value = preferences.readSettings()
    }

    private fun SharedPreferences.readSettings(): AppSettings {
        val hasPin = getString(KEY_PIN_HASH, null) != null
        val oldLockMethod = enumValueOrDefault(
            getString(KEY_LOCK_METHOD, null),
            LockMethod.PIN
        )
        val legacyAppLockEnabled = getBoolean(KEY_APP_LOCK_ENABLED, false)
        val hasMigratedUnlockMethods = contains(KEY_PIN_UNLOCK_ENABLED) ||
            contains(KEY_BIOMETRIC_UNLOCK_ENABLED)
        val pinUnlockEnabled = if (hasMigratedUnlockMethods) {
            getBoolean(KEY_PIN_UNLOCK_ENABLED, false)
        } else {
            legacyAppLockEnabled && oldLockMethod == LockMethod.PIN && hasPin
        }
        val biometricUnlockEnabled = if (hasMigratedUnlockMethods) {
            getBoolean(KEY_BIOMETRIC_UNLOCK_ENABLED, false)
        } else {
            legacyAppLockEnabled && oldLockMethod == LockMethod.BIOMETRIC
        }

        return AppSettings(
            appLockEnabled = legacyAppLockEnabled,
            pinUnlockEnabled = pinUnlockEnabled,
            biometricUnlockEnabled = biometricUnlockEnabled,
            autoLockTimer = enumValueOrDefault(
                getString(KEY_AUTO_LOCK_TIMER, null),
                AutoLockTimer.THIRTY_SECONDS
            ),
            hideAppPreview = getBoolean(KEY_HIDE_APP_PREVIEW, false),
            hasPin = hasPin
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T {
        return value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(getOrCreateSecretKey())
        mac.update(salt)
        return mac.doFinal(pin.toByteArray(Charsets.UTF_8))
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun ByteArray.toBase64(): String =
        Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray =
        Base64.decode(this, Base64.NO_WRAP)

    companion object {
        private const val PREFS_NAME = "informia_secure_settings"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_LOCK_METHOD = "lock_method"
        private const val KEY_PIN_UNLOCK_ENABLED = "pin_unlock_enabled"
        private const val KEY_BIOMETRIC_UNLOCK_ENABLED = "biometric_unlock_enabled"
        private const val KEY_AUTO_LOCK_TIMER = "auto_lock_timer"
        private const val KEY_HIDE_APP_PREVIEW = "hide_app_preview"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_LAST_BACKGROUNDED_AT = "last_backgrounded_at"
        private const val KEYSTORE_ALIAS = "informia_app_lock_hmac"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val SALT_BYTES = 32

        private val secureRandom = SecureRandom()

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
        }
    }
}
