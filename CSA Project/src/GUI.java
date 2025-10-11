
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.application.Application;
import javafx.geometry.HPos;
//import from javafx.geometry
import javafx.geometry.Insets;
import javafx.geometry.Pos;


//essential elements on GUI
import javafx.scene.Scene;
import javafx.scene.control.*; //include TextField, TextArea and Button
import javafx.scene.layout.*;
import javafx.stage.*;

//libraries allow us to adjust GUI
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.*;


public class GUI extends Application{
	    @Override
	    public void start(Stage primaryStage) {
	        primaryStage.setTitle("CSCI 6461 Machine Simulator");

	        HBox root = new HBox(50);
	       
	        VBox leftchild = new VBox(50);
	        
	        root.setPadding(new Insets(10));
	        root.setStyle("-fx-background-color: #C8E1F5;"); // Light blue background

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

	        // GPR (0–3)
	        
	        for (int i = 0; i < 4; i++) {
	            TextField tf = new TextField();
	            tf.setPrefWidth(70);
	            Button blueBtn = createGPRButton(Color.CYAN, i);
	            grid.add(new Label(String.valueOf(i)), 0, i + 1);
	            grid.add(tf, 1, i + 1);
	            grid.add(blueBtn, 2, i + 1);
	        }

	        // IXR (1–3)
	        for (int i = 0; i < 3; i++) {
	            TextField tf = new TextField();
	            tf.setPrefWidth(70);
	            Button blueBtn = createIXRButton(Color.CYAN, i);
	            grid.add(new Label(String.valueOf(i + 1)), 4, i + 1);
	            grid.add(tf, 5, i + 1);
	            grid.add(blueBtn, 6, i + 1);
	        }

	        // PC, MAR, MBR, IR
	        String[] rightRegs = {"PC", "MAR", "MBR", "IR", "CC", "MFR"};
	        int[] textFieldWidth = {70, 70, 120, 120, 40, 40};
	        int row = 1;
	        for (int i = 0; i<rightRegs.length; i++) {
	            Label label = new Label(rightRegs[i]);
	            TextField tf = new TextField();;
	            Button blueBtn = createButton(Color.CYAN, rightRegs[i]);
	            grid.add(label, 8, row);
	            tf.setMaxWidth(textFieldWidth[i]);
	            grid.add(tf, 9, row);
	            if (row < 4) {
	            	grid.add(blueBtn, 10, row);
	            }
	            row++;
	        }

	        return grid;
	    }

	    private VBox createRightPanel() {
	        VBox right = new VBox(10);
	        right.setAlignment(Pos.TOP_LEFT);
	        right.setPadding(new Insets(10, 20, 10, 10));

	        Label cacheLabel = new Label("Cache Content (Not Include In Project 1)");
	        TextArea cacheArea = new TextArea();
	        cacheArea.setPrefSize(250, 150);

	        Label printerLabel = new Label("Printer");
	        TextArea printerArea = new TextArea();
	        printerArea.setPrefSize(250, 70);

	        Label consoleLabel = new Label("Console Input");
	        TextField consoleField = new TextField();
	        consoleField.setPrefSize(250, 150);
	        right.setPrefWidth(600);
	        right.getChildren().addAll(cacheLabel, cacheArea, printerLabel, printerArea, consoleLabel, consoleField);
	        return right;
	    }

	    private BorderPane createBottomPanel() {
	        BorderPane bottom = new BorderPane();   
	        
	        


	        // Binary and Octal
	        HBox bBox = new HBox(10);
	        bBox.setAlignment(Pos.CENTER_LEFT);
	        HBox oBox = new HBox(10);
	        oBox.setAlignment(Pos.CENTER_LEFT);
	        

	        Label binLabel = new Label("BINARY");
	        TextField binField = new TextField();
	        binField.setPrefWidth(180);

	        Label octLabel = new Label("OCTAL INPUT");
	        TextField octField = new TextField("0");
	        octField.setPrefWidth(80);

	        bBox.getChildren().addAll(binLabel, binField);
	        oBox.getChildren().addAll(octLabel, octField);
	        
	        VBox input = new VBox(10);
	        input.getChildren().addAll(bBox, oBox);
	        

	        // Load & Store
	        VBox buttonBox = new VBox(10);
	        buttonBox.setPadding(new Insets(0, 0, 0, 10));
	        buttonBox.setAlignment(Pos.TOP_CENTER);
	        
	        Button load = new Button("Load");
	        load.setPrefWidth(70);
	        load.setOnAction(e -> System.out.println("Load clicked"));
	        
	        Button store = new Button("Store");
	        store.setPrefWidth(70);
	        store.setOnAction(e -> System.out.println("Store clicked"));
	        
	        buttonBox.getChildren().addAll(load, store);
	       
	        
	        
	        //Run, Step, Halt, IPL
	        VBox ExecutionBox = new VBox(10);
	        ExecutionBox.setAlignment(Pos.CENTER);
	        
	        Button run = new Button("Run");
	        run.setPrefWidth(70);
	        run.setOnAction(e -> System.out.println("Run clicked"));
	        
	        Button step = new Button("Step");
	        step.setPrefWidth(70);
	        step.setOnAction(e -> System.out.println("Step clicked"));
	        
	        Button halt = new Button("Halt");
	        halt.setPrefWidth(70);
	        halt.setOnAction(e -> System.out.println("Halt clicked"));
	        
	        Button ipl = new Button("IPL");
	        ipl.setPrefWidth(70);
	        ipl.setOnAction(e -> System.out.println("IPL clicked"));
	        ipl.setStyle("-fx-background-color: red;");
	 
	        ExecutionBox.setPrefWidth(150);
	        ExecutionBox.getChildren().addAll(run, step, halt, ipl);
     

	        // Program File field
	        HBox container = new HBox();
	        container.setPadding(new Insets(30, 0, 0, 0));
	        TextField programFile = new TextField();
	        programFile.setPrefWidth(850);
	        programFile.setPromptText("Program File");
	        container.getChildren().add(programFile);
	        

	        bottom.setRight(ExecutionBox);
	        bottom.setCenter(buttonBox);
	        bottom.setLeft(input);
	        bottom.setBottom(container);
	        return bottom;
	    }
	    
	    
	    // GPR Buttons
	    private Button createGPRButton(Color color, int index) {
	        Button button = new Button();
	        button.setPrefSize(20, 20);
	        button.setStyle(
	                         "-fx-min-width: 20px;"
	                        + "-fx-min-height: 20px;"
	                        + "-fx-background-color: " + toRgbString(color) + ";"
	                        + "-fx-border-color: gray;"
	        );
	        
	        EventHandler<ActionEvent> handler = GPRhandler(index);
        	button.setOnAction(handler);
        
        	return button;
	    }
	    
	    //GPR handler
	    private EventHandler<ActionEvent> GPRhandler(int index){
	    	switch (index) {
	    		case 0:
	    			return e -> System.out.println("R0 clicked");
	    		case 1:
	    			return e -> System.out.println("R1 clicked");
	    		case 2:
	    			return e -> System.out.println("R2 clicked");
	    		case 3:
	    			return e -> System.out.println("R3 clicked");
	    		default:
	                return e -> System.out.println("Unknown index: " + index);
	    	} 
	    }
	    
	    
	    // IXR buttons
	    private Button createIXRButton(Color color, int index) {
	        Button button = new Button();
	        button.setPrefSize(20, 20);
	        button.setStyle(
	                         "-fx-min-width: 20px;"
	                        + "-fx-min-height: 20px;"
	                        + "-fx-background-color: " + toRgbString(color) + ";"
	                        + "-fx-border-color: gray;"
	        );
	        EventHandler<ActionEvent> handler = IXRhandler(index);
	        button.setOnAction(handler);
	        return button;
	    }
	    
	    //IXR handler
	    private EventHandler<ActionEvent> IXRhandler(int index){
	    	switch (index) {
	    		case 0:
	    			return e -> System.out.println("X0 clicked");
	    		case 1:
	    			return e -> System.out.println("X1 clicked");
	    		case 2:
	    			return e -> System.out.println("X2 clicked");
	    		default:
	                return e -> System.out.println("Unknown index: " + index);
	    	} 
	    }
	
	    
	    // Other Register Buttons
	    private Button createButton(Color color, String str) {
	        Button button = new Button();
	        button.setPrefSize(20, 20);
	        button.setStyle(
	                         "-fx-min-width: 20px;"
	                        + "-fx-min-height: 20px;"
	                        + "-fx-background-color: " + toRgbString(color) + ";"
	                        + "-fx-border-color: gray;"
	        );
	        EventHandler<ActionEvent> handler = handler(str);
	        button.setOnAction(handler);
	        return button;
	    }
	    
	    // Other Register Handler
	    private EventHandler<ActionEvent> handler(String str){
	    	if (str.equals("PC")) {
	            return e -> System.out.println("PC clicked");
	        } else if (str.equals("MAR")) {
	            return e -> System.out.println("MAR clicked");
	        } else if (str.equals("MBR")) {
	            return e -> System.out.println("MBR clicked");
	        } else {
	            return e -> System.out.println("Unknown register");
	        }
	    } 
	    	    
	    private String toRgbString(Color c) {
	        return String.format("rgb(%d,%d,%d)",
	                (int) (c.getRed() * 255),
	                (int) (c.getGreen() * 255),
	                (int) (c.getBlue() * 255));
	    }  
	    
	    
	    public static void main(String[] args) {
	        launch(args);
	    }
}
	
	
