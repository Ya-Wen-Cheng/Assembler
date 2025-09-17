
public class TransferInstructions {
		//OpCode 10: Jump If Zero
		public Integer Op10(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
			int content = R.getValue(r); //c(r)
			int EA = address + IX.getValue(x); 
			if (content == 0) {
				location = EA;
			}else {
				location += 1;
			}
			return location;
		}
		
		//OpCode 11: Jump If Not Equal
		public Integer Op11(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
			int content = R.getValue(r); //c(r)
			int EA = address + IX.getValue(x);
			if (content != 0) {
				location = EA;
			}else {
				location += 1;
			}
			return location;
		}
		
		//OpCode 12: Jump If Condition Code
		public Integer Op12(ConditionRegister C, IndexRegister IX, Memory M, int c, int x, int address, int location) {	
			int EA = address + IX.getValue(x);
			if (C.getValue(c) == 1) {
				location = EA;
			}else {
				location += 1;
			}
			return location;
		}
		
		//OpCode 13: Unconditional Jump To Address
		public Integer Op13(IndexRegister IX, Memory M, int r, int x, int address) {
			int EA = address + IX.getValue(x);
			return EA;
		}
		
		//OpCode 14: Jump and Save Return Address
		public Integer Op14(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
			R.setValue(3, location+1); //R3 stores return address
			R.setValue(0, location+1); //R0 stores the first argument
			int EA = address + IX.getValue(x); 
			location = EA; //jump to subroutine address
			return location;
		}
		
		//OpCode 15: Return From Subroutine
		public Integer Op15(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
			R.setValue(r, address);
			location = R.getValue(3);
			return location;
		}
		
		//OpCode 16: Subtract One and Branch
		public Integer Op16(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
			int content = R.getValue(r);
			R.setValue(r, content-1);
			int EA = address + IX.getValue(x);
			if (content > 0) {
				location = EA;
			}else {
				location += 1;
			}
			return location;
		}
		
		//OpCode 17: Jump Greater Than or Equal To
		public Integer Op17(GeneralRegister R, IndexRegister IX, Memory M, int r, int x, int address, int location) {
			int content = R.getValue(r);
			int EA = address + IX.getValue(x);
			if(content == 0) {
				location = EA;
			}else {
				location += 1;
			}
			return location;
		}
		
		
}
