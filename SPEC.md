# 📋 Water Sort Puzzle — Game Specification

> เอกสารนี้กำหนดกฎเกม ข้อจำกัด โครงสร้างข้อมูล และข้อกำหนดของ Solver อย่างละเอียด
> เพื่อใช้เป็นข้อมูลอ้างอิงในการพัฒนาโปรแกรม

---

## 1. Game Elements (องค์ประกอบของเกม)

### 1.1 Tubes (หลอดแก้ว)

| Property | Description |
|---|---|
| **จำนวน** | `N` หลอด (ค่าปกติ 12–14 หลอด ขึ้นกับด่าน) |
| **ความจุ** | แต่ละหลอดบรรจุได้สูงสุด **4 บล็อกสี** (TUBE_CAPACITY = 4) |
| **หลอดว่าง** | เริ่มต้นจะมีหลอดว่าง 2 หลอด เป็นพื้นที่พักสี |
| **โครงสร้าง** | LIFO (Last-In-First-Out) — เข้าออกจากด้านบนเท่านั้น |

### 1.2 Colors (สี)

| Property | Description |
|---|---|
| **จำนวนสี** | `N - 2` สี (= จำนวนหลอดที่ไม่ว่าง) |
| **จำนวนบล็อกต่อสี** | **4 บล็อก** ต่อ 1 สี (เท่ากับ TUBE_CAPACITY) |
| **การแทนค่า** | ใน solver ใช้ `int` เป็น color ID (เริ่มที่ 1) — ชื่อสี ("Red", "Blue", ...) ใช้เฉพาะตอนอ่าน input/แสดงผล |

### 1.3 Board (กระดาน)

- Board = ชุดของ Tubes ทั้งหมด ณ จังหวะเวลาใดเวลาหนึ่ง
- Board State = snapshot ที่ใช้เปรียบเทียบกับสถานะอื่นได้ (immutable, hashable)

---

## 2. Game Rules (กฎการเล่น)

### 2.1 Valid Move (การเทที่ถูกต้อง)

การเท (pour) จาก **หลอดต้นทาง (source)** ไป **หลอดปลายทาง (destination)** จะถูกต้อง
ก็ต่อเมื่อ **ผ่านทุกเงื่อนไข** ต่อไปนี้:

```
RULE 1: source ≠ destination
RULE 2: source.isEmpty() == false               // หลอดต้นทางต้องไม่ว่าง
RULE 3: destination.size() < TUBE_CAPACITY       // หลอดปลายทางต้องยังไม่เต็ม
RULE 4: destination.isEmpty()                    // หลอดปลายทางว่าง
         OR destination.topColor() == source.topColor()  // หรือสีบนสุดตรงกัน
```

### 2.2 Pour Behavior (พฤติกรรมการเท)

เมื่อ Move ถูกต้อง:

1. ดึงสีบนสุดจาก source (pop)
2. ใส่สีนั้นลง destination (push)
3. **Multi-pour**: ถ้า source ยังมีสีบนสุดเป็นสีเดิม และ destination ยังไม่เต็ม → เทต่อ
   (ตามพฤติกรรมจริงของเกม ที่เทหลายบล็อกพร้อมกันถ้าสีเดียวกัน)

> **Note**: implementation ปัจจุบันใช้ multi-pour เสมอ (`BoardState.applyMove`) — 1 move = เททุกบล็อกสีเดียวกันที่เทได้ในครั้งเดียว
> จำนวน step ที่รายงาน (และ "Best possible" ในโหมด Play Game) นับตามนิยามนี้

### 2.3 Pruning Rules (กฎตัดกิ่ง — ลดการค้นหาที่ไม่จำเป็น)

เพื่อลดจำนวน state ที่ต้องสำรวจ solver ตัดกิ่งเหล่านี้ (implement ใน `BoardState.getValidMoves()`):

| Rule | Description | Implement |
|---|---|---|
| **No reverse move** | ไม่เทวนกลับไปกลับมา | ✅ ผ่าน state caching (BFS: `visited`, A*: `gScore`) ไม่ได้ตัดใน `getValidMoves()` |
| **Skip completed tubes** | หลอดที่ sorted เต็มแล้ว (4 สีเดียวกัน) ไม่ต้องเทออก | ✅ `getValidMoves()` |
| **Skip single-color source to empty** | ถ้า source มีแต่สีเดียว ไม่จำเป็นต้องเทไปหลอดเปล่า (ไม่มีประโยชน์) | ✅ `getValidMoves()` |
| **Identical empty tubes** | หลอดเปล่าทุกหลอดเท่ากัน → เทไปหลอดเปล่าแรกก็พอ | ✅ `getValidMoves()` |

> Pruning ใช้กับ **solver เท่านั้น** — ในโหมด Play Game ผู้เล่นเทได้ทุก move ที่ถูกกติกาตาม 2.1
> (`GameSession.canPour`) รวมถึง move ที่ solver ตัดทิ้ง

---

## 3. Goal State (เงื่อนไขชนะ)

```
สำหรับทุก Tube ในกระดาน:
  tube.isEmpty()
  OR (tube.size() == TUBE_CAPACITY AND tube มีสีเดียวกันทั้งหมด)
```

**กล่าวคือ**: ทุกหลอดต้อง "ว่างเปล่า" หรือ "เต็ม 4 บล็อกด้วยสีเดียวกัน" ถึงจะถือว่าชนะ

---

## 4. Data Structures (โครงสร้างข้อมูล)

### 4.1 Tube

```java
/**
 * แทนหลอดแก้ว 1 หลอด
 * ใช้ Stack-like behavior (LIFO) ความจุสูงสุด 4
 */
class Tube {
    static final int CAPACITY = 4;

    int[] colors;   // array ขนาด 4 เก็บสี (index 0 = ก้นหลอด)
    int size;       // จำนวนบล็อกสีที่อยู่ในหลอด (0–4)

    // --- Operations ---
    int  topColor();           // ดูสีบนสุด (peek)
    int  pop();                // ดึงสีบนสุดออก
    void push(int color);      // ใส่สีลงบนสุด
    boolean isEmpty();         // size == 0
    boolean isFull();          // size == CAPACITY
    boolean isSorted();        // เต็ม + สีเดียวกันทั้ง 4
    int  topColorCount();      // จำนวนบล็อกสีเดียวกันที่ต่อเนื่องจากบนสุด

    Tube deepCopy();           // สำเนาเพื่อใช้ใน BFS (ไม่กระทบต้นฉบับ)
}
```

### 4.2 BoardState

```java
/**
 * สถานะกระดาน = ชุดของ Tubes ทั้งหมด ณ จังหวะหนึ่ง
 * ต้อง implement equals() + hashCode() เพื่อใช้ใน HashSet
 */
class BoardState {
    Tube[] tubes;

    // --- Core ---
    boolean isGoal();                     // ตรวจว่าชนะหรือยัง
    List<Move> getValidMoves();           // หาทุก move ที่เป็นไปได้
    BoardState applyMove(Move move);      // สร้าง state ใหม่หลังเทน้ำ

    // --- Hashing ---
    // string กระชับ: 1 char ต่อ 1 บล็อกสี + '/' คั่นหลอด (cache ไว้ในตัว state)
    // equals()/hashCode() อิงจาก string นี้ → ใช้เป็น key ของ HashSet/HashMap ได้ตรง ๆ
    String toCanonicalString();
    @Override int hashCode();
    @Override boolean equals(Object o);
}
```

### 4.3 Move

```java
/**
 * แทนการกระทำ 1 ครั้ง = เทจากหลอด source ไปหลอด destination
 */
record Move(int source, int destination) {
    @Override
    public String toString() {
        return "Pour Tube " + (source + 1) + " → Tube " + (destination + 1);
    }
}
```

### 4.4 SearchNode (สำหรับ BFS/A*)

```java
/**
 * Node ในกราฟการค้นหา
 * BFS  : เก็บ moveHistory ทั้งเส้นทางใน node
 * A*   : เก็บแค่ move ล่าสุด + ตัวชี้ parent แล้ว reconstruct ตอนเจอ goal (ประหยัดเวลา/หน่วยความจำ)
 */
class SearchNode {
    BoardState state;
    SearchNode parent;        // (A*) node ก่อนหน้า
    Move move;                // (A*) move ที่พามาถึง state นี้ (null = ราก)
    List<Move> moveHistory;   // (BFS) ลำดับ move ตั้งแต่เริ่มจนถึง state นี้
    int g;                    // จำนวน moves ที่ทำมาแล้ว
    int h;                    // heuristic — estimated remaining cost (A*)
    int f;                    // g + h (A*)
}
```

---

## 5. Algorithm Specification (ข้อกำหนดอัลกอริทึม)

### 5.1 BFS (Breadth-First Search) — เวอร์ชันพื้นฐาน

```
function solveBFS(initialState):
    queue ← empty Queue
    visited ← empty HashSet

    queue.enqueue(SearchNode(initialState, emptyMoveList))
    visited.add(initialState)

    while queue is not empty:
        node ← queue.dequeue()

        if node.state.isGoal():
            return node.moveHistory     // ✅ เจอคำตอบ!

        for each move in node.state.getValidMoves():
            newState ← node.state.applyMove(move)
            if newState not in visited:
                visited.add(newState)
                newMoveHistory ← node.moveHistory + [move]
                queue.enqueue(SearchNode(newState, newMoveHistory))

    return null  // ❌ ด่านนี้ไม่มีทางออก (implementation: SolveResult.failure → isSolved() == false)
```

**คุณสมบัติ**: BFS รับประกันว่าจะเจอคำตอบที่ **จำนวน move น้อยที่สุด**

> **เพดานการค้นหา**: ทั้ง BFS และ A* หยุดเมื่อสำรวจครบ `MAX_STATES = 2,000,000` แล้วรายงานว่าไม่พบคำตอบ

### 5.2 A* Search — เวอร์ชัน Optimized

```
function solveAStar(initialState):
    openSet ← PriorityQueue (เรียงตาม f = g + h; ถ้า f เท่ากัน เลือก h น้อยกว่าก่อน)
    gScore  ← Map<StateKey, int>     // ต้นทุนที่ดีที่สุดที่รู้จนถึงแต่ละ state

    openSet.add(Node(initialState, parent=null, move=null, g=0))
    gScore[initialState] ← 0

    while openSet is not empty:
        node ← openSet.poll()                    // f(n) ต่ำสุด
        if node.g > gScore[node.state]: continue // entry เก่า (เจอทางที่สั้นกว่าแล้ว) → ข้าม

        if node.state.isGoal():
            return reconstructPath(node)         // ไล่ parent กลับไปราก แล้วกลับลำดับ

        for each move in node.state.getValidMoves():
            newState ← node.state.applyMove(move)
            g ← node.g + 1
            if g < gScore.getOrDefault(newState, ∞):
                gScore[newState] ← g
                openSet.add(Node(newState, parent=node, move=move, g))

    return null
```

### 5.3 Heuristic Function (ฟังก์ชันประมาณค่า — สำหรับ A*)

```
function heuristic(state):
    // h1: จำนวน "รอยต่อที่สีเปลี่ยน" รวมทุกหลอด
    h1 ← 0
    for each tube in state.tubes:
        for i from 1 to tube.size - 1:
            if tube[i] ≠ tube[i-1]: h1 += 1

    // h2: สีที่กระจายอยู่ k หลอด ต้องรวมให้เหลือหลอดเดียว → อย่างน้อย k-1 การเท
    h2 ← Σ over colors c of (จำนวนหลอดที่มีสี c  −  1)

    return max(h1, h2)
```

> **ความ admissible**: การเท 1 ครั้งลด h1 ได้ไม่เกิน 1 (ย้ายกลุ่มสีเดียวกันออกได้ครั้งละ 1 รอยต่อ)
> และลด h2 ได้ไม่เกิน 1 (มีผลกับสีที่ถูกเทเพียงสีเดียว) ทั้งคู่เป็น 0 ที่สถานะชนะ →
> `max(h1, h2)` ไม่ overestimate จึงรับประกันว่า A* เจอ optimal solution (มี unit test เทียบ step count กับ BFS)

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
ผู้ใช้อัปโหลดภาพ PNG/JPG ของหน้าจอเกม → ระบบ auto-detect → BoardState
```

### 6.2 Output Format

ตัวอย่างเอาต์พุตจริงของ `--demo`:

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
| `./gradlew runCli` | Interactive mode (กรอกหลอดเอง) |
| `--demo` | แก้ด่านตัวอย่าง |
| `--file puzzle.json` | อ่านด่านจาก JSON แล้วแก้ |
| `--image screenshot.png` | อ่านภาพหน้าจอเกม (Image Recognition) แล้วแก้ |
| `--generate N [easy\|medium\|hard]` | สร้างด่าน N สี (verify ว่าแก้ได้) |
| `--batch N C` | สร้าง C ด่าน ด่านละ N สี |
| `--help` | แสดงวิธีใช้ |

ตัวอย่าง: `./gradlew runCli --args="--file puzzle.json" --console=plain`

---

## 7. Constraints & Assumptions

| Constraint | Value |
|---|---|
| Max tubes | solver ไม่จำกัดตายตัว (ด่านปกติ 12–14); GUI Board Editor ตั้งได้ 2–28 หลอด |
| Tube capacity | 4 (fixed) |
| Color ID | จำนวนเต็มบวก 1, 2, 3, … (GUI มีสี/ชื่อให้ 24 สี) |
| Max search states | 2,000,000 (`MAX_STATES`; เกินแล้วหยุดและรายงานว่าไม่พบคำตอบ) |
| Empty tubes at start | ≥ 2 (ไม่งั้นส่วนใหญ่แก้ไม่ได้ — เป็น warning ใน GUI) |
| Puzzle Generator | โหมด Play Game ให้เลือก 3–10 สี (ตรวจว่าแก้ได้ด้วย A*; 10 สี ≈ 1 วินาที) |

---

## 8. Error Handling

| Case | Behavior |
|---|---|
| Invalid input file (JSON ผิด format / ไม่มี `tubes`) | CLI แสดง `❌ Error parsing file: …` (มี unit test) |
| Invalid board ใน GUI (สีไม่ครบ 4 บล็อก) | แสดงรายการปัญหาที่แบนเนอร์, ปุ่ม Confirm ถูกปิดจนกว่าจะแก้ |
| Unsolvable puzzle / ชนเพดาน states | แสดง "No solution found" พร้อมจำนวน states ที่สำรวจ |
| อ่านภาพไม่ได้ / ไม่พบหลอด | `RecognitionResult` มี `errors` และ `boardState = null` → GUI แจ้งที่หน้า Upload |
| Timeout ตามเวลา | ⬜ ยังไม่ทำ — ใช้เพดานจำนวน state แทน; GUI ให้ปุ่ม Cancel ระหว่างค้นหา |
| Out of memory | แอป GUI รันด้วย `-Xmx2g`; ถ้า solver ล้ม (Task failed) จะแสดงข้อความ error แทนที่จะค้าง |

---

## 9. Image Recognition Pipeline (Phase 2.5)

> ข้อกำหนดสำหรับระบบ auto-detect หลอดและสีจากภาพหน้าจอเกม

### 9.1 Input Requirements

| Property | Requirement |
|---|---|
| **Format** | PNG, JPG, JPEG (ตัวเลือกไฟล์ใน GUI); CLI `--image` ส่งให้ OpenCV `imread` โดยตรง |
| **Resolution** | ≥ 500×500 px (recommended ≥ 720p) — ภาพตัวอย่าง `test_image.png` คือ 981×1024 |
| **Content** | หน้าจอเกม Water Sort Puzzle ที่เห็นหลอดทั้งหมดชัดเจน |
| **Orientation** | Portrait หรือ Landscape |
| **Restrictions** | ไม่บังหลอด, ไม่มี popup/overlay ทับ |

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

        if 2.0 ≤ aspectRatio ≤ 6.0                       // หลอดแคบยาว
           and rect.height > 10% ของความสูงภาพ and rect.width > 10:
            tubeRects.append(rect)

    // ตัดกรอบซ้อนกัน: overlap / พื้นที่ของกรอบที่เล็กกว่า > 0.3 → ถือว่าเป็นหลอดเดียวกัน
    tubeRects ← removeOverlapping(tubeRects, threshold=0.3)

    // จัดลำดับแบบ row-major: y ต่างกันไม่เกินครึ่งความสูงหลอด = แถวเดียวกัน แล้วเรียงซ้าย → ขวา
    return sortRowMajor(tubeRects)
```

> `bounds` ที่ได้**รวมฝาหลอดด้านบนมาด้วย** — ขั้นตอนถัดไปจึงต้องตัดส่วนฝาออกก่อนแบ่ง slot

### 9.3 Color Extraction Algorithm (`ColorExtractor` + `ImageRecognizer`)

```
function extractColors(tubeImage):                     // ต่อ 1 หลอด
    liquidTop    ← height * 0.13                       // ตัดฝาหลอด (CAP_FRACTION)
    liquidBottom ← height * (1 - 0.015)                // ตัดขอบโค้งล่าง
    slotHeight   ← (liquidBottom - liquidTop) / 4

    for slot from 0 to 3:                              // บน → ล่าง
        region ← กึ่งกลางของ slot (เว้นขอบ 40% แนวตั้งและแนวนอน)
        record mean(LAB), mean(HSV), mean(RGB) ของ region
    return records กลับลำดับเป็น ก้นหลอด → บนสุด

function recognize(image):
    slots ← extractColors(ทุกหลอด)
    colored ← slots ที่ isColored(slot)                 // L > 55 หรือ chroma > 15 (ไม่ใช่พื้นหลังมืดไร้สี)
    k ← |colored| / 4                                   // ทุกสีมี 4 บล็อก
    clusters ← constrainedAgglomerativeClustering(colored, k, maxSize=4, space=LAB, linkage=average)
    for each cluster:  name ← ColorNamer.nearest(mean RGB), rgb ← mean RGB
    return BoardState จาก cluster id ของแต่ละบล็อก (id เริ่มที่ 1)
```

> ถ้า `|colored|` หาร 4 ไม่ลงตัว จะ **ไม่เดาเติมบล็อก** — ใส่ warning แล้วให้ validation รายงานสีที่จำนวนไม่ครบ เพื่อให้ผู้ใช้แก้ใน UI

### 9.4 Color Palette & Naming

**ColorNamer** (ใช้จริงใน pipeline) — ตั้งชื่อ cluster จากสีอ้างอิงที่ใกล้ที่สุด (redmean distance) ชื่อที่ซ้ำเติมเลขท้าย (เช่น "Dark Green 2"):

Red · Orange · Yellow · Olive · Light Green · Dark Green · Light Blue · Blue · Purple · Pink · Brown · Gray · White

**ColorPalette** (HSV lookup; เก็บไว้เป็นตัวอ้างอิง/ทดสอบ — pipeline หลักไม่เรียกใช้):

| Color Name | Hue Range | Saturation | Value | Notes |
|---|---|---|---|---|
| Red | 0–10, 170–180 | > 100 | > 80 | Wraps around in HSV |
| Orange | 10–25 | > 100 | > 120 | สว่างพอ (ถ้ามืดกว่านี้ → Brown) |
| Yellow | 25–35 | > 100 | > 80 | |
| Olive/YellowGreen | 35–50 | > 60 | > 60 | |
| Green | 50–80 | > 80 | > 60 | |
| Cyan | 80–100 | > 80 | > 60 | |
| Blue | 100–130 | > 80 | > 60 | |
| Purple | 130–155 | > 60 | > 50 | |
| Pink | 155–170 | > 40 | > 80 | |
| Brown | 10–30 | > 60 | 40–120 | Value ต่ำแยกจาก Orange |
| Gray | any | < 40 | ≥ 80 | Low saturation |
| *(Background)* | any | < 50 | < 60 | Dark = empty slot |

### 9.5 Validation Rules

```
function validateRecognition(result):
    errors ← []
    for each (color, count) in countOccurrences(result.allColors):
        if count ≠ TUBE_CAPACITY:                 // ทุกสีต้องมี = 4
            errors.append("Color " + color + " has " + count + " blocks (expected 4).")

    if emptyTubeCount < 2:
        errors.append("Only " + emptyTubeCount + " empty tubes detected (expected >= 2).")

    return errors     // ว่าง → isValid = true, confidence = 1.0; ไม่ว่าง → confidence = 0.5
```

ใน GUI ผู้ใช้แก้บอร์ดได้แล้ว validation รันซ้ำแบบ live (`BoardEditor.validate()`): สีที่จำนวนไม่ครบ 4 เป็น **error** (บล็อกปุ่ม Confirm),
หลอดว่าง < 2 เป็นแค่ **warning**

### 9.6 Recognition Result Data Structures

```java
/**
 * ผลลัพธ์จากการ recognize ภาพหน้าจอเกม
 */
class RecognitionResult {
    BoardState boardState;             // สถานะที่ extract ได้ (null ถ้าอ่านไม่ได้เลย)
    List<TubeRegion> tubeRegions;      // ตำแหน่ง + สีของแต่ละหลอด
    Map<Integer, String> colorMap;     // color ID → ชื่อสี (เช่น 1 → "Yellow")
    Map<Integer, int[]> colorRgb;      // color ID → {r,g,b} จริงที่วัดได้ (UI ใช้วาดสีตรงกับเกม)
    double confidence;                 // 1.0 = ผ่าน validation, 0.5 = ไม่ผ่าน
    List<String> warnings;            // เตือน (เช่น จำนวนบล็อกสีหาร 4 ไม่ลงตัว)
    List<String> errors;              // ข้อผิดพลาดจาก validation
    boolean isValid;                   // validation ผ่านหรือไม่
}

class TubeRegion {
    int tubeIndex;                     // ลำดับหลอด (0-based)
    Rectangle bounds;                  // พิกัด (x, y, w, h) ในภาพต้นฉบับ
    int[] extractedColors;             // สีที่ extract ได้ (bottom → top)
    double[] slotConfidences;          // ปัจจุบันเป็นค่าคงที่ 1.0 (ยังไม่คำนวณจริงต่อ slot)
}
```

### 9.7 User Confirmation Flow (GUI)

```
1. แสดงภาพต้นฉบับ + กรอบ/หมายเลขรอบหลอดที่ตรวจพบ (Recognition Preview) พร้อม chip ความมั่นใจ
2. ด้านข้างแสดง BoardState ที่ extract ได้เป็นหลอดกราฟิก ด้วย "สีจริงที่วัดได้จากภาพ"
   — hover หลอด/กรอบเพื่อดูความสัมพันธ์สองฝั่ง
3. หลอดที่มีสีซึ่งจำนวนไม่ครบ 4 → กรอบสีส้ม (น่าจะอ่านผิด); ถ้าบอร์ดไม่ valid จะเข้า Edit mode อัตโนมัติ
4. ผู้ใช้สามารถ:
   a. ✅ Confirm — ยืนยันว่าถูกต้อง → ไปขั้น Solve (กดได้เมื่อ validation ผ่าน)
   b. ✎ Edit — คลิกช่องเพื่อลงสี / คลิกขวาเพื่อลบ / เพิ่ม-ลดหลอด → แก้แล้ว Confirm
   c. 🔄 อัปโหลดใหม่ — ปุ่ม Change Mode → Image Recognizer
```

---

## 10. Desktop GUI Modes

หน้าแรกของแอป (`./gradlew run`) ให้เลือกโหมด:

| โหมด | Flow |
|---|---|
| **1. Image Recognizer** | Upload → Recognize → Preview → (Edit) → Confirm → Solve → Playback (Prev/Next/Auto-Play) |
| **2. Play Game** | Setup (3–10 สี, Easy/Medium/Hard) → Generate → ผู้เล่นเท → (Undo / Restart / Hint / Solve for me) |

### 10.1 กติกาโหมด Play Game (`GameSession`)

- ด่านสร้างโดย `PuzzleGenerator` (สุ่มแล้ว verify ด้วย A* ว่าแก้ได้) — Easy มีหลอดว่าง 3 หลอด, Medium/Hard มี 2
- ผู้เล่นคลิกหลอดต้นทาง (ยกขึ้น) แล้วคลิกหลอดปลายทาง — เทได้ตามกติกา 2.1 และเทแบบ multi-pour ตาม 2.2
  (ไม่ใช้ pruning ของ solver)
- **Moves** = จำนวนการเทที่ทำ; **Best possible** = จำนวน step ที่น้อยที่สุดจาก A* ตอนสร้างด่าน (ถ้า Moves ≤ Best possible = Perfect)
- **Undo** ย้อนทีละครั้ง, **Restart** กลับสถานะเริ่มต้น (Best possible เท่าเดิม)
- **Hint** — A* คำนวณจากตำแหน่งปัจจุบัน แล้วไฮไลท์คู่หลอดของ move แรก; ถ้าไม่มีทางแก้จากตำแหน่งนั้น จะแจ้งให้ Undo
- **Solve for me** — A* แก้จากตำแหน่งปัจจุบัน แล้วเล่นเป็น playback; **Back to Game** กลับไปเล่นต่อจากตำแหน่งเดิม
- ชนะเมื่อทุกหลอดว่างหรือเต็มด้วยสีเดียว (`BoardState.isGoal()`)
