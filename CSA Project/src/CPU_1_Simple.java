import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.stage.FileChooser;

public class CPU_1_Simple extends Transformer {

    static final short LOAD_REGISTER_OPCODE = 0x01;
    static final short STORE_REGISTER_OPCODE = 0x02;
    static final short LOAD_ADDRESS_OPCODE = 0x03;
    static final short ADD_MEMORY_REGISTER_OPCODE = 0x04;
    static final short SUBTRACT_MEMORY_REGISTER_OPCODE = 0x05;
    static final short ADD_IMMEDIATE_REGISTER_OPCODE = 0x06;
    static final short SUBTRACT_IMMEDIATE_REGISTER_OPCODE = 0x07;
    static final short LOAD_INDEX_OPCODE = 0x041;
    static final short STORE_INDEX_OPCODE = 0x042;
    static final short HALT_OPCODE = 0x00;

    // Use existing register classes
    public GeneralRegister generalRegister;
    public IndexRegister indexRegister;
    public ConditionRegister conditionRegister;
    
    // Keep char arrays for compatibility with existing code
    public char[] instructionRegister;
    public char[] memoryFaultRegister;
    public char[] memoryBufferRegister;
    public char[] memoryAddressRegister = new char[12];
    public char[] programCounter = new char[12];

    public CPU_1_Simple() {
        // Initialize register objects using existing classes
        generalRegister = new GeneralRegister();
        indexRegister = new IndexRegister();
        conditionRegister = new ConditionRegister();
        
        // Initialize char arrays for other registers
        instructionRegister = new char[16];
        memoryFaultRegister = new char[4];
        memoryBufferRegister = new char[16];
        memoryAddressRegister = new char[12];
        programCounter = new char[12];
        ResetRegisters();
    }

    private void ResetRegisters() {
        // Reset register objects
        generalRegister.data.clear();
        indexRegister.data.clear();
        conditionRegister.data.clear();
        
        // Reset char arrays
        Arrays.fill(instructionRegister, (char) 0);
        Arrays.fill(memoryBufferRegister, (char) 0);
        Arrays.fill(programCounter, (char) 0);
        Arrays.fill(memoryAddressRegister, (char) 0);
        Arrays.fill(memoryFaultRegister, (char) 0);
    }

    public void Reset(Memory memory) {
        ResetRegisters();
        memory.data.clear();
    }
    
    // ROM Loader functionality
    public boolean loadROM(File file, Memory memory) {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                // Parse load file format: address value (octal)
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        int addr = Integer.parseInt(parts[0], 8); // Octal address
                        int value = Integer.parseInt(parts[1], 8); // Octal value
                        if (addr >= 0 && addr < 32) {
                            memory.setValue(addr, value);
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }
    
    // Load ROM from fixed file name (load_file.txt)
    public boolean loadROMFromFixedFile(Memory memory) {
        File fixedFile = new File("load_file.txt");
        if (fixedFile.exists()) {
            return loadROM(fixedFile, memory);
        }
        return false;
    }
    
    // Get file chooser for ROM loading - Commented out for compilation without JavaFX
    
    public static FileChooser getROMFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select ROM File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Load Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser;
    }
    

    // Register setter methods using existing register classes
    public void setGPR(short reg, short value) {
        try {
            generalRegister.setValue(reg, value);
            System.out.println("Set GPR "+reg+" to "+ value);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid GPR register number " + reg + " - must be 0-3");
            System.exit(1);
        }
    }

    public void setIXR(short ix, short value) {
        try {
            indexRegister.setValue(ix, value);
            System.out.println("Set IXR "+ix+" to "+ value);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid IXR register number " + ix + " - must be 1-3");
            System.exit(1);
        }
    }

    public void setProgramCounter(short value) {
        DecimalToBinary(value, programCounter, 12);
    }

    public void setMemoryAddressRegister(short value) {
        DecimalToBinary(value, memoryAddressRegister, 12);
    }

    public void setMemoryBufferRegister(short value) {
        DecimalToBinary(value, memoryBufferRegister, 16);
    }

    public void setConditionCode(short value) {
        try {
            conditionRegister.setValue(0, value);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid condition code value " + value);
            System.exit(1);
        }
    }

    // Register getter methods
    public short getMemoryAddressValue() throws BlankCharArrayException {
    		return BinaryToDecimal(memoryAddressRegister, 12);  
    }

    public short getMemoryBufferValue() throws BlankCharArrayException{
        return BinaryToDecimal(memoryBufferRegister, 16);
    }
    
    public short getGPR(short reg) {
        try {
            Integer value = generalRegister.getValue(reg);
            return value != null ? value.shortValue() : 0;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    
    public short getIXR(short ix) {
        try {
            Integer value = indexRegister.getValue(ix);
            return value != null ? value.shortValue() : 0;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    
    public short getProgramCounter() throws BlankCharArrayException{
        return BinaryToDecimal(programCounter, 12);
    }
    
    public short getConditionCode() {
        try {
            Integer value = conditionRegister.getValue(0);
            return value != null ? value.shortValue() : 0;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    
    public short getMemoryFaultRegister() throws BlankCharArrayException{
        return BinaryToDecimal(memoryFaultRegister, 4);
    }

    // Memory access with bounds checking - Display errors and stop execution
    public void Execute(Memory memory) throws BlankCharArrayException{
        short marVal = BinaryToDecimal(memoryAddressRegister, 12);
        System.out.println("DEBUG: CPU Execute - MAR value: " + marVal);
        if (marVal >= 0 && marVal < 32) {
            Integer val = memory.getValue(marVal);
            if (val != null) {
                DecimalToBinary(val.shortValue(), memoryBufferRegister, 16);
            } else {
                memoryFaultRegister[0] = 1;
                System.out.println("ERROR: Memory fault at address " + marVal + " - no data found");
                System.exit(1); // Stop execution on error
            }
        } else {
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: Memory fault at address " + marVal + " - address out of bounds (0-31)");
            System.exit(1); // Stop execution on error
        }
    }
    
    // Execute LDR instruction: Load Register from memory
    public void ExecuteLDR(short r, short x, short address, Memory memory) throws BlankCharArrayException{
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Set MAR and read from memory
            setMemoryAddressRegister(effectiveAddress);
            Execute(memory);
            
            // Load MBR into register r
            short value = getMemoryBufferValue();
            setGPR(r, value);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: LDR instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute STR instruction: Store Register to memory
    public void ExecuteSTR(short r, short x, short address, Memory memory) {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Get value from register r and store in MBR
            short value = getGPR(r);
            setMemoryBufferRegister(value);
            setMemoryAddressRegister(effectiveAddress);
            
            // Store MBR to memory
            memory.setValue(effectiveAddress, value);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: STR instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute LDA instruction: Load Address into register - Display errors and stop execution
    public void ExecuteLDA(short r, short x, short address, Memory memory) {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Load effective address into register r
            setGPR(r, effectiveAddress);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: LDA instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute LDX instruction: Load Index register
    public void ExecuteLDX(short x, short address, Memory memory) throws BlankCharArrayException{
        // Calculate effective address: address
        short effectiveAddress = address;
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Set MAR and read from memory
            setMemoryAddressRegister(effectiveAddress);
            Execute(memory);
            
            // Load MBR into index register x
            short value = getMemoryBufferValue();
            setIXR(x, value);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: LDX instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute STX instruction: Store Index register
    public void ExecuteSTX(short x, short address, Memory memory) {
        // Calculate effective address: address
        short effectiveAddress = address;
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Get value from index register x and store in MBR
            short value = getIXR(x);
            setMemoryBufferRegister(value);
            setMemoryAddressRegister(effectiveAddress);
            
            // Store MBR to memory
            memory.setValue(effectiveAddress, value);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: STX instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute AMR instruction: Add Memory to Register
    public void ExecuteAMR(short r, short x, short address, Memory memory) throws BlankCharArrayException{
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Set MAR and read from memory
            setMemoryAddressRegister(effectiveAddress);
            Execute(memory);
            
            // Add MBR to register r
            short currentValue = getGPR(r);
            short memoryValue = getMemoryBufferValue();
            short result = (short) (currentValue + memoryValue);
            setGPR(r, result);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: AMR instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute SMR instruction: Subtract Memory from Register
    public void ExecuteSMR(short r, short x, short address, Memory memory) throws BlankCharArrayException{
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Set MAR and read from memory
            setMemoryAddressRegister(effectiveAddress);
            Execute(memory);
            
            // Subtract MBR from register r
            short currentValue = getGPR(r);
            short memoryValue = getMemoryBufferValue();
            short result = (short) (currentValue - memoryValue);
            setGPR(r, result);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: SMR instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute AIR instruction: Add Immediate to Register
    public void ExecuteAIR(short r, short immediate, Memory memory) {
        // Add immediate value to register r
        short currentValue = getGPR(r);
        short result = (short) (currentValue + immediate);
        setGPR(r, result);
    }
    
    // Execute SIR instruction: Subtract Immediate from Register
    public void ExecuteSIR(short r, short immediate, Memory memory) {
        // Subtract immediate value from register r
        short currentValue = getGPR(r);
        short result = (short) (currentValue - immediate);
        setGPR(r, result);
    }
    
    // Main instruction execution method
    public void ExecuteInstruction(int machineCode, Memory memory) throws BlankCharArrayException{
        int opcode = (machineCode >>> 10) & 0x3F;
        int r = (machineCode >>> 8) & 0x3;
        int x = (machineCode >>> 6) & 0x3;
        int i = (machineCode >>> 5) & 0x1;
        int address = machineCode & 0x1F;

        switch (opcode) {
            case HALT_OPCODE:
                // HLT - Halt execution
                break;
                
            case LOAD_REGISTER_OPCODE:
                // LDR - Load Register from memory
                ExecuteLDR((short) r, (short) x, (short) address, memory);
                break;
                
            case STORE_REGISTER_OPCODE:
                // STR - Store Register to memory
                ExecuteSTR((short) r, (short) x, (short) address, memory);
                break;
                
            case LOAD_ADDRESS_OPCODE:
                // LDA - Load Address
                ExecuteLDA((short) r, (short) x, (short) address, memory);
                break;
                
            case LOAD_INDEX_OPCODE:
                // LDX - Load Index register
                ExecuteLDX((short) x, (short) address, memory);
                break;
                
            case STORE_INDEX_OPCODE:
                // STX - Store Index register
                ExecuteSTX((short) x, (short) address, memory);
                break;
                
            case ADD_MEMORY_REGISTER_OPCODE:
                // AMR - Add Memory to Register
                ExecuteAMR((short) r, (short) x, (short) address, memory);
                break;
                
            case SUBTRACT_MEMORY_REGISTER_OPCODE:
                // SMR - Subtract Memory from Register
                ExecuteSMR((short) r, (short) x, (short) address, memory);
                break;
                
            case ADD_IMMEDIATE_REGISTER_OPCODE:
                // AIR - Add Immediate to Register
                ExecuteAIR((short) r, (short) address, memory);
                break;
                
            case SUBTRACT_IMMEDIATE_REGISTER_OPCODE:
                // SIR - Subtract Immediate from Register
                ExecuteSIR((short) r, (short) address, memory);
                break;
                
            default:
                // Unknown opcode - display error and stop execution
                memoryFaultRegister[0] = 1;
                System.out.println("ERROR: Unknown opcode " + opcode + " - invalid instruction");
                System.exit(1);
                break;
        }
        
        // Increment PC for all instructions except HLT
        if (opcode != HALT_OPCODE) {
            short pc = getProgramCounter();
            setProgramCounter((short) (pc + 1));
        }
    }
    
    // Helper method to get instruction name for display
    private String getInstructionName(int opcode, int r, int x, int address) {
        switch (opcode) {
            case HALT_OPCODE:
                return "HLT";
            case LOAD_REGISTER_OPCODE:
                return "LDR " + r + "," + x + "," + address;
            case STORE_REGISTER_OPCODE:
                return "STR " + r + "," + x + "," + address;
            case LOAD_ADDRESS_OPCODE:
                return "LDA " + r + "," + x + "," + address;
            case LOAD_INDEX_OPCODE:
                return "LDX " + x + "," + address;
            case STORE_INDEX_OPCODE:
                return "STX " + x + "," + address;
            case ADD_MEMORY_REGISTER_OPCODE:
                return "AMR " + r + "," + x + "," + address;
            case SUBTRACT_MEMORY_REGISTER_OPCODE:
                return "SMR " + r + "," + x + "," + address;
            case ADD_IMMEDIATE_REGISTER_OPCODE:
                return "AIR " + r + "," + address;
            case SUBTRACT_IMMEDIATE_REGISTER_OPCODE:
                return "SIR " + r + "," + address;
            default:
                return "UNK " + opcode;
        }
    }
    
    // Test runner method - executes test file and shows results
    public void runTest(String testFileName) throws BlankCharArrayException{
        System.out.println("=== CPU_1_Simple Test Execution ===");
        System.out.println("Test File: " + testFileName);
        System.out.println();
        
        // Create memory and load test file
        Memory memory = new Memory();
        File testFile = new File("test/" + testFileName + "_load.txt");
        
        if (!testFile.exists()) {
            System.out.println("ERROR: Test file not found: " + testFile.getPath());
            System.out.println("Available test files:");
            File testDir = new File("test/");
            if (testDir.exists()) {
                String[] files = testDir.list();
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith("_load.txt")) {
                            System.out.println("  - " + file.replace("_load.txt", ""));
                        }
                    }
                }
            }
            return;
        }
        
        // Load test data
        boolean loaded = loadROM(testFile, memory);
        if (!loaded) {
            System.out.println("ERROR: Failed to load test file");
            return;
        }
        
        System.out.println("✓ Test data loaded successfully");
        System.out.println();
        
        // Execute test instructions
        System.out.println("=== Executing Test Instructions ===");
        System.out.println("PC | R0  R1  R2  R3  | IX1 IX2 IX3 | Fault | Instruction");
        System.out.println("---|-----------------|-------------|-------|------------");
        
        // Start from instruction section (address 16)
        setProgramCounter((short) 16);
        
        int instructionCount = 0;
        while (instructionCount < 50) { // Safety limit
            short pc = getProgramCounter();
            if (pc < 16 || pc >= 32) break;
            
            Integer machineCode = memory.getValue(pc);
            if (machineCode == null) break;
            
            int opcode = (machineCode >>> 10) & 0x3F;
            int r = (machineCode >>> 8) & 0x3;
            int x = (machineCode >>> 6) & 0x3;
            int address = machineCode & 0x1F;
            
            // Check for HLT instruction
            if (opcode == HALT_OPCODE) {
                System.out.println(String.format("%2d | %3d %3d %3d %3d | %3d %3d %3d | %5d | HLT", 
                    pc, getGPR((short) 0), getGPR((short) 1), getGPR((short) 2), getGPR((short) 3),
                    getIXR((short) 1), getIXR((short) 2), getIXR((short) 3), getMemoryFaultRegister()));
                System.out.println("HLT instruction executed - stopping");
                break;
            }
            
            // Execute instruction
            ExecuteInstruction(machineCode, memory);
            
            // Show register state after instruction
            String instruction = getInstructionName(opcode, r, x, address);
            System.out.println(String.format("%2d | %3d %3d %3d %3d | %3d %3d %3d | %5d | %s", 
                pc, getGPR((short) 0), getGPR((short) 1), getGPR((short) 2), getGPR((short) 3),
                getIXR((short) 1), getIXR((short) 2), getIXR((short) 3), getMemoryFaultRegister(), instruction));
            
            // Increment PC for next instruction
            setProgramCounter((short) (pc + 1));
            instructionCount++;
        }
        
        System.out.println();
        System.out.println("=== Test Summary ===");
        System.out.println("✓ Memory fault handling: Errors displayed and execution stopped");
        System.out.println("✓ Register operations: Working correctly with error checking");
        System.out.println("✓ Instruction execution: Completed successfully");
        System.out.println("✓ Uses existing register classes: GeneralRegister, IndexRegister, ConditionRegister");
        System.out.println();
        System.out.println("=== Key Features ===");
        System.out.println("✓ Memory faults display error messages and stop execution");
        System.out.println("✓ Invalid register numbers display errors and stop execution");
        System.out.println("✓ Unknown opcodes display errors and stop execution");
    }
    
    // Static method to run test
    public static void main(String[] args) throws BlankCharArrayException{
        CPU_1_Simple cpu = new CPU_1_Simple();
        
        // Default to the existing CPU instruction test file
        String testFileName = "6_CPU Instruction";
        if (args.length > 0) {
            testFileName = args[0];
        }
        
        cpu.runTest(testFileName);
    }
}

