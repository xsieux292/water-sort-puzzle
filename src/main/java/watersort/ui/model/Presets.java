package watersort.ui.model;

import watersort.model.BoardState;
import watersort.model.Tube;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ด่านตัวอย่างสำหรับลองใช้งาน (deterministic — สุ่มด้วย seed คงที่)
 */
public final class Presets {

    public record Preset(String name, BoardState board) {
    }

    private Presets() {
    }

    public static List<Preset> all() {
        return List.of(
                new Preset("Demo — 3 colors", BoardState.fromArrays(new int[][]{
                        {1, 2, 1, 3}, {2, 3, 1, 2}, {3, 1, 3, 2}, {}, {}})),
                new Preset("Easy — 5 colors", shuffled(5, 2, 11)),
                new Preset("Medium — 8 colors", shuffled(8, 2, 7)),
                new Preset("Big board — 12 colors (14 tubes)", shuffled(12, 2, 3))
        );
    }

    static BoardState shuffled(int colors, int emptyTubes, long seed) {
        List<Integer> blocks = new ArrayList<>();
        for (int c = 1; c <= colors; c++) {
            for (int i = 0; i < Tube.CAPACITY; i++) {
                blocks.add(c);
            }
        }
        java.util.Collections.shuffle(blocks, new Random(seed));

        int[][] tubes = new int[colors + emptyTubes][];
        for (int t = 0; t < colors; t++) {
            tubes[t] = new int[Tube.CAPACITY];
            for (int s = 0; s < Tube.CAPACITY; s++) {
                tubes[t][s] = blocks.get(t * Tube.CAPACITY + s);
            }
        }
        for (int t = colors; t < tubes.length; t++) {
            tubes[t] = new int[0];
        }
        return BoardState.fromArrays(tubes);
    }
}
