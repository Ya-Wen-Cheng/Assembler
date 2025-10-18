import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Modules {
    public TextArea printerArea;
    public TextField consoleInput;

    // Constructor to initialize printer and console input fields
    public Modules(TextArea printerArea, TextField consoleInput) {
        this.printerArea = printerArea;
        this.consoleInput = consoleInput;
    }

    // Print a message to the printer area
    public void print(String message) {
        printerArea.appendText(message + "\n");
    }

    // Read input from the console input field
    public String readInput() {
        return consoleInput.getText();
    }

    // Clear both printer and console input fields
    public void clear() {
        printerArea.clear();
        consoleInput.clear();
    }
}
