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
     * @param valueFlags int in which value is packed
     * @return unpacked value
     */
    public static int unpackValue(int valueFlags) {
        return Bits.extract(valueFlags, 8, 16);
    }
    
    /**
     * unpacks flags from given int
     * @param valueFlags int in which flags are packed
     * @return unpacked flags
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.extract(valueFlags, 0, 8);
    }
    
    /**
     * Adds to 8 bits value, and an eventual carry
     * @param l the first 8 bits value
     * @param r the second 8 bits value
     * @param c0 the carry
     * @return packed int of sum and flags (sum may have been cropped)
     * @throws IllegalArgumentException if l or r isn't an 8 bits value
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int sum = l + r;
        if(c0) {
            sum += 1;
        }
        
        boolean h = (Bits.test(sum, 4) != Bits.test(l, 4) & Bits.test(r, 4)); //avoids clipping and adding 4bits value "again"
        boolean c = l + r > 0xFF;
        
        if(c) {
            Bits.set(sum, 8, false); //crops the result (to simulate overflow)
        }
        
        boolean z = (sum == 0);
        
        return packValueZNHC(sum, z, false, h, c);
    }
    
    /**
     * Adds to 8 bits value, no carry
     * @param l the first 8 bits value
     * @param r the second 8 bits value
     * @return packed int of sum and flags (sum may have been cropped)
     * @throws IllegalArgumentException if l or r isn't an 8 bits value
     * @see Alu#add(int l, int r, boolean c0)
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }
    
    
    
    
    
    
    /**
     * Packs value and flags in a single int
     * @param v value, 8 or 16 bits
     * @param z Z flag
     * @param n N flag
     * @param h H flag
     * @param c C flag
     * @return int of packed values
     * @throws IllegalArgumentException if v isn't a 16 bits value
     */
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
