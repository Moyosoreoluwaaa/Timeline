package com.timeline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timeline.ui.theme.Dimensions
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.*

@Composable
fun ImageAppIcon(
    resource: DrawableResource,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(Dimensions.IconSmall)
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

object PlaceholderIcons {
    @Composable
    fun GoogleIcon(modifier: Modifier = Modifier.size(Dimensions.IconExtraLarge)) {
        ImageAppIcon(
            resource = Res.drawable.google_logo,
            contentDescription = "Google Icon",
            modifier = modifier
        )
    }

    @Composable
    fun YoutubeIcon(modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = Res.drawable.youtube__,
            contentDescription = "YouTube Icon",
            modifier = modifier
        )
    }

    @Composable
    fun WhatsappIcon(modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = Res.drawable.whatsapp___,
            contentDescription = "WhatsApp Icon",
            modifier = modifier
        )
    }

    @Composable
    fun TiktokIcon(modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = Res.drawable.tik_tok___,
            contentDescription = "TikTok Icon",
            modifier = modifier
        )
    }

    @Composable
    fun InstagramIcon(modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = Res.drawable.instagram___,
            contentDescription = "Instagram Icon",
            modifier = modifier
        )
    }
}
