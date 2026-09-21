package watersort.ui;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import watersort.ui.model.ColorScheme;
import watersort.ui.model.TubeColors;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ColorSchemeTest {

    @Test
    void usesDetectedColorForKnownIdAndFallsBackOtherwise() {
        ColorScheme scheme = ColorScheme.fromRecognition(
                Map.of(1, new int[]{180, 60, 50}),
                Map.of(1, "Red"));

        assertEquals(Color.rgb(180, 60, 50), scheme.color(1), "known id → detected color");
        assertEquals("Red", scheme.name(1));
        // id ที่ไม่รู้จัก → พาเลตต์มาตรฐาน
        assertEquals(TubeColors.color(2), scheme.color(2));
        assertEquals(TubeColors.name(2), scheme.name(2));
    }

    @Test
    void emptyRecognitionYieldsDefaultScheme() {
        assertSame(ColorScheme.DEFAULT, ColorScheme.fromRecognition(Map.of(), Map.of()));
        assertEquals(TubeColors.color(1), ColorScheme.DEFAULT.color(1));
        assertEquals(TubeColors.name(3), ColorScheme.DEFAULT.name(3));
    }

    @Test
    void clampsOutOfRangeChannels() {
        ColorScheme scheme = ColorScheme.fromRecognition(Map.of(1, new int[]{300, -20, 128}), Map.of());
        assertEquals(Color.rgb(255, 0, 128), scheme.color(1));
    }
}
