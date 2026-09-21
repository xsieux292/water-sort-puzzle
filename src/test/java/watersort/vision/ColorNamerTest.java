package watersort.vision;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบการตั้งชื่อสีจากค่า RGB (ใช้ค่าที่วัดจริงจาก test_image.png) */
class ColorNamerTest {

    @Test
    void namesMeasuredPaletteColorsCorrectly() {
        // {R, G, B, expected} — ค่าเฉลี่ยจริงของแต่ละสีในภาพตัวอย่าง
        Object[][] cases = {
                {0xEC, 0xDA, 0x6C, "Yellow"},
                {0x85, 0xD3, 0x89, "Light Green"},
                {0xDC, 0x91, 0x54, "Orange"},
                {0x6A, 0xA2, 0xE0, "Light Blue"},
                {0x84, 0x9A, 0x38, "Olive"},
                {0xD8, 0x67, 0x7B, "Pink"},
                {0x66, 0x67, 0x69, "Gray"},
                {0xB7, 0x3F, 0x34, "Red"},
                {0x34, 0x68, 0x3F, "Dark Green"},
                {0x77, 0x4C, 0x1A, "Brown"},
                {0x68, 0x2F, 0x8E, "Purple"},
                {0x40, 0x36, 0xBD, "Blue"},
        };
        for (Object[] c : cases) {
            assertEquals(c[3], ColorNamer.nearestName((int) c[0], (int) c[1], (int) c[2]),
                    String.format("#%02X%02X%02X", c[0], c[1], c[2]));
        }
    }

    @Test
    void disambiguatesDuplicateNamesWithSuffix() {
        // สองสีเขียวเข้มเกือบเหมือนกัน → ควรได้ชื่อไม่ซ้ำ
        double[][] rgb = {{52, 104, 63}, {55, 108, 66}, {211, 60, 50}};
        Map<Integer, String> names = ColorNamer.nameClusters(rgb);
        assertEquals("Dark Green", names.get(1));
        assertEquals("Dark Green 2", names.get(2));
        assertEquals("Red", names.get(3));
    }
}
