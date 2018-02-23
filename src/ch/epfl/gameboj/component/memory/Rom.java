/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.memory;

import java.util.Arrays;


public final class Rom {
    
    private final byte[] data;
    
    /**
     * Constructor
     * @param data final data
     */
    public Rom( byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }
    
    /**
     * Rom's size
     * @return rom's size in byte 
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
    public int read(int index){
        if (index >= this.size() || index < 0)
            throw new IndexOutOfBoundsException();
        return Byte.toUnsignedInt(data[index]);
    }
}
