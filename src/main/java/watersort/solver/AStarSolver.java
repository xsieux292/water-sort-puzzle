package watersort.solver;

import watersort.model.BoardState;
import watersort.model.Move;

import java.util.*;

/**
 * A* Solver — ค้นหาแบบ A-Star ด้วย heuristic
 *
 * เร็วกว่า BFS ในด่านที่มีหลอดเยอะ เพราะใช้ heuristic นำทาง
 * ยังคงรับประกัน optimal solution ถ้า heuristic เป็น admissible
 */
public class AStarSolver implements Solver {

    @Override
    public SolveResult solve(BoardState initialState) {
        // TODO: Implement A* algorithm
        // ดู pseudocode ใน SPEC.md Section 5.2 + 5.3
        throw new UnsupportedOperationException("A* Solver not yet implemented");
    }

    /**
     * Heuristic: นับจำนวน "การเปลี่ยนสี" ในทุกหลอด
     * ยิ่งเปลี่ยนสีบ่อย → ยิ่งต้องเทมาก → ค่ายิ่งสูง
     *
     * @param state สถานะที่จะประเมิน
     * @return estimated remaining cost (admissible)
     */
    int heuristic(BoardState state) {
        // TODO: Implement heuristic function
        // ดู pseudocode ใน SPEC.md Section 5.3
        throw new UnsupportedOperationException("Heuristic not yet implemented");
    }
}
