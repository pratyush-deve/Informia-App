package com.pratyush.infoapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pratyush.infoapp.data.settings.AutoLockTimer
import com.pratyush.infoapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.settings.value
    )

    fun enableAppLockWithPin(pin: String) {
        repository.configureAppLock(
            enabled = true,
            pinUnlockEnabled = true,
            biometricUnlockEnabled = repository.settings.value.biometricUnlockEnabled,
            pin = pin
        )
    }

    fun configureAppLock(
        pin: String?,
        pinUnlockEnabled: Boolean,
        biometricUnlockEnabled: Boolean
    ) {
        repository.configureAppLock(
            enabled = true,
            pinUnlockEnabled = pinUnlockEnabled,
            biometricUnlockEnabled = biometricUnlockEnabled,
            pin = pin
        )
    }

    fun setAppLockEnabled(enabled: Boolean) {
        repository.setAppLockEnabled(enabled)
    }

    fun setPinUnlockEnabled(enabled: Boolean) {
        repository.setPinUnlockEnabled(enabled)
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        repository.setBiometricUnlockEnabled(enabled)
    }

    fun changePin(newPin: String) {
        repository.savePin(newPin)
    }

    fun setAutoLockTimer(timer: AutoLockTimer) {
        repository.setAutoLockTimer(timer)
    }

    fun setHideAppPreview(enabled: Boolean) {
        repository.setHideAppPreview(enabled)
    }

    fun lockIfNeeded(nowMillis: Long): Boolean {
        return repository.shouldLockOnForeground(nowMillis)
    }

    fun markBackgroundedAt(timestampMillis: Long) {
        repository.markBackgroundedAt(timestampMillis)
    }

    fun verifyPin(pin: String): Boolean {
        return repository.verifyPin(pin)
    }

    companion object {
        fun factory(repository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(repository) as T
                }
            }
    }
}
