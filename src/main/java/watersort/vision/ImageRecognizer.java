package watersort.vision;

/**
 * Orchestrator สำหรับ Image Recognition Pipeline
 *
 * เชื่อมทุก component เข้าด้วยกัน:
 *   TubeDetector → ColorExtractor → Validation → RecognitionResult
 *
 * Usage:
 *   ImageRecognizer recognizer = new ImageRecognizer();
 *   RecognitionResult result = recognizer.recognize("screenshot.png");
 *   if (result.isValid()) {
 *       BoardState board = result.getBoardState();
 *       // → ส่งต่อให้ Solver
 *   }
 *
 * @see SPEC.md Section 9 for full pipeline specification
 */
public class ImageRecognizer {

    // TODO: Implement orchestration pipeline
    // 1. TubeDetector.detect(imagePath)
    // 2. For each detected tube: ColorExtractor.extractColors(tubeImage)
    // 3. Validate (SPEC.md Section 9.5)
    // 4. Build RecognitionResult

    /**
     * Recognize หลอดและสีจากภาพหน้าจอเกม
     *
     * @param imagePath path ไปยังไฟล์ภาพ
     * @return ผลลัพธ์การ recognize พร้อม BoardState (ถ้า valid)
     */
    // public RecognitionResult recognize(String imagePath) { ... }
}
