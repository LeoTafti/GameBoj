/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus.save;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import ch.epfl.gameboj.Preconditions;

public final class Ram {

    private byte[] data;

    /**
     * Constructor
     * 
     * @param size
     *            size of Ram in bytes
     * @throws IllegalArgumentException
     *             if given size value is negative
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Getter for ram's size
     * 
     * @return size of Ram in bytes
     */
    public int size() {
        return data.length;
    }

    /**
     * Reads byte at given index
     * 
     * @param index
     *            address of required byte
     * @return byte read
     * @throws IndexOutOfBoundsException
     *             if given index is not in range from 0 (inclusive) to size
     *             (exclusive)
     */
    public int read(int index) {
        Objects.checkIndex(index, size());
        
        return Byte.toUnsignedInt(data[index]);
    }

    /**
     * Writes given value at given index
     * 
     * @param index
     *            index at which to write in ram
     * @param value
     *            value to store
     * @throws IndexOutOfBoundsException
     *             if given index is not in range from 0 (inclusive) to size
     *             (exclusive)      
     * @throws IllegalArgumentException
     *             if given value isn't an 8-bit value
     */
    public void write(int index, int value) {
        Objects.checkIndex(index, size());
        Preconditions.checkBits8(value);
        
        data[index] = (byte) value;
    }
    
    public void toFile(OutputStream s) throws IOException {
        s.write(data);
    }
    
    public void fromFile(byte[] data) {
        this.data = data;
        //TODO : simply removed final modifier to do that, but may be ugly
    }
}
