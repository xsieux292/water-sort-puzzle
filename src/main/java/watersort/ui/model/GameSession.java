package watersort.ui.model;

import watersort.model.BoardState;
import watersort.model.Move;
import watersort.model.Tube;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * เกมที่ผู้เล่นเล่นเอง (โหมดเล่นเกม) — เก็บสถานะปัจจุบัน ประวัติสำหรับ Undo และจำนวน move
 *
 * กติกาการเทตรงตาม SPEC: ต้นทางไม่ว่าง, ปลายทางไม่เต็ม, ปลายทางว่างหรือสีบนสุดตรงกัน
 * (ต่างจาก {@link BoardState#getValidMoves()} ที่ตัด move ที่ "ไม่มีประโยชน์" ออกเพื่อให้ solver เร็วขึ้น
 * ผู้เล่นสามารถเทแบบไม่มีประโยชน์ได้ ตามกติกาจริงของเกม)
 */
public class GameSession {

    /** การเท 1 ครั้ง: count บล็อกสี color จากหลอด from → to */
    public record Pour(int from, int to, int count, int color) {
    }

    private final BoardState initial;
    private final int optimalSteps;
    private final Deque<BoardState> history = new ArrayDeque<>();
    private final Deque<Pour> pours = new ArrayDeque<>();
    private BoardState current;

    /**
     * @param initial      กระดานเริ่มต้น
     * @param optimalSteps จำนวน step ที่น้อยที่สุด (par) — ใช้แสดงเทียบกับที่ผู้เล่นทำ
     */
    public GameSession(BoardState initial, int optimalSteps) {
        this.initial = initial;
        this.optimalSteps = optimalSteps;
        this.current = initial;
    }

    public static boolean canPour(BoardState state, int from, int to) {
        if (from == to || from < 0 || to < 0 || from >= state.tubeCount() || to >= state.tubeCount()) {
            return false;
        }
        Tube src = state.getTube(from);
        Tube dst = state.getTube(to);
        if (src.isEmpty() || dst.isFull()) {
            return false;
        }
        return dst.isEmpty() || dst.topColor() == src.topColor();
    }

    public boolean canPour(int from, int to) {
        return canPour(current, from, to);
    }

    /** เท from → to ถ้าทำได้ (คืนรายละเอียดการเท) มิฉะนั้นคืน null และไม่เปลี่ยนสถานะ */
    public Pour pour(int from, int to) {
        if (!canPour(current, from, to)) {
            return null;
        }
        Tube src = current.getTube(from);
        int color = src.topColor();
        int before = src.size();
        BoardState next = current.applyMove(new Move(from, to));
        Pour pour = new Pour(from, to, before - next.getTube(from).size(), color);
        history.push(current);
        pours.push(pour);
        current = next;
        return pour;
    }

    public boolean canUndo() {
        return !pours.isEmpty();
    }

    /** ย้อน 1 ครั้ง — คืน Pour ที่ถูกยกเลิก (null ถ้าไม่มีอะไรให้ย้อน) */
    public Pour undo() {
        if (pours.isEmpty()) {
            return null;
        }
        current = history.pop();
        return pours.pop();
    }

    public void restart() {
        history.clear();
        pours.clear();
        current = initial;
    }

    public BoardState state() {
        return current;
    }

    public int moves() {
        return pours.size();
    }

    public int optimalSteps() {
        return optimalSteps;
    }

    public boolean isWon() {
        return current.isGoal();
    }
}
