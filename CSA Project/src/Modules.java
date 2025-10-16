import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Modules {
    public TextArea printerArea;
    public TextField consoleInput;

    public Modules(TextArea printerArea, TextField consoleInput) {
        this.printerArea = printerArea;
        this.consoleInput = consoleInput;
    }

    public void print(String message) {
        printerArea.appendText(message + "\n");
    }

    public String readInput() {
        return consoleInput.getText();
    }

    public void clear() {
        printerArea.clear();
        consoleInput.clear();
    }
}
