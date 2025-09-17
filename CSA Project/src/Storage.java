import java.util.HashMap;

public class Storage {
    protected HashMap<Integer, Integer> data = new HashMap<>();

    // Set a value at a location
    public void setValue(int location, int value) {
        data.put(location, value);
    }

    // Get a value at a location
    public Integer getValue(int location) {
        return data.get(location);
    }

    // Check if location exists
    public boolean contains(int location) {
        return data.containsKey(location);
    }
}


