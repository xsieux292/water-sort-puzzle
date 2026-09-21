package watersort.vision;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ดึงสีจากแต่ละ slot ในหลอดแก้ว
 */
public class ColorExtractor {

    /**
     * ดึงค่าสี HSV จากทุก slot ของหลอด 1 หลอด
     *
     * @param tubeImage sub-image ของหลอด (BGR Mat)
     * @return List ของ Scalar (H, S, V) (bottom → top, ขนาด 4)
     */
    public List<Scalar> extractColors(Mat tubeImage) {
        int height = tubeImage.rows();
        int width = tubeImage.cols();
        
        int slotHeight = height / 4;
        double margin = 0.3; // 30% margin
        
        List<Scalar> extracted = new ArrayList<>();
        
        for (int slot = 0; slot < 4; slot++) {
            int y1 = (int) (slot * slotHeight + (slotHeight * margin));
            int y2 = (int) ((slot + 1) * slotHeight - (slotHeight * margin));
            int x1 = (int) (width * margin);
            int x2 = (int) (width * (1 - margin));
            
            if (x1 >= x2 || y1 >= y2 || x1 < 0 || y1 < 0 || x2 > width || y2 > height) {
                extracted.add(new Scalar(0, 0, 0, 0));
                continue;
            }
            
            Rect roi = new Rect(x1, y1, x2 - x1, y2 - y1);
            Mat cropped = new Mat(tubeImage, roi);
            
            Mat hsvRegion = new Mat();
            cvtColor(cropped, hsvRegion, COLOR_BGR2HSV);
            
            Scalar meanColor = mean(hsvRegion);
            extracted.add(meanColor);
            
            hsvRegion.close();
            cropped.close();
        }
        
        // Reverse for bottom-to-top
        List<Scalar> result = new ArrayList<>();
        for (int i = 0; i < extracted.size(); i++) {
            result.add(extracted.get(extracted.size() - 1 - i));
        }
        
        return result;
    }
}
