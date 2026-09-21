package watersort.ui.model;

import javafx.scene.paint.Color;

/**
 * แปลง color ID (1-based, ตามที่ Solver/Vision ใช้) เป็นสีสดใสสำหรับแสดงผล
 * พาเลตต์ถูกเลือกให้แยกออกจากกันง่ายบนพื้นหลังทั้งโทนมืดและสว่าง
 */
public final class TubeColors {

    public static final int MAX_COLORS = 24;

    private static final String[] NAMES = {
            "Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Cyan", "Pink", "Brown", "Lime",
            "Teal", "Gray", "Navy", "Maroon", "Sky", "Mint", "Peach", "Indigo", "Olive", "White"
    };

    private static final String[] HEX = {
            "#FF3B30", "#2979FF", "#22C55E", "#FFD60A", "#A855F7", "#FF9500", "#06D6E8", "#FF6FB5", "#8B5A3C", "#B5F23D",
            "#0E9F8E", "#9AA3B2", "#2A3BA8", "#9B1B30", "#8EC5FF", "#5FF2B0", "#FFB199", "#5B4BDB", "#A3A322", "#F1F5F9"
    };

    private TubeColors() {
    }

    /** @param id color ID เริ่มที่ 1 */
    public static Color color(int id) {
        if (id >= 1 && id <= HEX.length) {
            return Color.web(HEX[id - 1]);
        }
        return Color.hsb((id * 47) % 360, 0.6, 0.92);
    }

    public static String name(int id) {
        if (id >= 1 && id <= NAMES.length) {
            return NAMES[id - 1];
        }
        return "Color " + id;
    }
}
