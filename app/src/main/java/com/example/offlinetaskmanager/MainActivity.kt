
package com.example.offlinetaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.offlinetaskmanager.data.TaskDatabase
import com.example.offlinetaskmanager.data.TaskEntity
import com.example.offlinetaskmanager.data.TaskRepository
import com.example.offlinetaskmanager.viewmodel.TaskViewModel
import com.example.offlinetaskmanager.viewmodel.TaskViewModelFactory

private val Blue = Color(0xFF315BE8)
private val Background = Color(0xFFF5F7FC)
private val DarkText = Color(0xFF182039)
private val GrayText = Color(0xFF66708A)

data class SampleTask(
    val title: String,
    val course: String,
    val description: String,
    val deadline: String,
    val priority: String,
    val status: String,
    val id: Int = 0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)

        val taskViewModel = ViewModelProvider(
            this,
            factory
        )[TaskViewModel::class.java]

        setContent {
            MaterialTheme {
                TaskManagerApp(taskViewModel)
            }
        }
    }
}

@Composable
fun TaskManagerApp(viewModel: TaskViewModel) {
    val navController = rememberNavController()

    val taskEntities by viewModel.allTasks.collectAsState(
        initial = emptyList()
    )

    val tasks = taskEntities.map { entity ->
        SampleTask(
            id = entity.id,
            title = entity.title,
            course = entity.course,
            description = entity.description,
            deadline = entity.deadline,
            priority = entity.priority,
            status = entity.status
        )
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            TaskHomeScreen(
                tasks = tasks,
                onAddClick = {
                    navController.navigate("add")
                },
                onTaskClick = { taskId ->
                    navController.navigate("detail/$taskId")
                }
            )
        }

        composable("add") {
            AddTaskScreen(
                onCancel = {
                    navController.popBackStack()
                },
                onSave = { task ->
                    viewModel.insertTask(
                        TaskEntity(
                            title = task.title,
                            course = task.course,
                            description = task.description,
                            deadline = task.deadline,
                            priority = task.priority,
                            status = task.status
                        )
                    )

                    // Kembali ke halaman utama setelah menambah tugas.
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val taskId = entry.arguments?.getInt("taskId")
            val task = tasks.find { it.id == taskId }

            if (task != null) {
                TaskDetailScreen(
                    task = task,
                    onBack = {
                        navController.popBackStack()
                    },
                    onEdit = {
                        navController.navigate("edit/${task.id}")
                    },
                    onDelete = {
                        viewModel.deleteTask(
                            TaskEntity(
                                id = task.id,
                                title = task.title,
                                course = task.course,
                                description = task.description,
                                deadline = task.deadline,
                                priority = task.priority,
                                status = task.status
                            )
                        )

                        navController.popBackStack()
                    },
                    onStatusChange = { status ->
                        viewModel.updateTask(
                            TaskEntity(
                                id = task.id,
                                title = task.title,
                                course = task.course,
                                description = task.description,
                                deadline = task.deadline,
                                priority = task.priority,
                                status = status
                            )
                        )
                    }
                )
            }
        }

        composable(
            route = "edit/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val taskId = entry.arguments?.getInt("taskId")
            val task = tasks.find { it.id == taskId }

            if (task != null) {
                AddTaskScreen(
                    existingTask = task,
                    onCancel = {
                        navController.popBackStack()
                    },
                    onSave = { updatedTask ->
                        viewModel.updateTask(
                            TaskEntity(
                                id = task.id,
                                title = updatedTask.title,
                                course = updatedTask.course,
                                description = updatedTask.description,
                                deadline = updatedTask.deadline,
                                priority = updatedTask.priority,
                                status = updatedTask.status
                            )
                        )

                        // Kembali ke halaman utama setelah mengedit.
                        navController.popBackStack("home", false)
                    }
                )
            }
        }
    }
}

@Composable
fun TaskHomeScreen(
    tasks: List<SampleTask>,
    onAddClick: () -> Unit,
    onTaskClick: (Int) -> Unit
) {
    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Blue,
                contentColor = Color.White
            ) {
                Text("+", fontSize = 28.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Task Manager",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Kelola tugas kuliahmu dengan mudah.",
                fontSize = 14.sp,
                color = GrayText
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Tugas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Text(
                    text = "${tasks.size} tugas",
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "Belum ada tugas. Tekan + untuk menambahkan tugas.",
                    color = GrayText,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = tasks,
                        key = { task -> task.id }
                    ) { task ->
                        TaskCard(task = task) {
                            onTaskClick(task.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskScreen(
    existingTask: SampleTask? = null,
    onCancel: () -> Unit,
    onSave: (SampleTask) -> Unit
) {
    var title by remember(existingTask) {
        mutableStateOf(existingTask?.title ?: "")
    }
    var course by remember(existingTask) {
        mutableStateOf(existingTask?.course ?: "")
    }
    var description by remember(existingTask) {
        mutableStateOf(existingTask?.description ?: "")
    }
    var deadline by remember(existingTask) {
        mutableStateOf(existingTask?.deadline ?: "")
    }
    var priority by remember(existingTask) {
        mutableStateOf(existingTask?.priority ?: "Sedang")
    }
    var errorMessage by remember {
        mutableStateOf("")
    }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                text = if (existingTask == null) {
                    "Tambah Tugas"
                } else {
                    "Edit Tugas"
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Lengkapi informasi tugas kuliahmu.",
                color = GrayText,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Tugas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Mata Kuliah") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                item {
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline") },
                        placeholder = {
                            Text("Contoh: 20 Oktober 2026")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        text = "Prioritas",
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Rendah", "Sedang", "Tinggi").forEach { option ->
                            Button(
                                onClick = { priority = option },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (priority == option) {
                                        Blue
                                    } else {
                                        Color(0xFFE2E6F0)
                                    },
                                    contentColor = if (priority == option) {
                                        Color.White
                                    } else {
                                        DarkText
                                    }
                                )
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                item {
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFB3261E),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (
                        title.isBlank() ||
                        course.isBlank() ||
                        deadline.isBlank()
                    ) {
                        errorMessage =
                            "Nama tugas, mata kuliah, dan deadline wajib diisi."
                    } else {
                        onSave(
                            SampleTask(
                                id = existingTask?.id ?: 0,
                                title = title.trim(),
                                course = course.trim(),
                                description = description.trim(),
                                deadline = deadline.trim(),
                                priority = priority,
                                status = existingTask?.status ?: "Belum"
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                )
            ) {
                Text(
                    text = if (existingTask == null) {
                        "Simpan Tugas"
                    } else {
                        "Simpan Perubahan"
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE2E6F0),
                    contentColor = DarkText
                )
            ) {
                Text("Batal")
            }
        }
    }
}

@Composable
fun TaskDetailScreen(
    task: SampleTask,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Hapus Tugas?")
            },
            text = {
                Text("Tugas \"${task.title}\" akan dihapus dari daftar.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Ya, Hapus",
                        color = Color(0xFFB3261E)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                text = "Detail Tugas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailItem("Nama Tugas", task.title)
                    DetailItem("Mata Kuliah", task.course)
                    DetailItem(
                        "Deskripsi",
                        task.description.ifBlank {
                            "Tidak ada deskripsi"
                        }
                    )
                    DetailItem("Deadline", task.deadline)
                    DetailItem("Prioritas", task.priority)
                    DetailItem("Status", task.status)
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Ubah Status Tugas",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            listOf("Belum", "Proses", "Selesai").forEach { status ->
                Button(
                    onClick = {
                        onStatusChange(status)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.status == status) {
                            Blue
                        } else {
                            Color(0xFFE2E6F0)
                        },
                        contentColor = if (task.status == status) {
                            Color.White
                        } else {
                            DarkText
                        }
                    )
                ) {
                    Text(
                        text = if (task.status == status) {
                            "$status (Aktif)"
                        } else {
                            status
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                )
            ) {
                Text("Edit Tugas")
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB3261E)
                )
            ) {
                Text("Hapus Tugas")
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE2E6F0),
                    contentColor = DarkText
                )
            ) {
                Text("Kembali ke Daftar")
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = GrayText
        )

        Spacer(Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = DarkText
        )
    }
}

@Composable
fun TaskCard(
    task: SampleTask,
    onClick: () -> Unit
) {
    val statusColor = when (task.status) {
        "Selesai" -> Color(0xFF16845B)
        "Proses" -> Color(0xFFB7791F)
        else -> Blue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.course,
                fontSize = 12.sp,
                color = GrayText
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = task.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Deadline: ${task.deadline}",
                fontSize = 12.sp,
                color = DarkText
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Prioritas: ${task.priority}",
                fontSize = 12.sp,
                color = GrayText
            )

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = task.status,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    )
                )
            }
        }
    }
}
