package com.university.attendance.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rollNumber: String
)

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val date: String,
    val time: String,
    val period: String,
    val topicsTaught: String
)

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val present: Boolean
)

data class SessionWithCourse(
    @Embedded val session: Session,
    @Relation(parentColumn = "courseId", entityColumn = "id")
    val course: Course?
)

data class RecordWithStudent(
    @Embedded val record: AttendanceRecord,
    @Relation(parentColumn = "studentId", entityColumn = "id")
    val student: Student?
)

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name")
    fun observeAll(): Flow<List<Course>>

    @Insert
    suspend fun insert(course: Course): Long
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY rollNumber")
    fun observeAll(): Flow<List<Student>>

    @Insert
    suspend fun insert(student: Student): Long

    @Delete
    suspend fun delete(student: Student)
}

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM sessions ORDER BY date DESC, time DESC")
    fun observeAll(): Flow<List<SessionWithCourse>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :id")
    fun observeById(id: Long): Flow<SessionWithCourse?>

    @Insert
    suspend fun insert(session: Session): Long

    @Delete
    suspend fun delete(session: Session)
}

@Dao
interface RecordDao {
    @Transaction
    @Query("SELECT * FROM records WHERE sessionId = :sessionId")
    fun observeForSession(sessionId: Long): Flow<List<RecordWithStudent>>

    @Insert
    suspend fun insertAll(records: List<AttendanceRecord>)
}

@Database(
    entities = [Course::class, Student::class, Session::class, AttendanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun studentDao(): StudentDao
    abstract fun sessionDao(): SessionDao
    abstract fun recordDao(): RecordDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "attendance.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
