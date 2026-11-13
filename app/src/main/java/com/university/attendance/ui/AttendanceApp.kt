package com.university.attendance.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.university.attendance.AttendanceViewModel
import com.university.attendance.data.Course
import com.university.attendance.data.SessionWithCourse
import com.university.attendance.data.Student
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceApp(viewModel: AttendanceViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("courses") {
            CoursesScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable("students") {
            StudentsScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable("take") {
            TakeAttendanceScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable("history") {
            HistoryScreen(
                viewModel,
                onOpen = { id -> navController.navigate("session/$id") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("session/{sessionId}") { entry ->
            val sessionId = entry.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
            SessionDetailScreen(viewModel, sessionId, onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = { actions() }
            )
        },
        content = content
    )
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    AppScaffold(title = "Easy Attendance") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeCard(
                title = "Take Attendance",
                subtitle = "Date, time, period, course, present/absent, topics taught"
            ) { onNavigate("take") }
            HomeCard(
                title = "History",
                subtitle = "Past attendance sessions and their details"
            ) { onNavigate("history") }
            HomeCard(
                title = "Students",
                subtitle = "Manage the student list"
            ) { onNavigate("students") }
            HomeCard(
                title = "Courses",
                subtitle = "Manage course names and codes"
            ) { onNavigate("courses") }
        }
    }
}

@Composable
fun HomeCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CoursesScreen(viewModel: AttendanceViewModel, onBack: () -> Unit) {
    val courses by viewModel.courses.collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    AppScaffold(
        title = "Courses",
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add course")
            }
        }
    ) { padding ->
        if (courses.isEmpty()) {
            EmptyHint("No courses yet. Tap + to add one.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(courses, key = { it.id }) { course ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.code, fontWeight = FontWeight.SemiBold)
                                Text(
                                    course.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddCourseDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, code ->
                viewModel.addCourse(name, code)
                showAdd = false
            }
        )
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add course") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course code") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, code) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StudentsScreen(viewModel: AttendanceViewModel, onBack: () -> Unit) {
    val students by viewModel.students.collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    AppScaffold(
        title = "Students",
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add student")
            }
        }
    ) { padding ->
        if (students.isEmpty()) {
            EmptyHint("No students yet. Tap + to add one.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    student.rollNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteStudent(student) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete student")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddStudentDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, roll ->
                viewModel.addStudent(name, roll)
                showAdd = false
            }
        )
    }
}

@Composable
fun AddStudentDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var roll by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = roll,
                    onValueChange = { roll = it },
                    label = { Text("Roll number") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, roll) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(viewModel: AttendanceViewModel, onBack: () -> Unit) {
    val courses by viewModel.courses.collectAsState(initial = emptyList())
    val students by viewModel.students.collectAsState(initial = emptyList())

    var selectedCourseId by remember { mutableStateOf<Long?>(null) }
    var courseExpanded by remember { mutableStateOf(false) }
    var date by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var time by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    var period by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf("") }
    var present by remember(students) {
        mutableStateOf(students.associate { it.id to true })
    }

    val selectedCourse = courses.firstOrNull { it.id == selectedCourseId } ?: courses.firstOrNull()
    val canSave = selectedCourse != null && students.isNotEmpty()

    AppScaffold(title = "Take Attendance", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = courseExpanded,
                onExpandedChange = { courseExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCourse?.let { "${it.code} — ${it.name}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = courseExpanded,
                    onDismissRequest = { courseExpanded = false }
                ) {
                    if (courses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No courses yet — add one first") },
                            onClick = {}
                        )
                    }
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text("${course.code} — ${course.name}") },
                            onClick = {
                                selectedCourseId = course.id
                                courseExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = period,
                onValueChange = { period = it },
                label = { Text("Period (e.g. 3 or 10:00–11:00)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = topics,
                onValueChange = { topics = it },
                label = { Text("Topics taught") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Text(
                "Students — tap to toggle Present / Absent",
                style = MaterialTheme.typography.titleSmall
            )

            if (students.isEmpty()) {
                EmptyHint("No students yet. Add students from the home screen.", Modifier)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        val isPresent = present[student.id] ?: true
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        present = present.toMutableMap()
                                            .apply { put(student.id, !isPresent) }
                                    }
                                    .padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        student.rollNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    if (isPresent) "Present" else "Absent",
                                    color = if (isPresent) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Checkbox(
                                    checked = isPresent,
                                    onCheckedChange = { checked ->
                                        present = present.toMutableMap()
                                            .apply { put(student.id, checked) }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val course = selectedCourse ?: return@Button
                    viewModel.saveSession(
                        courseId = course.id,
                        date = date.trim(),
                        time = time.trim(),
                        period = period.trim(),
                        topics = topics,
                        marks = students.map { it.id to (present[it.id] ?: true) },
                        onDone = onBack
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save attendance")
            }
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: AttendanceViewModel,
    onOpen: (Long) -> Unit,
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState(initial = emptyList())

    AppScaffold(title = "History", onBack = onBack) { padding ->
        if (sessions.isEmpty()) {
            EmptyHint("No attendance sessions saved yet.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.session.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(item.session.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${item.course?.code ?: "?"} — ${item.course?.name ?: ""}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${item.session.date}   ${item.session.time}   ${item.session.period}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSession(item.session) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete session")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    viewModel: AttendanceViewModel,
    sessionId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val item by remember(sessionId) { viewModel.sessionFlow(sessionId) }
        .collectAsState(initial = null)
    val records by remember(sessionId) { viewModel.recordsFlow(sessionId) }
        .collectAsState(initial = emptyList())

    val session = item?.session
    val course = item?.course
    val presentList = records.filter { it.record.present }.mapNotNull { it.student }
    val absentList = records.filter { !it.record.present }.mapNotNull { it.student }

    val summary = buildString {
        appendLine("Course: ${course?.name ?: "?"} (${course?.code ?: "?"})")
        if (session != null) {
            appendLine("Date: ${session.date}   Time: ${session.time}   Period: ${session.period}")
            appendLine("Topics taught: ${session.topicsTaught.ifBlank { "-" }}")
        }
        appendLine()
        appendLine("Present (${presentList.size}):")
        presentList.forEach { appendLine(" - ${it.name} (${it.rollNumber})") }
        appendLine()
        appendLine("Absent (${absentList.size}):")
        absentList.forEach { appendLine(" - ${it.name} (${it.rollNumber})") }
    }

    AppScaffold(
        title = "Session",
        onBack = onBack,
        actions = {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, summary)
                }
                context.startActivity(Intent.createChooser(intent, "Share attendance"))
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
        }
    ) { padding ->
        if (session == null) {
            EmptyHint("Session not found.", Modifier.padding(padding))
            return@AppScaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DetailRow("Course", "${course?.name ?: "?"} (${course?.code ?: "?"})")
                        DetailRow("Date", session.date)
                        DetailRow("Time", session.time)
                        DetailRow("Period", session.period)
                        DetailRow("Topics taught", session.topicsTaught.ifBlank { "-" })
                    }
                }
            }
            item {
                Text(
                    "Present (${presentList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (presentList.isEmpty()) {
                item { Text("Nobody marked present.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(presentList, key = { "p${it.id}" }) { student ->
                    Text("• ${student.name} (${student.rollNumber})", style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Text(
                    "Absent (${absentList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (absentList.isEmpty()) {
                item { Text("No absentees.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(absentList, key = { "a${it.id}" }) { student ->
                    Text("• ${student.name} (${student.rollNumber})", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmptyHint(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
