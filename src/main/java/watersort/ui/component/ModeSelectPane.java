package watersort.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import watersort.ui.util.Fx;

/**
 * หน้าแรก: เลือกโหมดการใช้งาน
 *
 *  1. Image Recognizer — อัปโหลดภาพหน้าจอเกม ระบบอ่านหลอด/สี แล้วหาวิธีแก้ให้
 *  2. Play Game       — ระบบสร้างด่านให้ (Puzzle Generator) แล้วผู้เล่นเล่นเอง
 */
public class ModeSelectPane extends VBox {

    public ModeSelectPane(Runnable onImageMode, Runnable onGameMode) {
        setAlignment(Pos.CENTER);
        setSpacing(26);
        setPadding(new Insets(24));

        Label title = new Label("Choose a mode");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("How would you like to use Water Sort Solver?");
        sub.getStyleClass().add("muted");

        Node imageCard = card(
                "M4,5 L20,5 L20,19 L4,19 Z M4,16 L9,11 L13,15 L16,12 L20,16",
                "Image Recognizer",
                "Upload a screenshot of your game. We detect the tubes and colors, "
                        + "you confirm or fix them, then we solve it step by step.",
                "Upload a screenshot", onImageMode);
        Node gameCard = card(
                "M8,5 L19,12 L8,19 Z",
                "Play Game",
                "We generate a solvable puzzle for you. Pour the tubes yourself — "
                        + "with undo, hints, and a \"solve for me\" button when you're stuck.",
                "Generate a puzzle", onGameMode);

        HBox cards = new HBox(26, imageCard, gameCard);
        cards.setAlignment(Pos.CENTER);
        getChildren().addAll(title, sub, cards);
    }

    private static Node card(String iconPath, String title, String description, String action, Runnable onChosen) {
        Circle disc = new Circle(34);
        disc.getStyleClass().add("upload-icon-disc");
        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);
        icon.getStyleClass().add("mode-icon");
        icon.setScaleX(1.9);
        icon.setScaleY(1.9);
        StackPane iconBox = new StackPane(disc, icon);

        Label name = new Label(title);
        name.getStyleClass().add("mode-title");
        Label desc = new Label(description);
        desc.getStyleClass().add("muted");
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // ปุ่มเป็นแค่ visual — ทั้งการ์ดคลิกได้ (กันเรียก onChosen ซ้ำสองครั้ง)
        Button go = new Button(action);
        go.getStyleClass().add("primary");
        go.setMouseTransparent(true);
        go.setFocusTraversable(false);

        VBox card = new VBox(16, iconBox, name, desc, go);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 28, 28, 28));
        card.setPrefWidth(340);
        card.setMinWidth(260);
        card.setMaxWidth(420);
        card.getStyleClass().addAll("card", "mode-card");
        HBox.setHgrow(card, Priority.SOMETIMES);
        card.setOnMouseClicked(e -> onChosen.run());
        Fx.hoverScale(card, 1.03, 0.98);
        return card;
    }
}
