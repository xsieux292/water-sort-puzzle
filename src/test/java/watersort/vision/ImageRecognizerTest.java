package watersort.vision;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

import static org.junit.jupiter.api.Assertions.*;

class ImageRecognizerTest {

    // Note: We skip complex assertions here to just ensure the pipeline runs without exceptions.
    // Full E2E logic should be tested manually or with a known ground truth image.

    @Test
    void testColorPalette() {
        ColorPalette palette = new ColorPalette();
        
        // Red
        assertEquals(1, palette.mapToColorId(0, 200, 200));
        assertEquals(1, palette.mapToColorId(175, 200, 200));
        
        // Green
        assertEquals(5, palette.mapToColorId(60, 200, 200));
        
        // Background (low value/saturation)
        assertEquals(-1, palette.mapToColorId(0, 0, 0));
        assertEquals(-1, palette.mapToColorId(100, 20, 20));
    }
    
    @Test
    void testImageRecognizerEndToEnd() throws Exception {
        // Find the test image in resources
        URL url = getClass().getClassLoader().getResource("test_image.png");
        assertNotNull(url, "Test image not found in resources");
        
        String imagePath = url.getPath();
        ImageRecognizer recognizer = new ImageRecognizer();
        RecognitionResult result = recognizer.recognize(imagePath);
        
        // V2: TubeDetector detect ภาพตัวอย่าง 14 หลอด -> ได้ >= 12 หลอด
        assertNotNull(result.getTubeRegions());
        assertTrue(result.getTubeRegions().size() >= 12, "Should detect at least 12 tubes");
        assertEquals(14, result.getTubeRegions().size(), "Should detect exactly 14 tubes for this specific image");
        
        // V3 & V5: ColorExtractor & ImageRecognizer End-to-End
        // Even if some validation fails (due to color shading), the board state must be constructed!
        assertNotNull(result.getBoardState(), "Board state should be parsed even if partially valid");
        
        // Verify BoardState shape (14 tubes)
        assertEquals(14, result.getBoardState().tubeCount(), "Board should have 14 tubes");
        
        // V7: Validation logic checks empty tubes >= 2
        // We know this image has 2 empty tubes
        int emptyCount = 0;
        for (int i = 0; i < result.getBoardState().tubeCount(); i++) {
            if (result.getBoardState().getTube(i).isEmpty()) {
                emptyCount++;
            }
        }
        assertTrue(emptyCount >= 2, "Should detect at least 2 empty tubes");
        
        // V6: Check that confidence, warnings, errors exist
        assertNotNull(result.getWarnings());
        assertNotNull(result.getErrors());
        assertTrue(result.getConfidence() > 0.0);
    }

    private static String samplePath() {
        URL url = ImageRecognizerTest.class.getClassLoader().getResource("test_image.png");
        assertNotNull(url, "Test image not found in resources");
        return url.getPath();
    }

    /** V5 + V7: ภาพตัวอย่างต้องได้บอร์ดที่ตรงกับ ground truth ทุกบล็อก และผ่าน validation */
    @Test
    void sampleImageMatchesGroundTruth() {
        RecognitionResult result = new ImageRecognizer().recognize(samplePath());

        assertTrue(result.isValid(), "errors: " + result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertEquals(1.0, result.getConfidence(), 1e-9);
        assertEquals(SampleBoard.canonical(SampleBoard.TUBES), SampleBoard.canonical(result.getBoardState()));
    }

    /** ตรวจจับสี 100%: ชื่อสีที่ตั้งให้ทุกบล็อกต้องตรงกับที่มองเห็นในภาพ (ทั้ง 12 สี × 48 บล็อก) */
    @Test
    void sampleImageNamesEveryColorCorrectly() {
        RecognitionResult result = new ImageRecognizer().recognize(samplePath());

        assertEquals(12, result.getColorMap().size(), "should detect all 12 distinct colors");
        assertEquals(SampleBoard.expectedNamedBoard(),
                SampleBoard.namedBoard(result.getBoardState(), result.getColorMap()));
    }

    /** V7: ถ้าสีบางสีจำนวนไม่ครบ 4 ต้องมี error (ทาบล็อกหนึ่งด้วยสีอื่นเพื่อสร้างสถานการณ์นี้) */
    @Test
    void validationReportsColorCountsThatAreNotFour(@TempDir Path tmp) {
        Mat img = imread(samplePath());
        // บล็อกบนสุดของหลอด 1 (สีเขียวอ่อน) → ทาทับด้วยสีพื้นหลัง เหมือน recognizer มองไม่เห็นบล็อกนี้
        rectangle(img, new Point(60, 125), new Point(160, 195), new Scalar(30, 30, 30, 0), -1, 8, 0);
        Path out = tmp.resolve("tampered.png");
        imwrite(out.toString(), img);
        img.close();

        RecognitionResult result = new ImageRecognizer().recognize(out.toString());

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("expected 4")), result.getErrors().toString());
        assertTrue(result.getConfidence() < 1.0);
    }

    /** V3: ColorExtractor — แยก "สีเดียวกัน / ต่างสี" ของทุกคู่บล็อกได้ถูก ≥ 80% และแยกช่องว่างออกจากบล็อกสีได้ครบ */
    @Test
    void colorExtractorSeparatesColorsAndEmptySlots() {
        String path = samplePath();
        List<java.awt.Rectangle> bounds = new TubeDetector().detect(path);
        assertEquals(SampleBoard.TUBES.length, bounds.size());

        Mat src = imread(path);
        ColorExtractor extractor = new ColorExtractor();
        java.util.List<double[]> labs = new java.util.ArrayList<>();
        java.util.List<Character> truth = new java.util.ArrayList<>();
        for (int i = 0; i < bounds.size(); i++) {
            java.awt.Rectangle b = bounds.get(i);
            Mat crop = new Mat(src, new Rect(b.x, b.y, b.width, b.height));
            List<double[]> slots = extractor.extractColors(crop);
            crop.close();
            assertEquals(4, slots.size());
            for (int s = 0; s < 4; s++) {
                boolean filled = s < SampleBoard.TUBES[i].length();
                assertEquals(filled, ImageRecognizer.isColored(slots.get(s)), "tube " + (i + 1) + " slot " + s);
                if (filled) {
                    labs.add(slots.get(s));
                    truth.add(SampleBoard.TUBES[i].charAt(s));
                }
            }
        }
        src.close();

        int correct = 0;
        int total = 0;
        for (int i = 0; i < labs.size(); i++) {
            for (int j = i + 1; j < labs.size(); j++) {
                double d = Math.sqrt(Math.pow(labs.get(i)[0] - labs.get(j)[0], 2)
                        + Math.pow(labs.get(i)[1] - labs.get(j)[1], 2)
                        + Math.pow(labs.get(i)[2] - labs.get(j)[2], 2));
                boolean predictedSame = d < 10;
                if (predictedSame == truth.get(i).equals(truth.get(j))) {
                    correct++;
                }
                total++;
            }
        }
        assertEquals(48, labs.size());
        assertTrue((double) correct / total >= 0.80, "accuracy = " + (double) correct / total);
    }
}
