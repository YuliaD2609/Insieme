package com.insieme.app.ui.media

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
import com.insieme.app.data.model.MediaItem
import com.insieme.app.data.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDialog(
    item: MediaItem? = null,
    onDismiss: () -> Unit,
    onSave: (MediaItem) -> Unit
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var selectedType by remember { mutableStateOf(item?.type ?: MediaType.FILM) }
    
    val deepBeige = Color(0xFFBCB1A1)

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
                    if (item == null) "Cosa vogliamo vedere?" else "Modifica Media",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = deepBeige
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = deepBeige, focusedLabelColor = deepBeige)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedType == MediaType.FILM,
                        onClick = { selectedType = MediaType.FILM },
                        label = { Text("Film") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepBeige, selectedLabelColor = Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedType == MediaType.SERIE_TV,
                        onClick = { selectedType = MediaType.SERIE_TV },
                        label = { Text("Serie TV") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepBeige, selectedLabelColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(item?.copy(title = title, type = selectedType) ?: MediaItem(title = title, type = selectedType))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = deepBeige)
                ) {
                    Text("Salva", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
