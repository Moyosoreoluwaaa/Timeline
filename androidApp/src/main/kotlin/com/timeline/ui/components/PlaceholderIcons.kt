package com.timeline.ui.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.timeline.R
import com.timeline.ui.theme.Dimensions

@Composable
fun ImageAppIcon(
    @DrawableRes resource: Int,
    contentDescription: String? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(Dimensions.IconSmall)
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

object PlaceholderIcons {
    @Composable
    fun GoogleIcon(@SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(Dimensions.IconExtraLarge)) {
        ImageAppIcon(
            resource = R.drawable.google_logo,
            contentDescription = "Google Icon",
            modifier = modifier
        )
    }

    @Composable
    fun YoutubeIcon(@SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = R.drawable.youtube__,
            contentDescription = "YouTube Icon",
            modifier = modifier
        )
    }

    @Composable
    fun WhatsappIcon(@SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = R.drawable.whatsapp___,
            contentDescription = "WhatsApp Icon",
            modifier = modifier
        )
    }

    @Composable
    fun TiktokIcon(@SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = R.drawable.tik_tok___,
            contentDescription = "TikTok Icon",
            modifier = modifier
        )
    }

    @Composable
    fun InstagramIcon(@SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(Dimensions.IconSmall)) {
        ImageAppIcon(
            resource = R.drawable.instagram___,
            contentDescription = "Instagram Icon",
            modifier = modifier
        )
    }
}
