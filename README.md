# KARE Aero — Easy Attendance App

A simple Android app for university teachers to record attendance per class session.

## Features

- **Take attendance**: pick a course, record date, time, period, mark each student present or absent, and note the topics taught.
- **History**: browse all past sessions, open any session for full details, or delete it.
- **Session details**: course name and code, date, time, period, topics taught, and complete present/absentee lists.
- **Share**: send any session summary via WhatsApp, email, etc.
- **Students & Courses**: manage the lists used by attendance sessions.

All data is stored locally on the device (Room / SQLite). No internet permission required.

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- Room database
- MVVM (AndroidViewModel + Kotlin Flows)

## Project structure

```
app/src/main/java/com/university/attendance/
├── MainActivity.kt          # Entry point, application class, view model
├── data/
│   └── Database.kt          # Room entities, DAOs, database
└── ui/
    └── AttendanceApp.kt     # Navigation and all screens
```

## Building the app

### Option A — run from Android Studio (recommended)

1. Install [Android Studio](https://developer.android.com/studio) (Ladybug or newer).
2. **File > Open** and select this project folder.
3. Wait for Gradle sync to finish (it downloads dependencies automatically).
4. Plug in your phone with **USB debugging** enabled (Settings > developer options), or start an emulator.
5. Press **Run** (Shift+F10). The app installs and launches on the device.

### Option B — build an APK and share it

1. Open the project in Android Studio.
2. **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
3. Click **locate** in the notification, or find the APK at
   `app/build/outputs/apk/debug/app-debug.apk`.
4. Copy that APK to any Android 7.0+ phone and open it to install
   (allow "Install unknown apps" if prompted).

## Using the app

1. **Courses** — add each course with its name and code (e.g. "Aerodynamics" / `AE2301`).
2. **Students** — add students with name and roll number.
3. **Take Attendance** — select the course, adjust date/time/period, toggle any absentees, type the topics taught, then **Save attendance**.
4. **History** — tap any session to review or share it; the share icon sends the full summary (present/absent lists included) via WhatsApp, Gmail, etc.
