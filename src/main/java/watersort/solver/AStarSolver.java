package watersort.solver;

import watersort.model.BoardState;
import watersort.model.Move;
import watersort.model.Tube;

import java.util.*;

/**
 * A* Solver — ค้นหาแบบ A-Star ด้วย heuristic
 *
 * เร็วกว่า BFS ในด่านที่มีหลอดเยอะ เพราะใช้ heuristic นำทาง
 * ยังคงรับประกัน optimal solution ตราบใดที่ heuristic เป็น admissible
 *
 * <p>Performance notes:
 * <ul>
 *   <li><b>Parent-pointer nodes</b> — แต่ละ node เก็บแค่ move + ตัวชี้ไป parent
 *       แล้ว reconstruct เส้นทางตอนเจอ goal จึงไม่ต้อง copy ทั้ง path ทุก node
 *       (เดิมเป็น O(depth) ต่อ node → O(states·depth) รวม ทั้งเวลาและหน่วยความจำ)</li>
 *   <li><b>Cached state key</b> — ใช้ {@link BoardState#toCanonicalString()} ที่ cache ผลไว้
 *       จึงไม่ต้อง encode state ซ้ำเวลาเทียบ gScore</li>
 *   <li><b>Tie-breaking ด้วย h</b> — เมื่อ f เท่ากัน เลือก node ที่ h น้อยกว่า (ใกล้ goal กว่า)
 *       ช่วยให้เจอคำตอบเร็วขึ้นโดยยังคง optimal</li>
 * </ul>
 */
public class AStarSolver implements Solver {

    private static final int MAX_STATES = 2_000_000;

    @Override
    public SolveResult solve(BoardState initialState) {
        long startTime = System.currentTimeMillis();

        if (initialState.isGoal()) {
            return SolveResult.success(Collections.emptyList(), 1, elapsed(startTime));
        }

        // f น้อยก่อน; ถ้า f เท่ากันเลือก h น้อยก่อน (ใกล้ goal) → ลด node ที่ต้องขยาย
        PriorityQueue<SearchNode> pq = new PriorityQueue<>(
                Comparator.comparingInt((SearchNode n) -> n.f).thenComparingInt(n -> n.h));
        // gScore: ต้นทุนที่ดีที่สุดที่รู้จนถึง state นั้น (key = canonical string ที่ cache ไว้)
        Map<String, Integer> gScore = new HashMap<>();

        pq.add(new SearchNode(initialState, null, null, 0));
        gScore.put(initialState.toCanonicalString(), 0);

        int statesExplored = 0;

        while (!pq.isEmpty()) {
            if (statesExplored >= MAX_STATES) {
                return SolveResult.failure(statesExplored, elapsed(startTime));
            }

            SearchNode node = pq.poll();

            // ถ้าเจอเส้นทางที่สั้นกว่ามาถึง state นี้แล้วหลังจาก node ถูก enqueue → ข้าม (stale entry)
            if (node.g > gScore.getOrDefault(node.state.toCanonicalString(), Integer.MAX_VALUE)) {
                continue;
            }

            statesExplored++;

            if (node.state.isGoal()) {
                return SolveResult.success(reconstruct(node), statesExplored, elapsed(startTime));
            }

            for (Move move : node.state.getValidMoves()) {
                BoardState newState = node.state.applyMove(move);
                String stateKey = newState.toCanonicalString();
                int tentativeG = node.g + 1;

                if (tentativeG < gScore.getOrDefault(stateKey, Integer.MAX_VALUE)) {
                    gScore.put(stateKey, tentativeG);
                    pq.add(new SearchNode(newState, node, move, tentativeG));
                }
            }
        }

        return SolveResult.failure(statesExplored, elapsed(startTime));
    }

    /** ไล่ตัวชี้ parent จาก goal กลับไปต้นทาง แล้วกลับลำดับให้เป็นต้นทาง → goal */
    private List<Move> reconstruct(SearchNode goal) {
        ArrayDeque<Move> stack = new ArrayDeque<>();
        for (SearchNode n = goal; n.move != null; n = n.parent) {
            stack.push(n.move);
        }
        return new ArrayList<>(stack);
    }

    /**
     * Heuristic (admissible) = ค่ามากสุดของ 2 lower bound:
     *
     * <ol>
     *   <li><b>h1 — จำนวนรอยต่อที่สีเปลี่ยน</b> รวมทุกหลอด: แต่ละ pour ลดค่านี้ได้ ≤ 1
     *       เพราะย้ายบล็อกสีเดียวกัน 1 กลุ่มออกได้ครั้งละ 1 รอยต่อ</li>
     *   <li><b>h2 — จำนวนหลอดที่ต้อง "รวมสี"</b> = Σ_สี (จำนวนหลอดที่มีสีนั้น − 1):
     *       การรวมสีที่กระจายอยู่ k หลอดให้เหลือหลอดเดียว ต้อง pour ≥ k−1 ครั้ง
     *       และแต่ละ pour ลดค่านี้ได้ ≤ 1</li>
     * </ol>
     *
     * ทั้งคู่เป็น lower bound ที่ถูกต้อง → max ของทั้งคู่ก็ยัง admissible
     * และมักแน่นกว่า h1 เดี่ยว ๆ มากในสถานะที่สีกระจายหลายหลอด (เช่นตอนเริ่มด่านใหญ่)
     */
    int heuristic(BoardState state) {
        int h1 = 0;
        // นับจำนวนหลอดที่แต่ละสีปรากฏ (index = colorId)
        int[] tubesWithColor = null;
        boolean[] seenInThisTube = null;
        int maxColor = 0;

        for (int i = 0; i < state.tubeCount(); i++) {
            Tube tube = state.getTube(i);
            for (int j = 0; j < tube.size(); j++) {
                maxColor = Math.max(maxColor, tube.getColorAt(j));
            }
        }
        tubesWithColor = new int[maxColor + 1];
        seenInThisTube = new boolean[maxColor + 1];

        for (int i = 0; i < state.tubeCount(); i++) {
            Tube tube = state.getTube(i);
            if (tube.isEmpty()) continue;

            Arrays.fill(seenInThisTube, false);
            int lastColor = tube.getColorAt(0);
            seenInThisTube[lastColor] = true;
            for (int j = 1; j < tube.size(); j++) {
                int c = tube.getColorAt(j);
                if (c != lastColor) {
                    h1++;
                    lastColor = c;
                }
                seenInThisTube[c] = true;
            }
            for (int c = 1; c <= maxColor; c++) {
                if (seenInThisTube[c]) tubesWithColor[c]++;
            }
        }

        int h2 = 0;
        for (int c = 1; c <= maxColor; c++) {
            if (tubesWithColor[c] > 1) h2 += tubesWithColor[c] - 1;
        }

        return Math.max(h1, h2);
    }

    private static long elapsed(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    /** Node แบบ parent-pointer: เก็บแค่ move ที่พามาถึง + ตัวชี้ parent (ไม่ copy ทั้ง path) */
    private final class SearchNode {
        final BoardState state;
        final SearchNode parent;
        final Move move;   // move ที่ทำจาก parent มาถึง state นี้ (null = ราก)
        final int g;       // cost so far (จำนวน moves)
        final int h;       // heuristic
        final int f;       // g + h

        SearchNode(BoardState state, SearchNode parent, Move move, int g) {
            this.state = state;
            this.parent = parent;
            this.move = move;
            this.g = g;
            this.h = heuristic(state);
            this.f = g + h;
        }
    }
}
