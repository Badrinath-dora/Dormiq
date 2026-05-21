package com.dormiq.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.dormiq.ui.theme.*

// ─── Dormiq Card ─────────────────────────────────────────────────────────────

@Composable
fun DormiqCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glowColor: Color = Color.Transparent,
    containerColor: Color = DormiqCard,
    borderColor: Color = DormiqBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val cardModifier = modifier
        .then(
            if (glowColor != Color.Transparent) {
                Modifier.shadow(
                    elevation = 4.dp,
                    shape = shape
                )
            } else Modifier
        )
        .background(containerColor, shape)
        .border(1.dp, borderColor, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .background(DormiqCard, shape)
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(glowColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DormiqTextSecondary
            )
        }
    }
}

// ─── Dormiq Button ───────────────────────────────────────────────────────────

@Composable
fun DormiqPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DormiqBlue,
            contentColor = Color.White,
            disabledContainerColor = if (isLoading) DormiqBlue.copy(alpha = 0.7f) else DormiqCard,
            disabledContentColor = if (isLoading) Color.White else DormiqTextMuted
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
fun DormiqSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DormiqBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DormiqBlue,
            disabledContentColor = DormiqTextMuted
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DormiqTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = DormiqBlue, style = MaterialTheme.typography.labelLarge)
    }
}

// ─── Dormiq TextField ────────────────────────────────────────────────────────

@Composable
fun DormiqTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Next,
    maxLines: Int = 1,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder, color = DormiqTextMuted) }} else null,
            leadingIcon = if (leadingIcon != null) {{ Icon(leadingIcon, contentDescription = null, tint = DormiqTextSecondary) }} else null,
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DormiqBlue,
                unfocusedBorderColor = DormiqBorder,
                errorBorderColor = DormiqRed,
                focusedLabelColor = DormiqBlue,
                unfocusedLabelColor = DormiqTextSecondary,
                cursorColor = DormiqBlue,
                focusedTextColor = DormiqText,
                unfocusedTextColor = DormiqText,
                disabledTextColor = DormiqTextMuted,
                unfocusedContainerColor = DormiqSurface,
                focusedContainerColor = DormiqSurface,
                disabledContainerColor = DormiqCard
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorText.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorText,
                color = DormiqRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ─── Status Chip ─────────────────────────────────────────────────────────────

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, bgColor, label) = when (status.lowercase()) {
        "vacant" -> Triple(DormiqGreen, DormiqGreenGlow, "Vacant")
        "occupied" -> Triple(DormiqBlue, DormiqBlueGlow, "Occupied")
        "blocked" -> Triple(DormiqAmber, DormiqAmberGlow, "Blocked")
        "maintenance" -> Triple(DormiqRed, DormiqRedGlow, "Maintenance")
        "open" -> Triple(DormiqRed, DormiqRedGlow, "Open")
        "in_progress" -> Triple(DormiqAmber, DormiqAmberGlow, "In Progress")
        "resolved" -> Triple(DormiqGreen, DormiqGreenGlow, "Resolved")
        "closed" -> Triple(DormiqTextSecondary, DormiqCard, "Closed")
        "active" -> Triple(DormiqGreen, DormiqGreenGlow, "Active")
        "checked_out" -> Triple(DormiqTextSecondary, DormiqCard, "Checked Out")
        "pending" -> Triple(DormiqAmber, DormiqAmberGlow, "Pending")
        "general" -> Triple(DormiqBlue, DormiqBlueGlow, "General")
        "reminder" -> Triple(DormiqAmber, DormiqAmberGlow, "Reminder")
        "emergency" -> Triple(DormiqRed, DormiqRedGlow, "Emergency")
        else -> Triple(DormiqTextSecondary, DormiqCard, status.replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Category Chip ────────────────────────────────────────────────────────────

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (category.lowercase()) {
        "electricity" -> Pair(Icons.Default.ElectricBolt, DormiqAmber)
        "plumbing" -> Pair(Icons.Default.WaterDrop, DormiqBlue)
        "internet" -> Pair(Icons.Default.Wifi, DormiqTeal)
        "cleaning" -> Pair(Icons.Default.CleaningServices, DormiqGreen)
        "furniture" -> Pair(Icons.Default.Chair, DormiqPurple)
        "security" -> Pair(Icons.Default.Security, DormiqRed)
        else -> Pair(Icons.Default.Build, DormiqTextSecondary)
    }

    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            text = category.replaceFirstChar { it.uppercase() },
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Dormiq Top Bar ───────────────────────────────────────────────────────────

@Composable
fun DormiqTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = DormiqNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp)
        ) {
            // Back Button and Title
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = DormiqText, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DormiqText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Actions
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = DormiqText
        )
        if (action != null && onAction != null) {
            DormiqTextButton(text = action, onClick = onAction)
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(DormiqBlueGlow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = DormiqBlue, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = DormiqText, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = DormiqTextSecondary)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            DormiqPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

// ─── Info Row ─────────────────────────────────────────────────────────────────

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = DormiqBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = FontWeight.Medium)
    }
}

// ─── Gradient Header ─────────────────────────────────────────────────────────

@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientBlueStart, GradientBlueEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        content = content
    )
}

// ─── Occupancy Progress Bar ───────────────────────────────────────────────────

@Composable
fun OccupancyBar(
    percent: Float,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 100f),
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "occupancy"
    )

    val color = when {
        percent > 85f -> DormiqRed
        percent > 60f -> DormiqAmber
        else -> DormiqGreen
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Occupancy", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
            Text("${animatedPercent.toInt()}%", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedPercent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = DormiqBorder
        )
    }
}

// ─── Avatar Circle ────────────────────────────────────────────────────────────

@Composable
fun AvatarCircle(
    name: String,
    size: Dp = 44.dp,
    backgroundColor: Color = DormiqBlueGlow,
    textColor: Color = DormiqBlue
) {
    val initials = name.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
    Box(
        modifier = Modifier.size(size).background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.32f).sp
        )
    }
}

// ─── Bed Grid Item ────────────────────────────────────────────────────────────

@Composable
fun BedGridItem(
    label: String,
    status: String,
    tenantName: String = "",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (color, bgColor, icon) = when (status) {
        "occupied" -> Triple(DormiqBlue, DormiqBlueGlow, Icons.Default.Person)
        "vacant" -> Triple(DormiqGreen, DormiqGreenGlow, Icons.Outlined.Bed)
        "blocked" -> Triple(DormiqAmber, DormiqAmberGlow, Icons.Default.Block)
        else -> Triple(DormiqRed, DormiqRedGlow, Icons.Default.Build)
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        if (tenantName.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                tenantName.split(" ").firstOrNull() ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = DormiqTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Shimmer Loading ─────────────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(modifier = modifier.background(DormiqCard.copy(alpha = shimmerAlpha), shape))
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0A0F1E)
@Composable
fun ComponentsPreview() {
    DormiqTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Cards", style = MaterialTheme.typography.titleLarge, color = DormiqText)
            DormiqCard(modifier = Modifier.fillMaxWidth()) {
                Text("Standard Card", color = DormiqText)
            }
            DormiqCard(modifier = Modifier.fillMaxWidth(), glowColor = DormiqBlueGlow) {
                Text("Glowing Card", color = DormiqText)
            }

            Text("Stats", style = MaterialTheme.typography.titleLarge, color = DormiqText)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Tenants",
                    value = "44",
                    icon = Icons.Default.People,
                    color = DormiqBlue,
                    glowColor = DormiqBlueGlow,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Issues",
                    value = "3",
                    icon = Icons.Default.ReportProblem,
                    color = DormiqRed,
                    glowColor = DormiqRedGlow,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Buttons", style = MaterialTheme.typography.titleLarge, color = DormiqText)
            DormiqPrimaryButton("Primary Button", {}, modifier = Modifier.fillMaxWidth())
            DormiqSecondaryButton("Secondary Button", {}, modifier = Modifier.fillMaxWidth())
            DormiqPrimaryButton("Loading...", {}, isLoading = true, modifier = Modifier.fillMaxWidth())

            Text("Chips", style = MaterialTheme.typography.titleLarge, color = DormiqText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("vacant")
                StatusChip("occupied")
                StatusChip("maintenance")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip("electricity")
                CategoryChip("plumbing")
            }

            Text("Input", style = MaterialTheme.typography.titleLarge, color = DormiqText)
            DormiqTextField(value = "", onValueChange = {}, label = "Enter Name", placeholder = "John Doe")
            DormiqTextField(value = "Error Input", onValueChange = {}, label = "Label", isError = true, errorText = "Field is required")

            Text("Avatar", style = MaterialTheme.typography.titleLarge, color = DormiqText)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarCircle("Arjun Singh")
                AvatarCircle("Rahul", size = 60.dp)
            }
        }
    }
}
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(20.dp).background(DormiqBlue, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DormiqText)
    }
}

@Composable
fun SelectionRow(label: String, isSelected: Boolean, onClick: () -> Unit, subtitle: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(if (isSelected) DormiqBlueGlow else DormiqSurface, RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) DormiqBlue else DormiqBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = DormiqBlue, modifier = Modifier.size(20.dp))
        } else {
            Icon(Icons.Default.RadioButtonUnchecked, null, tint = DormiqBorder, modifier = Modifier.size(20.dp))
        }
    }
}
