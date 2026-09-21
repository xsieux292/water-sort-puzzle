package watersort.vision;

/**
 * ดึงสีจากแต่ละ slot ในหลอดแก้ว
 *
 * Pipeline สำหรับแต่ละหลอด:
 * 1. แบ่งหลอดเป็น 4 slots (top → bottom)
 * 2. Crop ส่วนกลาง (หลีกเลี่ยงขอบหลอด/ไฮไลท์)
 * 3. Convert to HSV
 * 4. K-Means cluster (k=1) → dominant color
 * 5. Map HSV → nearest color ID จาก ColorPalette
 * 6. ถ้าสี = background → slot ว่าง
 *
 * @see SPEC.md Section 9.3 for algorithm details
 */
public class ColorExtractor {

    // TODO: Implement color extraction
    // ดู pseudocode ใน SPEC.md Section 9.3

    /**
     * ดึงสีจากทุก slot ของหลอด 1 หลอด
     *
     * @param tubeImage sub-image ของหลอด (cropped)
     * @return int[] ของ color IDs (bottom → top, ขนาด 0–4)
     */
    // public int[] extractColors(BufferedImage tubeImage) { ... }
}
