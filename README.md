# 🧪 Water Sort Puzzle — Auto-Solver

> เครื่องมือครบวงจรสำหรับ Water Sort Puzzle — ถ่ายภาพหน้าจอ → ระบบ detect อัตโนมัติ → หาวิธีแก้ → แสดง step-by-step
> หรือเล่นเองกับด่านที่ระบบสร้างให้ (Undo / Hint / Solve for me)

[![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 Overview

**Water Sort Puzzle Auto-Solver** ไม่ใช่แค่โปรแกรมแก้เกม — แต่เป็น **เครื่องมือครบวงจร**
ที่ผู้ใช้เพียงอัปโหลดภาพหน้าจอเกม → ระบบจะ detect หลอดแก้ว + แยกสีอัตโนมัติ
→ คำนวณหาวิธีแก้ที่สั้นที่สุด → แสดงผลเป็น animation ทีละขั้นตอน

### ปัญหาที่แก้ได้

| ปัญหา | วิธีแก้ |
|---|---|
| **ผู้เล่นติดตัน (Stuck State)** | โปรแกรมค้นหาทุกเส้นทางที่เป็นไปได้ ถ้าด่านมีทางออก จะเจอเสมอ |
| **เทน้ำวนลูป (Infinite Loop)** | ใช้ State Caching (`HashSet` ใน BFS / `HashMap` gScore ใน A*) จดจำสถานะที่เคยผ่าน ไม่ย้อนกลับ |
| **หาเส้นทางสั้นที่สุด (Optimization)** | BFS รับประกัน shortest path / A* เพิ่มความเร็วด้วย heuristic |

---

## 🎯 Features

### Core — Solver & CLI
- [x] Game Rules & Specification ([SPEC.md](SPEC.md))
- [x] Data Model (`Tube`, `BoardState`, `Move`)
- [x] BFS Solver + A* Solver (heuristic `max(h1, h2)`, optimal)
- [x] State Caching & Loop Detection
- [x] CLI: interactive, `--demo`, `--file` (JSON), `--image`, `--generate`, `--batch`
- [x] Puzzle Generator (สุ่มด่าน + verify ว่าแก้ได้ด้วย A*, 3 ระดับความยาก)
- [x] Unit Tests (JUnit 5) + JaCoCo coverage

### Image Recognition Pipeline ★
- [x] 📸 รับภาพหน้าจอเกม (PNG/JPG)
- [x] 🔍 Detect & Crop หลอดแก้วจากภาพ (OpenCV / Classical CV)
- [x] 🎨 Extract สีแต่ละ slot (LAB) + จัดกลุ่มสีด้วย constrained clustering + ตั้งชื่อสี
- [x] ✅ สร้าง `BoardState` อัตโนมัติ + Validation (ทุกสี 4 บล็อก, หลอดว่าง ≥ 2)
- [x] 👤 User Preview & Confirmation ก่อน Solve
- [ ] Vision API fallback (optional)

### Desktop GUI (JavaFX) — 2 โหมด
- [x] 🧭 หน้าเลือกโหมด: **Image Recognizer** / **Play Game**
- [x] 📸 Drag & Drop อัปโหลดภาพหน้าจอ
- [x] 🔍 Recognition Preview (ไฮไลท์หลอดบนภาพต้นฉบับ) + แสดงสีจริงจากภาพ
- [x] 🎨 Board Editor — แก้ไขสีที่ recognize ผิด
- [x] ▶️ Solve → Pour animation ทีละ step, ⏮️⏭️ Step Navigator, Auto-Play ปรับความเร็วได้
- [x] 🎮 Play Game — ระบบสร้างด่าน ผู้เล่นเทเอง + Undo / Restart / Hint / Solve for me
- [x] 🌓 Dark theme (ค่าเริ่มต้น) / Light theme
- [ ] Save/Load puzzle state
- [ ] Web UI (optional)

---

## 🏗️ Project Structure

```
water-sort-puzzle/
├── README.md                   # ภาพรวมโปรเจกต์
├── SPEC.md                     # Game Specification & Rules
├── PLAN.md                     # Implementation Plan & Roadmap
├── build.gradle.kts            # Gradle (JavaFX plugin, OpenCV/JavaCV, JUnit 5, JaCoCo)
├── LICENSE
│
├── src/main/java/watersort/
│   ├── Main.java               # CLI entry point
│   ├── model/                  # Data structures
│   │   ├── Tube.java · BoardState.java · Move.java
│   ├── solver/                 # Search algorithms
│   │   ├── Solver.java (interface) · SolveResult.java
│   │   └── BFSSolver.java · AStarSolver.java
│   ├── generator/              # Puzzle Generator
│   │   └── PuzzleGenerator.java · GeneratedPuzzle.java
│   ├── vision/                 # ★ Image Recognition Pipeline
│   │   ├── TubeDetector.java      (detect tubes)
│   │   ├── ColorExtractor.java    (LAB/HSV/RGB per slot)
│   │   ├── ColorNamer.java        (ตั้งชื่อสี)
│   │   ├── ColorPalette.java      (HSV lookup — อ้างอิง)
│   │   ├── ImageRecognizer.java   (orchestrator + clustering + validation)
│   │   └── RecognitionResult.java · TubeRegion.java
│   ├── util/
│   │   └── BoardParser.java       (interactive / JSON)
│   └── ui/                     # ★ Desktop GUI (JavaFX 21)
│       ├── WaterSortApp.java      (GUI entry point)
│       ├── MainView.java          (mode select + user flow controller)
│       ├── component/             (ModeSelectPane, GameSetupPane, GameBar, UploadPane,
│       │                           RecognitionPreviewPane, TubeView, BoardView,
│       │                           ColorPickerPanel, ControlBar, SolutionPanel)
│       ├── model/                 (GameSession, PlaybackModel, BoardEditor, Validation,
│       │                           ColorScheme, Presets, TubeColors)
│       └── util/Fx.java           (micro-animations)
├── src/main/resources/watersort/ui/   # app.css · dark.css · light.css
│
└── src/test/
    ├── resources/test_image.png       # ภาพตัวอย่าง 14 หลอด (12 สี)
    └── java/watersort/
        ├── model/     TubeTest · BoardStateTest
        ├── solver/    BFSSolverTest · AStarSolverTest
        ├── generator/ PuzzleGeneratorTest
        ├── util/      BoardParserTest
        ├── vision/    ImageRecognizerTest · ColorPaletteTest · ColorNamerTest · SampleBoard (ground truth)
        └── ui/        BoardEditorTest · PlaybackModelTest · GameSessionTest · ColorSchemeTest
```

---

## 🚀 Quick Start

```bash
# Desktop GUI (JavaFX) — เลือกโหมด: Image Recognizer หรือ Play Game
./gradlew run

# Command-line interface
./gradlew runCli --console=plain                              # interactive
./gradlew runCli --args="--demo" --console=plain              # ด่านตัวอย่าง
./gradlew runCli --args="--file puzzle.json" --console=plain  # จาก JSON
./gradlew runCli --args="--image screenshot.png" --console=plain
./gradlew runCli --args="--generate 6 hard" --console=plain   # สร้างด่าน 6 สี ระดับ hard

# Build + test (พร้อม coverage report ที่ build/reports/jacoco)
./gradlew build
```

**GUI shortcuts (playback ในโหมดใดก็ได้):** `←` Prev · `→` Next · `Space` Auto-Play/Pause. Dark theme is the default; toggle Light/Dark with the button in the top-right corner.

### Example Input

```
Tube  1: [Red, Blue, Red, Green]
Tube  2: [Blue, Green, Red, Blue]
Tube  3: [Green, Red, Green, Blue]
Tube  4: []
Tube  5: []
```

### Example Output

```
✅ Solution found in 12 steps!

Step  1: Pour Tube 1 → Tube 4
Step  2: Pour Tube 2 → Tube 5
Step  3: Pour Tube 1 → Tube 2
...
Step 12: Pour Tube 3 → Tube 5

🎉 All tubes sorted!
```

### End-to-End Flow (with Image Recognition)

```
📸 Upload Screenshot → 🔍 Auto-Detect Tubes → 🎨 Extract Colors
                              ↓
                  👤 User Preview & Confirm
                              ↓
                      ▶️ Solve (BFS/A*)
                              ↓
                  ⏮️ Step-by-step Animation ⏭️
```

---

## 🎮 Modes

เปิดแอป (`./gradlew run`) แล้วเลือกโหมดที่หน้าแรก (กลับมาเลือกใหม่ได้ด้วยปุ่ม **Change Mode** มุมขวาบน):

| โหมด | ทำอะไร | Flow |
|---|---|---|
| **1. Image Recognizer** | อัปโหลดภาพหน้าจอเกมจริง ระบบอ่านหลอด/สี แล้วหาวิธีแก้ให้ | Upload → Recognize → Preview → (Edit) → Confirm → Solve → Playback |
| **2. Play Game** | ระบบสร้างด่านให้ด้วย `PuzzleGenerator` (การันตีว่าแก้ได้) แล้วผู้เล่นเล่นเอง | Setup (3–10 สี, Easy/Medium/Hard) → Generate → เล่น |

### โหมด Play Game
- **เล่น:** คลิกหลอดเพื่อยกขึ้น (เลือกต้นทาง) แล้วคลิกหลอดปลายทางเพื่อเท — เทได้เมื่อปลายทางไม่เต็ม และว่าง/สีบนสุดตรงกัน (ตรงตามกติกาใน SPEC; เทได้แม้เป็น move ที่ solver ตัดทิ้ง) เทไม่ได้จะย้ายการเลือก/สั่นหลอดที่เต็ม
- **Undo / Restart** — ย้อนทีละครั้ง หรือเริ่มด่านใหม่
- **Hint** — ให้ A* คำนวณจากตำแหน่งปัจจุบัน แล้วไฮไลท์คู่หลอดที่ควรเท (บอกด้วยว่าอย่างน้อยเหลืออีกกี่ move; ถ้าเดินจนไม่มีทางแก้ได้ จะบอกให้ Undo)
- **Solve for me** — ให้ solver แก้ต่อจากตำแหน่งปัจจุบัน แล้วดูเป็น playback (Prev/Next/Auto-Play) กด **Back to Game** เพื่อกลับไปเล่นต่อจากเดิม
- **คะแนน:** แสดง Moves เทียบกับ "Best possible" (จำนวน move ที่น้อยที่สุด จาก A*) ชนะแล้วจะบอกว่า Perfect หรือไม่
- ระดับความยาก: Easy มีหลอดว่าง 3 หลอด, Medium/Hard มี 2 หลอด (Hard บังคับให้คำตอบสั้นสุดยาวขึ้น)

> `PuzzleGenerator` เดิมตรวจว่าด่านแก้ได้ด้วย BFS ซึ่งช้ามากเมื่อสีเยอะ (10 สีค้างเกิน 10 นาที) เปลี่ยนเป็น A* (ได้จำนวน step optimal เท่ากัน) — ตอนนี้สร้างด่าน 10 สีได้ใน ~1 วินาที

---

## ⚡ Performance & Optimization

### A* Solver

ปรับ A* ให้เร็วขึ้นและกินหน่วยความจำน้อยลงมาก โดยยังรับประกัน **optimal solution** เท่าเดิม (มี unit test เทียบ step count กับ BFS ทุกครั้ง):

| จุดที่ปรับ | เดิม | ใหม่ | ผล |
|---|---|---|---|
| **เก็บเส้นทาง (path)** | copy `ArrayList` ของ moves ทั้งเส้นทางในทุก node → O(states × depth) | node เก็บแค่ `move` + ตัวชี้ไป **parent** แล้ว reconstruct ตอนเจอ goal | ลด allocation/GC ต่อ node จาก O(depth) → O(1) |
| **Heuristic** | นับจำนวนรอยต่อสีที่เปลี่ยน (h1) อย่างเดียว | `max(h1, h2)` เมื่อ **h2 = Σ_สี (จำนวนหลอดที่มีสีนั้น − 1)** — lower bound ที่แน่นกว่าเมื่อสีกระจายหลายหลอด | ตัด state ที่ไม่จำเป็นออกเยอะ |
| **State key** | `toCanonicalString()` สร้าง string ใหม่ทุกครั้งที่เรียก (หลายครั้งต่อ state) + format ยาว `"[1, 2, 3]"` | encode กระชับ 1 char/บล็อก และ **cache** ผลไว้ใน `BoardState` (พร้อม `hashCode`) | ไม่ encode ซ้ำ, string สั้นลง |
| **Tie-breaking** | เรียงตาม `f` อย่างเดียว | `f` เท่ากันเลือก `h` น้อยก่อน (ใกล้ goal) | เจอคำตอบเร็วขึ้น |

**ผลวัดจริง** (บอร์ด preset, เครื่องเดียวกัน):

| ด่าน | States เดิม | States ใหม่ | เวลา เดิม | เวลา ใหม่ |
|---|--:|--:|--:|--:|
| Demo (3 สี) | 751 | **55** | 13 ms | **7 ms** |
| Easy (5 สี) | 5,467 | **922** | 24 ms | **5 ms** |
| Medium (8 สี) | 32,895 | **5,346** | 103 ms | **28 ms** |
| Big (12 สี, 14 หลอด) | 869,495 | **210,150** | 3,622 ms | **788 ms** |

> ด่านใหญ่ 12 สีเร็วขึ้น **~4.6 เท่า** และสำรวจ state น้อยลง ~4 เท่า

### Vision Pipeline

- **ตัดฝาหลอดออกก่อนแบ่ง slot** — bounds ที่ detect ได้รวมฝาสีเข้มด้านบนมาด้วย ถ้าไม่ตัดจะทำให้สี slot บนสุดเพี้ยน (`ColorExtractor.CAP_FRACTION`)
- **แยกพื้นหลังด้วย L + chroma** — เดิมใช้แค่ความสว่าง (L) ทำให้บล็อกสีเข้มถูกมองเป็นหลอดว่าง ตอนนี้ดู chroma ใน LAB ด้วย (`ImageRecognizer.isColored`)
- **ไม่ "เดา" เติมบล็อก** — เดิมถ้าจำนวนบล็อกสีหาร 4 ไม่ลงตัว โค้ดจะเลื่อนช่องว่างที่สว่างที่สุดขึ้นเป็นสี (ทำให้หลอดว่างดูเหมือนมีสี) ตอนนี้ออก warning แล้วให้ validation/ผู้ใช้แก้แทน
- **จัดกลุ่มสีด้วย constrained agglomerative clustering** ใน LAB space (จำกัด ≤ 4 บล็อก/สี) แล้ว **ตั้งชื่อสีที่อ่านง่าย** ด้วย `ColorNamer` (nearest reference + redmean distance)
- **UI แสดงสีจริงจากภาพ** — recognizer ส่งค่า RGB จริงของแต่ละสีออกมา (`RecognitionResult.getColorRgb()`) แล้ว `ColorScheme` ให้ `BoardView`/`ColorPickerPanel`/pour animation วาดด้วยสีที่ตรงกับเกม (แทนพาเลตต์สมมติเดิม) เพื่อให้ผู้ใช้ตรวจสอบกับเกมได้ด้วยตา; ด่าน manual/preset ยังใช้พาเลตต์สดใสมาตรฐาน

ผลกับภาพตัวอย่าง `test_image.png`: ตรวจจับ **ครบทั้ง 12 สี × 48 บล็อก ถูก 100%** และหลอดว่าง 2 หลอด (ล็อกไว้ด้วย test `sampleImageNamesEveryColorCorrectly`)

---

## 📄 Documentation

- **[SPEC.md](SPEC.md)** — Game rules, data structures, algorithm specification
- **[PLAN.md](PLAN.md)** — Implementation roadmap, phases, and milestones

---

## 📜 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
