
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

	// Multiply Register by Register
	public void Op70(GeneralRegister R, ConditionRegister CR, int rx, int ry) {

		if (!((rx == 0 || rx == 2) && (ry == 0 || ry == 2))) {
			throw new IllegalArgumentException("MLT: rx and ry must be 0 or 2");
		}

		int valRx = R.getValue(rx);
		int valRy = R.getValue(ry);
		long result = (long)valRx * (long)valRy;
		int high = (int)(result >>> 16); // high order 16 bits
		int low = (int)(result & 0xFFFF); // low order 16 bits
		R.setValue(rx, high);
		R.setValue(rx + 1, low);
		if (result > 0xFFFFFFFFL || result < 0) {
			CR.setValue(0, 1); 
		} else {
			CR.setValue(0, 0); 
		}
	}

	// Divide Register by Register
	public void Op71(GeneralRegister R, ConditionRegister CR, int rx, int ry) {
		if (!((rx == 0 || rx == 2) && (ry == 0 || ry == 2))) {
			throw new IllegalArgumentException("DVD: rx and ry must be 0 or 2");
		}
		int valRx = R.getValue(rx);
		int valRy = R.getValue(ry);
		if (valRy == 0) {
			CR.setValue(3, 1); // Set DIVZERO flag
			return;
		} else {
			CR.setValue(3, 0); // Clear DIVZERO flag
		}
		int quotient = valRx / valRy;
		int remainder = valRx % valRy;
		R.setValue(rx, quotient);
		R.setValue(rx + 1, remainder);
	}

	// Test the Equality of Register and Register
	public void Op72(GeneralRegister R, ConditionRegister CR, int rx, int ry) {
		int valRx = R.getValue(rx);
		int valRy = R.getValue(ry);
		if (valRx == valRy) {
			CR.setValue(4, 1); // Set EQUAL flag
		} else {
			CR.setValue(4, 0); // Clear EQUAL flag
		}
	}

	// Logical And of Register and Register
	public void Op73(GeneralRegister R, int rx, int ry) {
		int valRx = R.getValue(rx);
		int valRy = R.getValue(ry);
		R.setValue(rx, valRx & valRy);
	}

	// Logical Or of Register and Register
	public void Op74(GeneralRegister R, int rx, int ry) {
		int valRx = R.getValue(rx);
		int valRy = R.getValue(ry);
		R.setValue(rx, valRx | valRy);
	}

	// Logical Not of Register To Register
	public void Op75(GeneralRegister R, int rx) {
		int valRx = R.getValue(rx);
		R.setValue(rx, ~valRx);
	}
}
