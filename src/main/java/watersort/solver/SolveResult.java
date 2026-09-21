package watersort.solver;

import watersort.model.BoardState;
import watersort.model.Move;

import java.util.List;

/**
 * ผลลัพธ์จากการ solve ประกอบด้วย:
 * - ลำดับ moves (ถ้าแก้ได้)
 * - สถิติการค้นหา
 */
public class SolveResult {

    private final List<Move> moves;       // null ถ้าแก้ไม่ได้
    private final boolean solved;
    private final int statesExplored;
    private final long timeMillis;

    public SolveResult(List<Move> moves, boolean solved, int statesExplored, long timeMillis) {
        this.moves = moves;
        this.solved = solved;
        this.statesExplored = statesExplored;
        this.timeMillis = timeMillis;
    }

    public static SolveResult success(List<Move> moves, int statesExplored, long timeMillis) {
        return new SolveResult(moves, true, statesExplored, timeMillis);
    }

    public static SolveResult failure(int statesExplored, long timeMillis) {
        return new SolveResult(null, false, statesExplored, timeMillis);
    }

    // --- Getters ---

    public List<Move> getMoves() {
        return moves;
    }

    public boolean isSolved() {
        return solved;
    }

    public int getStatesExplored() {
        return statesExplored;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public int getStepCount() {
        return moves != null ? moves.size() : 0;
    }

    @Override
    public String toString() {
        if (solved) {
            return String.format("✅ Solved in %d steps | States: %,d | Time: %dms",
                    getStepCount(), statesExplored, timeMillis);
        } else {
            return String.format("❌ No solution | States: %,d | Time: %dms",
                    statesExplored, timeMillis);
        }
    }
}
