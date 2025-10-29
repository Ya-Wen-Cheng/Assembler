import java.io.*;
import java.util.*;

class Memory extends Storage {
	private static final int MAX_ADDRESS = 4096;
	
	@Override
	public void setValue(int address, int value) {
        if (address < 0 || address >= MAX_ADDRESS) {
        	throw new IllegalArgumentException("Invalid Memory Address: " + address);
        }
        super.setValue(address, value);
    }
	
	
	@Override
	public Integer getValue(int address) {
		if (address < 0 || address >= MAX_ADDRESS) {
            throw new IllegalArgumentException("Invalid Memory Address: " + address);
        }
        return super.getValue(address);
    }
}
