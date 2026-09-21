package watersort.solver;

import watersort.model.BoardState;
import watersort.model.Move;

import java.util.*;

/**
 * BFS Solver — ค้นหาแบบกว้าง (Breadth-First Search)
 *
 * รับประกันว่าจะเจอคำตอบที่ใช้จำนวน move น้อยที่สุด (shortest path)
 * ใช้ HashSet สำหรับ state caching เพื่อป้องกันการทำงานวนลูป
 */
public class BFSSolver implements Solver {

    @Override
    public SolveResult solve(BoardState initialState) {
        // TODO: Implement BFS algorithm
        // ดู pseudocode ใน SPEC.md Section 5.1
        throw new UnsupportedOperationException("BFS Solver not yet implemented");
    }
}
