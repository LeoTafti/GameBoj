/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cartridge;

public interface Savable {
    /**
     * Returns data to save, as array of bytes or null if no data to save
     * @return data to save
     */
    public abstract byte[] save();
    
    /**
     * Loads given data bytes
     * @param data data to load
     */
    public abstract void load(byte[] data);
}
