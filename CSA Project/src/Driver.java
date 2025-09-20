import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Driver {
    public int location = 0; // location counter

    // Input assembly program
    File file = new File("sourceProgram.asm");

    // Symbol table (label → address)
    Map<String, Integer> symbolTable = new HashMap<>();

    // Holds all cleaned source lines (instructions + directives)
    List<String> inputLines = new ArrayList<>();

    // Output file
    File loadFile = new File("output_file.txt");

    // ============================
    // PASS 1: Build symbol table
    // ============================
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
                    inputLines.add(line);
                    continue;
                }

                // Handle labels (example: End: HLT)
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

                // Save instructions/directives (without comments/labels)
                if (!line.isEmpty()) {
                    inputLines.add(line);
                    location++; // increment location for each instruction or data
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Source file not found: " + e.getMessage());
        }

        // Debug
        System.out.println("Symbol Table: " + symbolTable);
        System.out.println("Input Lines: " + inputLines);
    }
public void secondPass() {
    location = 0; // Reset location counter

    File listingFile = new File("listing_file.txt");
    File loadFile = new File("load_file.txt");

    try (BufferedWriter listWriter = new BufferedWriter(new FileWriter(listingFile));
         BufferedWriter loadWriter = new BufferedWriter(new FileWriter(loadFile))) {

        for (String rawLine : inputLines) {
            if (rawLine == null || rawLine.trim().isEmpty()) continue;

            // --- Separate code and comment ---
            String[] partsComment = rawLine.split(";", 2);
            String codePart = partsComment[0].trim();
            String comment = (partsComment.length > 1) ? partsComment[1].trim() : "";

            if (codePart.isEmpty()) continue;

            // ---- LOC ----
            if (codePart.toUpperCase().startsWith("LOC")) {
                if (!comment.isEmpty()) {
                    listWriter.write(String.format("%-6s\t%-6s\t%-20s\t;%s",
                            "", "", codePart, comment));
                } else {
                    listWriter.write(String.format("%-6s\t%-6s\t%-20s",
                            "", "", codePart));
                }
                listWriter.newLine();

                String[] parts = codePart.split("\\s+");
                if (parts.length > 1) {
                    try {
                        location = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid LOC value: " + parts[1]);
                    }
                }
                continue;
            }

            // ---- DATA ----
            if (codePart.toUpperCase().startsWith("DATA")) {
                String[] parts = codePart.split("\\s+");
                if (parts.length < 2) continue;
                String operand = parts[1].trim();
                int value;

                if (symbolTable.containsKey(operand)) {
                    value = symbolTable.get(operand);
                } else if (operand.equalsIgnoreCase("End")) {
                    value = 1024;
                } else {
                    try {
                        value = Integer.parseInt(operand);
                    } catch (NumberFormatException e) {
                        value = 0;
                    }
                }

                // write to listing
                if (!comment.isEmpty()) {
                    listWriter.write(String.format("%06o\t%06o\t%-20s\t;%s",
                            location, value, codePart, comment));
                } else {
                    listWriter.write(String.format("%06o\t%06o\t%-20s",
                            location, value, codePart));
                }
                listWriter.newLine();

                // write to load (only 2 columns)
                loadWriter.write(String.format("%06o\t%06o", location, value));
                loadWriter.newLine();

                location++;
                continue;
            }

            // ---- Instructions ----
            String[] parts = codePart.split("\\s+", 2);
            String opcode = parts[0].trim().toUpperCase();
            String[] rawOperands = (parts.length > 1) ? parts[1].split(",") : new String[0];

            List<String> operandList = new ArrayList<>();
            for (String op : rawOperands) {
                String o = op.trim();
                if (o.isEmpty()) continue;
                if (symbolTable.containsKey(o)) {
                    operandList.add(String.valueOf(symbolTable.get(o)));
                } else {
                    operandList.add(o);
                }
            }
            while (operandList.size() < 3) operandList.add("0");
            String[] operands = operandList.toArray(new String[0]);

            int machineCode;
            try {
                machineCode = InstructionEncoder.encode(opcode, operands);
            } catch (Exception e) {
                System.out.println("Encoding error at line: " + codePart + " → " + e.getMessage());
                continue;
            }

            // write to listing
            if (!comment.isEmpty()) {
                listWriter.write(String.format("%06o\t%06o\t%-20s\t;%s",
                        location, machineCode, codePart, comment));
            } else {
                listWriter.write(String.format("%06o\t%06o\t%-20s",
                        location, machineCode, codePart));
            }
            listWriter.newLine();

            // write to load (only 2 columns)
            loadWriter.write(String.format("%06o\t%06o", location, machineCode));
            loadWriter.newLine();

            location++;
        }

    } catch (IOException e) {
        System.out.println("Error during second pass: " + e.getMessage());
    }
}

    // ============================
    // MAIN
    // ============================
    public static void main(String[] args) {
        Driver driver = new Driver();
        driver.firstPass(driver.file);   // Run Pass 1
        driver.secondPass();             // Run Pass 2
        //System.out.println("Assembly complete. Output written to output_file.txt");
    }
}
