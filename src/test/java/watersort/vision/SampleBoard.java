package watersort.vision;

import watersort.model.BoardState;
import watersort.model.Tube;

import java.util.HashMap;
import java.util.Map;

/**
 * Ground truth ของ test_image.png (อ่านจากภาพโดยตรง) — ก้นหลอด → บนสุด
 *
 * P=purple L=light-blue R=red G=light-green N=brown Z=gray U=navy
 * K=pink D=dark-green Y=yellow O=orange V=olive
 */
final class SampleBoard {

    static final String[] TUBES = {
            "PLRG", "NZUP", "KDDG", "LKKZ", "YOGV", "OUND", "VZON",
            "VZNU", "YPRV", "YOLD", "YURK", "GLRP", "", ""
    };

    /** ตัวอักษรย่อ → ชื่อสีที่ ColorNamer ควรตั้งให้ (ตรวจแล้วด้วยตากับภาพ) */
    static final Map<Character, String> NAMES = Map.ofEntries(
            Map.entry('P', "Purple"), Map.entry('L', "Light Blue"), Map.entry('R', "Red"),
            Map.entry('G', "Light Green"), Map.entry('N', "Brown"), Map.entry('Z', "Gray"),
            Map.entry('U', "Blue"), Map.entry('K', "Pink"), Map.entry('D', "Dark Green"),
            Map.entry('Y', "Yellow"), Map.entry('O', "Orange"), Map.entry('V', "Olive"));

    private SampleBoard() {
    }

    /** ชื่อสีของทุกบล็อก (ก้น→บน) ตาม ground truth เช่น "Purple,Light Blue,Red,Light Green|..." */
    static String namedBoard(BoardState board, Map<Integer, String> colorMap) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.tubeCount(); i++) {
            Tube t = board.getTube(i);
            for (int j = 0; j < t.size(); j++) {
                if (j > 0) sb.append(',');
                sb.append(colorMap.get(t.getColorAt(j)));
            }
            sb.append('|');
        }
        return sb.toString();
    }

    static String expectedNamedBoard() {
        StringBuilder sb = new StringBuilder();
        for (String tube : TUBES) {
            for (int j = 0; j < tube.length(); j++) {
                if (j > 0) sb.append(',');
                sb.append(NAMES.get(tube.charAt(j)));
            }
            sb.append('|');
        }
        return sb.toString();
    }

    /** แปลงบอร์ดเป็นรูปแบบ "ลำดับการปรากฏครั้งแรก" เพื่อเทียบโดยไม่ขึ้นกับ color ID ที่ recognizer ตั้งให้ */
    static String canonical(BoardState board) {
        Map<Integer, Character> ids = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.tubeCount(); i++) {
            Tube t = board.getTube(i);
            for (int j = 0; j < t.size(); j++) {
                sb.append(ids.computeIfAbsent(t.getColorAt(j), k -> (char) ('a' + ids.size())));
            }
            sb.append('|');
        }
        return sb.toString();
    }

    static String canonical(String[] tubes) {
        Map<Character, Character> ids = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (String tube : tubes) {
            for (char c : tube.toCharArray()) {
                sb.append(ids.computeIfAbsent(c, k -> (char) ('a' + ids.size())));
            }
            sb.append('|');
        }
        return sb.toString();
    }
}
