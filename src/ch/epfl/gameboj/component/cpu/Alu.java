/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {
    
    /**
     * non-instantiable class
     */
    private Alu() {
        //nothing here
    }
    
    /**
     * Flags gives additional informations on results
     * Z : result is 0
     * N : operation was a substraction
     * H : half-carry
     * C : carry
     */
    public enum Flag implements Bit{
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, Z, N, H, C;
    }
    
    /**
     * Defines RotDir
     * Used to specify rotate methods the direction in which to rotate
     */
    public enum RotDir{
        LEFT, RIGHT;
    }
    
    /**
     * returns an int in which flags are packed, with 1 if param is true, 0 otherwise
     * @param z true if operation result is 0
     * @param n true if operation was a substraction
     * @param h true if addition/substraction of 4 LSB produced a carry/borrow
     * @param c true if addition/substraction of all 8 bits produced a carry/borrow
     * @return mask of flags, packed in a int
     */
    public static int maskZNHC (boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(0, z, n, h, c);
    }
    
    /**
     * unpacks value form given int
     * @param valueFlags int in which flags are packed
     * @return unpacked value
     */
    public static int unpackValue(int valueFlags) {
        return Bits.extract(valueFlags, 8, 16);
    }
    
    
    
    private static int packValueZNHC(int v,boolean z, boolean n, boolean h, boolean c) {
        Preconditions.checkBits16(v);
        int packed = v << 8;
        packed = Bits.set(packed, Flag.Z.index(), z);
        packed = Bits.set(packed, Flag.N.index(), n);
        packed = Bits.set(packed, Flag.H.index(), h);
        packed = Bits.set(packed, Flag.C.index(), c);
        
        return packed;
    }
    
    
}
