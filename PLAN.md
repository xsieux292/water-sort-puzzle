# 🗺️ Water Sort Puzzle — Implementation Plan

> แผนการพัฒนาโปรเจกต์ แบ่งเป็น Phase พร้อม Milestone ที่ชัดเจน

---

## Phase Overview

```
Phase 1:   Core Logic & Algorithm        ✅ เสร็จ
Phase 2:   CLI Polish & Testing          🟡 ส่วนใหญ่เสร็จ (ค้าง: timeout, integration tests 10+ ด่าน)
Phase 2.5: Image Recognition Pipeline    ✅ เสร็จ (Approach C — Hybrid)
Phase 3:   Desktop UI (JavaFX) + Solution Playback   ✅ เสร็จ (ค้าง: Save/Load)
Phase 4:   Web UI (Optional)             ⬜ ยังไม่เริ่ม
```

> สัญลักษณ์: ✅ เสร็จ · 🟡 ทำบางส่วน · ⬜ ยังไม่ทำ
>
> **วิสัยทัศน์ใหม่**: โปรเจกต์นี้ไม่ใช่แค่ solver — แต่เป็น **เครื่องมือครบวงจร**
> ที่ผู้ใช้แค่ถ่ายภาพหน้าจอเกม → ระบบจัดการทุกอย่างให้อัตโนมัติ → แสดง solution ทีละ step

---

## Phase 1 — Core Logic & Algorithm 🔧

> **เป้าหมาย**: Solver ทำงานได้ถูกต้อง มี Unit Test ครอบคลุม

### Milestone 1.1 — Data Model

| Task | File | Status |
|---|---|---|
| สร้าง `Tube` class (push, pop, peek, isFull, isEmpty, isSorted, deepCopy) | `model/Tube.java` | ✅ |
| สร้าง `Move` record (source, destination) | `model/Move.java` | ✅ |
| สร้าง `BoardState` class (tubes array, isGoal, getValidMoves, applyMove) | `model/BoardState.java` | ✅ |
| Implement `equals()` + `hashCode()` สำหรับ `BoardState` | `model/BoardState.java` | ✅ |
| Implement `toCanonicalString()` สำหรับ state caching | `model/BoardState.java` | ✅ |
| Unit test: `TubeTest` — push/pop/isSorted/deepCopy | `test/.../TubeTest.java` | ✅ |
| Unit test: `BoardStateTest` — isGoal/getValidMoves/applyMove/equality | `test/.../BoardStateTest.java` | ✅ |

### Milestone 1.2 — BFS Solver

| Task | File | Status |
|---|---|---|
| สร้าง `Solver` interface (solve method) | `solver/Solver.java` | ✅ |
| สร้าง `SolveResult` class (moves, stats) | `solver/SolveResult.java` | ✅ |
| Implement `BFSSolver` — BFS ตาม spec | `solver/BFSSolver.java` | ✅ |
| Implement pruning rules (no reverse, skip completed, identical empties) | `solver/BFSSolver.java` | ✅ |
| Unit test: ด่านง่าย (3 หลอด 1 สี) → ต้องได้คำตอบ | `test/.../BFSSolverTest.java` | ✅ |
| Unit test: ด่านปานกลาง (5 หลอด) → ต้องได้ optimal | `test/.../BFSSolverTest.java` | ✅ |
| Unit test: ด่านที่แก้ไม่ได้ → `isSolved() == false` | `test/.../BFSSolverTest.java` | ✅ |

### Milestone 1.3 — A* Solver

| Task | File | Status |
|---|---|---|
| Implement heuristic function (color-change count) | `solver/AStarSolver.java` | ✅ |
| Implement `AStarSolver` — A* ตาม spec | `solver/AStarSolver.java` | ✅ |
| เทียบ performance BFS vs A* กับด่านจริง 14 หลอด (ผลอยู่ใน README → Performance) | benchmark | ✅ |

### Milestone 1.4 — CLI Entry Point

| Task | File | Status |
|---|---|---|
| สร้าง `BoardParser` — parse input จาก stdin/file | `util/BoardParser.java` | ✅ |
| สร้าง `Main.java` — CLI entry point | `Main.java` | ✅ |
| รองรับ JSON input file | `util/BoardParser.java` | ✅ |
| แสดงผลลัพธ์แบบ step-by-step | `Main.java` | ✅ |
| แสดง stats (states explored, time elapsed) | `Main.java` | ✅ |

---

## Phase 2 — CLI Polish & Testing 🧪

> **เป้าหมาย**: โปรแกรม CLI ใช้งานได้จริง พร้อม edge case handling

| Task | Status |
|---|---|
| Error handling สำหรับ invalid input (JSON ผิด format, ภาพอ่านไม่ได้, บอร์ดไม่ valid ใน UI) | ✅ |
| Timeout mechanism (เช่น 30 วินาที) | ⬜ — ตอนนี้จำกัดที่จำนวน state (2,000,000) แทนเวลา |
| Memory limit warning | ⬜ — ตอนนี้ใช้เพดาน 2,000,000 states + แอป GUI รันด้วย `-Xmx2g` |
| เพิ่ม integration tests กับด่านจริง 10+ ด่าน | ⬜ |
| Performance profiling & optimization (A* parent-pointer, heuristic h2, cached key) | ✅ |
| สร้าง `build.gradle` หรือ `pom.xml` สำหรับ build tool (ใช้ `build.gradle.kts`) | ✅ |
| README: เพิ่ม benchmark results | ✅ |

---

## Phase 2.5 — Image Recognition Pipeline 📸

> **เป้าหมาย**: ผู้ใช้อัปโหลดภาพหน้าจอเกม → ระบบตรวจจับหลอด, แยกสี, สร้าง BoardState อัตโนมัติ

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

### Architecture — 3 Approaches (เลือก 1)

| Approach | วิธีการ | Pros | Cons |
|---|---|---|---|
| **A. Classical CV** | OpenCV: edge detection → contour → crop → dominant color per region | ไม่ต้องใช้ model, เร็ว, เบา | เปราะต่อ background/theme ที่ต่างกัน |
| **B. Pre-trained Vision Model** | ใช้ Vision API (Google Cloud Vision, GPT-4V, Gemini) ส่งรูปไป → ได้ JSON config กลับ | แม่นยำมาก, รองรับหลาย theme | ต้องมี API key, มีค่าใช้จ่าย |
| **C. Hybrid** | Classical CV ตรวจจับ/crop หลอด + simple color matching (K-Means / HSV lookup) | สมดุลระหว่างความแม่น + ความประหยัด | ต้อง tune ค่าสี |

> **แนะนำ**: เริ่มจาก **Approach C (Hybrid)** เพราะประหยัดและควบคุมได้
> ถ้าแม่นไม่พอ ค่อยเพิ่ม Approach B เป็น fallback
>
> ✅ **เลือกแล้ว: Approach C** — Classical CV (OpenCV) ตรวจจับหลอด + จัดกลุ่มสีด้วย clustering ใน LAB
> (ภาพตัวอย่าง `test_image.png` อ่านถูก 100%; Vision API fallback ยังไม่ได้ทำ)

### Step-by-Step Pipeline Detail

#### Step 1 — Tube Detection & Cropping

```
Input: ภาพหน้าจอเกม (เช่น 1024x1024 px)

1. Convert to grayscale
2. Apply Gaussian blur (reduce noise)
3. Edge detection (Canny)
4. Find contours → filter by:
   - Aspect ratio ≈ 1:3 ถึง 1:4 (หลอดแคบยาว)
   - Area ≈ ขนาดที่คาดหวัง (ไม่เล็ก/ใหญ่เกินไป)
   - Position clustering (หลอดอยู่เป็นแถว)
5. Sort contours left-to-right, top-to-bottom → ได้ลำดับหลอด
6. Crop แต่ละหลอดออกมาเป็น sub-image

Output: List<Image> tubeImages (เรียงตามลำดับ)
```

#### Step 2 — Color Extraction & Identification (ตามที่ implement จริง)

```
Input: sub-image ของหลอด 1 หลอด (bounds จาก Step 1 รวมฝาหลอดด้านบนมาด้วย)

1. ตัดฝาหลอดด้านบน (CAP_FRACTION = 13% ของความสูง) และขอบโค้งด้านล่าง (1.5%)
   แล้วแบ่งส่วนที่เหลือเป็น 4 ช่อง (slot) เท่า ๆ กัน
2. สำหรับแต่ละ slot:
   a. ใช้เฉพาะกึ่งกลาง (เว้นขอบ 40% ทั้งแนวตั้ง/แนวนอน) เลี่ยงขอบหลอด/ไฮไลท์
   b. คำนวณค่าเฉลี่ย LAB, HSV และ RGB ของพื้นที่นั้น
3. แยก "ช่องว่าง" ออกจาก "บล็อกสี": ช่องว่าง = พื้นหลังมืดที่ไม่มีสี (L ≤ 55 และ chroma ≤ 15)
   — บล็อกสีเข้มแต่อิ่มตัวยังนับเป็นสี
4. จัดกลุ่มสีทั้งภาพด้วย constrained agglomerative clustering ใน LAB
   (average linkage, ≤ 4 บล็อกต่อกลุ่ม, จำนวนกลุ่ม = จำนวนบล็อกสี ÷ 4)
5. ตั้งชื่อสีของแต่ละกลุ่มด้วย ColorNamer (nearest reference, redmean distance)
   และส่งค่า RGB จริงของกลุ่มออกไปให้ UI วาดบอร์ดตรงกับเกม

Output: int[] colors (ขนาด 0–4, ก้นหลอด → บนสุด)
```

> **ต่างจากแผนเดิม**: ไม่ใช้ HSV lookup ตายตัว + K-Means k=1 ต่อ slot เพราะต้อง tune ค่าต่อธีม —
> การจัดกลุ่มแบบ "ทุกสีต้องมี 4 บล็อก" ไม่ต้องรู้ palette ล่วงหน้า (`ColorPalette` ยังอยู่ในโค้ดเป็น HSV lookup อ้างอิง แต่ pipeline หลักไม่เรียกใช้)

#### Step 3 — Validation & BoardState Construction

```
1. ตรวจสอบว่าทุกสีมีจำนวน = 4 (TUBE_CAPACITY)
2. ตรวจสอบว่าจำนวนหลอดเปล่า ≥ 2
3. ถ้าตรวจไม่ผ่าน → แสดง preview ให้ user ยืนยัน/แก้ไข
4. สร้าง BoardState จากข้อมูลที่ extract ได้
```

### Data Structures (เพิ่มเติม)

```java
/**
 * ผลลัพธ์จากการ recognize ภาพ
 */
class RecognitionResult {
    BoardState boardState;           // สถานะที่ extract ได้ (null ถ้าอ่านไม่ได้เลย)
    List<TubeRegion> tubeRegions;    // ตำแหน่งของหลอดในภาพ
    Map<Integer, String> colorMap;   // color ID → ชื่อสี (เช่น "Light Green")
    Map<Integer, int[]> colorRgb;    // color ID → {r,g,b} จริงที่วัดได้ (ให้ UI วาดสีตรงกับเกม)
    double confidence;               // ความมั่นใจ (1.0 = ผ่าน validation, 0.5 = ไม่ผ่าน)
    List<String> warnings;           // เช่น "Colored blocks count (47) is not a multiple of 4."
    List<String> errors;             // เช่น "Color 'Red' has 3 blocks (expected 4)."
    boolean isValid;                 // validation ผ่านหรือไม่
}

class TubeRegion {
    int tubeIndex;
    Rectangle bounds;      // พิกัดในภาพต้นฉบับ
    int[] extractedColors; // สีที่ extract ได้ (ก้นหลอด → บนสุด)
    double[] slotConfidences; // ปัจจุบันเป็นค่าคงที่ 1.0 (ยังไม่ได้คำนวณจริงต่อ slot)
}
```

### Tasks

| Task | File | Status |
|---|---|---|
| ตัดสินใจ approach (A/B/C) → เลือก C (Hybrid) | — | ✅ |
| Setup OpenCV dependency (ถ้าใช้ approach A/C) | `pom.xml` / `build.gradle` | ✅ |
| Implement `TubeDetector` — detect & crop tubes from screenshot | `vision/TubeDetector.java` | ✅ |
| Implement `ColorExtractor` — extract สีเฉลี่ย LAB/HSV/RGB ต่อ slot | `vision/ColorExtractor.java` | ✅ |
| สร้าง `ColorPalette` — predefined HSV ranges for known colors (อ้างอิง; pipeline หลักใช้ clustering) | `vision/ColorPalette.java` | ✅ |
| สร้าง `ColorNamer` — ตั้งชื่อสีที่อ่านง่ายจาก RGB | `vision/ColorNamer.java` | ✅ |
| Implement `ImageRecognizer` — orchestrate pipeline (detect → extract → validate) | `vision/ImageRecognizer.java` | ✅ |
| สร้าง `RecognitionResult` + `TubeRegion` data classes | `vision/RecognitionResult.java` | ✅ |
| Unit test: ทดสอบกับภาพตัวอย่าง (14 หลอด) → ต้อง extract ถูกต้อง | `test/.../ImageRecognizerTest.java` | ✅ |
| Unit test: ทดสอบกับ theme/background ที่ต่างกัน | `test/.../ImageRecognizerTest.java` | ⬜ |
| Implement user confirmation flow — แสดง preview ก่อน solve (อยู่ในชั้น UI: Recognition Preview + Confirm) | `ui/MainView.java` | ✅ |
| (Optional) เพิ่ม Vision API fallback (Approach B) | `vision/VisionApiFallback.java` | ⬜ |

---

## Phase 3 — Desktop GUI (JavaFX) + Solution Playback 🖥️

> **เป้าหมาย**: มี UI ครบวงจร — อัปโหลดรูป / แก้ไขสี / กด Solve / ดู animation ทีละ step

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
| **📸 Screenshot Upload** | Drag & Drop หรือ Browse ภาพหน้าจอเกม → ระบบ recognize อัตโนมัติ |
| **🔍 Recognition Preview** | แสดงผลการ extract สีให้ user ยืนยันก่อน solve (พร้อมไฮไลท์ตำแหน่งหลอดบนภาพต้นฉบับ) |
| **🎨 Board Editor** | ให้ผู้ใช้คลิกแก้ไขสี กรณี recognize ผิดหรืออยากใส่เอง |
| **Visual Tubes** | แสดงหลอดแก้วเป็นภาพกราฟิก สีสดใส |
| **▶️ Solve Button** | กดแล้วรัน Solver ใน background thread |
| **⏮️⏭️ Step Navigator** | เดินหน้า/ถอยหลังทีละ step ดูการเทน้ำ |
| **🎬 Pour Animation** | แสดง animation สีไหลจากหลอดหนึ่งไปอีกหลอด (TranslateTransition) |
| **📊 Progress Bar** | แสดง Step X/Y + progress bar |
| **▶️ Auto-Play** | กด play แล้วระบบเล่น animation ต่อเนื่องจนจบ (ปรับความเร็วได้) |
| **Preset Puzzles** | มีด่านตัวอย่างให้เลือกทดลอง |
| **🧭 Mode Select** | หน้าแรกให้เลือกโหมด: (1) Image Recognizer (2) Play Game |
| **🎮 Play Game** | ระบบสร้างด่านด้วย `PuzzleGenerator` ผู้เล่นคลิกหลอดเทเอง มี Undo / Restart / Hint / Solve for me |

### User Flow (End-to-End)

```
0. ผู้ใช้เปิดแอป → เลือกโหมด
   ├── 🎮 Play Game: เลือกจำนวนสี/ความยาก → Generate → ผู้เล่นเท (Undo/Hint/Solve for me)
   └── 📸 Image Recognizer: ไปต่อขั้นตอนด้านล่าง
         │
1. (โหมด Image Recognizer)
         │
2. เลือกวิธี input:
   ├── 📸 อัปโหลดภาพหน้าจอ  ───────────────────────┐
   │       │                                        │
   │   3a. ระบบ detect หลอด + extract สี             │
   │       │                                        │
   │   3b. แสดง preview + ไฮไลท์บนภาพต้นฉบับ         │
   │       │                                        │
   │   3c. ผู้ใช้ตรวจ → แก้ไขถ้าผิด → กด ✅ Confirm  │
   │                                                │
   └── 🎨 ใส่สีเอง (Manual Editor)  ────────────────┘
         │
4. กด ▶️ Solve
         │
5. ระบบแสดง Solution:
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
| สร้าง `UploadPane` — Drag & Drop zone สำหรับอัปโหลดภาพ | ✅ |
| สร้าง `RecognitionPreviewPane` — แสดงภาพต้นฉบับ + ไฮไลท์หลอด + สีที่ extract ได้ | ✅ |
| สร้าง `TubeView` component — แสดงหลอดเป็น Rectangle + Color | ✅ |
| สร้าง `BoardView` — layout หลอดทั้งหมดแบบ grid | ✅ |
| สร้าง `ColorPicker` panel สำหรับ edit mode (แก้ไขสีที่ recognize ผิด) | ✅ |
| สร้าง control bar (Confirm / Solve / Next / Prev / Auto-Play / Reset) | ✅ |
| เชื่อม Vision Pipeline (Phase 2.5) กับ UI | ✅ |
| เชื่อม Solver กับ UI (run on background thread) | ✅ |
| Implement step-by-step playback + progress bar | ✅ |
| Implement pour animation (TranslateTransition) | ✅ |
| Implement auto-play mode (ปรับความเร็วได้) | ✅ |
| Light/Dark theme support | ✅ |
| หน้าเลือกโหมด: (1) Image Recognizer / (2) Play Game | ✅ |
| โหมด Play Game: สร้างด่านด้วย `PuzzleGenerator`, ผู้เล่นเทเอง, Undo/Restart/Hint/Solve for me | ✅ |
| Save/Load puzzle state | ⬜ |

---

## Phase 4 — Web UI (Optional) 🌐

> **เป้าหมาย**: Deploy เป็น web app ให้ทุกคนใช้ได้โดยไม่ต้องติดตั้ง

### Options

| Option | Pros | Cons |
|---|---|---|
| **Port Solver to JavaScript** | รันบน browser ได้เลย | ต้อง rewrite |
| **Java backend + Web frontend** | ใช้ Solver เดิมได้ | ต้องมี server |
| **GraalVM/TeaVM compile to WASM** | ใช้ Java code เดิม, รันบน browser | ซับซ้อนกว่า |

### Planned Stack (ถ้าเลือก Option 2)

```
Frontend: HTML/CSS/JS (Vanilla or React)
Backend:  Spring Boot REST API
API:      POST /api/solve  { tubes: [...] }  → { steps: [...] }
Deploy:   Docker + any cloud platform
```

### Tasks

| Task | Status |
|---|---|
| ตัดสินใจ approach (Option 1/2/3) | ⬜ |
| สร้าง REST API สำหรับ Solver | ⬜ |
| สร้าง Web frontend — Board Editor | ⬜ |
| สร้าง Web frontend — Solution Viewer | ⬜ |
| Animation ใน browser (Canvas or CSS) | ⬜ |
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
| **Spring Boot** | Web backend (ถ้าเลือก) | 4 |
| *(Optional)* **Google Cloud Vision / Gemini API** | Vision API fallback สำหรับ recognition ที่แม่นยำกว่า | 2.5 |

---

## Risk & Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| BFS memory explosion (ด่าน 14+ หลอด) | High | ใช้ A* + pruning rules ลด state space |
| Heuristic ไม่ admissible | Medium | ทดสอบกับ BFS เทียบว่าได้ optimal เหมือนกัน |
| **Image recognition ผิดพลาด** (สีคล้ายกัน, theme มืด) | **High** | **ให้ user preview + แก้ไขก่อน solve เสมอ** |
| **หลอดตรวจไม่ครบ** (ภาพเอียง/ถูกบัง) | **Medium** | **Fallback: manual editor / ใช้ Vision API** |
| OpenCV dependency ใหญ่ | Medium | ใช้ JavaCV wrapper, bundle เฉพาะ module ที่ต้องใช้ |
| JavaFX ติดตั้งยากบน user เครื่อง | Medium | พิจารณา Web UI เป็น alternative |
| State string collision (hash) | Low | ใช้ canonical string ที่ unique จริงๆ |
