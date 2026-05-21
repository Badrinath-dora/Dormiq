package com.dormiq.ui.screens.owner

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dormiq.domain.model.FoodMenu
import com.dormiq.domain.model.HostelGallery
import com.dormiq.ui.components.*
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.GalleryFoodViewModel
import java.util.Calendar

// ─── Hostel Gallery Screen ───────────────────────────────────────────────────

@Composable
fun HostelGalleryScreen(
    hostelId: String,
    isOwner: Boolean,
    onBack: () -> Unit,
    viewModel: GalleryFoodViewModel
) {
    val images by viewModel.galleryImages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var newCaption by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    var selectedImageForLightbox by remember { mutableStateOf<HostelGallery?>(null) }

    LaunchedEffect(hostelId) {
        viewModel.loadGallery(hostelId)
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Hostel Gallery", onBack = onBack) },
        floatingActionButton = {
            if (isOwner) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = DormiqBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Add Image")
                }
            }
        }
    ) { padding ->
        if (isLoading && images.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = DormiqBlue)
            }
        } else if (images.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        null,
                        tint = DormiqTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No Images Added Yet", style = MaterialTheme.typography.titleMedium, color = DormiqText)
                    Text("Images uploaded of the hostel will show up here.", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(images) { gallery ->
                    DormiqCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        onClick = { selectedImageForLightbox = gallery }
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(gallery.imageUrl)
                                    .crossfade(true)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .build(),
                                contentDescription = gallery.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Caption Overlay at the bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = gallery.caption,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }

                            // Owner Delete Button
                            if (isOwner) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .background(Color.Red.copy(0.8f), CircleShape)
                                        .clickable { viewModel.deleteGalleryImage(hostelId, gallery.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Lightbox Dialog ---
    selectedImageForLightbox?.let { gallery ->
        Dialog(
            onDismissRequest = { selectedImageForLightbox = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Main Image
                AsyncImage(
                    model = gallery.imageUrl,
                    contentDescription = gallery.caption,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Header Top Bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(gallery.caption, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { selectedImageForLightbox = null },
                        modifier = Modifier.background(Color.DarkGray.copy(0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                }
            }
        }
    }

    // --- Add Image Dialog ---
    if (showAddDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { if (!isLoading) showAddDialog = false },
            title = { Text("Add Hostel Image", color = DormiqText, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedImageUri == null) {
                        // Interactive dotted border upload box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(DormiqNavy.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = DormiqBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    null,
                                    tint = DormiqBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Choose Photo", style = MaterialTheme.typography.bodyMedium, color = DormiqText, fontWeight = FontWeight.Bold)
                                Text("Select from device gallery", style = MaterialTheme.typography.bodySmall, color = DormiqTextSecondary)
                            }
                        }
                    } else {
                        // Visual Image Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Clear Image Button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .clickable { selectedImageUri = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    DormiqTextField(
                        value = newCaption,
                        onValueChange = { newCaption = it },
                        label = "Caption / Category",
                        placeholder = "e.g. Study Room, Mess Hall"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedImageUri != null && newCaption.isNotBlank() && !isLoading,
                    onClick = {
                        val uri = selectedImageUri
                        if (uri != null && newCaption.isNotBlank()) {
                            viewModel.uploadAndAddGalleryImage(
                                context = context,
                                hostelId = hostelId,
                                uri = uri,
                                caption = newCaption,
                                onSuccess = {
                                    selectedImageUri = null
                                    newCaption = ""
                                    showAddDialog = false
                                }
                            )
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DormiqBlue)
                    } else {
                        Text("Upload & Add", color = DormiqBlue, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        selectedImageUri = null
                        newCaption = ""
                        showAddDialog = false
                    }
                ) {
                    Text("Cancel", color = DormiqTextSecondary)
                }
            },
            containerColor = DormiqCard
        )
    }
}

// ─── Food Menu Screen ─────────────────────────────────────────────────────────

@Composable
fun FoodMenuScreen(
    hostelId: String,
    isOwner: Boolean,
    onBack: () -> Unit,
    viewModel: GalleryFoodViewModel
) {
    val menus by viewModel.foodMenu.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    
    // Auto-select current day of week on initial load
    val currentDayIndex = remember {
        val calendar = Calendar.getInstance()
        // Calendar.DAY_OF_WEEK: Sunday is 1, Monday is 2...
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }
    
    var selectedDay by remember { mutableStateOf(days[currentDayIndex]) }
    val activeMenu = menus.find { it.dayOfWeek.equals(selectedDay, ignoreCase = true) } ?: FoodMenu(
        hostelId = hostelId,
        dayOfWeek = selectedDay
    )

    // Edit states for owner
    var isEditing by remember { mutableStateOf(false) }
    var breakfastVal by remember { mutableStateOf("") }
    var lunchVal by remember { mutableStateOf("") }
    var snacksVal by remember { mutableStateOf("") }
    var dinnerVal by remember { mutableStateOf("") }

    LaunchedEffect(hostelId) {
        viewModel.loadFoodMenu(hostelId)
    }

    // Sync input values when selected day or active menu changes
    LaunchedEffect(activeMenu, isEditing) {
        if (!isEditing) {
            breakfastVal = activeMenu.breakfast
            lunchVal = activeMenu.lunch
            snacksVal = activeMenu.snacks
            dinnerVal = activeMenu.dinner
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        topBar = { DormiqTopBar(title = "Weekly Food Menu", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Days Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val isSelected = day == selectedDay
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) DormiqBlue else DormiqCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) DormiqBlue else DormiqBorder,
                                shape = RoundedCornerShape(100.dp)
                            )
                            .clickable {
                                if (isEditing) {
                                    // Save changes before switching day
                                    viewModel.updateDailyMenu(
                                        hostelId,
                                        selectedDay,
                                        breakfastVal,
                                        lunchVal,
                                        snacksVal,
                                        dinnerVal
                                    )
                                    isEditing = false
                                }
                                selectedDay = day
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else DormiqTextSecondary
                        )
                    }
                }
            }

            if (isLoading && menus.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    CircularProgressIndicator(color = DormiqBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Meal card builder helper
                    @Composable
                    fun MealCard(
                        title: String,
                        icon: String,
                        value: String,
                        onValueChange: (String) -> Unit
                    ) {
                        DormiqCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(DormiqBlueGlow, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(icon, fontSize = 24.sp)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DormiqBlue
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    if (isEditing) {
                                        TextField(
                                            value = value,
                                            onValueChange = onValueChange,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedTextColor = DormiqText,
                                                unfocusedTextColor = DormiqText
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = DormiqText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    MealCard("Breakfast", "🍳", breakfastVal) { breakfastVal = it }
                    MealCard("Lunch", "🍛", lunchVal) { lunchVal = it }
                    MealCard("Evening Snacks", "☕", snacksVal) { snacksVal = it }
                    MealCard("Dinner", "🍲", dinnerVal) { dinnerVal = it }

                    Spacer(Modifier.height(24.dp))

                    if (isOwner) {
                        if (isEditing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DormiqPrimaryButton(
                                    text = "Save Daily Menu",
                                    onClick = {
                                        viewModel.updateDailyMenu(
                                            hostelId,
                                            selectedDay,
                                            breakfastVal,
                                            lunchVal,
                                            snacksVal,
                                            dinnerVal
                                        )
                                        isEditing = false
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { isEditing = false },
                                    modifier = Modifier.weight(0.4f)
                                ) {
                                    Text("Cancel", color = DormiqTextSecondary)
                                }
                            }
                        } else {
                            DormiqPrimaryButton(
                                text = "Edit $selectedDay's Menu",
                                onClick = { isEditing = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
