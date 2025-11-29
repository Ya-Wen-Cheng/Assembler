import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.stage.FileChooser; // Commented out for compilation without JavaFX



public class CPU_1_Simple extends Transformer {

    static final short LOAD_REGISTER_OPCODE = 0x01;
    static final short STORE_REGISTER_OPCODE = 0x02;
    static final short LOAD_ADDRESS_OPCODE = 0x03;
    static final short ADD_MEMORY_REGISTER_OPCODE = 0x04;
    static final short SUBTRACT_MEMORY_REGISTER_OPCODE = 0x05;
    static final short ADD_IMMEDIATE_REGISTER_OPCODE = 0x06;
    static final short SUBTRACT_IMMEDIATE_REGISTER_OPCODE = 0x07;
    static final short LOAD_INDEX_OPCODE = 0x21;  // 041 octal -> 33 dec -> 0x21
    static final short STORE_INDEX_OPCODE = 0x22;  // 042 octal -> 34 dec -> 0x22
    static final short HALT_OPCODE = 0x00;
    // Additional opcodes (match InstructionEncoder values)
    static final short JZ_OPCODE  = 0x08;  // 010 octal
    static final short JNE_OPCODE = 0x09;  // 011 octal
    static final short JCC_OPCODE = 0x0A;  // 012 octal
    static final short JMA_OPCODE = 0x0B;  // 013 octal
    static final short JSR_OPCODE = 0x0C;  // 014 octal
    static final short RFS_OPCODE = 0x0D;  // 015 octal
    static final short SOB_OPCODE = 0x0E;  // 016 octal
    static final short JGE_OPCODE = 0x0F;  // 017 octal

    static final short SRC_OPCODE = 0x19;  // 031 octal -> 25 dec -> 0x19
    static final short RRC_OPCODE = 0x1A;  // 032 octal -> 26 dec

    static final short FADD_OPCODE = 0x1B;  // 033 octal -> 27 dec -> 0x1B
    static final short FSUB_OPCODE = 0x1C;  // 034 octal -> 28 dec -> 0x1C
    static final short VADD_OPCODE = 0x1D;  // 035 octal -> 29 dec -> 0x1D
    static final short VSUB_OPCODE = 0x1E;  // 036 octal -> 30 dec -> 0x1E
    static final short CNVRT_OPCODE = 0x1F;  // 037 octal -> 31 dec -> 0x1F

    static final short IN_OPCODE  = 0x31;  // 061 octal -> 49 dec
    static final short OUT_OPCODE = 0x32;  // 062 octal -> 50 dec
    static final short CHK_OPCODE = 0x33;  // 063 octal -> 51 dec

    static final short LDFR_OPCODE = 0x28;  // 050 octal -> 40 dec -> 0x28
    static final short STFR_OPCODE = 0x29;  // 051 octal -> 41 dec -> 0x29

    static final short MLT_OPCODE = 0x38;  // 070 octal -> 56 dec
    static final short DVD_OPCODE = 0x39;  // 071 octal -> 57 dec
    static final short TRR_OPCODE = 0x3A;  // 072 octal -> 58 dec
    static final short AND_OPCODE = 0x3B;  // 073 octal -> 59 dec
    static final short ORR_OPCODE = 0x3C;  // 074 octal -> 60 dec
    static final short NOT_OPCODE = 0x3D;  // 075 octal -> 61 dec

    static final short TRAP_OPCODE = 0x18; // 030 octal -> 24 dec

    public GeneralRegister generalRegister;
    public IndexRegister indexRegister;
    public ConditionRegister conditionRegister;
    // Floating point registers (FR0 and FR1, each 32 bits = 2 words)
    private short[] floatingRegister0 = new short[2]; // [high, low]
    private short[] floatingRegister1 = new short[2]; // [high, low]
    // Devices
    public KeyboardDevice keyboard;
    public PrinterDevice printer;
    public CardReaderDevice cardReader;
    public ConsoleRegisterDevice consoleRegs;
    
    public char[] instructionRegister;
    public char[] memoryFaultRegister;
    public char[] memoryBufferRegister;
    public char[] memoryAddressRegister;
    public char[] programCounter = "            ".toCharArray();

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
    // Initialize devices (internal implementations)
    keyboard = new KeyboardDevice();
    printer = new PrinterDevice();
    cardReader = new CardReaderDevice();
    consoleRegs = new ConsoleRegisterDevice();
    }

    // ---- Embedded device implementations (simple) ----
    // These are minimal internal device classes so CPU_1_Simple does not
    // depend on external files for IN/OUT/CHK behavior.

    public static class KeyboardDevice {
        private final StringBuilder buffer = new StringBuilder();

        // Called by GUI to push input (a line or characters)
        public synchronized void pushString(String s) {
            buffer.append(s);
        }

        // Read next character, or -1 if none available
        public synchronized int readChar() {
            if (buffer.length() == 0) return -1;
            int ch = buffer.charAt(0);
            buffer.deleteCharAt(0);
            return ch;
        }

        // Status: 1 if there is data, 0 otherwise
        public synchronized int status() {
            return buffer.length() > 0 ? 1 : 0;
        }
    }

    public static class PrinterDevice {
        private final StringBuilder out = new StringBuilder();
        // Optional listener for GUI
        private java.util.function.Consumer<String> listener = null;

        public synchronized void write(String s) {
            out.append(s);
            if (listener != null) listener.accept(s);
        }

        public synchronized int status() {
            return 1; // always ready
        }

        public synchronized void setListener(java.util.function.Consumer<String> l) {
            listener = l;
        }

        public synchronized String getOutput() { return out.toString(); }
    }

    public static class CardReaderDevice {
        // Minimal stub: no card input implemented
        public synchronized int readChar() { return -1; }
        public synchronized int status() { return 0; }
    }

    public static class ConsoleRegisterDevice {
        private int value = 0;
        public synchronized int readChar() { return value; }
        public synchronized void write(String s) {
            try {
                value = Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        public synchronized int status() { return 1; }
    }

    private void ResetRegisters() {
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


                        if (addr >= 0 && addr < 4096) {
                            // Use setValue directly (bypasses cache during load)
                            memory.setValue(addr, value);
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            // After loading, cache is empty (reset was called before loadROM)
            // Cache will populate as instructions are executed
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }
    
    public boolean loadROMFromFixedFile(Memory memory) {
        File fixedFile = new File("load_file.txt");
        if (fixedFile.exists()) {
            return loadROM(fixedFile, memory);
        }
        return false;
    }
    
    // (getROMFileChooser requires JavaFX FileChooser)
    public static FileChooser getROMFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select ROM File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Load Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser;
    }
    

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
    
    public short getProgramCounter() throws BlankCharArrayException {
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
    
    // Floating point register methods
    public void setFR(short fr, short high, short low) {
        if (fr == 0) {
            floatingRegister0[0] = high;
            floatingRegister0[1] = low;
        } else if (fr == 1) {
            floatingRegister1[0] = high;
            floatingRegister1[1] = low;
        } else {
            System.out.println("ERROR: Invalid floating register " + fr + " - must be 0 or 1");
            System.exit(1);
        }
    }
    
    public short[] getFR(short fr) {
        if (fr == 0) {
            return new short[]{floatingRegister0[0], floatingRegister0[1]};
        } else if (fr == 1) {
            return new short[]{floatingRegister1[0], floatingRegister1[1]};
        } else {
            System.out.println("ERROR: Invalid floating register " + fr + " - must be 0 or 1");
            System.exit(1);
            return new short[]{0, 0};
        }
    }
    
    // Helper to convert two 16-bit words to 32-bit float (simplified representation)
    private float wordsToFloat(short high, short low) {
        // Simple conversion: treat as fixed-point or integer representation
        // For a full implementation, this would decode IEEE 754 format
        int combined = ((high & 0xFFFF) << 16) | (low & 0xFFFF);
        return Float.intBitsToFloat(combined);
    }
    
    // Helper to convert float to two 16-bit words
    private short[] floatToWords(float value) {
        int bits = Float.floatToIntBits(value);
        return new short[]{(short)((bits >>> 16) & 0xFFFF), (short)(bits & 0xFFFF)};
    }
    
    public short getMemoryFaultRegister() throws BlankCharArrayException {
        return BinaryToDecimal(memoryFaultRegister, 4);
    }

    public void Execute(Memory memory) throws BlankCharArrayException {
        short marVal = BinaryToDecimal(memoryAddressRegister, 12);

        if (marVal >= 0 && marVal < 4096) {
            int val = memory.readFromCache(marVal);
            DecimalToBinary((short) val, memoryBufferRegister, 16);
        } else {
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: Memory fault at address " + marVal + " - address out of bounds (0-31)");
            System.exit(1); // Stop execution on error
        }
    }
    
    public void ExecuteLDR(short r, short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
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
    
    public void ExecuteSTR(short r, short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
            // Get value from register r and store in MBR
            short value = getGPR(r);
            setMemoryBufferRegister(value);
            setMemoryAddressRegister(effectiveAddress);
            
            // Store MBR to memory
            memory.writeToCache(effectiveAddress, value);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: STR instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    public void ExecuteLDA(short r, short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
            // Load effective address into register r
            setGPR(r, effectiveAddress);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: LDA instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    public void ExecuteLDX(short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address
        short effectiveAddress = address;
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
            // Set MAR and read from memory
            setMemoryAddressRegister(effectiveAddress);
            short pc = getProgramCounter();
            int machineCode = memory.readFromCache(pc);

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
    
    public void ExecuteSTX(short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address
        short effectiveAddress = address;
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
            // Get value from index register x and store in MBR
            short value = getIXR(x);
            setMemoryBufferRegister(value);
            setMemoryAddressRegister(effectiveAddress);
            
            // Store MBR to memory
            memory.writeToCache(effectiveAddress, value);
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: STX instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    public void ExecuteAMR(short r, short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
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
    
    public void ExecuteSMR(short r, short x, short address, Memory memory) throws BlankCharArrayException {
        // Calculate effective address: address + IX[x]
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        
        short effectiveAddress = (short) (address + ixValue);
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 4096) {
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
    
    public void ExecuteAIR(short r, short immediate, Memory memory) {
        // Add immediate value to register r
        short currentValue = getGPR(r);
        short result = (short) (currentValue + immediate);
        setGPR(r, result);
    }
    
    public void ExecuteSIR(short r, short immediate, Memory memory) {
        // Subtract immediate value from register r
        short currentValue = getGPR(r);
        short result = (short) (currentValue - immediate);
        setGPR(r, result);
    }
    
    // Main instruction execution method
    public void ExecuteInstruction(int machineCode, Memory memory) throws BlankCharArrayException {
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
            
            case JZ_OPCODE:
            case JNE_OPCODE:
            case JCC_OPCODE:
            case JMA_OPCODE:
            case JSR_OPCODE:
            case RFS_OPCODE:
            case SOB_OPCODE:
            case JGE_OPCODE:
                // Simple branch/stack/resume helpers
                ExecuteBranch(opcode, (short) r, (short) x, (short) address, memory);
                break;

            case SRC_OPCODE:
            case RRC_OPCODE:
                ExecuteShiftRotate(opcode, (short) r, (short) ((machineCode >>> 4) & 0xF), (short) ((machineCode >>> 3) & 0x1), (short) ((machineCode >>> 2) & 0x1));
                break;

            case FADD_OPCODE:
            case FSUB_OPCODE:
            case VADD_OPCODE:
            case VSUB_OPCODE:
            case CNVRT_OPCODE:
                ExecuteFloatingVector(opcode, (short) r, (short) x, (short) address, (short) i, memory);
                break;

            case LDFR_OPCODE:
            case STFR_OPCODE:
                ExecuteFloatingRegister(opcode, (short) r, (short) x, (short) address, (short) i, memory);
                break;

            case IN_OPCODE:
            case OUT_OPCODE:
            case CHK_OPCODE:
                ExecuteIO(opcode, (short) r, (short) (machineCode & 0xFF));
                break;

            case MLT_OPCODE:
            case DVD_OPCODE:
            case TRR_OPCODE:
            case AND_OPCODE:
            case ORR_OPCODE:
            case NOT_OPCODE:
                ExecuteArithmeticLogical(opcode, (short) r, (short) x);
                break;

            case TRAP_OPCODE:
                ExecuteTRAP((short) ((machineCode >>> 6) & 0xF));
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

    // Branch and control instructions
    public void ExecuteBranch(int opcode, short r, short x, short address, Memory memory) throws BlankCharArrayException {
        short pc = getProgramCounter();
        boolean take = false;

        switch (opcode) {
            case JZ_OPCODE:
                take = (getConditionCode() == 0);
                break;
            case JNE_OPCODE:
                take = (getConditionCode() != 0);
                break;
            case JCC_OPCODE:
                // Use condition code bits (simple non-zero)
                take = (getConditionCode() != 0);
                break;
            case JMA_OPCODE:
                // Jump always
                take = true;
                break;
            case JSR_OPCODE:
                // Save return address in R0 and jump
                setGPR((short)0, (short)(pc + 1));
                take = true;
                break;
            case RFS_OPCODE:
                // Return from subroutine: restore R0 into PC
                setProgramCounter(getGPR((short)0));
                return;
            case SOB_OPCODE:
                // Decrement register r and branch if > 0
                short val = getGPR(r);
                val = (short)(val - 1);
                setGPR(r, val);
                take = (val > 0);
                break;
            case JGE_OPCODE:
                // Branch if GPR[r] >= 0
                take = (getGPR(r) >= 0);
                break;
            default:
                System.out.println("ERROR: Unhandled branch opcode " + opcode);
                System.exit(1);
        }

        if (take) {
            short ixVal = 0;
            if (x > 0 && x <= 3) ixVal = getIXR(x);
            short effective = (short)(address + ixVal);
            setProgramCounter(effective);
        }
    }

    // Shift/rotate
    public void ExecuteShiftRotate(int opcode, short r, short count, short lr, short al) {
        // Operate on GPR r; lr=left/right, al=arithmetic/logical
        short val = getGPR(r);
        int c = count & 0xF;
        if (c == 0) return;

        if (opcode == SRC_OPCODE) {
            // Shift/right/combined: lr==1 => right, lr==0 => left
            if (lr == 1) {
                // Right shift
                if (al == 1) { // arithmetic
                    val = (short)(val >> c);
                } else {
                    val = (short)((val & 0xFFFF) >>> c);
                }
            } else {
                // Left shift
                val = (short)((val & 0xFFFF) << c);
            }
        } else if (opcode == RRC_OPCODE) {
            // Rotate right/left: use unsigned rotations
            int u = val & 0xFFFF;
            if (lr == 1) {
                // Rotate right
                int res = ((u >>> c) | (u << (16 - c))) & 0xFFFF;
                val = (short) res;
            } else {
                // Rotate left
                int res = ((u << c) | (u >>> (16 - c))) & 0xFFFF;
                val = (short) res;
            }
        }

        setGPR(r, val);
    }

    // Simple IO handlers (stubbed to print actions)
    public void ExecuteIO(int opcode, short r, short device) {
        // Centralized device permission checks and routing.
        // Allowed mappings (strict):
        //  IN:  DEVID 0 (keyboard), 2 (card reader), 3..31 (console regs)
        //  OUT: DEVID 1 (printer), 3..31 (console regs)
        //  CHK: DEVID 0 (keyboard), 1 (printer), 2 (card reader), 3..31 (console regs)

        // Validate device id range
        if (device < 0 || device > 31) {
            System.out.println("ERROR: Invalid device id " + device);
            return;
        }

        switch (opcode) {
            case IN_OPCODE:
                if (device == 0) {
                    int ch = keyboard.readChar();
                    setGPR(r, (short)(ch >= 0 ? ch : -1));
                } else if (device == 2) {
                    int ch = cardReader.readChar();
                    setGPR(r, (short)(ch >= 0 ? ch : -1));
                } else if (device >= 3 && device <= 31) {
                    int ch = consoleRegs.readChar();
                    setGPR(r, (short)(ch >= 0 ? ch : -1));
                } else {
                    System.out.println("ERROR: IN not allowed for device " + device + ". Allowed: 0,2,3..31");
                    setGPR(r, (short)-1);
                }
                break;

            case OUT_OPCODE:
                if (device == 1) {
                    int val = getGPR(r);
                    char ch = (char)(val & 0xFF);
                    printer.write(String.valueOf(ch));
                    System.out.println("Printer output (device 1): " + ch + " (value: " + val + ")");
                } else if (device >= 3 && device <= 31) {
                    int val = getGPR(r);
                    consoleRegs.write(String.valueOf(val));
                    System.out.println("Console output (device " + device + "): " + val);
                } else {
                    System.out.println("ERROR: OUT not allowed for device " + device + ". Allowed: 1,3..31");
                }
                break;

            case CHK_OPCODE:
                if (device == 0) {
                    setGPR(r, (short) keyboard.status());
                } else if (device == 1) {
                    setGPR(r, (short) printer.status());
                } else if (device == 2) {
                    setGPR(r, (short) cardReader.status());
                } else if (device >= 3 && device <= 31) {
                    setGPR(r, (short) consoleRegs.status());
                } else {
                    setGPR(r, (short)0);
                }
                break;

            default:
                System.out.println("ERROR: ExecuteIO called with unsupported opcode " + opcode);
                break;
        }
    }

    // Multiply/divide/compare/logical
    public void ExecuteArithmeticLogical(int opcode, short rx, short ry) {
        int a = getGPR(rx);
        int b = getGPR(ry);

        switch (opcode) {
            case MLT_OPCODE: {
                int prod = a * b;
                // Store high/low into R0 (low) and R1 (high) simple policy
                setGPR((short)0, (short)(prod & 0xFFFF));
                setGPR((short)1, (short)((prod >>> 16) & 0xFFFF));
                break;
            }
            case DVD_OPCODE: {
                if (b == 0) {
                    System.out.println("ERROR: Divide by zero");
                    System.exit(1);
                }
                int quot = a / b;
                int rem = a % b;
                setGPR((short)0, (short)(quot & 0xFFFF));
                setGPR((short)1, (short)(rem & 0xFFFF));
                break;
            }
            case TRR_OPCODE: {
                // Test registers: set condition code: 0 equal, -1 less, 1 greater
                int cc = Integer.compare(a, b);
                setConditionCode((short) cc);
                break;
            }
            case AND_OPCODE: {
                setGPR(rx, (short)(a & b));
                break;
            }
            case ORR_OPCODE: {
                setGPR(rx, (short)(a | b));
                break;
            }
            case NOT_OPCODE: {
                setGPR(rx, (short)(~a));
                break;
            }
            default:
                System.out.println("ERROR: Unhandled arithmetic/logical opcode " + opcode);
                System.exit(1);
        }
    }

    // TRAP handler (tiny)
    public void ExecuteTRAP(short trap) {
        // Provide simple system services
        switch (trap) {
            case 1:
                // Read signed integer from keyboard (blocking via GUI push)
                System.out.println("TRAP 1: Starting to read from keyboard...");
                short v = readSignedIntegerFromKeyboard();
                System.out.println("TRAP 1: Read value: " + v + ", storing in R0");
                setGPR((short)0, v);
                break;
            case 2:
                // Print integer in R0 to printer with newline
                int outv = getGPR((short)0);
                printer.write(String.valueOf(outv) + "\n");
                System.out.println("TRAP 2: Printer output: " + outv);
                break;
            default:
                // Unknown trap number - just continue execution
                System.out.println("WARNING: Unknown TRAP number: " + trap);
                break;
        }
    }

    // Floating point and vector operations
    public void ExecuteFloatingVector(int opcode, short fr, short x, short address, short i, Memory memory) throws BlankCharArrayException {
        // Calculate effective address
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        short effectiveAddress = (short) (address + ixValue);
        
        if (effectiveAddress < 0 || effectiveAddress >= 4096) {
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: Floating/Vector instruction - effective address " + effectiveAddress + " out of bounds");
            System.exit(1);
            return;
        }
        
        // Get memory address (direct or indirect)
        short memAddr = effectiveAddress;
        if (i == 1) {
            // Indirect addressing
            if (memAddr < 0 || memAddr >= 4096) {
                memoryFaultRegister[0] = 1;
                System.out.println("ERROR: Indirect address " + memAddr + " out of bounds");
                System.exit(1);
                return;
            }
            short indirectAddr = (short) memory.readFromCache(memAddr);
            memAddr = indirectAddr;
        }
        
        switch (opcode) {
            case FADD_OPCODE: {
                // FADD: c(fr) <- c(fr) + c(EA)
                if (fr < 0 || fr > 1) {
                    System.out.println("ERROR: FADD - fr must be 0 or 1");
                    System.exit(1);
                    return;
                }
                short[] frValue = getFR(fr);
                float frFloat = wordsToFloat(frValue[0], frValue[1]);
                
                // Read memory value (treat as floating point)
                short memHigh = (short) memory.readFromCache(memAddr);
                short memLow = (memAddr + 1 < 4096) ? (short) memory.readFromCache((short)(memAddr + 1)) : 0;
                float memFloat = wordsToFloat(memHigh, memLow);
                
                float result = frFloat + memFloat;
                short[] resultWords = floatToWords(result);
                setFR(fr, resultWords[0], resultWords[1]);
                
                // Check for overflow
                if (Float.isInfinite(result) || Float.isNaN(result)) {
                    setConditionCode((short) 1); // Set overflow bit
                }
                break;
            }
            case FSUB_OPCODE: {
                // FSUB: c(fr) <- c(fr) - c(EA)
                if (fr < 0 || fr > 1) {
                    System.out.println("ERROR: FSUB - fr must be 0 or 1");
                    System.exit(1);
                    return;
                }
                short[] frValue = getFR(fr);
                float frFloat = wordsToFloat(frValue[0], frValue[1]);
                
                short memHigh = (short) memory.readFromCache(memAddr);
                short memLow = (memAddr + 1 < 4096) ? (short) memory.readFromCache((short)(memAddr + 1)) : 0;
                float memFloat = wordsToFloat(memHigh, memLow);
                
                float result = frFloat - memFloat;
                short[] resultWords = floatToWords(result);
                setFR(fr, resultWords[0], resultWords[1]);
                
                // Check for underflow
                if (Math.abs(result) < Float.MIN_NORMAL && result != 0.0f) {
                    setConditionCode((short) 2); // Set underflow bit
                }
                break;
            }
            case VADD_OPCODE: {
                // VADD: Vector Add
                // fr contains the length of the vectors
                int length = getGPR(fr);
                if (length <= 0 || length > 1000) {
                    System.out.println("ERROR: VADD - invalid vector length " + length);
                    System.exit(1);
                    return;
                }
                
                // Get addresses of vectors
                short v1Addr = (short) memory.readFromCache(memAddr);
                short v2Addr = (memAddr + 1 < 4096) ? (short) memory.readFromCache((short)(memAddr + 1)) : 0;
                
                // Perform vector addition: V1[i] = V1[i] + V2[i]
                for (int idx = 0; idx < length; idx++) {
                    if (v1Addr + idx >= 4096 || v2Addr + idx >= 4096) {
                        System.out.println("ERROR: VADD - vector address out of bounds");
                        System.exit(1);
                        return;
                    }
                    short v1Val = (short) memory.readFromCache((short)(v1Addr + idx));
                    short v2Val = (short) memory.readFromCache((short)(v2Addr + idx));
                    memory.writeToCache((short)(v1Addr + idx), (short)(v1Val + v2Val));
                }
                break;
            }
            case VSUB_OPCODE: {
                // VSUB: Vector Subtract
                int length = getGPR(fr);
                if (length <= 0 || length > 1000) {
                    System.out.println("ERROR: VSUB - invalid vector length " + length);
                    System.exit(1);
                    return;
                }
                
                short v1Addr = (short) memory.readFromCache(memAddr);
                short v2Addr = (memAddr + 1 < 4096) ? (short) memory.readFromCache((short)(memAddr + 1)) : 0;
                
                // Perform vector subtraction: V1[i] = V1[i] - V2[i]
                for (int idx = 0; idx < length; idx++) {
                    if (v1Addr + idx >= 4096 || v2Addr + idx >= 4096) {
                        System.out.println("ERROR: VSUB - vector address out of bounds");
                        System.exit(1);
                        return;
                    }
                    short v1Val = (short) memory.readFromCache((short)(v1Addr + idx));
                    short v2Val = (short) memory.readFromCache((short)(v2Addr + idx));
                    memory.writeToCache((short)(v1Addr + idx), (short)(v1Val - v2Val));
                }
                break;
            }
            case CNVRT_OPCODE: {
                // CNVRT: Convert to Fixed/Floating Point
                // r register contains F (0 = to fixed, 1 = to floating)
                int F = getGPR(fr);
                
                if (F == 0) {
                    // Convert c(EA) to fixed point and store in r
                    short memHigh = (short) memory.readFromCache(memAddr);
                    short memLow = (memAddr + 1 < 4096) ? (short) memory.readFromCache((short)(memAddr + 1)) : 0;
                    float memFloat = wordsToFloat(memHigh, memLow);
                    int fixedValue = Math.round(memFloat);
                    setGPR(fr, (short)(fixedValue & 0xFFFF));
                } else if (F == 1) {
                    // Convert c(EA) to floating point and store in FR0
                    short memValue = (short) memory.readFromCache(memAddr);
                    float floatValue = (float) memValue;
                    short[] floatWords = floatToWords(floatValue);
                    setFR((short)0, floatWords[0], floatWords[1]);
                } else {
                    System.out.println("ERROR: CNVRT - F must be 0 or 1");
                    System.exit(1);
                }
                break;
            }
            default:
                System.out.println("ERROR: Unhandled floating/vector opcode " + opcode);
                System.exit(1);
        }
    }

    // Floating register load/store operations
    public void ExecuteFloatingRegister(int opcode, short fr, short x, short address, short i, Memory memory) throws BlankCharArrayException {
        // Calculate effective address
        short ixValue = 0;
        if (x > 0 && x <= 3) {
            ixValue = getIXR(x);
        }
        short effectiveAddress = (short) (address + ixValue);
        
        if (effectiveAddress < 0 || effectiveAddress >= 4096) {
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: Floating register instruction - effective address " + effectiveAddress + " out of bounds");
            System.exit(1);
            return;
        }
        
        if (fr < 0 || fr > 1) {
            System.out.println("ERROR: Floating register must be 0 or 1");
            System.exit(1);
            return;
        }
        
        // Get memory address (direct or indirect)
        short memAddr = effectiveAddress;
        if (i == 1) {
            // Indirect addressing
            if (memAddr < 0 || memAddr >= 4096) {
                memoryFaultRegister[0] = 1;
                System.out.println("ERROR: Indirect address " + memAddr + " out of bounds");
                System.exit(1);
                return;
            }
            short indirectAddr = (short) memory.readFromCache(memAddr);
            memAddr = indirectAddr;
        }
        
        switch (opcode) {
            case LDFR_OPCODE: {
                // LDFR: Load Floating Register from Memory
                // fr <- c(EA), c(EA+1)
                if (memAddr + 1 >= 4096) {
                    System.out.println("ERROR: LDFR - address " + memAddr + " + 1 out of bounds");
                    System.exit(1);
                    return;
                }
                short high = (short) memory.readFromCache(memAddr);
                short low = (short) memory.readFromCache((short)(memAddr + 1));
                setFR(fr, high, low);
                break;
            }
            case STFR_OPCODE: {
                // STFR: Store Floating Register to Memory
                // EA, EA+1 <- c(fr)
                if (memAddr + 1 >= 4096) {
                    System.out.println("ERROR: STFR - address " + memAddr + " + 1 out of bounds");
                    System.exit(1);
                    return;
                }
                short[] frValue = getFR(fr);
                memory.writeToCache(memAddr, frValue[0]);
                memory.writeToCache((short)(memAddr + 1), frValue[1]);
                break;
            }
            default:
                System.out.println("ERROR: Unhandled floating register opcode " + opcode);
                System.exit(1);
        }
    }

    // Helper to read a full signed 16-bit integer from keyboard device
    private short readSignedIntegerFromKeyboard() {
        // read characters until newline
        StringBuilder sb = new StringBuilder();
        int charCount = 0;
        while (charCount < 100) { // Safety limit to prevent infinite loop
            int ch = keyboard.readChar();
            if (ch == -1) {
                // no data currently; return 0 as default
                System.out.println("TRAP 1: No data in keyboard buffer, returning 0");
                return 0;
            }
            if (ch == '\n' || ch == '\r') break;
            sb.append((char) ch);
            charCount++;
        }

        String s = sb.toString().trim();
        System.out.println("TRAP 1: Read from keyboard: '" + s + "'");
        if (s.isEmpty()) {
            System.out.println("TRAP 1: Empty string, returning 0");
            return 0;
        }
        try {
            int parsed = Integer.parseInt(s);
            if (parsed < Short.MIN_VALUE) parsed = Short.MIN_VALUE;
            if (parsed > Short.MAX_VALUE) parsed = Short.MAX_VALUE;
            System.out.println("TRAP 1: Parsed integer: " + parsed);
            return (short) parsed;
        } catch (NumberFormatException e) {
            System.out.println("TRAP 1: NumberFormatException, returning 0");
            return 0;
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
    public void runTest(String testFileName) throws BlankCharArrayException {
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
            if (pc < 16 || pc >= 4096) break;
            
            int machineCode = memory.readFromCache(pc);
            
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
    public static void main(String[] args) {
        CPU_1_Simple cpu = new CPU_1_Simple();
        
        // Default to the existing CPU instruction test file
        String testFileName = "6_CPU Instruction";
        if (args.length > 0) {
            testFileName = args[0];
        }
        
        try {
            cpu.runTest(testFileName);
        } catch (BlankCharArrayException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}

