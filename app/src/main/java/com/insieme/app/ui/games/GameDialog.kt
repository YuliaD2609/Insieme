package com.insieme.app.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.insieme.app.data.model.GameItem

import com.insieme.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDialog(
    item: GameItem? = null,
    onDismiss: () -> Unit,
    onSave: (GameItem) -> Unit
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var isOwned by remember { mutableStateOf(item?.isOwned ?: false) }
    
    val primaryColor = SoftBlue

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    if (item == null) "Nuovo Gioco 🎮" else "Modifica Gioco",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Date un nome alla vostra prossima sfida!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome del gioco") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = TextDark.copy(alpha = 0.1f),
                        focusedLabelColor = TextDark
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(false, true).forEach { owned ->
                        val isSelected = isOwned == owned
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) primaryColor else BackgroundWhite)
                                .clickable { isOwned = owned }
                                .border(
                                    1.dp, 
                                    if (isSelected) primaryColor else TextDark.copy(alpha = 0.1f), 
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (!owned) "Non abbiamo" else "Abbiamo",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) TextDark else TextDark.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(item?.copy(title = title, isOwned = isOwned) ?: GameItem(title = title, isOwned = isOwned))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark)
                ) {
                    Text("Conferma", fontWeight = FontWeight.Bold)
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = TextDark.copy(alpha = 0.4f))
                ) {
                    Text("Annulla")
                }
            }
        }
    }
}
