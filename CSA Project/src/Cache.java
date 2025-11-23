import java.util.Arrays;

public class Cache {

    private static final int CACHE_SIZE = 8;   // you can change to 16 if needed
    private CacheLine[] lines;
    private Memory memory;

    public Cache(Memory memory) {
        this.memory = memory;
        this.lines = new CacheLine[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            lines[i] = new CacheLine();
        }
    }

    // ---------- CACHE LINE CLASS ----------
    private static class CacheLine {
        int address = -1;
        int data = 0;
        boolean valid = false;
        boolean dirty = false;
        String lastAccess = ""; // "HIT", "MISS", "WRITE", ""

        @Override
        public String toString() {
            return String.format("[Addr=%02d | Data=%d | V=%b | D=%b]", address, data, valid, dirty);
        }
    }

    // ---------- READ FROM CACHE ----------
    public int readFromCache(int address) {
        int index = address % CACHE_SIZE;
        CacheLine line = lines[index];

        // Cache Hit
        if (line.valid && line.address == address) {
            line.lastAccess = "HIT";
            return line.data;
        }

        // Cache Miss → Fetch from main memory
        Integer memoryValue = memory.getValue(address);
        if (memoryValue == null) {
            // Return 0 for uninitialized memory instead of throwing exception
            // This allows execution to continue gracefully
            line.address = address;
            line.data = 0;
            line.valid = true;
            line.dirty = false;
            line.lastAccess = "MISS";
            return 0;
        }

        // Write back if dirty
        if (line.valid && line.dirty) {
            memory.setValue(line.address, line.data);
        }

        // Update cache line
        line.address = address;
        line.data = memoryValue;
        line.valid = true;
        line.dirty = false;
        line.lastAccess = "MISS";

        return memoryValue;
    }

    // ---------- WRITE TO CACHE ----------
    public void writeToCache(int address, int data) {
        int index = address % CACHE_SIZE;
        CacheLine line = lines[index];

        // Cache Hit
        if (line.valid && line.address == address) {
            line.data = data;
            line.dirty = true;
            line.lastAccess = "WRITE-HIT";
        }
        // Cache Miss
        else {
            if (line.valid && line.dirty) {
                memory.setValue(line.address, line.data);
            }
            line.address = address;
            line.data = data;
            line.valid = true;
            line.dirty = true;
            line.lastAccess = "WRITE-MISS";
        }
    }

    // ---------- FLUSH CACHE ----------
    public void flushCache() {
        for (CacheLine line : lines) {
            if (line.valid && line.dirty) {
                memory.setValue(line.address, line.data);
                line.dirty = false;
            }
        }
    }
    
    // ---------- RESET CACHE ----------
    public void resetCache() {
        for (CacheLine line : lines) {
            line.address = -1;
            line.data = 0;
            line.valid = false;
            line.dirty = false;
            line.lastAccess = "";
        }
    }

    // ---------- SHOW CACHE CONTENTS ----------
    public void showCache() {
        System.out.println("=== CACHE CONTENTS ===");
        for (int i = 0; i < CACHE_SIZE; i++) {
            System.out.println("Line " + i + ": " + lines[i]);
        }
        System.out.println("======================");
    }
    
    // ---------- GET CACHE LINES FOR GUI DISPLAY ----------
    public static class CacheLineInfo {
        public int lineIndex;
        public int address;
        public int data;
        public boolean valid;
        public boolean dirty;
        public String lastAccess;
        
        public CacheLineInfo(int lineIndex, int address, int data, boolean valid, boolean dirty, String lastAccess) {
            this.lineIndex = lineIndex;
            this.address = address;
            this.data = data;
            this.valid = valid;
            this.dirty = dirty;
            this.lastAccess = lastAccess;
        }
        
        @Override
        public String toString() {
            return String.format("Line %d: Addr=%02d | Data=%d | V=%b | D=%b | %s", 
                lineIndex, address, data, valid, dirty, lastAccess);
        }
    }
    
    public CacheLineInfo[] getCacheLines() {
        CacheLineInfo[] info = new CacheLineInfo[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            info[i] = new CacheLineInfo(i, lines[i].address, lines[i].data, 
                lines[i].valid, lines[i].dirty, lines[i].lastAccess);
        }
        return info;
    }
    
    public int getCacheSize() {
        return CACHE_SIZE;
    }
}
