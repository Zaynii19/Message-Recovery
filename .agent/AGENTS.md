# Message Recovery App - Agent Instructions & Workspace Guide

## 1. Project Overview
- **App Name:** Message Recovery
- **Package ID:** `com.example.messagerecovery`
- **Platform:** Android (minSdk 28, targetSdk 37, compileSdk 37)
- **Primary Goal:** Provide a clean, privacy-centric message and notification recovery tool for Android (capturing incoming notifications, tracking deleted messages, and displaying categorized chat logs).

---

## 2. Technology Stack
- **Language:** Kotlin 2.4+
- **UI Framework:** Jetpack Compose (Material 3) with Compose BOM
- **Build System:** Gradle (Kotlin DSL, `build.gradle.kts`) with Android Gradle Plugin (AGP) 9.4.1
- **JDK Runtime:** Java 17+ (Android Studio bundled JBR at `C:\Program Files\Android\Android Studio\jbr`)
- **Architecture:** Clean Architecture / MVVM (Model-View-ViewModel) + StateFlow / Coroutines

---

## 3. Directory & Package Structure
```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/messagerecovery/
│   ├── MainActivity.kt
│   ├── presentation/
│   │   ├── theme/          # Color, Theme, Type
│   │   ├── ui/             # Compose Screens and Components
│   │   └── viewmodel/      # UI ViewModels
│   ├── data/               # Room DB, DAOs, Entities, Repository implementations
│   ├── domain/             # Domain models, Repository interfaces, Use cases
│   └── service/            # NotificationListenerService & Background workers
└── res/
```

---

## 4. Common Build & Test Commands
> When running Gradle commands in PowerShell, ensure `JAVA_HOME` points to the JBR or uses the configured `org.gradle.java.home`.

- **Assemble Debug APK:**
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew assembleDebug
  ```
- **Run Unit Tests:**
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew test
  ```
- **Clean Build:**
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew clean
  ```

---

## 5. Architectural & Design Guidelines
- **UI Design:** Adhere to Material 3 design principles with dark/light theme support, accessible typography, and smooth micro-interactions.
- **State Management:** Use unidirectional data flow (UDF) with Compose `State` or Kotlin Coroutines `StateFlow`.
- **Permissions:** For notification recovery, implement standard `NotificationListenerService` patterns and guide the user through the Android Notification Access permission flow.
- **Privacy & Storage:** Recovered messages should remain strictly on-device using local encrypted Room database or secure DataStore.
