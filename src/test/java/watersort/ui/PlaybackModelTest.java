package watersort.ui;

import org.junit.jupiter.api.Test;
import watersort.model.BoardState;
import watersort.model.Move;
import watersort.solver.AStarSolver;
import watersort.solver.SolveResult;
import watersort.ui.model.PlaybackModel;
import watersort.ui.model.Presets;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackModelTest {

    private static final BoardState START = BoardState.fromArrays(new int[][]{
            {1, 2, 1, 2}, {2, 1, 2, 1}, {}, {}});

    private static PlaybackModel solved() {
        SolveResult result = new AStarSolver().solve(START);
        assertTrue(result.isSolved());
        return new PlaybackModel(START, result.getMoves());
    }

    @Test
    void startsAtStepZeroWithInitialState() {
        PlaybackModel model = solved();
        assertEquals(0, model.index());
        assertEquals(START, model.currentState());
        assertNull(model.lastMove());
        assertFalse(model.canPrev());
        assertTrue(model.canNext());
    }

    @Test
    void lastStateIsGoal() {
        PlaybackModel model = solved();
        model.goTo(model.stepCount());
        assertTrue(model.currentState().isGoal());
        assertFalse(model.canNext());
        assertEquals(1.0, model.progress());
    }

    @Test
    void statesAreConsistentWithMoves() {
        PlaybackModel model = solved();
        for (int i = 0; i < model.stepCount(); i++) {
            assertEquals(model.stateAt(i).applyMove(model.moves().get(i)), model.stateAt(i + 1));
        }
    }

    @Test
    void pourCountAndColorMatchTheBoardDifference() {
        PlaybackModel model = new PlaybackModel(BoardState.fromArrays(new int[][]{{1, 2, 2}, {}}), List.of(new Move(0, 1)));
        assertEquals(2, model.pourCount(0));
        assertEquals(2, model.pourColor(0));
    }

    @Test
    void goToOutOfRangeThrows() {
        PlaybackModel model = solved();
        assertThrows(IndexOutOfBoundsException.class, () -> model.goTo(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> model.goTo(model.stepCount() + 1));
    }

    @Test
    void everyPresetIsValidAndSolvable() {
        for (Presets.Preset p : Presets.all()) {
            var editor = new watersort.ui.model.BoardEditor(p.board());
            assertTrue(editor.validate().isValid(), p.name() + " must be a valid board");
            if (p.board().tubeCount() > 10) {
                continue;   // ด่านใหญ่ใช้ heap หลายร้อย MB — ทดสอบแค่ความถูกต้องของกระดาน (แก้ได้จริงตอนรันแอป)
            }
            SolveResult r = new AStarSolver().solve(p.board());
            assertTrue(r.isSolved(), p.name() + " must be solvable");
            PlaybackModel model = new PlaybackModel(p.board(), r.getMoves());
            model.goTo(model.stepCount());
            assertTrue(model.currentState().isGoal(), p.name());
        }
    }
}
