package watersort.ui;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * JavaFX entry point — Water Sort Solver (Desktop GUI)
 *
 * เริ่มด้วย Dark theme; สลับ Light/Dark ได้จากปุ่มมุมขวาบน
 */
public class WaterSortApp extends Application {

    private static final String BASE_CSS = "/watersort/ui/app.css";
    private static final String DARK_CSS = "/watersort/ui/dark.css";
    private static final String LIGHT_CSS = "/watersort/ui/light.css";

    private final BooleanProperty darkTheme = new SimpleBooleanProperty(true);

    @Override
    public void start(Stage stage) {
        MainView root = new MainView(darkTheme);
        Scene scene = new Scene(root, 1280, 820);
        applyTheme(scene);
        darkTheme.addListener((obs, was, isDark) -> applyTheme(scene));

        stage.setTitle("Water Sort Solver");
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().setAll(
                css(BASE_CSS),
                css(darkTheme.get() ? DARK_CSS : LIGHT_CSS));
    }

    private static String css(String path) {
        return Objects.requireNonNull(WaterSortApp.class.getResource(path), "Missing stylesheet " + path).toExternalForm();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
