package com.insieme.app.ui.media

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.insieme.app.data.model.ActivityStatus
import com.insieme.app.data.model.MediaItem
import com.insieme.app.data.model.MediaType
import com.insieme.app.ui.activities.ParticipantAvatar
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.components.GroupWarningCard
import com.insieme.app.ui.viewmodel.InsiemeViewModel

import com.insieme.app.ui.theme.*
import com.insieme.app.ui.components.DuckIcon
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    viewModel: InsiemeViewModel,
    onNavigateToProfile: () -> Unit
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val groupSize by viewModel.groupSize.collectAsState()
    val userImages by viewModel.userImages.collectAsState()
    val idToName by viewModel.idToName.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MediaItem?>(null) }
    
    val primaryColor = SoftMint

    val todoMedia = mediaItems.filter { it.status == ActivityStatus.TODO }
    val todoMovies = todoMedia.filter { it.type == MediaType.FILM }
    val todoSeries = todoMedia.filter { it.type == MediaType.SERIE_TV }
    val doneMedia = mediaItems.filter { it.status == ActivityStatus.DONE }

    if (showCreateDialog) {
        MediaDialog(onDismiss = { showCreateDialog = false }, onSave = { viewModel.addMediaItem(it); showCreateDialog = false })
    }

    if (itemToEdit != null) {
        MediaDialog(item = itemToEdit, onDismiss = { itemToEdit = null }, onSave = { viewModel.updateMediaItem(it); itemToEdit = null })
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Decorative background elements
        DuckIcon(modifier = Modifier.size(100.dp).align(Alignment.TopStart).offset(x = (-30).dp, y = (-20).dp).alpha(0.08f))
        FlowerIcon(modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(x = 30.dp, y = 40.dp), color = SoftPink.copy(alpha = 0.15f))

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 16.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cosa vediamo?", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Cinema e Serie TV insieme", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(primaryColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 120.dp), 
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (spaceId.isBlank()) { 
                    item { GroupWarningCard(primaryColor = primaryColor, onNavigateToProfile = onNavigateToProfile) } 
                } else {
                    if (todoMovies.isNotEmpty()) {
                        item { SectionHeader("Film", SoftBlue) }
                        items(todoMovies, key = { it.id }) { item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MediaCard(item, userId, groupSize, SoftBlue, userImages, idToName, viewModel, onVote = { viewModel.toggleMediaParticipation(item) }, onDone = { viewModel.toggleMediaStatus(item) }, onDelete = { viewModel.deleteMediaItem(item.id) }, onEdit = { itemToEdit = item })
                            }
                        }
                    }
                    if (todoSeries.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("Serie TV", AccentOrange) }
                        items(todoSeries, key = { it.id }) { item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MediaCard(item, userId, groupSize, AccentOrange, userImages, idToName, viewModel, onVote = { viewModel.toggleMediaParticipation(item) }, onDone = { viewModel.toggleMediaStatus(item) }, onDelete = { viewModel.deleteMediaItem(item.id) }, onEdit = { itemToEdit = item })
                            }
                        }
                    }
                    if (doneMedia.isNotEmpty()) {
                        item { 
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                "Visti & Apprezzati", 
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                doneMedia.forEach { item ->
                                    DoneMediaCard(item, SoftPurple) { 
                                        viewModel.toggleMediaStatus(item) 
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
fun MediaCard(
    item: MediaItem, 
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
    val isParticipating = item.participants.contains(currentUserId)
    val everyoneAgreed = item.participants.size >= groupSize
    
    val cardColor by animateColorAsState(
        targetValue = if (isParticipating) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
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
                        item.title, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = if (isParticipating) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.1f), 
                        shape = CircleShape
                    ) { 
                        Text(
                            if (item.type == MediaType.FILM) "Film" else "Serie TV", 
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ) 
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (everyoneAgreed) { 
                        IconButton(onClick = onDone, modifier = Modifier.padding(end = 8.dp).size(48.dp)) { 
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
                            if (isParticipating) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, 
                            null, 
                            tint = if (isParticipating) MaterialTheme.colorScheme.onSurface else accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    item.participants.forEach { id -> 
                        ParticipantAvatar(idToName[id] ?: "Utente", userImages[id], viewModel = viewModel) 
                    }
                }
                if (item.creatorId == currentUserId) {
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
fun DoneMediaCard(item: MediaItem, color: Color, onUndo: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onUndo() }, 
        shape = MaterialTheme.shapes.medium, 
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                item.title, 
                modifier = Modifier.weight(1f), 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
        }
    }
}
