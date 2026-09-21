package watersort;

import watersort.model.BoardState;
import watersort.model.Move;
import watersort.solver.BFSSolver;
import watersort.solver.Solver;
import watersort.solver.SolveResult;
import watersort.util.BoardParser;

import java.util.Scanner;

/**
 * CLI Entry Point สำหรับ Water Sort Puzzle Solver
 *
 * Usage:
 *   java watersort.Main          → interactive mode
 *   java watersort.Main --demo   → run demo puzzle
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("🧪 Water Sort Puzzle — Auto-Solver");
        System.out.println("===================================\n");

        BoardState board;

        if (args.length > 0 && args[0].equals("--demo")) {
            board = createDemoPuzzle();
            System.out.println("📋 Demo puzzle loaded:\n");
        } else {
            Scanner scanner = new Scanner(System.in);
            board = BoardParser.parseFromInput(scanner);
            System.out.println("\n📋 Puzzle loaded:\n");
        }

        System.out.println(board);

        // Solve
        System.out.println("🔍 Solving...\n");

        Solver solver = new BFSSolver();
        SolveResult result = solver.solve(board);

        // Display result
        System.out.println(result);

        if (result.isSolved()) {
            System.out.println();
            int step = 1;
            for (Move move : result.getMoves()) {
                System.out.printf("  Step %2d: %s%n", step++, move);
            }
            System.out.println("\n🎉 All tubes sorted!");
        }

        System.out.printf("%n📊 Stats: %,d states explored in %dms%n",
                result.getStatesExplored(), result.getTimeMillis());
    }

    /**
     * ด่านตัวอย่างสำหรับทดสอบ (5 หลอด, 3 สี)
     * สี: 1=Red, 2=Blue, 3=Green
     */
    private static BoardState createDemoPuzzle() {
        return BoardState.fromArrays(new int[][]{
                {1, 2, 1, 3},   // Tube 1: [R, B, R, G]
                {2, 3, 1, 2},   // Tube 2: [B, G, R, B]
                {3, 1, 3, 2},   // Tube 3: [G, R, G, B]
                {},              // Tube 4: empty
                {}               // Tube 5: empty
        });
    }
}
