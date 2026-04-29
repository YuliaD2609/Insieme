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
    val participantIds by viewModel.participantIds.collectAsState()
    val idToName by viewModel.idToName.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val userImages by viewModel.userImages.collectAsState()
    
    var selectedParticipantId by remember { mutableStateOf("Tutti") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<WishlistItem?>(null) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    val deepPink = Color(0xFFF48FB1)
    val participantIdList = listOf("Tutti") + participantIds.toList().sortedBy { if (it == userId) 0 else 1 }
    
    val filteredItems = if (selectedParticipantId == "Tutti") items else items.filter { it.ownerId == selectedParticipantId }

    if (showCreateDialog) {
        WishlistDialog(onDismiss = { showCreateDialog = false }, onSave = { t, l -> viewModel.addWishlistItem(t, l); showCreateDialog = false })
    }
    if (itemToEdit != null) {
        WishlistDialog(item = itemToEdit, onDismiss = { itemToEdit = null }, onSave = { t, l -> viewModel.updateWishlistItem(itemToEdit!!.id, t, l); itemToEdit = null })
    }

    if (zoomImageUrl != null) {
        AlertDialog(onDismissRequest = { zoomImageUrl = null }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(modifier = Modifier.fillMaxSize().clickable { zoomImageUrl = null }, contentAlignment = Alignment.Center) {
                AsyncImage(model = zoomImageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.FillWidth)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Lista dei Desideri", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), modifier = Modifier.weight(1f))
                FlowerIcon(modifier = Modifier.size(36.dp), color = deepPink)
            }

            if (spaceId.isNotBlank()) {
                ScrollableTabRow(
                    selectedTabIndex = participantIdList.indexOf(selectedParticipantId).coerceAtLeast(0), containerColor = Color.White, contentColor = deepPink, edgePadding = 24.dp, divider = {},
                    indicator = { tabPositions ->
                        val index = participantIdList.indexOf(selectedParticipantId).coerceAtLeast(0)
                        if (index < tabPositions.size) { TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[index]), color = deepPink) }
                    }
                ) {
                    participantIdList.forEach { id ->
                        Tab(selected = selectedParticipantId == id, onClick = { selectedParticipantId = id }, text = { Text(when(id) { "Tutti" -> "Tutti"; userId -> "Miei"; else -> idToName[id] ?: "Utente" }, fontWeight = FontWeight.Bold, fontSize = 13.sp) })
                    }
                }
            }

            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(24.dp, 16.dp, 24.dp, 100.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (spaceId.isBlank()) { item(span = { GridItemSpan(2) }) { GroupWarningCard(primaryColor = deepPink, onNavigateToProfile = onNavigateToProfile) } }
                else {
                    if (filteredItems.isEmpty()) { item(span = { GridItemSpan(2) }) { Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { Text("Nessun desiderio ancora...", color = Color.LightGray) } } }
                    items(filteredItems) { item ->
                        WishlistCard(item, userId, deepPink, userImages[item.ownerId], idToName[item.ownerId] ?: "Utente", viewModel, onDelete = { viewModel.deleteWishlistItem(item.id) }, onEdit = { itemToEdit = item }, onZoom = { zoomImageUrl = item.imageUrl })
                    }
                }
            }
        }
        if (spaceId.isNotBlank()) {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = deepPink, contentColor = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp)) }
        }
    }
}

@Composable
fun WishlistCard(item: WishlistItem, currentUserId: String, color: Color, ownerImage: String?, ownerName: String, viewModel: InsiemeViewModel, onDelete: () -> Unit, onEdit: () -> Unit, onZoom: () -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().clickable { if (!item.link.isNullOrBlank()) { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.link))) } catch (e: Exception) { onZoom() } } else if (!item.imageUrl.isNullOrBlank()) { onZoom() } }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(color.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                if (item.imageUrl != null) { AsyncImage(model = item.imageUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                else { Text("🛍️", fontSize = 40.sp) }
                
                Surface(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).size(24.dp), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)) {
                    val decoded = viewModel.decodeImage(ownerImage)
                    if (decoded != null) { AsyncImage(model = decoded, contentDescription = null, contentScale = ContentScale.Crop) }
                    else { Box(modifier = Modifier.background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Text(ownerName.take(1).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color) } }
                }

                if (item.ownerId == currentUserId) {
                    Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) }
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) { Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, minLines = 2, fontSize = 13.sp) }
        }
    }
}
