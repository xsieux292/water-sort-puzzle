package watersort.vision;

import java.util.HashMap;
import java.util.Map;

/**
 * Predefined HSV ranges สำหรับ map dominant color → color ID
 *
 * ค่า HSV ที่ใช้ (อิงตาม OpenCV):
 *   H (Hue):        0–180
 *   S (Saturation):  0–255
 *   V (Value):       0–255
 */
public class ColorPalette {

    private final Map<Integer, String> colorNames = new HashMap<>();

    public ColorPalette() {
        colorNames.put(1, "Red");
        colorNames.put(2, "Orange");
        colorNames.put(3, "Yellow");
        colorNames.put(4, "Olive/YellowGreen");
        colorNames.put(5, "Green");
        colorNames.put(6, "Cyan");
        colorNames.put(7, "Blue");
        colorNames.put(8, "Purple");
        colorNames.put(9, "Pink");
        colorNames.put(10, "Brown");
        colorNames.put(11, "Gray");
    }

    /**
     * Map HSV value → nearest known color ID
     *
     * @param h hue (0–180)
     * @param s saturation (0–255)
     * @param v value (0–255)
     * @return color ID (1-based), or -1 if background/empty
     */
    public int mapToColorId(int h, int s, int v) {
        // ถือว่าเป็น background/empty slot ถ้าสว่างน้อยหรือไม่อิ่มตัวแบบมืด
        // Background: Value < 60 and Saturation < 50
        if (v < 60 && s < 50) return -1;
        
        // Gray
        if (s < 40 && v >= 80) return 11;
        
        // Red wraps around 0 and 180
        if ((h <= 10 || h >= 170) && s > 100 && v > 80) return 1;
        
        if (h > 10 && h <= 25 && s > 100 && v > 80) return 2; // Orange
        if (h > 10 && h <= 30 && s > 60 && v >= 40 && v <= 120) return 10; // Brown (low value orange)
        
        if (h > 25 && h <= 35 && s > 100 && v > 80) return 3; // Yellow
        if (h > 35 && h <= 50 && s > 60 && v > 60) return 4; // Olive/YellowGreen
        if (h > 50 && h <= 80 && s > 80 && v > 60) return 5; // Green
        if (h > 80 && h <= 100 && s > 80 && v > 60) return 6; // Cyan
        if (h > 100 && h <= 130 && s > 80 && v > 60) return 7; // Blue
        if (h > 130 && h <= 155 && s > 60 && v > 50) return 8; // Purple
        if (h > 155 && h < 170 && s > 40 && v > 80) return 9; // Pink
        
        // Fallback: use hue to find nearest if S and V didn't match perfectly, 
        // but let's just do a strict nearest hue check
        if (h <= 10 || h >= 170) return 1;
        if (h <= 25) {
            if (v < 120) return 10;
            return 2;
        }
        if (h <= 35) return 3;
        if (h <= 50) return 4;
        if (h <= 80) return 5;
        if (h <= 100) return 6;
        if (h <= 130) return 7;
        if (h <= 155) return 8;
        return 9; // 155-170
    }

    public Map<Integer, String> getColorNames() {
        return colorNames;
    }
}
