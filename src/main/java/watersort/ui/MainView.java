package watersort.ui;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import watersort.model.BoardState;
import watersort.model.Move;
import watersort.generator.GeneratedPuzzle;
import watersort.generator.PuzzleGenerator;
import watersort.solver.AStarSolver;
import watersort.solver.SolveResult;
import watersort.ui.component.BoardView;
import watersort.ui.component.ColorPickerPanel;
import watersort.ui.component.ControlBar;
import watersort.ui.component.GameBar;
import watersort.ui.component.GameSetupPane;
import watersort.ui.component.ModeSelectPane;
import watersort.ui.component.RecognitionPreviewPane;
import watersort.ui.component.SolutionPanel;
import watersort.ui.component.UploadPane;
import watersort.ui.model.BoardEditor;
import watersort.ui.model.ColorScheme;
import watersort.ui.model.GameSession;
import watersort.ui.model.PlaybackModel;
import watersort.ui.model.Presets;
import watersort.ui.model.TubeColors;
import watersort.ui.model.Validation;
import watersort.ui.util.Fx;
import watersort.vision.ImageRecognizer;
import watersort.vision.RecognitionResult;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * หน้าจอหลักของแอป — เริ่มด้วยเลือกโหมด แล้วแยกเป็น 2 flow:
 *
 *   โหมด 1 Image Recognizer: Upload → Recognize → Preview → (Edit) → Confirm → Solve → Playback
 *   โหมด 2 Play Game:        Setup (สี/ความยาก) → Generate → ผู้เล่นเทเอง (Undo/Hint/Solve for me)
 */
public class MainView extends BorderPane {

    private enum Phase { MODE, INPUT, SETUP, PLAY, REVIEW, CONFIRMED, SOLVING, PLAYBACK }

    /** ความเร็ว animation เวลาผู้เล่นเทเอง (เร็วกว่า playback ปกติเล็กน้อย จะได้ไม่หน่วง) */
    private static final double PLAY_RATE = 1.6;

    private static final String[] STEP_NAMES = {"Upload", "Review", "Solve", "Play"};

    // ── components ──
    private final UploadPane uploadPane = new UploadPane();
    private final RecognitionPreviewPane previewPane = new RecognitionPreviewPane();
    private final BoardView boardView = new BoardView();
    private final ColorPickerPanel pickerPanel = new ColorPickerPanel();
    private final SolutionPanel solutionPanel = new SolutionPanel();
    private final ControlBar controls = new ControlBar();
    private final GameSetupPane setupPane = new GameSetupPane();
    private final GameBar gameBar = new GameBar();

    // ── layout pieces ──
    private final Label banner = new Label();
    private final Label[] stepLabels = new Label[STEP_NAMES.length];
    private final Button recognizeButton = new Button("Recognize");
    private final Button newPuzzleButton = new Button("Change Mode");
    private ModeSelectPane modePane;
    private HBox stepsBox;
    private StackPane setupHolder;
    private final VBox inputPage = new VBox();
    private final BorderPane workspace = new BorderPane();
    private final VBox bottomBox = new VBox();
    private final StackPane busyOverlay = new StackPane();
    private final Label busyLabel = new Label();
    private final Button busyCancel = new Button("Cancel");

    // ── state ──
    private Phase phase = Phase.MODE;
    private boolean editing;
    private File selectedFile;
    private BoardState originalBoard;
    private BoardState confirmedBoard;
    private BoardEditor editor;
    private Validation validation;
    private PlaybackModel playback;
    private BoardState animationTarget;
    private boolean autoPlaying;
    private PauseTransition autoTimer;
    private Task<?> runningTask;
    private ColorScheme scheme = ColorScheme.DEFAULT;
    private boolean gameMode;
    private GameSession session;
    private int selectedTube = -1;

    public MainView(BooleanProperty darkTheme) {
        getStyleClass().add("app-root");
        setTop(buildHeader(darkTheme));
        setCenter(buildCenter());
        setBottom(bottomBox);
        bottomBox.getChildren().addAll(solutionPanel, controls, gameBar);
        VBox.setMargin(solutionPanel, new Insets(0, 18, 0, 18));

        wireEvents();
        applyPhase();

        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
    }

    // ════════════════════════════════════════════════════════════
    //  Layout
    // ════════════════════════════════════════════════════════════

    private Node buildHeader(BooleanProperty darkTheme) {
        HBox logo = new HBox(4);
        logo.setAlignment(Pos.CENTER);
        for (int id : new int[]{1, 2, 3, 4}) {
            Circle c = new Circle(6, TubeColors.color(id));
            logo.getChildren().add(c);
        }
        Label title = new Label("Water Sort Solver");
        title.getStyleClass().add("app-title");

        HBox steps = new HBox(6);
        stepsBox = steps;
        steps.setAlignment(Pos.CENTER);
        for (int i = 0; i < STEP_NAMES.length; i++) {
            Label l = new Label((i + 1) + "  " + STEP_NAMES[i]);
            l.getStyleClass().add("step-pill");
            stepLabels[i] = l;
            steps.getChildren().add(l);
        }

        newPuzzleButton.getStyleClass().add("secondary");
        newPuzzleButton.setFocusTraversable(false);
        newPuzzleButton.setOnAction(e -> goHome());
        Fx.hoverScale(newPuzzleButton, 1.05, 0.95);

        ToggleButton themeToggle = new ToggleButton();
        themeToggle.getStyleClass().addAll("secondary", "icon-button");
        themeToggle.setFocusTraversable(false);
        themeToggle.setSelected(!darkTheme.get());
        themeToggle.textProperty().bind(javafx.beans.binding.Bindings.when(darkTheme).then("☀").otherwise("☾"));
        themeToggle.setOnAction(e -> darkTheme.set(!themeToggle.isSelected()));
        themeToggle.setTooltip(new javafx.scene.control.Tooltip("Toggle light / dark theme"));
        Fx.hoverScale(themeToggle, 1.1, 0.92);

        Region left = new Region();
        Region right = new Region();
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox header = new HBox(14, logo, title, left, steps, right, newPuzzleButton, themeToggle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(12, 18, 12, 18));
        return header;
    }

    private Node buildCenter() {
        // ---- input page ----
        Label heading = new Label("Solve any Water Sort level");
        heading.getStyleClass().add("hero-title");
        Label sub = new Label("Upload a screenshot — we detect the tubes, you confirm, and we play the solution step by step.");
        sub.getStyleClass().add("muted");
        sub.setWrapText(true);

        VBox.setVgrow(uploadPane, Priority.ALWAYS);
        uploadPane.setMaxHeight(360);

        recognizeButton.getStyleClass().addAll("primary", "big");
        recognizeButton.setDisable(true);
        recognizeButton.setFocusTraversable(false);
        Fx.hoverScale(recognizeButton, 1.05, 0.96, () -> !recognizeButton.isDisabled());

        Label or = new Label("— or start without an image —");
        or.getStyleClass().add("muted");

        Button manual = new Button("Manual Editor");
        manual.getStyleClass().add("secondary");
        manual.setFocusTraversable(false);
        manual.setOnAction(e -> startManual());
        Fx.hoverScale(manual, 1.05, 0.95);

        MenuButton presets = new MenuButton("Preset Puzzles");
        presets.getStyleClass().add("secondary");
        presets.setFocusTraversable(false);
        for (Presets.Preset p : Presets.all()) {
            MenuItem item = new MenuItem(p.name());
            item.setOnAction(e -> loadPreset(p));
            presets.getItems().add(item);
        }
        Fx.hoverScale(presets, 1.05, 0.95);

        HBox alt = new HBox(12, manual, presets);
        alt.setAlignment(Pos.CENTER);

        inputPage.setSpacing(16);
        inputPage.setAlignment(Pos.CENTER);
        inputPage.setMaxWidth(780);
        inputPage.setPadding(new Insets(24));
        inputPage.getChildren().addAll(heading, sub, uploadPane, recognizeButton, or, alt);

        StackPane inputHolder = new StackPane(inputPage);
        inputHolder.setPadding(new Insets(10));

        // ---- mode select + game setup pages ----
        modePane = new ModeSelectPane(this::startImageMode, this::startGameMode);
        StackPane modeHolder = new StackPane(modePane);
        modeHolder.setPadding(new Insets(10));
        setupHolder = new StackPane(setupPane);
        setupHolder.setPadding(new Insets(10));

        // ---- workspace ----
        banner.setWrapText(true);
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setMinHeight(Region.USE_PREF_SIZE);
        banner.getStyleClass().add("banner");

        boardView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane boardCard = new StackPane(boardView);
        boardCard.getStyleClass().addAll("card", "board-card");
        boardCard.setPadding(new Insets(10));
        VBox.setVgrow(boardCard, Priority.ALWAYS);

        VBox boardArea = new VBox(12, banner, boardCard);
        workspace.setCenter(boardArea);
        workspace.setLeft(previewPane);
        workspace.setRight(pickerPanel);
        BorderPane.setMargin(previewPane, new Insets(0, 14, 0, 0));
        BorderPane.setMargin(pickerPanel, new Insets(0, 0, 0, 14));
        BorderPane.setAlignment(pickerPanel, Pos.TOP_CENTER);
        previewPane.prefWidthProperty().bind(workspace.widthProperty().multiply(0.30));
        previewPane.setMaxWidth(440);
        workspace.setPadding(new Insets(14, 18, 12, 18));

        // ---- busy overlay ----
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(56, 56);
        busyLabel.getStyleClass().add("busy-label");
        busyCancel.getStyleClass().add("secondary");
        busyCancel.setOnAction(e -> cancelTask());
        VBox busyBox = new VBox(14, spinner, busyLabel, busyCancel);
        busyBox.setAlignment(Pos.CENTER);
        busyBox.getStyleClass().add("busy-box");
        busyBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        busyOverlay.getChildren().add(busyBox);
        busyOverlay.getStyleClass().add("busy-overlay");
        busyOverlay.setVisible(false);

        return new StackPane(modeHolder, inputHolder, setupHolder, workspace, busyOverlay);
    }

    // ════════════════════════════════════════════════════════════
    //  Wiring
    // ════════════════════════════════════════════════════════════

    private void wireEvents() {
        uploadPane.setOnImageChosen(file -> {
            selectedFile = file;
            recognizeButton.setDisable(false);
        });
        recognizeButton.setOnAction(e -> recognize());

        controls.editButton().setOnAction(e -> setEditing(controls.editButton().isSelected()));
        controls.confirmButton().setOnAction(e -> confirm());
        controls.solveButton().setOnAction(e -> solve());
        controls.prevButton().setOnAction(e -> manualStep(-1));
        controls.nextButton().setOnAction(e -> manualStep(1));
        controls.playButton().setOnAction(e -> toggleAutoPlay());
        controls.resetButton().setOnAction(e -> reset());

        boardView.setSlotClickHandler((tube, slot, button) -> {
            if (!editing || phase != Phase.REVIEW) {
                return;
            }
            boolean erase = button == MouseButton.SECONDARY || pickerPanel.getSelectedColor() == ColorPickerPanel.ERASER;
            boolean changed = erase
                    ? editor.erase(tube, slot)
                    : editor.paint(tube, slot, pickerPanel.getSelectedColor());
            if (changed) {
                onEdited();
            }
        });
        pickerPanel.selectedColorProperty().addListener((o, was, is) -> updateGhost());
        pickerPanel.setOnAddColor(() -> {
            pickerPanel.setColorCount(pickerPanel.getColorCount() + 1);
            pickerPanel.selectedColorProperty().set(pickerPanel.getColorCount());
        });
        pickerPanel.setOnAddTube(() -> {
            if (editor.addTube()) {
                onEdited();
            }
        });
        pickerPanel.setOnRemoveTube(() -> {
            if (editor.removeLastTube()) {
                onEdited();
            }
        });

        // โหมดเล่นเกม
        setupPane.setOnGenerate(this::generateGame);
        boardView.setOnTubeClick(this::onTubeClicked);
        gameBar.undoButton().setOnAction(e -> undo());
        gameBar.hintButton().setOnAction(e -> hint());
        gameBar.restartButton().setOnAction(e -> restartGame());
        gameBar.solveButton().setOnAction(e -> solveForMe());
        gameBar.newGameButton().setOnAction(e -> newGame());
        controls.backToGameButton().setOnAction(e -> backToGame());

        boardView.setOnTubeHover(previewPane::setHighlightedTube);
        previewPane.setOnTubeHover(boardView::setHighlightedTube);
    }

    private void handleKey(KeyEvent e) {
        if (phase != Phase.PLAYBACK) {
            return;
        }
        switch (e.getCode()) {
            case RIGHT -> manualStep(1);
            case LEFT -> manualStep(-1);
            case SPACE -> toggleAutoPlay();
            default -> {
                return;
            }
        }
        e.consume();
    }

    // ════════════════════════════════════════════════════════════
    //  Flow: input → review
    // ════════════════════════════════════════════════════════════

    private void recognize() {
        if (selectedFile == null) {
            return;
        }
        File file = selectedFile;
        showBusy("Recognizing tubes...", true);
        Task<RecognitionResult> task = new Task<>() {
            @Override
            protected RecognitionResult call() {
                return new ImageRecognizer().recognize(file.getAbsolutePath());
            }
        };
        runTask(task, result -> {
            if (result.getBoardState() == null) {
                String reason = result.getErrors() == null || result.getErrors().isEmpty()
                        ? "Could not recognize this image."
                        : String.join(" ", result.getErrors());
                uploadPane.showError(reason + " Try another screenshot or use the Manual Editor.");
                return;
            }
            Image image = new Image(file.toURI().toString());
            enterWorkspace(result.getBoardState(), image, result, !result.isValid(), result.getColorMap().size());
        }, error -> uploadPane.showError("Recognition failed: " + describe(error)));
    }

    private void startManual() {
        enterWorkspace(BoardEditor.blank(6).toBoardState(), null, null, true, 6);
    }

    private void loadPreset(Presets.Preset preset) {
        enterWorkspace(preset.board(), null, null, false, 0);
    }

    private void enterWorkspace(BoardState board, Image image, RecognitionResult result, boolean startEditing, int colorHint) {
        stopAutoPlay();
        playback = null;
        confirmedBoard = null;
        originalBoard = board;
        editor = new BoardEditor(board);
        // ใช้สีจริงจากภาพ (ถ้ามาจากการ recognize) เพื่อให้บอร์ดใน UI ตรงกับเกม
        scheme = result != null
                ? ColorScheme.fromRecognition(result.getColorRgb(), result.getColorMap())
                : ColorScheme.DEFAULT;
        boardView.setColorSource(scheme::color);
        pickerPanel.setColorScheme(scheme);
        pickerPanel.setColorCount(Math.max(Math.max(colorHint, editor.maxColorId()), 4));
        pickerPanel.selectedColorProperty().set(1);
        if (image != null && result != null) {
            previewPane.show(image, result);
        }
        previewPane.setVisible(image != null);
        previewPane.setManaged(image != null);

        editing = startEditing;
        phase = Phase.REVIEW;
        boardView.setState(board);
        refreshValidation();
        applyPhase();
        Fx.fadeIn(workspace, 260);
    }

    // ════════════════════════════════════════════════════════════
    //  Flow: edit / confirm / solve
    // ════════════════════════════════════════════════════════════

    private void setEditing(boolean on) {
        if (phase == Phase.SOLVING || phase == Phase.INPUT) {
            return;
        }
        if (on && phase != Phase.REVIEW) {
            // กลับมาแก้กระดานที่ Confirm ไปแล้ว → ทิ้ง solution เดิม
            stopAutoPlay();
            boardView.cancelAnimation();
            playback = null;
            editor = new BoardEditor(confirmedBoard != null ? confirmedBoard : originalBoard);
            phase = Phase.REVIEW;
            boardView.setState(editor.toBoardState());
            refreshValidation();
        }
        editing = on;
        applyPhase();
    }

    private void onEdited() {
        boardView.setState(editor.toBoardState());
        refreshValidation();
    }

    private void refreshValidation() {
        validation = editor.validate();
        pickerPanel.setCounts(validation.colorCounts());
        previewPane.setSuspectTubes(validation.suspectTubes());
        if (phase == Phase.REVIEW) {
            updateReviewBanner();
        }
        updateControls();
    }

    private void confirm() {
        if (phase != Phase.REVIEW || validation == null || !validation.isValid()) {
            return;
        }
        editing = false;
        confirmedBoard = editor.toBoardState();
        phase = Phase.CONFIRMED;
        applyPhase();
    }

    private void solve() {
        if (phase != Phase.CONFIRMED) {
            return;
        }
        BoardState start = confirmedBoard;
        phase = Phase.SOLVING;
        applyPhase();
        showBusy("Searching for the shortest solution...", true);

        Task<SolveResult> task = new Task<>() {
            @Override
            protected SolveResult call() {
                return new AStarSolver().solve(start);
            }
        };
        runTask(task, result -> {
            if (!result.isSolved()) {
                phase = Phase.CONFIRMED;
                applyPhase();
                setBanner("No solution found (explored " + String.format("%,d", result.getStatesExplored())
                        + " states). The board may be unsolvable — click Edit to double-check the colors.", "error");
                return;
            }
            if (result.getMoves().isEmpty()) {
                phase = Phase.CONFIRMED;
                applyPhase();
                setBanner("This board is already solved — nothing to pour.", "ok");
                return;
            }
            playback = new PlaybackModel(start, result.getMoves());
            solutionPanel.setStats(String.format("A*  ·  %,d states  ·  %d ms", result.getStatesExplored(), result.getTimeMillis()));
            phase = Phase.PLAYBACK;
            boardView.setState(playback.stateAt(0));
            updatePlaybackUi();
            applyPhase();
        }, error -> {
            phase = Phase.CONFIRMED;
            applyPhase();
            setBanner("Solver failed: " + describe(error), "error");
        });
    }

    // ════════════════════════════════════════════════════════════
    //  Playback
    // ════════════════════════════════════════════════════════════

    private void manualStep(int dir) {
        if (phase != Phase.PLAYBACK) {
            return;
        }
        stopAutoPlay();
        step(dir, null);
    }

    /** เดิน 1 step (dir = +1 ไปข้างหน้า, −1 ถอยหลัง) พร้อม animation เทน้ำ */
    private void step(int dir, Runnable after) {
        if (phase != Phase.PLAYBACK) {
            return;
        }
        if (boardView.isAnimating()) {
            // ผู้ใช้กดรัวๆ: ข้าม animation เดิมไปที่ผลลัพธ์ทันที
            boardView.cancelAnimation();
            if (animationTarget != null) {
                boardView.setState(animationTarget);
            }
            animationTarget = null;
        }
        int idx = playback.index();
        int moveIdx = dir > 0 ? idx : idx - 1;
        if (moveIdx < 0 || moveIdx >= playback.stepCount()) {
            return;
        }
        Move m = playback.moves().get(moveIdx);
        int count = playback.pourCount(moveIdx);
        int color = playback.pourColor(moveIdx);
        int from = dir > 0 ? m.source() : m.destination();
        int to = dir > 0 ? m.destination() : m.source();
        BoardState target = playback.stateAt(idx + dir);

        playback.goTo(idx + dir);
        animationTarget = target;
        updatePlaybackUi();
        updateControls();

        boardView.playPour(from, to, count, color, controls.speedProperty().get(), () -> {
            animationTarget = null;
            boardView.setState(target);
            if (dir > 0 && !playback.canNext()) {
                boardView.celebrate();
            }
            updateControls();
            if (after != null) {
                after.run();
            }
        });
    }

    private void toggleAutoPlay() {
        if (phase != Phase.PLAYBACK) {
            return;
        }
        if (autoPlaying) {
            stopAutoPlay();
            return;
        }
        if (!playback.canNext()) {
            // เล่นจบแล้ว → เริ่มใหม่จาก step 0
            resetPlayback();
        }
        autoPlaying = true;
        controls.setPlaying(true);
        autoTick();
    }

    private void autoTick() {
        if (!autoPlaying) {
            return;
        }
        if (!playback.canNext()) {
            stopAutoPlay();
            return;
        }
        step(1, () -> {
            if (!autoPlaying) {
                return;
            }
            autoTimer = new PauseTransition(Duration.millis(380));
            autoTimer.setRate(controls.speedProperty().get());
            autoTimer.setOnFinished(e -> autoTick());
            autoTimer.play();
        });
    }

    private void stopAutoPlay() {
        autoPlaying = false;
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
        controls.setPlaying(false);
    }

    private void resetPlayback() {
        stopAutoPlay();
        boardView.cancelAnimation();
        animationTarget = null;
        playback.goTo(0);
        boardView.setState(playback.stateAt(0));
        updatePlaybackUi();
        updateControls();
    }

    private void updatePlaybackUi() {
        Move last = playback.lastMove();
        if (last == null) {
            solutionPanel.update(0, playback.stepCount(), null, null, null);
        } else {
            int color = playback.pourColor(playback.index() - 1);
            solutionPanel.update(playback.index(), playback.stepCount(), last.toString(),
                    scheme.color(color), scheme.name(color));
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Play Game mode
    // ════════════════════════════════════════════════════════════

    private void generateGame(int colors, PuzzleGenerator.Difficulty difficulty) {
        showBusy("Generating a puzzle...", true);
        Task<GeneratedPuzzle> task = new Task<>() {
            @Override
            protected GeneratedPuzzle call() {
                return new PuzzleGenerator().generate(colors, difficulty);
            }
        };
        runTask(task, this::enterGame,
                error -> setupPane.showError("Could not generate a puzzle: " + describe(error)));
    }

    private void enterGame(GeneratedPuzzle puzzle) {
        stopAutoPlay();
        playback = null;
        editing = false;
        selectedTube = -1;
        session = new GameSession(puzzle.getBoard(), puzzle.getOptimalSteps());
        scheme = ColorScheme.DEFAULT;
        boardView.setColorSource(scheme::color);
        previewPane.setVisible(false);
        previewPane.setManaged(false);

        phase = Phase.PLAY;
        boardView.setState(session.state());
        boardView.clearHint();
        applyPhase();
        updateGameUi();
        Fx.fadeIn(workspace, 260);
    }

    /** คลิกหลอดในโหมดเล่นเกม: คลิกแรก = เลือกต้นทาง (หลอดยกขึ้น), คลิกที่สอง = เท */
    private void onTubeClicked(int index) {
        if (phase != Phase.PLAY || session == null) {
            return;
        }
        settleBoard();
        boardView.clearHint();
        if (session.isWon()) {
            return;
        }
        var tube = session.state().getTube(index);
        if (selectedTube < 0) {
            if (tube.isEmpty()) {
                boardView.shakeTube(index);
            } else {
                selectTube(index);
            }
            return;
        }
        if (index == selectedTube) {
            selectTube(-1);
            return;
        }
        if (session.canPour(selectedTube, index)) {
            pour(selectedTube, index);
            return;
        }
        var source = session.state().getTube(selectedTube);
        if (tube.isFull() && tube.topColor() == source.topColor()) {
            boardView.shakeTube(index);     // สีตรงแต่หลอดเต็ม — บอกด้วยการสั่น และคงต้นทางไว้
        } else {
            selectTube(index);              // เทไม่ได้ → ย้ายการเลือกไปหลอดที่คลิก
        }
    }

    private void selectTube(int index) {
        selectedTube = index;
        boardView.setSelectedTube(index);
    }

    private void pour(int from, int to) {
        GameSession.Pour p = session.pour(from, to);
        selectedTube = -1;   // หลอดต้นทางที่ยกอยู่จะเคลื่อนต่อเข้า animation เทเอง
        updateGameUi();
        boardView.playPour(p.from(), p.to(), p.count(), p.color(), PLAY_RATE, () -> {
            boardView.setState(session.state());
            afterMove();
        });
    }

    /** ถ้ามี animation ค้างอยู่ให้ข้ามไปที่ผลลัพธ์ทันที (ผู้เล่นกดรัว ๆ จะไม่ต้องรอ) */
    private void settleBoard() {
        if (session != null && boardView.isAnimating()) {
            boardView.cancelAnimation();
            boardView.setState(session.state());
            selectedTube = -1;
            afterMove();
        }
    }

    private void afterMove() {
        updateGameUi();
        if (session.isWon()) {
            boardView.celebrate();
        }
    }

    private void undo() {
        if (phase != Phase.PLAY || session == null) {
            return;
        }
        settleBoard();
        selectTube(-1);
        boardView.clearHint();
        GameSession.Pour p = session.undo();
        if (p == null) {
            return;
        }
        updateGameUi();
        boardView.playPour(p.to(), p.from(), p.count(), p.color(), PLAY_RATE, () -> {
            boardView.setState(session.state());
            afterMove();
        });
    }

    private void restartGame() {
        if (phase != Phase.PLAY || session == null) {
            return;
        }
        settleBoard();
        session.restart();
        selectedTube = -1;
        boardView.clearHint();
        boardView.setState(session.state());
        updateGameUi();
    }

    private void hint() {
        if (phase != Phase.PLAY || session == null || session.isWon()) {
            return;
        }
        settleBoard();
        selectTube(-1);
        boardView.clearHint();
        BoardState here = session.state();
        showBusy("Finding a hint...", true);
        Task<SolveResult> task = new Task<>() {
            @Override
            protected SolveResult call() {
                return new AStarSolver().solve(here);
            }
        };
        runTask(task, result -> {
            if (!result.isSolved() || result.getMoves().isEmpty()) {
                setBanner("No solution from this position — undo a few moves or restart.", "error");
                return;
            }
            Move m = result.getMoves().get(0);
            boardView.setHintTubes(m.source(), m.destination());
            int color = here.getTube(m.source()).topColor();
            setBanner(String.format("Hint: pour Tube %d → Tube %d (%s). At best %d more move%s to finish.",
                    m.source() + 1, m.destination() + 1, scheme.name(color),
                    result.getStepCount(), result.getStepCount() == 1 ? "" : "s"), "warn");
        }, error -> setBanner("Hint failed: " + describe(error), "error"));
    }

    /** "Solve for me": ให้ solver แก้ต่อจากตำแหน่งปัจจุบัน แล้วเปิดโหมด playback (Prev/Next/Auto-Play) */
    private void solveForMe() {
        if (phase != Phase.PLAY || session == null || session.isWon()) {
            return;
        }
        settleBoard();
        selectTube(-1);
        boardView.clearHint();
        BoardState here = session.state();
        showBusy("Solving from here...", true);
        Task<SolveResult> task = new Task<>() {
            @Override
            protected SolveResult call() {
                return new AStarSolver().solve(here);
            }
        };
        runTask(task, result -> {
            if (!result.isSolved() || result.getMoves().isEmpty()) {
                setBanner("No solution from this position — undo a few moves or restart.", "error");
                return;
            }
            playback = new PlaybackModel(here, result.getMoves());
            solutionPanel.setStats(String.format("A*  ·  %,d states  ·  %d ms", result.getStatesExplored(), result.getTimeMillis()));
            phase = Phase.PLAYBACK;
            boardView.setState(here);
            updatePlaybackUi();
            applyPhase();
        }, error -> setBanner("Solver failed: " + describe(error), "error"));
    }

    private void backToGame() {
        if (session == null) {
            return;
        }
        stopAutoPlay();
        boardView.cancelAnimation();
        animationTarget = null;
        playback = null;
        selectedTube = -1;
        phase = Phase.PLAY;
        boardView.setState(session.state());
        applyPhase();
        updateGameUi();
    }

    private void newGame() {
        boardView.cancelAnimation();
        selectedTube = -1;
        boardView.clearHint();
        phase = Phase.SETUP;
        applyPhase();
        Fx.fadeIn(setupPane, 220);
    }

    private void updateGameUi() {
        gameBar.setStats(session.moves(), session.optimalSteps());
        refreshGameBanner();
        updateControls();
    }

    private void refreshGameBanner() {
        if (session == null) {
            return;
        }
        if (session.isWon()) {
            int moves = session.moves();
            int best = session.optimalSteps();
            setBanner(moves <= best
                    ? "Perfect! Solved in " + moves + " moves — the best possible."
                    : "Solved in " + moves + " moves (best possible: " + best + "). Restart to try for fewer!", "ok");
        } else {
            setBanner("Tap a tube to pick it up, then tap another to pour. "
                    + "You can pour onto the same color or into an empty tube.", "info");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Reset / home
    // ════════════════════════════════════════════════════════════

    private void reset() {
        switch (phase) {
            case PLAYBACK -> resetPlayback();
            case REVIEW, CONFIRMED -> {
                editor = new BoardEditor(originalBoard);
                confirmedBoard = null;
                phase = Phase.REVIEW;
                boardView.setState(originalBoard);
                refreshValidation();
                applyPhase();
            }
            case SOLVING -> cancelTask();
            default -> { }
        }
    }

    private void goHome() {
        stopAutoPlay();
        cancelTask();
        boardView.cancelAnimation();
        playback = null;
        editing = false;
        gameMode = false;
        session = null;
        selectedTube = -1;
        boardView.clearHint();
        phase = Phase.MODE;
        applyPhase();
    }

    // ════════════════════════════════════════════════════════════
    //  Mode selection
    // ════════════════════════════════════════════════════════════

    private void startImageMode() {
        gameMode = false;
        phase = Phase.INPUT;
        applyPhase();
        Fx.fadeIn(inputPage, 220);
    }

    private void startGameMode() {
        gameMode = true;
        phase = Phase.SETUP;
        applyPhase();
        Fx.fadeIn(setupPane, 220);
    }

    // ════════════════════════════════════════════════════════════
    //  Background tasks
    // ════════════════════════════════════════════════════════════

    private <T> void runTask(Task<T> task, Consumer<T> onOk, Consumer<Throwable> onFail) {
        runningTask = task;
        task.setOnSucceeded(e -> {
            if (runningTask != task) {
                return;     // ถูกยกเลิกไปแล้ว
            }
            runningTask = null;
            hideBusy();
            onOk.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            if (runningTask != task) {
                return;
            }
            runningTask = null;
            hideBusy();
            onFail.accept(task.getException());
        });
        Thread thread = new Thread(task, "watersort-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void cancelTask() {
        boolean wasSolving = phase == Phase.SOLVING;
        runningTask = null;    // ผลลัพธ์ที่มาทีหลังจะถูกทิ้ง
        hideBusy();
        if (wasSolving) {
            phase = Phase.CONFIRMED;
            applyPhase();
        }
    }

    private void showBusy(String message, boolean cancellable) {
        busyLabel.setText(message);
        busyCancel.setVisible(cancellable);
        busyCancel.setManaged(cancellable);
        busyOverlay.setVisible(true);
        Fx.fadeIn(busyOverlay, 160);
    }

    private void hideBusy() {
        busyOverlay.setVisible(false);
    }

    private static String describe(Throwable t) {
        if (t == null) {
            return "unknown error";
        }
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    // ════════════════════════════════════════════════════════════
    //  View state
    // ════════════════════════════════════════════════════════════

    private void applyPhase() {
        boolean inWorkspace = phase != Phase.MODE && phase != Phase.INPUT && phase != Phase.SETUP;
        show(modePane.getParent(), phase == Phase.MODE);
        show(inputPage.getParent(), phase == Phase.INPUT);
        show(setupHolder, phase == Phase.SETUP);
        show(workspace, inWorkspace);
        show(bottomBox, inWorkspace);
        show(controls, phase != Phase.PLAY);
        show(gameBar, phase == Phase.PLAY);
        show(newPuzzleButton, phase != Phase.MODE);
        show(stepsBox, !gameMode && phase != Phase.MODE);
        controls.setGameMode(gameMode);
        show(solutionPanel, phase == Phase.PLAYBACK);

        boolean editMode = editing && phase == Phase.REVIEW;
        show(pickerPanel, editMode);
        boardView.setEditable(editMode);
        updateGhost();

        int stepIndex = switch (phase) {
            case MODE, SETUP, PLAY -> -1;   // ขั้นตอนนี้เป็นของโหมดอ่านภาพเท่านั้น
            case INPUT -> 0;
            case REVIEW -> 1;
            case CONFIRMED, SOLVING -> 2;
            case PLAYBACK -> 3;
        };
        for (int i = 0; i < stepLabels.length; i++) {
            Label l = stepLabels[i];
            l.getStyleClass().removeAll("step-active", "step-done");
            if (i == stepIndex) {
                l.getStyleClass().add("step-active");
            } else if (i < stepIndex) {
                l.getStyleClass().add("step-done");
            }
        }

        switch (phase) {
            case REVIEW -> updateReviewBanner();
            case CONFIRMED -> setBanner("Board confirmed. Press Solve to find the shortest solution.", "ok");
            case SOLVING -> setBanner("Searching for the shortest solution...", "info");
            case PLAYBACK -> setBanner("Solution ready — use Prev / Next, or press Auto-Play. Shortcuts: ← → and Space.", "ok");
            case PLAY -> refreshGameBanner();
            default -> { }
        }
        updateControls();
    }

    private void updateReviewBanner() {
        if (validation == null) {
            return;
        }
        if (!validation.isValid()) {
            StringBuilder sb = new StringBuilder(editing
                    ? "Fix these before confirming:"
                    : "The recognized board has problems — click Edit to fix them:");
            List<String> problems = new java.util.ArrayList<>(validation.errors());
            problems.addAll(validation.warnings());
            final int maxShown = 4;
            problems.stream().limit(maxShown).forEach(p -> sb.append("\n•  ").append(p));
            if (problems.size() > maxShown) {
                sb.append("\n•  ...and ").append(problems.size() - maxShown).append(" more");
            }
            setBanner(sb.toString(), "error");
        } else if (!validation.warnings().isEmpty()) {
            StringBuilder sb = new StringBuilder("Looks valid, but:");
            validation.warnings().forEach(w -> sb.append("\n•  ").append(w));
            setBanner(sb.toString(), "warn");
        } else {
            setBanner(editing
                    ? "Board looks valid. Click Confirm when it matches your game."
                    : "Check the board against your game, then Confirm — or click Edit to adjust colors.", "ok");
        }
    }

    private void updateControls() {
        boolean playbackPhase = phase == Phase.PLAYBACK;
        controls.editButton().setDisable(!(phase == Phase.REVIEW || phase == Phase.CONFIRMED || phase == Phase.PLAYBACK));
        controls.editButton().setSelected(editing && phase == Phase.REVIEW);
        controls.confirmButton().setDisable(!(phase == Phase.REVIEW && validation != null && validation.isValid()));
        controls.solveButton().setDisable(phase != Phase.CONFIRMED);
        controls.prevButton().setDisable(!(playbackPhase && playback != null && playback.canPrev()));
        controls.nextButton().setDisable(!(playbackPhase && playback != null && playback.canNext()));
        controls.playButton().setDisable(!playbackPhase);
        controls.resetButton().setDisable(phase == Phase.INPUT);
        recognizeButton.setDisable(selectedFile == null);

        boolean playing = phase == Phase.PLAY && session != null;
        gameBar.undoButton().setDisable(!(playing && session.canUndo()));
        gameBar.hintButton().setDisable(!(playing && !session.isWon()));
        gameBar.solveButton().setDisable(!(playing && !session.isWon()));
        gameBar.restartButton().setDisable(!(playing && session.moves() > 0));
    }

    private void updateGhost() {
        int sel = pickerPanel.getSelectedColor();
        if (sel == ColorPickerPanel.ERASER) {
            boardView.setGhost(null, true);
        } else {
            boardView.setGhost(scheme.color(sel), false);
        }
    }

    private void setBanner(String text, String kind) {
        banner.setText(text);
        banner.getStyleClass().removeAll("banner-info", "banner-ok", "banner-warn", "banner-error");
        banner.getStyleClass().add("banner-" + kind);
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
