package watersort.ui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import watersort.model.BoardState;
import watersort.model.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * สถานะการเล่น solution ทีละ step
 *
 * เก็บ BoardState ของทุก step ไว้ล่วงหน้า (states[0] = ตั้งต้น, states[n] = จบ)
 * ทำให้ Prev/Next เป็นแค่การเลื่อน index — ไม่ต้อง "undo" move
 */
public class PlaybackModel {

    private final List<Move> moves;
    private final List<BoardState> states;
    private final IntegerProperty index = new SimpleIntegerProperty(0);

    public PlaybackModel(BoardState initial, List<Move> moves) {
        this.moves = Collections.unmodifiableList(new ArrayList<>(moves));
        List<BoardState> all = new ArrayList<>(moves.size() + 1);
        BoardState current = initial;
        all.add(current);
        for (Move move : moves) {
            current = current.applyMove(move);
            all.add(current);
        }
        this.states = Collections.unmodifiableList(all);
    }

    public int stepCount() {
        return moves.size();
    }

    public List<Move> moves() {
        return moves;
    }

    public BoardState stateAt(int i) {
        return states.get(i);
    }

    public BoardState currentState() {
        return states.get(index.get());
    }

    public int index() {
        return index.get();
    }

    public ReadOnlyIntegerProperty indexProperty() {
        return index;
    }

    public boolean canNext() {
        return index.get() < moves.size();
    }

    public boolean canPrev() {
        return index.get() > 0;
    }

    public void goTo(int i) {
        if (i < 0 || i > moves.size()) {
            throw new IndexOutOfBoundsException("Step " + i + " not in [0," + moves.size() + "]");
        }
        index.set(i);
    }

    /** Move ที่เพิ่งถูกเล่นไป (null ถ้ายังอยู่ที่ step 0) */
    public Move lastMove() {
        return index.get() == 0 ? null : moves.get(index.get() - 1);
    }

    /** จำนวนบล็อกที่ถูกเทใน move ที่ moveIndex (0-based) */
    public int pourCount(int moveIndex) {
        Move m = moves.get(moveIndex);
        return states.get(moveIndex).getTube(m.source()).size()
                - states.get(moveIndex + 1).getTube(m.source()).size();
    }

    /** สีที่ถูกเทใน move ที่ moveIndex (0-based) */
    public int pourColor(int moveIndex) {
        Move m = moves.get(moveIndex);
        return states.get(moveIndex).getTube(m.source()).topColor();
    }

    public double progress() {
        return moves.isEmpty() ? 1.0 : (double) index.get() / moves.size();
    }
}
