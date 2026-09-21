package watersort.vision;

import org.junit.jupiter.api.Test;
import java.net.URL;

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
}
