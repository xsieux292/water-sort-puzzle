package watersort.ui.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ผลตรวจสอบกระดานที่ผู้ใช้กำลังแก้ไข
 *
 * @param errors        ปัญหาที่ต้องแก้ก่อน Confirm (เช่น สีมีจำนวน ≠ 4)
 * @param warnings      คำเตือนที่ไม่บล็อก (เช่น หลอดเปล่าน้อยกว่า 2)
 * @param colorCounts   color ID → จำนวนบล็อกบนกระดาน
 * @param suspectTubes  index (0-based) ของหลอดที่มีสีที่จำนวนผิดปกติ
 */
public record Validation(List<String> errors,
                         List<String> warnings,
                         Map<Integer, Integer> colorCounts,
                         Set<Integer> suspectTubes) {

    public boolean isValid() {
        return errors.isEmpty();
    }
}
