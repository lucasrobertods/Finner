# Project Rules & Architecture Conventions

## 1. Architecture & Layers
- **Pattern:** Strict Clean Architecture + MVVM (Data -> Domain -> UI).
- **Domain Layer:** Must remain 100% pure Kotlin with zero Android framework dependencies (no `android.*`, no `Context`).
- **State Management:** Use Kotlin `StateFlow` exposed from ViewModels via `asStateFlow()`. Avoid LiveData entirely.
- **Dependency Injection:** Use Hilt (`@HiltViewModel`, `@Inject`). Do not use manual constructor injection in Composables.

## 2. UI & Design System (Jetpack Compose)
- **Theme:** Always use `AppTheme` (never raw `MaterialTheme` or hardcoded colors/spacings). Use tokens defined in `ui/theme/`.
- **Edge-to-Edge:** All screens must handle window insets properly. Use `Modifier.safeDrawingPadding()` or consume `WindowInsets` at the scaffold level.
- **Previews:** Every standalone screen Composable must include a `@Preview` annotation supporting both light and dark themes using a custom preview wrapper.
- **State Hoisting:** Keep Composables stateless where possible; hoist state and callbacks up to the ViewModel-bound screen root.

## 3. Persistence & Networking
- **Local DB:** Room with Flow-returning queries for reactive UI updates. DAOs must be interfaces.
- **Network:** Retrofit + Kotlin Serialization (do not use Gson). All network calls must be wrapped in a safe coroutine dispatcher (`withContext(Dispatchers.IO)` or handled inside repositories).

## 4. Testing Requirements
- **Unit Tests:** ViewModels and UseCases require JUnit 5, MockK, and `Turbine` for testing `StateFlow` emissions.
- **UI Tests:** Compose UI tests must use semantic matchers (`hasTestTag`) rather than text lookups.

## 5. Coding & Workflow Guardrails
- **Build Verification:** Always run `./gradlew lintDebug` and `./gradlew testDebugUnitTest` after adding or modifying core logic.
- **File Structure:** Keep feature modules modularized under `features/<feature_name>/` or organize by clean layers inside `app/src/main/java/com/yourpackage/`.
- **Forbidden:** Do not introduce third-party logging libraries or unapproved dependencies without explicit confirmation.
