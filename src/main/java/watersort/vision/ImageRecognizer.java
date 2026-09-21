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

            List<List<double[]>> allTubeScalars = new ArrayList<>();
            List<double[]> flatScalars = new ArrayList<>();

            // 2. Extract LAB & HSV colors
            for (int i = 0; i < boundsList.size(); i++) {
                Rectangle b = boundsList.get(i);
                Rect cvRect = new Rect(b.x, b.y, b.width, b.height);
                Mat cropped = new Mat(src, cvRect);
                
                List<double[]> scalars = colorExtractor.extractColors(cropped);
                allTubeScalars.add(scalars);
                flatScalars.addAll(scalars);
                
                cropped.close();
            }

            // The empty slots are always the background, which is the darkest color (lowest L).
            // We sort all blocks by L descending. We don't know exactly how many colored blocks there are,
            // but we know it's a multiple of 4, and the rest are background.
            // Let's find the threshold L that separates the dark background from the colored blocks.
            // Actually, we can just sort them by L, and since we know background L is usually < 50,
            // and colored blocks are > 60. But wait, what if we just use a reliable L threshold?
            // Let's look for the largest gap in L values at the lower end!
            flatScalars.sort((a, b) -> Double.compare(b[0], a[0])); // Highest L first
            
            // We know the number of colored blocks must be a multiple of 4.
            // Let's assume the background blocks are all identical and have very low L.
            // Let's just find how many blocks have L > 50.
            List<double[]> coloredScalars = new ArrayList<>();
            for (double[] s : flatScalars) {
                // Background L is usually around 30-45. Colored blocks are > 55 even for dark colors.
                if (s[0] > 55) {
                    coloredScalars.add(s);
                }
            }
            
            // Ensure it's a multiple of 4 by adjusting the threshold dynamically if needed
            while (coloredScalars.size() % 4 != 0 && coloredScalars.size() < flatScalars.size()) {
                // Add the next brightest block until we hit a multiple of 4
                coloredScalars.add(flatScalars.get(coloredScalars.size()));
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

            // We need a way to know if a scalar from allTubeScalars is in coloredScalars.
            // Let's just use a threshold based on the lowest L in coloredScalars.
            double thresholdL = coloredScalars.get(coloredScalars.size() - 1)[0] - 1.0; // anything > this is colored

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

            // Map each tube's scalars to the color IDs
            // We need to map the original scalars back to the coloredScalars index
            int[][] boardArrays = new int[boundsList.size()][];
            for (int i = 0; i < boundsList.size(); i++) {
                List<double[]> scalars = allTubeScalars.get(i);
                List<Integer> mapped = new ArrayList<>();
                
                for (double[] s : scalars) {
                    if (s[0] <= thresholdL) {
                        continue; // Empty slot
                    }
                    
                    // Find exactly which coloredScalar this is
                    int matchedIndex = -1;
                    for(int j=0; j<coloredScalars.size(); j++) {
                        if (coloredScalars.get(j) == s) {
                            matchedIndex = j;
                            break;
                        }
                    }
                    
                    if (matchedIndex != -1) {
                        mapped.add(assignments[matchedIndex] + 1); // 1-based color ID
                    }
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
            for (int c = 0; c < k; c++) {
                colorMap.put(c + 1, "Color_" + (c+1));
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
    
    private double clusterDistance(List<Integer> c1, List<Integer> c2, List<double[]> allScalars) {
        // Average linkage
        double sumDist = 0;
        for (int i : c1) {
            for (int j : c2) {
                sumDist += colorDistance(allScalars.get(i), allScalars.get(j));
            }
        }
        return sumDist / (c1.size() * c2.size());
    }
    
    private double colorDistance(double[] s1, double[] s2) {
        // Simple Euclidean distance in LAB space
        double dL = s1[0] - s2[0];
        double da = s1[1] - s2[1];
        double db = s1[2] - s2[2];
        return Math.sqrt(dL*dL + da*da + db*db);
    }
}
