package watersort.model;

/**
 * แทนการกระทำ 1 ครั้ง = เทจากหลอด source ไปหลอด destination
 *
 * @param source      index ของหลอดต้นทาง (0-based)
 * @param destination index ของหลอดปลายทาง (0-based)
 */
public record Move(int source, int destination) {

    /**
     * แสดงผลในรูปแบบ "Pour Tube X → Tube Y" (1-based index สำหรับ user)
     */
    @Override
    public String toString() {
        return "Pour Tube " + (source + 1) + " → Tube " + (destination + 1);
    }
}
