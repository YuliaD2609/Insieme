package com.insieme.app.ui.media

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    
    val deepBeige = Color(0xFFBCB1A1)

    val todoMedia = mediaItems.filter { it.status == ActivityStatus.TODO }
    val doneMedia = mediaItems.filter { it.status == ActivityStatus.DONE }

    if (showCreateDialog) {
        MediaDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newItem ->
                viewModel.addMediaItem(newItem)
                showCreateDialog = false
            }
        )
    }

    if (itemToEdit != null) {
        MediaDialog(
            item = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                viewModel.updateMediaItem(updatedItem)
                itemToEdit = null
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
                    "Cosa vediamo?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepBeige)
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (spaceId.isBlank()) {
                    item {
                        GroupWarningCard(
                            primaryColor = deepBeige,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                } else {
                    if (todoMedia.isNotEmpty()) {
                        items(todoMedia, key = { it.id }) { item ->
                            MediaCard(
                                item = item,
                                currentUserId = userId,
                                groupSize = groupSize,
                                primaryColor = deepBeige,
                                userImages = userImages,
                                idToName = idToName,
                                onVote = { viewModel.toggleMediaParticipation(item) },
                                onDone = { viewModel.toggleMediaStatus(item) },
                                onDelete = { viewModel.deleteMediaItem(item.id) },
                                onEdit = { itemToEdit = item }
                            )
                        }
                    }

                    if (doneMedia.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Già visti", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.LightGray))
                        }
                        items(doneMedia, key = { it.id }) { item ->
                            DoneMediaCard(item, deepBeige) { viewModel.toggleMediaStatus(item) }
                        }
                    }
                }
            }
        }

        if (spaceId.isNotBlank()) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = deepBeige,
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
fun MediaCard(
    item: MediaItem,
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
    val isParticipating = item.participants.contains(currentUserId)
    val everyoneAgreed = item.participants.size >= groupSize

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = primaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (item.type == MediaType.FILM) "Film" else "Serie TV",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = primaryColor)
                        )
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
                    item.participants.forEach { id -> 
                        ParticipantAvatar(idToName[id] ?: "Utente", userImages[id]) 
                    }
                }
                
                if (item.creatorId == currentUserId) {
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
fun DoneMediaCard(item: MediaItem, primaryColor: Color, onUndo: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onUndo() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = primaryColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
            Icon(Icons.Default.Refresh, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
