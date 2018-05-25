/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cartridge;

public interface Savable {
    public abstract byte[] save();
    public abstract void load(byte[] data);
}
