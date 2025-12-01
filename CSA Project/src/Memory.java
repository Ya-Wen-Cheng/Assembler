import java.io.*;
import java.util.*;

class Memory extends Storage {
    private static final int MAX_ADDRESS = 32;
    private Cache cache;

    public Memory() {
        cache = new Cache(this);  // link cache to this memory
    }

    public Cache getCache() {
        return cache;
    }

    @Override
    public void setValue(int address, int value) {
        if (address < 0 || address >= MAX_ADDRESS)
            throw new IllegalArgumentException("Invalid Memory Address: " + address);
        super.setValue(address, value);
    }

    @Override
    public Integer getValue(int address) {
        if (address < 0 || address >= MAX_ADDRESS)
            throw new IllegalArgumentException("Invalid Memory Address: " + address);
        return super.getValue(address);
    }

    // Cache-aware access methods
    public int readFromCache(int address) {
        return cache.readFromCache(address);
    }

    public void writeToCache(int address, int data) {
        cache.writeToCache(address, data);
    }

    public void flushCache() {
        cache.flushCache();
    }

    public void showCache() {
        cache.showCache();
    }
    
    public void resetCache() {
        cache.resetCache();
    }
}
