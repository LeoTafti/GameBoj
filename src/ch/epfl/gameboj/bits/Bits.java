/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bits;

import java.util.Objects;
import ch.epfl.gameboj.Preconditions;

public final class Bits {

    /**
     * Non-instantiable class
     */
    private Bits() {}

    /**
     * Creates bit-string with single one at given index
     * 
     * @param index
     *            required position of the 1 bit (starts at 0)
     * @return bit-string containing a single 1
     * @throws IndexOutOfBoundsException
     *             if index is out-of-bound
     */
    public static int mask(int index) {
        Objects.checkIndex(index, Integer.SIZE);
        return (int) Math.round(Math.pow(2, index));
    }

    /**
     * Tests if bit at given index is 1
     * 
     * @param bits
     *            bit-string
     * @param index
     *            position of tested bit
     * @return True if given bit is 1, False if 0
     * @throws IndexOutOfBoundsException
     *             if index is greater than 31
     */
    public static boolean test(int bits, int index) {
        Objects.checkIndex(index, Integer.SIZE);
        return ((bits & mask(index)) != 0);
    }

    /**
     * Tests if bit at given index is 1
     * 
     * @param bits
     *            bit-string
     * @param bit
     *            bit to check
     * @return True if given bit is 1, False if 0
     * @throws IndexOutOfBoundsException
     *             if index is out-of-bound
     * @see Bits#test(int bits, int index)
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * Sets bit at given index to given value
     * 
     * @param bits
     *            original bit-string
     * @param index
     *            position of change (starts at 0)
     * @param newValue
     *            true for 1, false for 0
     * @return modified bit-string
     * @throws IndexOutOfBoundsException
     *             if index is greater than 31
     */
    public static int set(int bits, int index, boolean newValue) {
        Objects.checkIndex(index, Integer.SIZE);
        if (newValue) {
            return (bits | mask(index));
        } else {
            return (bits & ~mask(index));
        }
    }

    /**
     * Keeps required number of least significant bits from original bit-string
     * 
     * @param size
     *            number of bits to keep
     * @param bits
     *            original bit-string
     * @return clipped bit-string
     * @throws IllegalArgumentException
     *             if size isn't between 0 (inclusive) and 32 (inclusive)
     */
    public static int clip(int size, int bits) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.SIZE);

        if (size == Integer.SIZE) {
            return bits;
        }
        int mask = (1 << size) - 1;
        return (bits & mask);

    }

    /**
     * Extracts from given bit-string required number of bits from start
     * location (inclusive) to start+size (exclusive)
     * 
     * @param bits
     *            original bit-string
     * @param start
     *            least significant extracted bit
     * @param size
     *            number of bits to extract
     * @return bit string with extracted bits as least-significant bits
     * @throws IndexOutOfBoundsException
     *             if interval designated by start and size isn't valid
     */
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        int extract = bits >>> start;
        return clip(size, extract);
    }

    /**
     * Rotates given bit-string's size-least significant bits by given distance
     * 
     * @param size
     *            number of bits to rotate
     * @param bits
     *            original bit-string
     * @param distance
     *            distance (in bits) of rotation (positive for leftwards,
     *            negative for rightwards)
     * @return original bit-string with size-least significant bits rotated by
     *         given distance;
     * @throws IllegalArgumentException
     *             if size isn't between 0 (exclusive) and 32 (inclusive) or if
     *             bits isn't a size-bit value
     */
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(size > 0 && size <= Integer.SIZE);
        //TODO : fails a test in BitsTest...
        if(size < Integer.SIZE)
            Preconditions.checkArgument(bits < (1<<size)); 
        
        int clipped = clip(size, bits);

        int originalClipped = bits - clipped;
        // 0 in size least-significant bits

        int rotated = clipped << Math.floorMod(distance, size)
                | clipped >>> size - Math.floorMod(distance, size);
        // bitwise left shift of distance mod(size) concatenated with bitwise right
        // shift of size-distance

        return originalClipped + clip(size, rotated);
        // original bit-string most significant bits concatenated with size
        // rotated least significant bit
    }

    /**
     * Extends sign (0 for positive ; 1 for negative ) of byte to remaining most significant
     * (32-8) bits
     * 
     * @param bits
     *            original byte
     * @return 24 most significant bits of value 0 or 1 and original byte as
     *         least significant 8 bits
     * @throws IllegalArgumentException
     *             if bits isn't an 8-bit value
     */
    public static int signExtend8(int bits) {
        Preconditions.checkBits8(bits);
        return (int) (byte) bits;
    }

    /**
     * Reverses 8 least significant bits (0 -> 7 , 1 -> 6 , ...)
     * 
     * @param bits
     *            original byte
     * @return reversed byte
     * @throws IllegalArgumentException
     *             if bits isn't an 8-bit value
     */
    public static int reverse8(int bits) {
        Preconditions.checkBits8(bits);
        int[] table = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60,
                0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0, 0x08,
                0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58,
                0xD8, 0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24,
                0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74,
                0xF4, 0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C,
                0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42,
                0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32,
                0xB2, 0x72, 0xF2, 0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A,
                0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06,
                0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56,
                0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE, 0x2E,
                0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E,
                0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11,
                0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49,
                0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39,
                0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65,
                0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D,
                0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D,
                0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23,
                0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73,
                0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B,
                0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47,
                0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37,
                0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F,
                0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };
        return table[bits];
    }

    /**
     * Computes bitwise complement of given byte
     * 
     * @param bits
     *            original byte
     * @return bitwise complement of given byte
     * @throws IllegalArgumentException
     *             if bits isn't an 8-bit value
     */
    public static int complement8(int bits) {
        Preconditions.checkBits8(bits);
        return bits^0xFF;
    }

    /**
     * Constructs a 16-bit bit-string by concatenating two bytes
     * 
     * @param highB
     *            most significant byte of returned bit-string
     * @param lowB
     *            least significant byte or returned bit-string
     * @return 16-bit bit-string from given bytes
     * @throws IllegalArgumentException
     *             if highB or lowB aren't 8-bit values
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(highB);
        Preconditions.checkBits8(lowB);
        return (highB << 8) + lowB;
    }

}
