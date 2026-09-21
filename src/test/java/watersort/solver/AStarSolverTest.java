package watersort.solver;

import org.junit.jupiter.api.Test;
import watersort.model.BoardState;
import static org.junit.jupiter.api.Assertions.*;

class AStarSolverTest {

    @Test
    void testAlreadySolved() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 1, 1},
            {}
        });
        
        AStarSolver solver = new AStarSolver();
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
        
        AStarSolver solver = new AStarSolver();
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
        
        AStarSolver aStar = new AStarSolver();
        SolveResult resultAStar = aStar.solve(board);
        
        BFSSolver bfs = new BFSSolver();
        SolveResult resultBFS = bfs.solve(board);
        
        assertTrue(resultAStar.isSolved());
        // A* should yield the same optimal step count as BFS
        assertEquals(resultBFS.getStepCount(), resultAStar.getStepCount()); 
        assertEquals(10, resultAStar.getStepCount());
    }

    @Test
    void testUnsolvable() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2, 1, 2},
            {2, 1, 2, 1}
        }); 
        
        AStarSolver solver = new AStarSolver();
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
        
        AStarSolver aStar = new AStarSolver();
        SolveResult resultAStar = aStar.solve(board);
        
        assertTrue(resultAStar.isSolved());
        assertEquals(3, resultAStar.getStepCount());
    }

    @Test
    void testHeuristic() {
        AStarSolver solver = new AStarSolver();
        
        // Sorted tube should have 0 color changes
        BoardState sorted = BoardState.fromArrays(new int[][]{{1, 1, 1, 1}});
        assertEquals(0, solver.heuristic(sorted));
        
        // Tube with 2 colors changing once should have 1 color change
        BoardState oneChange = BoardState.fromArrays(new int[][]{{1, 1, 2, 2}});
        assertEquals(1, solver.heuristic(oneChange));
        
        // Tube alternating should have 3 color changes
        BoardState threeChanges = BoardState.fromArrays(new int[][]{{1, 2, 1, 2}});
        assertEquals(3, solver.heuristic(threeChanges));
    }
    
    @Test
    void testAStarPerformanceVsBFS() {
        // A complex puzzle (e.g. 7-8 colors) to show A* explores fewer states
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2, 3, 4},
            {5, 6, 7, 1},
            {2, 3, 4, 5},
            {6, 7, 1, 2},
            {3, 4, 5, 6},
            {7, 1, 2, 3},
            {4, 5, 6, 7},
            {},
            {}
        });
        
        AStarSolver aStar = new AStarSolver();
        SolveResult resultAStar = aStar.solve(board);
        
        BFSSolver bfs = new BFSSolver();
        SolveResult resultBFS = bfs.solve(board);
        
        assertTrue(resultAStar.isSolved());
        assertTrue(resultBFS.isSolved());
        
        // A* should find the same shortest path
        assertEquals(resultBFS.getStepCount(), resultAStar.getStepCount());
        
        // A* should explore significantly fewer states than BFS for complex puzzles
        assertTrue(resultAStar.getStatesExplored() < resultBFS.getStatesExplored(), 
            "A* (" + resultAStar.getStatesExplored() + ") should explore fewer states than BFS (" + resultBFS.getStatesExplored() + ")");
    }
}
