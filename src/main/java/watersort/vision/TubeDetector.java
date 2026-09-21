package watersort.vision;

/**
 * ตรวจจับและ crop หลอดแก้วจากภาพหน้าจอเกม
 *
 * Pipeline:
 * 1. Convert to grayscale
 * 2. Gaussian blur → Canny edge detection
 * 3. Find contours → filter by aspect ratio & area
 * 4. Non-max suppression (remove overlapping)
 * 5. Sort left-to-right, top-to-bottom
 * 6. Crop each tube as sub-image
 *
 * @see SPEC.md Section 9.2 for algorithm details
 */
public class TubeDetector {

    // TODO: Implement tube detection using OpenCV (JavaCV)
    // ดู pseudocode ใน SPEC.md Section 9.2

    /**
     * ตรวจจับหลอดทั้งหมดจากภาพหน้าจอเกม
     *
     * @param imagePath path ไปยังไฟล์ภาพ (PNG/JPG)
     * @return รายการ TubeRegion ที่ตรวจพบ (เรียงลำดับ)
     */
    // public List<TubeRegion> detect(String imagePath) { ... }
}
