# 🗺️ Water Sort Puzzle — Implementation Plan

> Development plan divided into clear Phases and Milestones.

---

## Phase Overview

```
Phase 1:   Core Logic & Algorithm        ✅ Completed
Phase 2:   CLI Polish & Testing          🟡 Mostly Completed (Pending: timeout, 10+ puzzle integration tests)
Phase 2.5: Image Recognition Pipeline    ✅ Completed (Approach C — Hybrid)
Phase 3:   Desktop UI (JavaFX)           ✅ Completed
Phase 4:   Web UI (Optional)             ⬜ Not Started
```

> Legend: ✅ Completed · 🟡 Partially Completed · ⬜ Not Started
>
> **New Vision**: This project is not just a solver — it is a **comprehensive toolset**.
> Users simply capture a screenshot → the system automatically handles everything → and displays the solution step-by-step.

---

## Phase 1 — Core Logic & Algorithm 🔧

> **Goal**: A fully functional Solver with comprehensive Unit Test coverage.

### Milestone 1.1 — Data Model

| Task | File | Status |
|---|---|---|
| Create `Tube` class (push, pop, peek, isFull, isEmpty, isSorted, deepCopy) | `model/Tube.java` | ✅ |
| Create `Move` record (source, destination) | `model/Move.java` | ✅ |
| Create `BoardState` class (tubes array, isGoal, getValidMoves, applyMove) | `model/BoardState.java` | ✅ |
| Implement `equals()` + `hashCode()` for `BoardState` | `model/BoardState.java` | ✅ |
| Implement `toCanonicalString()` for state caching | `model/BoardState.java` | ✅ |
| Unit test: `TubeTest` — push/pop/isSorted/deepCopy | `test/.../TubeTest.java` | ✅ |
| Unit test: `BoardStateTest` — isGoal/getValidMoves/applyMove/equality | `test/.../BoardStateTest.java` | ✅ |

### Milestone 1.2 — BFS Solver

| Task | File | Status |
|---|---|---|
| Create `Solver` interface (solve method) | `solver/Solver.java` | ✅ |
| Create `SolveResult` class (moves, stats) | `solver/SolveResult.java` | ✅ |
| Implement `BFSSolver` — BFS according to spec | `solver/BFSSolver.java` | ✅ |
| Implement pruning rules (no reverse, skip completed, identical empties) | `solver/BFSSolver.java` | ✅ |
| Unit test: Easy puzzle (3 tubes, 1 color) → Must solve | `test/.../BFSSolverTest.java` | ✅ |
| Unit test: Medium puzzle (5 tubes) → Must find optimal path | `test/.../BFSSolverTest.java` | ✅ |
| Unit test: Unsolvable puzzle → `isSolved() == false` | `test/.../BFSSolverTest.java` | ✅ |

### Milestone 1.3 — A* Solver

| Task | File | Status |
|---|---|---|
| Implement heuristic function (color-change count) | `solver/AStarSolver.java` | ✅ |
| Implement `AStarSolver` — A* according to spec | `solver/AStarSolver.java` | ✅ |
| Benchmark BFS vs A* with real 14-tube puzzles (results in README) | benchmark | ✅ |

### Milestone 1.4 — CLI Entry Point

| Task | File | Status |
|---|---|---|
| Create `BoardParser` — parse input from stdin/file | `util/BoardParser.java` | ✅ |
| Create `Main.java` — CLI entry point | `Main.java` | ✅ |
| Support JSON input file | `util/BoardParser.java` | ✅ |
| Display step-by-step results | `Main.java` | ✅ |
| Display stats (states explored, time elapsed) | `Main.java` | ✅ |

---

## Phase 2 — CLI Polish & Testing 🧪

> **Goal**: A robust CLI application with edge case handling.

| Task | Status |
|---|---|
| Error handling for invalid inputs (invalid JSON, unreadable image, invalid UI board) | ✅ |
| Timeout mechanism (e.g., 30 seconds) | ⬜ — Currently capped by state limit (2,000,000) instead of time |
| Memory limit warning | ⬜ — Using 2M states limit + GUI app runs with `-Xmx2g` |
| Add integration tests with 10+ real puzzles | ⬜ |
| Performance profiling & optimization (A* parent-pointer, heuristic h2, cached key) | ✅ |
| Setup `build.gradle` or `pom.xml` (using `build.gradle.kts`) | ✅ |
| README: Add benchmark results | ✅ |

---

## Phase 2.5 — Image Recognition Pipeline 📸

> **Goal**: Users upload a game screenshot → the system auto-detects tubes, extracts colors, and constructs the BoardState.

### End-to-End Pipeline

```
┌──────────────┐    ┌──────────────────┐    ┌──────────────────┐    ┌─────────────┐
│  📸 Input    │    │  🔍 Detection    │    │  🎨 Extraction   │    │  🧩 Output  │
│  Screenshot  │ ──▶│  Tube Region     │ ──▶│  Color per Slot   │ ──▶│  BoardState │
│  (PNG/JPG)   │    │  Cropping        │    │  Identification   │    │  (ready!)   │
└──────────────┘    └──────────────────┘    └──────────────────┘    └─────────────┘
                           │                        │
                     Step 1: Detect             Step 2: Classify
                     & Crop Tubes               Colors → IDs
```

### Architecture — 3 Approaches (Select 1)

| Approach | Method | Pros | Cons |
|---|---|---|---|
| **A. Classical CV** | OpenCV: edge detection → contour → crop → dominant color per region | No model needed, fast, lightweight | Fragile against varying backgrounds/themes |
| **B. Pre-trained Vision Model** | Use Vision API (Google Cloud Vision, GPT-4V, Gemini) → returns JSON config | Highly accurate, supports multiple themes | Requires API key, incurs cost |
| **C. Hybrid** | Classical CV to detect/crop tubes + simple color matching (K-Means / HSV lookup) | Balanced accuracy and cost | Requires color tuning |

> **Recommendation**: Start with **Approach C (Hybrid)** for cost-efficiency and control.
> If accuracy is insufficient, add Approach B as a fallback.
>
> ✅ **Selected: Approach C** — Classical CV (OpenCV) detects tubes + color grouping via clustering in LAB space.
> (Sample image `test_image.png` is read with 100% accuracy; Vision API fallback is not implemented yet).

### Step-by-Step Pipeline Detail

#### Step 1 — Tube Detection & Cropping

```
Input: Game screenshot (e.g., 1024x1024 px)

1. Convert to grayscale
2. Apply Gaussian blur (reduce noise)
3. Edge detection (Canny)
4. Find contours → filter by:
   - Aspect ratio ≈ 1:3 to 1:4 (tall narrow tubes)
   - Area ≈ Expected size
   - Position clustering (tubes align in rows)
5. Sort contours left-to-right, top-to-bottom → determine tube order
6. Crop each tube into a sub-image

Output: List<Image> tubeImages (ordered)
```

#### Step 2 — Color Extraction & Identification (Actual Implementation)

```
Input: Tube sub-image (bounds from Step 1 include the top cap)

1. Crop the top cap (CAP_FRACTION = 13% of height) and the bottom curved edge (1.5%), then divide the rest into 4 equal slots.
2. For each slot:
   a. Sample only the center (excluding 40% margins vertically/horizontally) to avoid reflections.
   b. Calculate mean LAB, HSV, and RGB for that region.
3. Separate "empty slots" from "colored blocks": Empty = dark background without color (L ≤ 55 and chroma ≤ 15).
   — Dark but saturated blocks still count as colors.
4. Group colors across the image using constrained agglomerative clustering in LAB space.
   (average linkage, ≤ 4 blocks per group, group count = colored blocks ÷ 4)
5. Assign human-readable names to each group via ColorNamer (nearest reference, redmean distance)
   and pass actual RGB values to the UI for accurate rendering.

Output: int[] colors (size 0–4, bottom → top)
```

> **Deviation from original plan**: Instead of rigid HSV lookups + K-Means (k=1) per slot which required tuning per theme, we group colors based on the rule "every color must have 4 blocks" without prior palette knowledge. (`ColorPalette` remains as a reference lookup, but the main pipeline does not use it).

#### Step 3 — Validation & BoardState Construction

```
1. Verify every color has exactly 4 blocks (TUBE_CAPACITY).
2. Verify empty tubes count ≥ 2.
3. If verification fails → display preview for user confirmation/editing.
4. Construct BoardState from the extracted data.
```

### Data Structures (Additional)

```java
/**
 * Result from image recognition
 */
class RecognitionResult {
    BoardState boardState;           // Extracted state (null if unreadable)
    List<TubeRegion> tubeRegions;    // Tube bounding boxes in the image
    Map<Integer, String> colorMap;   // color ID → name (e.g., "Light Green")
    Map<Integer, int[]> colorRgb;    // color ID → actual {r,g,b} for UI rendering
    double confidence;               // 1.0 = passed validation, 0.5 = failed
    List<String> warnings;           // e.g., "Colored blocks count is not a multiple of 4."
    List<String> errors;             // e.g., "Color 'Red' has 3 blocks (expected 4)."
    boolean isValid;                 // Did it pass validation?
}

class TubeRegion {
    int tubeIndex;
    Rectangle bounds;      // Coordinates in the original image
    int[] extractedColors; // Extracted colors (bottom → top)
    double[] slotConfidences; // Currently fixed at 1.0
}
```

### Tasks

| Task | File | Status |
|---|---|---|
| Decide approach (A/B/C) → Selected C (Hybrid) | — | ✅ |
| Setup OpenCV dependency | `build.gradle.kts` | ✅ |
| Implement `TubeDetector` — detect & crop tubes | `vision/TubeDetector.java` | ✅ |
| Implement `ColorExtractor` — extract mean LAB/HSV/RGB per slot | `vision/ColorExtractor.java` | ✅ |
| Create `ColorPalette` — predefined HSV ranges (reference) | `vision/ColorPalette.java` | ✅ |
| Create `ColorNamer` — generate readable color names | `vision/ColorNamer.java` | ✅ |
| Implement `ImageRecognizer` — orchestrate pipeline | `vision/ImageRecognizer.java` | ✅ |
| Create `RecognitionResult` + `TubeRegion` data classes | `vision/RecognitionResult.java` | ✅ |
| Unit test: Sample image (14 tubes) → must extract correctly | `test/.../ImageRecognizerTest.java` | ✅ |
| Unit test: Test against varying themes/backgrounds | `test/.../ImageRecognizerTest.java` | ⬜ |
| Implement user confirmation flow (UI Layer) | `ui/MainView.java` | ✅ |
| (Optional) Add Vision API fallback (Approach B) | `vision/VisionApiFallback.java` | ⬜ |

---

## Phase 3 — Desktop GUI (JavaFX) + Solution Playback 🖥️

> **Goal**: Complete UI lifecycle — Upload Image / Edit Colors / Solve / Step-by-Step Animation.

### Architecture

```
┌───────────────────────────────────────────────────────────────┐
│                        JavaFX UI                              │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  📸 Upload Zone  (Drag & Drop / Browse)                 │  │
│  │  "Drop your game screenshot here"                       │  │
│  └─────────────────────────────────────────────────────────┘  │
│                          │                                    │
│                    [🔍 Recognize]                              │
│                          ▼                                    │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ... ┌─────────┐     │
│  │ Tube 1  │  │ Tube 2  │  │ Tube 3  │      │ Tube 14 │     │
│  │ ██ Red  │  │ ██ Blue │  │ ██ Grn  │      │ (empty) │     │
│  │ ██ Blue │  │ ██ Gray │  │ ██ Grn  │      │         │     │
│  │ ██ Blue │  │ ██ Brwn │  │ ██ Red  │      │         │     │
│  │ ██ Purp │  │ ██ Brwn │  │ ██ Pink │      │         │     │
│  └─────────┘  └─────────┘  └─────────┘      └─────────┘     │
│                                                               │
│  [🎨 Edit]  [✅ Confirm]  [▶️ Solve]  [⏮️ Prev] [⏭️ Next]    │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Step 3/12: Pour Tube 1 → Tube 14  (Red)               │  │
│  │  ████████████████████░░░░░░░░░░░░░░  Progress: 25%      │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
          │                           │
          ▼                           ▼
┌──────────────────────┐   ┌──────────────────────────────────┐
│  Vision Pipeline     │   │      Core Logic (Phase 1)        │
│  (Phase 2.5)         │   │  BoardState → Solver → Moves     │
│  Image → BoardState  │   │                                  │
└──────────────────────┘   └──────────────────────────────────┘
```

### Planned Features

| Feature | Description |
|---|---|
| **📸 Screenshot Upload** | Drag & Drop or Browse game screenshot → auto recognize |
| **🔍 Recognition Preview** | Display extracted colors for user confirmation (highlights tubes on original image) |
| **🎨 Board Editor** | Allows users to manually correct misrecognized colors |
| **Visual Tubes** | Renders tubes as vibrant graphical components |
| **▶️ Solve Button** | Triggers Solver in a background thread |
| **⏮️⏭️ Step Navigator** | Move forward/backward through solution steps |
| **🎬 Pour Animation** | Displays color flowing from one tube to another (TranslateTransition) |
| **📊 Progress Bar** | Shows Step X/Y + progress bar |
| **▶️ Auto-Play** | Automatically plays the animation sequence (adjustable speed) |
| **Preset Puzzles** | Quick access to sample puzzles |
| **🧭 Mode Select** | Home screen to choose between: (1) Image Recognizer (2) Play Game |
| **🎮 Play Game** | System generates puzzles via `PuzzleGenerator`. Players can manually pour. Includes Undo/Restart/Hint/Solve for me |

### User Flow (End-to-End)

```
0. Launch App → Select Mode
   ├── 🎮 Play Game: Choose Colors/Difficulty → Generate → Play (Undo/Hint/Solve for me)
   └── 📸 Image Recognizer: Proceed below
         │
1. (Image Recognizer Mode)
         │
2. Choose input method:
   ├── 📸 Upload Screenshot ───────────────────────┐
   │       │                                        │
   │   3a. System detects tubes + extracts colors    │
   │       │                                        │
   │   3b. Displays preview + highlight on image     │
   │       │                                        │
   │   3c. User verifies → Edits if needed → ✅ Confirm │
   │                                                │
   └── 🎨 Manual Entry (Board Editor) ──────────────┘
         │
4. Press ▶️ Solve
         │
5. System displays Solution:
   ├── Step-by-step navigation (⏮️ Prev / ⏭️ Next)
   ├── Pour animation
   └── Auto-play mode (▶️ ⏸️)
         │
6. 🎉 Done!
```

### Tasks

| Task | Status |
|---|---|
| Setup JavaFX project module | ✅ |
| Create `UploadPane` — Drag & Drop zone for image uploads | ✅ |
| Create `RecognitionPreviewPane` — Original image + tube highlights + extracted colors | ✅ |
| Create `TubeView` component — Renders tubes as Rectangles + Colors | ✅ |
| Create `BoardView` — Grid layout for all tubes | ✅ |
| Create `ColorPicker` panel for edit mode (correcting misrecognitions) | ✅ |
| Create control bar (Confirm / Solve / Next / Prev / Auto-Play / Reset) | ✅ |
| Integrate Vision Pipeline (Phase 2.5) with UI | ✅ |
| Integrate Solver with UI (run on background thread) | ✅ |
| Implement step-by-step playback + progress bar | ✅ |
| Implement pour animation (TranslateTransition) | ✅ |
| Implement auto-play mode (adjustable speed) | ✅ |
| Light/Dark theme support | ✅ |
| Mode Selection: (1) Image Recognizer / (2) Play Game | ✅ |
| Play Game mode: PuzzleGenerator integration, manual play, Undo/Restart/Hint/Solve | ✅ |
| Save/Load puzzle state | ⬜ |

---

## Phase 4 — Web UI (Optional) 🌐

> **Goal**: Deploy as a web application, making it universally accessible without installation.

### Options

| Option | Pros | Cons |
|---|---|---|
| **Port Solver to JavaScript** | Runs natively in browser | Requires a complete rewrite |
| **Java backend + Web frontend** | Reuses existing Java Solver | Requires server hosting |
| **GraalVM/TeaVM compile to WASM** | Reuses Java code natively in browser | High complexity |

### Planned Stack (if Option 2 is chosen)

```
Frontend: HTML/CSS/JS (Vanilla or React)
Backend:  Spring Boot REST API
API:      POST /api/solve  { tubes: [...] }  → { steps: [...] }
Deploy:   Docker + any cloud platform
```

### Tasks

| Task | Status |
|---|---|
| Decide approach (Option 1/2/3) | ⬜ |
| Create REST API for Solver | ⬜ |
| Create Web frontend — Board Editor | ⬜ |
| Create Web frontend — Solution Viewer | ⬜ |
| Browser Animation (Canvas or CSS) | ⬜ |
| Deploy | ⬜ |

---

## Dependencies & Tools

| Tool | Purpose | Phase |
|---|---|---|
| **Java 17+** | Main language | All |
| **JUnit 5** | Unit testing | 1–2 |
| **Gradle (Kotlin DSL)** | Build automation (`./gradlew build/test/run/runCli`) | 2+ |
| **JaCoCo** | Test coverage report | 2+ |
| **OpenCV (JavaCV)** | Image processing — tube detection, color extraction | 2.5 |
| **JavaFX 21** (org.openjfx plugin) | Desktop GUI + Solution Playback | 3 |
| **Spring Boot** | Web backend (if chosen) | 4 |
| *(Optional)* **Google Cloud Vision / Gemini API** | Vision API fallback for higher accuracy | 2.5 |

---

## Risk & Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| BFS memory explosion (14+ tubes) | High | Implement A* + pruning rules to drastically reduce state space |
| Inadmissible Heuristic | Medium | Benchmark against BFS to guarantee optimal path accuracy |
| **Image recognition failures** (similar colors, dark themes) | **High** | **Mandatory user preview + edit step before solving** |
| **Undetected tubes** (skewed image / obscured UI) | **Medium** | **Fallback: Manual editor / Utilize Vision API** |
| Large OpenCV dependency | Medium | Use JavaCV wrapper, bundle only required modules |
| JavaFX installation friction for users | Medium | Consider Web UI as a frictionless alternative |
| State string collision (hash collisions) | Low | Ensure canonical string is purely unique |
