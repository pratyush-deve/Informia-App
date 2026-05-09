package com.pratyush.infoapp

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pratyush.infoapp.di.VaultAppContainer
import com.pratyush.infoapp.ui.navigation.Routes
import com.pratyush.infoapp.ui.theme.InfoAppTheme
import com.pratyush.infoapp.ui.vault.VaultScreen
import com.pratyush.infoapp.ui.vault.VaultViewModel
import com.pratyush.infoapp.ui.vault.editor.CardEditScreen

class MainActivity : ComponentActivity() {
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
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val editorState by viewModel.editorState.collectAsStateWithLifecycle()

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
                }
            }
        }
    }
}
