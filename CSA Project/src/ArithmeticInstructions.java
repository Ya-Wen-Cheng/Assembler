
public class ArithmeticInstructions {
	//OpCode 04: Add Memory To Register
	public void Op10(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
		int content = R.getValue(r); //c(r)
		int EA = address + IX.getValue(x); 
		int contentEA = M.getValue(EA);
		R.setValue(r, content+contentEA);
	}
	
	//OpCode 05: Subtract Memory From Register
	public void Op05(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
		int content = R.getValue(r); //c(r)
		int EA = address + IX.getValue(x); 
		int contentEA = M.getValue(EA);
		R.setValue(r, content-contentEA);
	}
	
	//OpCode 06: Add Immediate To Register
	public void Op06(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
		int content = R.getValue(r); //c(r)
		R.setValue(r, content+address);
	}
	
	public void Op07(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
		int content = R.getValue(r); //c(r)
		R.setValue(r, content-address);
	}
}
