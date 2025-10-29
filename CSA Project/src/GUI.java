import javafx.event.ActionEvent;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.io.File;

public class GUI extends Application {


    private CPU_1_Simple cpu = new CPU_1_Simple();
    private Memory memory = new Memory();
    private boolean haltRequested = false;

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
                    System.out.println("Set Memory Address Register to: "+marVal);
                    cpu.Execute(memory);
                    mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
                });
                case "MBR" -> btn.setOnAction(e -> {
                    short val = Short.parseShort(mbrField.getText());
                    memory.setValue(cpu.getMemoryAddressValue(), val);
                });
                case "PC" -> btn.setOnAction(e -> {
                	cpu.setProgramCounter(Short.parseShort(pcField.getText()));
                	System.out.println("Set PC to: "+cpu.getProgramCounter() );             	
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
        load.setOnAction(e -> {
            // Load value from memory address (MAR) into MBR
        	
            short marVal = cpu.getMemoryAddressValue();
            System.out.println("DEBUG: Load button - MAR from GUI field: " + marVal);
            if (marVal >= 0 && marVal < 32) {
                // Set MAR and execute memory read
                cpu.setMemoryAddressRegister(marVal);
                cpu.Execute(memory);
                
                // Update MBR display with the loaded value
                short mbrVal = cpu.getMemoryBufferValue();
                mbrField.setText(String.valueOf(mbrVal));
                
                // Update all register displays
                updateRegisterDisplay();
                System.out.println("Loaded value " + mbrVal + " from memory address " + marVal);
            } else {
                System.out.println("Invalid memory address: " + marVal + " (must be 0-31)");
            }
        });

        Button store = new Button("Store");
        store.setOnAction(e -> {
            // Store instruction - store MBR value to memory at MAR address
            short marVal = cpu.getMemoryAddressValue();
            short mbrVal = cpu.getMemoryBufferValue();
            if (marVal >= 0 && marVal < 32) {
                memory.setValue(marVal, mbrVal);
                System.out.println("Stored value " + mbrVal + " to memory address " + marVal);
                updateRegisterDisplay();
            } else {
                System.out.println("Invalid memory address: " + marVal);
            }
        });

        Button run = new Button("Run");
        run.setOnAction(e -> {
            // Run all instructions continuously until HLT or completion
            runAllInstructions();
        });

        Button step = new Button("Step");
        step.setOnAction(e -> {
            // Step through one instruction at a time
            stepOneInstruction();
        });

        Button halt = new Button("Halt");
        halt.setOnAction(e -> {
            haltRequested = true;
            System.out.println("Halt requested - program will stop at next instruction");
        });

        Button ipl = new Button("IPL"); 
        ipl.setStyle("-fx-background-color: red;");
        ipl.setOnAction(e -> {
            // Show file chooser for ROM loading
            FileChooser fileChooser = CPU_1_Simple.getROMFileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);
            
            if (selectedFile != null) {
                // Reset system first
                cpu.Reset(memory);
                haltRequested = false; // Reset halt flag
                
                // Load ROM file
                if (cpu.loadROM(selectedFile, memory)) {
                    System.out.println("ROM loaded successfully from: " + selectedFile.getName());
                    // Update GUI display with loaded values
                    updateRegisterDisplay();
                } else {
                    System.out.println("Failed to load ROM file: " + selectedFile.getName());
                }
            } else {
                // Just reset if no file selected
                cpu.Reset(memory);
                haltRequested = false; // Reset halt flag
                System.out.println("System Reset");
            }
        });

        controlButtons.getChildren().addAll(load, store, run, step, halt, ipl);
        bottom.setLeft(input);
        bottom.setRight(controlButtons);

        return bottom;
    }

    // Step through one instruction at a time
    private void stepOneInstruction() {
        short pc = cpu.getProgramCounter();
        if (pc >= 0 && pc < 32) {
            Integer machineCode = memory.getValue(pc);
            if (machineCode != null) {
                // Execute the instruction based on opcode
                executeInstruction(machineCode);
                updateRegisterDisplay();
            }
        }
    }
    
    // Run all instructions continuously until HLT or completion
    private void runAllInstructions() {
        haltRequested = false; // Reset halt flag
        short maxInstructions = 1000; // Safety limit to prevent infinite loops
        short instructionCount = 0;
        
        while (instructionCount < maxInstructions && !haltRequested) {
            short pc = cpu.getProgramCounter();
            if (pc < 0 || pc >= 32) {
                break; // Out of bounds
            }
            
            Integer machineCode = memory.getValue(pc);
            if (machineCode == null) {
                break; // No instruction at this address
            }
            
            // Check for HLT instruction (opcode 0x00)
            int opcode = (machineCode >>> 10) & 0x3F;
            if (opcode == 0x00) {
                System.out.println("Program halted (HLT instruction)");
                break;
            }
            
            // Execute the instruction
            executeInstruction(machineCode);
            instructionCount++;
        }
        
        if (haltRequested) {
            System.out.println("Program stopped: Halt requested by user");
        } else if (instructionCount >= maxInstructions) {
            System.out.println("Program stopped: Maximum instruction limit reached");
        }
        
        updateRegisterDisplay();
    }
    
    // Execute a single instruction based on machine code
    private void executeInstruction(int machineCode) {
        int opcode = (machineCode >>> 10) & 0x3F;
        int r = (machineCode >>> 8) & 0x3;
        int x = (machineCode >>> 6) & 0x3;
        int i = (machineCode >>> 5) & 0x1;
        int address = machineCode & 0x1F;
        
        switch (opcode) {
            case 0x03: // LDA - Load Address
                cpu.ExecuteLDA((short) r, (short) x, (short) address, memory);
                // Increment PC for next instruction
                short currentPC = cpu.getProgramCounter();
                cpu.setProgramCounter((short) (currentPC + 1));
                break;
            case 0x00: // HLT - Halt
                System.out.println("HLT instruction executed");
                break;
            default:
                // For other instructions, just increment PC
                short pc = cpu.getProgramCounter();
                cpu.setProgramCounter((short) (pc + 1));
                break;
        }
    }

    // Update register display in GUI
    private void updateRegisterDisplay() {
        // Update GPR fields
        for (int i = 0; i < 4; i++) {
            short value = cpu.getGPR((short) i);
            gprFields[i].setText(String.valueOf(value));
        }
        
        // Update IXR fields
        for (int i = 0; i < 3; i++) {
            short value = cpu.getIXR((short) (i + 1));
            ixrFields[i].setText(String.valueOf(value));
        }
        
        // Update other registers
        pcField.setText(String.valueOf(cpu.getProgramCounter()));
        marField.setText(String.valueOf(cpu.getMemoryAddressValue()));
        mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
        irField.setText("0"); // Instruction register not directly accessible
        ccField.setText(String.valueOf(cpu.getConditionCode()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
