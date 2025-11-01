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
            System.out.println("✅ Cache HIT at address: " + address);
            return line.data;
        }

        // Cache Miss → Fetch from main memory
        System.out.println("❌ Cache MISS at address: " + address);
        Integer memoryValue = memory.getValue(address);
        if (memoryValue == null)
            throw new IllegalArgumentException("Memory read failed at address: " + address);

        // Write back if dirty
        if (line.valid && line.dirty) {
            memory.setValue(line.address, line.data);
            System.out.println("↩️  Wrote back dirty line for address: " + line.address);
        }

        // Update cache line
        line.address = address;
        line.data = memoryValue;
        line.valid = true;
        line.dirty = false;

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
            System.out.println("✏️ Cache WRITE HIT at address: " + address + ", new data = " + data);
        }
        // Cache Miss
        else {
            System.out.println("✏️ Cache WRITE MISS at address: " + address);
            if (line.valid && line.dirty) {
                memory.setValue(line.address, line.data);
                System.out.println("↩️  Wrote back dirty line for address: " + line.address);
            }
            line.address = address;
            line.data = data;
            line.valid = true;
            line.dirty = true;
        }
    }

    // ---------- FLUSH CACHE ----------
    public void flushCache() {
        System.out.println("🧹 Flushing cache to main memory...");
        for (CacheLine line : lines) {
            if (line.valid && line.dirty) {
                memory.setValue(line.address, line.data);
                line.dirty = false;
            }
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
}
