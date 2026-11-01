# Obsidian Calc — Advanced Scientific Calculator

An engineering-grade scientific calculator for Android designed with a deep OLED dark theme, real-time algebraic expression evaluation, an expandable scientific function matrix, memory registers, and interactive calculation tape history.

## User Review & Critical Decisions

> [!IMPORTANT]
> The user confirmed "Ok" to all recommended options during Phase 1 clarification. The plan proceeds with the recommended defaults across all three dimensions:

- **Confirmed Decision 1 — Scientific Key Access**: **Expandable Scientific Drawer / Toggle Panel** positioned directly above the primary numeric and operator keypad, allowing instant switching between a streamlined everyday layout and the full two-page (`1st` / `2nd`) scientific matrix.
- **Confirmed Decision 2 — Advanced Mathematical Scope**: **Comprehensive Scientific Suite** including trigonometry (`sin`, `cos`, `tan`, `asin`, `acos`, `atan`), hyperbolic functions (`sinh`, `cosh`, `tanh`), logarithms (`ln`, `log10`, `log2`), powers & roots (`x²`, `x³`, `xʸ`, `√`, `∛`, `ʸ√x`), factorials (`n!`), permutations/combinations (`nPr`, `nCr`), modulo (`mod`), absolute value (`|x|`), physical/mathematical constants (`π`, `e`, `φ`, `c`, `g`), and interactive history tape + memory registers (`MC`, `MR`, `M+`, `M-`, `MS`).
- **Confirmed Decision 3 — Dark Mode Aesthetic**: **Deep OLED Black (`#07090E`) with Vivid Cyan (`#00E5FF`) & Warm Amber (`#FFB300`) Accents**, engineered to reduce eye strain, maximize contrast on AMOLED screens, and visually separate numbers, scientific functions, and primary operators.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a fast, tactile, high-precision calculator that evaluates complex nested mathematical expressions with real-time live result previews as the user types, backed by a searchable/reusable history tape and dedicated memory registers.
- **Target Audience / Persona**: Students, engineers, scientists, and power users who need desktop-grade expression parsing, angle-unit awareness (`DEG` / `RAD` / `GRAD`), and instant recall of previous calculations on mobile and tablet form factors.
- **Key Value**: Eliminates the frustration of single-step calculators by displaying the full mathematical expression alongside a live evaluated preview, catching syntax or domain errors gracefully, and letting users tap any historical entry to re-insert its expression or result.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Live Expression Entry & Instant Preview**:
   - As the user taps digits, parentheses, and scientific functions (e.g., `sin(45) + ln(e^2)`), the top display renders the formatted expression with smart cursor support and auto-matched closing parenthesis hints.
   - Directly below the active expression, a live preview calculates and displays the result in real time before `=` is pressed.
2. **Scientific Drawer & `2nd` Function Shift**:
   - Users can toggle the **Scientific Matrix** open or closed with a smooth spring animation (`AnimatedVisibility`), or keep it permanently visible on larger screens.
   - Tapping the `2nd` shift button flips trigonometric and logarithmic keys to their inverse/alternate counterparts (`sin⁻¹`, `cos⁻¹`, `tan⁻¹`, `sinh`, `cosh`, `tanh`, `10ˣ`, `eˣ`, `∛x`, `nCr`).
3. **Angle Mode & Output Format Switching**:
   - A status bar pill in the display header lets users cycle between **DEG**, **RAD**, and **GRAD** with a single tap, immediately recalculating the live preview.
   - Users can also toggle output formatting between **Standard Decimal**, **Scientific Notation (`SCI`)**, and **Engineering Notation (`ENG`)**.
4. **Constants & History Tape Sheet**:
   - Tapping the **Constants (`CONST`)** chip opens a quick-insert bar with fundamental mathematical and physics constants (`π`, `e`, Golden Ratio `φ`, Speed of Light `c`, Gravitational Acceleration `g`, Avogadro's Constant `N_A`, Planck's Constant `h`).
   - Tapping the **History Tape** icon reveals past calculations with timestamps; tapping an item loads either its expression or its result into the active workspace, and users can pin favorite calculations or clear history.

### Visual Identity & Theme
- **Aesthetic Direction**: Precision instrument / luxury dark hardware aesthetic inspired by modern physical graphing calculators and high-contrast OLED instrumentation.
- **Color Palette & Mood**:
  - `--background` / Canvas: Deep Obsidian Black (`#07090E`)
  - `--surface` / Display Well: Recessed Carbon (`#0F141C`) with subtle top/bottom inner border highlights (`#1E293B`)
  - `--surface-variant` / Scientific Keys: Slate Charcoal (`#151C28`) with crisp `#94A3B8` labels
  - `--numeric-key` / Digit Keys: Elevated Graphite (`#1C2536`) with pure white (`#F8FAFC`) numerals
  - `--primary` / Vivid Cyan (`#00E5FF`): Active mode indicators (`DEG`/`RAD`), cursor, scientific function accents, and `=` button gradient
  - `--secondary` / Warm Amber (`#FFB300`): Primary arithmetic operators (`÷`, `×`, `−`, `+`) and active `2nd` shift state
  - `--error` / Coral Red (`#FF5252`): Clear (`AC`), Backspace (`⌫`), and domain error indicators (e.g., division by zero)
- **Typography & Hierarchy**:
  - **Display / Numeric Readout**: `JetBrains Mono` (bundled locally via `font-util`) for crisp tabular numeral alignment, unambiguous mathematical symbols, and zero horizontal jitter during live previews.
  - **UI & Function Labels**: `Space Grotesk` (bundled locally via `font-util`) for clean, technical button labels, mode badges, and history headers.
- **Component Styling & Layout**:
  - Adaptive layout using `BoxWithConstraints`:
    - **Compact (Phone Portrait)**: Top recessed OLED display well (32% height), collapsible scientific drawer + memory/constants strip, and 4x5 primary tactile button grid with `minimumInteractiveComponentSize()` (≥48dp touch targets).
    - **Medium / Expanded (Landscape & Tablets)**: Canonical two-pane layout placing the History Tape & Constants library alongside a side-by-side Scientific + Primary keypad.
- **Interactive Feedback & Motion**:
  - Haptic feedback (`LocalHapticFeedback`) on every key press.
  - Material 3 ripple effects and spring-based scale feedback on button press.
  - Smooth `AnimatedVisibility` spring transitions when expanding/collapsing the scientific panel or switching `1st` / `2nd` key layers.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Custom Recursive-Descent Expression Parser vs. Single-Register Accumulator**
  - *Chosen Approach*: Full recursive-descent mathematical expression tokenizer and AST evaluator supporting operator precedence (PEMDAS), implicit multiplication (e.g., `2π`, `3sin(30)`, `(2+3)(4+5)`), nested parentheses, postfix operators (`!`, `%`), and binary scientific operators (`xʸ`, `ʸ√x`, `nPr`, `nCr`, `mod`).
  - *Why*: Engineers and students need to see and edit full formulas before committing, rather than losing track of intermediate states.
  - *Alternatives Considered*: Immediate-execution calculators (like basic desktop calculators) are harder to audit for multi-term scientific formulas.
- **Decision 2: In-Memory + Session State via MVVM `StateFlow`**
  - *Chosen Approach*: Unidirectional data flow in `CalculatorViewModel` using `MutableStateFlow<CalculatorUiState>` holding the expression string, cursor/tokens, angle mode, notation mode, memory register value, and session history list (pre-populated with nothing so empty-state guidance shines, plus quick one-tap sample formulas in the empty history view).
  - *Why*: Keeps startup instantaneous, requires zero network or login friction, and follows Android architecture guidelines for single-session utilities while keeping state resilient across configuration changes.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         MainActivity (Edge-to-Edge)                       │
│                    ObsidianCalcTheme (OLED Dark Palette)                  │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│               CalculatorScreen (Adaptive BoxWithConstraints)              │
├──────────────────────┬───────────────────────────┬────────────────────────┤
│  DisplayWellSection  │   ScientificControlPanel  │   History & Constants  │
│  • Mode Badges       │   • Memory Bar (MC/MR/M+) │   • Modal BottomSheet  │
│    (DEG/RAD/GRAD,    │   • Constants Quick-Strip │     (Compact) or       │
│     DEC/SCI/ENG)     │   • Expandable Sci Grid   │     Side Pane (Tablet) │
│  • Expression Scroll │     with 1st / 2nd Shift  │   • Pin / Reuse /      │
│  • Live Preview      │   • 4×5 Primary Keypad    │     Clear Controls     │
└──────────┬───────────┴─────────────┬─────────────┴───────────┬────────────┘
           │                         │                         │
           └─────────────────────────┼─────────────────────────┘
                                     ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                   CalculatorViewModel (StateFlow<UiState>)                │
│  • Handles key intents, parenthesis auto-balancing, memory ops, modes     │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                     MathEngine (Tokenizer + AST Evaluator)                │
│  • Lexical Tokenizer (Numbers, Constants, Unary/Binary Ops, Implicit ×)   │
│  • Recursive Descent Parser (Precedence: () → Postfix → ^ → ×/÷ → +/-)    │
│  • Angle Unit Converter (DEG / RAD / GRAD) & Precision Formatter          │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- `CalculatorUiState`:
  - `expression`: Current raw/formatted mathematical expression string.
  - `livePreview`: Real-time evaluated result string (or subtle hint when expression is incomplete).
  - `errorMessage`: Explicit mathematical domain error (e.g., `"Cannot divide by zero"`, `"Domain error: √ of negative"`) only triggered on `=` evaluation.
  - `angleMode`: `AngleMode.DEG`, `AngleMode.RAD`, `AngleMode.GRAD`.
  - `notationMode`: `NotationMode.NORMAL`, `NotationMode.SCIENTIFIC`, `NotationMode.ENGINEERING`.
  - `isScientificExpanded`: Boolean controlling the expandable scientific keypad drawer.
  - `isSecondFunctionActive`: Boolean toggling `1st` vs `2nd` scientific layers (`sin` ↔ `asin`, `ln` ↔ `eˣ`, etc.).
  - `memoryValue`: `Double` storing the current `M` register with an indicator badge in the display well when non-zero.
  - `history`: List of `CalculationRecord(id, expression, result, angleMode, timestamp, isPinned)`.

### Interactive Component & State Mapping
- **Mode Header Pills (`DEG/RAD/GRAD`, `NORM/SCI/ENG`, `History`, `Constants`)**: Tapping cycles the active mode in `CalculatorViewModel` and immediately re-runs `MathEngine.evaluate()` so the live preview updates in place.
- **Memory Register Strip (`MC`, `MR`, `M+`, `M-`, `MS`)**: Operates on the currently evaluated value or live preview, updating `memoryValue` and displaying an `M = ...` chip inside the OLED display well.
- **Scientific Matrix & `2nd` Toggle**: Dispatches structured `CalculatorAction` events (`InsertFunction("sin(")`, `InsertConstant("π")`, `InsertOperator("^")`, `ToggleSecondMode`), automatically inserting opening parentheses for unary functions and showing ghost closing parentheses in the readout.
- **History & Constants Sheet**: Tapping any historical calculation inserts either its expression or result into the current cursor position; tapping `Pin` preserves key calculations at the top of the tape.
