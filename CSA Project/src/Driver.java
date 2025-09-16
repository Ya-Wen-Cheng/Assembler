import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Driver {
    public int location = 0; // location counter
    // 1. File input (update with real filename later)
    File file = new File("sourceProgram.txt");

    // 2. First pass: build symbol table and collect input lines
    Map<String, Integer> symbolTable = new HashMap<>();
    List<String> inputLines = new ArrayList<>();

    public void firstPass(File file) {
        location = 0; // start from address 0
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                // Skip blank lines and comments (comments start with ;)
                if (line.isEmpty() || line.startsWith(";")) {
                    continue;
                }

                // Handle LOC directive (set location counter)
                if (line.startsWith("LOC")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 1) {
                        location = Integer.parseInt(parts[1]);
                    }
                    continue;
                }

                // Handle labels (example: LOOP: ADD R1, R2)
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    String label = parts[0].trim();
                    symbolTable.put(label, location); // store label with current address
                    if (parts.length > 1) {
                        line = parts[1].trim(); // keep the instruction part
                    } else {
                        line = ""; // only label, no instruction
                    }
                }

                // Save instruction (without comments/labels)
                if (!line.isEmpty()) {
                    inputLines.add(line);
                    location++; // increment location for each instruction
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Source file not found: " + e.getMessage());
        }

        // Print symbol table for debugging
        System.out.println("Symbol Table: " + symbolTable);
        // Print cleaned input lines for debugging
        System.out.println("Input Lines: " + inputLines);
    }

    // 3. Second pass (to be done by teammate)
    File loadFile = new File("output_file");
    public void secondPass(File file) {
        /**
         * (1) Reset location counter to 0
         * (2) Read inputLines again
         * (3) Replace labels using symbolTable
         * (4) Call instruction methods (LoadInstructions.OpXX)
         * (5) Write machine code to loadFile
         */
    }

    // 4. Main
    public static void main(String[] args) {
        Driver driver = new Driver();
        driver.firstPass(driver.file);  // run your First Pass
        driver.secondPass(driver.file); // teammate will implement this
    }
}
