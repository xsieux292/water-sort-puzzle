package watersort.vision;

/**
 * Predefined HSV ranges สำหรับ map dominant color → color ID
 *
 * ค่า HSV ที่ใช้:
 *   H (Hue):        0–180 (OpenCV convention)
 *   S (Saturation):  0–255
 *   V (Value):       0–255
 *
 * @see SPEC.md Section 9.4 for full color table
 */
public class ColorPalette {

    // TODO: Implement HSV-based color lookup
    // ดูตาราง HSV ใน SPEC.md Section 9.4

    // Example color definitions:
    // RED:    H=[0,10]∪[170,180], S>100, V>80
    // BLUE:   H=[100,130],        S>80,  V>60
    // GREEN:  H=[50,80],          S>80,  V>60
    // ...

    /**
     * Map HSV value → nearest known color ID
     *
     * @param h hue (0–180)
     * @param s saturation (0–255)
     * @param v value (0–255)
     * @return color ID (1-based), or -1 if background/empty
     */
    // public int mapToColorId(int h, int s, int v) { ... }

    /**
     * @return Map of color ID → color name (e.g. 1 → "Red")
     */
    // public Map<Integer, String> getColorNames() { ... }
}
