package watersort.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import watersort.ui.util.Fx;

/**
 * แถบควบคุมของโหมดเล่นเกม: Undo / Hint  ·  Moves & Par  ·  Restart / Solve for me / New Game
 */
public class GameBar extends HBox {

    private final Button undo = new Button("↶ Undo");
    private final Button hint = new Button("Hint");
    private final Button restart = new Button("↺ Restart");
    private final Button solve = new Button("Solve for me");
    private final Button newGame = new Button("New Game");
    private final Label movesLabel = new Label();
    private final Label parLabel = new Label();

    public GameBar() {
        getStyleClass().add("control-bar");
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(12, 18, 14, 18));

        undo.getStyleClass().add("secondary");
        hint.getStyleClass().add("secondary");
        restart.getStyleClass().add("secondary");
        solve.getStyleClass().add("primary");
        newGame.getStyleClass().add("secondary");

        undo.setTooltip(new Tooltip("Take back your last pour"));
        hint.setTooltip(new Tooltip("Highlight a move that leads to a solution"));
        restart.setTooltip(new Tooltip("Start this puzzle over"));
        solve.setTooltip(new Tooltip("Watch the solver finish it from here"));
        newGame.setTooltip(new Tooltip("Generate a different puzzle"));

        for (Button b : new Button[]{undo, hint, restart, solve, newGame}) {
            b.setFocusTraversable(false);
            Fx.hoverScale(b, 1.05, 0.95, () -> !b.isDisabled());
        }

        movesLabel.getStyleClass().add("step-title");
        parLabel.getStyleClass().add("muted");
        HBox stats = new HBox(14, movesLabel, parLabel);
        stats.setAlignment(Pos.CENTER);

        Region left = new Region();
        Region right = new Region();
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox helpers = new HBox(10, undo, hint);
        HBox tail = new HBox(10, restart, solve, newGame);
        helpers.setAlignment(Pos.CENTER);
        tail.setAlignment(Pos.CENTER);
        getChildren().addAll(helpers, left, stats, right, tail);
        setStats(0, 0);
    }

    public Button undoButton() {
        return undo;
    }

    public Button hintButton() {
        return hint;
    }

    public Button restartButton() {
        return restart;
    }

    public Button solveButton() {
        return solve;
    }

    public Button newGameButton() {
        return newGame;
    }

    /** @param par จำนวน move ที่น้อยที่สุดที่เป็นไปได้ (0 = ไม่แสดง) */
    public void setStats(int moves, int par) {
        movesLabel.setText("Moves: " + moves);
        parLabel.setText(par > 0 ? "Best possible: " + par : "");
    }
}
