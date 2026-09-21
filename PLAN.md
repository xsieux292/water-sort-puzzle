# 🗺️ Water Sort Puzzle — Implementation Plan

> แผนการพัฒนาโปรเจกต์ แบ่งเป็น Phase พร้อม Milestone ที่ชัดเจน

---

## Phase Overview

```
Phase 1: Core Logic & Algorithm     ← เราอยู่ตรงนี้
Phase 2: CLI Polish & Testing
Phase 3: Desktop UI (JavaFX)
Phase 4: Web UI (Optional)
```

---

## Phase 1 — Core Logic & Algorithm 🔧

> **เป้าหมาย**: Solver ทำงานได้ถูกต้อง มี Unit Test ครอบคลุม

### Milestone 1.1 — Data Model

| Task | File | Status |
|---|---|---|
| สร้าง `Tube` class (push, pop, peek, isFull, isEmpty, isSorted, deepCopy) | `model/Tube.java` | ⬜ |
| สร้าง `Move` record (source, destination) | `model/Move.java` | ⬜ |
| สร้าง `BoardState` class (tubes array, isGoal, getValidMoves, applyMove) | `model/BoardState.java` | ⬜ |
| Implement `equals()` + `hashCode()` สำหรับ `BoardState` | `model/BoardState.java` | ⬜ |
| Implement `toCanonicalString()` สำหรับ state caching | `model/BoardState.java` | ⬜ |
| Unit test: `TubeTest` — push/pop/isSorted/deepCopy | `test/.../TubeTest.java` | ⬜ |
| Unit test: `BoardStateTest` — isGoal/getValidMoves/applyMove/equality | `test/.../BoardStateTest.java` | ⬜ |

### Milestone 1.2 — BFS Solver

| Task | File | Status |
|---|---|---|
| สร้าง `Solver` interface (solve method) | `solver/Solver.java` | ⬜ |
| สร้าง `SolveResult` class (moves, stats) | `solver/SolveResult.java` | ⬜ |
| Implement `BFSSolver` — BFS ตาม spec | `solver/BFSSolver.java` | ⬜ |
| Implement pruning rules (no reverse, skip completed, identical empties) | `solver/BFSSolver.java` | ⬜ |
| Unit test: ด่านง่าย (3 หลอด 1 สี) → ต้องได้คำตอบ | `test/.../BFSSolverTest.java` | ⬜ |
| Unit test: ด่านปานกลาง (5 หลอด) → ต้องได้ optimal | `test/.../BFSSolverTest.java` | ⬜ |
| Unit test: ด่านที่แก้ไม่ได้ → ต้อง return null | `test/.../BFSSolverTest.java` | ⬜ |

### Milestone 1.3 — A* Solver

| Task | File | Status |
|---|---|---|
| Implement heuristic function (color-change count) | `solver/AStarSolver.java` | ⬜ |
| Implement `AStarSolver` — A* ตาม spec | `solver/AStarSolver.java` | ⬜ |
| เทียบ performance BFS vs A* กับด่านจริง 14 หลอด | benchmark | ⬜ |

### Milestone 1.4 — CLI Entry Point

| Task | File | Status |
|---|---|---|
| สร้าง `BoardParser` — parse input จาก stdin/file | `util/BoardParser.java` | ⬜ |
| สร้าง `Main.java` — CLI entry point | `Main.java` | ⬜ |
| รองรับ JSON input file | `util/BoardParser.java` | ⬜ |
| แสดงผลลัพธ์แบบ step-by-step | `Main.java` | ⬜ |
| แสดง stats (states explored, time elapsed) | `Main.java` | ⬜ |

---

## Phase 2 — CLI Polish & Testing 🧪

> **เป้าหมาย**: โปรแกรม CLI ใช้งานได้จริง พร้อม edge case handling

| Task | Status |
|---|---|
| Error handling สำหรับ invalid input | ⬜ |
| Timeout mechanism (เช่น 30 วินาที) | ⬜ |
| Memory limit warning | ⬜ |
| เพิ่ม integration tests กับด่านจริง 10+ ด่าน | ⬜ |
| Performance profiling & optimization | ⬜ |
| สร้าง `build.gradle` หรือ `pom.xml` สำหรับ build tool | ⬜ |
| README: เพิ่ม benchmark results | ⬜ |

---

## Phase 3 — Desktop GUI (JavaFX) 🖥️

> **เป้าหมาย**: มี UI ให้ผู้ใช้เห็นหลอดแก้วเป็นภาพ กดปุ่ม Solve ได้

### Architecture

```
┌──────────────────────────────────────────┐
│                JavaFX UI                 │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │ Tube    │  │ Tube    │  │ Tube    │  │
│  │ View    │  │ View    │  │ View    │  │
│  └─────────┘  └─────────┘  └─────────┘  │
│                                          │
│  [🎨 Edit Mode]  [▶️ Solve]  [⏭️ Next]   │
│                                          │
│  Step 3/12: Pour Tube 1 → Tube 4        │
└──────────────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│            Core Logic (Phase 1)          │
│  BoardState → Solver → List<Move>        │
└──────────────────────────────────────────┘
```

### Planned Features

| Feature | Description |
|---|---|
| **Board Editor** | ให้ผู้ใช้คลิกเลือกสีแล้วใส่ลงหลอด |
| **Visual Tubes** | แสดงหลอดแก้วเป็นภาพกราฟิก สีสดใส |
| **Solve Button** | กดแล้วรัน Solver ใน background thread |
| **Step Navigator** | เดินหน้า/ถอยหลังทีละ step ดูการเทน้ำ |
| **Animation** | เมื่อเท → แสดง animation สีไหลจากหลอดหนึ่งไปอีกหลอด |
| **Preset Puzzles** | มีด่านตัวอย่างให้เลือกทดลอง |

### Tasks

| Task | Status |
|---|---|
| Setup JavaFX project module | ⬜ |
| สร้าง `TubeView` component — แสดงหลอดเป็น Rectangle + Color | ⬜ |
| สร้าง `BoardView` — layout หลอดทั้งหมดแบบ grid | ⬜ |
| สร้าง `ColorPicker` panel สำหรับ edit mode | ⬜ |
| สร้าง control bar (Solve / Next / Prev / Reset) | ⬜ |
| เชื่อม Solver กับ UI (run on background thread) | ⬜ |
| Implement step-by-step playback | ⬜ |
| Implement pour animation (TranslateTransition) | ⬜ |
| Light/Dark theme support | ⬜ |
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

| Tool | Purpose |
|---|---|
| **Java 17+** | Main language |
| **JUnit 5** | Unit testing |
| **Gradle** or **Maven** | Build automation (จะเลือกใน Phase 2) |
| **JavaFX 21** | Desktop GUI (Phase 3) |
| **Spring Boot** | Web backend (Phase 4, ถ้าเลือก) |

---

## Risk & Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| BFS memory explosion (ด่าน 14+ หลอด) | High | ใช้ A* + pruning rules ลด state space |
| Heuristic ไม่ admissible | Medium | ทดสอบกับ BFS เทียบว่าได้ optimal เหมือนกัน |
| JavaFX ติดตั้งยากบน user เครื่อง | Medium | พิจารณา Web UI เป็น alternative |
| State string collision (hash) | Low | ใช้ canonical string ที่ unique จริงๆ |
