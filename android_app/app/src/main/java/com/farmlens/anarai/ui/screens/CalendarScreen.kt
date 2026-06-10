package com.farmlens.anarai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmlens.anarai.data.AppDatabase
import com.farmlens.anarai.data.SprayScheduleEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scheduleList by db.sprayScheduleDao().getAllSchedules().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treatment Calendar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { padding ->
        if (scheduleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("No upcoming treatments scheduled.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(scheduleList) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onToggleComplete = {
                            coroutineScope.launch {
                                db.sprayScheduleDao().update(schedule.copy(isCompleted = !schedule.isCompleted))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showDialog) {
            AddEditScheduleDialog(
                schedule = null, // null means add new
                onDismiss = { showDialog = false },
                onSave = { chemical, notes, dateMillis ->
                    coroutineScope.launch {
                        db.sprayScheduleDao().insert(
                            SprayScheduleEntity(
                                dateMillis = dateMillis,
                                chemicalName = chemical,
                                notes = notes
                            )
                        )
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ScheduleCard(schedule: SprayScheduleEntity, onToggleComplete: () -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isCompleted) Color(0xFFE8F5E9) else Color(0xFFBBDEFB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (schedule.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Toggle Completion",
                    tint = if (schedule.isCompleted) Color(0xFF388E3C) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.chemicalName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (schedule.isCompleted) Color.Gray else MaterialTheme.colorScheme.primary,
                    textDecoration = if (schedule.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (schedule.notes.isNotEmpty()) {
                    Text(
                        text = schedule.notes,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                Text(
                    text = if (schedule.isCompleted) "Completed" else "Scheduled for: ${dateFormat.format(Date(schedule.dateMillis))}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp),
                    fontWeight = if (schedule.isCompleted) FontWeight.Bold else FontWeight.Normal
                )
            }
            TextButton(onClick = { showEditDialog = true }) {
                Text("Edit", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }

    if (showEditDialog) {
        AddEditScheduleDialog(
            schedule = schedule,
            onDismiss = { showEditDialog = false },
            onSave = { chemical, notes, dateMillis ->
                coroutineScope.launch {
                    db.sprayScheduleDao().update(
                        schedule.copy(
                            chemicalName = chemical,
                            notes = notes,
                            dateMillis = dateMillis
                        )
                    )
                }
                showEditDialog = false
            },
            onDelete = {
                coroutineScope.launch {
                    db.sprayScheduleDao().delete(schedule)
                }
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    schedule: SprayScheduleEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var chemical by remember { mutableStateOf(schedule?.chemicalName ?: "") }
    var notes by remember { mutableStateOf(schedule?.notes ?: "") }
    var dateMillis by remember { mutableStateOf(schedule?.dateMillis ?: (System.currentTimeMillis() + 86400000)) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            dateMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule == null) "Add Treatment" else "Edit Treatment", color = MaterialTheme.colorScheme.primary) },
        text = {
            Column {
                OutlinedTextField(
                    value = chemical,
                    onValueChange = { chemical = it },
                    label = { Text("Chemical Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateFormat.format(Date(dateMillis)))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chemical.isNotBlank()) onSave(chemical, notes, dateMillis)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = Color.Red)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    )
}
