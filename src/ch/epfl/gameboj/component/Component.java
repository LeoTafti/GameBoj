/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

public interface Component {
    public static final int NO_DATA = 0x100;

    /**
     * Returns byte stored at given address or NO_DATA if the component has no
     * value at given address
     * 
     * @param address
     *            address to read from
     * @return stored byte or NO_DATA
     * @throws IllegalArgumentException
     *             if address isn't a 16-bit value
     */
    abstract int read(int address);

    /**
     * Writes given value (data) at given address or does nothing if component
     * doesn't allow to write at this address
     * 
     * @param address
     *            address to write at
     * @param data
     *            value to store
     * @throws IllegalArgumentException
     *             if address isn't a 16-bit value or data isn't an 8-bit value
     */
    abstract void write(int address, int data);

    /**
     * Attaches component to given bus
     * 
     * @param bus
     *            bus to connect component to
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }

}
