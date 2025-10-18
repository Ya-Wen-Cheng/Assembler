import java.util.Arrays;

public class CPU extends Transformer {

    static final short LOAD_REGISTER_OPCODE = 0x01;
    static final short STORE_REGISTER_OPCODE = 0x02;
    static final short HALT_OPCODE = 0x00;

    public char[] gpr0Register, gpr1Register, gpr2Register, gpr3Register;
    public char[] instructionRegister;
    public char[] memoryFaultRegister;
    public char[] memoryBufferRegister;
    public char[] memoryAddressRegister;
    public char[] conditionCodeRegister;
    public char[] indexRegister1, indexRegister2, indexRegister3;
    public char[] programCounter;

    public CPU() {
        gpr0Register = new char[16];
        gpr1Register = new char[16];
        gpr2Register = new char[16];
        gpr3Register = new char[16];
        indexRegister1 = new char[16];
        indexRegister2 = new char[16];
        indexRegister3 = new char[16];
        instructionRegister = new char[16];
        memoryFaultRegister = new char[4];
        memoryBufferRegister = new char[16];
        memoryAddressRegister = new char[12];
        conditionCodeRegister = new char[4];
        programCounter = new char[12];
        ResetRegisters();
    }

    private void ResetRegisters() {
        for (char[] arr : new char[][]{
                gpr0Register, gpr1Register, gpr2Register, gpr3Register,
                indexRegister1, indexRegister2, indexRegister3,
                instructionRegister, memoryBufferRegister})
            Arrays.fill(arr, (char) 0);

        Arrays.fill(programCounter, (char) 0);
        Arrays.fill(memoryAddressRegister, (char) 0);
        Arrays.fill(memoryFaultRegister, (char) 0);
        Arrays.fill(conditionCodeRegister, (char) 0);
    }

    public void Reset(Memory memory) {
        ResetRegisters();
        memory.data.clear();
    }

    public void setGPR(short reg, short value) {
        char[] target = switch (reg) {
            case 1 -> gpr1Register;
            case 2 -> gpr2Register;
            case 3 -> gpr3Register;
            default -> gpr0Register;
        };
        DecimalToBinary(value, target, 16);
    }

    public void setIXR(short ix, short value) {
        char[] target = switch (ix) {
            case 1 -> indexRegister1;
            case 2 -> indexRegister2;
            case 3 -> indexRegister3;
            default -> indexRegister1;
        };
        DecimalToBinary(value, target, 16);
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

    public short getMemoryAddressValue() {
        return BinaryToDecimal(memoryAddressRegister, 12);
    }

    public short getMemoryBufferValue() {
        return BinaryToDecimal(memoryBufferRegister, 16);
    }

    public void Execute(Memory memory) {
        short marVal = BinaryToDecimal(memoryAddressRegister, 12);
        if (marVal >= 0 && marVal < 32) {
            int val = memory.getValue(marVal);
            DecimalToBinary((short) val, memoryBufferRegister, 16);
        } else {
            memoryFaultRegister[0] = 1;
            System.out.println("Memory fault at address: " + marVal);
        }
    }
}
