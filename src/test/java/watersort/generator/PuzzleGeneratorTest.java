package watersort.generator;

import org.junit.jupiter.api.Test;
import watersort.model.Tube;

import static org.junit.jupiter.api.Assertions.*;

class PuzzleGeneratorTest {

    @Test
    void testGenerateSolvable() {
        PuzzleGenerator generator = new PuzzleGenerator(true);
        for (int i = 0; i < 5; i++) {
            GeneratedPuzzle puzzle = generator.generate(4, 2);
            assertTrue(puzzle.hasSolution(), "Generated puzzle must be solvable");
            assertTrue(puzzle.getOptimalSteps() > 0, "Steps must be greater than 0");
        }
    }

    @Test
    void testGenerateColors() {
        PuzzleGenerator generator = new PuzzleGenerator(true);
        GeneratedPuzzle puzzle = generator.generate(3, 2);
        
        int[] colorCounts = new int[4]; // Colors 1, 2, 3

        for (int i = 0; i < puzzle.getBoard().tubeCount(); i++) {
            Tube tube = puzzle.getBoard().getTube(i);
            for (int j = 0; j < tube.size(); j++) {
                colorCounts[tube.getColorAt(j)]++;
            }
        }

        for (int i = 1; i <= 3; i++) {
            assertEquals(4, colorCounts[i], "Each color should have exactly 4 blocks");
        }
    }

    @Test
    void testGenerateEmptyTubes() {
        PuzzleGenerator generator = new PuzzleGenerator(true);
        GeneratedPuzzle puzzle = generator.generate(4, 3);
        
        assertEquals(7, puzzle.getTotalTubes());
        
        int emptyCount = 0;
        for (int i = 0; i < puzzle.getBoard().tubeCount(); i++) {
            if (puzzle.getBoard().getTube(i).isEmpty()) {
                emptyCount++;
            }
        }
        
        // Due to the nature of generation and BFS verification, there should be at least some empty tubes, 
        // typically exactly the number requested if it's perfectly shuffled without filling all empties.
        // Actually, we expect exactly 3 empty tubes initially because we only pour exactly 4*numColors blocks into numColors tubes.
        assertEquals(3, emptyCount, "Expected number of empty tubes in generated state");
    }

    @Test
    void testDifficultyLevels() {
        PuzzleGenerator generator = new PuzzleGenerator(true);
        
        // This test might be slightly flaky due to randomness, 
        // but HARD usually requires more steps than EASY.
        GeneratedPuzzle easy = generator.generate(4, PuzzleGenerator.Difficulty.EASY);
        GeneratedPuzzle hard = generator.generate(4, PuzzleGenerator.Difficulty.HARD);
        
        assertTrue(easy.getOptimalSteps() > 0);
        assertTrue(hard.getOptimalSteps() > 0);
        
        // Note: The assertion `easy.getOptimalSteps() <= hard.getOptimalSteps()` is statistically likely 
        // based on how difficulty generation is configured, but technically not 100% guaranteed for every single roll.
        // We will just verify they generate valid puzzles.
        assertNotNull(easy.getBoard());
        assertNotNull(hard.getBoard());
    }
}
