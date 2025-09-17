import java.io.*;
import java.util.*;

/*
 * 1: GPRs 0-3 R0-R3
 * 2: IXRs 1-3 X1-X3
 * 3: Address: 32 locations M1-M32
 */

public class LoadInstructions {

	//OpCode 01: r <- c(EA)
	public void Op01(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address) {
		int content = M.getValue(address) + IX.getValue(x);
		R.setValue(r, content);
	}
	
	//OpCode 02: Memory(EA) <- c(r)
	public void Op02(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address) {
		int content = R.getValue(r);
		int EA = address + IX.getValue(x);
		M.setValue(EA, content);
	}
	
	//Opcode 03: r <- EA
	public void Op03(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address) {
		int value = address + IX.getValue(x);
		R.setValue(r, value);
	}
	
	//Opcode 41: Xx <- c(EA)
	public void Op41(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address) {
		int content = M.getValue(address) + IX.getValue(x);
		IX.setValue(x, content);
	}
	
	//Opcode 42: Memory(EA) <- c(Xx)
	public void Op42(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address) {
		int content = IX.getValue(x);
		int EA = address + IX.getValue(x);
		M.setValue(EA, content);
	}

}
