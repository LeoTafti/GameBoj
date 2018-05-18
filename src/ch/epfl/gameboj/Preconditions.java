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
    public static void checkArgument(boolean b) {
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
    public static int checkBits8(int v) {
        checkArgument(v >= 0x00 && v <= 0xFF);
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
    public static int checkBits16(int v) {
        checkArgument(v >= 0x0000 && v <= 0xFFFF);
        return v;
    }

}
