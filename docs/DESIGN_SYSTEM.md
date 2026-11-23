# Finner Design System & Token Conventions

The **Finner Design System** (`com.lrds.finner.ui.theme`) is the single source of truth for all colors, spacing, component dimensions, corner radii, shapes, elevations, typography, and tabular numeric styles across the Android app.

Token definitions are aligned with the [Finner Stitch Design Project](https://stitch.google.com/projects/11478136884279065151) (OLED near-black dark mode + crisp neutral light mode, Revolut-inspired pills, 22dp soft cards, and tabular numeric figures).

---

## 1. Architecture & Theme Entry Point

All tokens are exposed through `FinnerTheme` using `CompositionLocalProvider` and `@ReadOnlyComposable` accessors:

| Accessor | File | Purpose |
| :--- | :--- | :--- |
| `FinnerTheme.colors` | [`Color.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Color.kt) | Semantic Light/Dark surface, accent, status, chrome, and budget bucket tokens |
| `FinnerTheme.spacing` | [`Spacing.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Spacing.kt) | 4dp-base spacing scale (`xxs`..`xxxl`) and semantic layout spacing |
| `FinnerTheme.sizes` | [`Size.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Size.kt) | Icon scale, touch targets, button/pill dimensions, progress bars, borders |
| `FinnerTheme.radius` | [`Shape.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Shape.kt) | Corner radius scale (`xs`, `sm`, `md`, `lg`, `xl`, `dock`) |
| `FinnerTheme.shapes` | [`Shape.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Shape.kt) | Pre-built `RoundedCornerShape` tokens derived directly from `FinnerRadius` |
| `FinnerTheme.elevation` | [`Size.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Size.kt) | Surface & floating dock shadow elevations |
| `FinnerTheme.typography` | [`Type.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Type.kt) | Plus Jakarta Sans text hierarchy with tabular figures (`tnum, lnum`) |
| `FinnerTheme.numericStyles` | [`Type.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Type.kt) | Financial amount & stat callout styles with `tnum, lnum` |
| `FinnerAlpha` | [`Color.kt`](../app/src/main/java/com/lrds/finner/ui/theme/Color.kt) | Standardized opacity constants for tinted badges, chip states, dividers, and auras |

`FinnerTheme` also bridges `FinnerColors.toMaterialColorScheme()`, `FinnerTypography.toMaterialTypography()`, and `FinnerShapeTokens.toMaterialShapes()` into `MaterialTheme` so standard Material 3 components (`OutlinedTextField`, `Button`, `FilterChip`, `SegmentedButton`, `ModalBottomSheet`) automatically inherit the exact same tokens without drift.

---

## 2. Token Reference

### 2.1 Colors (`FinnerTheme.colors`)

Raw hex primitives (`Color(0x...)`) are **`private`** to `Color.kt`. Feature screens must consume semantic roles:

- **Brand & Accent**: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondary`, `secondaryContainer`, `tertiary`, `error`, `onError`, `errorContainer`.
- **Surfaces & Neutrals**: `background`, `surface`, `onSurface`, `onSurfaceVariant`, `surfaceContainerLowest`, `surfaceContainerLow` (modal sheets), `surfaceContainer` (section cards), `surfaceContainerHigh` (pills & inactive controls), `surfaceContainerHighest` (progress tracks), `inverseSurface` / `inverseOnSurface` (high-contrast active pills & primary modal CTA).
- **Financial Semantics**:
  - `inflow`, `onInflow`, `inflowContainer` (positive delta / income / paid status)
  - `outflow`, `onOutflow`, `outflowContainer` (negative delta / deficit / local outflow)
  - `brazilAccent`, `onBrazilAccent`, `brazilContainer` (BRL commitments & installment badges)
  - `warning`, `onWarning`, `warningContainer`
- **Chrome & Ambient Effects**: `cardBorder`, `navDockBackground`, `heroGradientStart`, `heroGradientEnd`.
- **Budget Buckets (`FinnerTheme.colors.buckets`)**:
  - Contrast-tuned for both **Dark** (`DarkFinnerBucketColors`) and **Light** (`LightFinnerBucketColors`) themes.
  - Known envelopes (`bills`, `reserve`, `joint`, `personal`, `trip`) and deterministic hashing for custom buckets via `FinnerTheme.colors.buckets.forName(name)` (or the `@Composable` helper `bucketColorFor(name)`).

### 2.2 Spacing (`FinnerTheme.spacing`)

Built on a strict **4dp base grid** (with a `2.dp` micro step):

| Token | Value | Typical Usage |
| :--- | :--- | :--- |
| `xxs` | `2.dp` | Tight label/value vertical stacks, subtle icon gaps |
| `xs` | `4.dp` | Compact vertical gaps, chip row spacing, subtle divider padding |
| `sm` | `8.dp` | Inline element gaps, filter pill spacing, compact row padding |
| `md` | `12.dp` | Modal vertical gaps, form field spacing, action button row gaps |
| `lg` | `16.dp` | Screen horizontal margins, section spacing, sheet bottom spacer |
| `xl` | `20.dp` | Top bar and modal bottom sheet horizontal padding |
| `xxl` | `24.dp` | Hero section vertical breathing room |
| `xxxl` | `32.dp` | Large structural breaks |

Semantic layout aliases (`screenHorizontal`, `topBarHorizontal`, `sheetHorizontal`, `sheetVertical`, `cardPadding`, `sectionGap`, `itemGap`, `pillHorizontal`, `pillVertical`, `floatingNavBottomClearance`, `detailListBottomClearance`) map onto the scale so global layout adjustments happen in one place.

### 2.3 Sizes & Elevations (`FinnerTheme.sizes`, `FinnerTheme.elevation`)

- **Icons**: `iconXs` (`14.dp`), `iconSm` (`16.dp`), `iconMd` (`18.dp`), `iconLg` (`20.dp`), `iconXl` (`22.dp`), `iconXxl` (`24.dp`).
- **Targets & Containers**: `minTouchTarget` (`48.dp`), `primaryButtonHeight` (`52.dp`), `quickActionButton` (`52.dp`), `avatarLg` (`52.dp`), `iconButtonSm` (`28.dp`), `iconButtonMd` (`36.dp`), `iconButtonLg` (`40.dp`), `iconBadgeSm` (`36.dp`), `iconBadgeMd` (`40.dp`).
- **Pills & Indicators**: `pillMinHeight` (`38.dp`), `monthPillMinWidth` (`64.dp`), `statusDot` (`6.dp`), `progressBarSm` (`4.dp`), `progressBarMd` (`6.dp`).
- **Borders & Chrome**: `borderThin` (`1.dp`), `borderSelected` (`1.5.dp`), `floatingNavSlotHeight` (`84.dp`).
- **Elevation**: `none` (`0.dp`), `sm` (`2.dp`), `md` (`6.dp`), `lg` (`12.dp`), `floatingDock` (`16.dp`).

### 2.4 Radius & Shapes (`FinnerTheme.radius`, `FinnerTheme.shapes`)

All `FinnerShapeTokens` are derived from `FinnerRadius`:
- `xs` (`8.dp`) → `extraSmall`
- `sm` (`12.dp`) → `small`, `rowItem`
- `md` (`16.dp`) → `medium`, `actionButton`
- `lg` (`22.dp`) → `large`, `sectionCard`, `heroLedgerCard` (Stitch `rounded-[22px]`)
- `xl` (`28.dp`) → `extraLarge`, `bottomSheet` (top corners), `navDockItem`
- `dock` (`32.dp`) → `navDock`
- `pill` → `CircleShape`

### 2.5 Typography & Numeric Styles (`FinnerTheme.typography`, `FinnerTheme.numericStyles`)

- Font family: bundled `Plus Jakarta Sans` variable font (`R.font.plus_jakarta_sans_variable`) across weights `400`–`800`.
- **Tabular lining numerals (`tnum, lnum`)** are enabled on every `FinnerTypography` and `FinnerNumericStyles` definition so multi-currency (`EUR` / `BRL`) columns align vertically without jitter.
- Weight variants (`titleMediumBold`, `titleSmallBold`, `bodyMediumSemiBold`, `bodyMediumBold`, `bodySmallSemiBold`, `labelLargeBold`, `labelLargeMedium`, `labelMediumBold`, `labelMediumMedium`, `labelSmallBold`, `labelSmallMedium`) eliminate ad-hoc `fontWeight = FontWeight.*` overrides in Composables.
- Use `FinnerTheme.numericStyles` (`heroAmount`, `sectionAmount`, `cardAmount`, `rowAmount`, `rowAmountBold`, `metaAmount`) for all monetary figures.

---

## 3. Automated Enforcement Conventions (M2 Konsist & Detekt)

To prevent design drift, Milestone 2 (`M2`) Konsist architecture tests and Detekt custom/regex rules should enforce the following invariants for all Kotlin files under `com.lrds.finner.ui..` **excluding** `com.lrds.finner.ui.theme..`:

| Rule ID | Scope | Forbidden Pattern | Allowed Alternative |
| :--- | :--- | :--- | :--- |
| `DS-01` | Outside `ui/theme/` | `Color(0x...)` or `Color(0x...)` literals | `FinnerTheme.colors.*` (`Color.Transparent` allowed for overlay/scaffold containers) |
| `DS-02` | Outside `ui/theme/` | Raw `.dp` literals (`import androidx.compose.ui.unit.dp`) | `FinnerTheme.spacing.*`, `FinnerTheme.sizes.*`, `FinnerTheme.radius.*`, `FinnerTheme.elevation.*` |
| `DS-03` | Outside `ui/theme/` | Raw `.sp` literals (`import androidx.compose.ui.unit.sp`) or `fontSize =` | `FinnerTheme.typography.*` or `FinnerTheme.numericStyles.*` |
| `DS-04` | Outside `ui/theme/` | Inline `TextStyle(...)` construction or ad-hoc `FontWeight.*` overrides | Semantic style variants in `FinnerTheme.typography` / `FinnerTheme.numericStyles` |
| `DS-05` | Outside `ui/theme/` | Inline `RoundedCornerShape(...)` construction | `FinnerTheme.shapes.*` |
| `DS-06` | Outside `ui/theme/` | Magic alpha float literals (`alpha = 0.xxf`) | `FinnerAlpha.*` |
| `DS-07` | Inside `ui/theme/` | Any `FinnerNumericStyles` or `FinnerTypography` style missing `fontFeatureSettings = "tnum, lnum"` | Always apply `TABULAR_NUMBERS` (`"tnum, lnum"`) |
| `DS-08` | Inside `ui/theme/` | Public raw hex color `val`s in `Color.kt` | Keep hex primitives `private`; expose only semantic properties on `FinnerColors` |

### Reference Konsist Test Blueprint (for M2)

```kotlin
// Example Konsist assertions ready to drop into M2 architecture tests:
Konsist.scopeFromPackage("com.lrds.finner.ui..")
    .files
    .filterNot { it.packagee?.name == "com.lrds.finner.ui.theme" }
    .assertFalse { file ->
        file.hasImport {
            it.name in setOf(
                "androidx.compose.ui.unit.dp",
                "androidx.compose.ui.unit.sp",
                "androidx.compose.foundation.shape.RoundedCornerShape",
                "androidx.compose.ui.text.TextStyle",
                "androidx.compose.ui.text.font.FontWeight",
            )
        }
    }
```

### Reference Detekt `ForbiddenImport` / `ForbiddenMethodCall` Blueprint (for M2)

```yaml
style:
  ForbiddenImport:
    active: true
    imports:
      - value: 'androidx.compose.ui.unit.dp'
        reason: 'Use FinnerTheme.spacing, FinnerTheme.sizes, FinnerTheme.radius, or FinnerTheme.elevation.'
      - value: 'androidx.compose.ui.unit.sp'
        reason: 'Use FinnerTheme.typography or FinnerTheme.numericStyles.'
      - value: 'androidx.compose.foundation.shape.RoundedCornerShape'
        reason: 'Use FinnerTheme.shapes.'
      - value: 'androidx.compose.ui.text.TextStyle'
        reason: 'Use FinnerTheme.typography or FinnerTheme.numericStyles.'
    excludes:
      - '**/ui/theme/**'
      - '**/test/**'
      - '**/androidTest/**'
```
