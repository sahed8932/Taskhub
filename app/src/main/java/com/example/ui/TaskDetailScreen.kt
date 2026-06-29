package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskStep
import com.example.data.TaskStepSerializer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    viewModel: TaskHubViewModel,
    onNavigate: (Screen) -> Unit
) {
    val selectedTask by viewModel.selectedTask.collectAsState()
    val acceptedTaskIds by viewModel.acceptedTaskIds.collectAsState()
    val isAccepted = selectedTask?.let { it.id in acceptedTaskIds } ?: false
    var screenshots = remember { mutableStateListOf<String>() }
    var noteText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val task = selectedTask
    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Task not found.", color = TextLight)
        }
        return
    }

    val steps = remember(task) {
        TaskStepSerializer.deserialize(task.instructionsJson)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onNavigate(Screen.TasksList) }
                    .padding(vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TelegramBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Tasks", color = TelegramBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Title and Reward Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.category,
                        color = TelegramBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = TelegramBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "+${task.reward} BDT",
                            color = TelegramBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.title,
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TaskMetaBadge(icon = Icons.Filled.Timer, text = task.estimatedTime)
                    TaskMetaBadge(icon = Icons.Filled.TrendingUp, text = task.difficulty)
                    TaskMetaBadge(icon = Icons.Filled.Group, text = "${task.remainingSlots}/${task.totalSlots} Slots")
                }
            }
        }

        // Instructions steps section
        item {
            Text(
                text = "Instructions Steps",
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(steps) { step ->
            StepRow(step = step)
        }

        // Proof Submission section
        item {
            if (!isAccepted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Accept This Task",
                        color = TextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Accepting this task secures your slot for completion. You will then be able to submit your proof.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.acceptTask(task.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Accept Task", fontWeight = FontWeight.Bold, color = ColorPrimaryOnText)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    // Accepted Badge Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Active", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Task Status: Active & Accepted",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Submit Proof of Completion",
                        color = TextLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Screenshot simulator button
                    Text(
                        text = "Upload Screenshots (Proofs)",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Simulation upload trigger
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TelegramBlue.copy(alpha = 0.1f))
                                .border(1.dp, TelegramBlue, RoundedCornerShape(8.dp))
                                .clickable {
                                    // Add simulated high-quality mock screenshot
                                    val index = screenshots.size + 1
                                    screenshots.add("https://picsum.photos/400/800?random=$index")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Filled.AddAPhoto, contentDescription = "Add", tint = TelegramBlue, modifier = Modifier.size(20.dp))
                                Text("Mock Up", color = TelegramBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Display uploaded screenshots
                        screenshots.forEachIndexed { idx, url ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Screen\n#${idx + 1}",
                                    color = TextLight,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                                // Remove screenshot overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable { screenshots.removeAt(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove", tint = TextLight, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes Field
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("Add optional note/username/ID...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramBlue,
                            unfocusedBorderColor = HighDensityBorder,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (screenshots.isEmpty()) {
                                // Needs at least one screenshot simulation
                                screenshots.add("https://picsum.photos/400/800?random=default")
                            }
                            isSubmitting = true
                            viewModel.submitTask(task.id, screenshots.toList(), noteText)
                            onNavigate(Screen.UserDashboard)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Task proof", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TaskMetaBadge(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, HighDensityBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StepRow(step: TaskStep) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when (step.type.lowercase()) {
                    "warning" -> Color.Red.copy(alpha = 0.08f)
                    "note" -> TelegramBlue.copy(alpha = 0.08f)
                    else -> SlateCard
                }
            )
            .border(
                1.dp,
                when (step.type.lowercase()) {
                    "warning" -> Color.Red.copy(alpha = 0.3f)
                    "note" -> TelegramBlue.copy(alpha = 0.3f)
                    else -> HighDensityBorder
                },
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        // Step Badge Number
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when (step.type.lowercase()) {
                        "warning" -> Color.Red
                        "note" -> TelegramBlue
                        else -> TelegramBlue
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${step.stepNumber}",
                color = TextLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column {
            Text(
                text = step.text,
                color = TextLight,
                fontSize = 13.sp,
                fontWeight = when (step.type.lowercase()) {
                    "bold" -> FontWeight.Bold
                    "warning" -> FontWeight.Bold
                    else -> FontWeight.Normal
                }
            )
            if (step.type.lowercase() == "warning") {
                Text("Warning / Critical Step", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } else if (step.type.lowercase() == "note") {
                Text("Note / Context", color = TelegramBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
