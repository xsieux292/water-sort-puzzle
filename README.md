# 🧪 Water Sort Puzzle — Auto-Solver & Game Engine

> เครื่องมือครบวงจรสำหรับ Water Sort Puzzle (ถ่ายภาพหน้าจอ → ตรวจจับสีอัตโนมัติ → หาวิธีแก้ → แสดงแอนิเมชันทีละก้าว) 
> พร้อม Game Engine ในตัวที่ให้คุณสร้างด่านและท้าทายตัวเองได้เต็มรูปแบบ!

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

### 📸 Workflow (การใช้งานจริง)

ระบบถูกออกแบบมาให้ใช้งานง่ายที่สุด เพียง 4 ขั้นตอน:

1. **Upload & Recognize**  
   ลากรูปภาพหน้าจอเกมของคุณมาวางในโปรแกรม ระบบจะประมวลผลอ่านหลอดแก้วและแยกสีให้ทันที  
   ![Step 1: Upload and Recognize](docs/images/workflow_1_upload.png)

2. **Preview & Confirm**  
   ตรวจสอบความถูกต้องของสีที่ระบบอ่านได้เทียบกับภาพต้นฉบับ หากมีจุดไหนที่เงาสะท้อนของเกมทำให้สีเพี้ยนไป คุณสามารถคลิกเพื่อแก้ไขสีให้ถูกต้องได้ทันที  
   ![Step 2: Preview and Edit](docs/images/workflow_2_preview.png)

3. **Solve**  
   เมื่อข้อมูลถูกต้อง กดปุ่ม Solve ระบบจะใช้อัลกอริทึม A* ค้นหาเส้นทางที่สั้นที่สุดให้ภายในเสี้ยววินาที  
   ![Step 3: Solve](docs/images/workflow_3_solve.png)

4. **Playback**  
   ดูแอนิเมชันการเทน้ำทีละขั้นตอน (Step-by-step) หรือกด Auto-Play เพื่อดูจนจบได้อย่างเพลิดเพลิน  
   ![Step 4: Playback](docs/images/workflow_4_playback.png)

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

### 🎮 Game Workflow

สำหรับผู้ที่อยากท้าทายตัวเอง สามารถเปลี่ยนมาเล่นโหมด Play Game ได้!

1. **Setup Game**  
   เลือกระดับความยากและจำนวนสีตามต้องการ ระบบจะสุ่มสร้างด่านที่รับประกันว่า "มีทางแก้ 100%"  
   ![Setup Game](docs/images/game_1_setup.png)

2. **Play & Enjoy**  
   คลิกหลอดแก้วเพื่อเทน้ำทีละหลอด หากติดขัดสามารถใช้ฟังก์ชัน Hint เพื่อดูคำใบ้ หรือกด Solve for me ให้ระบบแก้ต่อให้ได้ตลอดเวลา  
   ![Play Game](docs/images/game_2_play.png)

> **เบื้องหลังความเร็ว:** `PuzzleGenerator` เดิมตรวจว่าด่านแก้ได้ด้วย BFS ซึ่งช้ามากเมื่อสีเยอะ (10 สีค้างเกิน 10 นาที) เราจึงเปลี่ยนมาใช้ A* — ทำให้ตอนนี้สุ่มสร้างด่าน 10 สีที่การันตีทางแก้และหาจำนวน step (Best possible) ได้ในเวลา ~1 วินาที!

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

### Vision Pipeline (Image Recognition)

เราได้พยายามปรับจูน Vision Pipeline เพื่อให้รับมือกับภาพจากเกมจริงที่มีปัญหาเรื่อง "เงาสะท้อน (Glare)" และ "ความมืด" ได้อย่างแม่นยำ โดยใช้วิธีทางคณิตศาสตร์เข้ามาช่วยลดการตั้งค่า Threshold แบบตายตัว:

- **ปรับใช้ CIELAB Color Space:** เราเปลี่ยนมาคำนวณระยะห่างของสีในปริภูมิ LAB ซึ่งสอดคล้องกับการรับรู้ของดวงตามนุษย์มากกว่า RGB/HSV ทำให้การแยกแยะเฉดสีที่ใกล้เคียงกันทำได้เป็นธรรมชาติมากขึ้น
- **การเก็บตัวอย่างสี (Core Sampling):** เนื่องจากภาพหลอดแก้วมักจะมีเงาสะท้อนสว่างๆ ที่ขอบข้าง เราจึงสั่งให้ระบบเจาะเก็บค่าสีเฉพาะพื้นที่ 20% ตรงกลางของแต่ละบล็อก เพื่อให้ได้ "สีเนื้อแท้" ของน้ำจริงๆ
- **แยกพื้นหลังแบบไดนามิก (Lightness Sorting):** สีในเกมบางสีมีความมืดมากจนระบบมักจะเข้าใจผิดว่าเป็นสีดำของฉากหลัง เราจึงเปลี่ยนวิธีมาเป็นการนำค่าสีทั้งหมดมาเรียงลำดับความสว่าง (Lightness) แล้วคัดแยกบล็อกที่สว่างที่สุดออกมาเป็นสีตามจำนวนที่เกมกำหนด ทำให้เรากรองฉากหลังทิ้งได้อย่างหมดจดโดยไม่กระทบกับสีโทนเข้ม
- **Constrained Agglomerative Clustering:** บังคับเงื่อนไขให้กลุ่มสี 1 กลุ่มมีสมาชิกได้ไม่เกิน 4 บล็อกเสมอ ทำให้การควบรวมสีในขั้นตอนสุดท้ายสมบูรณ์แบบตามกติกาของเกม

ผลลัพธ์จากการนำเทคนิคเหล่านี้มาประกอบกัน ทำให้ระบบสามารถสกัดสีจากภาพตัวอย่างได้อย่างถูกต้อง 100% และลดภาระของผู้ใช้ในการต้องมานั่งแก้ไขสีเองลงไปได้มากครับ

---

## 📄 Documentation

- **[SPEC.md](SPEC.md)** — Game rules, data structures, algorithm specification
- **[PLAN.md](PLAN.md)** — Implementation roadmap, phases, and milestones

---

## 📜 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
