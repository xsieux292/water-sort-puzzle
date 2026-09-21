package watersort;

import watersort.ui.WaterSortApp;
import java.util.Arrays;

public class Launcher {
    public static void main(String[] args) {
        // If there are arguments, and they are not JavaFX specific, we can route to CLI
        // But for simplicity, let's just launch the JavaFX app if no args or if it's UI.
        // Actually, let's just mimic what Main does, but default to GUI.
        
        if (args.length > 0 && !args[0].startsWith("--javafx")) {
            Main.main(args);
        } else {
            WaterSortApp.main(args);
        }
    }
}
