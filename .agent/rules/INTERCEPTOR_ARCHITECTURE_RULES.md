# 🛡️ Message & Media Interceptor Architecture Rules

This rule document governs all implementation work on the Message Recovery project.

---

## 1. Architectural Integrity & Patterns
- **Clean Architecture + MVVM + Hilt:** Strict separation between `data`, `domain`, `service`, and `presentation`. Use Dagger Hilt for dependency injection (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
- **Navigation 3:** Use AndroidX Navigation 3 (`androidx.navigation3`) for state-driven type-safe navigation.
- **State Hoisting Rules:** Every screen must strictly follow `STATE_HOISTING_GUIDELINES.md` (Stateful `*Screen` + Stateless `*Content` + `@Preview` with mock data).
- **Deduplication:** Always route incoming text and media events through `DeduplicationEngine` using the 2-second SHA-256 window before inserting into Room DB.
- **Zero Data Loss:** In `MediaFileObserverService`, media files must be duplicated immediately into `context.filesDir/vault/` upon `CLOSE_WRITE` or `CREATE` to safeguard against host app unsend routines.
- **Zero Play Store Constraints:** Utilize maximum privileges (`MANAGE_EXTERNAL_STORAGE`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `AccessibilityService`, `NotificationListenerService`).

---

## 2. Ingestion Rules & Target Package Mapping
- **WhatsApp (`com.whatsapp`):**
  - Notification: Extract `MessagingStyle.Message` from `Notification.EXTRA_MESSAGES`.
  - Media path: `/sdcard/Android/media/com.whatsapp/WhatsApp/Media/`
  - Unsend trigger text: `"This message was deleted"`, `"You deleted this message"`.
- **WhatsApp Business (`com.whatsapp.w4b`):**
  - Notification: Extract `MessagingStyle.Message` from `Notification.EXTRA_MESSAGES`.
  - Media path: `/sdcard/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/`
  - Unsend trigger text: `"This message was deleted"`, `"You deleted this message"`.
- **Messenger (`com.facebook.orca`):**
  - Notification: Extract `NotificationCompat.MessagingStyle` or parse `EXTRA_TITLE` / `EXTRA_TEXT`.
  - Inline media: Check `EXTRA_PICTURE` or `EXTRA_LARGE_ICON_BIG`.
  - Media path: `/sdcard/Pictures/Messenger/`, `/sdcard/Movies/Messenger/`, `/sdcard/Download/Messenger/`
  - Unsend trigger text: `"unsent a message"`, `"This message was unsent"`.
- **Instagram Direct (`com.instagram.android`):**
  - Notification: Extract handle from `EXTRA_TITLE` and body from `EXTRA_TEXT`.
  - Filter out telemetry notifications (likes, active status, live videos).
  - Media path: `/sdcard/Pictures/Instagram/`, `/sdcard/Movies/Instagram/`
  - Unsend trigger text: `"unsent a message"`.

---

## 3. Room Database & Threading
- Database queries and insertions must be asynchronous using Kotlin Coroutines and `Flow`.
- Foreign key cascading: deleting a thread cascades to messages; deleting a message sets `message_id` on attachments to `NULL` to keep orphaned media files preserved in the vault.
- `dedup_hash` must be indexed and unique.

---

## 4. Background & Process Immortality Rules
- Persistent foreground service must use low-priority ongoing notification (`PRIORITY_MIN` / `IMPORTANCE_LOW`).
- Implement watchdog to cycle component state via `PackageManager` (`COMPONENT_ENABLED_STATE_DISABLED` -> `COMPONENT_ENABLED_STATE_ENABLED`) when re-binding `NotificationListenerService` is required.
- Auto-restart on `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `ACTION_POWER_CONNECTED`.
