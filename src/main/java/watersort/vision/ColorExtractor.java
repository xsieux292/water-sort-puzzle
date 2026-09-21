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

    /** สัดส่วนความสูงหลอดที่เป็นฝา/ขอบด้านบน (ไม่มีของเหลว) — bounds ที่ detect ได้รวมส่วนนี้มาด้วย */
    static final double CAP_FRACTION = 0.13;
    /** สัดส่วนความสูงหลอดที่เป็นขอบโค้งด้านล่าง */
    static final double BOTTOM_FRACTION = 0.015;

    /**
     * ดึงค่าสี HSV จากทุก slot ของหลอด 1 หลอด
     *
     * @param tubeImage sub-image ของหลอด (BGR Mat)
     * @return List ของ Scalar (H, S, V) (bottom → top, ขนาด 4)
     */
    /**
     * ดึงค่าสีจากทุก slot ของหลอด 1 หลอด
     *
     * @param tubeImage sub-image ของหลอด (BGR Mat)
     * @return List ของ double[] (LAB, LAB, LAB, H, S, V) (bottom → top, ขนาด 4)
     */
    public List<double[]> extractColors(Mat tubeImage) {
        int height = tubeImage.rows();
        int width = tubeImage.cols();
        
        // แบ่ง slot เฉพาะส่วนที่เป็นของเหลวจริง (ตัดฝาหลอดด้านบนออก) ไม่เช่นนั้นตัวอย่างสีของ slot บนสุดจะปนกับสีฝา
        int liquidTop = (int) Math.round(height * CAP_FRACTION);
        int liquidBottom = (int) Math.round(height * (1 - BOTTOM_FRACTION));
        int slotHeight = (liquidBottom - liquidTop) / 4;
        
        List<double[]> extracted = new ArrayList<>();
        
        for (int slot = 0; slot < 4; slot++) {
            // Take only the very center of the block (40% margin -> 20% width/height box)
            double marginY = 0.4;
            double marginX = 0.4;
            
            int y1 = (int) (liquidTop + slot * slotHeight + (slotHeight * marginY));
            int y2 = (int) (liquidTop + (slot + 1) * slotHeight - (slotHeight * marginY));
            int x1 = (int) (width * marginX);
            int x2 = (int) (width * (1 - marginX));
            
            if (x1 >= x2 || y1 >= y2 || x1 < 0 || y1 < 0 || x2 > width || y2 > height) {
                extracted.add(new double[]{0,0,0,0,0,0,0,0,0});
                continue;
            }

            Rect roi = new Rect(x1, y1, x2 - x1, y2 - y1);
            Mat cropped = new Mat(tubeImage, roi);

            Scalar bgrMean = mean(cropped); // ค่าเฉลี่ย BGR ดิบ (ไว้ตั้งชื่อสีให้อ่านง่าย)

            Mat labRegion = new Mat();
            cvtColor(cropped, labRegion, COLOR_BGR2Lab);
            Scalar labMean = mean(labRegion);

            Mat hsvRegion = new Mat();
            cvtColor(cropped, hsvRegion, COLOR_BGR2HSV);
            Scalar hsvMean = mean(hsvRegion);

            extracted.add(new double[]{
                labMean.get(0), labMean.get(1), labMean.get(2),   // 0-2: LAB (ใช้จัดกลุ่มสี)
                hsvMean.get(0), hsvMean.get(1), hsvMean.get(2),    // 3-5: HSV
                bgrMean.get(2), bgrMean.get(1), bgrMean.get(0)     // 6-8: R, G, B (ใช้ตั้งชื่อสี)
            });

            labRegion.close();
            hsvRegion.close();
            cropped.close();
        }
        
        // Reverse for bottom-to-top
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < extracted.size(); i++) {
            result.add(extracted.get(extracted.size() - 1 - i));
        }
        
        return result;
    }
}
