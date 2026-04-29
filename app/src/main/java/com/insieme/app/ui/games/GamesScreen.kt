package com.insieme.app.ui.games

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
import com.insieme.app.data.model.GameItem
import com.insieme.app.ui.activities.ParticipantAvatar
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
    val groupSize by viewModel.groupSize.collectAsState()
    val userImages by viewModel.userImages.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<GameItem?>(null) }
    
    val pastelBlue = Color(0xFFF0F7FF)
    val deepBlue = Color(0xFF90CAF9)

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
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "A cosa giochiamo?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepBlue)
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp, 8.dp, 24.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (spaceId.isBlank()) {
                    item {
                        GroupWarningCard(
                            primaryColor = deepBlue,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                } else {
                    if (todoGames.isNotEmpty()) {
                        items(todoGames, key = { it.id }) { item ->
                            GameCard(
                                item = item,
                                currentUserId = currentUserId,
                                groupSize = groupSize,
                                primaryColor = deepBlue,
                                userImages = userImages,
                                onVote = { viewModel.toggleGameParticipation(item) },
                                onDone = { viewModel.toggleGameStatus(item) },
                                onDelete = { viewModel.deleteGame(item.id) },
                                onEdit = { itemToEdit = item }
                            )
                        }
                    }

                    if (doneGames.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Già fatti", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.LightGray))
                        }
                        items(doneGames, key = { it.id }) { item ->
                            DoneGameCard(item, deepBlue) { viewModel.toggleGameStatus(item) }
                        }
                    }
                }
            }
        }

        if (spaceId.isNotBlank()) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = deepBlue,
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
fun GameCard(
    item: GameItem,
    currentUserId: String,
    groupSize: Int,
    primaryColor: Color,
    userImages: Map<String, String>,
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
                Text(item.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))

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
                    item.participants.forEach { name -> 
                        ParticipantAvatar(name, userImages[name]) 
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
fun DoneGameCard(item: GameItem, primaryColor: Color, onUndo: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onUndo() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = primaryColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
            Icon(Icons.Default.Refresh, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
