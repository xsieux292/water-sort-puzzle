package watersort.ui.model;

import watersort.model.BoardState;
import watersort.model.Tube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * กระดานที่แก้ไขได้ (mutable) สำหรับ Edit mode
 *
 * หลอดยังคงเป็น stack: สีต้องเรียงติดกันจากก้นหลอดเสมอ ไม่มี "ช่องว่างตรงกลาง"
 */
public class BoardEditor {

    public static final int MIN_TUBES = 2;
    public static final int MAX_TUBES = 28;

    private final List<List<Integer>> tubes = new ArrayList<>();

    public BoardEditor(BoardState board) {
        for (int i = 0; i < board.tubeCount(); i++) {
            Tube t = board.getTube(i);
            List<Integer> colors = new ArrayList<>();
            for (int j = 0; j < t.size(); j++) {
                colors.add(t.getColorAt(j));
            }
            tubes.add(colors);
        }
    }

    public static BoardEditor blank(int tubeCount) {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[0][]));
        for (int i = 0; i < tubeCount; i++) {
            editor.tubes.add(new ArrayList<>());
        }
        return editor;
    }

    public int tubeCount() {
        return tubes.size();
    }

    public List<Integer> tube(int index) {
        return Collections.unmodifiableList(tubes.get(index));
    }

    /**
     * ใส่สีที่ช่อง slot: ถ้า slot มีสีอยู่แล้ว → แทนที่, ถ้าเป็นช่องว่าง → วางต่อบนสุด
     *
     * @return true ถ้ากระดานเปลี่ยน
     */
    public boolean paint(int tubeIndex, int slot, int colorId) {
        List<Integer> tube = tubes.get(tubeIndex);
        if (slot < tube.size()) {
            if (tube.get(slot) == colorId) {
                return false;
            }
            tube.set(slot, colorId);
            return true;
        }
        if (tube.size() >= Tube.CAPACITY) {
            return false;
        }
        tube.add(colorId);
        return true;
    }

    /** ลบสีที่ช่อง slot (สีที่อยู่เหนือขึ้นไปจะเลื่อนลงมา) */
    public boolean erase(int tubeIndex, int slot) {
        List<Integer> tube = tubes.get(tubeIndex);
        if (slot < 0 || slot >= tube.size()) {
            return false;
        }
        tube.remove(slot);
        return true;
    }

    public boolean addTube() {
        if (tubes.size() >= MAX_TUBES) {
            return false;
        }
        tubes.add(new ArrayList<>());
        return true;
    }

    /** ลบหลอดสุดท้าย (สีที่อยู่ในหลอดจะหายไปด้วย) */
    public boolean removeLastTube() {
        if (tubes.size() <= MIN_TUBES) {
            return false;
        }
        tubes.remove(tubes.size() - 1);
        return true;
    }

    public int maxColorId() {
        int max = 0;
        for (List<Integer> tube : tubes) {
            for (int c : tube) {
                max = Math.max(max, c);
            }
        }
        return max;
    }

    public BoardState toBoardState() {
        int[][] arrays = new int[tubes.size()][];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = tubes.get(i).stream().mapToInt(Integer::intValue).toArray();
        }
        return BoardState.fromArrays(arrays);
    }

    public Validation validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<Integer, Integer> counts = new TreeMap<>();
        int emptyTubes = 0;

        for (List<Integer> tube : tubes) {
            if (tube.isEmpty()) {
                emptyTubes++;
            }
            for (int c : tube) {
                counts.merge(c, 1, Integer::sum);
            }
        }

        Set<Integer> badColors = new LinkedHashSet<>();
        if (counts.isEmpty()) {
            errors.add("The board is empty — paint some colors first.");
        }
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            if (e.getValue() != Tube.CAPACITY) {
                badColors.add(e.getKey());
                errors.add(String.format("%s has %d block%s (needs %d).",
                        TubeColors.name(e.getKey()), e.getValue(), e.getValue() == 1 ? "" : "s", Tube.CAPACITY));
            }
        }
        if (!counts.isEmpty() && emptyTubes < 2) {
            warnings.add(String.format("Only %d empty tube%s — the puzzle may be unsolvable.",
                    emptyTubes, emptyTubes == 1 ? "" : "s"));
        }

        Set<Integer> suspects = new LinkedHashSet<>();
        for (int i = 0; i < tubes.size(); i++) {
            for (int c : tubes.get(i)) {
                if (badColors.contains(c)) {
                    suspects.add(i);
                    break;
                }
            }
        }
        return new Validation(errors, warnings, counts, suspects);
    }
}
