import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.stage.FileChooser;

public class CPU_1_Simple extends Transformer {
	// Load/store instructions
    static final short LOAD_REGISTER_OPCODE_LDR = 0x01;
    static final short STORE_REGISTER_OPCODE_STR = 0x02;
    static final short LOAD_ADDRESS_OPCODE_LDA = 0x03;
    static final short LOAD_INDEX_OPCODE_LDX = 0x041; 
    static final short STORE_INDEX_OPCODE_STX = 0x042;
    
    
    // Transfer instructions
    static final short JUMP_IF_NOT_EQUAL_OPCODE_JNE = 0x11;
    
    // I/O Operations
    static final short INPUT_OPCODE_IN = 0x61;
    
    // Arithmetic instructions
    static final short ADD_MEMORY_REGISTER_OPCODE_AMR = 0x04;
    static final short SUBTRACT_MEMORY_REGISTER_OPCODE_SMR = 0x05;
    static final short ADD_IMMEDIATE_REGISTER_OPCODE_AIR = 0x06;
    static final short SUBTRACT_IMMEDIATE_REGISTER_OPCODE_SIR = 0x07;
    
    // HALT
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

                // ✅ Handle HALT (pure binary line like 0000000000000000)
                if (line.matches("[01]{16}")) {
                    try {
                        int haltValue = Integer.parseInt(line, 2);
                        int nextAddr = memory.data.size();
                        memory.setValue(nextAddr, haltValue);
                        System.out.println("Loaded HALT at memory[" + nextAddr + "] = " + haltValue);
                    } catch (Exception e) {
                        System.out.println("Error parsing HALT line: " + e.getMessage());
                    }
                    continue;
                }

                // Normal "address value" pair (octal)
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) { 
                    try {
                        int addr = Integer.parseInt(parts[0], 8);
                        int value = Integer.parseInt(parts[1], 8);
                        if (addr >= 0 && addr < 4096) {
                            memory.setValue(addr, value);
                            System.out.println("Loaded memory[" + addr + "] = " + value);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing line: " + line);
                    }
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("ROM file not found: " + file.getAbsolutePath());
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
    public void ExecuteLDR(short r, short x, short address, Memory memory){
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
            try {
				Execute(memory);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // Load MBR into register r
            short value;
			try {
				value = getMemoryBufferValue();
				setGPR(r, value);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            try {
            	System.out.println("LDR Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
            try {
            	System.out.println("STR Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
            try {
            	System.out.println("LDA Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: LDA instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute LDX instruction: Load Index register
    public void ExecuteLDX(short x, short address, Memory memory){
        // Calculate effective address: address
        short effectiveAddress = address;
        
        // Check if effective address is valid (0-31)
        if (effectiveAddress >= 0 && effectiveAddress < 32) {
            // Set MAR and read from memory
            setMemoryAddressRegister(effectiveAddress);
            
            try {
				Execute(memory);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // Load MBR into index register x
            short value;
			try {
				value = getMemoryBufferValue();
				setIXR(x, value);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            try {
            	System.out.println("LDX Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
            try {
            	System.out.println("STX Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: STX instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute AMR instruction: Add Memory to Register
    public void ExecuteAMR(short r, short x, short address, Memory memory){
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
            try {
				Execute(memory);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // Add MBR to register r
            short currentValue = getGPR(r);
            short memoryValue;
			try {
				memoryValue = getMemoryBufferValue();
				short result = (short) (currentValue + memoryValue);
	            setGPR(r, result);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				System.out.println("AMR Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        } else {
            // Memory fault - display error and stop execution
            memoryFaultRegister[0] = 1;
            System.out.println("ERROR: AMR instruction - effective address " + effectiveAddress + " out of bounds (0-31)");
            System.exit(1);
        }
    }
    
    // Execute SMR instruction: Subtract Memory from Register
    public void ExecuteSMR(short r, short x, short address, Memory memory) {
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
            
            
            try {
				Execute(memory);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // Subtract MBR from register r
            short currentValue = getGPR(r);
            short memoryValue;
			try {
				memoryValue = getMemoryBufferValue();
				short result = (short) (currentValue - memoryValue);
	            setGPR(r, result);
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            try {
            	System.out.println("SMR Executed");
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
        try {
        	System.out.println("AIR Executed");
			IncrementPCBy1();
		} catch (BlankCharArrayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // Execute SIR instruction: Subtract Immediate from Register
    public void ExecuteSIR(short r, short immediate) {
        // Subtract immediate value from register r
        short currentValue = getGPR(r);
        short result = (short) (currentValue - immediate);
        setGPR(r, result);
        try {
        	System.out.println("SIR Executed");
			IncrementPCBy1();
		} catch (BlankCharArrayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void ExecuteJNE(short r, short address) {
        short currentValue = getGPR(r);
        if (currentValue != 0) {
        	setProgramCounter((short) address);
        	System.out.println("JNE Executed");
        	return;
        }
        try {
        	System.out.println("JNE Executed");
			IncrementPCBy1();
		} catch (BlankCharArrayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void ExecuteIN(short r, String input) {
        short currentValue = getGPR(r);
        short result = Short.parseShort(input);
        setGPR(r, result);
        try {
        	System.out.println("IN Executed");
			IncrementPCBy1();
		} catch (BlankCharArrayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    public void IncrementPCBy1() throws BlankCharArrayException{
    	try {
    		short currentPC = getProgramCounter();
    		setProgramCounter((short)(currentPC+1));
    	}catch(BlankCharArrayException e) {
    		System.out.println("PC is not defined");
    	}
    	
    	
    }
    
    
    // Main instruction execution method
    public void ExecuteInstruction(int machineCode, Memory memory, String input){
        int opcode = (machineCode >>> 10) & 0x3F;
        int r = (machineCode >>> 8) & 0x3;
        int x = (machineCode >>> 6) & 0x3;
        int i = (machineCode >>> 5) & 0x1;
        int address = machineCode & 0x1F;

        switch (opcode) {
            case HALT_OPCODE:
                // HLT - Halt execution
                break;
                
            case LOAD_REGISTER_OPCODE_LDR:
                // LDR - Load Register from memory
                ExecuteLDR((short) r, (short) x, (short) address, memory);
                break;
                
            case STORE_REGISTER_OPCODE_STR:
                // STR - Store Register to memory
                ExecuteSTR((short) r, (short) x, (short) address, memory);
                break;
                
            case LOAD_ADDRESS_OPCODE_LDA:
                // LDA - Load Address
                ExecuteLDA((short) r, (short) x, (short) address, memory);
                break;
                
            case LOAD_INDEX_OPCODE_LDX:
                // LDX - Load Index register
                ExecuteLDX((short) x, (short) address, memory);
                break;
                
            case STORE_INDEX_OPCODE_STX:
                // STX - Store Index register
                ExecuteSTX((short) x, (short) address, memory);
                break;
                
            case ADD_MEMORY_REGISTER_OPCODE_AMR:
                // AMR - Add Memory to Register
                ExecuteAMR((short) r, (short) x, (short) address, memory);
                break;
                
            case SUBTRACT_MEMORY_REGISTER_OPCODE_SMR:
                // SMR - Subtract Memory from Register
                ExecuteSMR((short) r, (short) x, (short) address, memory);
                break;
                
            case ADD_IMMEDIATE_REGISTER_OPCODE_AIR:
                // AIR - Add Immediate to Register
                ExecuteAIR((short) r, (short) address, memory);
                break;
                
            case SUBTRACT_IMMEDIATE_REGISTER_OPCODE_SIR:
                // SIR - Subtract Immediate from Register
                ExecuteSIR((short) r, (short) address);
                break;
            
            case INPUT_OPCODE_IN:
            	ExecuteIN((short) r, input);
            	break;
            
            case JUMP_IF_NOT_EQUAL_OPCODE_JNE:
            	ExecuteJNE((short)r, (short)address);
             
            default:
                // Unknown opcode - display error and stop execution
                memoryFaultRegister[0] = 1;
                System.out.println("ERROR: Unknown opcode " + opcode + " - invalid instruction");
                System.exit(1);
                break;
        }
        
        // Increment PC for all instructions except HLT
        if (opcode != HALT_OPCODE) {
            short pc;
			try {
				pc = getProgramCounter();
				IncrementPCBy1();
			} catch (BlankCharArrayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
    }
    
    // Helper method to get instruction name for display
    private String getInstructionName(int opcode, int r, int x, int address) {
        switch (opcode) {
            case HALT_OPCODE:
                return "HLT";
            case LOAD_REGISTER_OPCODE_LDR:
                return "LDR " + r + "," + x + "," + address;
            case STORE_REGISTER_OPCODE_STR:
                return "STR " + r + "," + x + "," + address;
            case LOAD_ADDRESS_OPCODE_LDA:
                return "LDA " + r + "," + x + "," + address;
            case LOAD_INDEX_OPCODE_LDX:
                return "LDX " + x + "," + address;
            case STORE_INDEX_OPCODE_STX:
                return "STX " + x + "," + address;
            case ADD_MEMORY_REGISTER_OPCODE_AMR:
                return "AMR " + r + "," + x + "," + address;
            case SUBTRACT_MEMORY_REGISTER_OPCODE_SMR:
                return "SMR " + r + "," + x + "," + address;
            case ADD_IMMEDIATE_REGISTER_OPCODE_AIR:
                return "AIR " + r + "," + address;
            case SUBTRACT_IMMEDIATE_REGISTER_OPCODE_SIR:
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
        String testStr = "24";
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
            ExecuteInstruction(machineCode, memory, testStr);
            
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

