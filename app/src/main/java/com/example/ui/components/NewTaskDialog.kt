package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Priority
import com.example.data.model.TaskType
import com.example.ui.theme.AmberSafety
import com.example.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTaskDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        machineId: String,
        location: String,
        type: TaskType,
        priority: Priority,
        reporterName: String,
        reporterPhone: String,
        description: String
    ) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var title by remember { mutableStateOf("") }
    var machineId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TaskType.AWARIA) }
    var priority by remember { mutableStateOf(Priority.WYSOKI) }
    var reporterName by remember { mutableStateOf("") }
    var reporterPhone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Instant time registration for industrial audit
    val currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val formattedCurrentTime = remember {
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(currentTimestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nowe Zgłoszenie UR",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Data zdarzenia: $formattedCurrentTime",
                            color = AmberSafety,
                            fontSize = 11.5.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Task Type selection
                Text(text = "Kategoria zlecenia:", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskType.values().forEach { t ->
                        val isSelected = t == type
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) AmberSafety else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { type = t }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = t.label,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority selection
                Text(text = "Priorytet:", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Priority.values().forEach { p ->
                        val isSelected = p == priority
                        val color = p.getColor(isDark)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, color, RoundedCornerShape(6.dp))
                                .clickable { priority = p }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = p.label,
                                color = if (isSelected) Color.Black else color,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = AmberSafety,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = AmberSafety
                )

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł usterki / zlecenia *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Wyciek oleju, awaria serwonapędu", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Machine / Line
                OutlinedTextField(
                    value = machineId,
                    onValueChange = { machineId = it },
                    label = { Text("Maszyna / Linia / Urządzenie *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Prasa PH-600, Linia CNC-02", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_machine")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokalizacja na fabryce", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Hala A, Stanowisko 12", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Reporter info and phone for instant call
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reporterName,
                        onValueChange = { reporterName = it },
                        label = { Text("Zgłaszający", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("np. Mistrz zmiany", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = reporterPhone,
                        onValueChange = { reporterPhone = it },
                        label = { Text("Telefon zgłaszającego", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("+48 ...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = fieldColors,
                        modifier = Modifier.weight(1f).testTag("input_task_phone")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis objawów awarii i wstępna diagnoza", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("Kody błędów, wycieki, dźwięki, stan urządzeń...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    minLines = 3,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit button
                Button(
                    onClick = {
                        if (title.isNotBlank() && machineId.isNotBlank()) {
                            onSave(title, machineId, location, type, priority, reporterName, reporterPhone, description)
                        }
                    },
                    enabled = title.isNotBlank() && machineId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberSafety,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_task_btn")
                ) {
                    Text(
                        text = "Zaloguj zgłoszenie i wyślij alert",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
