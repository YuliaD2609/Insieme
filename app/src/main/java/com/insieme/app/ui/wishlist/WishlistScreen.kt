package com.insieme.app.ui.wishlist

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import coil.compose.AsyncImage
import com.insieme.app.data.model.WishlistItem
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.components.GroupWarningCard
import com.insieme.app.ui.viewmodel.InsiemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: InsiemeViewModel,
    onNavigateToProfile: () -> Unit
) {
    val items by viewModel.sharedWishlist.collectAsState()
    val participants by viewModel.allParticipantNames.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    var selectedParticipant by remember { mutableStateOf(currentUserId) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<WishlistItem?>(null) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    val pastelPink = Color(0xFFFFF5F8)
    val deepPink = Color(0xFFF48FB1)

    val participantList = participants.toList().sortedBy { if (it == currentUserId) 0 else 1 }
    
    if (selectedParticipant !in participants && participants.isNotEmpty()) {
        selectedParticipant = currentUserId
    }

    val filteredItems = items.filter { it.ownerId == selectedParticipant }

    if (showCreateDialog) {
        WishlistDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, link ->
                viewModel.addWishlistItem(title, link)
                showCreateDialog = false
            }
        )
    }

    if (itemToEdit != null) {
        WishlistDialog(
            item = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { title, link ->
                viewModel.updateWishlistItem(itemToEdit!!.id, title, link)
                itemToEdit = null
            }
        )
    }

    if (zoomImageUrl != null) {
        AlertDialog(
            onDismissRequest = { zoomImageUrl = null },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().clickable { zoomImageUrl = null }, contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = zoomImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Lista dei Desideri", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp), modifier = Modifier.weight(1f))
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepPink)
            }

            if (spaceId.isNotBlank()) {
                ScrollableTabRow(
                    selectedTabIndex = participantList.indexOf(selectedParticipant).coerceAtLeast(0),
                    containerColor = Color.White,
                    contentColor = deepPink,
                    edgePadding = 24.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[participantList.indexOf(selectedParticipant).coerceAtLeast(0)]),
                            color = deepPink
                        )
                    }
                ) {
                    participantList.forEach { name ->
                        Tab(
                            selected = selectedParticipant == name,
                            onClick = { selectedParticipant = name },
                            text = { Text(if (name == currentUserId) "La mia" else "Lista di $name", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (spaceId.isBlank()) {
                    item(span = { GridItemSpan(2) }) {
                        GroupWarningCard(
                            title = "Cosa desiderate?",
                            description = "Crea un gruppo per condividere i vostri desideri e fare regali perfetti.",
                            primaryColor = deepPink,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                } else {
                    items(filteredItems) { item ->
                        WishlistCard(
                            item = item, 
                            currentUserId = currentUserId, 
                            color = deepPink,
                            onDelete = { viewModel.deleteWishlistItem(item.id) },
                            onEdit = { itemToEdit = item },
                            onZoom = { zoomImageUrl = item.imageUrl }
                        )
                    }
                }
            }
        }

        if (spaceId.isNotBlank()) {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = deepPink, contentColor = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun WishlistCard(item: WishlistItem, currentUserId: String, color: Color, onDelete: () -> Unit, onEdit: () -> Unit, onZoom: () -> Unit) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!item.link.isNullOrBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        onZoom()
                    }
                } else if (!item.imageUrl.isNullOrBlank()) {
                    onZoom()
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(color.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🛍️", fontSize = 40.sp)
                }
                
                if (item.ownerId == currentUserId) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 2
                )
            }
        }
    }
}
