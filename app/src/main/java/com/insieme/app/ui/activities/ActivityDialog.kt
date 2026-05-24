package com.insieme.app.ui.activities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.insieme.app.data.model.Activity
import com.insieme.app.ui.theme.*

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

    val primaryColor = PastelGreen

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    if (activity == null) "Nuova Attività" else "Modifica Attività",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Crea una nuova attività da fare insieme!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Cosa facciamo?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = primaryColor, focusedLabelColor = MaterialTheme.colorScheme.onSurface),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(true, false).forEach { atHome ->
                        val isSelected = isAtHome == atHome
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .clickable { isAtHome = atHome }
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (atHome) "In casa" else "Fuori",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (atHome) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                if (!isAtHome) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = locationDetail,
                        onValueChange = { locationDetail = it; error = null },
                        label = { Text("Dove? (es: Roma, Parco...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = primaryColor, focusedLabelColor = MaterialTheme.colorScheme.onSurface),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = budget,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            budget = input
                        }
                    },
                    label = { Text("Budget stimato (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = primaryColor, focusedLabelColor = MaterialTheme.colorScheme.onSurface),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Durata", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Breve", "Media", "Lunga").forEach { t ->
                        val isSelected = time == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .clickable { time = t }
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                t,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        if (title.isBlank()) {
                            error = "Inserisci un titolo!"
                            return@OutlinedButton
                        }
                        if (!isAtHome && locationDetail.isBlank()) {
                            error = "Specifica un luogo per le attività fuori!"
                            return@OutlinedButton
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Salva", fontWeight = FontWeight.Bold)
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                ) {
                    Text("Annulla")
                }
            }
        }
    }
}
