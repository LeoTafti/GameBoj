/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {

    /**
     * Non-instantiable class
     */
    private Alu() {}

    /**
     * Flags gives additional informations on results
     * Z : result is 0
     * N : operation was a subtraction
     * H : operation produced a half-carry / half-borrow
     * C : operation produced a carry / borrow
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z;
    }

    /**
     * Used to specify rotate methods the direction in which to rotate
     */
    public enum RotDir {
        LEFT, RIGHT;
    }

    /**
     * Returns an int in which flags are packed at indices 4 to 7, with 1 if
     * flag is true, 0 otherwise.
     * 
     * @param z
     *            true if operation result is 0
     * @param n
     *            true if operation was a substraction
     * @param h
     *            true if operation on 4 LSB produced a carry/borrow
     * @param c
     *            true if operation on all 8 bits produced a carry/borrow
     * @return mask of flags, packed in a int
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int packed = 0;
        
        packed = Bits.set(packed, Flag.Z.index(), z);
        packed = Bits.set(packed, Flag.N.index(), n);
        packed = Bits.set(packed, Flag.H.index(), h);
        packed = Bits.set(packed, Flag.C.index(), c);

        return packed;
    }

    /**
     * Unpacks value form given int
     * 
     * @param valueFlags
     *            int in which value is packed (ie. with flags)
     * @return unpacked value (without flags)
     * @throws IllegalArgumentException
     *             if given value has any active bits at indices [0, 3] or
     *             [24, 31] (all boundaries inclusive)
     */
    public static int unpackValue(int valueFlags) {
        checkIntValue(valueFlags);
        return Bits.extract(valueFlags, 8, 16);
    }

    /**
     * Unpacks flags from given int
     * 
     * @param valueFlags
     *            int in which flags are packed (ie. with value)
     * @return unpacked flags
     * @throws IllegalArgumentException
     *             if given value has any active bits at indices [0, 3] or
     *             [24, 31] (all boundaries inclusive)
     */
    public static int unpackFlags(int valueFlags) {
        checkIntValue(valueFlags);
        return Bits.clip(8, valueFlags);
    }

    /**
     * Adds two 8-bit values, and an possible initial carry
     * 
     * @param l
     *            the first 8-bit value
     * @param r
     *            the second 8-bit value
     * @param c0
     *            the carry
     * @return packed int of sum and flags Z0HC (sum may have been truncated)
     * @throws IllegalArgumentException
     *             if l or r isn't an 8-bit value
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int carry = c0 ? 1 : 0;
        int sum = Bits.clip(8, l + r + carry);

        boolean h = Bits.clip(4, l) + Bits.clip(4, r) + carry > 0xF;
        boolean c = l + r + carry > 0xFF;

        boolean z = getZFlag(sum);

        return packValueZNHC(sum, z, false, h, c);
    }

    /**
     * Adds two 8-bit values, no initial carry
     * 
     * @param l
     *            the first 8-bit value
     * @param r
     *            the second 8-bit value
     * @return packed int of sum and flags Z0HC (sum may have been truncated)
     * @throws IllegalArgumentException
     *             if l or r isn't an 8-bit value
     * @see Alu#add(int l, int r, boolean c0)
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }


    /**
     * Adds two 16-bit values
     * 
     * @param l
     *            the first 16-bit value
     * @param r
     *            the second 16-bit value
     * @return packed int of sum and flags 00HC – H, C determined by the
     *         addition of the 8 LSB of l and r (sum may have been truncated)
     * @throws IllegalArgumentException
     *             if l or r aren't 16-bit values
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);

        int sum = Bits.clip(16, l + r);

        int l8L = Bits.clip(8, l);
        int r8L = Bits.clip(8, r);

        boolean h = Bits.clip(4, l8L) + Bits.clip(4, r8L) > 0xF;
        boolean c = l8L + r8L > 0xFF;

        return packValueZNHC(sum, false, false, h, c);
    }

    /**
     * Adds two 16-bit values
     * 
     * @param l
     *            the first 16-bit value
     * @param r
     *            the second 16-bit value
     * @return packed int of sum and flags 00HC – H, C determined by the
     *         addition of the 8 MSB of l and r (sum may have been truncated)
     * @throws IllegalArgumentException
     *             if l or r aren't 16-bit values
     * @see Alu#add16L(int l, int r)
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int sum = Bits.clip(16, l + r);
        
        int l8H = Bits.extract(l, 8, 8);
        int r8H = Bits.extract(r, 8, 8);

        int lsbCout = Bits.clip(8, l) + Bits.clip(8, r) > 0xFF ? 1 : 0;
        boolean h = Bits.clip(4, l8H) + Bits.clip(4, r8H) + lsbCout > 0xF;
        boolean c = l8H + r8H + lsbCout > 0xFF;

        return packValueZNHC(sum, false, false, h, c);
    }

    /**
     * Subtracts two 8-bit values, with possible initial borrow
     * 
     * @param l
     *            first 8-bit value
     * @param r
     *            subtracted 8-bit value
     * @param b0
     *            true if initial borrow
     * @return result of subtraction and flags Z1HC, packed in an int
     * @throws IllegalArgumentException
     *             if l or r aren't 8-bit values
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int borrow = b0 ? 1 : 0;
        int sub = l - r - borrow;
        sub = Bits.clip(8, sub);

        boolean H = (Bits.clip(4, l) < Bits.clip(4, r) + borrow);
        boolean C = (l < r + borrow);

        return packValueZNHC(sub, getZFlag(sub), true, H, C);

    }

    /**
     * Subtracts two 8-bit values
     * 
     * @param l
     *            first 8-bit value
     * @param r
     *            subtracted 8-bit value
     * @return result of subtraction and flags Z1HC, packed in an int
     * @throws IllegalArgumentException
     *             if l or r aren't 8-bit values
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Converts given 8-bit value to binary coded decimal (BCD) format
     * 
     * @param v
     *            original 8-bit value
     * @param n
     *            subtraction flag N
     * @param h
     *            half-carry/half-borrow flag H
     * @param c
     *            carry/borrow flag C
     * @return value adjusted to BCD format (value between 0 and 99) and flags
     *         ZN0C, packed in an int
     * @throws IllegalArgumentException
     *             if v isn't 8-bit value
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);

        // Java implementation of given algorithm (see project guidelines step 2, §2.7)
        int fixL = h | (!n & Bits.clip(4, v) > 9) ? 1 : 0;
        int fixH = c | (!n & v > 0x99) ? 1 : 0;
        int vAdj = n ? v - (0x60 * fixH + 6 * fixL)
                : v + (0x60 * fixH + 6 * fixL);
        vAdj = Bits.clip(8, vAdj);

        return packValueZNHC(vAdj, getZFlag(vAdj), n, false, (fixH == 1));
    }

    /**
     * Computes bitwise "and" of given 8-bit values
     * 
     * @param l
     *            first value
     * @param r
     *            second value
     * @return result and flags Z010, packed in an int
     * @throws IllegalArgumentException
     *             if l or r aren't 8-bit values
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int res = l & r;

        return packValueZNHC(res, getZFlag(res), false, true, false);
    }

    /**
     * Computes bitwise "or" of given values
     * 
     * @param l
     *            first value
     * @param r
     *            second value
     * @return result and flags Z000, packed in an int
     * @throws IllegalArgumentException
     *             if l or r aren't 8-bit values
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int res = l | r;

        return packValueZNHC(res, getZFlag(res), false, false, false);
    }

    /**
     * Computes bitwise "xor" of given values
     * 
     * @param l
     *            first value
     * @param r
     *            second value
     * @return result and flags Z000, packed in an int
     * @throws IllegalArgumentException
     *             if l or r aren't 8-bit values
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        int res = l ^ r;

        return packValueZNHC(res, getZFlag(res), false, false, false);
    }

    /**
     * Shifts given 8-bit value left by 1 bit
     * 
     * @param v
     *            value to shift
     * @return shifted value and Flags Z00C packed in an int C worth the value
     *         of the "ejected" bit (true for 1, false for 0)
     * @throws IllegalArgumentException
     *             if v isn't an 8-bits value
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);

        boolean c = Bits.test(v, 7);
        int res = Bits.clip(8, v << 1);

        return packValueZNHC(res, getZFlag(res), false, false, c);
    }

    /**
     * Shifts given 8-bit value right by 1 bit using arithmetic shifting >>
     * 
     * @param v
     *            value to shift
     * @return shifted value and Flags Z00C packed in an int – C worth the value
     *         of the "ejected" bit (true for 1, false for 0)
     * @throws IllegalArgumentException
     *             if v isn't an 8-bit value
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);

        boolean c = Bits.test(v, 0);
        int res = Bits.signExtend8(v);
        res = res >> 1;
        res = Bits.clip(8, res);

        return packValueZNHC(res, getZFlag(res), false, false, c);
    }

    /**
     * Shifts given 8-bit value right by 1 bit using logic shifting >>>
     * 
     * @param v
     *            value to shift
     * @return shifted value and Flags Z00C packed in an int – C worth the value
     *         of the "ejected" bit (true for 1, false for 0)
     * @throws IllegalArgumentException
     *             if v ins't an 8-bit value
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);

        boolean c = Bits.test(v, 0);
        int res = v >>> 1;

        return packValueZNHC(res, getZFlag(res), false, false, c);
    }

    /**
     * Rotates given 8-bit value in given direction by 1 bit
     * 
     * @param d
     *            rotation direction
     * @param v
     *            value to rotate
     * @return rotated value and flags Z00C, packed in an int – C worth the
     *         value of the bit which passed from one end to the other
     * @throws IllegalArgumentException
     *             if v ins't an 8-bit value
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);

        boolean c = false;
        int res = 0;

        if (d == RotDir.LEFT) {
            c = Bits.test(v, 7);
            res = Bits.rotate(8, v, 1);
        } else {
            c = Bits.test(v, 0);
            res = Bits.rotate(8, v, -1);
        }

        return packValueZNHC(res, getZFlag(res), false, false, c);

    }

    /**
     * Computes "rotation through carry" by 1 bit of given 8-bit value
     * 
     * @param d
     *            rotation direction
     * @param v
     *            value to rotate
     * @param c
     *            carry
     * @return rotated value and flags Z00C, packed in an int – C worth the MSB
     *         (bit at index 8) before truncating result to an 8-bits value
     * @throws IllegalArgumentException
     *             if v isn't an 8-bit value
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);

        int res = 0;
        res = Bits.set(v, 8, c);

        if (d == RotDir.LEFT) {
            res = Bits.rotate(9, res, 1);
        } else {
            res = Bits.rotate(9, res, -1);
        }

        boolean newC = Bits.test(res, 8);

        res = Bits.clip(8, res);

        return packValueZNHC(res, getZFlag(res), false, false, newC);
    }

    /**
     * Swaps four most significant bits with 4 least significant bits of given 8-bit value
     * 
     * @param v
     *            original value
     * @return value with swapped MSBs/LSBs and flags Z000, packed in an int
     * @throws IllegalArgumentException
     *             if v isn't 8-bit value
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);

        //TODO : remove
//        int vL = Bits.clip(4, v);
//        int vH = Bits.extract(v, 4, 4);
//
//        int vSwapped = (vL << 4) + vH;
        int vSwapped = Bits.rotate(8, v, 4);
        
        return packValueZNHC(vSwapped, getZFlag(vSwapped), false, false, false);
    }

    /**
     * Tests if bit of given value at given index is 0, and stores the result in
     * flag Z
     * 
     * @param v
     *            byte to be tested
     * @param bitIndex
     *            index of tested bit (0 to 7)
     * @return value 0 and flags Z010 packed in an int, where Z is 1 if bit at
     *         given index is 0
     * @throws IllegalArgumentException
     *             if v isn't an 8-bit value
     * @throws IndexOutOfBoundsException
     *             if bitIndex is not in [0 : 7] (both inclusive)
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, 8);

        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }

    /**
     * Packs value and flags in a single int.
     * Flags are stored on bits [4:7] –
     * Value is stored on bits [8:23] –
     * Bits at indices [0 : 3] and [24 : 31] are unused (0 by default)
     * 
     * @param v
     *            value, 16-bit max
     * @param z
     *            Z flag
     * @param n
     *            N flag
     * @param h
     *            H flag
     * @param c
     *            C flag
     * @return int of packed value and flags
     * @throws IllegalArgumentException
     *             if v isn't a 16 bit value
     */
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        Preconditions.checkBits16(v);
        
        return (v << 8) | maskZNHC(z, n, h, c);
    }

    /**
     * Computes Z-flag value
     * 
     * @param v
     *            value
     * @return flag Z value (true if v == 0, false otherwise)
     */
    private static boolean getZFlag(int v) {
        return (v == 0);
    }

    /**
     * Checks that given value respects given standard, namely that all bits at
     * index [0, 3] or [24, 31] (all boundaries inclusive) are set to 0
     * 
     * @param value
     *            value to check
     * @throws IllegalArgumentExcepetion
     *             if given value doesn't respect standard
     */
    private static void checkIntValue(int value) {
        int mask = (1 << 12) - 1;
        mask = Bits.rotate(32, mask, -8);
        if ((mask & value) != 0) {
            throw new IllegalArgumentException();
        }
    }

}
