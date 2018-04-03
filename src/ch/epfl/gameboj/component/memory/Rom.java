/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

public final class Rom {

    private final byte[] data;

    /**
     * Constructor
     * 
     * @param data
     *            rom's data
     */
    public Rom(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Getter for rom's size
     * 
     * @return rom's size in bytes
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

}
