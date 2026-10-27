package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.ui.theme.AmberSafety
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewExtraWorkDialog(
    initialDateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        machineOrClient: String,
        dateMillis: Long,
        hours: Double,
        note: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var machineOrClient by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("1.0") }
    var note by remember { mutableStateOf("") }

    val formattedDate = remember(initialDateMillis) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(initialDateMillis))
    }

    val hoursVal = hoursText.toDoubleOrNull() ?: 0.0

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
                            text = "Nowa Praca Dodatkowa",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Data: $formattedDate • Rozliczenie indywidualne",
                            color = AmberSafety,
                            fontSize = 11.5.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = AmberSafety,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = AmberSafety
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Zakres prac dodatkowych *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Montaż dodatkowych czujników, nadgodziny...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_extra_title")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = machineOrClient,
                    onValueChange = { machineOrClient = it },
                    label = { Text("Maszyna / Dział zleceniodawcy *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Linia montażu LM-2 / Dział Produkcji", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_extra_client")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it },
                    label = { Text("Liczba godzin (h) *", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. 2.5", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_extra_hours")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Settlement mode information banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tryb rozliczenia:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        text = "Rozliczenie indywidualne",
                        color = AmberSafety,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Szczegóły wykonanych czynności (co było robione)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Podłączenie do szafy sterowniczej, testy działania...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    minLines = 2,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Dodatkowe uwagi / materiały", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("np. Wykorzystano 10m przewodu, zlecenie mistrza", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && machineOrClient.isNotBlank() && hoursVal > 0.0) {
                            onSave(title, description, machineOrClient, initialDateMillis, hoursVal, note)
                        }
                    },
                    enabled = title.isNotBlank() && machineOrClient.isNotBlank() && hoursVal > 0.0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberSafety,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_extra_work_btn")
                ) {
                    Text(
                        text = "Zapisz pozycję pracy (${hoursVal} h)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
