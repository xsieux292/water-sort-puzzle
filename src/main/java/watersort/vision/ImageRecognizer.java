package watersort.vision;

import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import watersort.model.BoardState;

import java.awt.Rectangle;
import java.util.*;

/**
 * Orchestrator สำหรับ Image Recognition Pipeline
 */
public class ImageRecognizer {

    private final TubeDetector tubeDetector;
    private final ColorExtractor colorExtractor;

    public ImageRecognizer() {
        this.tubeDetector = new TubeDetector();
        this.colorExtractor = new ColorExtractor();
    }

    /**
     * Recognize หลอดและสีจากภาพหน้าจอเกม
     */
    public RecognitionResult recognize(String imagePath) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<TubeRegion> tubeRegions = new ArrayList<>();

        Mat src = imread(imagePath);
        if (src == null || src.empty()) {
            errors.add("Cannot read image: " + imagePath);
            return new RecognitionResult(null, tubeRegions, new HashMap<>(), 0.0, warnings, errors, false);
        }

        try {
            // 1. Detect tubes
            List<Rectangle> boundsList = tubeDetector.detect(imagePath);
            if (boundsList.isEmpty()) {
                errors.add("No tubes detected in the image.");
                return new RecognitionResult(null, tubeRegions, new HashMap<>(), 0.0, warnings, errors, false);
            }

            List<List<Scalar>> allTubeScalars = new ArrayList<>();
            List<Scalar> coloredScalars = new ArrayList<>();

            // 2. Extract HSV
            for (int i = 0; i < boundsList.size(); i++) {
                Rectangle b = boundsList.get(i);
                Rect cvRect = new Rect(b.x, b.y, b.width, b.height);
                Mat cropped = new Mat(src, cvRect);
                
                List<Scalar> scalars = colorExtractor.extractColors(cropped);
                allTubeScalars.add(scalars);
                
                for (Scalar s : scalars) {
                    // Check if not background
                    double h = s.get(0), sat = s.get(1), v = s.get(2);
                    if (!(v < 60 && sat < 50)) {
                        coloredScalars.add(s);
                    }
                }
                
                cropped.close();
            }

            // 3. Constrained Agglomerative Clustering (ensures max 4 per cluster)
            int k = coloredScalars.size() / 4;
            if (coloredScalars.size() % 4 != 0) {
                warnings.add("Colored blocks count (" + coloredScalars.size() + ") is not a multiple of 4.");
            }

            if (k == 0) {
                errors.add("No colors detected.");
                return new RecognitionResult(null, tubeRegions, new HashMap<>(), 0.0, warnings, errors, false);
            }

            // Each colored block is initially its own cluster
            List<List<Integer>> clusters = new ArrayList<>();
            for (int i = 0; i < coloredScalars.size(); i++) {
                List<Integer> c = new ArrayList<>();
                c.add(i);
                clusters.add(c);
            }

            // Keep merging closest clusters until we have k clusters
            while (clusters.size() > k) {
                double minFDistance = Double.MAX_VALUE;
                int mergeI = -1;
                int mergeJ = -1;

                for (int i = 0; i < clusters.size(); i++) {
                    for (int j = i + 1; j < clusters.size(); j++) {
                        List<Integer> c1 = clusters.get(i);
                        List<Integer> c2 = clusters.get(j);
                        if (c1.size() + c2.size() <= 4) {
                            double dist = clusterDistance(c1, c2, coloredScalars);
                            if (dist < minFDistance) {
                                minFDistance = dist;
                                mergeI = i;
                                mergeJ = j;
                            }
                        }
                    }
                }

                if (mergeI != -1 && mergeJ != -1) {
                    clusters.get(mergeI).addAll(clusters.get(mergeJ));
                    clusters.remove(mergeJ);
                } else {
                    // Cannot merge anymore without violating size constraints (should rarely happen in this specific puzzle)
                    break;
                }
            }

            // Create assignments array
            int[] assignments = new int[coloredScalars.size()];
            for (int c = 0; c < clusters.size(); c++) {
                for (int idx : clusters.get(c)) {
                    assignments[idx] = c;
                }
            }

            // Calculate centers for naming
            List<Scalar> centers = new ArrayList<>();
            for (int c = 0; c < clusters.size(); c++) {
                double sumH = 0, sumS = 0, sumV = 0;
                for (int idx : clusters.get(c)) {
                    sumH += coloredScalars.get(idx).get(0);
                    sumS += coloredScalars.get(idx).get(1);
                    sumV += coloredScalars.get(idx).get(2);
                }
                int count = clusters.get(c).size();
                centers.add(new Scalar(sumH / count, sumS / count, sumV / count, 0));
            }

            // Map each tube's scalars to the color IDs
            int coloredIndex = 0;
            int[][] boardArrays = new int[boundsList.size()][];
            for (int i = 0; i < boundsList.size(); i++) {
                List<Scalar> scalars = allTubeScalars.get(i);
                List<Integer> mapped = new ArrayList<>();
                
                for (Scalar s : scalars) {
                    double h = s.get(0), sat = s.get(1), v = s.get(2);
                    if (v < 60 && sat < 50) {
                        continue; // Empty slot
                    }
                    mapped.add(assignments[coloredIndex] + 1); // 1-based color ID
                    coloredIndex++;
                }
                
                int[] arr = new int[mapped.size()];
                for (int j = 0; j < mapped.size(); j++) {
                    arr[j] = mapped.get(j);
                }
                boardArrays[i] = arr;
                
                double[] conf = new double[4];
                java.util.Arrays.fill(conf, 1.0);
                tubeRegions.add(new TubeRegion(i, boundsList.get(i), arr, conf));
            }

            // Create Color Map
            Map<Integer, String> colorMap = new HashMap<>();
            ColorPalette predefined = new ColorPalette(); 
            for (int c = 0; c < centers.size(); c++) {
                Scalar center = centers.get(c);
                int predefinedId = predefined.mapToColorId((int)center.get(0), (int)center.get(1), (int)center.get(2));
                String name = predefined.getColorNames().getOrDefault(predefinedId, "Color_" + (c+1));
                colorMap.put(c + 1, name + "_" + (c+1)); // append ID to ensure unique names for validation
            }

            // 4. Validation
            Map<Integer, Integer> colorCounts = new HashMap<>();
            int emptyTubes = 0;
            
            for (int[] tubeColors : boardArrays) {
                if (tubeColors.length == 0) {
                    emptyTubes++;
                }
                for (int c : tubeColors) {
                    colorCounts.put(c, colorCounts.getOrDefault(c, 0) + 1);
                }
            }
            
            if (emptyTubes < 2) {
                errors.add(String.format("Only %d empty tubes detected (expected >= 2).", emptyTubes));
            }
            
            for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
                if (entry.getValue() != 4) {
                    errors.add(String.format("Color '%s' has %d blocks (expected 4).", colorMap.get(entry.getKey()), entry.getValue()));
                }
            }

            BoardState boardState = null;
            boolean isValid = errors.isEmpty();
            
            if (isValid || boardArrays.length > 0) {
                boardState = BoardState.fromArrays(boardArrays);
            }

            return new RecognitionResult(boardState, tubeRegions, colorMap,
                    isValid ? 1.0 : 0.5, warnings, errors, isValid);

        } catch (Exception e) {
            errors.add("Exception during recognition: " + e.getMessage());
            return new RecognitionResult(null, tubeRegions, new HashMap<>(), 0.0, warnings, errors, false);
        } finally {
            src.close();
        }
    }
    
    private double clusterDistance(List<Integer> c1, List<Integer> c2, List<Scalar> allScalars) {
        // Average linkage
        double sumDist = 0;
        for (int i : c1) {
            for (int j : c2) {
                sumDist += colorDistance(allScalars.get(i), allScalars.get(j));
            }
        }
        return sumDist / (c1.size() * c2.size());
    }
    
    private double colorDistance(Scalar s1, Scalar s2) {
        // Simple Euclidean distance in HSV space
        // Hue is circular (0-180 in OpenCV)
        double dh = Math.min(Math.abs(s1.get(0) - s2.get(0)), 180 - Math.abs(s1.get(0) - s2.get(0)));
        // Weight Hue more because Saturation and Value can vary due to shadows
        double dhW = dh * 2.0; 
        double ds = s1.get(1) - s2.get(1);
        double dv = s1.get(2) - s2.get(2);
        return Math.sqrt(dhW*dhW + ds*ds + dv*dv);
    }
}
