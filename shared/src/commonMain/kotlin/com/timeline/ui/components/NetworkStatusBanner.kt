package com.timeline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.timeline.domain.NetworkMonitor
import com.timeline.domain.NetworkStatus
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val OFFLINE_DEBOUNCE_MS = 1000L
private const val OFFLINE_BANNER_MS = 5000L
private const val ONLINE_BANNER_MS = 3000L

/** Subscription-related banner requested by the caller (decided above the nav host). */
enum class SubscriptionBanner {
    None,
    Upgrade,       // not subscribed: persistent "Go Pro" banner that opens the paywall
    ThankYou,      // just subscribed / subscriber: transient thank-you, never persists
    TrialEnding1,  // 1 day remaining in trial
    TrialEnding2,  // 2 days remaining in trial
    TrialEnding3   // 3 days remaining in trial
}

/** What the banner is actually showing right now. */
enum class BannerState { Hidden, Offline, Online, ProPlan, ThankYou, TrialEnding }

/**
 * Drives the banner. Priority: network states (Offline / Online) > ThankYou > Pro plan.
 * When nothing applies the banner hides.
 */
@Composable
fun rememberNetworkBannerState(
    subscriptionBanner: SubscriptionBanner,
    isProBannerDismissed: Boolean = false,
    networkMonitor: NetworkMonitor = koinInject()
): State<BannerState> {
    val status by networkMonitor.status.collectAsStateWithLifecycle()
    var transient by remember { mutableStateOf(BannerState.Hidden) }
    var wasOffline by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        Logger.d(tag = "NetworkBanner") { "status=$status" }
        when (status) {
            NetworkStatus.DISCONNECTED -> {
                delay(OFFLINE_DEBOUNCE_MS)
                wasOffline = true
                transient = BannerState.Offline
                Logger.d(tag = "NetworkBanner") { "showing Offline" }
                delay(OFFLINE_BANNER_MS)
                transient = BannerState.Hidden
            }
            NetworkStatus.CONNECTED -> {
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

    return remember(subscriptionBanner, isProBannerDismissed) {
        derivedStateOf {
            when {
                transient != BannerState.Hidden -> transient
                subscriptionBanner == SubscriptionBanner.ThankYou -> BannerState.ThankYou
                subscriptionBanner in listOf(
                    SubscriptionBanner.TrialEnding1,
                    SubscriptionBanner.TrialEnding2,
                    SubscriptionBanner.TrialEnding3
                ) -> BannerState.TrialEnding
                subscriptionBanner == SubscriptionBanner.Upgrade && !isProBannerDismissed -> BannerState.ProPlan
                else -> BannerState.Hidden
            }
        }
    }
}

private class LastVisibleBanner {
    var value: BannerState = BannerState.Offline
}

@Composable
fun NetworkStatusBanner(
    state: BannerState,
    modifier: Modifier = Modifier,
    cutoutRadius: Dp = TopAppBarCutoutRadius,
    onProPlanClick: () -> Unit = {},
    onDismissProPlan: () -> Unit = {},
    offlineMessage: String = "Offline • Highlights require an active network",
    onlineMessage: String = "Back online",
    proPlanMessage: String = "Go Pro • Unlock more features",
    thankYouMessage: String = "Thank you for subscribing to Pro!"
) {
    val last = remember { LastVisibleBanner() }
    if (state != BannerState.Hidden) last.value = state
    val current = last.value

    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = state != BannerState.Hidden

    val containerColor by animateColorAsState(
        targetValue = when (current) {
            BannerState.Online -> Color(0xFF81C784)
            BannerState.ProPlan -> Color(0xFFFFD54F)
            BannerState.ThankYou -> MaterialTheme.colorScheme.primaryContainer
            BannerState.TrialEnding -> Color(0xFFFF8F00)
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
            BannerState.TrialEnding -> Color(0xFF1A0A00)
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
            shape = BottomConcaveBannerShape(cutoutRadius),
            color = containerColor,
            contentColor = contentColor
        ) {
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 3 })
                },
                label = "BannerContent",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = current == BannerState.ProPlan || current == BannerState.TrialEnding, onClick = onProPlanClick)
            ) { content ->
                BannerRow(
                    state = content,
                    cutoutRadius = cutoutRadius,
                    offlineMessage = offlineMessage,
                    onlineMessage = onlineMessage,
                    proPlanMessage = proPlanMessage,
                    thankYouMessage = thankYouMessage,
                    onDismissProPlan = onDismissProPlan
                )
            }
        }
    }
}
