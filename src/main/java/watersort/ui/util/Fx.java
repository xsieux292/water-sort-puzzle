package watersort.ui.util;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.function.BooleanSupplier;

/**
 * Micro-animation helpers (hover / press / click feedback)
 */
public final class Fx {

    private static final String SCALE_KEY = "fx.scaleAnim";

    private Fx() {
    }

    /** ขยายเล็กน้อยเมื่อ hover และหดเมื่อกด */
    public static void hoverScale(Node node, double hover, double press) {
        hoverScale(node, hover, press, () -> true);
    }

    public static void hoverScale(Node node, double hover, double press, BooleanSupplier enabled) {
        node.setOnMouseEntered(e -> {
            if (enabled.getAsBoolean()) {
                scaleTo(node, hover, 130);
            }
        });
        node.setOnMouseExited(e -> scaleTo(node, 1.0, 130));
        node.setOnMousePressed(e -> {
            if (enabled.getAsBoolean()) {
                scaleTo(node, press, 70);
            }
        });
        node.setOnMouseReleased(e -> scaleTo(node, node.isHover() && enabled.getAsBoolean() ? hover : 1.0, 110));
    }

    private static void scaleTo(Node node, double target, double millis) {
        ScaleTransition st = (ScaleTransition) node.getProperties()
                .computeIfAbsent(SCALE_KEY, k -> new ScaleTransition(Duration.millis(millis), node));
        st.stop();
        st.setDuration(Duration.millis(millis));
        st.setToX(target);
        st.setToY(target);
        st.playFromStart();
    }

    /** เด้งสั้นๆ เป็น feedback ตอนคลิก */
    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(90), node);
        st.setToX(1.12);
        st.setToY(1.12);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    /** สั่นซ้าย-ขวา (ใช้กับ error) */
    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    public static Animation fadeIn(Node node, double millis) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
        ft.setToValue(1);
        ft.play();
        return ft;
    }
}
