package watersort.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BoardStateTest {

    @Test
    void testIsGoalTrue() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 1, 1},
            {2, 2, 2, 2},
            {}
        });
        assertTrue(board.isGoal());
    }

    @Test
    void testIsGoalFalse() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 1, 2},
            {2, 2, 2, 1},
            {}
        });
        assertFalse(board.isGoal());

        BoardState boardNotFull = BoardState.fromArrays(new int[][]{
            {1, 1, 1},
            {2, 2, 2, 2},
            {}
        });
        assertFalse(boardNotFull.isGoal());
    }

    @Test
    void testGetValidMoves() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2},
            {3, 2},
            {1, 1, 1, 1},
            {}
        });

        List<Move> moves = board.getValidMoves();

        // rule 1: source != dest
        // rule 2: source not empty (tube 3 is empty)
        // rule 3: dest not full (tube 2 is full)
        // rule 4: top color match or dest is empty
        
        // expected valid moves:
        // Tube 0 (top 2) -> Tube 1 (top 2)
        // Tube 0 (top 2) -> Tube 3 (empty)
        // Tube 1 (top 2) -> Tube 0 (top 2)
        // Tube 1 (top 2) -> Tube 3 (empty)
        // Tube 2 (top 1) - skip because it's sorted

        assertTrue(moves.contains(new Move(0, 1)));
        assertTrue(moves.contains(new Move(0, 3)));
        assertTrue(moves.contains(new Move(1, 0)));
        assertTrue(moves.contains(new Move(1, 3)));

        // check pruning (tube 2 is sorted)
        for (Move move : moves) {
            assertNotEquals(2, move.source());
        }
    }

    @Test
    void testPruningSkipCompleted() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1, 1, 1},
            {}
        });
        List<Move> moves = board.getValidMoves();
        assertTrue(moves.isEmpty());
    }

    @Test
    void testPruningIdenticalEmpties() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2},
            {},
            {}
        });
        List<Move> moves = board.getValidMoves();
        // Should only generate move to ONE empty tube
        assertEquals(1, moves.size());
        assertEquals(new Move(0, 1), moves.get(0));
    }

    @Test
    void testPruningUniformToEmpty() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 1},
            {}
        });
        List<Move> moves = board.getValidMoves();
        assertTrue(moves.isEmpty());
    }

    @Test
    void testApplyMove() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2},
            {3}
        });

        BoardState next = board.applyMove(new Move(0, 1));
        
        // Next state should have tube 0: {1}, tube 1: {3, 2}
        assertEquals(1, next.getTube(0).size());
        assertEquals(1, next.getTube(0).topColor());
        assertEquals(2, next.getTube(1).size());
        assertEquals(2, next.getTube(1).topColor());
    }

    @Test
    void testApplyMoveMultiPour() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2, 2},
            {}
        });

        BoardState next = board.applyMove(new Move(0, 1));
        
        // Multi-pour: should move both '2's
        assertEquals(1, next.getTube(0).size());
        assertEquals(1, next.getTube(0).topColor());
        assertEquals(2, next.getTube(1).size());
        assertEquals(2, next.getTube(1).topColor());
    }

    @Test
    void testImmutability() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2},
            {}
        });

        BoardState next = board.applyMove(new Move(0, 1));

        // Original board should not change
        assertEquals(2, board.getTube(0).size());
        assertEquals(0, board.getTube(1).size());
        assertNotEquals(board, next);
    }

    @Test
    void testFromArrays() {
        BoardState board = BoardState.fromArrays(new int[][]{
            {1, 2},
            {}
        });
        assertEquals(2, board.tubeCount());
        assertEquals(2, board.getTube(0).size());
        assertEquals(0, board.getTube(1).size());
    }

    @Test
    void testEqualsAndHashCode() {
        BoardState board1 = BoardState.fromArrays(new int[][]{{1, 2}, {}});
        BoardState board2 = BoardState.fromArrays(new int[][]{{1, 2}, {}});
        BoardState board3 = BoardState.fromArrays(new int[][]{{1}, {2}});

        assertEquals(board1, board1);
        assertEquals(board1, board2);
        assertNotEquals(board1, board3);
        assertNotEquals(board1, null);
        assertNotEquals(board1, new Object());

        assertEquals(board1.hashCode(), board2.hashCode());
        assertNotEquals(board1.hashCode(), board3.hashCode());
    }
}
