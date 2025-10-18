import javafx.event.ActionEvent;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class GUI extends Application {

    private CPU cpu = new CPU();
    private Memory memory = new Memory();

    private TextField[] gprFields = new TextField[4];
    private TextField[] ixrFields = new TextField[3];
    private TextField pcField, marField, mbrField, irField, ccField;
    private TextField binaryField, octalField;
    

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSCI 6461 Machine Simulator");

        HBox root = new HBox(50);
        VBox leftchild = new VBox(50);

        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #C8E1F5;");

        GridPane leftPanel = createRegisterPanel();
        VBox rightPanel = createRightPanel();
        BorderPane bottomPanel = createBottomPanel();

        leftchild.getChildren().addAll(leftPanel, bottomPanel);
        root.getChildren().addAll(leftchild, rightPanel);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createRegisterPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 20, 10, 20));

        Label gprLabel = new Label("GPR");
        Label ixrLabel = new Label("IXR");
        grid.add(gprLabel, 0, 0);
        grid.add(ixrLabel, 4, 0);

        for (int i = 0; i < 4; i++) {
            gprFields[i] = new TextField();
            gprFields[i].setPrefWidth(70);
            int index = i;
            Button btn = new Button("↔");
            btn.setOnAction(e -> {
                short val = Short.parseShort(gprFields[index].getText());
                cpu.setGPR((short) index, val);
            });
            grid.add(new Label("R" + i), 0, i + 1);
            grid.add(gprFields[i], 1, i + 1);
            grid.add(btn, 2, i + 1);
        }

        for (int i = 0; i < 3; i++) {
            ixrFields[i] = new TextField();
            ixrFields[i].setPrefWidth(70);
            int index = i;
            Button btn = new Button("↔");
            btn.setOnAction(e -> {
                short val = Short.parseShort(ixrFields[index].getText());
                cpu.setIXR((short) (index + 1), val);
            });
            grid.add(new Label("X" + (i + 1)), 4, i + 1);
            grid.add(ixrFields[i], 5, i + 1);
            grid.add(btn, 6, i + 1);
        }

        String[] regs = {"PC", "MAR", "MBR", "IR", "CC"};
        TextField[] fields = {pcField = new TextField(), marField = new TextField(),
                mbrField = new TextField(), irField = new TextField(), ccField = new TextField()};

        int row = 1;
        for (int i = 0; i < regs.length; i++) {
            Label label = new Label(regs[i]);
            TextField tf = fields[i];
            tf.setPrefWidth(100);
            Button btn = new Button("↔");

            switch (regs[i]) {
                case "MAR" -> btn.setOnAction(e -> {
                    short marVal = Short.parseShort(marField.getText());
                    cpu.setMemoryAddressRegister(marVal);
                    cpu.Execute(memory);
                    mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
                });
                case "MBR" -> btn.setOnAction(e -> {
                    short val = Short.parseShort(mbrField.getText());
                    memory.setValue(cpu.getMemoryAddressValue(), val);
                });
                case "PC" -> btn.setOnAction(e -> {
                	cpu.setProgramCounter(Short.parseShort(pcField.getText()));
                	System.out.print(cpu.getProgramCounter());
                });
            }

            grid.add(label, 8, row);
            grid.add(tf, 9, row);
            if (!regs[i].equals("IR") && !regs[i].equals("CC")) grid.add(btn, 10, row);
            row++;
        }

        return grid;
    }

    private VBox createRightPanel() {
        VBox right = new VBox(10);
        right.setAlignment(Pos.TOP_LEFT);
        right.setPadding(new Insets(10, 20, 10, 10));

        Label printerLabel = new Label("Printer");
        TextArea printerArea = new TextArea();
        printerArea.setPrefSize(250, 70);

        Label consoleLabel = new Label("Console Input");
        TextField consoleField = new TextField();
        consoleField.setPrefSize(250, 150);

        right.getChildren().addAll(printerLabel, printerArea, consoleLabel, consoleField);
        return right;
    }

    private BorderPane createBottomPanel() {
        BorderPane bottom = new BorderPane();

        HBox input = new HBox(15);
        binaryField = new TextField();
        binaryField.setPrefWidth(150);
        octalField = new TextField("0");
        octalField.setPrefWidth(80);
        input.getChildren().addAll(new Label("Binary"), binaryField, new Label("Octal"), octalField);

        VBox controlButtons = new VBox(10);
        Button load = new Button("Load");
        load.setOnAction(e -> cpu.Execute(memory));
        

        Button store = new Button("Store");
        store.setOnAction(e -> System.out.println("Store clicked"));

        Button run = new Button("Run");
        run.setOnAction(e -> cpu.Execute(memory));

        Button step = new Button("Step");
        step.setOnAction(e -> cpu.Execute(memory));

        Button halt = new Button("Halt");
        halt.setOnAction(e -> System.out.println("Halt clicked"));

        Button ipl = new Button("IPL"); 
        ipl.setStyle("-fx-background-color: red;");
        ipl.setOnAction(e -> {
            cpu.Reset(memory);
            System.out.println("System Reset");
        });

        controlButtons.getChildren().addAll(load, store, run, step, halt, ipl);
        bottom.setLeft(input);
        bottom.setRight(controlButtons);

        return bottom;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
