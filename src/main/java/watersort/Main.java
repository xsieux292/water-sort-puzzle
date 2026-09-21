package watersort;

import watersort.generator.GeneratedPuzzle;
import watersort.generator.PuzzleGenerator;
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
 *   java watersort.Main                → interactive mode
 *   java watersort.Main --demo         → solve demo puzzle
 *   java watersort.Main --generate N   → generate puzzle with N colors
 *   java watersort.Main --generate N DIFFICULTY  → generate with difficulty (easy/medium/hard)
 *   java watersort.Main --batch N COUNT → generate COUNT puzzles with N colors
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("🧪 Water Sort Puzzle — Auto-Solver");
        System.out.println("===================================\n");

        if (args.length > 0) {
            switch (args[0]) {
                case "--generate" -> handleGenerate(args);
                case "--batch" -> handleBatch(args);
                case "--demo" -> handleSolve(createDemoPuzzle(), "Demo puzzle");
                case "--file" -> {
                    if (args.length > 1) {
                        try {
                            handleSolve(BoardParser.parseFromJsonFile(args[1]), "File: " + args[1]);
                        } catch (Exception e) {
                            System.err.println("❌ Error parsing file: " + e.getMessage());
                        }
                    } else {
                        System.err.println("❌ Missing file path after --file");
                    }
                }
                case "--image" -> {
                    if (args.length > 1) {
                        watersort.vision.ImageRecognizer recognizer = new watersort.vision.ImageRecognizer();
                        watersort.vision.RecognitionResult res = recognizer.recognize(args[1]);
                        System.out.println(res);
                        if (res.getBoardState() != null) {
                            handleSolve(res.getBoardState(), "Image: " + args[1]);
                        }
                    } else {
                        System.err.println("❌ Missing image path after --image");
                    }
                }
                case "--help" -> printHelp();
                default -> handleSolve(parseInput(), "User puzzle");
            }
        } else {
            handleSolve(parseInput(), "User puzzle");
        }
    }

    // ─── Generate Mode ─────────────────────────────────────────────

    private static void handleGenerate(String[] args) {
        int numColors = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        PuzzleGenerator.Difficulty difficulty = PuzzleGenerator.Difficulty.MEDIUM;

        if (args.length > 2) {
            difficulty = switch (args[2].toLowerCase()) {
                case "easy" -> PuzzleGenerator.Difficulty.EASY;
                case "hard" -> PuzzleGenerator.Difficulty.HARD;
                default -> PuzzleGenerator.Difficulty.MEDIUM;
            };
        }

        System.out.printf("🎲 Generating puzzle: %d colors, difficulty=%s...%n%n",
                numColors, difficulty);

        PuzzleGenerator generator = new PuzzleGenerator(true);
        long startTime = System.currentTimeMillis();
        GeneratedPuzzle puzzle = generator.generate(numColors, difficulty);
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println(puzzle);
        System.out.println();
        System.out.println(puzzle.solutionToString());
        System.out.printf("%n⏱️  Generated in %dms%n", elapsed);
    }

    // ─── Batch Mode ────────────────────────────────────────────────

    private static void handleBatch(String[] args) {
        int numColors = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        int count = args.length > 2 ? Integer.parseInt(args[2]) : 5;

        System.out.printf("🎲 Generating %d puzzles with %d colors...%n%n", count, numColors);

        PuzzleGenerator generator = new PuzzleGenerator(true);
        int totalSteps = 0;
        int minSteps = Integer.MAX_VALUE;
        int maxSteps = 0;

        for (int i = 1; i <= count; i++) {
            GeneratedPuzzle puzzle = generator.generate(numColors);
            int steps = puzzle.getOptimalSteps();
            totalSteps += steps;
            minSteps = Math.min(minSteps, steps);
            maxSteps = Math.max(maxSteps, steps);

            System.out.printf("  Puzzle %2d: %d steps (explored %,d states)%n",
                    i, steps, puzzle.getStatesExplored());
        }

        System.out.printf("%n📊 Summary: %d puzzles generated%n", count);
        System.out.printf("   Min steps: %d%n", minSteps);
        System.out.printf("   Max steps: %d%n", maxSteps);
        System.out.printf("   Avg steps: %.1f%n", (double) totalSteps / count);
    }

    // ─── Solve Mode ────────────────────────────────────────────────

    private static void handleSolve(BoardState board, String label) {
        System.out.printf("📋 %s loaded:%n%n", label);
        System.out.println(board);
        System.out.println("🔍 Solving...\n");

        Solver solver = new watersort.solver.AStarSolver();
        SolveResult result = solver.solve(board);

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

    // ─── Helpers ───────────────────────────────────────────────────

    private static BoardState parseInput() {
        Scanner scanner = new Scanner(System.in);
        return BoardParser.parseFromInput(scanner);
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

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  java watersort.Main                 Interactive mode (manual input)");
        System.out.println("  java watersort.Main --demo          Solve a demo puzzle");
        System.out.println("  java watersort.Main --generate N    Generate puzzle with N colors");
        System.out.println("  java watersort.Main --generate N D  Generate with difficulty (easy/medium/hard)");
        System.out.println("  java watersort.Main --batch N C     Generate C puzzles with N colors");
        System.out.println("  java watersort.Main --file F.json   Solve a puzzle from a JSON file");
        System.out.println("  java watersort.Main --image F.png   Recognize a screenshot, then solve it");
        System.out.println("  java watersort.Main --help          Show this help");
    }
}
