# 🧪 Water Sort Puzzle — Auto-Solver

> โปรแกรมแก้เกม Water Sort Puzzle อัตโนมัติ ใช้อัลกอริทึม BFS/A* หาเส้นทางที่สั้นที่สุด

[![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 Overview

**Water Sort Puzzle Auto-Solver** คือโปรแกรมที่รับข้อมูลสถานะเริ่มต้นของหลอดแก้วทั้งหมด
แล้วใช้อัลกอริทึมการค้นหา (Search Algorithm) คำนวณลำดับการเทน้ำ (Move Sequence)
ที่จะนำไปสู่สถานะชนะ (Goal State) โดยอัตโนมัติ

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

### Phase 2 — UI (future)
- [ ] Desktop GUI (JavaFX) — แสดงหลอดแก้วเป็นภาพ ลาก-วางเทน้ำ
- [ ] Step-by-step animation ของ solution
- [ ] Web UI (optional) — พอร์ตเป็น JavaScript/TypeScript

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
                └── solver/
                    └── BFSSolverTest.java
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

---

## 📄 Documentation

- **[SPEC.md](SPEC.md)** — Game rules, data structures, algorithm specification
- **[PLAN.md](PLAN.md)** — Implementation roadmap, phases, and milestones

---

## 📜 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.
