package watersort.util;

import watersort.model.BoardState;
import watersort.model.Tube;

import java.util.*;

/**
 * Utility class สำหรับ parse input เป็น BoardState
 * รองรับทั้ง interactive prompt และ JSON file
 */
public class BoardParser {

    /** Color name → int mapping */
    private static final Map<String, Integer> COLOR_MAP = new HashMap<>();

    /**
     * Parse จาก String array เช่น:
     * { "R,B,R,G", "B,G,R,B", "G,R,G,B", "", "" }
     */
    public static BoardState parseFromStrings(String[] tubeStrings) {
        // Build color map dynamically
        COLOR_MAP.clear();
        int nextColorId = 1;

        int[][] tubeArrays = new int[tubeStrings.length][];

        for (int i = 0; i < tubeStrings.length; i++) {
            String s = tubeStrings[i].trim();
            if (s.isEmpty()) {
                tubeArrays[i] = new int[0];
                continue;
            }

            String[] parts = s.split(",");
            tubeArrays[i] = new int[parts.length];
            for (int j = 0; j < parts.length; j++) {
                String colorName = parts[j].trim().toUpperCase();
                if (!COLOR_MAP.containsKey(colorName)) {
                    COLOR_MAP.put(colorName, nextColorId++);
                }
                tubeArrays[i][j] = COLOR_MAP.get(colorName);
            }
        }

        return BoardState.fromArrays(tubeArrays);
    }

    /**
     * Parse จาก Scanner (interactive prompt)
     */
    public static BoardState parseFromInput(Scanner scanner) {
        System.out.print("Enter number of tubes: ");
        int n = Integer.parseInt(scanner.nextLine().trim());

        String[] tubeStrings = new String[n];
        for (int i = 0; i < n; i++) {
            System.out.printf("Tube %d (bottom to top, comma-separated): ", i + 1);
            tubeStrings[i] = scanner.nextLine();
        }

        return parseFromStrings(tubeStrings);
    }

    /**
     * @return mapping ของ color ID กลับเป็นชื่อสี
     */
    public static Map<Integer, String> getColorNames() {
        Map<Integer, String> reversed = new HashMap<>();
        for (Map.Entry<String, Integer> entry : COLOR_MAP.entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }
        return reversed;
    }

    /**
     * Parse จาก JSON file
     * รูปแบบ: { "tubes": [ ["R", "B"], ["B", "R"], [], [] ] }
     */
    public static BoardState parseFromJsonFile(String filePath) throws Exception {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try (java.io.Reader reader = new java.io.FileReader(filePath)) {
            PuzzleJson json = gson.fromJson(reader, PuzzleJson.class);
            if (json == null || json.tubes == null) {
                throw new IllegalArgumentException("Invalid JSON format: missing 'tubes' array.");
            }
            
            String[] tubeStrings = new String[json.tubes.size()];
            for (int i = 0; i < json.tubes.size(); i++) {
                List<String> tube = json.tubes.get(i);
                if (tube == null || tube.isEmpty()) {
                    tubeStrings[i] = "";
                } else {
                    tubeStrings[i] = String.join(",", tube);
                }
            }
            return parseFromStrings(tubeStrings);
        }
    }

    private static class PuzzleJson {
        List<List<String>> tubes;
    }
}
