package watersort.ui;

import org.junit.jupiter.api.Test;
import watersort.model.BoardState;
import watersort.ui.model.BoardEditor;
import watersort.ui.model.Validation;

import static org.junit.jupiter.api.Assertions.*;

class BoardEditorTest {

    @Test
    void paintOnEmptySlotAppendsOnTop() {
        BoardEditor editor = BoardEditor.blank(3);
        assertTrue(editor.paint(0, 3, 2));            // คลิกช่องบนสุดของหลอดว่าง → ต่อจากก้นหลอด (ไม่มีช่องว่างตรงกลาง)
        assertEquals(java.util.List.of(2), editor.tube(0));
    }

    @Test
    void paintOnFilledSlotReplacesColor() {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[][]{{1, 2, 3}, {}}));
        assertTrue(editor.paint(0, 1, 5));
        assertEquals(java.util.List.of(1, 5, 3), editor.tube(0));
        assertFalse(editor.paint(0, 1, 5), "same color → no change");
    }

    @Test
    void paintOnFullTubeBeyondCapacityIsRejected() {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[][]{{1, 1, 1, 1}, {}}));
        assertEquals(4, editor.tube(0).size());
        assertFalse(editor.paint(0, 4, 2));           // slot นอกช่วง = ไม่มีอะไรเปลี่ยน
        assertEquals(4, editor.tube(0).size());
    }

    @Test
    void eraseRemovesBlockAndShiftsDown() {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[][]{{1, 2, 3}, {}}));
        assertTrue(editor.erase(0, 0));
        assertEquals(java.util.List.of(2, 3), editor.tube(0));
        assertFalse(editor.erase(0, 3), "erasing an empty slot is a no-op");
    }

    @Test
    void tubeCountIsBounded() {
        BoardEditor editor = BoardEditor.blank(BoardEditor.MIN_TUBES);
        assertFalse(editor.removeLastTube());
        assertTrue(editor.addTube());
        assertEquals(BoardEditor.MIN_TUBES + 1, editor.tubeCount());
    }

    @Test
    void validBoardHasNoErrors() {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[][]{
                {1, 2, 1, 2}, {2, 1, 2, 1}, {}, {}}));
        Validation v = editor.validate();
        assertTrue(v.isValid());
        assertTrue(v.warnings().isEmpty());
        assertEquals(4, v.colorCounts().get(1));
    }

    @Test
    void wrongColorCountIsReportedWithSuspectTubes() {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[][]{
                {1, 2, 1, 2}, {2, 1, 2}, {}, {}}));
        Validation v = editor.validate();
        assertFalse(v.isValid());
        assertEquals(1, v.errors().size());           // สี 1 มี 3 บล็อก (ขาดไป 1), สี 2 ครบ 4
        assertTrue(v.suspectTubes().containsAll(java.util.Set.of(0, 1)));
        assertFalse(v.suspectTubes().contains(2));
    }

    @Test
    void fewEmptyTubesIsOnlyAWarning() {
        BoardEditor editor = new BoardEditor(BoardState.fromArrays(new int[][]{
                {1, 2, 1, 2}, {2, 1, 2, 1}, {}}));
        Validation v = editor.validate();
        assertTrue(v.isValid());
        assertEquals(1, v.warnings().size());
    }

    @Test
    void emptyBoardIsInvalid() {
        assertFalse(BoardEditor.blank(4).validate().isValid());
    }

    @Test
    void toBoardStateRoundTrips() {
        BoardState original = BoardState.fromArrays(new int[][]{{1, 2, 3}, {3, 2}, {}});
        assertEquals(original, new BoardEditor(original).toBoardState());
    }
}
