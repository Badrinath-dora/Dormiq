package com.dormiq.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.dormiq.domain.model.UserRole
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOwnerHome: () -> Unit,
    onNavigateToTenantHome: () -> Unit
) {
    // Check session on start
    val authState by hiltViewModel<AuthViewModel>().authState.collectAsState()

    LaunchedEffect(authState) {
        delay(2000) // Keep splash for a bit for branding
        if (authState is AuthState.Authenticated) {
            val user = (authState as AuthState.Authenticated).user
            if (user.role == UserRole.OWNER) {
                onNavigateToOwnerHome()
            } else {
                onNavigateToTenantHome()
            }
        } else if (authState is AuthState.NotAuthenticated || authState is AuthState.Error) {
            onNavigateToLogin()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DormiqBlueDark.copy(alpha = 0.4f),
                        DormiqNavy
                    ),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Glow ring
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(DormiqBlue.copy(alpha = glowAlpha * 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(
                            Brush.linearGradient(
                                listOf(GradientBlueStart, GradientBlueEnd),
                                start = Offset(0f, 0f),
                                end = Offset(100f, 100f)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Apartment,
                        contentDescription = null,
                        tint = DormiqText,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "DORMIQ",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DormiqText,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Smart Hostel Management",
                style = MaterialTheme.typography.bodyMedium,
                color = DormiqTextSecondary
            )

            Spacer(Modifier.height(60.dp))

            CircularProgressIndicator(
                color = DormiqBlue,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    DormiqTheme {
        SplashScreen({}, {}, {})
    }
}
