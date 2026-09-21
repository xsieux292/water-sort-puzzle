package watersort.ui.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import watersort.model.Tube;
import watersort.ui.model.ColorScheme;
import watersort.ui.model.TubeColors;
import watersort.ui.util.Fx;

import java.util.HashMap;
import java.util.Map;

/**
 * แผงเลือกสีสำหรับ Edit mode
 *
 * selectedColor: 0 = ยางลบ, 1..N = color ID
 * แต่ละสีแสดงจำนวนบล็อกที่ใช้แล้ว (เช่น 3/4) — สีแดงถ้าจำนวนไม่ครบ 4
 */
public class ColorPickerPanel extends VBox {

    public static final int ERASER = 0;

    private final IntegerProperty selectedColor = new SimpleIntegerProperty(1);
    private final FlowPane swatchPane = new FlowPane(10, 10);
    private final Map<Integer, Swatch> swatches = new HashMap<>();
    private final Button addColor = new Button("+ Color");
    private final Button addTube = new Button("+ Tube");
    private final Button removeTube = new Button("− Tube");

    private int colorCount;
    private Map<Integer, Integer> counts = Map.of();
    private ColorScheme scheme = ColorScheme.DEFAULT;

    public ColorPickerPanel() {
        getStyleClass().addAll("card", "picker-panel");
        setSpacing(12);
        setPadding(new Insets(14));
        setPrefWidth(230);
        setMinWidth(200);

        Label title = new Label("Color Palette");
        title.getStyleClass().add("card-title");
        Label hint = new Label("Left-click a slot to paint it.\nRight-click (or use the eraser) to remove.");
        hint.getStyleClass().add("muted");
        hint.setWrapText(true);

        swatchPane.setPrefWrapLength(190);

        for (Button b : new Button[]{addColor, addTube, removeTube}) {
            b.getStyleClass().add("secondary");
            b.setFocusTraversable(false);
            Fx.hoverScale(b, 1.05, 0.95);
        }
        addColor.setTooltip(new Tooltip("Add another color to the palette"));
        addTube.setTooltip(new Tooltip("Add an empty tube at the end"));
        removeTube.setTooltip(new Tooltip("Remove the last tube"));
        HBox tubeButtons = new HBox(8, addTube, removeTube);

        getChildren().addAll(title, hint, swatchPane, addColor, tubeButtons);
        setColorCount(4);
        selectedColor.addListener((o, was, is) -> refreshSelection());
    }

    public IntegerProperty selectedColorProperty() {
        return selectedColor;
    }

    public int getSelectedColor() {
        return selectedColor.get();
    }

    public int getColorCount() {
        return colorCount;
    }

    /** ตั้งแผนผังสี (เช่น สีจริงจากภาพ) แล้ววาด swatch ใหม่ให้ตรงกัน */
    public void setColorScheme(ColorScheme scheme) {
        this.scheme = scheme != null ? scheme : ColorScheme.DEFAULT;
        if (colorCount > 0) {
            setColorCount(colorCount);
        }
    }

    public void setColorCount(int n) {
        this.colorCount = Math.max(1, Math.min(TubeColors.MAX_COLORS, n));
        swatchPane.getChildren().clear();
        swatches.clear();

        Swatch eraser = new Swatch(ERASER);
        swatches.put(ERASER, eraser);
        swatchPane.getChildren().add(eraser);
        for (int id = 1; id <= colorCount; id++) {
            Swatch s = new Swatch(id);
            swatches.put(id, s);
            swatchPane.getChildren().add(s);
        }
        addColor.setDisable(colorCount >= TubeColors.MAX_COLORS);
        if (selectedColor.get() > colorCount) {
            selectedColor.set(colorCount);
        }
        refreshSelection();
        refreshCounts();
    }

    public void setCounts(Map<Integer, Integer> counts) {
        this.counts = counts;
        refreshCounts();
    }

    public void setOnAddColor(Runnable r) {
        addColor.setOnAction(e -> r.run());
    }

    public void setOnAddTube(Runnable r) {
        addTube.setOnAction(e -> r.run());
    }

    public void setOnRemoveTube(Runnable r) {
        removeTube.setOnAction(e -> r.run());
    }

    private void refreshSelection() {
        swatches.forEach((id, s) -> s.setSelected(id == selectedColor.get()));
    }

    private void refreshCounts() {
        swatches.forEach((id, s) -> {
            if (id != ERASER) {
                s.setCount(counts.getOrDefault(id, 0));
            }
        });
    }

    private class Swatch extends StackPane {
        private final int id;
        private final Circle ring = new Circle(23);
        private final Label countLabel = new Label();

        Swatch(int id) {
            this.id = id;
            getStyleClass().add("swatch");
            setPrefSize(46, 46);
            setMinSize(46, 46);
            setMaxSize(46, 46);

            ring.getStyleClass().add("swatch-ring");
            Circle dot = new Circle(18);
            if (id == ERASER) {
                dot.getStyleClass().add("swatch-eraser-dot");
                Label x = new Label("✕");
                x.getStyleClass().add("swatch-eraser-x");
                getChildren().addAll(ring, dot, x);
                Tooltip.install(this, new Tooltip("Eraser"));
            } else {
                dot.setFill(scheme.color(id));
                dot.getStyleClass().add("swatch-dot");
                countLabel.getStyleClass().add("swatch-count");
                StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT);
                getChildren().addAll(ring, dot, countLabel);
                Tooltip.install(this, new Tooltip(scheme.name(id)));
            }
            setOnMouseClicked(e -> {
                selectedColor.set(this.id);
                Fx.pulse(this);
            });
            Fx.hoverScale(this, 1.1, 0.94);
        }

        void setSelected(boolean selected) {
            ring.getStyleClass().remove("swatch-ring-selected");
            if (selected) {
                ring.getStyleClass().add("swatch-ring-selected");
            }
        }

        void setCount(int count) {
            countLabel.setText(count + "/" + Tube.CAPACITY);
            countLabel.getStyleClass().removeAll("swatch-count-bad", "swatch-count-ok");
            if (count == Tube.CAPACITY) {
                countLabel.getStyleClass().add("swatch-count-ok");
            } else if (count > 0) {
                countLabel.getStyleClass().add("swatch-count-bad");
            }
        }
    }
}
