package com.timeline.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    cutoutRadius: Dp = TopAppBarCutoutRadius,
    bottomContentPadding: Dp = 8.dp,
    showNetworkBanner: Boolean = true,
    subscriptionBanner: SubscriptionBanner = SubscriptionBanner.None,
    onProPlanClick: () -> Unit = {},
    expandableContent: (@Composable () -> Unit)? = null
) {
    val containerColor = colors.containerColor
    var isProBannerDismissed by remember { mutableStateOf(false) }
    val bannerState by if (showNetworkBanner) rememberNetworkBannerState(subscriptionBanner, isProBannerDismissed)
    else remember { mutableStateOf(BannerState.Hidden) }

    // Banner already consumed the status bar inset, so don't apply it twice
    val barInsets = if (bannerState != BannerState.Hidden) WindowInsets(0) else windowInsets

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(BottomConcaveBannerShape(cutoutRadius = cutoutRadius)),
        color = containerColor,
        contentColor = colors.titleContentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(bottom = cutoutRadius + bottomContentPadding)
        ) {
            NetworkStatusBanner(
                state = bannerState,
                cutoutRadius = cutoutRadius,
                onProPlanClick = onProPlanClick,
                onDismissProPlan = { isProBannerDismissed = true }
            )

            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = barInsets,
                colors = colors.copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )

            expandableContent?.invoke()
        }
    }
}
