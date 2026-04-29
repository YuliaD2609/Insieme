package com.insieme.app.ui.games

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
import com.insieme.app.data.model.GameItem
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.components.GroupWarningCard
import com.insieme.app.ui.viewmodel.InsiemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    viewModel: InsiemeViewModel,
    onNavigateToProfile: () -> Unit
) {
    val games by viewModel.games.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<GameItem?>(null) }
    
    val pastelBlue = Color(0xFFE3F2FD)
    val deepBlue = Color(0xFF64B5F6)

    val todoGames = games.filter { it.status == ActivityStatus.TODO }
    val doneGames = games.filter { it.status == ActivityStatus.DONE }

    if (showCreateDialog) {
        GameDialog(
            onDismiss = { showCreateDialog = false }, 
            onSave = { newItem -> 
                viewModel.addGame(newItem)
                showCreateDialog = false 
            }
        )
    }

    if (itemToEdit != null) {
        GameDialog(
            item = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                viewModel.updateGame(updatedItem)
                itemToEdit = null
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Giochiamo insieme!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp), modifier = Modifier.weight(1f))
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepBlue)
            }

            LazyColumn(contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (spaceId.isBlank()) {
                    item {
                        GroupWarningCard(
                            title = "Sfida i tuoi amici!",
                            description = "Crea un gruppo per tenere traccia dei giochi che volete fare.",
                            primaryColor = deepBlue,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                } else {
                    itemsIndexed(todoGames) { _, item -> 
                        GameCard(
                            item = item, 
                            currentUserId = currentUserId,
                            primaryColor = deepBlue, 
                            onToggle = { viewModel.toggleGameStatus(item) },
                            onDelete = { viewModel.deleteGame(item.id) },
                            onEdit = { itemToEdit = item }
                        ) 
                    }
                    if (doneGames.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(24.dp)); Text("Finiti", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Gray), modifier = Modifier.padding(bottom = 12.dp)) }
                        itemsIndexed(doneGames) { _, item -> 
                            DoneGameCard(item, deepBlue) { viewModel.toggleGameStatus(item) } 
                        }
                    }
                }
            }
        }

        if (spaceId.isNotBlank()) {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = deepBlue, contentColor = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun GameCard(item: GameItem, currentUserId: String, primaryColor: Color, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = primaryColor.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { FlowerIcon(modifier = Modifier.size(28.dp), color = primaryColor) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(item.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                IconButton(onClick = onToggle, modifier = Modifier.size(44.dp).background(primaryColor.copy(alpha = 0.05f), RoundedCornerShape(14.dp))) {
                    Icon(Icons.Default.Check, null, tint = primaryColor.copy(alpha = 0.5f))
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
fun DoneGameCard(item: GameItem, primaryColor: Color, onUndo: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onUndo() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = primaryColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.title, style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), modifier = Modifier.weight(1f))
            Icon(Icons.Default.Refresh, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
