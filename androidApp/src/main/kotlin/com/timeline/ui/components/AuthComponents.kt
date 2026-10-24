package com.timeline.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.AuthState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings

@Composable
fun AuthHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // Brand Hourglass Icon (Brand Orange)
        Icon(
            imageVector = Icons.Rounded.HourglassEmpty,
            contentDescription = null,
            tint = Color(0xFFE67E22),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
        
        // App Title "Timeline"
        Text(
            text = AppStrings.AppName,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = Color(0xFF2D3436)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Row of 5 app icon cards
        AuthAppIconsRow()
    }
}

@Composable
fun AuthAppIconsRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TikTok, Instagram, WhatsApp, YouTube icon cards
        val iconList = listOf<@Composable () -> Unit>({
            PlaceholderIcons.TiktokIcon(modifier = Modifier.size(Dimensions.IconSmall))
        }, {
            PlaceholderIcons.InstagramIcon(modifier = Modifier.size(Dimensions.IconSmall))
        }, {
            PlaceholderIcons.WhatsappIcon(modifier = Modifier.size(Dimensions.IconSmall))
        }, {
            PlaceholderIcons.YoutubeIcon(modifier = Modifier.size(Dimensions.IconSmall))
        })

        iconList.forEach { iconContent ->
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFF1F2F6))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    iconContent()
                }
            }
        }
        
        // 1 plus icon card
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFF1F2F6))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color(0xFFE67E22),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun AuthForm(
    state: AuthState,
    onSignInGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Google Sign-In Card Button
        Surface(
            onClick = onSignInGoogle,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, Color(0xFFE5E5E5))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                PlaceholderIcons.GoogleIcon()
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = AppStrings.AuthSignInGoogle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2D3436)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // "Don't have an account? Sign in with Google" Link
        Row(
            modifier = Modifier
                .clickable(enabled = !state.isLoading, onClick = onSignInGoogle)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = AppStrings.AuthDontHaveAccount + " ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = AppStrings.AuthSignInWithGoogleLink,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFE67E22)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Privacy Policy Notice
        Text(
            text = "By continuing, you agree to using your data online. Your email, user ID, and name will be connected to your account to improve our service and provide the best of the service.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
