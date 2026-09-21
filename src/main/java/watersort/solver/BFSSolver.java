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

    /** จำนวน state สูงสุดที่จะสำรวจ (ป้องกัน OOM) */
    private static final int MAX_STATES = 2_000_000;

    @Override
    public SolveResult solve(BoardState initialState) {
        long startTime = System.currentTimeMillis();

        // เช็คก่อนว่าชนะแล้วหรือยัง
        if (initialState.isGoal()) {
            long elapsed = System.currentTimeMillis() - startTime;
            return SolveResult.success(Collections.emptyList(), 1, elapsed);
        }

        // BFS Queue: เก็บ [state, moveHistory]
        Queue<SearchNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        String initialKey = initialState.toCanonicalString();
        queue.add(new SearchNode(initialState, new ArrayList<>()));
        visited.add(initialKey);

        int statesExplored = 0;

        while (!queue.isEmpty()) {
            if (statesExplored >= MAX_STATES) {
                long elapsed = System.currentTimeMillis() - startTime;
                return SolveResult.failure(statesExplored, elapsed);
            }

            SearchNode node = queue.poll();
            statesExplored++;

            // หา valid moves ทั้งหมดจาก state ปัจจุบัน
            List<Move> validMoves = node.state.getValidMoves();

            for (Move move : validMoves) {
                BoardState newState = node.state.applyMove(move);

                // เช็ค goal
                if (newState.isGoal()) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    List<Move> solution = new ArrayList<>(node.moveHistory);
                    solution.add(move);
                    return SolveResult.success(solution, statesExplored, elapsed);
                }

                // เช็ค visited
                String stateKey = newState.toCanonicalString();
                if (!visited.contains(stateKey)) {
                    visited.add(stateKey);
                    List<Move> newHistory = new ArrayList<>(node.moveHistory);
                    newHistory.add(move);
                    queue.add(new SearchNode(newState, newHistory));
                }
            }
        }

        // ค้นหาหมดแล้ว ไม่เจอคำตอบ
        long elapsed = System.currentTimeMillis() - startTime;
        return SolveResult.failure(statesExplored, elapsed);
    }

    /**
     * Node สำหรับ BFS Queue
     */
    private static class SearchNode {
        final BoardState state;
        final List<Move> moveHistory;

        SearchNode(BoardState state, List<Move> moveHistory) {
            this.state = state;
            this.moveHistory = moveHistory;
        }
    }
}
