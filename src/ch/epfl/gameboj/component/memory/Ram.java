/*
* Author : Paul Juillard (288519)
* Date: 19/02
*/

package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;

public final class Ram {
    private Byte[] data;

    /**
     * Constructor
     * @param size of Ram in byte      
     * @throws IllegalArgumentException
     */
    Ram(int size) throws IllegalArgumentException {
        Preconditions.checkArgument(size >= 0);
        data = new Byte[size];
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
     * @param index adress of required byte
     * @return required byte
     */
    public int read(int index)  throws IndexOutOfBoundsException{
        if (index >= this.size() && index < 0)
            throw new IndexOutOfBoundsException();
        return Byte.toUnsignedInt(data[index]);
    }
    
    public void write(int index, int value) {
        if (index >= this.size() && index < 0)
            throw new IndexOutOfBoundsException();
        Preconditions.checkBits8(value);
//        data[index] = (byte) value;
    }
}
