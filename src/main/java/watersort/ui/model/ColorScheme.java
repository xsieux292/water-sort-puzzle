package watersort.ui.model;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * แผนผังสี: color ID → สี/ชื่อ ที่จะใช้แสดงใน UI
 *
 * ถ้ามาจากการ recognize ภาพ จะใช้ "สีจริงที่วัดได้จากภาพ" เพื่อให้บอร์ดใน UI
 * ตรงกับเกมจริง (ตรวจสอบด้วยตาได้) ถ้าไม่มี (manual/preset) หรือเป็น id ที่ไม่รู้จัก
 * จะ fallback ไปที่พาเลตต์สดใสมาตรฐาน {@link TubeColors}
 */
public final class ColorScheme {

    /** ค่าเริ่มต้น: ใช้พาเลตต์มาตรฐานล้วน */
    public static final ColorScheme DEFAULT = new ColorScheme(Map.of(), Map.of());

    private final Map<Integer, Color> colors;
    private final Map<Integer, String> names;

    public ColorScheme(Map<Integer, Color> colors, Map<Integer, String> names) {
        this.colors = colors;
        this.names = names;
    }

    /** สร้างจากผลการ recognize: rgb (id → {r,g,b}) และชื่อสี (id → name) */
    public static ColorScheme fromRecognition(Map<Integer, int[]> rgb, Map<Integer, String> nameMap) {
        if (rgb == null || rgb.isEmpty()) {
            return DEFAULT;
        }
        Map<Integer, Color> colors = new HashMap<>();
        for (Map.Entry<Integer, int[]> e : rgb.entrySet()) {
            int[] c = e.getValue();
            colors.put(e.getKey(), Color.rgb(clamp(c[0]), clamp(c[1]), clamp(c[2])));
        }
        return new ColorScheme(colors, nameMap == null ? Map.of() : nameMap);
    }

    public Color color(int id) {
        Color c = colors.get(id);
        return c != null ? c : TubeColors.color(id);
    }

    public String name(int id) {
        String n = names.get(id);
        return n != null ? n : TubeColors.name(id);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
