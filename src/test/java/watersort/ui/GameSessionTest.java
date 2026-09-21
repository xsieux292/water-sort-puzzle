package watersort.ui;

import org.junit.jupiter.api.Test;
import watersort.model.BoardState;
import watersort.ui.model.GameSession;
import watersort.ui.model.GameSession.Pour;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private static BoardState board(int[]... tubes) {
        return BoardState.fromArrays(tubes);
    }

    @Test
    void pouringRulesFollowTheSpec() {
        BoardState s = board(new int[]{1, 2}, new int[]{3, 2}, new int[]{1, 1, 1, 1}, new int[]{}, new int[]{4});
        assertTrue(GameSession.canPour(s, 0, 1), "same top color");
        assertTrue(GameSession.canPour(s, 0, 3), "into empty tube");
        assertFalse(GameSession.canPour(s, 3, 0), "empty source");
        assertFalse(GameSession.canPour(s, 0, 0), "same tube");
        assertFalse(GameSession.canPour(s, 0, 2), "destination is full");
        assertFalse(GameSession.canPour(s, 0, 4), "top colors differ");
        assertFalse(GameSession.canPour(s, 0, 9), "index out of range");
    }

    @Test
    void playerMayMakeMovesTheSolverWouldPrune() {
        // เทสีเดียวทั้งหลอดไปหลอดว่าง solver จะตัดทิ้ง แต่กติกาจริงอนุญาต
        BoardState s = board(new int[]{1, 1}, new int[]{});
        assertTrue(s.getValidMoves().isEmpty());
        assertTrue(GameSession.canPour(s, 0, 1));
    }

    @Test
    void pourMovesAllMatchingBlocksThatFit() {
        GameSession g = new GameSession(board(new int[]{1, 2, 2, 2}, new int[]{2, 2}, new int[]{}), 5);
        Pour p = g.pour(0, 1);
        assertEquals(new Pour(0, 1, 2, 2), p, "only 2 fit in the destination");
        assertEquals(1, g.moves());
        assertEquals(2, g.state().getTube(0).size());
        assertEquals(4, g.state().getTube(1).size());
    }

    @Test
    void illegalPourChangesNothing() {
        GameSession g = new GameSession(board(new int[]{1}, new int[]{2}), 1);
        assertNull(g.pour(0, 1));
        assertEquals(0, g.moves());
        assertFalse(g.canUndo());
    }

    @Test
    void undoRestoresPreviousStateStepByStep() {
        BoardState start = board(new int[]{1, 2}, new int[]{2, 1}, new int[]{}, new int[]{});
        GameSession g = new GameSession(start, 4);
        Pour a = g.pour(0, 2);
        Pour b = g.pour(1, 3);
        assertEquals(2, g.moves());

        assertEquals(b, g.undo());
        assertEquals(1, g.moves());
        assertEquals(start.applyMove(new watersort.model.Move(0, 2)), g.state());
        assertEquals(a, g.undo());
        assertEquals(start, g.state());
        assertNull(g.undo());
    }

    @Test
    void restartGoesBackToTheInitialBoard() {
        BoardState start = board(new int[]{1, 2}, new int[]{2, 1}, new int[]{}, new int[]{});
        GameSession g = new GameSession(start, 4);
        g.pour(0, 2);
        g.restart();
        assertEquals(start, g.state());
        assertEquals(0, g.moves());
        assertFalse(g.canUndo());
        assertEquals(4, g.optimalSteps(), "par is kept");
    }

    @Test
    void winIsDetectedWhenEveryTubeIsSortedOrEmpty() {
        GameSession g = new GameSession(board(new int[]{1, 1, 1}, new int[]{1}, new int[]{}), 1);
        assertFalse(g.isWon());
        g.pour(1, 0);
        assertTrue(g.isWon());
        assertEquals(1, g.moves());
    }
}
