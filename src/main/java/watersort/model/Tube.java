package watersort.model;

/**
 * แทนหลอดแก้ว 1 หลอด ใช้ Stack-like behavior (LIFO)
 * ความจุสูงสุด 4 บล็อกสี
 *
 * colors[0] = ก้นหลอด (bottom), colors[size-1] = บนสุด (top)
 */
public class Tube {

    public static final int CAPACITY = 4;

    private final int[] colors;
    private int size;

    public Tube() {
        this.colors = new int[CAPACITY];
        this.size = 0;
    }

    public Tube(int... initialColors) {
        this();
        for (int color : initialColors) {
            push(color);
        }
    }

    // --- Stack Operations ---

    /**
     * ดูสีบนสุดของหลอด (peek)
     * @throws IllegalStateException ถ้าหลอดว่าง
     */
    public int topColor() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot peek: tube is empty");
        }
        return colors[size - 1];
    }

    /**
     * ดึงสีบนสุดออกจากหลอด
     * @return สีที่ถูกดึงออก
     * @throws IllegalStateException ถ้าหลอดว่าง
     */
    public int pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot pop: tube is empty");
        }
        int color = colors[--size];
        colors[size] = 0; // clear
        return color;
    }

    /**
     * ใส่สีลงบนสุดของหลอด
     * @param color สีที่จะใส่
     * @throws IllegalStateException ถ้าหลอดเต็ม
     */
    public void push(int color) {
        if (isFull()) {
            throw new IllegalStateException("Cannot push: tube is full");
        }
        colors[size++] = color;
    }

    // --- Query Methods ---

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == CAPACITY;
    }

    public int size() {
        return size;
    }

    /**
     * ตรวจสอบว่าหลอดเต็ม + สีเดียวกันทั้งหมด (sorted/completed)
     */
    public boolean isSorted() {
        if (size != CAPACITY) return false;
        for (int i = 1; i < CAPACITY; i++) {
            if (colors[i] != colors[0]) return false;
        }
        return true;
    }

    /**
     * นับจำนวนบล็อกสีเดียวกันที่ต่อเนื่องจากบนสุด
     * เช่น [R, B, B, B] → topColorCount() = 3
     */
    public int topColorCount() {
        if (isEmpty()) return 0;
        int count = 1;
        int top = colors[size - 1];
        for (int i = size - 2; i >= 0; i--) {
            if (colors[i] == top) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * ตรวจว่าหลอดมีสีเดียวทั้งหมด (อาจยังไม่เต็ม)
     */
    public boolean isUniform() {
        if (isEmpty()) return true;
        for (int i = 1; i < size; i++) {
            if (colors[i] != colors[0]) return false;
        }
        return true;
    }

    /**
     * ดูสีที่ตำแหน่ง index (0 = bottom)
     */
    public int getColorAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        return colors[index];
    }

    // --- Copy ---

    /**
     * สำเนาหลอดแบบ deep copy (ไม่กระทบต้นฉบับ)
     */
    public Tube deepCopy() {
        Tube copy = new Tube();
        System.arraycopy(this.colors, 0, copy.colors, 0, CAPACITY);
        copy.size = this.size;
        return copy;
    }

    // --- Object Overrides ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tube other)) return false;
        if (this.size != other.size) return false;
        for (int i = 0; i < size; i++) {
            if (this.colors[i] != other.colors[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = size;
        for (int i = 0; i < size; i++) {
            hash = 31 * hash + colors[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(colors[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
