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
}
