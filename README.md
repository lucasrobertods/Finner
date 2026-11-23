# Finner — Multi-Currency Annual & Monthly Financial Planner

**Finner** (`com.lrds.finner`) is an Android personal and household financial planning application designed for multi-currency (`EUR` € and `BRL` R$) annual and monthly budget management. It combines an **offline-first** local Room database with real-time Firebase Cloud Firestore synchronization so household members can plan monthly budgets, track expenses across currencies, manage installment contracts, and forecast financial goals.

---

## Key Features

- **Annual & Monthly Planning (`JAN`–`DEC`)**:
  - Interactive year selector in the top bar that starts on the current year, supports creating new years (replicating December's categories, exchange rate, and recurring inflows across January–December of the new year), and deleting years with a confirmation bottom sheet.
  - Capsule/pill monthly tabs with horizontal pager navigation, opening automatically on the current calendar month.
- **Multi-Currency (`EUR` € & `BRL` R$) & Per-Month Exchange Rate**:
  - Primary dashboard and all transaction/installment entries are tracked in Euro (`€`), with Brazilian Real (`R$`) used exclusively for Brazil category planned targets.
  - Inline editable exchange rate badge (`1 € = R$ 5,50` default) customizable per month.
  - Brazil category math: `Planned` in `R$`, `Actual` and `Available` in `€`, with `Available = (Planned / EUR rate) - Actual`.
- **Flexible Category Budgets & Color-Coded Envelopes**:
  - Default envelopes (**Bills**, **Reserve**, **Joint**, **Personal**) plus custom user-defined buckets, each with distinct visual colors across cards, badges, progress bars, and chips.
  - Category planned amounts support either a **fixed value** or a **percentage of planned inflows** (automatically recalculating whenever planned inflows change, with a live preview on create/edit).
  - Newly created categories automatically replicate to subsequent months; deleting a category also removes its entries and lets the user choose between deleting only the current month or all months and years.
- **Category Reconciliation (`PLANNED` × `ACTUAL` × `AVAILABLE`)**:
  - Categories are grouped into three sections with section-level totals:
    1. **Inflows (€)** — Cash inflows (without bucket association).
    2. **Outflows (€)** — Local Euro outflows.
    3. **Brazil (R$)** — Brazilian Real planned commitments reconciled against Euro entries.
  - All single entries and installment contracts are editable and deletable (with confirmation sheets) directly from category cards.
  - One-tap `+` quick-entry action on any category card opens the entry sheet pre-filled with that category.
- **Installment Contracts (`1/N`) with Per-Month Paid Tracking**:
  - Automatic projection of multi-month installment contracts (`1/3`, `2/3`, `3/3`, or ongoing loans like `53/360`) across months and years.
  - Installment contracts track paid status per month (`paid_months`), can only be marked/unmarked as paid during the **current calendar month**, and are counted toward **Actual** values only once marked as paid.
- **Financial Objectives & Completion Forecast**:
  - Short-, medium-, and long-term goals that can be linked to a budget bucket (e.g., `Reserve`), aggregating bucket balances and forecasting completion month/year.
- **Household Cloud Sync & Authentication**:
  - Offline-first operation with optional real-time multi-device household sync powered by Firebase Authentication (Email/Password & Google Sign-In) and Cloud Firestore.
- **Revolut-Inspired Material 3 Design System & Jetpack Navigation 3**:
  - Revolut-inspired Stitch redesign with ambient top gradient aura, hero spare-cash summary + circular quick actions, dark/light `FinnerSectionCard` surfaces, frosted glass auto-hiding floating bottom navigation dock, and dedicated `MonthDetailsScreen` drill-down with type & bucket pill filters.
  - **Jetpack Navigation 3** (`androidx.navigation3`) multi-stack architecture with `@Serializable` type-safe `NavKey` routes (`Dashboard`, `MonthDetails(year, month, initialType, initialBucket)`, `Goals`, `History`, `Profile`), directional Shared-Axis/Fade-Through tab switches, horizontal slide + parallax drill-down transitions, native Predictive Back support, and independent per-tab scroll/state restoration across tab switches and configuration changes.

---

## Architecture

Finner follows **Clean Architecture** principles combined with **Unidirectional Data Flow (UDF)** and **MVVM**, organized into four main layers:

```text
app/src/main/java/com/lrds/finner/
├── core/
│   ├── currency/        # CurrencyConverter (integer cents/centavos arithmetic & formatting)
│   └── model/           # HouseholdAuthManager, HouseholdSession (Firebase Auth + Credential Manager)
├── data/
│   ├── local/
│   │   ├── converter/   # Room TypeConverters (LocalDate, enums)
│   │   ├── dao/         # CategoryDao, TransactionDao, InstallmentContractDao, ExchangeRateDao, GoalDao, BudgetBucketDao
│   │   ├── entity/      # Room @Entity models (CategoryEntity, TransactionEntity, InstallmentContractEntity, GoalEntity, etc.)
│   │   └── FinnerDatabase.kt  # Room DB v7 with incremental migrations (e.g., MIGRATION_6_7)
│   ├── remote/
│   │   └── FirestoreSyncDataSource.kt  # Real-time Firestore snapshot listeners & background push
│   └── repository/
│       ├── FinanceRepository.kt
│       └── OfflineFirstFinanceRepository.kt
├── domain/
│   ├── model/           # MonthlyDashboardData, CategoryReconciliationRow, EnvelopeSummary, ActiveInstallmentItem
│   └── usecase/
│       └── ObserveMonthlyDashboardUseCase.kt
└── ui/
    ├── dashboard/
    │   ├── components/  # FinnerTopBar, MonthPillTabs, ExchangeRateChip, FinnerSectionCard, CircularQuickActionButton, FinnerFloatingScaffold, finnerTopAura, bucketColorFor, bucketIconFor
    │   ├── MonthDetailsScreen.kt
    │   ├── MonthlyDashboardScreen.kt
    │   └── MonthlyDashboardViewModel.kt
    ├── goals/           # GoalsScreen
    ├── history/         # HistoryScreen
    ├── navigation/      # FinnerRoutes, NavigationState, Navigator, FinnerTransitions, FinnerAppHost (NavDisplay)
    ├── profile/         # ProfileScreen
    ├── theme/           # Material 3 Color.kt, Type.kt, Shape.kt, Theme.kt (+ FinnerExtendedColors & FinnerNumericStyles)
    └── transaction/     # ModalBottomSheets: QuickTransactionModal, CategoryDialog, GoalDialog, BucketDialog, ExchangeRateDialog, ConfirmDeleteBottomSheet, HouseholdSyncDialog
```

### Architectural Highlights
1. **Single Source of Truth (Offline-First)**:
   - All UI state streams originate from **Room** (`FinnerDatabase`, v7) via Kotlin `Flow`s. Writes persist immediately to Room with `SyncStatus.PENDING_UPLOAD` and push asynchronously to Cloud Firestore (`FirestoreSyncDataSource`). Incoming snapshot updates merge back into Room as `SyncStatus.SYNCED`.
2. **Exact Monetary Arithmetic**:
   - All monetary values (`plannedAmountCents`, `amountCents`, `monthlyAmountCents`, `targetAmountCents`) are stored and calculated as 64-bit integers (`Long`) representing minor units (cents/centavos) to avoid floating-point rounding drift.
3. **Reactive Domain Aggregation**:
   - `ObserveMonthlyDashboardUseCase` combines reactive streams for categories, transactions, installment contracts, exchange rates, and budget buckets per `(year, month)` into an immutable `MonthlyDashboardData` snapshot consumed by `MonthlyDashboardViewModel`.

---

## Android Technologies & Libraries Used

| Category | Technology / Library | Version / Details |
| :--- | :--- | :--- |
| **Language** | **Kotlin** | `2.2.10` (Coroutines, `Flow`, `StateFlow`) |
| **Build System** | **Android Gradle Plugin (AGP)** & **KSP** | AGP `9.4.1`, KSP `2.3.6`, Gradle Version Catalog (`libs.versions.toml`) |
| **SDK Levels** | **Android SDK** | `minSdk = 26` (Android 8.0), `targetSdk = 37`, `compileSdk = 37` |
| **UI Toolkit** | **Jetpack Compose** & **Material Design 3** | Compose BOM `2026.02.01`, `activity-compose:1.10.1`, `HorizontalPager`, `ModalBottomSheet`, `NestedScrollConnection`, bundled variable fonts (`Fraunces`, `Plus Jakarta Sans`) |
| **Navigation** | **Jetpack Navigation 3** & **Kotlinx Serialization** | `navigation3-runtime:1.0.1`, `navigation3-ui:1.0.1`, `lifecycle-viewmodel-navigation3:2.10.0`, `kotlinx-serialization-core:1.9.0` |
| **Architecture & Lifecycle** | **AndroidX Lifecycle & ViewModel** | `2.10.0` (`lifecycle-viewmodel-compose`, `collectAsStateWithLifecycle`, `dropUnlessResumed`) |
| **Local Persistence** | **Jetpack Room** | `2.7.0` (`room-runtime`, `room-ktx`, `room-compiler` via KSP) |
| **Backend & Cloud Sync** | **Firebase (BOM `33.10.0`)** | **Cloud Firestore** (real-time household sync & `firestore.rules`) and **Firebase Authentication** |
| **Identity & Sign-In** | **AndroidX Credentials & Google ID** | `androidx.credentials:1.5.0`, `credentials-play-services-auth`, `com.google.android.libraries.identity.googleid:1.1.1` |
| **Concurrency** | **Kotlinx Coroutines** | `1.10.1` (`kotlinx-coroutines-android`, `kotlinx-coroutines-play-services`) |
| **Testing** | **JUnit 4, Coroutines Test & Compose UI Test** | `junit:4.13.2`, `kotlinx-coroutines-test`, `androidx.test.ext:junit`, `espresso-core`, `ui-test-junit4` |
| **Security & Tooling** | **Gitleaks** | Local `gitleaks` pre-commit hook scanning staged changes (`--staged --redact`) |

---

## Setup, Security & Testing

- **Firebase Configuration (Local Only)**:
  - Place your Firebase Android config at `app/google-services.json` (gitignored so API keys are never committed). Firestore access is secured by [`firestore.rules`](firestore.rules).
- **Secret Scanning (`gitleaks`)**:
  - Install locally via `brew install gitleaks`.
  - The local `.git/hooks/pre-commit` hook runs `gitleaks git --staged --redact --no-banner` before every commit and reminds you if code/config changes are staged without updating `README.md`.
- **Run Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Assemble Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
