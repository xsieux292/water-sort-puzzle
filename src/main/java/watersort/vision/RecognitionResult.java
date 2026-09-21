package watersort.vision;

import watersort.model.BoardState;

import java.util.List;
import java.util.Map;

/**
 * ผลลัพธ์จากการ recognize ภาพหน้าจอเกม
 */
public class RecognitionResult {
    private final BoardState boardState;
    private final List<TubeRegion> tubeRegions;
    private final Map<Integer, String> colorMap;
    private final double confidence;
    private final List<String> warnings;
    private final List<String> errors;
    private final boolean isValid;

    public RecognitionResult(BoardState boardState, List<TubeRegion> tubeRegions, Map<Integer, String> colorMap,
                             double confidence, List<String> warnings, List<String> errors, boolean isValid) {
        this.boardState = boardState;
        this.tubeRegions = tubeRegions;
        this.colorMap = colorMap;
        this.confidence = confidence;
        this.warnings = warnings;
        this.errors = errors;
        this.isValid = isValid;
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public List<TubeRegion> getTubeRegions() {
        return tubeRegions;
    }

    public Map<Integer, String> getColorMap() {
        return colorMap;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RecognitionResult {\n");
        sb.append("  isValid=").append(isValid).append("\n");
        sb.append("  confidence=").append(String.format("%.2f", confidence)).append("\n");
        if (errors != null && !errors.isEmpty()) {
            sb.append("  errors=").append(errors).append("\n");
        }
        if (warnings != null && !warnings.isEmpty()) {
            sb.append("  warnings=").append(warnings).append("\n");
        }
        if (boardState != null) {
            sb.append("  boardState=\n").append(boardState).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
