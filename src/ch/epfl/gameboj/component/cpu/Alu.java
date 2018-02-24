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
     * @return packed int of sum and flags Z0HC
     *      (sum may have been cropped)
     * @throws IllegalArgumentException if l or r isn't an 8 bits value
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int carry = c0 ? 0 : 1;
        int sum = l + r + carry;
        
        boolean h = getHFlag(l, r, c0);
        boolean c = getCFlag(l, r, c0);
        
        //TODO : ask if needed (but probably yes);
        if(c) {
            Bits.set(sum, 8, false); //crops the result (to simulate overflow)
        }
        
        boolean z = getZFlag(sum);
        
        return packValueZNHC(sum, z, false, h, c);
    }
    
    /**
     * Adds two 8 bits value, no carry
     * @param l the first 8 bits value
     * @param r the second 8 bits value
     * @return packed int of sum and flags Z0HC
     *      (sum may have been cropped)
     * @throws IllegalArgumentException if l or r isn't an 8 bits value
     * @see Alu#add(int l, int r, boolean c0)
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }
    
    /**
     * Adds two 16 bits value
     * @param l the first 16 bits value
     * @param r the second 16 bits value
     * @return packed int of sum and flags 00HC
     *      H, C determined by the addition of the 8 LSB of l and r
     *      (sum may have been cropped)
     * @throws IllegalArgumentException if l or r aren't 16 bits values
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int sum = l+r;
        
        int l8L = Bits.clip(8, l);
        int r8L = Bits.clip(8, r);
        
        boolean h = getHFlag(l8L, r8L, false);
        boolean c = getCFlag(l8L, r8L, false);
        
        if(sum > 0xFFFF) {
            Bits.set(sum, 16, false); //crops the result (to simulate overflow)
        }
        
        //TODO : Ask – project guidelines says to return flags 00HC
        //Why can't Z be true ? Why don't we compute Z ?
        return packValueZNHC(sum, false, false, h, c);
    }
    
    /**
     * Adds two 16 bits value
     * @param l the first 16 bits value
     * @param r the second 16 bits value
     * @return packed int of sum and flags 00HC
     *      H, C determined by the addition of the 8 MSB of l and r
     *      (sum may have been cropped)
     * @throws IllegalArgumentException if l or r aren't 16 bits values
     * @see Alu#add16L(int l, int r)
     */
    public static int add16H(int l, int r) {
        //TODO : better to reuse add16L() or copy add16L() code and change flags-related portion ?
        int valueFlags = add16L(l, r);
        
        int l8H = Bits.extract(l, 8, 8);
        int r8H = Bits.extract(r, 8, 8);
        
        boolean h = getHFlag(l8H, r8H, false);
        boolean c = getCFlag(l8H, r8H, false);
        
        Bits.set(valueFlags, Flag.H.index(), h);
        Bits.set(valueFlags, Flag.C.index(), c);
        
        return valueFlags;
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
    
    private static boolean getZFlag(int n) {
        return (n == 0);
    }
    private static boolean getHFlag(int l, int r, boolean c0) {
        //TODO : ask TA's if better/more efficient method than clipping and adding "again"
        //to get H-Flag
        
        int carry = c0 ? 0 : 1;
        int l4 = Bits.clip(4, l);
        int r4 = Bits.clip(4, r);
        
        return(l4 + r4 + carry > 0xF);
    }
    private static boolean getCFlag(int l, int r, boolean c0) {
        int carry = c0 ? 0 : 1;
        return (l + r + carry > 0xFF);
    }
    
}
