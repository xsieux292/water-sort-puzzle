package watersort.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * สถานะกระดาน = ชุดของ Tubes ทั้งหมด ณ จังหวะหนึ่ง
 *
 * BoardState ถูกออกแบบให้เป็น immutable-like:
 * - applyMove() จะสร้าง state ใหม่ ไม่แก้ไข state เดิม
 * - implement equals() + hashCode() เพื่อใช้ใน HashSet
 */
public class BoardState {

    private final Tube[] tubes;

    // Cached lazily — BoardState is treated as immutable ตลอดการค้นหา (applyMove สร้าง state ใหม่เสมอ)
    // การ cache ช่วยให้ solver ที่เรียก key/hash ซ้ำ ๆ ต่อ state ไม่ต้องคำนวณใหม่ทุกครั้ง
    private String canonicalKey;
    private int cachedHash;

    public BoardState(Tube[] tubes) {
        this.tubes = tubes;
    }

    /**
     * สร้าง BoardState จาก int[][] (สะดวกสำหรับ test)
     * เช่น new BoardState(new int[][]{ {1,2,3,4}, {5,6,7,8}, {} })
     */
    public static BoardState fromArrays(int[][] tubeArrays) {
        Tube[] tubes = new Tube[tubeArrays.length];
        for (int i = 0; i < tubeArrays.length; i++) {
            tubes[i] = new Tube(tubeArrays[i]);
        }
        return new BoardState(tubes);
    }

    // --- Core Logic ---

    /**
     * ตรวจสอบว่าชนะหรือยัง:
     * ทุกหลอดต้อง ว่างเปล่า หรือ เต็มด้วยสีเดียวกัน
     */
    public boolean isGoal() {
        for (Tube tube : tubes) {
            if (!tube.isEmpty() && !tube.isSorted()) {
                return false;
            }
        }
        return true;
    }

    /**
     * หาทุก move ที่ valid จากสถานะปัจจุบัน
     * รวม pruning rules เพื่อลด state space
     */
    public List<Move> getValidMoves() {
        List<Move> moves = new ArrayList<>();
        boolean usedEmpty = false; // track ว่าเทไปหลอดเปล่าแล้วหรือยัง (identical empties rule)

        for (int src = 0; src < tubes.length; src++) {
            if (tubes[src].isEmpty()) continue;          // RULE 2: source ต้องไม่ว่าง
            if (tubes[src].isSorted()) continue;         // Pruning: skip completed tubes

            for (int dst = 0; dst < tubes.length; dst++) {
                if (src == dst) continue;                // RULE 1: source ≠ destination
                if (tubes[dst].isFull()) continue;       // RULE 3: destination ต้องไม่เต็ม

                if (tubes[dst].isEmpty()) {
                    // Pruning: ถ้า source มีแต่สีเดียว ไม่ต้องเทไปหลอดเปล่า
                    if (tubes[src].isUniform()) continue;

                    // Pruning: หลอดเปล่าทั้งหมดเท่ากัน เทไปหลอดแรกก็พอ
                    if (usedEmpty) continue;
                    usedEmpty = true;

                    moves.add(new Move(src, dst));
                } else {
                    // RULE 4: สีบนสุดต้องตรงกัน
                    if (tubes[dst].topColor() == tubes[src].topColor()) {
                        moves.add(new Move(src, dst));
                    }
                }
            }
            // reset usedEmpty for next source
            usedEmpty = false;
        }

        return moves;
    }

    /**
     * สร้าง BoardState ใหม่หลังจาก apply move
     * ไม่แก้ไข state เดิม (immutable pattern)
     *
     * ใช้ multi-pour: เทหลายบล็อกพร้อมกันถ้าสีเดียวกัน
     */
    public BoardState applyMove(Move move) {
        Tube[] newTubes = new Tube[tubes.length];
        for (int i = 0; i < tubes.length; i++) {
            newTubes[i] = tubes[i].deepCopy();
        }

        Tube src = newTubes[move.source()];
        Tube dst = newTubes[move.destination()];

        // Multi-pour: เททุกบล็อกสีเดียวกันจากบนสุด
        int color = src.topColor();
        while (!src.isEmpty() && src.topColor() == color && !dst.isFull()) {
            src.pop();
            dst.push(color);
        }

        return new BoardState(newTubes);
    }

    // --- Accessors ---

    public int tubeCount() {
        return tubes.length;
    }

    public Tube getTube(int index) {
        return tubes[index];
    }

    // --- Hashing & Equality ---

    /**
     * แปลงสถานะเป็น canonical string สำหรับ state caching
     *
     * ใช้ encoding แบบกระชับ: 1 char ต่อ 1 บล็อกสี + ตัวคั่นต่อหลอด
     * (เดิมใช้ "[1, 2, 3]" ซึ่งยาวและ allocate มากกว่า) ผลลัพธ์ถูก cache ไว้
     * เพราะ solver เรียกซ้ำหลายครั้งต่อ state เดียวกัน
     */
    public String toCanonicalString() {
        String key = canonicalKey;
        if (key == null) {
            StringBuilder sb = new StringBuilder(tubes.length * (Tube.CAPACITY + 1));
            for (Tube tube : tubes) {
                for (int j = 0; j < tube.size(); j++) {
                    sb.append((char) (tube.getColorAt(j) + 1));  // +1 กัน '\0'
                }
                sb.append('/');
            }
            key = canonicalKey = sb.toString();
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardState other)) return false;
        return toCanonicalString().equals(other.toCanonicalString());
    }

    @Override
    public int hashCode() {
        int h = cachedHash;
        if (h == 0) {
            h = toCanonicalString().hashCode();
            cachedHash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Board:\n");
        for (int i = 0; i < tubes.length; i++) {
            sb.append(String.format("  Tube %2d: %s%n", i + 1, tubes[i]));
        }
        return sb.toString();
    }
}
