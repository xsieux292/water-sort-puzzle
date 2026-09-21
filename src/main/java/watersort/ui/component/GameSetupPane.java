package watersort.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import watersort.generator.PuzzleGenerator.Difficulty;
import watersort.generator.PuzzleGenerator;
import watersort.ui.util.Fx;

/**
 * ตั้งค่าด่านที่จะให้ Puzzle Generator สร้าง: จำนวนสี + ระดับความยาก
 */
public class GameSetupPane extends VBox {

    public static final int MIN_COLORS = 3;
    public static final int MAX_COLORS = 10;

    @FunctionalInterface
    public interface GenerateHandler {
        void generate(int colors, Difficulty difficulty);
    }

    private final Slider colors = new Slider(MIN_COLORS, MAX_COLORS, 5);
    private final Label summary = new Label();
    private final ToggleGroup difficultyGroup = new ToggleGroup();
    private final ToggleButton easy = difficultyButton("Easy", Difficulty.EASY);
    private final ToggleButton medium = difficultyButton("Medium", Difficulty.MEDIUM);
    private final ToggleButton hard = difficultyButton("Hard", Difficulty.HARD);
    private final Label difficultyHint = new Label();
    private final Button generate = new Button("Generate & Play");
    private final Label error = new Label();

    private GenerateHandler handler = (c, d) -> { };

    public GameSetupPane() {
        setAlignment(Pos.CENTER);
        setSpacing(18);
        setPadding(new Insets(28, 34, 28, 34));
        setMaxWidth(560);
        setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);   // ห่อพอดีเนื้อหา ไม่ยืดเต็มจอ
        getStyleClass().addAll("card", "setup-pane");

        Label title = new Label("New game");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Every puzzle is generated and verified to be solvable.");
        sub.getStyleClass().add("muted");

        Label colorsTitle = new Label("Number of colors");
        colorsTitle.getStyleClass().add("card-title");
        colors.setMajorTickUnit(1);
        colors.setMinorTickCount(0);
        colors.setSnapToTicks(true);
        colors.setShowTickMarks(true);
        colors.setShowTickLabels(true);
        colors.setBlockIncrement(1);
        colors.setFocusTraversable(false);
        colors.setPrefWidth(420);
        colors.valueProperty().addListener((o, was, is) -> refresh());

        Label diffTitle = new Label("Difficulty");
        diffTitle.getStyleClass().add("card-title");
        HBox diffRow = new HBox(10, easy, medium, hard);
        diffRow.setAlignment(Pos.CENTER);
        medium.setSelected(true);
        difficultyGroup.selectedToggleProperty().addListener((o, was, is) -> {
            if (is == null) {
                was.setSelected(true);   // ต้องมีระดับที่เลือกอยู่เสมอ
            }
            refresh();
        });

        summary.getStyleClass().add("step-title");
        difficultyHint.getStyleClass().add("muted");

        generate.getStyleClass().addAll("primary", "big");
        generate.setFocusTraversable(false);
        generate.setOnAction(e -> {
            error.setVisible(false);
            error.setManaged(false);
            handler.generate(selectedColors(), selectedDifficulty());
        });
        Fx.hoverScale(generate, 1.05, 0.96);

        error.getStyleClass().add("error-text");
        error.setWrapText(true);
        error.setVisible(false);
        error.setManaged(false);

        getChildren().addAll(title, sub, colorsTitle, colors, diffTitle, diffRow, difficultyHint, summary, generate, error);
        refresh();
    }

    public void showError(String message) {
        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    public void setOnGenerate(GenerateHandler handler) {
        this.handler = handler;
    }

    public int selectedColors() {
        return (int) Math.round(colors.getValue());
    }

    public Difficulty selectedDifficulty() {
        return (Difficulty) difficultyGroup.getSelectedToggle().getUserData();
    }

    private ToggleButton difficultyButton(String text, Difficulty d) {
        ToggleButton b = new ToggleButton(text);
        b.setUserData(d);
        b.setToggleGroup(difficultyGroup);
        b.getStyleClass().add("secondary");
        b.setFocusTraversable(false);
        b.setPrefWidth(110);
        Fx.hoverScale(b, 1.05, 0.95);
        return b;
    }

    private void refresh() {
        Difficulty d = difficultyGroup.getSelectedToggle() == null ? Difficulty.MEDIUM : selectedDifficulty();
        int empty = PuzzleGenerator.emptyTubesFor(d);
        int c = selectedColors();
        summary.setText(c + " colors  ·  " + (c + empty) + " tubes");
        difficultyHint.setText(switch (d) {
            case EASY -> "Easy: " + empty + " empty tubes — lots of room to maneuver.";
            case MEDIUM -> "Medium: " + empty + " empty tubes, and a solution needs a fair number of moves.";
            case HARD -> "Hard: " + empty + " empty tubes, and the shortest solution is long.";
        });
    }
}
