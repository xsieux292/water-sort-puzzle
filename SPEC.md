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
| **การแทนค่า** | ใช้ `enum` หรือ `int` (0, 1, 2, ...) หรือ `String` ("Red", "Blue", ...) |

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

> **Note**: ในเวอร์ชัน BFS พื้นฐาน อาจเททีละ 1 บล็อกก่อน แล้วค่อย optimize เป็น multi-pour

### 2.3 Pruning Rules (กฎตัดกิ่ง — ลดการค้นหาที่ไม่จำเป็น)

เพื่อลดจำนวน state ที่ต้องสำรวจ ควรตัดกิ่งเหล่านี้:

| Rule | Description |
|---|---|
| **No reverse move** | ห้ามเทกลับทันที (A→B แล้ว B→A ในก้าวถัดไป) |
| **Skip completed tubes** | หลอดที่ sorted เต็มแล้ว (4 สีเดียวกัน) ไม่ต้องเทออก |
| **Skip single-color source to empty** | ถ้า source มีแต่สีเดียว ไม่จำเป็นต้องเทไปหลอดเปล่า (ไม่มีประโยชน์) |
| **Identical empty tubes** | หลอดเปล่าทุกหลอดเท่ากัน → เทไปหลอดเปล่าแรกก็พอ |

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
    String toCanonicalString();           // แปลงเป็น string เพื่อเทียบ/hash
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
 * Node ในกราฟการค้นหา เก็บสถานะปัจจุบัน + ประวัติการเท
 */
class SearchNode {
    BoardState state;
    List<Move> moveHistory;   // ลำดับ move ตั้งแต่เริ่มจนถึง state นี้
    int cost;                 // g(n) = จำนวน moves (สำหรับ A*)
    int heuristic;            // h(n) = estimated remaining cost (สำหรับ A*)
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

    return null  // ❌ ด่านนี้ไม่มีทางออก
```

**คุณสมบัติ**: BFS รับประกันว่าจะเจอคำตอบที่ **จำนวน move น้อยที่สุด**

### 5.2 A* Search — เวอร์ชัน Optimized

```
function solveAStar(initialState):
    openSet ← PriorityQueue (sorted by f = g + h)
    visited ← empty HashSet

    startNode ← SearchNode(initialState, emptyMoveList, g=0, h=heuristic(initialState))
    openSet.add(startNode)

    while openSet is not empty:
        node ← openSet.poll()     // ดึง node ที่ f(n) ต่ำสุด

        if node.state.isGoal():
            return node.moveHistory

        visited.add(node.state)

        for each move in node.state.getValidMoves():
            newState ← node.state.applyMove(move)
            if newState not in visited:
                g ← node.cost + 1
                h ← heuristic(newState)
                openSet.add(SearchNode(newState, node.moveHistory + [move], g, h))

    return null
```

### 5.3 Heuristic Function (ฟังก์ชันประมาณค่า — สำหรับ A*)

```
function heuristic(state):
    score ← 0
    for each tube in state.tubes:
        if tube is not empty and not sorted:
            // นับจำนวน "การเปลี่ยนสี" ในหลอด
            // ยิ่งเปลี่ยนสีบ่อย → ยิ่งต้องเทมาก
            for i from 1 to tube.size - 1:
                if tube[i] ≠ tube[i-1]:
                    score += 1
    return score
```

> **หมายเหตุ**: heuristic นี้เป็น admissible (ไม่ overestimate) จึงรับประกันว่า A* จะเจอ optimal solution

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

**Option B — JSON file:**
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

```
🔍 Solving Water Sort Puzzle...
   Tubes: 5 | Colors: 3 | Capacity: 4

✅ Solution found in 12 steps!

Step  1: Pour Tube 1 → Tube 4    (Red)
Step  2: Pour Tube 2 → Tube 5    (Blue)
Step  3: Pour Tube 1 → Tube 2    (Red)
...
Step 12: Pour Tube 3 → Tube 5    (Green)

🎉 All tubes sorted!

📊 Stats:
   States explored: 1,247
   Time elapsed: 0.03s
```

---

## 7. Constraints & Assumptions

| Constraint | Value |
|---|---|
| Max tubes | 20 (ด่านปกติ 12–14) |
| Tube capacity | 4 (fixed) |
| Colors per block | integer 0 to N-3 |
| Max search states | ~10^6 (ถ้าเกินจะ timeout/out-of-memory) |
| Empty tubes at start | ≥ 2 (ไม่งั้นส่วนใหญ่แก้ไม่ได้) |

---

## 8. Error Handling

| Case | Behavior |
|---|---|
| Invalid input (สีไม่ครบ, หลอดเกิน 4) | แสดง error message ชัดเจน |
| Unsolvable puzzle | แสดง "No solution found" พร้อมจำนวน states ที่สำรวจ |
| Timeout (เกิน N วินาที) | หยุดค้นหา แสดง partial result |
| Out of memory | Catch `OutOfMemoryError`, แนะนำลดขนาดปัญหา |

---

## 9. Image Recognition Pipeline (Phase 2.5)

> ข้อกำหนดสำหรับระบบ auto-detect หลอดและสีจากภาพหน้าจอเกม

### 9.1 Input Requirements

| Property | Requirement |
|---|---|
| **Format** | PNG, JPG, JPEG, BMP |
| **Resolution** | ≥ 500×500 px (recommended ≥ 720p) |
| **Content** | หน้าจอเกม Water Sort Puzzle ที่เห็นหลอดทั้งหมดชัดเจน |
| **Orientation** | Portrait หรือ Landscape |
| **Restrictions** | ไม่บังหลอด, ไม่มี popup/overlay ทับ |

### 9.2 Tube Detection Algorithm

```
function detectTubes(image):
    gray ← convertToGrayscale(image)
    blurred ← gaussianBlur(gray, kernelSize=5)
    edges ← cannyEdgeDetect(blurred, threshold1=50, threshold2=150)
    contours ← findContours(edges)

    tubeContours ← []
    for each contour in contours:
        rect ← boundingRect(contour)
        aspectRatio ← rect.height / rect.width

        if 2.0 ≤ aspectRatio ≤ 5.0:           // หลอดแคบยาว
            if minArea ≤ rect.area ≤ maxArea:   // ขนาดอยู่ในช่วงที่คาดหวัง
                tubeContours.append(rect)

    // Remove duplicates (overlapping contours)
    tubeContours ← nonMaxSuppression(tubeContours, overlapThreshold=0.5)

    // Sort: left-to-right, then top-to-bottom (row-major order)
    tubeContours ← sortByPosition(tubeContours)

    return tubeContours
```

### 9.3 Color Extraction Algorithm

```
function extractColors(tubeImage):
    height ← tubeImage.height
    slotHeight ← height / 4    // 4 slots per tube
    margin ← 0.2               // 20% margin to avoid tube edges

    colors ← []
    for slot from 0 to 3:      // top to bottom
        y1 ← slot * slotHeight + (slotHeight * margin)
        y2 ← (slot + 1) * slotHeight - (slotHeight * margin)
        x1 ← tubeImage.width * margin
        x2 ← tubeImage.width * (1 - margin)

        region ← crop(tubeImage, x1, y1, x2, y2)
        hsvRegion ← convertToHSV(region)
        dominantHSV ← kMeansCluster(hsvRegion, k=1)

        if isBgColor(dominantHSV):   // dark / transparent
            continue                  // slot is empty
        else:
            colorId ← mapToNearestColor(dominantHSV, palette)
            colors.append(colorId)

    return colors    // bottom-to-top order (reversed for stack)
```

### 9.4 Color Palette (Predefined HSV Ranges)

| Color Name | Hue Range | Saturation | Value | Notes |
|---|---|---|---|---|
| Red | 0–10, 170–180 | > 100 | > 80 | Wraps around in HSV |
| Orange | 10–25 | > 100 | > 80 | |
| Yellow | 25–35 | > 100 | > 80 | |
| Olive/YellowGreen | 35–50 | > 60 | > 60 | |
| Green | 50–80 | > 80 | > 60 | |
| Cyan | 80–100 | > 80 | > 60 | |
| Blue | 100–130 | > 80 | > 60 | |
| Purple | 130–155 | > 60 | > 50 | |
| Pink | 155–170 | > 40 | > 80 | |
| Brown | 10–30 | > 60 | 40–120 | Low value distinguishes from orange |
| Gray | any | < 40 | 80–180 | Low saturation |
| *(Background)* | any | < 30 | < 60 | Dark = empty slot |

> **Note**: ค่าเหล่านี้เป็นค่าเริ่มต้น ต้อง tune ตามจริงเมื่อทดสอบกับภาพตัวอย่าง
> อีกวิธีคือ auto-detect palette: รวม dominant colors จากทุก slot → K-Means cluster เป็น N สี

### 9.5 Validation Rules

```
function validateRecognition(result):
    colorCounts ← countOccurrences(result.allColors)

    errors ← []
    for each (color, count) in colorCounts:
        if count ≠ TUBE_CAPACITY:     // ทุกสีต้องมี = 4
            errors.append("Color " + color + " has " + count + " blocks (expected 4)")

    emptyTubeCount ← count tubes where tube.isEmpty()
    if emptyTubeCount < 2:
        errors.append("Only " + emptyTubeCount + " empty tubes (expected ≥ 2)")

    totalColorBlocks ← sum(colorCounts.values)
    expectedBlocks ← (result.tubeCount - emptyTubeCount) * TUBE_CAPACITY
    if totalColorBlocks ≠ expectedBlocks:
        errors.append("Block count mismatch: " + totalColorBlocks + " vs " + expectedBlocks)

    return errors
```

### 9.6 Recognition Result Data Structures

```java
/**
 * ผลลัพธ์จากการ recognize ภาพหน้าจอเกม
 */
class RecognitionResult {
    BoardState boardState;             // สถานะที่ extract ได้ (null ถ้า invalid)
    List<TubeRegion> tubeRegions;      // ตำแหน่ง + สีของแต่ละหลอด
    Map<Integer, String> colorMap;     // color ID → ชื่อสี (เช่น 1 → "Red")
    double confidence;                 // ความมั่นใจรวม (0.0–1.0)
    List<String> warnings;            // เตือนเรื่อง slot ที่ไม่ชัด
    List<String> errors;              // ข้อผิดพลาด (ถ้ามี)
    boolean isValid;                   // validation ผ่านหรือไม่
}

class TubeRegion {
    int tubeIndex;                     // ลำดับหลอด (0-based)
    Rectangle bounds;                  // พิกัด (x, y, w, h) ในภาพต้นฉบับ
    int[] extractedColors;             // สีที่ extract ได้ (bottom → top)
    double[] slotConfidences;          // ความมั่นใจของแต่ละ slot
}
```

### 9.7 User Confirmation Flow

```
1. ระบบแสดงภาพต้นฉบับ + วาด bounding box รอบหลอดที่ตรวจพบ
2. ด้านข้างแสดง BoardState ที่ extract ได้ (หลอดเป็นกราฟิก)
3. ถ้ามี warnings → highlight slot ที่ไม่มั่นใจ (เช่น ขอบสีแดง)
4. ผู้ใช้สามารถ:
   a. ✅ Confirm — ยืนยันว่าถูกต้อง → ไปขั้น Solve
   b. 🎨 Edit — คลิกที่ slot เพื่อเปลี่ยนสี → แก้ไขแล้ว Confirm
   c. 🔄 Re-upload — อัปโหลดภาพใหม่
```

