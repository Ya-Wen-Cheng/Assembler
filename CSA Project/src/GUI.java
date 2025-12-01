import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Scanner;

public class GUI extends Application {

    private final CPU_1_Simple cpu = new CPU_1_Simple();
    private final Memory memory = new Memory();

    private volatile boolean haltRequested = false;
    private volatile boolean running = false;

    private TextField[] gprFields = new TextField[4];
    private TextField[] ixrFields = new TextField[3];
    private TextField pcField, marField, mbrField, irField, ccField;
    private TextArea printerArea;

    // GUI-side paragraph + last word
    private String loadedParagraph = "";
    private String lastWord = "";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSCI 6461 Machine Simulator — Program 2");

        HBox root = new HBox(50);
        VBox leftchild = new VBox(30);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #C8E1F5;");

        GridPane leftPanel = createRegisterPanel();
        VBox rightPanel = createRightPanel();
        BorderPane bottomPanel = createBottomPanel();

        leftchild.getChildren().addAll(leftPanel, bottomPanel);
        root.getChildren().addAll(leftchild, rightPanel);

        Scene scene = new Scene(root, 1100, 640);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateRegisterDisplay();
    }

    // ---------------------------------------------------------
    //  REGISTER PANEL (GPR, IXR, PC, MAR, MBR, IR, CC)
    // ---------------------------------------------------------
    private GridPane createRegisterPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 20, 10, 20));

        grid.add(new Label("GPR"), 0, 0);
        grid.add(new Label("IXR"), 4, 0);

        // GPRs
        for (int i = 0; i < 4; i++) {
            gprFields[i] = new TextField();
            gprFields[i].setPrefWidth(80);
            int idx = i;

            Button btn = new Button("↔");
            btn.setOnAction(e -> {
                short val = parseShortSafe(gprFields[idx].getText());
                cpu.setGPR((short) idx, val);
                updateRegisterDisplay();
            });

            grid.add(new Label("R" + i), 0, i + 1);
            grid.add(gprFields[i], 1, i + 1);
            grid.add(btn, 2, i + 1);
        }

        // IXRs
        for (int i = 0; i < 3; i++) {
            ixrFields[i] = new TextField();
            ixrFields[i].setPrefWidth(80);
            int idx = i + 1;

            Button btn = new Button("↔");
            btn.setOnAction(e -> {
                short val = parseShortSafe(ixrFields[idx - 1].getText());
                cpu.setIXR((short) idx, val);
                updateRegisterDisplay();
            });

            grid.add(new Label("X" + (i + 1)), 4, i + 1);
            grid.add(ixrFields[i], 5, i + 1);
            grid.add(btn, 6, i + 1);
        }

        // PC, MAR, MBR, IR, CC
        pcField = new TextField();
        marField = new TextField();
        mbrField = new TextField();
        irField = new TextField();
        ccField = new TextField();

        TextField[] fields = { pcField, marField, mbrField, irField, ccField };
        String[] names = { "PC", "MAR", "MBR", "IR", "CC" };

        int row = 1;
        for (int i = 0; i < names.length; i++) {
            Label lbl = new Label(names[i]);
            TextField tf = fields[i];
            tf.setPrefWidth(120);

            Button btn = new Button("↔");

            switch (names[i]) {
                case "PC" -> btn.setOnAction(e -> {
                    cpu.setProgramCounter(parseShortSafe(pcField.getText()));
                    updateRegisterDisplay();
                });
                case "MAR" -> btn.setOnAction(e -> {
                    short v = parseShortSafe(marField.getText());
                    cpu.setMemoryAddressRegister(v);
                    cpu.memReadToMBR(memory, v);
                    mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
                    updateRegisterDisplay();
                });
                case "MBR" -> btn.setOnAction(e -> {
                    short v = parseShortSafe(mbrField.getText());
                    memory.writeToCache(cpu.getMemoryAddressValue(), v);
                    updateRegisterDisplay();
                });
                default -> btn.setDisable(true);
            }

            grid.add(lbl, 8, row);
            grid.add(tf, 9, row);
            if (!names[i].equals("IR") && !names[i].equals("CC"))
                grid.add(btn, 10, row);

            row++;
        }

        return grid;
    }

    // ---------------------------------------------------------
    // RIGHT PANEL (Printer + Console)
    // ---------------------------------------------------------
    private VBox createRightPanel() {
        VBox right = new VBox(10);
        right.setAlignment(Pos.TOP_LEFT);
        right.setPadding(new Insets(10, 20, 10, 10));

        // Printer
        printerArea = new TextArea();
        printerArea.setPrefSize(320, 220);
        printerArea.setEditable(false);

        // Console Input
        TextField consoleField = new TextField();
        consoleField.setPrefWidth(320);

        consoleField.setOnAction(e -> {
            String text = consoleField.getText();
            if (text == null) text = "";
            lastWord = text.trim();
            cpu.keyboard.pushString(text + "\n");
            consoleField.clear();
        });

        // Printer → GUI
        cpu.printer.setListener(s ->
                Platform.runLater(() -> printerArea.appendText(s))
        );

        right.getChildren().addAll(new Label("Printer"), printerArea,
                new Label("Console Input"), consoleField);
        return right;
    }

    // ---------------------------------------------------------
    //  BOTTOM CONTROL PANEL
    // ---------------------------------------------------------
    private BorderPane createBottomPanel() {
        BorderPane bottom = new BorderPane();
        VBox controls = new VBox(10);

        Button run = new Button("Run");
        run.setOnAction(e -> runProgramAsync());

        Button step = new Button("Step");
        step.setOnAction(e -> stepOne());

        Button ipl = new Button("IPL");
        ipl.setStyle("-fx-background-color: red;");
        ipl.setOnAction(this::handleIPL);

        Button loadCard = new Button("Load Card");
        loadCard.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File f = fc.showOpenDialog(null);
            if (f != null) {
                try (Scanner sc = new Scanner(f)) {
                    sc.useDelimiter("\\Z");
                    loadedParagraph = sc.hasNext() ? sc.next() : "";
                    cpu.cardReader.loadText(loadedParagraph);
                    logToPrinter("[Card] Loaded " + f.getName() + "\n");
                } catch (Exception ex) {
                    logToPrinter("[Card] Load failed.\n");
                }
            }
        });

        // --- DORMANT BUTTONS (Do Nothing) ---
        Button loadPlus = new Button("Load+");
        Button storePlus = new Button("Store+");

        // no setOnAction -> they do absolutely nothing

        controls.getChildren().addAll(
                run,
                step,
                ipl,
                loadCard,
                loadPlus,
                storePlus
        );

        bottom.setRight(controls);
        return bottom;
    }

    // ---------------------------------------------------------
    // IPL
    // ---------------------------------------------------------
    private void handleIPL(ActionEvent e) {
        FileChooser fc = CPU_1_Simple.getROMFileChooser();
        File f = fc.showOpenDialog(null);

        cpu.Reset(memory);
        haltRequested = false;
        loadedParagraph = "";
        lastWord = "";

        if (f != null) {
            int first = -1;
            try (Scanner sc = new Scanner(f)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty() || line.startsWith(";")) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) continue;

                    int addr = Integer.parseInt(parts[0], 8);
                    int val = Integer.parseInt(parts[1], 8);
                    memory.setValue(addr, val);

                    if (first == -1 || addr < first) first = addr;
                }
            } catch (Exception ex) {
                logToPrinter("[IPL] Failed.\n");
                return;
            }

            if (first >= 0) cpu.setProgramCounter((short) first);
            logToPrinter("[IPL] Loaded ROM.\n");
        }
        updateRegisterDisplay();
        cpu.setProgramCounter((short) 012);   // <-- Octal 12 = decimal 10

    }

    // ---------------------------------------------------------
    //  STEP ONE
    // ---------------------------------------------------------
    private void stepOne() {
        short pc = cpu.getProgramCounter();
        Integer mc = memory.readFromCache(pc);
        if (mc == null) {
            logToPrinter("[Step] No instruction.\n");
            return;
        }

        boolean cont = cpu.ExecuteInstruction(mc, memory);
        updateRegisterDisplay();

        if (!cont) {
            logToPrinter("[Step] HLT.\n");
        }
    }

    // ---------------------------------------------------------
    // RUN LOOP WITH AUTO SEARCH ON HLT
    // ---------------------------------------------------------
    private void runProgramAsync() {
        if (running) return;
        running = true;

        new Thread(() -> {
            while (!haltRequested) {
                short pc = cpu.getProgramCounter();
                Integer mc = memory.readFromCache(pc);
                if (mc == null) break;

                boolean cont = cpu.ExecuteInstruction(mc, memory);
                if (!cont) {
                    Platform.runLater(() -> {});
                    break;
                }

                Platform.runLater(this::updateRegisterDisplay);
            }
            running = false;
        }).start();
    }

    // ---------------------------------------------------------
    private short parseShortSafe(String s) {
        try { return Short.parseShort(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private void updateRegisterDisplay() {
        for (int i = 0; i < 4; i++)
            gprFields[i].setText(String.valueOf(cpu.getGPR((short) i)));

        for (int i = 0; i < 3; i++)
            ixrFields[i].setText(String.valueOf(cpu.getIXR((short) (i + 1))));

        pcField.setText(String.valueOf(cpu.getProgramCounter()));
        marField.setText(String.valueOf(cpu.getMemoryAddressValue()));
        mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
        irField.setText("—");
        ccField.setText(String.valueOf(cpu.getConditionCode()));
    }

    private void logToPrinter(String s) {
        cpu.printer.write(s);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

