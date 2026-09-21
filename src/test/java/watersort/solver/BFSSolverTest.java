package watersort.solver;

import org.junit.jupiter.api.Test;
import watersort.model.BoardState;
import static org.junit.jupiter.api.Assertions.*;

class BFSSolverTest {

    @Test
    void testAlreadySolved() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 1, 1},
            {}
        });
        
        BFSSolver solver = new BFSSolver();
        SolveResult result = solver.solve(board);
        
        assertTrue(result.isSolved());
        assertEquals(0, result.getStepCount());
        assertEquals(1, result.getStatesExplored());
    }

    @Test
    void testSimplePuzzle() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 1},
            {1},
            {}
        });
        
        BFSSolver solver = new BFSSolver();
        SolveResult result = solver.solve(board);
        
        assertTrue(result.isSolved());
        assertTrue(result.getStepCount() <= 2);
    }

    @Test
    void testMediumPuzzle() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2, 1, 3},
            {2, 3, 1, 2},
            {3, 1, 3, 2},
            {},
            {}
        });
        
        BFSSolver solver = new BFSSolver();
        SolveResult result = solver.solve(board);
        
        assertTrue(result.isSolved());
        assertEquals(10, result.getStepCount()); // optimal for this demo puzzle
    }

    @Test
    void testUnsolvable() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2, 1, 2},
            {2, 1, 2, 1}
        }); // full tubes, no empty tubes
        
        BFSSolver solver = new BFSSolver();
        SolveResult result = solver.solve(board);
        
        assertFalse(result.isSolved());
    }

    @Test
    void testShortestPath() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 2, 2},
            {2, 2, 1, 1},
            {}
        });
        
        BFSSolver solver = new BFSSolver();
        SolveResult result = solver.solve(board);
        
        assertTrue(result.isSolved());
        // optimal steps for this simple one is 3
        assertEquals(3, result.getStepCount());
    }
}
