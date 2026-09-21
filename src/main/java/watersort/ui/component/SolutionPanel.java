package watersort.ui.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * แสดง "Step X/Y: Pour Tube A → Tube B (สี)" + progress bar + สถิติการค้นหา
 */
public class SolutionPanel extends VBox {

    private final Label stepLabel = new Label();
    private final Label moveLabel = new Label();
    private final Label percentLabel = new Label();
    private final Label statsLabel = new Label();
    private final Circle colorChip = new Circle(8);
    private final ProgressBar progress = new ProgressBar(0);

    public SolutionPanel() {
        getStyleClass().addAll("card", "solution-panel");
        setSpacing(8);
        setPadding(new Insets(12, 18, 12, 18));

        stepLabel.getStyleClass().add("step-title");
        moveLabel.getStyleClass().add("step-move");
        colorChip.getStyleClass().add("step-chip");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        statsLabel.getStyleClass().add("muted");
        HBox top = new HBox(10, stepLabel, colorChip, moveLabel, spacer, statsLabel);
        top.setAlignment(Pos.CENTER_LEFT);

        progress.setMaxWidth(Double.MAX_VALUE);
        progress.getStyleClass().add("solution-progress");
        percentLabel.getStyleClass().add("step-percent");
        percentLabel.setMinWidth(90);
        percentLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(progress, Priority.ALWAYS);
        HBox bar = new HBox(12, progress, percentLabel);
        bar.setAlignment(Pos.CENTER);

        getChildren().addAll(top, bar);
    }

    /**
     * @param step   step ปัจจุบัน (0 = ยังไม่เริ่ม)
     * @param total  จำนวน step ทั้งหมด
     * @param moveText ข้อความ move ล่าสุด (null ถ้าอยู่ที่ step 0)
     */
    public void update(int step, int total, String moveText, Color color, String colorName) {
        stepLabel.setText("Step " + step + "/" + total);
        if (moveText == null) {
            moveLabel.setText(total == 0 ? "" : "Press Next or Auto-Play to start");
            colorChip.setVisible(false);
            colorChip.setManaged(false);
        } else {
            moveLabel.setText(moveText + "  (" + colorName + ")");
            colorChip.setFill(color);
            colorChip.setVisible(true);
            colorChip.setManaged(true);
        }
        double target = total == 0 ? 1.0 : (double) step / total;
        new Timeline(new KeyFrame(Duration.millis(260), new KeyValue(progress.progressProperty(), target))).play();
        percentLabel.setText("Progress: " + Math.round(target * 100) + "%");

        getStyleClass().remove("solution-done");
        if (total > 0 && step == total) {
            getStyleClass().add("solution-done");
            moveLabel.setText("Solved! Every tube is sorted.");
            colorChip.setVisible(false);
            colorChip.setManaged(false);
        }
    }

    public void setStats(String text) {
        statsLabel.setText(text);
    }
}
