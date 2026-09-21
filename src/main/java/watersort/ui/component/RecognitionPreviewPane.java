package watersort.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import watersort.vision.RecognitionResult;
import watersort.vision.TubeRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * แสดงภาพต้นฉบับ + ไฮไลท์กรอบหลอดที่ detect ได้ (พร้อมหมายเลข)
 *
 * - กรอบสีเขียว = ปกติ, สีส้ม = หลอดที่มีสีซึ่งจำนวนผิดปกติ (น่าจะ recognize ผิด)
 * - hover กรอบ ↔ hover หลอดใน BoardView เชื่อมกันสองทาง
 */
public class RecognitionPreviewPane extends VBox {

    private final Label confidenceChip = new Label();
    private final Label summary = new Label();
    private final PreviewCanvas canvas = new PreviewCanvas();

    private IntConsumer hoverHandler = i -> { };

    public RecognitionPreviewPane() {
        getStyleClass().addAll("card", "preview-pane");
        setSpacing(10);
        setPadding(new Insets(14));
        setMinWidth(220);

        Label title = new Label("Recognition Preview");
        title.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        confidenceChip.getStyleClass().add("chip");
        HBox header = new HBox(8, title, spacer, confidenceChip);
        header.setAlignment(Pos.CENTER_LEFT);

        summary.setWrapText(true);
        summary.getStyleClass().add("muted");

        VBox.setVgrow(canvas, Priority.ALWAYS);
        getChildren().addAll(header, canvas, summary);
    }

    public void show(Image image, RecognitionResult result) {
        canvas.setContent(image, result.getTubeRegions());

        int pct = (int) Math.round(result.getConfidence() * 100);
        confidenceChip.setText(pct + "% confidence");
        confidenceChip.getStyleClass().removeAll("chip-ok", "chip-warn", "chip-bad");
        confidenceChip.getStyleClass().add(pct >= 90 ? "chip-ok" : pct >= 60 ? "chip-warn" : "chip-bad");

        StringBuilder sb = new StringBuilder();
        sb.append(result.getTubeRegions().size()).append(" tubes detected");
        if (result.getWarnings() != null) {
            result.getWarnings().forEach(w -> sb.append("\n⚠ ").append(w));
        }
        summary.setText(sb.toString());
    }

    public void setOnTubeHover(IntConsumer handler) {
        this.hoverHandler = handler;
    }

    public void setHighlightedTube(int index) {
        canvas.setHighlighted(index);
    }

    /** ทำเครื่องหมายหลอดที่น่าสงสัย (สีจำนวนไม่ครบ 4) */
    public void setSuspectTubes(Set<Integer> suspects) {
        canvas.setSuspects(suspects);
    }

    // ── canvas ──────────────────────────────────────────────────

    private class PreviewCanvas extends Pane {
        private final ImageView imageView = new ImageView();
        private final List<javafx.scene.shape.Rectangle> boxes = new ArrayList<>();
        private final List<StackPane> badges = new ArrayList<>();
        private List<TubeRegion> regions = List.of();
        private Image image;

        PreviewCanvas() {
            getStyleClass().add("preview-canvas");
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            getChildren().add(imageView);
            setMinHeight(160);
        }

        void setContent(Image img, List<TubeRegion> newRegions) {
            getChildren().removeAll(boxes);
            getChildren().removeAll(badges);
            boxes.clear();
            badges.clear();
            this.image = img;
            this.regions = newRegions;
            imageView.setImage(img);

            for (int i = 0; i < regions.size(); i++) {
                final int idx = i;
                javafx.scene.shape.Rectangle box = new javafx.scene.shape.Rectangle();
                box.getStyleClass().add("region-box");
                box.setArcWidth(8);
                box.setArcHeight(8);
                box.setOnMouseEntered(e -> hoverHandler.accept(idx));
                box.setOnMouseExited(e -> hoverHandler.accept(-1));
                Label num = new Label(String.valueOf(i + 1));
                num.getStyleClass().add("region-badge-text");
                StackPane badge = new StackPane(num);
                badge.getStyleClass().add("region-badge");
                badge.setMouseTransparent(true);
                boxes.add(box);
                badges.add(badge);
                getChildren().addAll(box, badge);
            }
            requestLayout();
        }

        void setHighlighted(int index) {
            for (int i = 0; i < boxes.size(); i++) {
                toggle(boxes.get(i), "region-highlight", i == index);
                toggle(badges.get(i), "region-badge-highlight", i == index);
            }
        }

        void setSuspects(Set<Integer> suspects) {
            for (int i = 0; i < boxes.size(); i++) {
                toggle(boxes.get(i), "region-suspect", suspects.contains(i));
            }
        }

        private void toggle(javafx.scene.Node node, String styleClass, boolean on) {
            if (on) {
                if (!node.getStyleClass().contains(styleClass)) {
                    node.getStyleClass().add(styleClass);
                }
            } else {
                node.getStyleClass().remove(styleClass);
            }
        }

        @Override
        protected void layoutChildren() {
            if (image == null || image.getWidth() <= 0) {
                return;
            }
            double iw = image.getWidth();
            double ih = image.getHeight();
            double s = Math.min(getWidth() / iw, getHeight() / ih);
            double ox = (getWidth() - iw * s) / 2;
            double oy = (getHeight() - ih * s) / 2;

            imageView.setFitWidth(iw * s);
            imageView.setFitHeight(ih * s);
            imageView.relocate(ox, oy);

            for (int i = 0; i < regions.size(); i++) {
                java.awt.Rectangle b = regions.get(i).getBounds();
                javafx.scene.shape.Rectangle box = boxes.get(i);
                box.setX(ox + b.x * s);
                box.setY(oy + b.y * s);
                box.setWidth(b.width * s);
                box.setHeight(b.height * s);

                StackPane badge = badges.get(i);
                badge.autosize();
                badge.relocate(ox + b.x * s + b.width * s / 2 - badge.getWidth() / 2,
                        Math.max(0, oy + b.y * s - badge.getHeight() - 2));
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            return 300;
        }

        @Override
        protected double computePrefWidth(double height) {
            return 300;
        }
    }
}
