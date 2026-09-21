package watersort.vision;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ตรวจจับและ crop หลอดแก้วจากภาพหน้าจอเกม
 */
public class TubeDetector {

    /**
     * ตรวจจับขอบเขตของหลอดในรูป
     * @param imagePath path ไปยังรูป
     * @return List ของ Rectangle (พิกัดและขนาดหลอด)
     */
    public List<Rectangle> detect(String imagePath) {
        // Load image using OpenCV
        Mat src = imread(imagePath);
        if (src == null || src.empty()) {
            throw new IllegalArgumentException("Cannot read image: " + imagePath);
        }

        Mat gray = new Mat();
        cvtColor(src, gray, COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        GaussianBlur(gray, blurred, new Size(5, 5), 0);

        Mat edges = new Mat();
        Canny(blurred, edges, 50, 150);

        // Find contours
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        findContours(edges, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        List<Rectangle> rectangles = new ArrayList<>();
        long numContours = contours.size();
        
        for (long i = 0; i < numContours; i++) {
            Mat contour = contours.get(i);
            Rect cvRect = boundingRect(contour);
            
            double aspectRatio = (double) cvRect.height() / cvRect.width();
            
            // Filters based on typical tube proportions and size
            if (aspectRatio >= 2.0 && aspectRatio <= 6.0) {
                if (cvRect.height() > src.rows() * 0.1 && cvRect.width() > 10) { 
                    rectangles.add(new Rectangle(cvRect.x(), cvRect.y(), cvRect.width(), cvRect.height()));
                }
            }
        }
        
        // Remove nested/overlapping rectangles
        rectangles = nonMaxSuppression(rectangles);

        // Sort left-to-right, top-to-bottom
        // First sort by Y (row), assuming tubes are roughly in 2 rows. 
        // We cluster them by Y. If diff Y < 50, they are in the same row.
        rectangles.sort((r1, r2) -> {
            if (Math.abs(r1.y - r2.y) > r1.height / 2) {
                return Integer.compare(r1.y, r2.y);
            }
            return Integer.compare(r1.x, r2.x);
        });

        // Release Mats
        src.close();
        gray.close();
        blurred.close();
        edges.close();
        contours.close();
        hierarchy.close();

        return rectangles;
    }

    private List<Rectangle> nonMaxSuppression(List<Rectangle> boxes) {
        List<Rectangle> result = new ArrayList<>();
        for (Rectangle box : boxes) {
            boolean keep = true;
            for (Rectangle existing : result) {
                if (existing.intersects(box)) {
                    Rectangle intersection = existing.intersection(box);
                    double overlapArea = intersection.width * intersection.height;
                    double minArea = Math.min(box.width * box.height, existing.width * existing.height);
                    if (overlapArea / minArea > 0.3) { // 30% overlap means same object
                        keep = false;
                        break;
                    }
                }
            }
            if (keep) {
                result.add(box);
            }
        }
        return result;
    }
}
