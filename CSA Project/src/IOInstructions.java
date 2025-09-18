// IOInstructions.java
import java.util.Scanner;

public class IOInstructions {
    private Scanner scanner = new Scanner(System.in);

    // IN r, devid: Input Character To Register from Device
    // r = 0..3
    public void Op61(GeneralRegister R, int r, int devid) {
        int input = 0;
        switch (devid) {
            case 0: // Keyboard
                System.out.print("Enter a character for device 0 (keyboard): ");
                String line = scanner.nextLine();
                if (!line.isEmpty()) {
                    input = line.charAt(0);
                }
                break;
            case 1: // Console (simulate as keyboard)
                System.out.print("Enter a character for device 1 (console): ");
                line = scanner.nextLine();
                if (!line.isEmpty()) {
                    input = line.charAt(0);
                }
                break;
            case 2: // Printer (no input, return 0)
                input = 0;
                break;
            default:
                System.out.println("Unknown device ID: " + devid);
        }
        R.setValue(r, input);
    }

    // OUT r, devid: Output Character to Device from Register
    // r = 0..3
    public void Op62(GeneralRegister R, int r, int devid) {
        int value = R.getValue(r);
        char ch = (char) (value & 0xFF);
        switch (devid) {
            case 0: // Keyboard (simulate as console output)
                System.out.println("Device 0 (keyboard) output: " + ch);
                break;
            case 1: // Console
                System.out.println("Device 1 (console) output: " + ch);
                break;
            case 2: // Printer
                System.out.println("Device 2 (printer) output: " + ch);
                break;
            default:
                System.out.println("Unknown device ID: " + devid);
        }
    }

    // CHK r, devid: Check Device Status to Register
    // r = 0..3
    public void Op63(GeneralRegister R, int r, int devid) {
        int status = 0;
        switch (devid) {
            case 0: // Keyboard
            case 1: // Console
            case 2: // Printer
                status = 1; // Always ready in this simple model
                break;
            default:
                status = 0; // Unknown device, not ready
        }
        R.setValue(r, status);
    }
}
