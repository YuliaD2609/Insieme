package com.insieme.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.insieme.app.ui.components.FlowerIcon
import com.insieme.app.ui.viewmodel.InsiemeViewModel

class GroupCodeTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0, 8) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 && i != trimmed.lastIndex) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 8) return offset + (if (trimmed.length > 4) 1 else 0)
                return if (trimmed.length > 4) 9 else 8
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: InsiemeViewModel) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val spaceId by viewModel.spaceId.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val profileImage by viewModel.profileImage.collectAsState()
    val members by viewModel.allParticipantNames.collectAsState()
    var inputDigits by remember { mutableStateOf("") }
    
    val yellowColor = Color(0xFFFFE082)
    val pastelYellow = Color(0xFFFFFFF0)
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setProfileImage(it.toString()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Il Tuo Profilo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            FlowerIcon(modifier = Modifier.size(36.dp), color = yellowColor)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Image Picker
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(130.dp)
                    .clickable { galleryLauncher.launch("image/*") },
                shape = CircleShape,
                color = pastelYellow,
                border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (profileImage != null) {
                        AsyncImage(
                            model = profileImage,
                            contentDescription = "Foto Profilo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(currentUserId.take(1).uppercase(), fontSize = 52.sp, fontWeight = FontWeight.Bold, color = yellowColor)
                    }
                }
            }
            
            Surface(
                modifier = Modifier.size(38.dp).offset(x = (-4).dp, y = (-4).dp), 
                shape = CircleShape, 
                color = yellowColor,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = currentUserId,
            onValueChange = { 
                viewModel.setCurrentUserId(it)
            },
            label = { Text("Il tuo nome") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = yellowColor,
                focusedLabelColor = yellowColor,
                containerColor = pastelYellow.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (spaceId.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(32.dp), 
                colors = CardDefaults.cardColors(containerColor = pastelYellow.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Inizia la vostra avventura", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = yellowColor)
                    Text("Crea un gruppo o unisciti a uno esistente.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { viewModel.createSpace() }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(16.dp), 
                        colors = ButtonDefaults.buttonColors(containerColor = yellowColor)
                    ) { Text("Crea Nuovo Gruppo", fontWeight = FontWeight.Bold) }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = yellowColor.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = inputDigits, 
                        onValueChange = { input ->
                            val cleaned = input.filter { it.isDigit() }.take(8)
                            inputDigits = cleaned
                            viewModel.clearError()
                        }, 
                        label = { Text("Codice gruppo esistente") }, 
                        placeholder = { Text("1234-5678") },
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = GroupCodeTransformation(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = yellowColor, focusedLabelColor = yellowColor)
                    )
                    
                    if (error != null) {
                        Surface(
                            color = Color(0xFFFFEBEE), 
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                        ) {
                            Text(error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { 
                            val formatted = if (inputDigits.length > 4) {
                                inputDigits.take(4) + "-" + inputDigits.drop(4)
                            } else {
                                inputDigits
                            }
                            viewModel.joinSpace(formatted) 
                        }, 
                        modifier = Modifier.fillMaxWidth(), 
                        colors = ButtonDefaults.textButtonColors(contentColor = yellowColor)
                    ) { Text("Unisciti ora", fontWeight = FontWeight.Bold) }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(32.dp), 
                colors = CardDefaults.cardColors(containerColor = pastelYellow.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Il Tuo Gruppo", fontWeight = FontWeight.Bold, color = yellowColor, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = yellowColor, shape = CircleShape) {
                            Text("${members.size}", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.White, 
                        shape = RoundedCornerShape(20.dp), 
                        modifier = Modifier.fillMaxWidth(), 
                        border = androidx.compose.foundation.BorderStroke(2.dp, yellowColor.copy(alpha = 0.1f))
                    ) {
                        Text(
                            spaceId, 
                            modifier = Modifier.padding(20.dp), 
                            textAlign = TextAlign.Center, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 28.sp, 
                            color = yellowColor, 
                            letterSpacing = 3.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Membri: ${members.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = yellowColor.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Unisciti al mio gruppo su Insieme! Codice: $spaceId")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }, 
                            modifier = Modifier.weight(1.2f), 
                            shape = RoundedCornerShape(16.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = yellowColor)
                        ) { 
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Condividi") 
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.setSpaceId("") }, 
                            modifier = Modifier.weight(0.8f), 
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = yellowColor.copy(alpha = 0.6f))
                        ) { Text("Esci") }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Text("Versione 1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
    }
}
