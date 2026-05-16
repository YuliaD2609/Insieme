package com.insieme.app.ui.activities

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.insieme.app.data.model.Activity
import com.insieme.app.data.model.ActivityStatus
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.components.GroupWarningCard
import com.insieme.app.ui.viewmodel.InsiemeViewModel
import com.insieme.app.ui.viewmodel.SortOrder

import com.insieme.app.ui.theme.*
import com.insieme.app.ui.components.DuckIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: InsiemeViewModel,
    onNavigateToProfile: () -> Unit
) {
    val activities by viewModel.activities.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val groupSize by viewModel.groupSize.collectAsState()
    val userImages by viewModel.userImages.collectAsState()
    val idToName by viewModel.idToName.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<Activity?>(null) }
    
    val primaryColor = PastelGreen

    val todoActivities = activities.filter { it.status == ActivityStatus.TODO }
    val doneActivities = activities.filter { it.status == ActivityStatus.DONE }

    val homeActivities = todoActivities.filter { it.isAtHome }
    val awayActivities = todoActivities.filter { !it.isAtHome }
        .groupBy { it.locationDetail.trim().lowercase() }

    if (showCreateDialog) {
        ActivityDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newActivity -> viewModel.addActivity(newActivity); showCreateDialog = false }
        )
    }

    if (activityToEdit != null) {
        ActivityDialog(activity = activityToEdit, onDismiss = { activityToEdit = null }, onSave = { viewModel.updateActivity(it); activityToEdit = null })
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Decorative background elements
        DuckIcon(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-20).dp).alpha(0.1f))
        FlowerIcon(modifier = Modifier.size(80.dp).align(Alignment.BottomStart).offset(x = (-20).dp, y = 40.dp), color = SoftPink.copy(alpha = 0.2f))

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Andiamo?", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Cosa facciamo oggi?", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = sortOrder == SortOrder.COST,
                        onClick = { viewModel.setSortOrder(if (sortOrder == SortOrder.COST) SortOrder.DEFAULT else SortOrder.COST) },
                        label = { Text("€", fontWeight = FontWeight.Bold, color = if (sortOrder == SortOrder.COST) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftYellow,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = null,
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = sortOrder == SortOrder.DURATION,
                        onClick = { viewModel.setSortOrder(if (sortOrder == SortOrder.DURATION) SortOrder.DEFAULT else SortOrder.DURATION) },
                        label = { Text("⏱", modifier = Modifier.size(18.dp), textAlign = TextAlign.Center) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftBlue,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = null,
                        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (spaceId.isBlank()) {
                    item { GroupWarningCard(primaryColor = primaryColor, onNavigateToProfile = onNavigateToProfile) }
                } else {
                    if (homeActivities.isNotEmpty()) {
                        item { SectionHeader("A Casa", SoftPink) }
                        items(homeActivities, key = { it.id }) { activity ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ActivityCard(activity, userId, groupSize, SoftPink, userImages, idToName, viewModel, onVote = { viewModel.toggleParticipation(activity) }, onDone = { viewModel.markActivityAsDone(activity) }, onDelete = { viewModel.deleteActivity(activity.id) }, onEdit = { activityToEdit = activity })
                            }
                        }
                    }
                    if (awayActivities.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("Fuori Casa", SoftBlue) }
                        awayActivities.forEach { (loc, list) ->
                            item { 
                                Text(
                                    loc.replaceFirstChar { it.uppercase() }, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                ) 
                            }
                            items(list, key = { it.id }) { activity ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + expandVertically(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ActivityCard(activity, userId, groupSize, SoftBlue, userImages, idToName, viewModel, onVote = { viewModel.toggleParticipation(activity) }, onDone = { viewModel.markActivityAsDone(activity) }, onDelete = { viewModel.deleteActivity(activity.id) }, onEdit = { activityToEdit = activity })
                                }
                            }
                        }
                    }
                    if (doneActivities.isNotEmpty()) {
                        item { 
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                "Album dei Ricordi", 
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                            Text(
                                "Le avventure già concluse", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                doneActivities.forEach { activity ->
                                    DoneActivityCard(activity, SoftPurple) { 
                                        viewModel.updateActivity(activity.copy(status = ActivityStatus.TODO)) 
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (spaceId.isNotBlank()) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = primaryColor,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .size(72.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) { 
                Icon(Icons.Default.Add, null, modifier = Modifier.size(36.dp)) 
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ActivityCard(
    activity: Activity, 
    currentUserId: String, 
    groupSize: Int, 
    accentColor: Color, 
    userImages: Map<String, String>, 
    idToName: Map<String, String>, 
    viewModel: InsiemeViewModel, 
    onVote: () -> Unit, 
    onDone: () -> Unit, 
    onDelete: () -> Unit, 
    onEdit: () -> Unit
) {
    val isParticipating = activity.participants.contains(currentUserId)
    val everyoneAgreed = activity.participants.size >= groupSize
    
    val cardColor by animateColorAsState(
        targetValue = if (isParticipating) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        label = "card_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        activity.title, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("€", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(
                            "${activity.budget} • ", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray
                        )
                        Text("⏱", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        Text(
                            " ${activity.time}", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (everyoneAgreed) {
                        IconButton(
                            onClick = onDone,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(48.dp)
                        ) {
                            FlowerIcon(modifier = Modifier.size(40.dp), color = accentColor)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isParticipating) accentColor else accentColor.copy(alpha = 0.1f))
                            .clickable { onVote() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isParticipating) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                            null, 
                            tint = if (isParticipating) Color.White else accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    activity.participants.forEach { id -> 
                        ParticipantAvatar(idToName[id] ?: "Utente", userImages[id], viewModel = viewModel) 
                    }
                }
                if (activity.creatorId == currentUserId) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantAvatar(name: String, imageUrl: String?, viewModel: InsiemeViewModel, size: androidx.compose.ui.unit.Dp = 32.dp) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.background)
    ) {
        val decoded = viewModel.decodeImage(imageUrl)
        if (decoded != null) {
            AsyncImage(model = decoded, contentDescription = name, contentScale = ContentScale.Crop)
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(SoftMint.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.take(1).uppercase(), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DoneActivityCard(activity: Activity, color: Color, onUndo: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUndo() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                activity.title, 
                modifier = Modifier.weight(1f), 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Icon(
                Icons.Default.Check, 
                null, 
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), 
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
