

public class IndexRegister extends Storage {
    private static final int MAX_REGISTERS = 3;

    @Override
    public void setValue(int location, int value) {
        if (location < 1 || location > MAX_REGISTERS) {
            throw new IllegalArgumentException("Invalid Index Register: IX" + location);
        }
        super.setValue(location, value);
    }
}
