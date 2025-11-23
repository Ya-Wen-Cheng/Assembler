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
    private TextField pcField, marField, mbrField, irField, ccField, mfrField;
    private TextField binaryField, octalField;
    private TextArea cacheArea;
    private TextArea printerArea;
    private TextField consoleField;
    

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

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initial display - show all registers as 0 and cache state
        updateRegisterDisplay();
        updateCacheDisplay();
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

    String[] regs = {"PC", "MAR", "MBR", "IR", "CC", "MFR"};
    TextField[] fields = {pcField = new TextField(), marField = new TextField(),
        mbrField = new TextField(), irField = new TextField(), ccField = new TextField(),
        mfrField = new TextField()};

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
                });
                case "MBR" -> btn.setOnAction(e -> {
	                	short mbrVal = Short.parseShort(mbrField.getText());
	                    cpu.setMemoryBufferRegister(mbrVal);
	                    System.out.println("Set Memory Buffer Register to: "+mbrVal);      
                });
                case "PC" -> btn.setOnAction(e -> {
                	cpu.setProgramCounter(Short.parseShort(pcField.getText()));
                	try {
                		System.out.println("Set PC to: "+cpu.getProgramCounter() );
                	}catch(BlankCharArrayException exp) {
                		System.out.println("Nothing is found in PC");
                	}
                	             	
                });
            }

            grid.add(label, 8, row);
            grid.add(tf, 9, row);
            if (!regs[i].equals("IR") && !regs[i].equals("CC") && !regs[i].equals("MFR")) grid.add(btn, 10, row);
            row++;
        }

        return grid;
    }
    
    
    
    
    private VBox createRightPanel() {
        VBox right = new VBox(10);
        right.setAlignment(Pos.TOP_LEFT);
        right.setPadding(new Insets(10, 20, 10, 10));

        Label printerLabel = new Label("Printer");
        printerArea = new TextArea();
        printerArea.setPrefSize(250, 70);
        printerArea.setEditable(false);

        Label consoleLabel = new Label("Console Input");
        consoleField = new TextField();
        consoleField.setPrefSize(250, 150);
        
        // Connect printer device to GUI display
        cpu.printer.setListener(text -> {
            javafx.application.Platform.runLater(() -> {
                if (text != null) {
                    printerArea.appendText(text);
                }
            });
        });
        
        // Connect keyboard device to console input
        consoleField.setOnAction(e -> {
            String input = consoleField.getText();
            if (!input.isEmpty()) {
                cpu.keyboard.pushString(input + "\n");
                System.out.println("Console input sent to keyboard: '" + input + "'");
                consoleField.clear();
            }
        });

        // Cache Content View (Field Engineer Console)
        Label cacheLabel = new Label("Cache Contents (Field Engineer Console)");
        cacheArea = new TextArea();
        cacheArea.setPrefSize(250, 200);
        cacheArea.setEditable(false);
        cacheArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 10px;");

        right.getChildren().addAll(printerLabel, printerArea, consoleLabel, consoleField, 
            cacheLabel, cacheArea);
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
        	try {
        		short address = cpu.getMemoryAddressValue();
	        	if (address >= 0 && address < 4096) {
	        		marField.setText(Short.toString(address));
	        		int val = memory.readFromCache(address);
	        		cpu.setMemoryBufferRegister((short) val);
	        		mbrField.setText(Short.toString((short) val));
	        		System.out.println("memory ["+address+"] = "+val);
	        		updateRegisterDisplay();
	        		updateCacheDisplay();
        		}else {
        			System.out.println("Invalid memory address: " + address + " (must be 0-31)");
        		}
        	}catch(BlankCharArrayException exp) {
        		System.out.println("Nothing is found in MAR");
        	}catch(NullPointerException exp) {
        		System.out.println("Nothing is found in memory");
        	}
        });
        
        Button load_plus = new Button("Load+");
        load_plus.setOnAction(e -> {
        	// Load value from memory address (MAR) into MBR
        	try {
        		short marVal = cpu.getMemoryAddressValue();
	        	if (marVal >= 0 && marVal < 4096) {
	        		Short next_memory = (short)(marVal+1);
	        		cpu.setMemoryAddressRegister(next_memory);
	        		marField.setText(Short.toString(next_memory));
	        		int val = memory.readFromCache(next_memory);
	        		cpu.setMemoryBufferRegister((short) val);
	        		mbrField.setText(Short.toString((short) val));
	        		System.out.println("memory ["+next_memory+"] = "+val);
	        		updateRegisterDisplay();
	        		updateCacheDisplay();
	        		
        		}else {
        			System.out.println("Invalid memory address: " + marVal + " (must be 0-31)");
        		}
        	}catch(BlankCharArrayException exp) {
        		System.out.println("Nothing is found in MAR");
        	}catch(NullPointerException exp) {
        		System.out.println("Nothing is found in memory");
        	}
            
        	
        });

        Button store = new Button("Store");
        store.setOnAction(e -> {
        	try {
        		// memory address and value to be stored
        		short address = cpu.getMemoryAddressValue();
        		marField.setText(Short.toString(address));
        		short mbrVal = Short.parseShort(mbrField.getText());
        		memory.writeToCache(address, mbrVal);
        		System.out.println("Stored memory["+address+"] = "+mbrVal);
        		updateRegisterDisplay();
        		updateCacheDisplay();
        	
        	}catch(BlankCharArrayException exp) { 
        		System.out.println("Nothing is found in MAR");
        	}catch(NumberFormatException exp) {
        		System.out.println("MBR field is empty");
        	}

        });
        
        Button store_plus = new Button("Store+");
        store_plus.setOnAction(e -> {
        	try {
        		// memory address and value to be stored
        		short marVal = cpu.getMemoryAddressValue();
        		short next_memory = (short)(marVal+1);
        		cpu.setMemoryAddressRegister(next_memory);
        		marField.setText(Short.toString(next_memory));
        		short mbrVal = Short.parseShort(mbrField.getText());
        		memory.writeToCache(next_memory, mbrVal);
        		System.out.println("Stored memory["+next_memory+"] = "+mbrVal);
        		updateRegisterDisplay();
        		updateCacheDisplay();
        		
        	
        	}catch(BlankCharArrayException exp) { 
        		System.out.println("Nothing is found in MAR");
        	}catch(NumberFormatException exp) {
        		System.out.println("MBR field is empty");
        	}
            
        });
        

        Button run = new Button("Run");
        run.setOnAction(e -> {
            // Run all instructions continuously until HLT or completion
            try {
            	runAllInstructions();
            }catch(BlankCharArrayException exp) {
        		System.out.println("Nothing found in PC");
        	}
        });

        Button step = new Button("Step");
        step.setOnAction(e -> {
            // Step through one instruction at a time
        	try {
        		stepOneInstruction();
        	}catch(BlankCharArrayException exp) {
        		System.out.println("Nothing found in PC");
        	}
        });

        Button halt = new Button("Halt");
        halt.setOnAction(e -> {
            haltRequested = true;
            System.out.println("Halted - program will stop at next instruction");
        });
        
        Button ipl = new Button("IPL"); 
        ipl.setStyle("-fx-background-color: red;");
        ipl.setOnAction(e -> {
            // Show file chooser for ROM loading
            FileChooser fileChooser = CPU_1_Simple.getROMFileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);

            // Reset system first
            cpu.Reset(memory);
            memory.resetCache();
            haltRequested = false;
            
            // Clear printer display
            printerArea.clear();
            
            // Reconnect printer device to GUI display (in case CPU was reset)
            cpu.printer.setListener(text -> {
                javafx.application.Platform.runLater(() -> {
                    printerArea.appendText(text);
                });
            });
            
            // Update cache display immediately after reset
            updateCacheDisplay();

            if (selectedFile != null) {
                if (cpu.loadROM(selectedFile, memory)) {
                    System.out.println("ROM loaded successfully from: " + selectedFile.getName());
                    refreshGUIAfterIPL();
                    updateCacheDisplay(); // Update cache display after IPL load

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("ROM Load Complete");
                    alert.setHeaderText("ROM Loaded Successfully!");
                    alert.setContentText("Loaded " + memory.data.size() + " memory locations from " + selectedFile.getName());
                    alert.showAndWait();
                } else {
                    System.out.println("Failed to load ROM file: " + selectedFile.getName());
                }
            } else {
                System.out.println("System Reset (no ROM file selected)");
                refreshGUIAfterIPL();
                updateCacheDisplay();
            }
        });

        controlButtons.getChildren().addAll(load, load_plus, store, store_plus, run, step, halt, ipl);
        bottom.setLeft(input);
        bottom.setRight(controlButtons);

        return bottom;
    }

    // Step through one instruction at a time

    private void stepOneInstruction() throws BlankCharArrayException{
    	try {
    		short pc = cpu.getProgramCounter();
    		if (pc >= 0 && pc < 32) {
    			int machineCode = memory.readFromCache(pc);
            // Execute the instruction based on opcode
                executeInstruction(machineCode);
//	                cpu.setProgramCounter(pc++);
	                updateRegisterDisplay();
	                updateCacheDisplay();
//	                System.out.println("PC is now: "+pc++);
    		}
    	}catch(BlankCharArrayException exp) {
    		System.out.println("Nothing is found in PC");
    	}
    }
    
    // Run all instructions continuously until HLT or completion
    private void runAllInstructions() throws BlankCharArrayException{
        haltRequested = false; // Reset halt flag
        short maxInstructions = 1000; // Safety limit to prevent infinite loops
        short instructionCount = 0;

        System.out.println("=== MEMORY DUMP (first 25 entries) ===");
        for (int i = 0; i < 25; i++) {
            Integer val = memory.getValue(i);
            System.out.println("Memory[" + i + "] = " + (val != null ? val : "null"));
        }

        // Check for HLT after memory dump to handle exceptions properly
        try {
            short initialPC = cpu.getProgramCounter();
            if (initialPC >= 0 && initialPC < 32) {
                Integer initialVal = memory.getValue(initialPC);
                if (initialVal != null) {
                    int initialMachineCode = initialVal;
                    int initialOpcode = (initialMachineCode >>> 10) & 0x3F;
                    if (initialOpcode == 0x00) {
                        System.out.println("Program halted (HLT instruction at start)");
                        javafx.application.Platform.runLater(() -> {
                            updateRegisterDisplay();
                            updateCacheDisplay();
                        });
                        return; // Exit early if HLT is the first instruction
                    }
                } else {
                    System.out.println("Warning: Memory at PC=" + initialPC + " is null, cannot execute");
                    javafx.application.Platform.runLater(() -> {
                        updateRegisterDisplay();
                        updateCacheDisplay();
                    });
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking initial instruction: " + e.getMessage());
        }

        while (instructionCount < maxInstructions && !haltRequested) {

 
        	try {
	            short pc = cpu.getProgramCounter();
	            if (pc < 0 || pc >= 32) {
	                break; // Out of bounds
	            }
	            
	            // Check if memory exists before reading
	            Integer memVal = memory.getValue(pc);
	            if (memVal == null) {
	                System.out.println("Memory at PC=" + pc + " is null, stopping execution");
	                break;
	            }
	            
	            int machineCode = memory.readFromCache(pc);
	            
	            // Check for HLT instruction (opcode 0x00)
	            int opcode = (machineCode >>> 10) & 0x3F;
	            if (opcode == 0x00) {
	                System.out.println("Program halted (HLT instruction)");
	                break;
	            }
	            
	            // Execute the instruction
	            executeInstruction(machineCode);
	            
	            // Update displays on JavaFX thread
	            javafx.application.Platform.runLater(() -> {
	                updateRegisterDisplay();
	                updateCacheDisplay();
	            });
	            
	            instructionCount++;
        	}catch(BlankCharArrayException exp) {
        		System.out.println("Nothing is found in PC");
        		break; // Exit loop if PC is blank
        	}catch(Exception exp) {
        		System.out.println("Exception during execution: " + exp.getMessage());
        		// Update displays even on exception
        		javafx.application.Platform.runLater(() -> {
        		    updateRegisterDisplay();
        		    updateCacheDisplay();
        		});
        		// Check if it's a memory null issue, if so break gracefully
        		break;
        	}
        }
        
        // Final update of displays
        javafx.application.Platform.runLater(() -> {
            updateRegisterDisplay();
            updateCacheDisplay();
        });
        
        if (haltRequested) {
            System.out.println("Program stopped: Halt requested by user");
        } else if (instructionCount >= maxInstructions) {
            System.out.println("Program stopped: Maximum instruction limit reached");
        }
        
    }
    
    // Execute a single instruction based on machine code
    private void executeInstruction(int machineCode) {
        try {
            // Use CPU's full instruction execution which handles all opcodes
            cpu.ExecuteInstruction(machineCode, memory);
        } catch (BlankCharArrayException exp) {
            System.out.println("Error executing instruction: " + exp.getMessage());
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
        
        // Update PC
        try {
        	pcField.setText(String.valueOf(cpu.getProgramCounter()));
        	marField.setText(String.valueOf(cpu.getMemoryAddressValue()));
        	mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
        }catch(BlankCharArrayException exp) {}
        
        
        try {
            irField.setText("0");
            ccField.setText(String.valueOf(cpu.getConditionCode()));
            mfrField.setText(String.valueOf(cpu.getMemoryFaultRegister()));
        }catch(Exception exp) {
        	System.out.println(exp.getMessage());
        }
        
        // Update cache display
        updateCacheDisplay();
    }
    
    // Update cache content display
    private void updateCacheDisplay() {
        try {
            Cache.CacheLineInfo[] cacheLines = memory.getCache().getCacheLines();
            StringBuilder cacheText = new StringBuilder();
            cacheText.append("=== CACHE CONTENTS ===\n");
            cacheText.append(String.format("%-8s %-8s %-12s %-8s %-8s %-12s\n", 
                "Line", "Address", "Data", "Valid", "Dirty", "Status"));
            cacheText.append("------------------------------------------------------------\n");
            
            for (Cache.CacheLineInfo line : cacheLines) {
                String status = line.lastAccess.isEmpty() ? "-" : line.lastAccess;
                cacheText.append(String.format("%-8d %-8d %-12d %-8s %-8s %-12s\n",
                    line.lineIndex,
                    line.address >= 0 ? line.address : -1,
                    line.data,
                    line.valid ? "Yes" : "No",
                    line.dirty ? "Yes" : "No",
                    status));
            }
            cacheText.append("============================================================\n");
            cacheArea.setText(cacheText.toString());
        } catch (Exception e) {
            cacheArea.setText("Error displaying cache: " + e.getMessage());
        }
    }

    // 🔹 Refresh GUI fields after IPL load or reset
    private void refreshGUIAfterIPL() {
        // Update GPR values
        for (int i = 0; i < 4; i++) {
            short val = cpu.getGPR((short) i);
            gprFields[i].setText(String.valueOf(val));
        }

        // Update IXR values
        for (int i = 0; i < 3; i++) {
            short val = cpu.getIXR((short) (i + 1));
            ixrFields[i].setText(String.valueOf(val));
        }

     // Update PC
        try {
        	pcField.setText(String.valueOf(cpu.getProgramCounter()));
        	marField.setText(String.valueOf(cpu.getMemoryAddressValue()));
        	mbrField.setText(String.valueOf(cpu.getMemoryBufferValue()));
        }catch(BlankCharArrayException exp) {}
        
        
        try {
            irField.setText("0");
            ccField.setText(String.valueOf(cpu.getConditionCode()));
            mfrField.setText(String.valueOf(cpu.getMemoryFaultRegister()));
        }catch(Exception exp) {
        	System.out.println(exp.getMessage());
        }
        
        // Update cache display (should show all invalid after reset)
        updateCacheDisplay();

        // Log confirmation for debugging
        System.out.println("[GUI] Registers updated after IPL load");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
