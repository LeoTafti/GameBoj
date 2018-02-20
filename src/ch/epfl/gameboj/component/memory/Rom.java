/*
 *  Author : Paul Juillard (288519)
 *  Date: 19/02
*/
package ch.epfl.gameboj.component.memory;

import java.util.Arrays;


public final class Rom {
    
    private final byte[] data;
    
    /**
     * Constructor
     * @param data final data of cartridge
     */
    public Rom( byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }
    
    /**
     * Rom's size
     * @return cartridge's size in byte 
     */
    public int size() {
        return data.length;
    }
    
    /**
     * Read byte at given index
     * @param index adress of required byte
     * @return required byte
     * @throws IndexOutOfBoundsException
     */
    public int read(int index) throws IndexOutOfBoundsException{
        if (index >= this.size() && index < 0)
            throw new IndexOutOfBoundsException();
        return Byte.toUnsignedInt(data[index]);
    }
}
