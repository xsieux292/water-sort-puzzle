package watersort.vision;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ตั้งชื่อสีที่อ่านง่ายให้กับ cluster โดยหาสีอ้างอิงที่ใกล้ที่สุด (nearest reference)
 *
 * <p>สีอ้างอิงถูก calibrate กับพาเลตต์ของเกม Water/Ball Sort ที่พบบ่อย
 * (โทนสีในภาพตัวอย่าง {@code test_image.png}) ระยะห่างสีคำนวณด้วยสูตร
 * "redmean" ซึ่งประมาณการรับรู้ของตามนุษย์ได้ดีกว่าระยะ RGB ตรง ๆ
 *
 * <p>ถ้ามีหลาย cluster แม็ปไปชื่อเดียวกัน (พาเลตต์มีสองสีคล้ายกันมาก)
 * จะเติมเลขต่อท้าย เช่น "Green", "Green 2" เพื่อให้ชื่อไม่ซ้ำ
 */
public final class ColorNamer {

    /** สีอ้างอิง: ชื่อ → RGB */
    private static final String[] NAMES = {
            "Red", "Orange", "Yellow", "Olive", "Light Green", "Dark Green",
            "Light Blue", "Blue", "Purple", "Pink", "Brown", "Gray", "White"
    };
    private static final int[][] RGB = {
            {211, 60, 50},    // Red
            {220, 145, 84},   // Orange
            {236, 218, 108},  // Yellow
            {132, 154, 56},   // Olive
            {133, 211, 138},  // Light Green
            {52, 104, 63},    // Dark Green
            {106, 162, 224},  // Light Blue
            {64, 54, 189},    // Blue
            {111, 56, 146},   // Purple
            {216, 108, 128},  // Pink
            {124, 82, 33},    // Brown
            {102, 103, 105},  // Gray
            {235, 235, 235},  // White
    };

    private ColorNamer() {
    }

    /** ชื่อสีอ้างอิงที่ใกล้ที่สุดกับ (r, g, b) */
    public static String nearestName(double r, double g, double b) {
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < RGB.length; i++) {
            double d = redmeanDistance(r, g, b, RGB[i][0], RGB[i][1], RGB[i][2]);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return NAMES[best];
    }

    /**
     * ตั้งชื่อให้ทุก cluster พร้อมกัน โดยรับประกันว่าชื่อไม่ซ้ำ
     *
     * @param clusterRgb clusterRgb[c] = {r, g, b} เฉลี่ยของ cluster c (0-based)
     * @return map: colorId (c+1) → ชื่อสี
     */
    public static Map<Integer, String> nameClusters(double[][] clusterRgb) {
        Map<Integer, String> result = new LinkedHashMap<>();
        Map<String, Integer> used = new LinkedHashMap<>();
        for (int c = 0; c < clusterRgb.length; c++) {
            String base = nearestName(clusterRgb[c][0], clusterRgb[c][1], clusterRgb[c][2]);
            int count = used.merge(base, 1, Integer::sum);
            result.put(c + 1, count == 1 ? base : base + " " + count);
        }
        return result;
    }

    /** ระยะห่างสีแบบ redmean (ถูกกว่าและใกล้เคียงการรับรู้มากกว่า Euclidean RGB) */
    private static double redmeanDistance(double r1, double g1, double b1, double r2, double g2, double b2) {
        double rmean = (r1 + r2) / 2.0;
        double dr = r1 - r2;
        double dg = g1 - g2;
        double db = b1 - b2;
        return (2 + rmean / 256) * dr * dr + 4 * dg * dg + (2 + (255 - rmean) / 256) * db * db;
    }
}
