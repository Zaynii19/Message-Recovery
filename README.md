# 🛡️ Message Recovery

[![Platform: Android](https://img.shields.io/badge/Platform-Android_9.0+_(API_28–37)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Language: Kotlin](https://img.shields.io/badge/Kotlin-2.4+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI: Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database: Room](https://img.shields.io/badge/Database-Room_SQLite-FF6F00?logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Architecture: Clean MVVM](https://img.shields.io/badge/Architecture-Clean_MVVM_+_UDF-00C853)](https://developer.android.com/topic/architecture)

**Message Recovery** is a privacy-first, zero-data-loss notification interception and deleted message recovery engine for Android. It monitors incoming notifications and media directories to capture messages, voice notes, images, videos, and documents from supported messaging apps **before** the sender can unsend or delete them.

All captured data remains **100% strictly local** on your device inside an encrypted local Room database and sandboxed vault.

---

## ✨ Features

- 💬 **Zero-Data-Loss Notification Engine:**
  - Intercepts incoming messages across **WhatsApp**, **WhatsApp Business**, **Facebook Messenger**, and **Instagram Direct**.
  - Parses advanced framework notification bundles (`NotificationCompat.MessagingStyle`, `EXTRA_MESSAGES`, `EXTRA_TEXT`, `EXTRA_TITLE`).
  - Correctly captures both direct 1-on-1 chats and multi-participant group chats.

- 🗑️ **Deleted / Unsent Message Tracking:**
  - Instantly detects deletion tombstones (e.g., *"This message was deleted"*).
  - Flags previously captured messages as deleted and records the exact deletion timestamp.

- 📁 **Automated Multimedia Vault:**
  - FileObserver monitors active media directories (`/sdcard/Android/media/...`).
  - Clones media bytes (Images, Videos, Voice Notes / Audio, Documents) immediately into a sandboxed app vault before the original app can unlink them.
  - Video and audio files are stabilized with size checks to ensure uncorrupted playback.

- 🎨 **WAMR-Style Compact Chat UI:**
  - **Sleek Speech Bubbles:** Content-wrapping speech bubbles using WhatsApp-style dark color palettes (`#202C33` incoming, `#28171F` deleted message highlight).
  - **Inline Timestamps:** Compact layout puts short messages and timestamps side-by-side on the same line.
  - **Date Separator Chips:** Centered badges ("Today", "Yesterday", "22 September") partition conversations across calendar days.
  - **Auto-Scroll to Latest:** Opens directly at the most recent message with full fluid scroll-up to browse previous chat history.
  - **Integrated Voice Player:** Built-in audio player for voice notes with progress seeking and floating mini-player bar.

- 🔄 **Service Reliability & Watchdog:**
  - Sticky foreground service (`START_STICKY`) ensures Android system kill-resilience.
  - Automatic re-binding via official `NotificationListenerService.requestRebind()`.

---

## 📱 Supported Applications

| Application | Package Name | Media Storage Paths Monitored |
| :--- | :--- | :--- |
| **WhatsApp** | `com.whatsapp` | `/sdcard/Android/media/com.whatsapp/WhatsApp/Media/` |
| **WhatsApp Business** | `com.whatsapp.w4b` | `/sdcard/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/` |
| **Facebook Messenger** | `com.facebook.orca` | `/sdcard/Pictures/Messenger/`, `/sdcard/Movies/Messenger/`, `/sdcard/Download/Messenger/` |
| **Instagram Direct** | `com.instagram.android` | `/sdcard/Pictures/Instagram/`, `/sdcard/Movies/Instagram/` |

---

## 🏛️ Architecture & Project Structure

The project strictly follows **Clean Architecture** with MVVM and Unidirectional Data Flow (UDF):

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/messagerecovery/
│   ├── MainActivity.kt                      # Main activity & Compose Root
│   ├── presentation/
│   │   ├── navigation/                      # App Navigation routes & NavHost
│   │   ├── theme/                           # Material 3 colors, typography, theme
│   │   └── ui/
│   │       ├── chatlist/                    # Chat list & app filter tabs
│   │       ├── conversation/                # WAMR-style message bubble screen
│   │       ├── status/                      # Service health & permission status
│   │       └── onboarding/                  # Permission setup workflow
│   ├── domain/
│   │   ├── model/                           # RecoveredMessage, Thread, MediaAttachment
│   │   └── repository/                      # MessageRepository, MediaVaultRepository interfaces
│   ├── data/
│   │   ├── db/                              # Room Database, MessageDao, Entities
│   │   └── repository/                      # Concrete repository implementations
│   ├── service/
│   │   ├── RecoveryNotificationListener.kt  # NotificationListenerService engine
│   │   ├── MediaFileObserverService.kt      # Recursive file observer & media cloner
│   │   └── WatchdogForegroundService.kt     # Foreground keep-alive & watchdog
│   └── utils/                               # AudioPlayerManager, ServiceWatchdog
└── res/
```

---

## 🔐 Required Permissions & Setup

To enable message and media recovery, the application requires the following Android permissions:

1. **Notification Listener Access (`BIND_NOTIFICATION_LISTENER_SERVICE`):**
   Required to intercept notification bundles from supported apps.
2. **All Files Access (`MANAGE_EXTERNAL_STORAGE` / Media Permissions):**
   Required for `MediaFileObserverService` to monitor WhatsApp media trees and clone incoming media files before deletion.
3. **Foreground Service (`FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`):**
   Keeps the background watchdog active to prevent the OS from killing the notification engine.
4. **Ignore Battery Optimizations (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`):**
   Prevents OEM battery killers (MIUI/HyperOS, Samsung OneUI, etc.) from sleeping the recovery background service.

---

## 🛠️ Building & Running

### Prerequisites
- **Android Studio** (Hedgehog / Iguana / Ladybug or newer)
- **JDK 17+** (Bundled with Android Studio at `C:\Program Files\Android\Android Studio\jbr`)
- Android device or emulator running **Android 9.0 (API 28) to Android 15 (API 35+)**

### Build Commands (PowerShell)

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

Generated APK location:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Privacy Notice

- **Zero Cloud Communication:** No user messages, contacts, notifications, or media are ever uploaded to any server.
- **Local Sandbox Storage:** Recovered files reside exclusively in the application's private app directory (`context.filesDir/vault/`) and local SQLite database.
- **User Control:** Users can clear individual threads or delete the entire message history at any time from within the app.

---

## 📄 License
This project is developed for educational, backup, and personal diagnostic purposes.
