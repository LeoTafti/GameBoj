/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bits;

public interface Bit {

    /**
     * Will automatically be given a definition when implemented by an enum
     * 
     * @returns the ordinal of this enumeration constant
     * @see java.lang.Enum#ordinal()
     */
    public abstract int ordinal();

    /**
     * @return index as defined by ordinal()
     */
    public default int index() {
        return ordinal();
    }

    /**
     * @return bit-string with single 1 in position of index of object as
     *         defined in enumerated type
     */
    public default int mask() {
        return Bits.mask(ordinal());
    }
}
