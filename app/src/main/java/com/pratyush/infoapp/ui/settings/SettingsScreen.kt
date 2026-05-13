package com.pratyush.infoapp.ui.settings

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pratyush.infoapp.data.settings.AppSettings
import com.pratyush.infoapp.data.settings.AutoLockTimer

@Composable
private fun settingsBackgroundBrush(): Brush {
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
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onEnableAppLockWithPin: (String) -> Unit,
    onConfigureAppLock: (String?, Boolean, Boolean) -> Unit,
    onAppLockEnabledChange: (Boolean) -> Unit,
    onPinUnlockEnabledChange: (Boolean) -> Unit,
    onBiometricUnlockEnabledChange: (Boolean) -> Unit,
    onChangePin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onAutoLockTimerChange: (AutoLockTimer) -> Unit,
    onHideAppPreviewChange: (Boolean) -> Unit,
    onOpenWhatsNew: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSetupDialog by rememberSaveable { mutableStateOf(false) }
    var showCreatePinDialog by rememberSaveable { mutableStateOf(false) }
    var showChangePinDialog by rememberSaveable { mutableStateOf(false) }
    var showDisableAuthDialog by rememberSaveable { mutableStateOf(false) }
    var showAutoLockPicker by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            SettingsTopBar(
                title = "Settings",
                subtitle = "Privacy, security, and app details",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(settingsBackgroundBrush())
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { SettingsHeader() }
            item {
                SettingsSectionCard(
                    title = "Privacy & Security",
                    icon = Icons.Outlined.Security
                ) {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Lock,
                        title = "App Lock",
                        subtitle = settings.appLockSubtitle(),
                        checked = settings.appLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showSetupDialog = true
                            } else {
                                showDisableAuthDialog = true
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = settings.appLockEnabled,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(140))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SettingsSwitchRow(
                                icon = Icons.Outlined.Password,
                                title = "PIN Unlock",
                                subtitle = if (settings.pinUnlockEnabled && settings.hasPin) {
                                    "Use your Informia PIN to unlock"
                                } else {
                                    "Add a PIN fallback for quick access"
                                },
                                checked = settings.pinUnlockEnabled && settings.hasPin,
                                onCheckedChange = { enabled ->
                                    when {
                                        enabled && settings.hasPin -> onPinUnlockEnabledChange(true)
                                        enabled -> showCreatePinDialog = true
                                        settings.biometricUnlockEnabled -> onPinUnlockEnabledChange(false)
                                        else -> message = "Keep at least one unlock method enabled."
                                    }
                                }
                            )

                            SettingsSwitchRow(
                                icon = Icons.Outlined.Fingerprint,
                                title = "Biometric Unlock",
                                subtitle = "Use fingerprint or face unlock when available",
                                checked = settings.biometricUnlockEnabled,
                                onCheckedChange = { enabled ->
                                    when {
                                        enabled && context.isBiometricAvailable() -> onBiometricUnlockEnabledChange(true)
                                        enabled -> message = "Biometric unlock is not available on this device."
                                        settings.pinUnlockEnabled && settings.hasPin -> onBiometricUnlockEnabledChange(false)
                                        else -> message = "Keep at least one unlock method enabled."
                                    }
                                }
                            )

                            AnimatedVisibility(visible = settings.pinUnlockEnabled && settings.hasPin) {
                                SettingsClickableRow(
                                    icon = Icons.Outlined.Password,
                                    title = "Change PIN",
                                    subtitle = "Verify your current PIN before choosing a new one",
                                    onClick = { showChangePinDialog = true }
                                )
                            }

                            SettingsClickableRow(
                                icon = Icons.Outlined.Timer,
                                title = "Auto-Lock Timer",
                                subtitle = settings.autoLockTimer.label,
                                onClick = { showAutoLockPicker = true }
                            )
                        }
                    }

                    SettingsSwitchRow(
                        icon = Icons.Outlined.VisibilityOff,
                        title = "Hide App Preview in Recents",
                        subtitle = "Blocks screenshots, screen recording, and task previews",
                        checked = settings.hideAppPreview,
                        onCheckedChange = onHideAppPreviewChange
                    )
                }
            }
            item {
                SettingsSectionCard(
                    title = "App",
                    icon = Icons.Outlined.Info
                ) {
                    SettingsClickableRow(
                        icon = Icons.Outlined.NewReleases,
                        title = "What's New",
                        subtitle = "Replay the latest Informia update tour",
                        onClick = onOpenWhatsNew
                    )
                    SettingsClickableRow(
                        icon = Icons.Outlined.Info,
                        title = "About Informia",
                        subtitle = "Version, purpose, developer, and app philosophy",
                        onClick = onOpenAbout
                    )
                }
            }
        }
    }

    if (showSetupDialog) {
        SetupAppLockDialog(
            hasPin = settings.hasPin,
            biometricAvailable = context.isBiometricAvailable(),
            onDismiss = { showSetupDialog = false },
            onConfirm = { pin, pinEnabled, biometricEnabled ->
                onConfigureAppLock(pin, pinEnabled, biometricEnabled)
                showSetupDialog = false
            }
        )
    }

    if (showCreatePinDialog) {
        CreatePinDialog(
            title = "Create PIN Unlock",
            body = "Choose a PIN you can use alongside your other unlock methods.",
            onDismiss = { showCreatePinDialog = false },
            onConfirm = { pin ->
                onEnableAppLockWithPin(pin)
                showCreatePinDialog = false
            }
        )
    }

    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onVerifyCurrentPin = onVerifyPin,
            onChangePin = { newPin ->
                onChangePin(newPin)
                showChangePinDialog = false
            }
        )
    }

    if (showDisableAuthDialog) {
        AuthChallengeDialog(
            title = "Disable App Lock?",
            body = "Authenticate first to turn off Informia's app lock protection.",
            settings = settings,
            autoPromptBiometric = false,
            onVerifyPin = onVerifyPin,
            onAuthenticated = {
                onAppLockEnabledChange(false)
                showDisableAuthDialog = false
            },
            onDismiss = { showDisableAuthDialog = false }
        )
    }

    if (showAutoLockPicker) {
        AutoLockTimerSheet(
            selected = settings.autoLockTimer,
            onDismiss = { showAutoLockPicker = false },
            onSelect = {
                onAutoLockTimerChange(it)
                showAutoLockPicker = false
            }
        )
    }

    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
            title = { Text("App Lock") },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { message = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AppLockOverlay(
    settings: AppSettings,
    onVerifyPin: (String) -> Boolean,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findFragmentActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    var biometricAutoAttempts by rememberSaveable { mutableStateOf(0) }
    var biometricMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var biometricPromptInFlight by remember { mutableStateOf(false) }
    var resumeSignal by rememberSaveable { mutableStateOf(0) }

    fun launchBiometricPrompt(isAutomatic: Boolean) {
        if (
            activity == null ||
            !settings.biometricUnlockEnabled ||
            biometricPromptInFlight
        ) return

        if (isAutomatic) {
            if (biometricAutoAttempts >= MAX_AUTOMATIC_BIOMETRIC_ATTEMPTS) return
            biometricAutoAttempts += 1
        }

        biometricPromptInFlight = true
        biometricMessage = null
        activity.showBiometricPrompt(
            title = "Unlock Informia",
            subtitle = "Confirm it is you",
            onSuccess = {
                biometricPromptInFlight = false
                onUnlocked()
            },
            onFailure = { reason ->
                biometricPromptInFlight = false
                biometricMessage = if (settings.pinUnlockEnabled && settings.hasPin) {
                    "Biometric unlock was not completed. You can enter your PIN instead."
                } else {
                    reason.ifBlank {
                        "Biometric unlock was not completed. Try again when you are ready."
                    }
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeSignal += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(settings.biometricUnlockEnabled, settings.appLockEnabled, activity, resumeSignal) {
        if (
            settings.appLockEnabled &&
            settings.biometricUnlockEnabled &&
            biometricAutoAttempts < MAX_AUTOMATIC_BIOMETRIC_ATTEMPTS
        ) {
            launchBiometricPrompt(isAutomatic = true)
        }
    }

    BackHandler(enabled = true) {
        activity?.moveTaskToBack(true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(settingsBackgroundBrush())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecurityIconBubble(Icons.Outlined.Lock, size = 58)
                Text(
                    text = "Informia is locked",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = settings.unlockInstruction(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                AnimatedVisibility(visible = biometricMessage != null) {
                    Text(
                        text = biometricMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                if (settings.biometricUnlockEnabled) {
                    OutlinedButton(
                        onClick = {
                            launchBiometricPrompt(isAutomatic = false)
                        },
                        enabled = !biometricPromptInFlight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (biometricPromptInFlight) {
                                "Waiting for biometrics..."
                            } else {
                                "Try biometric again"
                            }
                        )
                    }
                }

                if (settings.pinUnlockEnabled && settings.hasPin) {
                    PinUnlockForm(
                        onVerifyPin = onVerifyPin,
                        onUnlocked = onUnlocked
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
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
                    Text(title, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsHeader() {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Private by design",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tune how Informia protects your cards, documents, and previews while keeping daily use quick.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AssistChip(
                onClick = {},
                label = { Text("Local security controls") },
                leadingIcon = {
                    Icon(Icons.Outlined.Security, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
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
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecurityIconBubble(icon, size = 36)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    SettingsRowShell(
        icon = icon,
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    )
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    SettingsRowShell(
        icon = icon,
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = onClick,
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRowShell(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        },
        label = "settingRowContainer"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .alpha(if (enabled) 1f else 0.56f),
        onClick = { if (enabled) onClick?.invoke() },
        enabled = enabled && onClick != null,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SecurityIconBubble(icon, size = 38)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing()
        }
    }
}

@Composable
private fun SecurityIconBubble(icon: ImageVector, size: Int = 48) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size((size * 0.54f).dp)
        )
    }
}

@Composable
private fun PinUnlockForm(
    onVerifyPin: (String) -> Boolean,
    onUnlocked: () -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it.filter(Char::isDigit).take(12)
                error = null
            },
            label = { Text("PIN") },
            singleLine = true,
            isError = error != null,
            supportingText = {
                AnimatedContent(targetState = error, label = "pinError") { message ->
                    Text(message.orEmpty())
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = {
                if (onVerifyPin(pin)) {
                    pin = ""
                    onUnlocked()
                } else {
                    error = "That PIN did not match."
                }
            },
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock with PIN")
        }
    }
}

@Composable
private fun SetupAppLockDialog(
    hasPin: Boolean,
    biometricAvailable: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (pin: String?, pinEnabled: Boolean, biometricEnabled: Boolean) -> Unit
) {
    var pinEnabled by rememberSaveable { mutableStateOf(hasPin) }
    var biometricEnabled by rememberSaveable { mutableStateOf(biometricAvailable) }
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    val needsPin = pinEnabled && !hasPin
    val pinValid = !needsPin || (pin.length >= 4 && pin == confirmPin)
    val canConfirm = (pinEnabled || biometricEnabled) && pinValid
    val error = when {
        needsPin && pin.isNotEmpty() && pin.length < 4 -> "Use at least 4 digits."
        needsPin && confirmPin.isNotEmpty() && pin != confirmPin -> "PINs do not match."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        title = { Text("Set up App Lock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose one or more unlock methods for Informia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CompactSwitchRow(
                    icon = Icons.Outlined.Password,
                    title = "PIN Unlock",
                    subtitle = if (hasPin) "Use your existing PIN" else "Create a new PIN",
                    checked = pinEnabled,
                    onCheckedChange = { pinEnabled = it }
                )
                CompactSwitchRow(
                    icon = Icons.Outlined.Fingerprint,
                    title = "Biometric Unlock",
                    subtitle = if (biometricAvailable) "Use device biometrics" else "Not available on this device",
                    checked = biometricEnabled,
                    onCheckedChange = { biometricEnabled = it },
                    enabled = biometricAvailable
                )
                AnimatedVisibility(visible = needsPin) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it.filter(Char::isDigit).take(12) },
                            label = { Text("PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it.filter(Char::isDigit).take(12) },
                            label = { Text("Confirm PIN") },
                            singleLine = true,
                            isError = error != null,
                            supportingText = { Text(error.orEmpty()) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                    }
                }
                if (!pinEnabled && !biometricEnabled) {
                    Text(
                        text = "Select at least one unlock method.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = { onConfirm(if (needsPin) pin else null, pinEnabled, biometricEnabled) }
            ) {
                Text("Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CreatePinDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    val error = when {
        pin.isNotEmpty() && pin.length < 4 -> "Use at least 4 digits."
        confirmPin.isNotEmpty() && pin != confirmPin -> "PINs do not match."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Password, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(12) },
                    label = { Text("New PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it.filter(Char::isDigit).take(12) },
                    label = { Text("Confirm PIN") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { Text(error.orEmpty()) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = pin.length >= 4 && pin == confirmPin,
                onClick = { onConfirm(pin) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangePinDialog(
    onDismiss: () -> Unit,
    onVerifyCurrentPin: (String) -> Boolean,
    onChangePin: (String) -> Unit
) {
    var currentPin by rememberSaveable { mutableStateOf("") }
    var currentVerified by rememberSaveable { mutableStateOf(false) }
    var newPin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val newPinError = when {
        newPin.isNotEmpty() && newPin.length < 4 -> "Use at least 4 digits."
        confirmPin.isNotEmpty() && newPin != confirmPin -> "PINs do not match."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Password, contentDescription = null) },
        title = { Text("Change PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedContent(targetState = currentVerified, label = "pinStep") { verified ->
                    if (!verified) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Enter your current PIN before choosing a new one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = currentPin,
                                onValueChange = {
                                    currentPin = it.filter(Char::isDigit).take(12)
                                    error = null
                                },
                                label = { Text("Current PIN") },
                                singleLine = true,
                                isError = error != null,
                                supportingText = { Text(error.orEmpty()) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Current PIN verified. Choose your new PIN.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { newPin = it.filter(Char::isDigit).take(12) },
                                label = { Text("New PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                            )
                            OutlinedTextField(
                                value = confirmPin,
                                onValueChange = { confirmPin = it.filter(Char::isDigit).take(12) },
                                label = { Text("Confirm New PIN") },
                                singleLine = true,
                                isError = newPinError != null,
                                supportingText = { Text(newPinError.orEmpty()) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!currentVerified) {
                TextButton(
                    enabled = currentPin.length >= 4,
                    onClick = {
                        if (onVerifyCurrentPin(currentPin)) {
                            currentVerified = true
                            error = null
                        } else {
                            error = "Current PIN is incorrect."
                        }
                    }
                ) {
                    Text("Continue")
                }
            } else {
                TextButton(
                    enabled = newPin.length >= 4 && newPin == confirmPin,
                    onClick = { onChangePin(newPin) }
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AuthChallengeDialog(
    title: String,
    body: String,
    settings: AppSettings,
    autoPromptBiometric: Boolean,
    onVerifyPin: (String) -> Boolean,
    onAuthenticated: () -> Unit,
    onDismiss: () -> Unit
) {
    val activity = LocalContext.current.findFragmentActivity()
    var biometricAttempted by rememberSaveable { mutableStateOf(false) }
    var biometricMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(settings.biometricUnlockEnabled) {
        if (autoPromptBiometric && settings.biometricUnlockEnabled && !biometricAttempted && activity != null) {
            biometricAttempted = true
            activity.showBiometricPrompt(
                title = title,
                subtitle = "Confirm it is you",
                onSuccess = onAuthenticated,
                onFailure = { biometricMessage = "Biometric authentication was not completed." }
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedVisibility(visible = biometricMessage != null) {
                    Text(
                        text = biometricMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (settings.biometricUnlockEnabled) {
                    OutlinedButton(
                        onClick = {
                            biometricMessage = null
                            activity?.showBiometricPrompt(
                                title = title,
                                subtitle = "Confirm it is you",
                                onSuccess = onAuthenticated,
                                onFailure = { biometricMessage = "Biometric authentication was not completed." }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use biometric unlock")
                    }
                }
                if (settings.pinUnlockEnabled && settings.hasPin) {
                    PinUnlockForm(
                        onVerifyPin = onVerifyPin,
                        onUnlocked = onAuthenticated
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CompactSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.56f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SecurityIconBubble(icon, size = 34)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoLockTimerSheet(
    selected: AutoLockTimer,
    onDismiss: () -> Unit,
    onSelect: (AutoLockTimer) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Auto-Lock Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Informia asks for unlock only after this timeout when returning from background.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AutoLockTimer.entries.forEach { timer ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(timer) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected == timer) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
                    },
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timer.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected == timer) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

private fun AppSettings.appLockSubtitle(): String {
    return if (!appLockEnabled) {
        "Require authentication after the auto-lock timer"
    } else {
        val methods = buildList {
            if (pinUnlockEnabled && hasPin) add("PIN")
            if (biometricUnlockEnabled) add("biometrics")
        }
        "Enabled with ${methods.joinToString(" and ")}"
    }
}

private fun AppSettings.unlockInstruction(): String {
    return when {
        pinUnlockEnabled && hasPin && biometricUnlockEnabled ->
            "Use biometrics or enter your PIN to continue."
        biometricUnlockEnabled ->
            "Use biometrics to continue."
        else ->
            "Enter your PIN to continue."
    }
}

private const val MAX_AUTOMATIC_BIOMETRIC_ATTEMPTS = 2

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    return findActivity() as? FragmentActivity
}

private fun Context.isBiometricAvailable(): Boolean {
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
    return BiometricManager.from(this).canAuthenticate(authenticators) ==
        BiometricManager.BIOMETRIC_SUCCESS
}

private fun FragmentActivity.showBiometricPrompt(
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    if (!isBiometricAvailable()) {
        onFailure("Biometric unlock is not available on this device.")
        return
    }

    val prompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onFailure("Biometric check did not match.")
            }
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Use PIN")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()
    prompt.authenticate(info)
}
