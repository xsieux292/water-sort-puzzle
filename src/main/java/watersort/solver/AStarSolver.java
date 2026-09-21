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

    private static final int MAX_STATES = 2_000_000;

    @Override
    public SolveResult solve(BoardState initialState) {
        long startTime = System.currentTimeMillis();

        if (initialState.isGoal()) {
            long elapsed = System.currentTimeMillis() - startTime;
            return SolveResult.success(Collections.emptyList(), 1, elapsed);
        }

        // PriorityQueue sorted by f(n) = g(n) + h(n)
        PriorityQueue<SearchNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<String, Integer> gScore = new HashMap<>();

        String initialKey = initialState.toCanonicalString();
        pq.add(new SearchNode(initialState, new ArrayList<>(), 0));
        gScore.put(initialKey, 0);

        int statesExplored = 0;

        while (!pq.isEmpty()) {
            if (statesExplored >= MAX_STATES) {
                long elapsed = System.currentTimeMillis() - startTime;
                return SolveResult.failure(statesExplored, elapsed);
            }

            SearchNode node = pq.poll();
            
            // If we found a shorter path to this state after this node was enqueued, skip it
            String currentKey = node.state.toCanonicalString();
            if (node.g > gScore.getOrDefault(currentKey, Integer.MAX_VALUE)) {
                continue;
            }
            
            statesExplored++;

            // We check goal when expanding in A*
            if (node.state.isGoal()) {
                long elapsed = System.currentTimeMillis() - startTime;
                return SolveResult.success(node.moveHistory, statesExplored, elapsed);
            }

            List<Move> validMoves = node.state.getValidMoves();

            for (Move move : validMoves) {
                BoardState newState = node.state.applyMove(move);
                String stateKey = newState.toCanonicalString();
                
                int tentativeG = node.g + 1;

                if (tentativeG < gScore.getOrDefault(stateKey, Integer.MAX_VALUE)) {
                    gScore.put(stateKey, tentativeG);
                    
                    List<Move> newHistory = new ArrayList<>(node.moveHistory);
                    newHistory.add(move);
                    
                    pq.add(new SearchNode(newState, newHistory, tentativeG));
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return SolveResult.failure(statesExplored, elapsed);
    }

    /**
     * Heuristic: นับจำนวน "การเปลี่ยนสี" ในทุกหลอด
     * ยิ่งเปลี่ยนสีบ่อย → ยิ่งต้องเทมาก → ค่ายิ่งสูง
     *
     * @param state สถานะที่จะประเมิน
     * @return estimated remaining cost (admissible)
     */
    int heuristic(BoardState state) {
        int h = 0;
        for (int i = 0; i < state.tubeCount(); i++) {
            var tube = state.getTube(i);
            if (tube.isEmpty()) continue;

            // นับจำนวนรอยต่อที่สีเปลี่ยน
            int colorChanges = 0;
            int lastColor = tube.getColorAt(0);
            for (int j = 1; j < tube.size(); j++) {
                int currentColor = tube.getColorAt(j);
                if (currentColor != lastColor) {
                    colorChanges++;
                    lastColor = currentColor;
                }
            }
            h += colorChanges;
        }
        return h;
    }

    private class SearchNode {
        final BoardState state;
        final List<Move> moveHistory;
        final int g; // cost so far (number of moves)
        final int h; // heuristic (estimated remaining cost)
        final int f; // total estimated cost: g + h

        SearchNode(BoardState state, List<Move> moveHistory, int g) {
            this.state = state;
            this.moveHistory = moveHistory;
            this.g = g;
            this.h = heuristic(state);
            this.f = this.g + this.h;
        }
    }
}
