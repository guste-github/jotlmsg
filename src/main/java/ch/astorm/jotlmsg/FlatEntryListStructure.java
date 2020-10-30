package ch.astorm.jotlmsg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents an [MS-OXCDATA] 2.3.3 FlatEntryList Structure in Java.
 * 
 * @author Guido Stein
 *
 */
public class FlatEntryListStructure<T extends FlatEntryStructure> implements Iterable<T> {
    private long count; // Number of FlatEntryStructures.
    private long size; // Number of bytes of all FlatEntryStructures.
    private List<T> flatEntryStructures = new ArrayList<T>();
    private FlatEntryStructureFactory<T> fesf = new FlatEntryStructureFactory<T>();

    /**
     * Returns the number of FlatEntryStructures.
     * 
     * @return
     */
    public long getCount() {
        return count;
    }

    /**
     * Returns the number of total bytes of all FlatEntryStructures.
     * 
     * @return
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns a list of FlatEntryStructures.
     * 
     * @return
     */
    public List<T> getFlatEntryStructures() {
        return flatEntryStructures;
    }

    /**
     * Sets a list of FlatEntryStructures.
     * 
     * @param flatEntries
     */
    public void setFlatEntryStructures(List<T> flatEntries) {
        this.flatEntryStructures = flatEntries;
    }

    /**
     * Adds a FlatEntryStructure.
     * 
     * @param flatEntry
     */
    public void addFlatEntryStructure(T flatEntry) {
        flatEntryStructures.add(flatEntry);

        long totalSize = 0;

        for (FlatEntryStructure fes : flatEntryStructures) {
            totalSize += fes.getSize();
            totalSize += 4; // 4 bytes variable size

            // Consider 4 byte alignment
            totalSize = (totalSize + 4) & ~3;
        }

        size = totalSize;
        count = (long) flatEntryStructures.size();
    }

    /**
     * Constructor, creates the java representation.
     * 
     */
    public FlatEntryListStructure() {
    };

    /**
     * Constructor, creates the java representation.
     * 
     * @param clazz
     * @param bytes
     */
    public FlatEntryListStructure(Class<T> clazz, byte[] bytes) {
        ByteBuffer bf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        // Count and size are stored in 4 bytes.
        count = bf.getInt();
        size = bf.getInt();

        for (int i = 0; i < count; i++) {
            flatEntryStructures.add(fesf.createFlatEntryStructure(clazz, bf));
            // Consider 4 byte alignment
            bf.position((bf.position() + 4) & ~3);
        }
    }

    /**
     * Creates a byte array.
     * 
     * @return
     */
    public byte[] toBytes() {
        // Calculate total size of ByteBuffer
        long totalSize = 8; // 4 bytes variable count + 4 bytes variable size

        for (FlatEntryStructure fes : flatEntryStructures) {
            totalSize += fes.getSize();
            totalSize += 4; // 4 bytes variable size

            // Consider 4 byte alignment
            totalSize = (totalSize + 4) & ~3;
        }

        // Allocate the complete ByteBuffer.
        ByteBuffer bf = ByteBuffer.allocate((int) totalSize).order(ByteOrder.LITTLE_ENDIAN);

        // Store count and size in 4 bytes.
        bf.putInt((int) flatEntryStructures.size()); // Number of FlatEntryStructures.
        bf.putInt((int) totalSize - 8); // Number of bytes of all FlatEntryStructures.

        for (FlatEntryStructure fes : flatEntryStructures) {
            bf.putInt((int) fes.getSize());
            bf.put(fes.getEntryID());

            // Consider 4 byte alignment
            bf.position((bf.position() + 4) & ~3);
        }
        size = totalSize - 8;
        return bf.array();
    }

    /**
     * Iterator.
     */
    public Iterator<T> iterator() {
        return flatEntryStructures.iterator();
    }
}
