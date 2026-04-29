package com.insieme.app.ui.activities

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.insieme.app.data.model.Activity
import com.insieme.app.data.model.ActivityStatus
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.components.GroupWarningCard
import com.insieme.app.ui.viewmodel.InsiemeViewModel
import com.insieme.app.ui.viewmodel.SortOrder

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
    
    val deepGreen = Color(0xFFA5D6A7)

    val todoActivities = activities.filter { it.status == ActivityStatus.TODO }
    val doneActivities = activities.filter { it.status == ActivityStatus.DONE }

    // Raggruppamento
    val homeActivities = todoActivities.filter { it.isAtHome }
    val awayActivities = todoActivities.filter { !it.isAtHome }
        .groupBy { it.locationDetail.trim().lowercase() }

    if (showCreateDialog) {
        ActivityDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newActivity ->
                viewModel.addActivity(newActivity)
                showCreateDialog = false
            }
        )
    }

    if (activityToEdit != null) {
        ActivityDialog(
            activity = activityToEdit,
            onDismiss = { activityToEdit = null },
            onSave = { updatedActivity ->
                viewModel.updateActivity(updatedActivity)
                activityToEdit = null
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Andiamo?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = sortOrder == SortOrder.COST,
                        onClick = { viewModel.setSortOrder(if (sortOrder == SortOrder.COST) SortOrder.DEFAULT else SortOrder.COST) },
                        label = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepGreen.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = sortOrder == SortOrder.DURATION,
                        onClick = { viewModel.setSortOrder(if (sortOrder == SortOrder.DURATION) SortOrder.DEFAULT else SortOrder.DURATION) },
                        label = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepGreen.copy(alpha = 0.2f))
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (spaceId.isBlank()) {
                    item {
                        GroupWarningCard(
                            primaryColor = deepGreen,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                } else {
                    // SEZIONE CASA
                    if (homeActivities.isNotEmpty()) {
                        item {
                            SectionHeader("In Casa", deepGreen)
                        }
                        items(homeActivities, key = { it.id }) { activity ->
                            ActivityCard(
                                activity = activity,
                                currentUserId = userId,
                                groupSize = groupSize,
                                primaryColor = deepGreen,
                                userImages = userImages,
                                idToName = idToName,
                                onVote = { viewModel.toggleParticipation(activity) },
                                onDone = { viewModel.markActivityAsDone(activity) },
                                onDelete = { viewModel.deleteActivity(activity.id) },
                                onEdit = { activityToEdit = activity }
                            )
                        }
                    }

                    // SEZIONE FUORI (raggruppata per luogo)
                    if (awayActivities.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader("Fuori Casa", deepGreen)
                        }
                        
                        awayActivities.forEach { (locationName, list) ->
                            item {
                                Text(
                                    locationName.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = deepGreen.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                            }
                            items(list, key = { it.id }) { activity ->
                                ActivityCard(
                                    activity = activity,
                                    currentUserId = userId,
                                    groupSize = groupSize,
                                    primaryColor = deepGreen,
                                    userImages = userImages,
                                    idToName = idToName,
                                    onVote = { viewModel.toggleParticipation(activity) },
                                    onDone = { viewModel.markActivityAsDone(activity) },
                                    onDelete = { viewModel.deleteActivity(activity.id) },
                                    onEdit = { activityToEdit = activity }
                                )
                            }
                        }
                    }

                    if (doneActivities.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Già fatte", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.LightGray))
                        }
                        items(doneActivities, key = { it.id }) { activity ->
                            DoneActivityCard(activity, deepGreen) { viewModel.updateActivity(activity.copy(status = ActivityStatus.TODO)) }
                        }
                    }
                }
            }
        }

        if (spaceId.isNotBlank()) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = deepGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
    }
}

@Composable
fun ActivityCard(
    activity: Activity,
    currentUserId: String,
    groupSize: Int,
    primaryColor: Color,
    userImages: Map<String, String>,
    idToName: Map<String, String>,
    onVote: () -> Unit,
    onDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isParticipating = activity.participants.contains(currentUserId)
    val everyoneAgreed = activity.participants.size >= groupSize

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(activity.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text("${activity.budget}€ • ${activity.time}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (everyoneAgreed) {
                        IconButton(onClick = onDone, modifier = Modifier.padding(end = 8.dp)) {
                            FlowerIcon(modifier = Modifier.size(32.dp), color = primaryColor)
                        }
                    }

                    IconButton(
                        onClick = onVote,
                        modifier = Modifier.size(44.dp).background(if (isParticipating) primaryColor else primaryColor.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Default.Check, null, tint = if (isParticipating) Color.White else primaryColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f)) {
                    activity.participants.forEach { id -> 
                        ParticipantAvatar(idToName[id] ?: "Utente", userImages[id]) 
                    }
                }
                
                if (activity.creatorId == currentUserId) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantAvatar(name: String, imageUrl: String?) {
    Surface(
        modifier = Modifier.size(28.dp).offset(x = 0.dp).clip(CircleShape),
        color = Color.LightGray.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(model = imageUrl, contentDescription = name, contentScale = ContentScale.Crop)
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DoneActivityCard(activity: Activity, primaryColor: Color, onUndo: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onUndo() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = primaryColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(activity.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
            Icon(Icons.Default.Refresh, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
