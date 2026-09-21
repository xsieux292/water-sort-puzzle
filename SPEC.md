# 📋 Water Sort Puzzle — Game Specification

> This document defines the game rules, constraints, data structures, and Solver specifications in detail, serving as a comprehensive reference for program development.

---

## 1. Game Elements

### 1.1 Tubes

| Property | Description |
|---|---|
| **Count** | `N` tubes (typically 12–14 depending on the puzzle) |
| **Capacity** | Each tube can hold up to **4 color blocks** (TUBE_CAPACITY = 4) |
| **Empty Tubes** | Each puzzle starts with 2 empty tubes to serve as working space |
| **Structure** | LIFO (Last-In-First-Out) — blocks can only enter or exit from the top |

### 1.2 Colors

| Property | Description |
|---|---|
| **Color Count** | `N - 2` colors (= number of initially non-empty tubes) |
| **Blocks per Color** | Exactly **4 blocks** per color (matching TUBE_CAPACITY) |
| **Representation** | The solver uses `int` for color IDs (starting at 1). Color names ("Red", "Blue") are only used for UI/Output. |

### 1.3 Board

- Board = The complete set of Tubes at any given moment.
- Board State = A snapshot that can be compared against other states (immutable, hashable).

---

## 2. Game Rules

### 2.1 Valid Move

A pour from a **source tube** to a **destination tube** is valid if and only if it **passes all of the following conditions**:

```
RULE 1: source ≠ destination
RULE 2: source.isEmpty() == false               // Source must not be empty
RULE 3: destination.size() < TUBE_CAPACITY      // Destination must not be full
RULE 4: destination.isEmpty()                   // Destination is empty
         OR destination.topColor() == source.topColor() // Or the top colors match
```

### 2.2 Pour Behavior

When a Move is valid:

1. Extract the top color from the source (pop).
2. Insert that color into the destination (push).
3. **Multi-pour**: If the source still has the same top color and the destination is not yet full → pour again.
   (This mimics the actual game behavior where all matching contiguous blocks are poured at once).

> **Note**: The current implementation always utilizes multi-pour (`BoardState.applyMove`) — 1 move = pouring all possible blocks of the same color in a single action. The step count reported (and the "Best possible" score in Play Game mode) is based on this definition.

### 2.3 Pruning Rules (Search Space Reduction)

To minimize the states the solver must explore, these pruning rules are implemented in `BoardState.getValidMoves()`:

| Rule | Description | Implementation |
|---|---|---|
| **No reverse move** | Prevents pouring back and forth | ✅ Handled via state caching (BFS: `visited`, A*: `gScore`) |
| **Skip completed tubes** | Fully sorted tubes (4 identical colors) should never be poured out | ✅ `getValidMoves()` |
| **Skip single-color source to empty** | Pouring a uniform source into an empty tube achieves nothing | ✅ `getValidMoves()` |
| **Identical empty tubes** | If all empty tubes are identical, pouring into the first one suffices | ✅ `getValidMoves()` |

> Pruning only applies to the **solver**. In Play Game mode, players can perform any valid move (as defined in 2.1) via `GameSession.canPour`, even moves the solver would prune.

---

## 3. Goal State

```
For every Tube on the board:
  tube.isEmpty()
  OR (tube.size() == TUBE_CAPACITY AND tube contains only one color)
```

**Simply put**: Every tube must either be completely empty or completely full with a single uniform color.

---

## 4. Data Structures

### 4.1 Tube

```java
/**
 * Represents a single test tube.
 * LIFO Stack-like behavior with a maximum capacity of 4.
 */
class Tube {
    static final int CAPACITY = 4;

    int[] colors;   // Array of size 4 storing colors (index 0 = bottom)
    int size;       // Number of color blocks currently inside (0–4)

    // --- Operations ---
    int  topColor();           // Inspect top color (peek)
    int  pop();                // Remove top color
    void push(int color);      // Insert color at top
    boolean isEmpty();         // size == 0
    boolean isFull();          // size == CAPACITY
    boolean isSorted();        // Full + 4 identical colors
    int  topColorCount();      // Count of identical contiguous colors from the top

    Tube deepCopy();           // Copy for BFS (leaves original unaffected)
}
```

### 4.2 BoardState

```java
/**
 * Board state = Set of all Tubes at a given moment.
 * Must implement equals() + hashCode() for use in HashSets.
 */
class BoardState {
    Tube[] tubes;

    // --- Core ---
    boolean isGoal();                     // Check for victory
    List<Move> getValidMoves();           // Find all possible moves
    BoardState applyMove(Move move);      // Generate new state after pouring

    // --- Hashing ---
    // Compact string: 1 char per block + '/' tube separator (cached internally)
    // equals()/hashCode() relies on this string → used as HashSet/HashMap keys
    String toCanonicalString();
    @Override int hashCode();
    @Override boolean equals(Object o);
}
```

### 4.3 Move

```java
/**
 * Represents a single action = pouring from a source tube to a destination tube.
 */
record Move(int source, int destination) {
    @Override
    public String toString() {
        return "Pour Tube " + (source + 1) + " → Tube " + (destination + 1);
    }
}
```

### 4.4 SearchNode (For BFS/A*)

```java
/**
 * Node within the search graph.
 * BFS : Stores the entire moveHistory sequence in the node.
 * A*  : Only stores the latest move + parent pointer, reconstructing at the goal (Saves time/memory).
 */
class SearchNode {
    BoardState state;
    SearchNode parent;        // (A*) Previous node
    Move move;                // (A*) The move that resulted in this state (null = root)
    List<Move> moveHistory;   // (BFS) Full sequence of moves from start to this state
    int g;                    // Number of moves taken so far
    int h;                    // Heuristic — estimated remaining cost (A*)
    int f;                    // g + h (A*)
}
```

---

## 5. Algorithm Specification

### 5.1 BFS (Breadth-First Search) — Basic Version

```
function solveBFS(initialState):
    queue ← empty Queue
    visited ← empty HashSet

    queue.enqueue(SearchNode(initialState, emptyMoveList))
    visited.add(initialState)

    while queue is not empty:
        node ← queue.dequeue()

        if node.state.isGoal():
            return node.moveHistory     // ✅ Solution found!

        for each move in node.state.getValidMoves():
            newState ← node.state.applyMove(move)
            if newState not in visited:
                visited.add(newState)
                newMoveHistory ← node.moveHistory + [move]
                queue.enqueue(SearchNode(newState, newMoveHistory))

    return null  // ❌ Unsolvable puzzle (implementation: SolveResult.failure → isSolved() == false)
```

**Property**: BFS guarantees finding the solution with the **absolute minimum number of moves**.

> **Search Limit**: Both BFS and A* halt when exploring `MAX_STATES = 2,000,000` and report no solution found.

### 5.2 A* Search — Optimized Version

```
function solveAStar(initialState):
    openSet ← PriorityQueue (Sorted by f = g + h; if f is equal, lower h prioritized)
    gScore  ← Map<StateKey, int>     // Best known cost to each state

    openSet.add(Node(initialState, parent=null, move=null, g=0))
    gScore[initialState] ← 0

    while openSet is not empty:
        node ← openSet.poll()                    // Lowest f(n)
        if node.g > gScore[node.state]: continue // Stale entry (shorter path already found) → skip

        if node.state.isGoal():
            return reconstructPath(node)         // Trace parents back to root, then reverse

        for each move in node.state.getValidMoves():
            newState ← node.state.applyMove(move)
            g ← node.g + 1
            if g < gScore.getOrDefault(newState, ∞):
                gScore[newState] ← g
                openSet.add(Node(newState, parent=node, move=move, g))

    return null
```

### 5.3 Heuristic Function (A*)

```
function heuristic(state):
    // h1: Total number of "color boundaries" across all tubes
    h1 ← 0
    for each tube in state.tubes:
        for i from 1 to tube.size - 1:
            if tube[i] ≠ tube[i-1]: h1 += 1

    // h2: Colors scattered across k tubes require at least k-1 pours to merge
    h2 ← Σ over colors c of (Number of tubes containing color c − 1)

    return max(h1, h2)
```

> **Admissibility**: A single pour can reduce h1 by at most 1 (removing one boundary) and reduce h2 by at most 1 (affecting only the poured color). Both resolve to 0 at the goal state → `max(h1, h2)` never overestimates, guaranteeing A* finds the optimal solution (verified by unit tests comparing step counts against BFS).

---

## 6. Input/Output Specification

### 6.1 Input Format (CLI)

**Option A — Interactive prompt:**
```
Enter number of tubes: 5
Tube 1 (bottom to top, comma-separated): R,B,R,G
Tube 2 (bottom to top, comma-separated): B,G,R,B
Tube 3 (bottom to top, comma-separated): G,R,G,B
Tube 4 (bottom to top, comma-separated):
Tube 5 (bottom to top, comma-separated):
```

**Option B — JSON file:** (`--file puzzle.json`)
```json
{
  "tubes": [
    ["R", "B", "R", "G"],
    ["B", "G", "R", "B"],
    ["G", "R", "G", "B"],
    [],
    []
  ]
}
```

**Option C — Screenshot upload (Phase 2.5):**
```
User uploads PNG/JPG of game screenshot → System auto-detects → BoardState
```

### 6.2 Output Format

Example output from `--demo`:

```
📋 Demo puzzle loaded:

Board:
  Tube  1: [1, 2, 1, 3]
  ...

🔍 Solving...

✅ Solved in 10 steps | States: 55 | Time: 5ms

  Step  1: Pour Tube 2 → Tube 4
  Step  2: Pour Tube 3 → Tube 4
  ...
  Step 10: Pour Tube 2 → Tube 4

🎉 All tubes sorted!

📊 Stats: 55 states explored in 5ms
```

### 6.3 CLI Options

| Command | Description |
|---|---|
| `./gradlew runCli` | Interactive mode (Manual tube entry) |
| `--demo` | Solve a pre-configured demo puzzle |
| `--file puzzle.json` | Read and solve a puzzle from JSON |
| `--image screenshot.png` | Read and solve via Image Recognition |
| `--generate N [easy\|medium\|hard]` | Generate an N-color puzzle (verified solvable) |
| `--batch N C` | Generate C puzzles, each with N colors |
| `--help` | Display usage instructions |

Example: `./gradlew runCli --args="--file puzzle.json" --console=plain`

---

## 7. Constraints & Assumptions

| Constraint | Value |
|---|---|
| Max tubes | Solver has no hard limit (typically 12–14); GUI Board Editor supports 2–28 tubes |
| Tube capacity | 4 (fixed) |
| Color ID | Positive integers 1, 2, 3, … (GUI provides palettes/names for 24 colors) |
| Max search states | 2,000,000 (`MAX_STATES`; halts and reports no solution if exceeded) |
| Empty tubes at start | ≥ 2 (Otherwise mostly unsolvable — throws warning in GUI) |
| Puzzle Generator | Play Game mode offers 3–10 colors (solvability verified via A*; 10 colors takes ≈ 1 second) |

---

## 8. Error Handling

| Case | Behavior |
|---|---|
| Invalid input file (malformed JSON / missing `tubes`) | CLI displays `❌ Error parsing file: …` (unit tested) |
| Invalid board in GUI (colors do not have exactly 4 blocks) | Displays error in banner; Confirm button is disabled until resolved |
| Unsolvable puzzle / Hits state limit | Displays "No solution found" along with explored state count |
| Image unreadable / No tubes detected | `RecognitionResult` yields `errors` and `boardState = null` → GUI alerts user in Upload screen |
| Time-based timeout | ⬜ Not implemented — relies on state limit instead; GUI provides Cancel button during search |
| Out of memory | GUI app runs with `-Xmx2g`; If solver fails (Task failed), it will display an error message rather than freezing |

---

## 9. Image Recognition Pipeline (Phase 2.5)

> Specifications for auto-detecting tubes and colors from game screenshots.

### 9.1 Input Requirements

| Property | Requirement |
|---|---|
| **Format** | PNG, JPG, JPEG (GUI file picker); CLI `--image` feeds directly to OpenCV `imread` |
| **Resolution** | ≥ 500×500 px (recommended ≥ 720p) — Demo image `test_image.png` is 981×1024 |
| **Content** | A clear Water Sort Puzzle game screen displaying all tubes |
| **Orientation** | Portrait or Landscape |
| **Restrictions** | Tubes must not be obscured by popups/overlays |

### 9.2 Tube Detection Algorithm (`TubeDetector`)

```
function detectTubes(image):
    gray ← convertToGrayscale(image)
    blurred ← gaussianBlur(gray, kernelSize=5)
    edges ← cannyEdgeDetect(blurred, threshold1=50, threshold2=150)
    contours ← findContours(edges, RETR_EXTERNAL)

    tubeRects ← []
    for each contour in contours:
        rect ← boundingRect(contour)
        aspectRatio ← rect.height / rect.width

        if 2.0 ≤ aspectRatio ≤ 6.0                       // Tall narrow tubes
           and rect.height > 10% of image height and rect.width > 10:
            tubeRects.append(rect)

    // Merge overlapping bounding boxes: overlap / area of smaller rect > 0.3 → treated as same tube
    tubeRects ← removeOverlapping(tubeRects, threshold=0.3)

    // Row-major sorting: y difference < half tube height = same row, then sort left → right
    return sortRowMajor(tubeRects)
```

> The resulting `bounds` **include the top cap of the tube** — the cap must be cropped in the next step before dividing into slots.

### 9.3 Color Extraction Algorithm (`ColorExtractor` + `ImageRecognizer`)

```
function extractColors(tubeImage):                     // Per tube
    liquidTop    ← height * 0.13                       // Crop top cap (CAP_FRACTION)
    liquidBottom ← height * (1 - 0.015)                // Crop bottom curve
    slotHeight   ← (liquidBottom - liquidTop) / 4

    for slot from 0 to 3:                              // Top → Bottom
        region ← Center of slot (excluding 40% vertical and horizontal margins)
        record mean(LAB), mean(HSV), mean(RGB) of region
    return records reversed as Bottom → Top

function recognize(image):
    slots ← extractColors(all tubes)
    colored ← slots where isColored(slot)               // L > 55 OR chroma > 15 (not dark background)
    k ← |colored| / 4                                   // Every color has 4 blocks
    clusters ← constrainedAgglomerativeClustering(colored, k, maxSize=4, space=LAB, linkage=average)
    for each cluster:  name ← ColorNamer.nearest(mean RGB), rgb ← mean RGB
    return BoardState mapped from cluster IDs (IDs start at 1)
```

> If `|colored|` is not perfectly divisible by 4, it **does not guess or artificially add blocks** — it issues a warning and relies on validation to report missing colors so the user can fix it in the UI.

### 9.4 Color Palette & Naming

**ColorNamer** (Actively used in pipeline) — Assigns cluster names based on the closest reference color (redmean distance). Duplicate names are suffixed with a number (e.g., "Dark Green 2"):

Red · Orange · Yellow · Olive · Light Green · Dark Green · Light Blue · Blue · Purple · Pink · Brown · Gray · White

**ColorPalette** (HSV lookup; kept for reference/testing — not used by main pipeline):

| Color Name | Hue Range | Saturation | Value | Notes |
|---|---|---|---|---|
| Red | 0–10, 170–180 | > 100 | > 80 | Wraps around in HSV |
| Orange | 10–25 | > 100 | > 120 | Bright enough (otherwise → Brown) |
| Yellow | 25–35 | > 100 | > 80 | |
| Olive/YellowGreen | 35–50 | > 60 | > 60 | |
| Green | 50–80 | > 80 | > 60 | |
| Cyan | 80–100 | > 80 | > 60 | |
| Blue | 100–130 | > 80 | > 60 | |
| Purple | 130–155 | > 60 | > 50 | |
| Pink | 155–170 | > 40 | > 80 | |
| Brown | 10–30 | > 60 | 40–120 | Low Value separates it from Orange |
| Gray | any | < 40 | ≥ 80 | Low saturation |
| *(Background)* | any | < 50 | < 60 | Dark = empty slot |

### 9.5 Validation Rules

```
function validateRecognition(result):
    errors ← []
    for each (color, count) in countOccurrences(result.allColors):
        if count ≠ TUBE_CAPACITY:                 // Every color must have exactly 4 blocks
            errors.append("Color " + color + " has " + count + " blocks (expected 4).")

    if emptyTubeCount < 2:
        errors.append("Only " + emptyTubeCount + " empty tubes detected (expected >= 2).")

    return errors     // Empty array → isValid = true, confidence = 1.0; Else → confidence = 0.5
```

In the GUI, as the user edits the board, validation runs continuously (`BoardEditor.validate()`): Any color lacking exactly 4 blocks triggers an **error** (disabling the Confirm button), while having < 2 empty tubes only triggers a **warning**.

### 9.6 Recognition Result Data Structures

```java
/**
 * Result from recognizing a game screenshot
 */
class RecognitionResult {
    BoardState boardState;             // Extracted state (null if unreadable)
    List<TubeRegion> tubeRegions;      // Locations + colors of each tube
    Map<Integer, String> colorMap;     // color ID → name (e.g. 1 → "Yellow")
    Map<Integer, int[]> colorRgb;      // color ID → actual {r,g,b} (UI uses this to match in-game colors)
    double confidence;                 // 1.0 = passed validation, 0.5 = failed
    List<String> warnings;            // Warnings (e.g. Colored blocks count is not a multiple of 4)
    List<String> errors;              // Errors from validation
    boolean isValid;                   // Validation status
}

class TubeRegion {
    int tubeIndex;                     // Tube index (0-based)
    Rectangle bounds;                  // Coordinates (x, y, w, h) in original image
    int[] extractedColors;             // Extracted colors (bottom → top)
    double[] slotConfidences;          // Currently fixed at 1.0
}
```

### 9.7 User Confirmation Flow (GUI)

```
1. Displays the original image + bounding boxes/numbers around detected tubes (Recognition Preview) with a confidence chip.
2. The sidebar displays the extracted BoardState as graphical tubes, rendered with the "actual colors measured from the image."
   — Hover over tubes/boxes to see the correlation between both sides.
3. Tubes containing colors with incomplete counts (≠ 4) → receive an orange border (likely misread); if the board is invalid, it automatically enters Edit mode.
4. The user can:
   a. ✅ Confirm — Verify accuracy → Proceed to Solve (only available if validation passes).
   b. ✎ Edit — Click slots to paint / Right-click to clear / Add-Remove tubes → Confirm when fixed.
   c. 🔄 Upload again — Change Mode button → Image Recognizer.
```

---

## 10. Desktop GUI Modes

The home screen of the app (`./gradlew run`) offers these modes:

| Mode | Flow |
|---|---|
| **1. Image Recognizer** | Upload → Recognize → Preview → (Edit) → Confirm → Solve → Playback (Prev/Next/Auto-Play) |
| **2. Play Game** | Setup (3–10 colors, Easy/Medium/Hard) → Generate → Player pours → (Undo / Restart / Hint / Solve for me) |

### 10.1 Play Game Mode Rules (`GameSession`)

- Puzzles are generated by `PuzzleGenerator` (randomized, then verified solvable by A*) — Easy provides 3 empty tubes, Medium/Hard provides 2.
- Players click a source tube (lifting it) then click a destination tube — Pouring strictly follows Rule 2.1 and utilizes multi-pour per Rule 2.2 (ignoring solver pruning).
- **Moves** = Total pours made; **Best possible** = Minimum steps calculated by A* during generation (If Moves ≤ Best possible = Perfect).
- **Undo** rewinds a single step; **Restart** resets to the beginning (Best possible remains identical).
- **Hint** — A* calculates from the current position and highlights the recommended first move; if unsolvable from that point, it prompts the player to Undo.
- **Solve for me** — A* solves from the current position and initiates playback; **Back to Game** returns the player to their previous state.
- Victory occurs when every tube is either empty or full with a single color (`BoardState.isGoal()`).
