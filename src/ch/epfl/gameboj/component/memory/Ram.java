/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class Ram implements Component {
    private byte[] data;

    /**
     * Constructor
     * @param size of Ram in byte      
     * @throws IllegalArgumentException
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Size of Ram
     * @return size of Ram
     */
    public int size() {
        return data.length;
    }

    /**
     * Read byte at given index
     * @param index address of required byte
     * @return required byte
     * @throws IndexOutOfBoundsException
     */
    public int read(int index) {
        if (index >= this.size() || index < 0)
            throw new IndexOutOfBoundsException();
        return Byte.toUnsignedInt(data[index]);
    }
    
    /**
     * Writes given value at given index
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException
     */
    public void write(int index, int value) {
        if (index >= this.size() || index < 0)
            throw new IndexOutOfBoundsException();
        Preconditions.checkBits8(value);
        data[index] = (byte) value;
    }
}
