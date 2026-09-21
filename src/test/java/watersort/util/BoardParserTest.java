package watersort.util;

import org.junit.jupiter.api.Test;
import watersort.model.BoardState;
import java.io.File;
import java.io.FileWriter;
import static org.junit.jupiter.api.Assertions.*;

class BoardParserTest {

    @Test
    void testParseFromStrings() {
        String[] input = {
            "R, B",
            "B, R",
            ""
        };
        
        BoardState board = BoardParser.parseFromStrings(input);
        
        assertEquals(3, board.tubeCount());
        
        // R is assigned 1, B is assigned 2 based on order of appearance (R first, then B)
        assertEquals(2, board.getTube(0).size());
        assertEquals(2, board.getTube(1).size());
        assertEquals(0, board.getTube(2).size());
        
        // Let's verify they have different colors
        assertNotEquals(board.getTube(0).getColorAt(0), board.getTube(0).getColorAt(1));
    }

    @Test
    void testParseFromJsonFile() throws Exception {
        // Create a temporary JSON file
        File tempFile = File.createTempFile("puzzle", ".json");
        tempFile.deleteOnExit();
        
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("""
            {
              "tubes": [
                ["R", "B", "R", "G"],
                ["B", "G", "R", "B"],
                [],
                []
              ]
            }
            """);
        }
        
        BoardState board = BoardParser.parseFromJsonFile(tempFile.getAbsolutePath());
        
        assertEquals(4, board.tubeCount());
        assertEquals(4, board.getTube(0).size());
        assertEquals(4, board.getTube(1).size());
        assertEquals(0, board.getTube(2).size());
        assertEquals(0, board.getTube(3).size());
    }

    @Test
    void testParseFromJsonFileInvalid() throws Exception {
        File tempFile = File.createTempFile("invalid_puzzle", ".json");
        tempFile.deleteOnExit();
        
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("""
            {
              "invalid": []
            }
            """);
        }
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            BoardParser.parseFromJsonFile(tempFile.getAbsolutePath());
        });
        
        assertTrue(exception.getMessage().contains("missing 'tubes' array"));
    }
}
