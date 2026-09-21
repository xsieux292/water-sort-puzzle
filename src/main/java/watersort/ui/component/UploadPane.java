package watersort.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import watersort.ui.util.Fx;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Drag & Drop zone สำหรับรับภาพหน้าจอเกม (PNG / JPG) — คลิกเพื่อ Browse ได้เช่นกัน
 */
public class UploadPane extends StackPane {

    private static final List<String> EXTENSIONS = List.of(".png", ".jpg", ".jpeg");

    private final VBox idleBox = new VBox(10);
    private final VBox loadedBox = new VBox(8);
    private final ImageView thumbnail = new ImageView();
    private final Label fileLabel = new Label();
    private final Label hintLabel = new Label();

    private Consumer<File> onImageChosen = f -> { };

    public UploadPane() {
        getStyleClass().add("upload-zone");
        setMinHeight(230);

        // --- idle content ---
        Circle disc = new Circle(38);
        disc.getStyleClass().add("upload-icon-disc");
        SVGPath arrow = new SVGPath();
        arrow.setContent("M12,3 L4,11 L9.5,11 L9.5,20 L14.5,20 L14.5,11 L20,11 Z");
        arrow.getStyleClass().add("upload-icon-arrow");
        arrow.setScaleX(2.0);
        arrow.setScaleY(2.0);
        StackPane icon = new StackPane(disc, arrow);

        Label title = new Label("Drop your game screenshot here");
        title.getStyleClass().add("upload-title");
        hintLabel.setText("PNG or JPG  ·  or click anywhere to browse");
        hintLabel.getStyleClass().add("muted");
        Button browse = new Button("Browse...");
        browse.getStyleClass().add("secondary");
        browse.setFocusTraversable(false);
        browse.setOnAction(e -> browse());
        Fx.hoverScale(browse, 1.05, 0.96);

        idleBox.setAlignment(Pos.CENTER);
        idleBox.getChildren().addAll(icon, title, hintLabel, browse);

        // --- loaded content ---
        thumbnail.setPreserveRatio(true);
        thumbnail.setFitHeight(200);
        thumbnail.getStyleClass().add("upload-thumb");
        fileLabel.getStyleClass().add("upload-file");
        Label replace = new Label("Click or drop another image to replace");
        replace.getStyleClass().add("muted");
        loadedBox.setAlignment(Pos.CENTER);
        loadedBox.getChildren().addAll(thumbnail, fileLabel, replace);
        loadedBox.setVisible(false);
        loadedBox.setManaged(false);

        getChildren().addAll(idleBox, loadedBox);

        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !(e.getTarget() instanceof Button)) {
                browse();
            }
        });
        setOnDragOver(this::handleDragOver);
        setOnDragExited(e -> setDragging(false));
        setOnDragDropped(this::handleDrop);
        Fx.hoverScale(this, 1.008, 0.995);
    }

    public void setOnImageChosen(Consumer<File> handler) {
        this.onImageChosen = handler;
    }

    /** แสดง thumbnail ของไฟล์ที่เลือกแล้ว */
    public void showSelected(File file) {
        thumbnail.setImage(new Image(file.toURI().toString(), 0, 260, true, true, true));
        fileLabel.setText(file.getName());
        idleBox.setVisible(false);
        idleBox.setManaged(false);
        loadedBox.setVisible(true);
        loadedBox.setManaged(true);
        Fx.fadeIn(loadedBox, 220);
    }

    public void clear() {
        thumbnail.setImage(null);
        loadedBox.setVisible(false);
        loadedBox.setManaged(false);
        idleBox.setVisible(true);
        idleBox.setManaged(true);
    }

    public void showError(String message) {
        hintLabel.setText(message);
        hintLabel.getStyleClass().remove("muted");
        if (!hintLabel.getStyleClass().contains("error-text")) {
            hintLabel.getStyleClass().add("error-text");
        }
        Fx.shake(this);
    }

    // ── internals ───────────────────────────────────────────────

    private void browse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a game screenshot");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (PNG, JPG)", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (file != null) {
            accept(file);
        }
    }

    private void handleDragOver(DragEvent e) {
        if (firstImage(e.getDragboard()) != null) {
            e.acceptTransferModes(TransferMode.COPY);
            setDragging(true);
        }
        e.consume();
    }

    private void handleDrop(DragEvent e) {
        Dragboard db = e.getDragboard();
        File file = firstImage(db);
        boolean ok = file != null;
        if (ok) {
            accept(file);
        } else {
            showError("Unsupported file — please drop a PNG or JPG image.");
        }
        setDragging(false);
        e.setDropCompleted(ok);
        e.consume();
    }

    private void accept(File file) {
        resetHint();
        showSelected(file);
        onImageChosen.accept(file);
    }

    private void resetHint() {
        hintLabel.setText("PNG or JPG  ·  or click anywhere to browse");
        hintLabel.getStyleClass().remove("error-text");
        if (!hintLabel.getStyleClass().contains("muted")) {
            hintLabel.getStyleClass().add("muted");
        }
    }

    private void setDragging(boolean dragging) {
        if (dragging) {
            if (!getStyleClass().contains("upload-zone-active")) {
                getStyleClass().add("upload-zone-active");
            }
        } else {
            getStyleClass().remove("upload-zone-active");
        }
    }

    private static File firstImage(Dragboard db) {
        if (!db.hasFiles()) {
            return null;
        }
        for (File f : db.getFiles()) {
            String name = f.getName().toLowerCase(Locale.ROOT);
            if (EXTENSIONS.stream().anyMatch(name::endsWith)) {
                return f;
            }
        }
        return null;
    }
}
