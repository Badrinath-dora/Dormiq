package com.dormiq.ui.screens.notices

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*

// ─── Notice Board ─────────────────────────────────────────────────────────────

import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

// ─── Notice Board ─────────────────────────────────────────────────────────────

@Composable
fun NoticeBoardScreen(
    hostelId: String,
    isOwner: Boolean = true,
    onCreateNotice: () -> Unit,
    onBack: () -> Unit,
    viewModel: NoticeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val notices by viewModel.notices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "General", "Maintenance", "Reminder", "Emergency")

    LaunchedEffect(hostelId) {
        viewModel.loadNotices(hostelId)
    }

    val filtered = notices.filter { 
        (isOwner || it.isPublished) && 
        (selectedCategory == "All" || it.category.equals(selectedCategory, true))
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Notice Board", onBack = onBack) },
        floatingActionButton = {
            if (isOwner) {
                ExtendedFloatingActionButton(
                    onClick = onCreateNotice,
                    containerColor = DormiqBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Post Notice", color = Color.White) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { padding ->
        if (isLoading && notices.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Category filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { 
                                Text(
                                    cat, 
                                    color = if (isSelected) Color.White else DormiqTextSecondary
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DormiqBlue,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = DormiqBorder,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        EmptyState(Icons.Default.Campaign, "No notices", if (isOwner) "You haven't posted any announcements yet" else "There are no announcements for this category")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { notice ->
                            NoticeCard(notice = notice, isOwner = isOwner)
                        }
                        item { Spacer(Modifier.height(90.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeCard(notice: Notice, isOwner: Boolean) {
    val (icon, color, bgColor) = when (notice.category) {
        NoticeCategory.MAINTENANCE -> Triple(Icons.Default.Build, DormiqAmber, DormiqAmberGlow)
        NoticeCategory.REMINDER -> Triple(Icons.Default.Notifications, DormiqBlue, DormiqBlueGlow)
        NoticeCategory.EMERGENCY -> Triple(Icons.Default.Warning, DormiqRed, DormiqRedGlow)
        else -> Triple(Icons.Default.Campaign, DormiqTeal, DormiqTealGlow)
    }

    DormiqCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (notice.category == NoticeCategory.EMERGENCY) DormiqRedGlow else Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notice.title, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold, 
                        color = DormiqText,
                        modifier = Modifier.weight(1f)
                    )
                    if (isOwner && !notice.isPublished) {
                        Text(
                            "DRAFT",
                            style = MaterialTheme.typography.labelSmall,
                            color = DormiqRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(DormiqRedGlow, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                StatusChip(notice.category)
                Spacer(Modifier.height(6.dp))
                Text(notice.body, style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary, maxLines = 3)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Person, null, tint = DormiqTextMuted, modifier = Modifier.size(12.dp))
                        Text(notice.createdByName, style = MaterialTheme.typography.labelSmall, color = DormiqTextMuted)
                    }
                    Text(notice.publishedAt?.take(10) ?: "Just now", style = MaterialTheme.typography.labelSmall, color = DormiqTextMuted)
                }
            }
        }
    }
}

// ─── Create Notice ────────────────────────────────────────────────────────────

@Composable
fun CreateNoticeScreen(
    hostelId: String,
    onPublished: () -> Unit,
    onBack: () -> Unit,
    viewModel: NoticeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(NoticeCategory.GENERAL) }
    var audience by remember { mutableStateOf(NoticeAudience.ALL) }
    var titleError by remember { mutableStateOf("") }
    var bodyError by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val categories = listOf(
        NoticeCategory.GENERAL to "General",
        NoticeCategory.MAINTENANCE to "Maintenance",
        NoticeCategory.REMINDER to "Reminder",
        NoticeCategory.EMERGENCY to "Emergency"
    )

    fun handleSave(isPublished: Boolean) {
        if (hostelId.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Error: Hostel identity not found. Please restart the app.")
            }
            return
        }
        
        titleError = if (title.isBlank()) "Title is required" else ""
        bodyError = if (body.length < 10) "Write a proper notice body" else ""
        
        if (titleError.isEmpty() && bodyError.isEmpty() && user != null) {
            val notice = Notice(
                id = java.util.UUID.randomUUID().toString(),
                hostelId = hostelId,
                title = title,
                body = body,
                category = category,
                targetAudience = audience,
                publishedAt = if (isPublished) java.time.LocalDateTime.now().toString() else null,
                createdById = user.id,
                createdByName = user.name,
                isPublished = isPublished
            )
            println("DEBUG: Notice being sent from UI: $notice")
            viewModel.createNotice(notice) { success ->
                if (success) {
                    onPublished()
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to ${if (isPublished) "publish" else "save"}. ID: ${hostelId.take(5)}... / ${user.id.take(5)}...")
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Create Notice", onBack = onBack) },
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DormiqCard,
                    contentColor = DormiqText,
                    actionColor = DormiqBlue
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionLabel("Notice Details")
            Spacer(Modifier.height(12.dp))

            DormiqTextField(
                value = title,
                onValueChange = { title = it },
                label = "Notice Title *",
                leadingIcon = Icons.Default.Title,
                isError = titleError.isNotEmpty(),
                errorText = titleError,
                placeholder = "e.g., Water Supply Disruption"
            )

            Spacer(Modifier.height(12.dp))

            DormiqTextField(
                value = body,
                onValueChange = { body = it },
                label = "Notice Body *",
                singleLine = false,
                maxLines = 8,
                minLines = 5,
                isError = bodyError.isNotEmpty(),
                errorText = bodyError,
                placeholder = "As today is festival..."
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("Category")
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { (cat, label) ->
                    val selected = category == cat
                    val color = when(cat) {
                        NoticeCategory.EMERGENCY -> DormiqRed
                        NoticeCategory.MAINTENANCE -> DormiqAmber
                        NoticeCategory.REMINDER -> DormiqBlue
                        else -> DormiqTeal
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selected) color.copy(0.15f) else DormiqCard, RoundedCornerShape(10.dp))
                            .border(1.dp, if (selected) color else DormiqBorder, RoundedCornerShape(10.dp))
                            .clickable { category = cat }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = if (selected) color else DormiqTextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionLabel("Target Audience")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(NoticeAudience.ALL to "All Tenants", NoticeAudience.FLOOR to "By Floor", NoticeAudience.ROOM to "By Room").forEach { (aud, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (audience == aud) DormiqBlueGlow else DormiqCard, RoundedCornerShape(10.dp))
                            .border(1.dp, if (audience == aud) DormiqBlue else DormiqBorder, RoundedCornerShape(10.dp))
                            .clickable { audience = aud }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = if (audience == aud) DormiqBlue else DormiqTextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            DormiqPrimaryButton(
                text = "Publish Notice", 
                onClick = { handleSave(true) }, 
                modifier = Modifier.fillMaxWidth(), 
                icon = Icons.Default.Publish,
                isLoading = isLoading
            )
            Spacer(Modifier.height(12.dp))
            DormiqSecondaryButton(
                text = "Save as Draft", 
                onClick = { handleSave(false) }, 
                modifier = Modifier.fillMaxWidth(), 
                icon = Icons.Default.Save,
                enabled = !isLoading
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
fun NoticeBoardScreenPreview() {
    DormiqTheme {
        NoticeBoardScreen("hostel-001", true, {}, {})
    }
}

@Preview
@Composable
fun CreateNoticeScreenPreview() {
    DormiqTheme {
        CreateNoticeScreen("hostel-001", {}, {})
    }
}
