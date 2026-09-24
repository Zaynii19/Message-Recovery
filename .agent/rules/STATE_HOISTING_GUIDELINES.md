# 🏗️ Jetpack Compose State Hoisting Guidelines & Rules

This document establishes the mandatory architectural pattern for all Jetpack Compose screens in the project. It ensures that UI components are **100% previewable in Android Studio without crashes**, fully testable, and cleanly decoupled from business logic and dependency injection frameworks.

---

## 🎯 Core Principles & Rules

1. **Rule 1: Every Screen MUST be Split into 2 Composables:**
   - **`*Screen` (Stateful / Route Layer):** Responsible for Hilt ViewModels, collecting StateFlows, coroutines, and Navigation.
   - **`*Content` (Stateless / Pure UI Layer):** Responsible *only* for rendering the UI using plain data and emitting events via lambdas.
2. **Rule 2: NEVER Pass `ViewModel` or `NavController` to `*Content` or `@Preview`:**
   - Passing a ViewModel or NavController into a UI composable breaks Android Studio's `@Preview` renderer with `IllegalStateException: BridgeContext` errors.
3. **Rule 3: ZERO Business Logic in `*Content`:**
   - `*Content` composables must only accept values (primitives, data classes, UI states) and lambda callbacks. They must not make API calls, access databases, or query preferences.
4. **Rule 4: Zero changes to business logic, UI layouts, colors, modifiers, or comments:**
   - When migrating an existing screen, **DO NOT alter any business logic, UI layouts, colors, modifiers, padding, strings, typography, animations, or comments**.
   - Every line of logic, docstring, and visual element must remain 100% identical. The migration is strictly a structural separation of UI from ViewModel acquisition.
5. **Rule 5: Previews MUST target `*Content` with mock/sample data:**
   - Every `@Preview` composable must directly invoke the `*Content` composable using lightweight mock or sample data.

---

## 📐 The Standard 3-Layer Pattern

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Stateful Screen Composable (e.g. NotificationScreen)     │
│    - Accepts `NavController` & default `hiltViewModel()`    │
│    - Collects `StateFlow` as Compose `State`                │
│    - Handles double-tap prevention & navigation actions     │
└──────────────────────────────┬──────────────────────────────┘
                               │ passes plain state + callbacks
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Stateless Content Composable (e.g. NotificationContent)  │
│    - Receives plain data (e.g. `state: ApiResult<...>`)     │
│    - Emits actions via lambdas (e.g. `onBackClick: () -> Unit`)
│    - NO ViewModels, NO NavController, NO Hilt               │
└──────────────────────────────┬──────────────────────────────┘
                               │ rendered in IDE
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. @Preview Composable (e.g. ShowNotificationScreen)        │
│    - Directly invokes `NotificationContent`                 │
│    - Supplies mock / sample data for instant IDE rendering  │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 Standard Code Template

```kotlin
// ==========================================
// 1. STATEFUL LAYER (Used in AppNavGraph)
// ==========================================
@Composable
fun FeatureScreen(
    navController: NavController,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navigation safety guards preserved here
    var hasNavigated by remember { mutableStateOf(false) }

    FeatureContent(
        state = state,
        onBackClick = {
            if (!hasNavigated) {
                hasNavigated = true
                navController.popBackStack()
            }
        },
        onActionClick = { itemId ->
            viewModel.performAction(itemId)
        }
    )
}

// ==========================================
// 2. STATELESS UI LAYER (Pure Visuals)
// ==========================================
@Composable
fun FeatureContent(
    state: FeatureUiState,
    onBackClick: () -> Unit,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            // TopBar with onBackClick
        }
    ) { paddingValues ->
        // Render UI based strictly on `state`
    }
}

// ==========================================
// 3. PREVIEW LAYER (Instant IDE Rendering)
// ==========================================
@Preview(showSystemUi = true)
@Composable
fun FeatureContentPreview() {
    StepCountTheme {
        FeatureContent(
            state = FeatureUiState.Success(data = mockSampleData),
            onBackClick = {},
            onActionClick = {}
        )
    }
}
```

---

## 🚫 What NOT To Do

| ❌ Anti-Pattern | ✅ Correct Pattern |
|---|---|
| Calling `hiltViewModel()` inside `@Preview` | Call `*Content` inside `@Preview` with mock data |
| Passing `navController: NavController` to `*Content` | Pass lambda callbacks: `onBackClick: () -> Unit`, `onNavigate: (String) -> Unit` |
| Calling `viewModel.doSomething()` inside `*Content` | Pass lambda: `onDoSomething = { viewModel.doSomething() }` |
| Putting network/DB checks inside `*Content` | Keep side effects in `*Screen` using `LaunchedEffect` or ViewModel |

---

## 🛡️ Migration Quality Checklist

Before completing migration of any screen, verify:
- [ ] `./gradlew assembleDebug` compiles with **zero errors**.
- [ ] Android Studio Split Design/Code `@Preview` renders the screen immediately.
- [ ] All existing comments and docstrings are preserved.
- [ ] No visual layout, color, modifier, or typography changes were made.
- [ ] All double-tap navigation guards (`!hasNavigated`, `debounceClick`) remain intact.
