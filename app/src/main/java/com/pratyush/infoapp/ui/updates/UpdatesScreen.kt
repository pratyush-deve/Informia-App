package com.pratyush.infoapp.ui.updates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    updateVersion: UpdateVersion,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBack: () -> Unit = onComplete
) {
    BackHandler {
        if (showBackButton) {
            onBack()
        } else {
            onComplete()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp
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
                        if (showBackButton) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Navigate back"
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "What's New",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(updateBackgroundBrush())
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            UpdatePager(
                updateVersion = updateVersion,
                onComplete = onComplete,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun updateBackgroundBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.background
        )
    )
}
