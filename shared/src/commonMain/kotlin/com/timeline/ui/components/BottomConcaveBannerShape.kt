package com.timeline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.timeline.domain.NetworkMonitor
import com.timeline.domain.NetworkStatus
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

val TopAppBarCutoutRadius = 24.dp

class BottomConcaveBannerShape(
    private val cutoutRadius: Dp = TopAppBarCutoutRadius
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cutoutRadius.toPx() }.coerceAtMost(size.height / 2f)

        val path = Path().apply {
            reset()
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)

            // Bottom-right concave arc
            quadraticTo(
                size.width,
                size.height - radiusPx,
                size.width - radiusPx,
                size.height - radiusPx
            )

            // Flat baseline where content rests safely above cutout
            lineTo(radiusPx, size.height - radiusPx)

            // Bottom-left concave arc
            quadraticTo(
                0f,
                size.height - radiusPx,
                0f,
                size.height
            )

            close()
        }

        return Outline.Generic(path)
    }
}

private const val OFFLINE_DEBOUNCE_MS = 1000L
private const val OFFLINE_BANNER_MS = 5000L // offline banner shows for 5s, then fades out
private const val ONLINE_BANNER_MS = 3000L

/** Subscription-related banner requested by the caller (decided above the nav host). */
enum class SubscriptionBanner {
    None,
    Upgrade,  // not subscribed: persistent "Go Pro" banner that opens the paywall
    ThankYou  // just subscribed / subscriber: transient thank-you, never persists
}

/** What the banner is actually showing right now. */
enum class BannerState { Hidden, Offline, Online, ProPlan, ThankYou }

/**
 * Drives the banner. Priority: network states (Offline / Online) > ThankYou > Pro plan.
 * When nothing applies the banner hides.
 */
@Composable
fun rememberNetworkBannerState(
    subscriptionBanner: SubscriptionBanner,
    networkMonitor: NetworkMonitor = koinInject()
): State<BannerState> {
    val status by networkMonitor.status.collectAsStateWithLifecycle()
    var transient by remember { mutableStateOf(BannerState.Hidden) }
    var wasOffline by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        Logger.d(tag = "NetworkBanner") { "status=$status" }
        when (status) {
            NetworkStatus.DISCONNECTED -> {
                // Ignore brief handover gaps; if status changes, this is cancelled
                delay(OFFLINE_DEBOUNCE_MS)
                wasOffline = true
                transient = BannerState.Offline
                Logger.d(tag = "NetworkBanner") { "showing Offline" }
                delay(OFFLINE_BANNER_MS)
                // Fades out (or crossfades into the subscription banner). Still offline,
                // but the offline banner is gone until we reconnect.
                transient = BannerState.Hidden
            }
            NetworkStatus.CONNECTED -> {
                // Works even after the offline banner has already faded out
                if (wasOffline) {
                    transient = BannerState.Online
                    Logger.d(tag = "NetworkBanner") { "showing Online" }
                    delay(ONLINE_BANNER_MS)
                    transient = BannerState.Hidden
                    wasOffline = false
                }
            }
        }
    }

    return remember(subscriptionBanner) {
        derivedStateOf {
            when {
                transient != BannerState.Hidden -> transient
                subscriptionBanner == SubscriptionBanner.ThankYou -> BannerState.ThankYou
                subscriptionBanner == SubscriptionBanner.Upgrade -> BannerState.ProPlan
                else -> BannerState.Hidden
            }
        }
    }
}

// Non-observable holder: remembers the last visible state so the banner keeps its
// content and colors while fading out (state is already Hidden at that point).
private class LastVisibleBanner {
    var value: BannerState = BannerState.Offline
}

@Composable
fun NetworkStatusBanner(
    state: BannerState,
    modifier: Modifier = Modifier,
    cutoutRadius: Dp = TopAppBarCutoutRadius,
    onProPlanClick: () -> Unit = {},
    offlineMessage: String = "Offline • AI highlights require an active network",
    onlineMessage: String = "Back online",
    proPlanMessage: String = "Go Pro • Unlock more AI highlights",
    thankYouMessage: String = "Thank you for subscribing to Pro!"
) {
    val last = remember { LastVisibleBanner() }
    if (state != BannerState.Hidden) last.value = state
    val current = last.value

    // visibleState (instead of a plain Boolean) so the banner also animates in when it is
    // already "visible" on first composition, e.g. returning from the paywall after a purchase.
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = state != BannerState.Hidden

    // Colors animate when switching between banners instead of snapping.
    val containerColor by animateColorAsState(
        targetValue = when (current) {
            BannerState.Online -> Color(0xFF81C784)
            BannerState.ProPlan -> Color(0xFFFFD54F)
            BannerState.ThankYou -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(300),
        label = "BannerContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when (current) {
            BannerState.Online -> Color(0xFF1B5E20)
            BannerState.ProPlan -> Color(0xFF3E2723)
            BannerState.ThankYou -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onErrorContainer
        },
        animationSpec = tween(300),
        label = "BannerContentColor"
    )

    AnimatedVisibility(
        visibleState = visibleState,
        enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
        exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                fadeOut(animationSpec = tween(500)),
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = BottomConcaveBannerShape(cutoutRadius), // same concave bottom as the bar
            color = containerColor,
            contentColor = contentColor
        ) {
            // Switching between banners (e.g. Offline -> Go Pro, Go Pro -> Thank you)
            // slides/fades the content inside the same shaped container.
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 3 })
                },
                label = "BannerContent",
                modifier = Modifier
                    .fillMaxWidth()
                    // Clicks live inside the Surface so they are clipped to the concave shape.
                    // Only the Pro banner is tappable.
                    .clickable(enabled = current == BannerState.ProPlan, onClick = onProPlanClick)
            ) { content ->
                BannerRow(
                    state = content,
                    cutoutRadius = cutoutRadius,
                    offlineMessage = offlineMessage,
                    onlineMessage = onlineMessage,
                    proPlanMessage = proPlanMessage,
                    thankYouMessage = thankYouMessage
                )
            }
        }
    }
}

@Composable
private fun BannerRow(
    state: BannerState,
    cutoutRadius: Dp,
    offlineMessage: String,
    onlineMessage: String,
    proPlanMessage: String,
    thankYouMessage: String
) {
    val icon = when (state) {
        BannerState.Online -> Icons.Rounded.Wifi
        BannerState.ProPlan -> Icons.Rounded.WorkspacePremium
        BannerState.ThankYou -> Icons.Rounded.Favorite
        else -> Icons.Rounded.CloudOff
    }
    val message = when (state) {
        BannerState.Online -> onlineMessage
        BannerState.ProPlan -> proPlanMessage
        BannerState.ThankYou -> thankYouMessage
        else -> offlineMessage
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            // Extra bottom padding keeps content above the concave baseline
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp + cutoutRadius
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        if (state == BannerState.ProPlan) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

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
    val bannerState by if (showNetworkBanner) rememberNetworkBannerState(subscriptionBanner)
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
                onProPlanClick = onProPlanClick
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