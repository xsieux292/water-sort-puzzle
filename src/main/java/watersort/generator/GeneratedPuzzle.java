package watersort.generator;

import watersort.model.BoardState;
import watersort.model.Move;

import java.util.Collections;
import java.util.List;

/**
 * โจทย์ที่สร้างจาก PuzzleGenerator
 *
 * ประกอบด้วย:
 * - BoardState: สถานะเริ่มต้นของโจทย์
 * - จำนวนสี, จำนวนหลอดว่าง
 * - Solution: ลำดับ moves ที่สั้นที่สุด (ถ้า verify)
 * - สถิติ
 */
public class GeneratedPuzzle {

    private final BoardState board;
    private final int numColors;
    private final int emptyTubes;
    private final List<Move> solution;      // null ถ้าไม่ verify
    private final int optimalSteps;          // -1 ถ้าไม่ verify
    private final int statesExplored;

    public GeneratedPuzzle(BoardState board, int numColors, int emptyTubes,
                           List<Move> solution, int optimalSteps, int statesExplored) {
        this.board = board;
        this.numColors = numColors;
        this.emptyTubes = emptyTubes;
        this.solution = solution != null ? Collections.unmodifiableList(solution) : null;
        this.optimalSteps = optimalSteps;
        this.statesExplored = statesExplored;
    }

    // --- Getters ---

    public BoardState getBoard() {
        return board;
    }

    public int getNumColors() {
        return numColors;
    }

    public int getEmptyTubes() {
        return emptyTubes;
    }

    public int getTotalTubes() {
        return numColors + emptyTubes;
    }

    /**
     * @return ลำดับ moves ที่สั้นที่สุด (optimal solution), null ถ้าไม่ได้ verify
     */
    public List<Move> getSolution() {
        return solution;
    }

    /**
     * @return จำนวน steps ที่สั้นที่สุด, -1 ถ้าไม่ได้ verify
     */
    public int getOptimalSteps() {
        return optimalSteps;
    }

    public int getStatesExplored() {
        return statesExplored;
    }

    /**
     * @return true ถ้ามี solution (ถ้า verify ไว้)
     */
    public boolean hasSolution() {
        return solution != null && !solution.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🧩 Generated Puzzle: %d colors, %d tubes (%d empty)%n",
                numColors, getTotalTubes(), emptyTubes));
        sb.append(board.toString());
        if (optimalSteps > 0) {
            sb.append(String.format("%n🎯 Optimal solution: %d steps", optimalSteps));
            sb.append(String.format("%n📊 States explored: %,d", statesExplored));
        }
        return sb.toString();
    }

    /**
     * แสดง solution step-by-step
     */
    public String solutionToString() {
        if (solution == null || solution.isEmpty()) {
            return "No solution available (verify was disabled)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("✅ Solution (%d steps):%n", solution.size()));
        int step = 1;
        for (Move move : solution) {
            sb.append(String.format("  Step %2d: %s%n", step++, move));
        }
        return sb.toString();
    }
}
