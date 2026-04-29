package com.insieme.app.ui.activities

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
import com.insieme.app.data.model.Activity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDialog(
    activity: Activity? = null,
    onDismiss: () -> Unit,
    onSave: (Activity) -> Unit
) {
    var title by remember { mutableStateOf(activity?.title ?: "") }
    var description by remember { mutableStateOf(activity?.description ?: "") }
    var budget by remember { mutableStateOf(activity?.budget?.toString() ?: "0") }
    var isAtHome by remember { mutableStateOf(activity?.isAtHome ?: true) }
    var locationDetail by remember { mutableStateOf(activity?.locationDetail ?: "") }
    var time by remember { mutableStateOf(activity?.time ?: "Breve") }
    
    var error by remember { mutableStateOf<String?>(null) }

    val pastelGreen = Color(0xFFE8F5E9)
    val deepGreen = Color(0xFF2E7D32)

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
                    if (activity == null) "Nuova Attività" else "Modifica Attività",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = deepGreen
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Cosa facciamo?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = deepGreen, focusedLabelColor = deepGreen)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = isAtHome,
                        onClick = { isAtHome = true },
                        label = { Text("In casa") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepGreen, selectedLabelColor = Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !isAtHome,
                        onClick = { isAtHome = false },
                        label = { Text("Fuori") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepGreen, selectedLabelColor = Color.White)
                    )
                }

                if (!isAtHome) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = locationDetail,
                        onValueChange = { locationDetail = it; error = null },
                        label = { Text("Dove? (es: Roma, Parco...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = deepGreen, focusedLabelColor = deepGreen)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget stimato (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = deepGreen, focusedLabelColor = deepGreen)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Durata", style = MaterialTheme.typography.labelLarge, color = deepGreen, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Breve", "Media", "Lunga").forEach { t ->
                        FilterChip(
                            selected = time == t,
                            onClick = { time = t },
                            label = { Text(t) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = deepGreen, selectedLabelColor = Color.White)
                        )
                    }
                }

                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            error = "Inserisci un titolo!"
                            return@Button
                        }
                        if (!isAtHome && locationDetail.isBlank()) {
                            error = "Specifica un luogo per le attività fuori!"
                            return@Button
                        }
                        onSave(
                            activity?.copy(
                                title = title,
                                description = description,
                                budget = budget.toDoubleOrNull() ?: 0.0,
                                isAtHome = isAtHome,
                                locationDetail = locationDetail,
                                time = time
                            ) ?: Activity(
                                title = title,
                                description = description,
                                budget = budget.toDoubleOrNull() ?: 0.0,
                                isAtHome = isAtHome,
                                locationDetail = locationDetail,
                                time = time
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = deepGreen)
                ) {
                    Text("Salva", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
