package com.insieme.app.ui.media

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MediaItem?>(null) }
    
    val pastelBeige = Color(0xFFFAF9F6)
    val deepBeige = Color(0xFFA1887F)

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
            Row(modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Cose da vedere", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp), modifier = Modifier.weight(1f))
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepBeige)
            }

            LazyColumn(contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (spaceId.isBlank()) {
                    item {
                        GroupWarningCard(
                            title = "Film e Serie insieme!",
                            description = "Unisciti a un gruppo nel profilo per condividere cosa guardare.",
                            primaryColor = deepBeige,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                } else {
                    itemsIndexed(todoMedia) { _, item -> 
                        MediaCard(
                            item = item, 
                            currentUserId = currentUserId, 
                            color = deepBeige, 
                            onToggle = { viewModel.toggleMediaStatus(item) }, 
                            onDelete = { viewModel.deleteMediaItem(item.id) },
                            onEdit = { itemToEdit = item }
                        ) 
                    }
                    if (doneMedia.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(24.dp)); Text("Visti", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Gray), modifier = Modifier.padding(bottom = 12.dp)) }
                        itemsIndexed(doneMedia) { _, item -> DoneMediaCard(item, deepBeige) { viewModel.toggleMediaStatus(item) } }
                    }
                }
            }
        }
        
        if (spaceId.isNotBlank()) {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = deepBeige, contentColor = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun MediaCard(item: MediaItem, currentUserId: String, color: Color, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = color.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { FlowerIcon(modifier = Modifier.size(28.dp), color = color) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                    Text(if (item.type == MediaType.FILM) "Film" else "Serie TV", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
                }
                IconButton(onClick = onToggle, modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.05f), RoundedCornerShape(14.dp))) {
                    Icon(Icons.Default.Check, null, tint = color.copy(alpha = 0.5f))
                }
            }
            
            if (item.creatorId == currentUserId) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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

@Composable
fun DoneMediaCard(item: MediaItem, color: Color, onUndo: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onUndo() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.title, style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), modifier = Modifier.weight(1f))
            Icon(Icons.Default.Refresh, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
