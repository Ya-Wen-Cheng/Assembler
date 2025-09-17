

public class GeneralRegister extends Storage {
    private static final int MAX_REGISTERS = 4;

    @Override
    public void setValue(int location, int value) {
        if (location < 0 || location >= MAX_REGISTERS) {
            throw new IllegalArgumentException("Invalid General Register: R" + location);
        }
        super.setValue(location, value);
    }
    
    @Override
    public Integer getValue(int location) {
        if (location < 0 || location >= MAX_REGISTERS) {
            throw new IllegalArgumentException("Invalid General Register: R" + location);
        }
        return super.getValue(location);
    }
    
}



