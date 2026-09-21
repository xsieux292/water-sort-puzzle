package watersort.ui.component;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import watersort.model.Tube;
import watersort.ui.model.TubeColors;
import watersort.ui.util.Fx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * หลอดแก้ว 1 หลอด — วาดด้วย Rectangle stack (1 Rectangle ต่อ 1 บล็อกสี)
 *
 * สถานะที่แสดง = colors[] (ก้นหลอด → บนสุด) + level (ระดับน้ำแบบต่อเนื่อง หน่วย = จำนวนบล็อก)
 * การ animate เทน้ำทำโดยเปลี่ยน level เท่านั้น: ชั้นบนสุดจะหดลงก่อน (drain) หรือโตขึ้นก่อน (fill)
 */
public class TubeView extends Pane {

    public static final double W = 58;
    public static final double SLOT = 42;
    public static final double TOP = 10;
    public static final double INSET = 4;
    public static final double BODY_H = TOP + Tube.CAPACITY * SLOT + INSET;
    public static final double LABEL_H = 28;
    public static final double TOTAL_H = BODY_H + LABEL_H;
    public static final double LIQUID_BOTTOM = BODY_H - INSET;
    private static final double SELECT_LIFT = 16;

    @FunctionalInterface
    public interface SlotClickHandler {
        void onSlotClick(int tube, int slot, MouseButton button);
    }

    private final Map<Integer, LinearGradient> gradientCache = new HashMap<>();
    private java.util.function.IntFunction<Color> colorSource = TubeColors::color;

    private final int index;
    private final Pane body = new Pane();
    private final Pane liquidLayer = new Pane();
    private final Rectangle[] layers = new Rectangle[Tube.CAPACITY];
    private final Rectangle[] slotOutlines = new Rectangle[Tube.CAPACITY];
    private final Rectangle[] hitAreas = new Rectangle[Tube.CAPACITY];
    private final Rectangle ghost = new Rectangle();
    private final SVGPath outline = new SVGPath();
    private final Rotate pourRotate = new Rotate(0, W / 2, 0);
    private final Label label;
    private final DoubleProperty level = new SimpleDoubleProperty();

    private int[] colors = new int[0];
    private boolean editable;
    private boolean hoverEnabled = true;
    private int hoveredSlot = -1;
    private Color ghostPaint;
    private boolean ghostErase;
    private SlotClickHandler slotHandler;
    private java.util.function.IntConsumer tubeClickHandler;
    private boolean selected;
    private javafx.animation.TranslateTransition liftAnim;

    public TubeView(int index) {
        this.index = index;
        getStyleClass().add("tube-view");
        setPrefSize(W, TOTAL_H);
        setMinSize(W, TOTAL_H);
        setMaxSize(W, TOTAL_H);

        body.setPrefSize(W, BODY_H);
        body.setMinSize(W, BODY_H);
        body.setMaxSize(W, BODY_H);
        body.getTransforms().add(pourRotate);

        SVGPath glass = new SVGPath();
        glass.setContent(uPath(0, true));
        glass.getStyleClass().add("tube-glass");

        liquidLayer.setPrefSize(W, BODY_H);
        SVGPath clip = new SVGPath();
        clip.setContent(uPath(INSET, true));
        liquidLayer.setClip(clip);

        for (int i = 0; i < Tube.CAPACITY; i++) {
            Rectangle outlineRect = new Rectangle(INSET + 3, slotTop(i) + 3, W - 2 * INSET - 6, SLOT - 6);
            outlineRect.setArcWidth(10);
            outlineRect.setArcHeight(10);
            outlineRect.getStyleClass().add("slot-outline");
            outlineRect.setMouseTransparent(true);
            outlineRect.setVisible(false);
            slotOutlines[i] = outlineRect;
            liquidLayer.getChildren().add(outlineRect);
        }
        for (int i = 0; i < Tube.CAPACITY; i++) {
            Rectangle r = new Rectangle(INSET, LIQUID_BOTTOM, W - 2 * INSET, 0);
            r.setStroke(Color.rgb(0, 0, 0, 0.22));
            r.setStrokeType(StrokeType.INSIDE);
            r.setStrokeWidth(1);
            r.setMouseTransparent(true);
            layers[i] = r;
            liquidLayer.getChildren().add(r);
        }
        ghost.setX(INSET);
        ghost.setWidth(W - 2 * INSET);
        ghost.setHeight(SLOT);
        ghost.setMouseTransparent(true);
        ghost.setVisible(false);
        ghost.setStrokeWidth(2);
        liquidLayer.getChildren().add(ghost);

        outline.setContent(uPath(0, false));
        outline.getStyleClass().add("tube-outline");
        outline.setMouseTransparent(true);

        Rectangle shine = new Rectangle(9, 14, 5, BODY_H - 46);
        shine.setArcWidth(5);
        shine.setArcHeight(5);
        shine.getStyleClass().add("tube-shine");
        shine.setMouseTransparent(true);

        body.getChildren().addAll(glass, liquidLayer, outline, shine);

        for (int i = 0; i < Tube.CAPACITY; i++) {
            final int slot = i;
            double top = i == Tube.CAPACITY - 1 ? 0 : slotTop(i);
            double bottom = i == 0 ? BODY_H : slotTop(i) + SLOT;
            Rectangle hit = new Rectangle(0, top, W, bottom - top);
            hit.setFill(Color.TRANSPARENT);
            hit.setMouseTransparent(true);
            hit.hoverProperty().addListener((obs, was, is) -> {
                hoveredSlot = is ? slot : (hoveredSlot == slot ? -1 : hoveredSlot);
                refreshGhost();
            });
            hit.setOnMouseClicked(e -> {
                if (editable && slotHandler != null) {
                    slotHandler.onSlotClick(this.index, slot, e.getButton());
                    Fx.pulse(this);
                }
            });
            hitAreas[i] = hit;
            body.getChildren().add(hit);
        }

        label = new Label(String.valueOf(index + 1));
        label.getStyleClass().add("tube-label");
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(W);
        label.relocate(0, BODY_H + 6);

        getChildren().addAll(body, label);

        level.addListener(obs -> render());
        Fx.hoverScale(this, 1.06, 0.97, () -> hoverEnabled && !editable);
        setOnMouseClicked(e -> {
            if (editable) {
                return;
            }
            if (tubeClickHandler != null) {
                tubeClickHandler.accept(this.index);   // โหมดเล่นเกม: ให้ MainView จัดการ (เลือก/เท)
            } else if (hoverEnabled) {
                Fx.pulse(this);
            }
        });
        render();
    }

    // ── Public API ──────────────────────────────────────────────

    public int getIndex() {
        return index;
    }

    /** แสดงเนื้อหาแบบนิ่ง (level = จำนวนบล็อก) */
    public void setContents(int[] newColors) {
        this.colors = newColors.clone();
        level.set(colors.length);
        render();
        updateDoneState();
        refreshGhost();
    }

    public int[] getContents() {
        return colors.clone();
    }

    public int size() {
        return colors.length;
    }

    public boolean isSortedTube() {
        if (colors.length != Tube.CAPACITY) {
            return false;
        }
        for (int c : colors) {
            if (c != colors[0]) {
                return false;
            }
        }
        return true;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        for (Rectangle hit : hitAreas) {
            hit.setMouseTransparent(!editable);
        }
        if (!editable) {
            hoveredSlot = -1;
        }
        render();
        refreshGhost();
    }

    public void setSlotClickHandler(SlotClickHandler handler) {
        this.slotHandler = handler;
    }

    /** สีที่จะแสดงเป็นเงา "preview" เมื่อ hover ช่องใน edit mode (erase = โหมดยางลบ) */
    public void setGhostPaint(Color paint, boolean erase) {
        this.ghostPaint = paint;
        this.ghostErase = erase;
        refreshGhost();
    }

    /** คลิกที่ตัวหลอด (นอก edit mode) — ใช้ในโหมดเล่นเกมเพื่อเลือกหลอด/เท */
    public void setOnTubeClick(java.util.function.IntConsumer handler) {
        this.tubeClickHandler = handler;
    }

    /** ยกหลอดขึ้นเล็กน้อยเพื่อบอกว่าถูกเลือก (ต้นทางของการเท) */
    public void setSelected(boolean on) {
        if (selected == on) {
            return;
        }
        selected = on;
        if (liftAnim != null) {
            liftAnim.stop();
        }
        liftAnim = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(130), body);
        liftAnim.setToY(on ? -SELECT_LIFT : 0);
        liftAnim.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        liftAnim.play();
    }

    /** ไฮไลท์เป็น "hint" (สีเหลือง) แยกจาก highlight ของการ hover */
    public void setHint(boolean on) {
        if (on) {
            if (!outline.getStyleClass().contains("tube-hint")) {
                outline.getStyleClass().add("tube-hint");
            }
        } else {
            outline.getStyleClass().remove("tube-hint");
        }
    }

    public void shake() {
        Fx.shake(this);
    }

    public void setHighlighted(boolean on) {
        if (on) {
            if (!outline.getStyleClass().contains("tube-highlight")) {
                outline.getStyleClass().add("tube-highlight");
            }
        } else {
            outline.getStyleClass().remove("tube-highlight");
        }
    }

    public void setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
    }

    // ── Pour animation support ─────────────────────────────────

    public Pane body() {
        return body;
    }

    public Rotate pourRotate() {
        return pourRotate;
    }

    public DoubleProperty levelProperty() {
        return level;
    }

    /** เตรียมรับน้ำ: เพิ่มบล็อกสีใหม่ (count ชั้น) แต่ยังไม่แสดง (level คงเดิม) */
    public void beginFill(int color, int count) {
        int old = colors.length;
        int[] next = Arrays.copyOf(colors, old + count);
        Arrays.fill(next, old, old + count, color);
        colors = next;
        level.set(old);
        render();
    }

    public void setPouring(boolean pouring) {
        if (pouring && liftAnim != null) {
            liftAnim.stop();   // ให้ animation เทเป็นตัวควบคุมตำแหน่งหลอดแทนการยกจากการเลือก
        }
        setViewOrder(pouring ? -100 : 0);
        label.setOpacity(pouring ? 0 : 1);
    }

    public void resetPour() {
        if (liftAnim != null) {
            liftAnim.stop();
        }
        selected = false;
        body.setTranslateX(0);
        body.setTranslateY(0);
        pourRotate.setAngle(0);
        setPouring(false);
    }

    // ── Rendering ───────────────────────────────────────────────

    private void render() {
        double lvl = level.get();
        for (int i = 0; i < Tube.CAPACITY; i++) {
            Rectangle r = layers[i];
            if (i >= colors.length) {
                r.setVisible(false);
                continue;
            }
            double h = Math.max(0, Math.min(1, lvl - i)) * SLOT;
            r.setVisible(h > 0.05);
            r.setHeight(h);
            r.setY(LIQUID_BOTTOM - i * SLOT - h);
            r.setFill(gradient(colors[i]));
        }
        for (int i = 0; i < Tube.CAPACITY; i++) {
            slotOutlines[i].setVisible(editable && i >= colors.length);
        }
    }

    private void updateDoneState() {
        boolean done = isSortedTube();
        label.getStyleClass().remove("tube-label-done");
        if (done) {
            label.getStyleClass().add("tube-label-done");
            body.setEffect(new DropShadow(BlurType.GAUSSIAN, colorSource.apply(colors[0]).deriveColor(0, 1, 1, 0.75), 16, 0.25, 0, 0));
        } else {
            body.setEffect(null);
        }
    }

    private void refreshGhost() {
        boolean active = editable && hoveredSlot >= 0 && (ghostErase || ghostPaint != null);
        if (!active) {
            ghost.setVisible(false);
            return;
        }
        int target = ghostErase
                ? (hoveredSlot < colors.length ? hoveredSlot : -1)
                : Math.min(hoveredSlot, colors.length);
        if (target < 0 || target >= Tube.CAPACITY) {
            ghost.setVisible(false);
            return;
        }
        ghost.setY(slotTop(target));
        if (ghostErase) {
            ghost.setFill(Color.rgb(255, 80, 80, 0.30));
            ghost.setStroke(Color.rgb(255, 100, 100, 0.95));
        } else {
            ghost.setFill(ghostPaint.deriveColor(0, 1, 1, 0.6));
            ghost.setStroke(Color.rgb(255, 255, 255, 0.75));
        }
        ghost.setVisible(true);
    }

    private static double slotTop(int slot) {
        return LIQUID_BOTTOM - (slot + 1) * SLOT;
    }

    /** ตั้งแหล่งสี (color ID → Color) เช่น สีจริงจากภาพ; ล้าง cache แล้ววาดใหม่ */
    public void setColorSource(java.util.function.IntFunction<Color> source) {
        this.colorSource = source != null ? source : TubeColors::color;
        gradientCache.clear();
        render();
        updateDoneState();
    }

    private LinearGradient gradient(int colorId) {
        return gradientCache.computeIfAbsent(colorId, id -> {
            Color base = colorSource.apply(id);
            return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, base.interpolate(Color.WHITE, 0.28)),
                    new Stop(0.45, base),
                    new Stop(1, base.interpolate(Color.BLACK, 0.2)));
        });
    }

    /** เส้นขอบหลอดรูปตัว U (ปากเปิดด้านบน) — inset > 0 ใช้เป็น clip ของน้ำ */
    private static String uPath(double inset, boolean closed) {
        double x0 = inset;
        double x1 = W - inset;
        double yb = BODY_H - inset;
        double r = (x1 - x0) / 2;
        String d = String.format(Locale.ROOT, "M%1$.2f,0 L%1$.2f,%2$.2f A%3$.2f,%3$.2f 0 0,0 %4$.2f,%5$.2f A%3$.2f,%3$.2f 0 0,0 %6$.2f,%2$.2f L%6$.2f,0",
                x0, yb - r, r, x0 + r, yb, x1);
        return closed ? d + " Z" : d;
    }
}
