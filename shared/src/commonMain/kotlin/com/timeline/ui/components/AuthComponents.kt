package com.timeline.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.AuthState

@Composable
fun AuthHeader(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Everett",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(100.dp))
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", color = Color.White, fontSize = 40.sp)
                }
            }
        }
    }
}

@Composable
fun AuthForm(
    state: AuthState,
    onSignInGoogle: () -> Unit,
    onSignInApple: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Everett",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Turn your app usage into clarity.",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.7f)),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        AuthButton(text = "Sign in with Google", icon = {}, onClick = onSignInGoogle, enabled = !state.isLoading)
        Spacer(modifier = Modifier.height(16.dp))
        AuthButton(text = "Sign in with Apple", icon = {}, onClick = onSignInApple, enabled = !state.isLoading)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "or", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f)))
        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Email address", color = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(text = "Don't have an account? Sign up", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
    }
}

@Composable
fun AuthButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}
