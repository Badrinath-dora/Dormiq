package com.dormiq.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.AuthState
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    phone: String,
    authState: AuthState,
    onVerify: (String) -> Unit,
    onBack: () -> Unit
) {
    val otpLength = 6
    var otp by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }

    val errorMessage = (authState as? AuthState.Error)?.message ?: ""
    val isLoading = authState is AuthState.Loading

    // Countdown timer for resend
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        } else {
            canResend = true
        }
    }

    fun verify() {
        if (otp.length < otpLength) {
            return
        }
        onVerify(otp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DormiqNavy)
    ) {
        // Top gradient decoration with blur blend effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientBlueStart.copy(alpha = 0.4f),
                            GradientBlueStart.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(30.dp))

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.Start)
                    .background(DormiqSurface, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBackIosNew, null, tint = DormiqText)
            }

            Spacer(Modifier.height(32.dp))

            // Lock icon with better glow
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(DormiqBlueGlow, CircleShape)
                    .border(2.dp, Brush.linearGradient(listOf(DormiqBlue, Color.Transparent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(54.dp).background(Color.White.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Verify OTP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DormiqText)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter the 6-digit code sent to\n+91 $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = DormiqTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // OTP Box input
            OtpInputField(
                otp = otp,
                otpLength = otpLength,
                onOtpChanged = {
                    otp = it
                }
            )

            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage, color = DormiqRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(36.dp))

            DormiqPrimaryButton(
                text = "Verify & Continue",
                onClick = ::verify,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.CheckCircle
            )

            Spacer(Modifier.height(24.dp))

            // Resend OTP
            if (canResend) {
                DormiqTextButton(
                    text = "Resend OTP",
                    onClick = {
                        countdown = 30
                        canResend = false
                        otp = ""
                    }
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = DormiqTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Resend in ${countdown}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = DormiqTextMuted
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Real-time hint (replaces demo rules)
            DormiqCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = DormiqSurface.copy(0.5f),
                borderColor = DormiqBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(24.dp).background(DormiqBlueGlow, CircleShape), Alignment.Center) {
                            Icon(Icons.Default.Info, null, tint = DormiqBlue, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Registered Numbers:", style = MaterialTheme.typography.labelMedium, color = DormiqText, fontWeight = FontWeight.Bold)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 34.dp)) {
                        Text("• Owner: 9900112233", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                        Text("• Tenant: 9876543210 (Arjun Singh)", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpInputField(
    otp: String,
    otpLength: Int,
    onOtpChanged: (String) -> Unit
) {
    // Hidden real text field with visual boxes
    Box(contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(otpLength) { index ->
                val char = otp.getOrNull(index)
                val isFocused = index == otp.length
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 52.dp)
                        .background(
                            if (isFocused) DormiqSurface else DormiqCard,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isFocused) DormiqBlue else if (char != null) DormiqBlueDark else DormiqBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (char != null) {
                        Text(
                            char.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DormiqText
                        )
                    }
                    if (isFocused) {
                        // Cursor blink
                        val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                        val alpha by infiniteTransition.animateFloat(
                            0f, 1f,
                            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                            label = "cursorAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(20.dp)
                                .background(DormiqBlue.copy(alpha = alpha))
                        )
                    }
                }
            }
        }

        // Invisible text field for input
        androidx.compose.foundation.text.BasicTextField(
            value = otp,
            onValueChange = { if (it.length <= otpLength && it.all { c -> c.isDigit() }) onOtpChanged(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .matchParentSize()
                .background(androidx.compose.ui.graphics.Color.Transparent),
            decorationBox = { } // Empty decoration — boxes drawn above
        )
    }
}

@Preview
@Composable
fun OtpScreenPreview() {
    DormiqTheme {
        OtpScreen("9876543210", AuthState.Initial, {}, {})
    }
}
