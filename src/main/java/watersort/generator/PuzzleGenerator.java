package watersort.generator;

import watersort.model.BoardState;
import watersort.model.Move;
import watersort.model.Tube;
import watersort.solver.BFSSolver;
import watersort.solver.SolveResult;

import java.util.*;

/**
 * สร้างโจทย์ Water Sort Puzzle ที่ **รับประกันว่ามีคำตอบ 100%**
 *
 * หลักการ: Random Shuffle + BFS Verify
 * ──────────────────────────────────────
 * 1. สร้างชุดสี: แต่ละสีมี 4 บล็อก → รวม numColors × 4 บล็อก
 * 2. สุ่มสลับตำแหน่งทั้งหมด (Fisher-Yates shuffle)
 * 3. เติมลงหลอด ทีละ 4 บล็อก (เหลือหลอดว่าง)
 * 4. ใช้ BFS Solver verify ว่ามีคำตอบ → ถ้าไม่มี → สุ่มใหม่
 * 5. การันตี: เกือบทุก random arrangement ที่มี ≥2 หลอดว่างจะ solvable
 *    (อัตราผ่านสูงมาก ~90%+ → ไม่ต้อง retry หลายรอบ)
 *
 * Usage:
 *   PuzzleGenerator gen = new PuzzleGenerator();
 *   GeneratedPuzzle puzzle = gen.generate(12);  // 12 สี (14 หลอด)
 *   System.out.println(puzzle.board());
 *   System.out.println("Optimal: " + puzzle.optimalSteps() + " steps");
 */
public class PuzzleGenerator {

    private final Random random;

    public PuzzleGenerator() {
        this.random = new Random();
    }

    public PuzzleGenerator(boolean verify) {
        this(); // verify parameter kept for API compat, always verifies
    }

    /**
     * สร้างโจทย์ใหม่
     *
     * @param numColors จำนวนสี (เช่น 5 → 7 หลอด = 5 หลอดสี + 2 หลอดว่าง)
     * @return โจทย์ที่สร้างพร้อม solution
     */
    public GeneratedPuzzle generate(int numColors) {
        return generate(numColors, 2);
    }

    /**
     * สร้างโจทย์ใหม่
     *
     * @param numColors   จำนวนสี
     * @param emptyTubes  จำนวนหลอดว่าง (ค่าเริ่มต้น 2)
     * @return โจทย์ที่การันตี solvable + optimal solution
     */
    public GeneratedPuzzle generate(int numColors, int emptyTubes) {
        if (numColors < 2) {
            throw new IllegalArgumentException("ต้องมีอย่างน้อย 2 สี");
        }
        if (emptyTubes < 1) {
            throw new IllegalArgumentException("ต้องมีหลอดว่างอย่างน้อย 1 หลอด");
        }

        int maxAttempts = 200;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            BoardState board = createRandomBoard(numColors, emptyTubes);

            // ข้ามถ้าบังเอิญเป็น goal
            if (board.isGoal()) continue;

            // ข้ามโจทย์ที่น่าเบื่อ (มีหลอดที่ sorted ครบเยอะ)
            if (countSortedTubes(board) > numColors / 3) continue;

            // Verify ด้วย BFS
            BFSSolver solver = new BFSSolver();
            SolveResult result = solver.solve(board);

            if (result.isSolved() && result.getStepCount() >= 2) {
                return new GeneratedPuzzle(
                        board, numColors, emptyTubes,
                        result.getMoves(), result.getStepCount(),
                        result.getStatesExplored()
                );
            }
        }

        throw new RuntimeException("ไม่สามารถ generate โจทย์ที่ solvable ได้ภายใน " + maxAttempts + " ครั้ง");
    }

    /**
     * สร้างโจทย์โดยกำหนดความยาก
     *
     * @param numColors  จำนวนสี
     * @param difficulty EASY / MEDIUM / HARD
     */
    public GeneratedPuzzle generate(int numColors, Difficulty difficulty) {
        int emptyTubes = switch (difficulty) {
            case EASY -> 3;   // หลอดว่างเยอะ → ง่ายกว่า
            case MEDIUM -> 2; // ค่ามาตรฐาน
            case HARD -> 2;   // หลอดเท่ากันแต่ต้องการ step เยอะขึ้น
        };

        int minSteps = switch (difficulty) {
            case EASY -> 2;
            case MEDIUM -> Math.max(3, numColors);
            case HARD -> Math.max(5, numColors * 2);
        };

        int maxAttempts = 200;
        GeneratedPuzzle best = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            BoardState board = createRandomBoard(numColors, emptyTubes);
            if (board.isGoal()) continue;
            if (countSortedTubes(board) > numColors / 3) continue;

            BFSSolver solver = new BFSSolver();
            SolveResult result = solver.solve(board);

            if (result.isSolved() && result.getStepCount() >= 2) {
                GeneratedPuzzle puzzle = new GeneratedPuzzle(
                        board, numColors, emptyTubes,
                        result.getMoves(), result.getStepCount(),
                        result.getStatesExplored()
                );

                // เก็บ best
                if (best == null || result.getStepCount() > best.getOptimalSteps()) {
                    best = puzzle;
                }

                // ถ้า step count มากพอ → return
                if (result.getStepCount() >= minSteps) {
                    return puzzle;
                }
            }
        }

        // Return best ที่หาได้
        if (best != null) return best;

        // Absolute fallback
        return generate(numColors, emptyTubes);
    }

    // ─── Internal Methods ──────────────────────────────────────────

    /**
     * สร้าง board สุ่มโดย:
     * 1. สร้าง pool ของสี (แต่ละสี 4 บล็อก)
     * 2. Fisher-Yates shuffle
     * 3. เติมลงหลอดทีละ 4 บล็อก
     */
    private BoardState createRandomBoard(int numColors, int emptyTubes) {
        int totalColorBlocks = numColors * Tube.CAPACITY;
        int totalTubes = numColors + emptyTubes;

        // สร้าง pool ของสี
        int[] pool = new int[totalColorBlocks];
        int idx = 0;
        for (int color = 1; color <= numColors; color++) {
            for (int j = 0; j < Tube.CAPACITY; j++) {
                pool[idx++] = color;
            }
        }

        // Fisher-Yates shuffle
        for (int i = pool.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = pool[i];
            pool[i] = pool[j];
            pool[j] = tmp;
        }

        // เติมลงหลอด
        int[][] tubeArrays = new int[totalTubes][];
        for (int t = 0; t < numColors; t++) {
            tubeArrays[t] = new int[Tube.CAPACITY];
            System.arraycopy(pool, t * Tube.CAPACITY, tubeArrays[t], 0, Tube.CAPACITY);
        }
        for (int t = numColors; t < totalTubes; t++) {
            tubeArrays[t] = new int[]{};
        }

        return BoardState.fromArrays(tubeArrays);
    }

    /**
     * นับจำนวนหลอดที่ sorted เรียบร้อยแล้ว
     */
    private int countSortedTubes(BoardState board) {
        int count = 0;
        for (int i = 0; i < board.tubeCount(); i++) {
            if (board.getTube(i).isSorted()) count++;
        }
        return count;
    }

    // ─── Difficulty Enum ───────────────────────────────────────────

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
}
