/*
 *  Author : Paul Juillard (288519)
 *  Date: 19/02
*/

package ch.epfl.gameboj;

public interface Preconditions {

    /**
     * Verifies argument based on given boolean
     * @param b boolean representing condition
     * @throws IllegalArgumentException
     */
    static void checkArgument(boolean b) throws IllegalArgumentException {
        if (!b)
            throw new IllegalArgumentException();
    }

    /**
     * Verifies given int is 8bit
     * @param v subject bit to be tested 
     * @return argument if a correct 8 bit
     * @throws IllegalArgumentException
     */
    static int checkBits8(int v) throws IllegalArgumentException {
        if (v >= 256 || v < 0)
            throw new IllegalArgumentException();
        else
            return v;
    }

    /**
     * Verifies given int is 16bit
     * @param v subject bit to be tested
     * @return argument if a correct 16 bit
     * @throws IllegalArgumentException
     */
    static int checkBits16(int v) throws IllegalArgumentException {
        if (v >= 65536 || v < 0)
            throw new IllegalArgumentException();
        else
            return v;
    }

}
