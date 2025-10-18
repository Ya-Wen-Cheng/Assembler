import java.io.*;
import java.util.*;

class Memory extends Storage {
	private static final int MAX_ADDRESS = 32;
	
    // Set value at a specific memory address, with bounds checking
    @Override
    public void setValue(int address, int value) {
        if (address < 0 || address >= MAX_ADDRESS) {
        	throw new IllegalArgumentException("Invalid Memory Address: " + address);
        }
        super.setValue(address, value);
    }
	
	
    // Get value from a specific memory address, with bounds checking
    @Override
    public Integer getValue(int address) {
        if (address < 0 || address >= MAX_ADDRESS) {
            throw new IllegalArgumentException("Invalid Memory Address: " + address);
        }
        return super.getValue(address);
    }
}
