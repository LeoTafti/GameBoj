package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {

    private enum ExtractType {
        ZERO_EXT, WRAPPED
    };

    private final int[] elements;

    /**
     * Constructs a BitVector of given size (in bits), all bits set to given
     * default value (false for 0, true for 1)
     * 
     * @param size
     *            size in bits
     * @param defaultValue
     *            initial value of all bits
     * @throws IllegalArgumentException
     *             if size is negative or size isn't a multiple of Integer.SIZE
     *             (32)
     */
    public BitVector(int size, boolean defaultValue) {
        Preconditions.checkArgument(size >= 0 && size % Integer.SIZE == 0);

        // TODO : should we absolutely call the private constructor ?
        // means we have define a method like "initializeArray" or something of
        // the sort
        elements = new int[size];

        Arrays.fill(elements, defaultValue ? 1 : 0);
    }

    /**
     * Constructs a BitVector of given size (in bits), all bits set by default
     * to 0
     * 
     * @param size
     *            size in bits
     * @param defaultValue
     *            initial value of all bits
     * @throws IllegalArgumentException
     *             if size is negative or size isn't a multiple of Integer.SIZE
     *             (32)
     */
    public BitVector(int size) {
        this(size, false);
    }

    /**
     * Private constructor Constructs a BitVector from given bits array (no
     * copy)
     * 
     * @param elements
     *            bits of BitVector
     */
    private BitVector(int[] elements) {
        this.elements = elements;
    }

    /**
     * Builder for BitVector Allows to create a BitVector incrementally, byte by
     * byte
     */
    public final static class Builder {
        private int[] elements;

        /**
         * Creates a new active Builder
         * 
         * @param size
         *            number of bits, must be 0mod32
         */
        public Builder(int size) {
            Preconditions.checkArgument(size >= 0 && size % Integer.SIZE == 0);
            elements = new int[size / Integer.SIZE];
        }

        /**
         * Builds BitVector of previously given elements (0 as default)
         * 
         * @return
         */
        public BitVector build() {
            if (elements == null)
                throw new IllegalStateException("already built");
            BitVector r = new BitVector(elements);
            elements = null;
            return r;

        }

        /**
         * Sets byte at given index of pending BitVector
         * 
         * @param index
         *            byte's LSB will be written at BitVetor's index
         * @param b
         *            byte value
         * @return
         */
        public Builder setByte(int index, byte b) {
            Objects.checkIndex(0, elements.length);
            if (elements == null)
                throw new IllegalStateException("already built");
            else
                elements[index / Integer.BYTES] += b << index * Byte.SIZE;
            return this;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object thatO) {
        return (thatO instanceof BitVector
                && Arrays.equals(elements, ((BitVector) thatO).elements));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i : elements) {
            sb.append(Integer.toBinaryString(i));
        }
        return sb.toString();
    }

    /**
     * Getter on BitVector size in bits
     * 
     * @return size in bits
     */
    public int size() {
        return elements.length * Integer.SIZE;
    }

    /**
     * Tests if bit at given index is 1
     * 
     * @param index
     *            index of bit to test
     * @return true if 1, false if 0
     * @throws IllegalArgumentException
     *             if given index is negative, or greater or equal this size in
     *             bits
     */
    public boolean testBit(int index) {
        Preconditions.checkArgument(index >= 0 && index < size());
        return Bits.test(elements[index / Integer.SIZE], index % Integer.SIZE);
    }

    /**
     * Computes bitwise "and" of this BitVector and given BitVector
     * 
     * @param that
     *            other BitVector
     * @return bitwise "and", as a new BitVector
     */
    public BitVector and(BitVector that) {
        int length = elements.length;
        Preconditions.checkArgument(length == that.elements.length);

        int[] res = new int[length];
        for (int i = 0; i < length; i++)
            res[i] = elements[i] & that.elements[i];
        return new BitVector(res);
    }

    /**
     * Computes bitwise "or" of this BitVector and given BitVector
     * 
     * @param that
     *            other bitVector
     * @return bitwise "or", as a new BitVector
     */
    public BitVector or(BitVector that) {
        int length = elements.length;
        Preconditions.checkArgument(length == that.elements.length);

        int[] res = new int[length];
        for (int i = 0; i < length; i++)
            res[i] = elements[i] | that.elements[i];
        return new BitVector(res);
    }

    /**
     * Computes the generalized extraction of size-bits, in the zero-extended
     * version ("extends" BitVector by adding as many 0's as necessary)
     * 
     * @param fromIndex
     *            index of LSB to extract (inclusive)
     * @param size
     *            number of bits to extract
     * @return new BitVector of extracted bits
     */
    public BitVector extractZeroExtended(int fromIndex, int size) {
        return extract(fromIndex, size, ExtractType.ZERO_EXT);
    }

    /**
     * Computes the generalized extraction of size-bits, in the wrapped version
     * ("extends" BitVector by repeating its bits over and over)
     * 
     * @param fromIndex
     *            index of LSB to extract (inclusive)
     * @param size
     *            number of bits to extract
     * @return new BitVector of extracted bits
     */
    public BitVector extractWrapped(int fromIndex, int size) {
        return extract(fromIndex, size, ExtractType.WRAPPED);
    }

    private BitVector extract(int fromIndex, int size, ExtractType type) {
        Preconditions.checkArgument((size%Integer.SIZE) == 0);
        int[] ex = new int[size];
        
        for(int i = 0; i < size/Integer.SIZE; i++) { //iterates on each 32-bit chunk
            ex[i] = combinedExtended32bits(fromIndex+Integer.SIZE*i, 
                        type);
        }
        
        return new BitVector(ex);
    }
    
    private int combinedExtended32bits(int i, ExtractType type) {

        int chunk = Math.floorMod(Math.floorDiv(i,Integer.SIZE),size()); //chunk of 'elements' designated by index
        int part = Math.floorMod(i,Integer.SIZE); //size of chunk designated by index

        if(type == ExtractType.WRAPPED) {
        return  Bits.extract(elements[chunk], part, 32-part) + //msb of chunk starting from 'part' as new32 lsb
            (Bits.clip(elements[Math.floorMod((chunk + 1), size())], part) << 32-part); //lsb of next chunk as new32 msb
        }
        
        else { //ExtractType == ZERO_EXT
// chunk not defined
            if( chunk < 0 || chunk > elements.length) { 
   // and chunk+1 not defined 
      //returns 32 0s, default case
                if(chunk+1 >= 0 && chunk+1 < elements.length) 
   // and chunk+1 defined
                return (Bits.clip(elements[chunk + 1], part) << 32-part); 
      //lsb of chunk+1 as new32 msb
            }
//chunk defined
            else {
   //chunk+1 not defined
                if( chunk+1 < 0 || chunk+1 >= elements.length)
      //msd of chunk starting from part as new32 lsb
                    return  Bits.extract(elements[chunk], part, 32-part);
   //chunk+1 defined
            else {
     //msb of chunk starting from part as new32 lsb
                return  Bits.extract(elements[chunk], part, 32-part) +
     //lsb of chunk+1 up to 32-part as new32 msb
                (Bits.clip(elements[chunk + 1], part) << 32-part);
                }
            }
        }
        return 0; //default case
    }

    /**
     * Computes shifting by delta-bits
     * 
     * @param delta
     *            number of bits to shift by (positive for a left shift,
     *            negative for a right shift)
     * @return new bitVector with shifted bits
     */
    public BitVector shift(int delta) {
        return extract(-delta, Integer.SIZE, ExtractType.ZERO_EXT);
    }
}
