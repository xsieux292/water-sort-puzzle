package watersort.solver;

import watersort.model.BoardState;

/**
 * Interface สำหรับ Solver ทุกประเภท (BFS, A*, etc.)
 */
public interface Solver {

    /**
     * แก้ปริศนา Water Sort Puzzle จากสถานะเริ่มต้น
     *
     * @param initialState สถานะเริ่มต้นของกระดาน
     * @return ผลลัพธ์ประกอบด้วยลำดับ moves (ถ้าแก้ได้) และสถิติ
     */
    SolveResult solve(BoardState initialState);
}
