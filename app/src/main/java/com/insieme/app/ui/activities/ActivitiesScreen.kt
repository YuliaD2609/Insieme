package com.insieme.app.ui.activities

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showCreateDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<Activity?>(null) }
    
    val activities by viewModel.activities.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val groupSize by viewModel.groupSize.collectAsState()
    val currentSort by viewModel.sortOrder.collectAsState()

    val pastelGreen = Color(0xFFF1F8E9)
    val deepGreen = Color(0xFF81C784)

    val todoActivities = activities.filter { it.status == ActivityStatus.TODO }
    val doneActivities = activities.filter { it.status == ActivityStatus.DONE }

    val homeActivities = todoActivities.filter { it.isAtHome }
    val awayActivitiesGrouped = todoActivities.filter { !it.isAtHome }
        .groupBy { it.locationDetail.trim().lowercase().ifBlank { "fuori casa" } }

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
                modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Facciamo qualcosa!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepGreen)
            }

            if (spaceId.isNotBlank()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { SortChip("Tutti", currentSort == SortOrder.DEFAULT, deepGreen) { viewModel.setSortOrder(SortOrder.DEFAULT) } }
                    item { SortChip("Costo", currentSort == SortOrder.COST, deepGreen) { viewModel.setSortOrder(SortOrder.COST) } }
                    item { SortChip("Durata", currentSort == SortOrder.DURATION, deepGreen) { viewModel.setSortOrder(SortOrder.DURATION) } }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 100.dp),
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
                    if (homeActivities.isNotEmpty()) {
                        item { SectionHeader("In casa", Icons.Default.Home, deepGreen) }
                        items(homeActivities, key = { "home_${it.id}_${homeActivities.indexOf(it)}" }) { activity ->
                            ActivityCard(
                                activity = activity, 
                                currentUserId = currentUserId, 
                                groupSize = groupSize, 
                                primaryColor = deepGreen,
                                onVote = { viewModel.toggleParticipation(activity) },
                                onDone = { viewModel.markActivityAsDone(activity) },
                                onDelete = { viewModel.deleteActivity(activity.id) },
                                onEdit = { activityToEdit = activity }
                            )
                        }
                    }

                    if (awayActivitiesGrouped.isNotEmpty()) {
                        item { 
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader("Fuori casa", Icons.Default.LocationOn, Color(0xFF8D6E63)) 
                        }
                        awayActivitiesGrouped.forEach { (location, list) ->
                            item { SubSectionHeader(location.replaceFirstChar { it.uppercase() }) }
                            items(list, key = { "away_${it.id}_${list.indexOf(it)}" }) { activity ->
                                ActivityCard(
                                    activity = activity, 
                                    currentUserId = currentUserId, 
                                    groupSize = groupSize, 
                                    primaryColor = Color(0xFF8D6E63),
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
                            SectionHeader("Fatte", Icons.Default.CheckCircle, Color.LightGray)
                        }
                            items(doneActivities, key = { "done_${it.id}_${doneActivities.indexOf(it)}" }) { activity ->
                            DoneActivityCard(activity, deepGreen) { viewModel.toggleParticipation(activity) }
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
fun SortChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) color else color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            label, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else color
            )
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = color))
    }
}

@Composable
fun SubSectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp), modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 4.dp))
}

@Composable
fun ActivityCard(
    activity: Activity, 
    currentUserId: String, 
    groupSize: Int, 
    primaryColor: Color,
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryTag("€${activity.budget}", primaryColor.copy(alpha = 0.1f), primaryColor)
                        CategoryTag(activity.time, primaryColor.copy(alpha = 0.1f), primaryColor)
                    }
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
                    activity.participants.forEach { name -> ParticipantAvatar(name) }
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
fun ParticipantAvatar(name: String) {
    Surface(modifier = Modifier.size(30.dp).offset(x = (-4).dp), shape = CircleShape, color = Color.LightGray.copy(alpha = 0.2f), border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)) {
        Box(contentAlignment = Alignment.Center) { Text(name.take(1).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
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

@Composable
fun CategoryTag(text: String, bgColor: Color, textColor: Color) {
    Surface(color = bgColor, shape = RoundedCornerShape(10.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, color = textColor))
    }
}
