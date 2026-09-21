package watersort.vision;

import java.awt.Rectangle;
import java.util.Arrays;

/**
 * Data class representing a detected tube region and its extracted colors.
 */
public class TubeRegion {
    private final int tubeIndex;
    private final Rectangle bounds;
    private final int[] extractedColors;
    private final double[] slotConfidences;

    public TubeRegion(int tubeIndex, Rectangle bounds, int[] extractedColors, double[] slotConfidences) {
        this.tubeIndex = tubeIndex;
        this.bounds = bounds;
        this.extractedColors = extractedColors;
        this.slotConfidences = slotConfidences;
    }

    public int getTubeIndex() {
        return tubeIndex;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int[] getExtractedColors() {
        return extractedColors;
    }

    public double[] getSlotConfidences() {
        return slotConfidences;
    }

    @Override
    public String toString() {
        return "TubeRegion{" +
                "index=" + tubeIndex +
                ", bounds=" + bounds +
                ", colors=" + Arrays.toString(extractedColors) +
                ", conf=" + Arrays.toString(slotConfidences) +
                '}';
    }
}
