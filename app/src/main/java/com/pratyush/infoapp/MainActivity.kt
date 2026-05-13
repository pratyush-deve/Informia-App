package com.pratyush.infoapp

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.fragment.app.FragmentActivity
import com.pratyush.infoapp.data.settings.SettingsRepository
import com.pratyush.infoapp.di.VaultAppContainer
import com.pratyush.infoapp.ui.navigation.Routes
import com.pratyush.infoapp.ui.settings.AboutScreen
import com.pratyush.infoapp.ui.settings.AppLockOverlay
import com.pratyush.infoapp.ui.settings.PrivacyPreviewOverlay
import com.pratyush.infoapp.ui.settings.SettingsScreen
import com.pratyush.infoapp.ui.settings.SettingsViewModel
import com.pratyush.infoapp.ui.theme.InfoAppTheme
import com.pratyush.infoapp.ui.updates.UpdateRepository
import com.pratyush.infoapp.ui.updates.UpdatesScreen
import com.pratyush.infoapp.ui.vault.VaultScreen
import com.pratyush.infoapp.ui.vault.VaultViewModel
import com.pratyush.infoapp.ui.vault.editor.CardEditScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.parseColor("#101826")),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.parseColor("#090B13"))
        )
        setContent {
            InfoAppTheme {
                val context = LocalContext.current
                val viewModel: VaultViewModel = viewModel(
                    factory = VaultViewModel.factory(VaultAppContainer.repository(context))
                )
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(SettingsRepository.getInstance(context))
                )
                val updateRepository = remember(context) {
                    UpdateRepository.getInstance(context)
                }
                val navController = rememberNavController()
                val lifecycleOwner = LocalLifecycleOwner.current
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val editorState by viewModel.editorState.collectAsStateWithLifecycle()
                val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
                val updateGateState by updateRepository.state.collectAsStateWithLifecycle()
                var appLocked by rememberSaveable { mutableStateOf(false) }
                var privacyPreviewVisible by rememberSaveable { mutableStateOf(false) }
                var updatesVisible by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(settings.hideAppPreview) {
                    if (settings.hideAppPreview) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        privacyPreviewVisible = false
                    }
                }

                LaunchedEffect(Unit) {
                    if (settingsViewModel.lockIfNeeded(System.currentTimeMillis())) {
                        appLocked = true
                    }
                }

                LaunchedEffect(updateGateState.shouldShowAutomatically) {
                    if (updateGateState.shouldShowAutomatically) {
                        updatesVisible = true
                    }
                }

                LaunchedEffect(settings.appLockEnabled, settings.hasAnyUnlockMethod) {
                    if (!settings.appLockEnabled || !settings.hasAnyUnlockMethod) {
                        appLocked = false
                    }
                }

                DisposableEffect(lifecycleOwner, settings.hideAppPreview) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> {
                                if (settings.hideAppPreview) {
                                    privacyPreviewVisible = true
                                }
                            }
                            Lifecycle.Event.ON_RESUME -> {
                                privacyPreviewVisible = false
                            }
                            Lifecycle.Event.ON_STOP -> {
                                settingsViewModel.markBackgroundedAt(System.currentTimeMillis())
                            }
                            Lifecycle.Event.ON_START -> {
                                if (settingsViewModel.lockIfNeeded(System.currentTimeMillis())) {
                                    appLocked = true
                                }
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.VAULT,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        composable(Routes.VAULT) {
                            VaultScreen(
                                uiState = uiState,
                                onCreateCard = {
                                    viewModel.startCreateCard()
                                    navController.navigate(Routes.CARD_EDIT)
                                },
                                onEditCard = { card ->
                                    viewModel.startEditing(card)
                                    navController.navigate(Routes.cardEdit(card.id))
                                },
                                onDeleteCard = { card ->
                                    viewModel.deleteCard(card.id)
                                },
                                onOpenSettings = {
                                    navController.navigate(Routes.SETTINGS) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(
                            route = Routes.CARD_EDIT + "?cardId={cardId}",
                            arguments = listOf(
                                androidx.navigation.navArgument("cardId") {
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val cardId = backStackEntry.arguments?.getString("cardId")?.toLong()

                            LaunchedEffect(cardId) {
                                if (cardId != null && cardId != 0L) {
                                    viewModel.startEditingById(cardId)
                                } else if (editorState == null) {
                                    viewModel.startCreateCard()
                                }
                            }

                            val currentState = editorState
                            if (currentState != null) {
                                CardEditScreen(
                                    navController = navController,
                                    state = currentState,
                                    onSave = {
                                        viewModel.saveEditor {
                                            navController.popBackStack()
                                        }
                                    },
                                    onDismiss = {
                                        viewModel.dismissEditor()
                                        navController.popBackStack()
                                    },
                                    onTitleChange = viewModel::updateTitle,
                                    onIconChange = viewModel::updateIcon,
                                    onGradientChange = viewModel::updateGradient,
                                    onImageChange = viewModel::updateImage,
                                    onAddField = viewModel::addField,
                                    onRemoveField = viewModel::removeField,
                                    onFieldLabelChange = viewModel::updateFieldLabel,
                                    onFieldValueChange = viewModel::updateFieldValue,
                                    onFieldTypeChange = viewModel::updateFieldType
                                )
                            }
                        }

                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                settings = settings,
                                onBack = { navController.popBackStack() },
                                onEnableAppLockWithPin = settingsViewModel::enableAppLockWithPin,
                                onConfigureAppLock = settingsViewModel::configureAppLock,
                                onAppLockEnabledChange = settingsViewModel::setAppLockEnabled,
                                onPinUnlockEnabledChange = settingsViewModel::setPinUnlockEnabled,
                                onBiometricUnlockEnabledChange = settingsViewModel::setBiometricUnlockEnabled,
                                onChangePin = settingsViewModel::changePin,
                                onVerifyPin = settingsViewModel::verifyPin,
                                onAutoLockTimerChange = settingsViewModel::setAutoLockTimer,
                                onHideAppPreviewChange = settingsViewModel::setHideAppPreview,
                                onOpenWhatsNew = {
                                    navController.navigate(Routes.UPDATES) {
                                        launchSingleTop = true
                                    }
                                },
                                onOpenAbout = {
                                    navController.navigate(Routes.ABOUT) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Routes.ABOUT) {
                            AboutScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.UPDATES) {
                            UpdatesScreen(
                                updateVersion = updateGateState.latestUpdate,
                                showBackButton = true,
                                onBack = { navController.popBackStack() },
                                onComplete = {
                                    updateRepository.markCurrentVersionSeen()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    if (updatesVisible) {
                        UpdatesScreen(
                            updateVersion = updateGateState.latestUpdate,
                            onComplete = {
                                updateRepository.markCurrentVersionSeen()
                                updatesVisible = false
                            }
                        )
                    }

                    if (appLocked) {
                        AppLockOverlay(
                            settings = settings,
                            onVerifyPin = settingsViewModel::verifyPin,
                            onUnlocked = { appLocked = false }
                        )
                    }

                    PrivacyPreviewOverlay(
                        visible = privacyPreviewVisible
                    )
                }
            }
        }
    }
}
