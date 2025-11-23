import java.util.*;

public class InstructionEncoder {

    public static int encode(String opcode, String[] rawOperands) {

        List<String> cleaned = new ArrayList<>();
        for (String op : rawOperands) {
            String o = op.trim();
            if (!o.isEmpty()) cleaned.add(o);
        }
        while (cleaned.size() < 4) cleaned.add("0");
        String[] operands = cleaned.toArray(new String[0]);

        int op;

        switch (opcode.toUpperCase()) {
            case "LDR": op = 01; break;
            case "STR": op = 02; break;
            case "LDA": op = 03; break;
            case "LDX": op = 041; break;
            case "STX": op = 042; break;

            case "AMR": op = 04; break;
            case "SMR": op = 05; break;
            case "AIR": op = 06; break;
            case "SIR": op = 07; break;

            case "JZ":  op = 010; break;
            case "JNE": op = 011; break;
            case "JCC": op = 012; break;
            case "JMA": op = 013; break;
            case "JSR": op = 014; break;
            case "RFS": op = 015; break;
            case "SOB": op = 016; break;
            case "JGE": op = 017; break;

            case "SRC": op = 031; break;
            case "RRC": op = 032; break;

            case "FADD": op = 033; break;
            case "FSUB": op = 034; break;
            case "VADD": op = 035; break;
            case "VSUB": op = 036; break;
            case "CNVRT": op = 037; break;

            case "IN":  op = 061; break;
            case "OUT": op = 062; break;
            case "CHK": op = 063; break;

            case "LDFR": op = 050; break;
            case "STFR": op = 051; break;

            case "MLT": op = 070; break;
            case "DVD": op = 071; break;
            case "TRR": op = 072; break;
            case "AND": op = 073; break;
            case "ORR": op = 074; break;
            case "NOT": op = 075; break;

            case "HLT": op = 00; break;
            case "TRAP": op = 030; break;

            default: throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }

        int code;

        if (opcode.matches("(?i)LDR|STR|LDA|AMR|SMR|JZ|JNE|JCC|JMA|JSR|SOB|JGE|FADD|FSUB|VADD|VSUB|CNVRT|LDFR|STFR")) {
            int r = parse(operands[0]);
            int x = parse(operands[1]);
            int addr = parse(operands[2]) & 0x1F;
            int i = parse(operands[3]) & 0x1;
            code = (op << 10) | (r << 8) | (x << 6) | (i << 5) | addr;
        } 
        else if (opcode.equalsIgnoreCase("LDX") || opcode.equalsIgnoreCase("STX")) {
            int x = parse(operands[0]);
            int addr = parse(operands[1]) & 0x1F;
            int i = parse(operands[2]) & 0x1;
            code = (op << 10) | (0 << 8) | (x << 6) | (i << 5) | addr;
        }
        else if (opcode.matches("(?i)AIR|SIR")) {
            int r = parse(operands[0]);
            int immed = parse(operands[1]) & 0x1F;
            code = (op << 10) | (r << 8) | immed;
        }
        else if (opcode.equalsIgnoreCase("RFS")) {
            int immed = parse(operands[0]) & 0x1F;
            code = (op << 10) | immed;
        }
        else if (opcode.matches("(?i)SRC|RRC")) {
            int r = parse(operands[0]);
            int count = parse(operands[1]) & 0xF;
            int lr = parse(operands[2]) & 0x1;
            int al = parse(operands[3]) & 0x1;
            code = (op << 10) | (r << 8) | (count << 4) | (lr << 3) | (al << 2);
        }
        else if (opcode.matches("(?i)IN|OUT|CHK")) {
            int r = parse(operands[0]);
            int devid = parse(operands[1]) & 0xFF;
            code = (op << 10) | (r << 8) | devid;
        }
        else if (opcode.matches("(?i)MLT|DVD|TRR|AND|ORR")) {
            int rx = parse(operands[0]);
            int ry = parse(operands[1]);
            code = (op << 10) | (rx << 8) | (ry << 6);
        }
        else if (opcode.equalsIgnoreCase("NOT")) {
            int rx = parse(operands[0]);
            code = (op << 10) | (rx << 8);
        }
        else if (opcode.equalsIgnoreCase("HLT")) {
            code = (op << 10);
        }
        else if (opcode.equalsIgnoreCase("TRAP")) {
            int trap = parse(operands[0]) & 0xF;
            code = (op << 10) | (trap << 6);
        }
        else {
            throw new IllegalArgumentException("Opcode not supported yet: " + opcode);
        }

        return code & 0xFFFF;
    }

    private static int parse(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        s = s.trim().toUpperCase();
        if (s.matches("GPR[0-3]")) return s.charAt(3) - '0';
        if (s.matches("R[0-3]")) return s.charAt(1) - '0';
        if (s.matches("IX[1-3]")) return s.charAt(2) - '0';
        if (s.matches("X[1-3]")) return s.charAt(1) - '0';
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
