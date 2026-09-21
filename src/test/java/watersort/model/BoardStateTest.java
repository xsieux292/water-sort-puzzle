package watersort.model;

// TODO: Add JUnit 5 dependency and implement tests

/**
 * Unit tests for BoardState class
 *
 * Planned test cases:
 * - testIsGoalTrue: ทุกหลอดว่างหรือ sorted → true
 * - testIsGoalFalse: มีหลอดที่ยังไม่ sorted → false
 * - testGetValidMoves: ตรวจสอบว่า moves ที่ generate มาถูกต้องตามกฎ
 * - testPruningSkipCompleted: หลอดที่ sorted แล้วไม่ถูกเลือกเป็น source
 * - testPruningIdenticalEmpties: เทไปหลอดเปล่าแค่ 1 หลอดเท่านั้น
 * - testPruningUniformToEmpty: หลอดที่มีสีเดียว ไม่เทไปหลอดเปล่า
 * - testApplyMove: apply move → state ใหม่ถูกต้อง
 * - testApplyMoveMultiPour: เทหลายบล็อกพร้อมกันถ้าสีเดียวกัน
 * - testImmutability: apply move → state เดิมไม่เปลี่ยน
 * - testEquals: state เหมือนกัน → true
 * - testHashCode: state เหมือนกัน → hashCode เท่ากัน
 * - testFromArrays: สร้างจาก int[][] ได้ถูกต้อง
 */
public class BoardStateTest {
    // TODO: Implement with JUnit 5
}
