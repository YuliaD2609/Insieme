package com.insieme.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.viewmodel.InsiemeViewModel

import com.insieme.app.ui.theme.*
import com.insieme.app.ui.components.DuckIcon
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: InsiemeViewModel) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val profileImage by viewModel.profileImage.collectAsState()
    val members by viewModel.allParticipantNames.collectAsState()
    val joinedGroups by viewModel.joinedGroups.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val creatorId by viewModel.currentSpaceCreatorId.collectAsState()
    val isCreator = creatorId == userId
    
    val primaryColor = SoftYellow
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setProfileImage(it.toString()) }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundWhite)) {
        // Decorative background elements
        DuckIcon(modifier = Modifier.size(160.dp).align(Alignment.BottomStart).offset(x = (-40).dp, y = 40.dp).alpha(0.08f))
        FlowerIcon(modifier = Modifier.size(100.dp).align(Alignment.TopEnd).offset(x = 20.dp, y = 120.dp), color = SoftPurple.copy(alpha = 0.15f))

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Il Tuo Profilo", 
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextDark
                            )
                            Text(
                                "Personalizza la tua esperienza", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDark.copy(alpha = 0.5f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(primaryColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = TextDark, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(48.dp)) }

                item {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .clickable { galleryLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(4.dp, primaryColor.copy(alpha = 0.2f)),
                            shadowElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val decoded = viewModel.decodeImage(profileImage)
                                if (decoded != null) {
                                    AsyncImage(model = decoded, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Text((currentUserId.ifBlank { "Tu" }).take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = primaryColor)
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier.size(44.dp).offset(x = (-4).dp, y = (-4).dp), 
                            shape = CircleShape, 
                            color = primaryColor, 
                            shadowElevation = 4.dp
                        ) {
                            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Edit, null, tint = TextDark, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }

                item {
                    OutlinedTextField(
                        value = currentUserId,
                        onValueChange = { viewModel.setCurrentUserId(it) },
                        label = { Text("Come ti chiami?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor, 
                            unfocusedBorderColor = TextDark.copy(alpha = 0.1f),
                            focusedLabelColor = TextDark, 
                            containerColor = Color.White
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item {
                    if (spaceId.isEmpty()) {
                        JoinCreateSection(viewModel, primaryColor)
                    } else {
                        ActiveGroupSection(viewModel, spaceId, members, isCreator, primaryColor)
                    }
                }

                if (joinedGroups.size > 1 || (joinedGroups.size == 1 && spaceId.isEmpty())) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "I Tuoi Gruppi", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = TextDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(joinedGroups.filter { it != spaceId }.toList()) { id ->
                        GroupListItem(id, onSwitch = { viewModel.setSpaceId(id) }, onLeave = { viewModel.leaveSpace(id) }, accentColor = primaryColor)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        "Insieme v1.1.0 • Made with ❤️", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = TextDark.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun GroupListItem(id: String, onSwitch: () -> Unit, onLeave: () -> Unit, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, TextDark.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person, 
                null, 
                tint = accentColor, 
                modifier = Modifier.size(24.dp).padding(4.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(id, style = MaterialTheme.typography.bodyMedium, color = TextDark)
            }
            IconButton(onClick = onSwitch) {
                Icon(Icons.Default.Search, null, tint = SoftBlue)
            }
            IconButton(onClick = onLeave) {
                Icon(Icons.Default.Delete, null, tint = ErrorRed.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCreateSection(viewModel: InsiemeViewModel, accentColor: Color) {
    var inputDigits by remember { mutableStateOf("") }
    val error by viewModel.errorMessage.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = MaterialTheme.shapes.extraLarge, 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Entra nel Gruppo", style = MaterialTheme.typography.titleLarge, color = TextDark)
            Text("Crea un nuovo spazio o usa un codice.", style = MaterialTheme.typography.bodySmall, color = TextDark.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.createSpace() }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                shape = CircleShape, 
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = TextDark)
            ) { 
                Text("Crea Nuovo Gruppo", fontWeight = FontWeight.Bold) 
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(TextDark.copy(alpha = 0.1f)))
                Text("OPPURE", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall, color = TextDark.copy(alpha = 0.3f))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(TextDark.copy(alpha = 0.1f)))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = inputDigits, 
                onValueChange = { input -> inputDigits = input.filter { it.isDigit() }.take(8); viewModel.clearError() }, 
                label = { Text("Codice a 8 cifre") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = TextDark.copy(alpha = 0.1f)
                )
            )
            
            if (error != null) { 
                Text(error!!, color = ErrorRed, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center) 
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { viewModel.joinSpace(inputDigits) }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.textButtonColors(contentColor = TextDark)
            ) { 
                Text("Unisciti ora", fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
fun ActiveGroupSection(viewModel: InsiemeViewModel, spaceId: String, members: Set<String>, isCreator: Boolean, accentColor: Color) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = MaterialTheme.shapes.extraLarge, 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Il Tuo Gruppo", style = MaterialTheme.typography.titleLarge, color = TextDark)
                Spacer(modifier = Modifier.width(12.dp))
                Surface(color = accentColor, shape = CircleShape) { 
                    Text(
                        "${members.size}", 
                        color = TextDark, 
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), 
                        fontWeight = FontWeight.Bold
                    ) 
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = BackgroundWhite, 
                shape = MaterialTheme.shapes.medium, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    spaceId, 
                    modifier = Modifier.padding(24.dp), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextDark,
                    letterSpacing = 4.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Membri: ${members.joinToString(", ")}", 
                style = MaterialTheme.typography.bodySmall, 
                color = TextDark.copy(alpha = 0.5f), 
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { 
                        val intent = Intent(Intent.ACTION_SEND).apply { 
                            action = Intent.ACTION_SEND; 
                            putExtra(Intent.EXTRA_TEXT, "Unisciti al mio gruppo su Insieme! Codice: $spaceId"); 
                            type = "text/plain" 
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }, 
                    modifier = Modifier.weight(1f).height(56.dp), 
                    shape = CircleShape, 
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = TextDark)
                ) { 
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invita") 
                }
                
                IconButton(
                    onClick = { viewModel.logout() }, 
                    modifier = Modifier.size(56.dp).background(BackgroundWhite, CircleShape).border(1.dp, TextDark.copy(alpha = 0.1f), CircleShape)
                ) { 
                    Icon(Icons.Default.Close, null, tint = TextDark.copy(alpha = 0.5f))
                }

                if (isCreator) {
                    IconButton(
                        onClick = { viewModel.deleteSpacePermanently(spaceId) }, 
                        modifier = Modifier.size(56.dp).background(ErrorRed.copy(alpha = 0.1f), CircleShape).border(1.dp, ErrorRed.copy(alpha = 0.2f), CircleShape)
                    ) { 
                        Icon(Icons.Default.Delete, null, tint = ErrorRed)
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.leaveSpace(spaceId) }, 
                        modifier = Modifier.size(56.dp).background(TextDark.copy(alpha = 0.05f), CircleShape).border(1.dp, TextDark.copy(alpha = 0.1f), CircleShape)
                    ) { 
                        Icon(Icons.Default.Delete, null, tint = TextDark.copy(alpha = 0.3f))
                    }
                }
            }
            
            if (isCreator) {
                Text(
                    "Sei il creatore di questo gruppo", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = accentColor,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
