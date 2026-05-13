package com.pratyush.infoapp.ui.updates

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UpdateCard(
    page: UpdatePage,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val compact = maxHeight < 560.dp
        val heroSize = if (compact) 176.dp else 218.dp
        val heroCardSize = if (compact) 122.dp else 150.dp
        val iconSize = if (compact) 58.dp else 72.dp
        val titleGap = if (compact) 20.dp else 30.dp
        val featureGap = if (compact) 18.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 14.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            UpdateHeroIllustration(
                page = page,
                heroSize = heroSize,
                cardSize = heroCardSize,
                iconSize = iconSize
            )
            Spacer(Modifier.height(titleGap))
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            if (page.features.isNotEmpty()) {
                Spacer(Modifier.height(featureGap))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    page.features.forEach { feature ->
                        UpdateFeatureRow(
                            feature = feature,
                            accentColor = page.accentColor
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun UpdateHeroIllustration(
    page: UpdatePage,
    heroSize: androidx.compose.ui.unit.Dp,
    cardSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "updateHero")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(heroSize)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(heroSize - 8.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.34f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        FloatingDot(
            color = page.accentColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = (18 + floatOffset * 8).dp, end = 22.dp)
        )
        FloatingDot(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = (24 + floatOffset * 7).dp)
                .alpha(0.82f)
        )
        ElevatedCard(
            modifier = Modifier
                .size(cardSize)
                .padding((floatOffset * 4).dp),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardSize)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                page.accentColor.copy(alpha = 0.24f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.accentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}


@Composable
private fun FloatingDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .background(color.copy(alpha = 0.72f), CircleShape)
    )
}

@Composable
private fun UpdateFeatureRow(
    feature: UpdateFeature,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UpdateIconBubble(
                icon = feature.icon,
                color = accentColor,
                size = 42
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpdateIconBubble(
    icon: ImageVector,
    color: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size((size * 0.54f).dp)
        )
    }
}
