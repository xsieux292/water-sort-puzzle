package watersort.ui.component;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import watersort.ui.util.Fx;

/**
 * แถบปุ่มควบคุม: Edit / Confirm / Solve  ·  Prev / Auto-Play / Next  ·  Reset + Speed
 * (ตัว ControlBar เพียงสร้างปุ่ม — logic การเปิด/ปิดปุ่มอยู่ที่ MainView)
 */
public class ControlBar extends HBox {

    private final ToggleButton edit = new ToggleButton("✎ Edit");
    private final Button confirm = new Button("✓ Confirm");
    private final Button solve = new Button("Solve");
    private final Button prev = new Button("◀ Prev");
    private final ToggleButton play = new ToggleButton("▶ Auto-Play");
    private final Button next = new Button("Next ▶");
    private final Button reset = new Button("↺ Reset");
    private final Button backToGame = new Button("◀ Back to Game");
    private final HBox setup;
    private final Slider speed = new Slider(0.5, 4.0, 1.0);
    private final Label speedLabel = new Label("1.0×");

    public ControlBar() {
        getStyleClass().add("control-bar");
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(12, 18, 14, 18));

        confirm.getStyleClass().add("success");
        solve.getStyleClass().add("primary");
        play.getStyleClass().add("primary");
        for (Button b : new Button[]{prev, next, reset}) {
            b.getStyleClass().add("secondary");
        }
        edit.getStyleClass().add("secondary");

        edit.setTooltip(new Tooltip("Edit colors manually"));
        confirm.setTooltip(new Tooltip("Lock the board and continue"));
        solve.setTooltip(new Tooltip("Find the shortest solution"));
        prev.setTooltip(new Tooltip("Previous step  (←)"));
        next.setTooltip(new Tooltip("Next step  (→)"));
        play.setTooltip(new Tooltip("Play all steps automatically  (Space)"));
        reset.setTooltip(new Tooltip("Reset the board / go back to step 0"));

        for (var control : new javafx.scene.control.ButtonBase[]{edit, confirm, solve, prev, play, next, reset}) {
            control.setFocusTraversable(false);   // ให้ปุ่มลัดคีย์บอร์ดทำงานที่ระดับหน้าต่าง
            Fx.hoverScale(control, 1.05, 0.95, () -> !control.isDisabled());
        }

        speed.setPrefWidth(120);
        speed.setFocusTraversable(false);
        speed.setBlockIncrement(0.5);
        speedLabel.getStyleClass().add("muted");
        speedLabel.setMinWidth(34);
        speed.valueProperty().addListener((o, was, is) -> speedLabel.setText(String.format("%.1f×", is.doubleValue())));
        Label speedTitle = new Label("Speed");
        speedTitle.getStyleClass().add("muted");
        HBox speedBox = new HBox(8, speedTitle, speed, speedLabel);
        speedBox.setAlignment(Pos.CENTER);

        Region left = new Region();
        Region right = new Region();
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        backToGame.getStyleClass().add("secondary");
        backToGame.setFocusTraversable(false);
        backToGame.setVisible(false);
        backToGame.setManaged(false);
        backToGame.setTooltip(new Tooltip("Return to your game where you left off"));
        Fx.hoverScale(backToGame, 1.05, 0.95);
        setup = new HBox(10, edit, confirm, solve, backToGame);
        HBox nav = new HBox(10, prev, play, next);
        HBox tail = new HBox(14, reset, speedBox);
        setup.setAlignment(Pos.CENTER);
        nav.setAlignment(Pos.CENTER);
        tail.setAlignment(Pos.CENTER);

        getChildren().addAll(setup, left, nav, right, tail);
    }

    public Button backToGameButton() {
        return backToGame;
    }

    /** โหมดเล่นเกม (กด Solve for me แล้วดู solution): ซ่อน Edit/Confirm/Solve แล้วแสดง "Back to Game" แทน */
    public void setGameMode(boolean game) {
        for (var b : new javafx.scene.Node[]{edit, confirm, solve}) {
            b.setVisible(!game);
            b.setManaged(!game);
        }
        backToGame.setVisible(game);
        backToGame.setManaged(game);
    }

    public ToggleButton editButton() {
        return edit;
    }

    public Button confirmButton() {
        return confirm;
    }

    public Button solveButton() {
        return solve;
    }

    public Button prevButton() {
        return prev;
    }

    public ToggleButton playButton() {
        return play;
    }

    public Button nextButton() {
        return next;
    }

    public Button resetButton() {
        return reset;
    }

    /** ตัวคูณความเร็ว Auto-Play / animation (0.5× – 4×) */
    public DoubleProperty speedProperty() {
        return speed.valueProperty();
    }

    public void setPlaying(boolean playing) {
        play.setSelected(playing);
        play.setText(playing ? "⏸ Pause" : "▶ Auto-Play");
    }
}
