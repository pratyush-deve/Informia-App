package com.pratyush.infoapp.ui.updates

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun UpdatePager(
    updateVersion: UpdateVersion,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { updateVersion.pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == updateVersion.pages.lastIndex

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            UpdateCard(
                page = updateVersion.pages[pageIndex],
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(onClick = onComplete) {
                    Text("Skip")
                }
            }
            PageIndicators(
                pagerState = pagerState,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "updateButton"
                ) { finalPage ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(if (finalPage) "Let's Go" else "Next")
                        Icon(
                            imageVector = if (finalPage) {
                                Icons.Outlined.CheckCircle
                            } else {
                                Icons.AutoMirrored.Outlined.ArrowForward
                            },
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicators(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { index ->
            val selected = pagerState.currentPage == index
            val width = animateDpAsState(
                targetValue = if (selected) 24.dp else 8.dp,
                label = "indicatorWidth"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(width.value)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
                        }
                    )
            )
        }
    }
}
