package com.insieme.app.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.insieme.app.data.model.GameItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDialog(
    item: GameItem? = null,
    onDismiss: () -> Unit,
    onSave: (GameItem) -> Unit
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    
    val deepBlue = Color(0xFF0288D1)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    if (item == null) "Che gioco aggiungiamo?" else "Modifica Gioco",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = deepBlue
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome del gioco") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = deepBlue, focusedLabelColor = deepBlue)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(item?.copy(title = title) ?: GameItem(title = title))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = deepBlue)
                ) {
                    Text("Salva", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
