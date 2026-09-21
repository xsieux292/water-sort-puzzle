# 🧪 Water Sort Puzzle — Auto-Solver

> เครื่องมือครบวงจรสำหรับแก้เกม Water Sort Puzzle — ถ่ายภาพหน้าจอ → ระบบ detect อัตโนมัติ → หาวิธีแก้ → แสดง step-by-step

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
| **เทน้ำวนลูป (Infinite Loop)** | ใช้ State Caching (`HashSet`) จดจำสถานะที่เคยผ่าน ไม่ย้อนกลับ |
| **หาเส้นทางสั้นที่สุด (Optimization)** | BFS รับประกัน shortest path / A* เพิ่มความเร็วด้วย heuristic |

---

## 🎯 Features (Planned)

### Phase 1 — Core Logic & Algorithm *(current)*
- [x] กำหนด Game Rules & Specification
- [ ] สร้าง Data Model (`Tube`, `BoardState`, `Move`)
- [ ] Implement BFS Solver
- [ ] Implement A* Solver with heuristic
- [ ] State Caching & Loop Detection
- [ ] CLI Input/Output
- [ ] Unit Tests

### Phase 2.5 — Image Recognition Pipeline ★
- [ ] 📸 รับภาพหน้าจอเกม (PNG/JPG)
- [ ] 🔍 Detect & Crop หลอดแก้วจากภาพ (OpenCV / Classical CV)
- [ ] 🎨 Extract สีแต่ละ slot ด้วย HSV + K-Means
- [ ] ✅ สร้าง `BoardState` อัตโนมัติ + Validation
- [ ] 👤 User Preview & Confirmation ก่อน Solve

### Phase 3 — UI + Solution Playback
- [ ] 📸 Drag & Drop อัปโหลดภาพหน้าจอ
- [ ] 🔍 แสดง Recognition Preview (ไฮไลท์หลอดบนภาพต้นฉบับ)
- [ ] 🎨 Board Editor — แก้ไขสีที่ recognize ผิด
- [ ] ▶️ Solve → แสดง animation ทีละ step
- [ ] ⏮️⏭️ Step Navigator (เดินหน้า/ถอยหลัง)
- [ ] ▶️ Auto-Play mode
- [ ] Web UI (optional)

---

## 🏗️ Project Structure

```
water-sort-puzzle/
├── README.md                   # ภาพรวมโปรเจกต์
├── SPEC.md                     # Game Specification & Rules
├── PLAN.md                     # Implementation Plan & Roadmap
├── .gitignore
│
├── src/
│   └── main/
│       └── java/
│           └── watersort/
│               ├── model/          # Data structures
│               │   ├── Tube.java
│               │   ├── BoardState.java
│               │   └── Move.java
│               ├── solver/         # Search algorithms
│               │   ├── Solver.java          (interface)
│               │   ├── BFSSolver.java
│               │   └── AStarSolver.java
│               ├── vision/         # ★ Image Recognition Pipeline
│               │   ├── TubeDetector.java     (detect & crop tubes)
│               │   ├── ColorExtractor.java   (extract colors per slot)
│               │   ├── ColorPalette.java     (HSV color mapping)
│               │   ├── ImageRecognizer.java   (orchestrator)
│               │   └── RecognitionResult.java (result data)
│               ├── util/           # Helpers
│               │   └── BoardParser.java
│               └── Main.java       # Entry point (CLI)
│
└── src/
    └── test/
        └── java/
            └── watersort/
                ├── model/
                │   ├── TubeTest.java
                │   └── BoardStateTest.java
                ├── solver/
                │   └── BFSSolverTest.java
                └── vision/
                    └── ImageRecognizerTest.java
```

---

## 🚀 Quick Start

```bash
# Compile
javac -d out src/main/java/watersort/**/*.java src/main/java/watersort/*.java

# Run
java -cp out watersort.Main
```

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

## 📄 Documentation

- **[SPEC.md](SPEC.md)** — Game rules, data structures, algorithm specification
- **[PLAN.md](PLAN.md)** — Implementation roadmap, phases, and milestones

---

## 📜 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
