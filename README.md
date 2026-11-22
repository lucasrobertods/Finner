# Finner — Multi-Currency Annual & Monthly Financial Planner

**Finner** (`com.lrds.finner`) is a modern Android personal and household financial planning application designed for multi-currency (`EUR` € and `BRL` R$) annual and monthly budget management. It combines an **offline-first** local database with real-time cloud synchronization so family members can plan monthly budgets, track expenses across currencies, manage installment contracts, and project when they will reach their financial goals.

---

## Key Features

- **Annual & Monthly Planning (`JAN`–`DEZ`)**:
  - Interactive year selector in the top bar that starts on the current year and lets users create subsequent years (automatically carrying over `DEZ` categories, exchange rate, and recurring inflows into `JAN`–`DEZ` of the new year).
  - Capsule/pill monthly tabs with horizontal pager navigation, opening automatically on the current calendar month.
- **Multi-Currency (`EUR` € & `BRL` R$) & Per-Month Exchange Rate**:
  - Primary base currency in Euro (`€`) with automatic conversion for Brazilian Real (`R$`) commitments and transactions.
  - Inline editable exchange rate badge (`1 € = R$ 5,50 ✏️` default) customizable per month.
- **Color-Coded Budget Buckets (Envelopes)**:
  - Default envelopes (**Bills**, **Reserve**, **Joint**, **Personal**) plus custom user-defined buckets, each with a distinct visual color across cards, badges, progress bars, and form chips.
  - Real-time calculation of each bucket's share of total monthly income (`%` of total inflows) and `Realizado × Previsto` progress bars.
- **Separated & Ordered Category Reconciliation (`Previsto × Realizado × Disponível`)**:
  - Categories are independent per month and grouped into three dedicated sections with section-level totals:
    1. **Entradas (€)** — Cash inflows (without bucket association).
    2. **Saídas (€)** — Local Euro outflows.
    3. **Brasil (R$)** — Brazilian Real commitments (displayed in both `R$` and converted `€`).
  - Ordered by bucket and then alphabetically by name.
  - Editing or creating categories, inflows, or transactions in month `M < 12` prompts the user whether to apply the change only to the current month or propagate it across subsequent months (`M..DEZ`).
  - One-tap `+` quick-entry action on any category card opens the transaction sheet pre-filled with that category's metadata.
- **Installment Contracts (`Parcelado 1/N`)**:
  - Automatic projection of multi-month installment contracts (`1/3`, `2/3`, `3/3`, or ongoing loans like `34/48`) across months and years without manual duplication.
- **Financial Objectives & Completion Forecast (`Objetivos`)**:
  - Short-, medium-, and long-term goals that can be linked to a specific budget bucket (e.g., `Reserve`).
  - Automatically aggregates the bucket's accumulated balance into the goal's progress and calculates the estimated completion month and year (`Previsão: MÊS/ANO`) based on the monthly contribution pace.
- **Household Cloud Sync & Authentication**:
  - Offline-first operation with optional real-time multi-device household sync powered by Firebase Authentication (Email/Password & Google Sign-In) and Cloud Firestore.
- **Modern Material 3 UI/UX**:
  - Floating auto-hiding pill bottom navigation bar (`Meses`, `Objetivos`, `Histórico`, `Perfil`) with nested scroll connection, paired with an anchored Extended FAB and Material 3 `ModalBottomSheet` forms with inline field validation and delete confirmation sheets.

---

## Architecture

Finner follows **Clean Architecture** principles combined with **Unidirectional Data Flow (UDF)** and the **MVVM (Model-View-ViewModel)** pattern, organized into four main layers:

```text
app/src/main/java/com/lrds/finner/
├── core/
│   ├── currency/        # CurrencyConverter (integer cents/centavos arithmetic & pt-BR formatting)
│   └── model/           # HouseholdAuthManager, HouseholdSession (Firebase Auth + Credential Manager)
├── data/
│   ├── local/
│   │   ├── converter/   # Room TypeConverters (LocalDate, enums)
│   │   ├── dao/         # CategoryDao, TransactionDao, InstallmentContractDao, ExchangeRateDao, GoalDao, BudgetBucketDao
│   │   ├── entity/      # Room @Entity models (CategoryEntity, TransactionEntity, GoalEntity, etc.)
│   │   └── FinnerDatabase.kt
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
    │   ├── components/  # FinnerTopBar, MonthPillTabs, ExchangeRateChip, FinnerFloatingScaffold, bucketColorFor
    │   ├── MonthlyDashboardScreen.kt
    │   └── MonthlyDashboardViewModel.kt
    ├── theme/           # Material 3 Color, Theme, Typography
    └── transaction/     # ModalBottomSheets: QuickTransactionModal, CategoryDialog, GoalDialog, BucketDialog, ExchangeRateDialog, ConfirmDeleteBottomSheet, HouseholdSyncDialog
```

### Architectural Highlights
1. **Single Source of Truth (Offline-First)**:
   - All UI state streams originate from **Room** (`FinnerDatabase`) via Kotlin `Flow`s. Writes are persisted immediately to Room with `SyncStatus.PENDING_UPLOAD` and pushed asynchronously to Cloud Firestore (`FirestoreSyncDataSource`). Incoming Firestore snapshot updates are merged directly into Room with `SyncStatus.SYNCED`.
2. **Exact Monetary Arithmetic**:
   - All monetary values (`plannedAmountCents`, `amountCents`, `targetAmountCents`) are stored and calculated as 64-bit integers (`Long`) representing minor units (cents/centavos) to prevent floating-point rounding errors.
3. **Reactive Domain Aggregation**:
   - `ObserveMonthlyDashboardUseCase` combines reactive streams for categories, transactions, installment contracts, exchange rates, and budget buckets for each `(year, month)` pair into an immutable `MonthlyDashboardData` snapshot consumed by `MonthlyDashboardViewModel`.

---

## Android Technologies & Libraries Used

| Category | Technology / Library | Version / Details |
| :--- | :--- | :--- |
| **Language** | **Kotlin** | `2.2.10` (Coroutines, `Flow`, `StateFlow`) |
| **Build System** | **Android Gradle Plugin (AGP)** & **KSP** | AGP `9.4.1`, KSP `2.3.6`, Gradle Version Catalog (`libs.versions.toml`) |
| **SDK Levels** | **Android SDK** | `minSdk = 26` (Android 8.0), `targetSdk = 37`, `compileSdk = 37` |
| **UI Toolkit** | **Jetpack Compose** & **Material Design 3** | Compose BOM `2026.02.01`, `HorizontalPager`, `ModalBottomSheet`, `NestedScrollConnection`, `AnimatedVisibility`, Material Icons Extended |
| **Architecture & Lifecycle** | **AndroidX Lifecycle & ViewModel** | `2.8.7` (`lifecycle-viewmodel-compose`, `collectAsStateWithLifecycle`) |
| **Local Persistence** | **Jetpack Room** | `2.7.0` (`room-runtime`, `room-ktx`, `room-compiler` via KSP) |
| **Backend & Cloud Sync** | **Firebase (BOM `33.10.0`)** | **Cloud Firestore** (real-time household sync & security rules) and **Firebase Authentication** |
| **Identity & Sign-In** | **AndroidX Credentials & Google ID** | `androidx.credentials:1.5.0`, `credentials-play-services-auth`, `com.google.android.libraries.identity.googleid:1.1.1` |
| **Concurrency** | **Kotlinx Coroutines** | `1.10.1` (`kotlinx-coroutines-android`, `kotlinx-coroutines-play-services`) |
| **Testing** | **JUnit 4, Coroutines Test & Compose UI Test** | `junit:4.13.2`, `kotlinx-coroutines-test`, `androidx.test.ext:junit`, `espresso-core`, `ui-test-junit4` |

---

## Building & Running Tests

- **Assemble Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Run Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Firebase Configuration**:
  - Project is linked to Firebase project `lrds-finner` via [`app/google-services.json`](app/google-services.json) and secured with [`firestore.rules`](firestore.rules).
