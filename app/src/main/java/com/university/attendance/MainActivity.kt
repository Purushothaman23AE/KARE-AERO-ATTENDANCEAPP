package com.university.attendance

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import com.university.attendance.data.AppDatabase
import com.university.attendance.data.AttendanceRecord
import com.university.attendance.data.Course
import com.university.attendance.data.Session
import com.university.attendance.data.SessionWithCourse
import com.university.attendance.data.Student
import com.university.attendance.ui.AttendanceApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AttendanceApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
}

class AttendanceViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as AttendanceApplication).database

    val courses = db.courseDao().observeAll()
    val students = db.studentDao().observeAll()
    val sessions = db.sessionDao().observeAll()

    fun sessionFlow(id: Long): Flow<SessionWithCourse?> = db.sessionDao().observeById(id)
    fun recordsFlow(sessionId: Long) = db.recordDao().observeForSession(sessionId)

    fun addCourse(name: String, code: String) {
        if (name.isBlank()) return
        viewModelScope.launch { db.courseDao().insert(Course(name = name.trim(), code = code.trim())) }
    }

    fun addStudent(name: String, rollNumber: String) {
        if (name.isBlank()) return
        viewModelScope.launch { db.studentDao().insert(Student(name = name.trim(), rollNumber = rollNumber.trim())) }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch { db.studentDao().delete(student) }
    }

    fun saveSession(
        courseId: Long,
        date: String,
        time: String,
        period: String,
        topics: String,
        marks: List<Pair<Long, Boolean>>,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val sessionId = db.sessionDao().insert(
                Session(
                    courseId = courseId,
                    date = date,
                    time = time,
                    period = period,
                    topicsTaught = topics.trim()
                )
            )
            db.recordDao().insertAll(
                marks.map { (studentId, present) ->
                    AttendanceRecord(sessionId = sessionId, studentId = studentId, present = present)
                }
            )
            onDone()
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch { db.sessionDao().delete(session) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val viewModel: AttendanceViewModel = viewModel()
                    AttendanceApp(viewModel)
                }
            }
        }
    }
}
