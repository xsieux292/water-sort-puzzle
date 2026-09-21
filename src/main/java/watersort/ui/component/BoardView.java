package watersort.ui.component;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import watersort.model.BoardState;
import watersort.model.Tube;
import watersort.ui.model.TubeColors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * แสดงหลอดทั้งหมดเป็น grid (แถวละ 7) และเล่น animation เทน้ำ
 *
 * ทุกอย่างวางในพิกัด "logical" คงที่ (content) แล้วย่อ/ขยายทั้งก้อนให้พอดีกับพื้นที่ที่มี
 * จึง responsive โดยไม่ต้องคำนวณขนาดหลอดใหม่ และ animation ใช้พิกัดเดียวกันตลอด
 */
public class BoardView extends Pane {

    public static final int COLUMNS = 7;

    private static final double GAP_X = 22;
    private static final double GAP_Y = 16;
    private static final double PAD_X = 40;
    private static final double HEADROOM = 88;   // พื้นที่เหนือแถวแรก สำหรับหลอดที่ยกขึ้นตอนเท
    private static final double PAD_BOTTOM = 8;
    private static final double MAX_SCALE = 1.6;

    private static final double TILT_DEG = 50;
    private static final double CLEARANCE = 26;  // ปากหลอดต้นทางสูงกว่าปากหลอดปลายทางเท่านี้
    private static final double STREAM_W = 8;

    private final Pane content = new Pane();
    private final Pane streamLayer = new Pane();
    private final Scale scale = new Scale(1, 1, 0, 0);
    private final List<TubeView> tubes = new ArrayList<>();

    private double logicalW = 300;
    private double logicalH = 200;

    private boolean editable;
    private IntFunction<Color> colorSource = TubeColors::color;
    private Color ghostPaint;
    private boolean ghostErase;
    private TubeView.SlotClickHandler slotHandler;
    private IntConsumer hoverHandler = i -> { };
    private IntConsumer tubeClickHandler;
    private int highlighted = -1;

    private Animation current;
    private Runnable currentCleanup;

    public BoardView() {
        getStyleClass().add("board-view");
        content.setManaged(false);
        content.getTransforms().add(scale);
        streamLayer.setMouseTransparent(true);
        content.getChildren().add(streamLayer);
        getChildren().add(content);
        setMinSize(240, 160);
    }

    // ── State ───────────────────────────────────────────────────

    public void setState(BoardState state) {
        cancelAnimation();
        if (tubes.size() != state.tubeCount()) {
            rebuild(state.tubeCount());
        }
        for (int i = 0; i < tubes.size(); i++) {
            tubes.get(i).resetPour();
            tubes.get(i).setContents(toArray(state.getTube(i)));
        }
    }

    public TubeView tubeView(int i) {
        return tubes.get(i);
    }

    public int tubeCount() {
        return tubes.size();
    }

    private void rebuild(int count) {
        content.getChildren().removeIf(n -> n instanceof TubeView);
        tubes.clear();
        for (int i = 0; i < count; i++) {
            TubeView tv = new TubeView(i);
            tv.setEditable(editable);
            tv.setColorSource(colorSource);
            tv.setOnTubeClick(tubeClickHandler == null ? null : this::dispatchTubeClick);
            tv.setGhostPaint(ghostPaint, ghostErase);
            tv.setSlotClickHandler((t, s, b) -> {
                if (slotHandler != null) {
                    slotHandler.onSlotClick(t, s, b);
                }
            });
            final int idx = i;
            tv.hoverProperty().addListener((o, was, is) -> hoverHandler.accept(is ? idx : -1));
            tubes.add(tv);
            content.getChildren().add(tv);
        }
        streamLayer.toFront();
        layoutLogical();
        setHighlightedTube(highlighted);
        requestLayout();
    }

    private static int[] toArray(Tube t) {
        int[] arr = new int[t.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = t.getColorAt(i);
        }
        return arr;
    }

    // ── Edit mode / linking ─────────────────────────────────────

    public void setEditable(boolean editable) {
        this.editable = editable;
        tubes.forEach(t -> t.setEditable(editable));
    }

    public void setGhost(Color paint, boolean erase) {
        this.ghostPaint = paint;
        this.ghostErase = erase;
        tubes.forEach(t -> t.setGhostPaint(paint, erase));
    }

    /** ตั้งแหล่งสีของทุกหลอด (เช่น สีจริงจากภาพ) และใช้กับ animation เทน้ำด้วย */
    public void setColorSource(IntFunction<Color> source) {
        this.colorSource = source != null ? source : TubeColors::color;
        tubes.forEach(t -> t.setColorSource(this.colorSource));
    }

    /** คลิกหลอด (นอก edit mode) — null = ปิดการคลิก (คงพฤติกรรมเดิม: แค่เด้งเป็น feedback) */
    public void setOnTubeClick(IntConsumer handler) {
        this.tubeClickHandler = handler;
        tubes.forEach(t -> t.setOnTubeClick(handler == null ? null : this::dispatchTubeClick));
    }

    private void dispatchTubeClick(int index) {
        if (tubeClickHandler != null) {
            tubeClickHandler.accept(index);
        }
    }

    /** ยกหลอดที่เลือก (-1 = ไม่เลือก) */
    public void setSelectedTube(int index) {
        for (int i = 0; i < tubes.size(); i++) {
            tubes.get(i).setSelected(i == index);
        }
    }

    /** ไฮไลท์คู่หลอดที่แนะนำให้เท (hint) */
    public void setHintTubes(int from, int to) {
        for (int i = 0; i < tubes.size(); i++) {
            tubes.get(i).setHint(i == from || i == to);
        }
    }

    public void clearHint() {
        setHintTubes(-1, -1);
    }

    public void shakeTube(int index) {
        if (index >= 0 && index < tubes.size()) {
            tubes.get(index).shake();
        }
    }

    public void setSlotClickHandler(TubeView.SlotClickHandler handler) {
        this.slotHandler = handler;
    }

    /** ถูกเรียกด้วย index ของหลอดที่เมาส์อยู่ (หรือ -1 เมื่อออก) */
    public void setOnTubeHover(IntConsumer handler) {
        this.hoverHandler = handler;
    }

    public void setHighlightedTube(int index) {
        this.highlighted = index;
        for (int i = 0; i < tubes.size(); i++) {
            tubes.get(i).setHighlighted(i == index);
        }
    }

    // ── Layout (logical → scaled) ───────────────────────────────

    private void layoutLogical() {
        int n = tubes.size();
        int cols = Math.max(1, Math.min(COLUMNS, n));
        int rows = n == 0 ? 1 : (n + cols - 1) / cols;
        logicalW = PAD_X * 2 + cols * TubeView.W + (cols - 1) * GAP_X;
        logicalH = HEADROOM + rows * TubeView.TOTAL_H + (rows - 1) * GAP_Y + PAD_BOTTOM;

        for (int i = 0; i < n; i++) {
            int row = i / cols;
            int col = i % cols;
            int inRow = Math.min(cols, n - row * cols);
            double rowWidth = inRow * TubeView.W + (inRow - 1) * GAP_X;
            double x0 = (logicalW - rowWidth) / 2;   // แถวสุดท้ายที่ไม่เต็มจะอยู่กึ่งกลาง
            tubes.get(i).relocate(x0 + col * (TubeView.W + GAP_X), HEADROOM + row * (TubeView.TOTAL_H + GAP_Y));
        }
        content.resize(logicalW, logicalH);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double s = Math.min(Math.min(w / logicalW, h / logicalH), MAX_SCALE);
        if (s <= 0 || Double.isNaN(s)) {
            s = 1;
        }
        scale.setX(s);
        scale.setY(s);
        content.relocate((w - logicalW * s) / 2, (h - logicalH * s) / 2);
    }

    @Override
    protected double computePrefWidth(double height) {
        return logicalW;
    }

    @Override
    protected double computePrefHeight(double width) {
        return logicalH;
    }

    // ── Pour animation ──────────────────────────────────────────

    /**
     * เล่น animation เทน้ำ count บล็อกสี color จากหลอด from → to
     *
     * 1) ยกหลอดต้นทางไปเหนือหลอดปลายทางและเอียง (TranslateTransition + tilt)
     * 2) น้ำไหลเป็นสายลงหลอดปลายทาง — ระดับน้ำต้นทางลด ปลายทางเพิ่ม
     * 3) หลอดกลับที่เดิม
     *
     * @param rate 1.0 = ความเร็วปกติ, 2.0 = เร็วเป็นสองเท่า
     */
    public void playPour(int from, int to, int count, int color, double rate, Runnable onFinished) {
        cancelAnimation();

        TubeView src = tubes.get(from);
        TubeView dst = tubes.get(to);
        double theta = Math.toRadians(TILT_DEG);
        int dir = tiltDirection(src, dst);

        double pivotX = src.getLayoutX() + TubeView.W / 2;
        double pivotY = src.getLayoutY();
        double dstCx = dst.getLayoutX() + TubeView.W / 2;
        double dstTop = dst.getLayoutY();

        // ตำแหน่งจุดหมุน (กึ่งกลางปากหลอด) ที่ทำให้ "ริมปาก" ที่เอียงลงอยู่เหนือกลางหลอดปลายทางพอดี
        double targetX = dstCx - dir * (TubeView.W / 2) * Math.cos(theta);
        double targetY = dstTop - CLEARANCE - (TubeView.W / 2) * Math.sin(theta);
        double dx = targetX - pivotX;
        double dy = targetY - pivotY;

        double lipY = dstTop - CLEARANCE;
        int dstSize = dst.size();
        int srcSize = src.size();
        double surfaceY = dst.getLayoutY() + TubeView.LIQUID_BOTTOM - (dstSize + count) * TubeView.SLOT;
        double streamLen = Math.max(12, surfaceY - lipY);

        Rectangle stream = new Rectangle(dstCx - STREAM_W / 2, lipY, STREAM_W, 0);
        stream.setArcWidth(STREAM_W);
        stream.setArcHeight(STREAM_W);
        stream.setFill(colorSource.apply(color));
        streamLayer.getChildren().add(stream);

        src.setPouring(true);
        dst.beginFill(color, count);
        tubes.forEach(t -> t.setHoverEnabled(false));

        Interpolator ease = Interpolator.EASE_BOTH;
        Duration liftDur = Duration.millis(380);

        TranslateTransition lift = new TranslateTransition(liftDur, src.body());
        lift.setToX(dx);
        lift.setToY(dy);
        lift.setInterpolator(ease);
        Timeline tilt = new Timeline(new KeyFrame(liftDur,
                new KeyValue(src.pourRotate().angleProperty(), dir * TILT_DEG, ease)));
        ParallelTransition up = new ParallelTransition(lift, tilt);

        double pourMs = 220 + 200 * count;
        Timeline pour = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(stream.yProperty(), lipY),
                        new KeyValue(stream.heightProperty(), 0),
                        new KeyValue(src.levelProperty(), srcSize),
                        new KeyValue(dst.levelProperty(), dstSize)),
                // สายน้ำพุ่งลงไปถึงผิวน้ำ
                new KeyFrame(Duration.millis(110),
                        new KeyValue(stream.heightProperty(), streamLen, Interpolator.EASE_OUT),
                        new KeyValue(src.levelProperty(), srcSize),
                        new KeyValue(dst.levelProperty(), dstSize)),
                // น้ำต้นทางลด / ปลายทางเพิ่ม
                new KeyFrame(Duration.millis(pourMs - 120),
                        new KeyValue(stream.yProperty(), lipY),
                        new KeyValue(stream.heightProperty(), streamLen),
                        new KeyValue(src.levelProperty(), srcSize - count, Interpolator.LINEAR),
                        new KeyValue(dst.levelProperty(), dstSize + count, Interpolator.LINEAR)),
                // สายน้ำหดกลับ (ปลายบนตามลงมาหาผิวน้ำ)
                new KeyFrame(Duration.millis(pourMs),
                        new KeyValue(stream.yProperty(), lipY + streamLen, Interpolator.EASE_IN),
                        new KeyValue(stream.heightProperty(), 0, Interpolator.EASE_IN)));

        TranslateTransition back = new TranslateTransition(Duration.millis(340), src.body());
        back.setToX(0);
        back.setToY(0);
        back.setInterpolator(ease);
        Timeline untilt = new Timeline(new KeyFrame(Duration.millis(340),
                new KeyValue(src.pourRotate().angleProperty(), 0, ease)));
        ParallelTransition down = new ParallelTransition(back, untilt);

        SequentialTransition seq = new SequentialTransition(up, pour, down);
        seq.setRate(Math.max(0.1, rate));

        currentCleanup = () -> {
            streamLayer.getChildren().remove(stream);
            src.resetPour();
            tubes.forEach(t -> t.setHoverEnabled(true));
        };
        seq.setOnFinished(e -> {
            Runnable cleanup = currentCleanup;
            current = null;
            currentCleanup = null;
            if (cleanup != null) {
                cleanup.run();
            }
            if (onFinished != null) {
                onFinished.run();
            }
        });
        current = seq;
        seq.play();
    }

    public boolean isAnimating() {
        return current != null;
    }

    /** หยุด animation ทันที (ไม่เรียก onFinished) — ผู้เรียกควร setState() ต่อเพื่อให้ภาพตรงกับข้อมูล */
    public void cancelAnimation() {
        if (current != null) {
            current.stop();
            current = null;
        }
        if (currentCleanup != null) {
            Runnable cleanup = currentCleanup;
            currentCleanup = null;
            cleanup.run();
        }
    }

    private int tiltDirection(TubeView src, TubeView dst) {
        double sx = src.getLayoutX();
        double dxx = dst.getLayoutX();
        if (Math.abs(sx - dxx) > 1) {
            return sx < dxx ? 1 : -1;
        }
        // หลอดอยู่คอลัมน์เดียวกัน: เอียงเข้าหาด้านที่มีที่ว่างมากกว่า
        return sx < logicalW / 2 ? -1 : 1;
    }

    /** เฉลิมฉลองตอนแก้ได้: หลอดที่เสร็จแล้วเด้งไล่กัน */
    public void celebrate() {
        int k = 0;
        for (TubeView tv : tubes) {
            if (!tv.isSortedTube()) {
                continue;
            }
            TranslateTransition hop = new TranslateTransition(Duration.millis(170), tv.body());
            hop.setByY(-16);
            hop.setCycleCount(2);
            hop.setAutoReverse(true);
            hop.setDelay(Duration.millis(k++ * 70L));
            hop.setOnFinished(e -> tv.body().setTranslateY(0));
            hop.play();
        }
    }
}
