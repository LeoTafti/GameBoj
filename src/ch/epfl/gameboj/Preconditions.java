/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

public interface Preconditions {

    /**
     * Verifies argument based on given boolean
     * 
     * @param b
     *            boolean representing condition
     * @throws IllegalArgumentException
     *             if given logic expression is false
     */
    static void checkArgument(boolean b) {
        if (!b)
            throw new IllegalArgumentException();
    }

    /**
     * Verifies given value is no longer than 8 bits
     * 
     * @param v
     *            value to check
     * @return given value if check passed
     * @throws IllegalArgumentException
     *             if given value is bigger than 8 bits
     */
    static int checkBits8(int v) {
        if (v > 0xFF || v < 0x00)
            throw new IllegalArgumentException();
        else
            return v;
    }

    /**
     * Verifies given value is no longer than 16 bits
     * 
     * @param v
     *            value to check
     * @return given value if check passed
     * @throws IllegalArgumentException
     *             if given value is bigger than 16 bits
     */
    static int checkBits16(int v) {
        if (v > 0xFFFF || v < 0x0000)
            throw new IllegalArgumentException();
        else
            return v;
    }

}
