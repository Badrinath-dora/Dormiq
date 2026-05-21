package com.dormiq.ui.screens.issues

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.sp
import com.dormiq.domain.model.*
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.dormiq.ui.viewmodel.IssueViewModel
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState

// ─── Issue List ───────────────────────────────────────────────────────────────

@Composable
fun IssueListScreen(
    hostelId: String,
    isOwner: Boolean = true,
    onIssueClick: (issueId: String) -> Unit,
    onReportIssue: () -> Unit,
    onBack: () -> Unit,
    viewModel: IssueViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val issues by viewModel.issues.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var categoryFilter by remember { mutableStateOf("All") }

    LaunchedEffect(hostelId, user?.id) {
        if (isOwner) {
            viewModel.loadIssues(hostelId, true)
        } else {
            user?.id?.let {
                viewModel.loadIssues(hostelId, false, it)
            }
        }
    }

    val filtered = issues.filter { issue ->
        val matchesSearch = (issue.description ?: "").contains(searchQuery, true) || issue.reporterName.contains(searchQuery, true) || (issue.roomNumber ?: "").contains(searchQuery, true)
        val matchesStatus = statusFilter == "All" || issue.status.replace("_", " ").equals(statusFilter, true)
        val matchesCategory = categoryFilter == "All" || issue.category.equals(categoryFilter, true)
        matchesSearch && matchesStatus && matchesCategory
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Issues", onBack = onBack) },
        floatingActionButton = {
            if (!isOwner) {
                ExtendedFloatingActionButton(
                    onClick = onReportIssue,
                    containerColor = DormiqBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Report Issue", color = Color.White) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { padding ->
        if (isLoading && issues.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Status summary bar
                if (isOwner) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("${issues.count { it.status == IssueStatus.OPEN }}", "Open", DormiqRed),
                            Triple("${issues.count { it.status == IssueStatus.IN_PROGRESS }}", "In Progress", DormiqAmber),
                            Triple("${issues.count { it.status == IssueStatus.RESOLVED }}", "Resolved", DormiqGreen),
                        ).forEach { (count, label, color) ->
                            Box(
                                modifier = Modifier.weight(1f)
                                    .background(color.copy(0.1f), RoundedCornerShape(10.dp))
                                    .border(1.dp, color.copy(0.3f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(count, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = DormiqTextSecondary)
                                }
                            }
                        }
                    }
                }

                // Filters
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DormiqTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search issues...",
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            val isSelected = statusFilter == "All"
                            FilterChip(
                                selected = isSelected,
                                onClick = { statusFilter = "All" },
                                label = { 
                                    Text(
                                        "All Status", 
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
                        listOf("Open", "In Progress", "Resolved").forEach { status ->
                            item {
                                val isSelected = statusFilter == status
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { statusFilter = status },
                                    label = { 
                                        Text(
                                            status, 
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
                    }
                    
                    Spacer(Modifier.height(8.dp))
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        EmptyState(Icons.Default.SearchOff, "No issues found", "Try adjusting your filters or search query")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { issue ->
                            IssueCard(issue, onClick = { onIssueClick(issue.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IssueCard(issue: Issue, onClick: () -> Unit) {
    DormiqCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryChip(issue.category)
                StatusChip(issue.status)
            }
            PriorityDot(issue.priority)
        }
        Spacer(Modifier.height(10.dp))
        Text(issue.description ?: "", style = MaterialTheme.typography.bodyMedium, color = DormiqText, maxLines = 2)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.MeetingRoom, null, tint = DormiqTextMuted, modifier = Modifier.size(12.dp))
                Text("Room ${issue.roomNumber} · ${issue.floorName}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
            }
            Text(issue.createdAt.take(10), style = MaterialTheme.typography.bodySmall, color = DormiqTextMuted)
        }
        if (!issue.reporterName.isNullOrEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Person, null, tint = DormiqTextMuted, modifier = Modifier.size(12.dp))
                Text("Reported by ${issue.reporterName}", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
            }
        }
    }
}

@Composable
fun PriorityDot(priority: String) {
    val (color, label) = when (priority) {
        IssuePriority.URGENT -> DormiqRed to "Urgent"
        IssuePriority.HIGH -> DormiqAmber to "High"
        IssuePriority.MEDIUM -> DormiqBlue to "Medium"
        else -> DormiqTextSecondary to "Low"
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ─── Report Issue ─────────────────────────────────────────────────────────────

@Composable
fun ReportIssueScreen(
    hostelId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
    viewModel: IssueViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(IssuePriority.MEDIUM) }
    var descError by remember { mutableStateOf("") }
    var catError by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    val categories = IssueCategory.all

    fun validate() {
        if (hostelId.isEmpty()) {
            catError = "Hostel data not loaded. Please try again."
            return
        }
        catError = if (selectedCategory.isEmpty()) "Select a category" else ""
        descError = if (description.length < 5) "Describe the issue clearly" else ""
        
        if (catError.isEmpty() && descError.isEmpty() && user != null) {
            val issue = Issue(
                id = java.util.UUID.randomUUID().toString(),
                hostelId = hostelId,
                roomId = user.roomId,
                tenantId = user.id,
                reportedById = user.id,
                reporterName = user.name,
                category = selectedCategory,
                description = description,
                status = IssueStatus.OPEN,
                priority = priority,
                roomNumber = user.roomNumber ?: "Unknown",
                floorName = user.bedLabel ?: "Unknown", // Using bed label as secondary info
                createdAt = java.time.LocalDateTime.now().toString(),
                updatedAt = java.time.LocalDateTime.now().toString()
            )
            viewModel.reportIssue(issue) { success ->
                if (success) onSubmitted()
            }
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Report Issue", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Auto-filled room
            DormiqCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MeetingRoom, null, tint = DormiqBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Room ${user?.roomNumber ?: "Not Assigned"} · ${user?.bedLabel ?: "No Bed"}", style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = FontWeight.Medium)
                        Text("Auto-filled from your assignment", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionLabel("Issue Category")
            if (catError.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(catError, color = DormiqRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))

            // Category grid
            val catRows = categories.chunked(3)
            catRows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { cat ->
                        val selected = selectedCategory == cat
                        val (icon, color) = when (cat) {
                            IssueCategory.ELECTRICITY -> Icons.Default.ElectricBolt to DormiqAmber
                            IssueCategory.PLUMBING -> Icons.Default.WaterDrop to DormiqBlue
                            IssueCategory.INTERNET -> Icons.Default.Wifi to DormiqTeal
                            IssueCategory.CLEANING -> Icons.Default.CleaningServices to DormiqGreen
                            IssueCategory.FURNITURE -> Icons.Default.Chair to DormiqPurple
                            IssueCategory.SECURITY -> Icons.Default.Security to DormiqRed
                            else -> Icons.Default.Build to DormiqTextSecondary
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) color.copy(0.15f) else DormiqCard, RoundedCornerShape(10.dp))
                                .border(1.5.dp, if (selected) color else DormiqBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, tint = if (selected) color else DormiqTextSecondary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(cat.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = if (selected) color else DormiqTextSecondary)
                        }
                    }
                    // Fill remaining cells
                    repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Priority
            SectionLabel("Priority")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(IssuePriority.LOW, IssuePriority.MEDIUM, IssuePriority.HIGH, IssuePriority.URGENT).forEach { p ->
                    val (color, _) = when (p) {
                        IssuePriority.URGENT -> DormiqRed to ""
                        IssuePriority.HIGH -> DormiqAmber to ""
                        IssuePriority.MEDIUM -> DormiqBlue to ""
                        else -> DormiqGreen to ""
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (priority == p) color.copy(0.15f) else DormiqCard, RoundedCornerShape(8.dp))
                            .border(1.dp, if (priority == p) color else DormiqBorder, RoundedCornerShape(8.dp))
                            .clickable { priority = p }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(p.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = if (priority == p) color else DormiqTextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description
            SectionLabel("Description")
            Spacer(Modifier.height(10.dp))
            DormiqTextField(
                value = description,
                onValueChange = { description = it },
                label = "Describe the issue *",
                singleLine = false,
                maxLines = 5,
                minLines = 4,
                isError = descError.isNotEmpty(),
                errorText = descError,
                placeholder = "Provide details about the problem..."
            )

            Spacer(Modifier.height(32.dp))

            DormiqPrimaryButton(
                text = if (isLoading) "Submitting..." else "Submit Issue",
                onClick = ::validate,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Send,
                enabled = !isLoading
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Issue Detail ─────────────────────────────────────────────────────────────

@Composable
fun IssueDetailScreen(
    issueId: String,
    isOwner: Boolean = true,
    onUpdateStatus: () -> Unit,
    onBack: () -> Unit,
    viewModel: IssueViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val issue by viewModel.selectedIssue.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val currentUser = (authState as? AuthState.Authenticated)?.user

    LaunchedEffect(issueId) {
        viewModel.loadIssue(issueId)
    }

    var newComment by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = {
            DormiqTopBar(
                title = "Issue Detail",
                onBack = onBack,
                actions = {
                    if (isOwner) {
                        IconButton(onClick = onUpdateStatus) {
                            Icon(Icons.Default.Edit, null, tint = DormiqText)
                        }
                    }
                }
            )
        }
    ) { padding ->
        val issueData = issue
        if (isLoading && issueData == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else if (issueData == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Issue not found", color = DormiqTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Issue header
                item {
                    DormiqCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            CategoryChip(issueData.category)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusChip(issueData.status)
                                PriorityDot(issueData.priority)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            issueData.description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DormiqText
                        )
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = DormiqDivider)
                        Spacer(Modifier.height(12.dp))
                        InfoRow(
                            "Room",
                            "Room ${issueData.roomNumber} · ${issueData.floorName}",
                            Icons.Default.MeetingRoom
                        )
                        InfoRow("Reported by", issueData.reporterName, Icons.Default.Person)
                        InfoRow("Created", issueData.createdAt.take(10), Icons.Default.DateRange)
                        InfoRow(
                            "Last Updated",
                            issueData.updatedAt.take(10),
                            Icons.Default.Update
                        )
                    }
                }

                // Status Timeline
                item {
                    SectionLabel("Status Timeline")
                    Spacer(Modifier.height(8.dp))
                    val createdFormatted = if (issueData.createdAt.length >= 16) {
                        issueData.createdAt.substring(0, 10) + " " + issueData.createdAt.substring(11, 16)
                    } else issueData.createdAt
                    
                    val updatedFormatted = if (issueData.updatedAt.length >= 16) {
                        issueData.updatedAt.substring(0, 10) + " " + issueData.updatedAt.substring(11, 16)
                    } else issueData.updatedAt

                    val isInProgress = issueData.status == IssueStatus.IN_PROGRESS || issueData.status == IssueStatus.RESOLVED || issueData.status == IssueStatus.CLOSED
                    val isResolved = issueData.status == IssueStatus.RESOLVED || issueData.status == IssueStatus.CLOSED

                    val timelineData = listOf(
                        Triple("Issue Reported", createdFormatted, true),
                        Triple("Issue Opened", createdFormatted, true),
                        Triple("In Progress", if (isInProgress && issueData.status == IssueStatus.IN_PROGRESS) updatedFormatted else if (isInProgress) "✓" else "—", isInProgress),
                        Triple("Resolved", if (isResolved) updatedFormatted else "—", isResolved)
                    )
                    
                    timelineData.forEachIndexed { i, (label, time, done) ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier.size(20.dp)
                                        .background(
                                            if (done) DormiqGreen else DormiqCard,
                                            CircleShape
                                        )
                                        .border(
                                            2.dp,
                                            if (done) DormiqGreen else DormiqBorder,
                                            CircleShape
                                        ),
                                    Alignment.Center
                                ) {
                                    if (done) Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = DormiqText,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                if (i < 3) Box(
                                    Modifier.width(2.dp).height(28.dp)
                                        .background(if (done) DormiqGreen.copy(0.4f) else DormiqBorder)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.padding(bottom = if (i < 3) 12.dp else 0.dp)) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (done) DormiqText else DormiqTextMuted,
                                    fontWeight = if (done) FontWeight.Medium else FontWeight.Normal
                                )
                                if (time != "—") Text(
                                    time,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DormiqTextSecondary
                                )
                            }
                        }
                    }
                }

                // Technician Notes
                if (!issueData.technicianNotes.isNullOrEmpty()) {
                    item {
                        SectionLabel("Technician Notes")
                        Spacer(Modifier.height(8.dp))
                        DormiqCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = DormiqAmberGlow
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Build,
                                    null,
                                    tint = DormiqAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    issueData.technicianNotes ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DormiqText
                                )
                            }
                        }
                    }
                }

                // Comments List
                if (comments.isNotEmpty()) {
                    item {
                        SectionLabel("Previous Comments")
                        Spacer(Modifier.height(8.dp))
                    }
                    items(comments) { comment ->
                        CommentItem(comment, isMe = comment.authorId == currentUser?.id)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // Add Comment
                item {
                    SectionLabel("Add Comment")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        DormiqTextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            label = "Type your message...",
                            modifier = Modifier.weight(1f),
                            singleLine = false,
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (newComment.isNotBlank() && currentUser != null) {
                                    val comment = IssueComment(
                                        issueId = issueId,
                                        authorId = currentUser.id,
                                        authorName = currentUser.name,
                                        comment = newComment,
                                        createdAt = java.time.LocalDateTime.now().toString()
                                    )
                                    viewModel.addComment(comment)
                                    newComment = ""
                                }
                            },
                            modifier = Modifier.size(48.dp)
                                .background(DormiqBlue, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                Icons.Default.Send,
                                null,
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }

                // Owner Update Button
                if (isOwner) {
                    item {
                        DormiqPrimaryButton(
                            "Update Status",
                            onUpdateStatus,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Update
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun CommentItem(comment: IssueComment, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Author and Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            if (!isMe) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(DormiqBlue.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = DormiqBlue, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    comment.authorName,
                    style = MaterialTheme.typography.labelSmall,
                    color = DormiqText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
            }
            
            Text(
                text = if (comment.createdAt.length >= 16) comment.createdAt.substring(11, 16) else "",
                style = MaterialTheme.typography.labelSmall,
                color = DormiqTextMuted
            )

            if (isMe) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "You",
                    style = MaterialTheme.typography.labelSmall,
                    color = DormiqBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        // Message bubble
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isMe) DormiqBlue.copy(alpha = 0.15f) else DormiqCard,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    )
                )
                .border(
                    1.dp,
                    if (isMe) DormiqBlue.copy(alpha = 0.3f) else DormiqBorder,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                comment.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = DormiqText,
                lineHeight = 20.sp
            )
        }
    }
}

// ─── Update Issue Status ──────────────────────────────────────────────────────

@Composable
fun UpdateIssueStatusScreen(
    issueId: String,
    onUpdated: () -> Unit,
    onBack: () -> Unit,
    viewModel: IssueViewModel = hiltViewModel()
) {
    val issue by viewModel.selectedIssue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(issueId) {
        viewModel.loadIssue(issueId)
    }

    var selectedStatus by remember { mutableStateOf(IssueStatus.IN_PROGRESS) }
    var techNotes by remember { mutableStateOf("") }
    var estimatedCompletion by remember { mutableStateOf("") }
    var resolutionNote by remember { mutableStateOf("") }

    // Initialize values dynamically when issue data loaded from DB
    LaunchedEffect(issue) {
        issue?.let {
            selectedStatus = it.status
            techNotes = it.technicianNotes ?: ""
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Update Issue", onBack = onBack) }
    ) { padding ->
        val issueData = issue
        if (isLoading && issueData == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else if (issueData == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Issue not found", color = DormiqTextSecondary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Current status
                DormiqCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Update Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DormiqText
                    )
                    Text(
                        issueData.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DormiqTextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        StatusChip(issueData.status)
                        Spacer(Modifier.width(8.dp))
                        CategoryChip(issueData.category)
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel("Update Status")
                Spacer(Modifier.height(10.dp))

                listOf(
                    IssueStatus.OPEN to "Open",
                    IssueStatus.IN_PROGRESS to "In Progress",
                    IssueStatus.RESOLVED to "Resolved",
                    IssueStatus.CLOSED to "Closed",
                ).forEach { (status, label) ->
                    SelectionRow(
                        label = label,
                        isSelected = selectedStatus == status,
                        onClick = { selectedStatus = status }
                    )
                    Spacer(Modifier.height(6.dp))
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("Technician Notes")
                Spacer(Modifier.height(10.dp))
                DormiqTextField(
                    value = techNotes,
                    onValueChange = { techNotes = it },
                    label = "Notes for this update",
                    singleLine = false,
                    maxLines = 4,
                    placeholder = "Describe what action was taken..."
                )

                Spacer(Modifier.height(12.dp))
                DormiqTextField(
                    value = estimatedCompletion,
                    onValueChange = { estimatedCompletion = it },
                    label = "Estimated Completion Date",
                    leadingIcon = Icons.Default.DateRange,
                    placeholder = "DD/MM/YYYY (optional)"
                )

                if (selectedStatus == IssueStatus.RESOLVED || selectedStatus == IssueStatus.CLOSED) {
                    Spacer(Modifier.height(12.dp))
                    DormiqTextField(
                        value = resolutionNote,
                        onValueChange = { resolutionNote = it },
                        label = "Resolution Summary *",
                        singleLine = false,
                        maxLines = 3,
                        placeholder = "Explain how the issue was resolved"
                    )
                }

                Spacer(Modifier.height(32.dp))

                var isSaving by remember { mutableStateOf(false) }

                DormiqPrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Update",
                    onClick = {
                        isSaving = true
                        val notes = if (resolutionNote.isNotBlank()) {
                            "$techNotes\nResolution: $resolutionNote"
                        } else techNotes
                        
                        viewModel.updateIssueStatus(
                            issueId = issueId,
                            status = selectedStatus,
                            notes = notes
                        ) { success ->
                            isSaving = false
                            if (success) {
                                onUpdated()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Check,
                    enabled = !isSaving
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    @Preview
    @Composable
    fun IssueListScreenPreview() {
        DormiqTheme {
            IssueListScreen("hostel-001", true, {}, {}, {})
        }
    }

    @Preview
    @Composable
    fun ReportIssueScreenPreview() {
        DormiqTheme {
            ReportIssueScreen("hostel-001", {}, {})
        }
    }

    @Preview
    @Composable
    fun IssueDetailScreenPreview() {
        DormiqTheme {
            IssueDetailScreen("issue-001", true, {}, {})
        }
    }

    @Preview
    @Composable
    fun UpdateIssueStatusScreenPreview() {
        DormiqTheme {
            UpdateIssueStatusScreen("issue-001", {}, {})
        }
    }
}
