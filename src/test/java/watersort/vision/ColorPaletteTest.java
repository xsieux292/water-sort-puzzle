package watersort.vision;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** V4: ColorPalette ต้อง map HSV → color ID ได้ครบทุกสีที่ประกาศไว้ */
class ColorPaletteTest {

    private final ColorPalette palette = new ColorPalette();

    @Test
    void mapsRepresentativeHsvOfEveryColor() {
        // {H, S, V, expected ID}   (OpenCV: H 0–180)
        int[][] cases = {
                {0, 220, 220, 1},     // Red
                {175, 220, 220, 1},   // Red (wrap-around)
                {15, 200, 230, 2},    // Orange
                {30, 200, 230, 3},    // Yellow
                {42, 200, 150, 4},    // Olive / yellow-green
                {65, 200, 200, 5},    // Green
                {90, 200, 200, 6},    // Cyan
                {115, 200, 200, 7},   // Blue
                {140, 150, 180, 8},   // Purple
                {165, 120, 230, 9},   // Pink
                {15, 200, 100, 10},   // Brown
                {0, 10, 150, 11},     // Gray
        };
        Set<Integer> seen = new HashSet<>();
        for (int[] c : cases) {
            assertEquals(c[3], palette.mapToColorId(c[0], c[1], c[2]), "HSV(" + c[0] + "," + c[1] + "," + c[2] + ")");
            seen.add(c[3]);
        }
        assertEquals(palette.getColorNames().keySet(), seen, "every declared color must be covered");
    }

    @Test
    void darkLowSaturationIsBackground() {
        assertEquals(-1, palette.mapToColorId(0, 0, 0));
        assertEquals(-1, palette.mapToColorId(100, 20, 20));
        assertEquals(-1, palette.mapToColorId(60, 30, 45));
    }
}
