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
     *             if size is smaller or equals zero or size isn't a multiple of
     *             Integer.SIZE (32)
     */
    public BitVector(int size, boolean defaultValue) {
        Preconditions.checkArgument(size > 0 && size % Integer.SIZE == 0);

        // TODO : should we absolutely call the private constructor ?
        // means we have define a method like "initializeArray" or something of
        // the sort
        elements = new int[size/Integer.SIZE];

        Arrays.fill(elements, defaultValue ? -1 : 0); //since int -1 in two's complement representation is 32 bits to 1
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
     *             if size is smaller or equals zero or size isn't a multiple of
     *             Integer.SIZE (32)
     */
    public BitVector(int size) {
        this(size, false);
    }

    /**
     * Private constructor
     * Constructs a BitVector from given bits array (no copy)
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
    public static final class Builder {
        private int[] elements;

        /**
         * Creates a new active Builder
         * 
         * @param size
         *            number of bits, must be 0mod32
         * @throws IllegalArgumentException
     *             if size is smaller or equals zero or size isn't a multiple of
     *             Integer.SIZE (32)
         */
        public Builder(int size) {
            Preconditions.checkArgument(size > 0 && size % Integer.SIZE == 0);
            elements = new int[size / Integer.SIZE];
        }

        /**
         * Builds BitVector of previously given elements (0 as default)
         * 
         * @throws IlegalStateException if already built
         * @return built vector from given bytes
         */
        public BitVector build() {
            if (elements == null)
                throw new IllegalStateException("Already built");
            BitVector r = new BitVector(elements);
            elements = null;
            
            return r;
        }

        /**
         * Sets byte at given index of pending BitVector
         * 
         * @param index
         *            bitVector's Index in BYTES (i.e 8-th bit is index 1)
         * @param b
         *            byte value to add
         *            
         * @return builder
         * 
         * @throws IlegalStateException if already built
         */
        public Builder setByte(int index, int b) {
            if (elements == null)
                throw new IllegalStateException("Already built");
            
            Objects.checkIndex(index, elements.length*Integer.BYTES);
            Preconditions.checkBits8(b);
            
            elements[index / Integer.BYTES] += b << index*Byte.SIZE;
            return this;
        }
        
        /**
         * Getter for Builder's size
         * @return Builder's size in bits
         */
        public int size() {
            return elements.length * Integer.SIZE;
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
        //TODO : move it somewhere else ?
        final String ZERO_32 = "00000000000000000000000000000000";
        
        StringBuilder sb = new StringBuilder();
        for (int i = elements.length-1 ; i >= 0 ; i--) {
            String binaryRep = Integer.toBinaryString(elements[i]);
            sb.append((ZERO_32 + binaryRep).substring(binaryRep.length()));
            //TODO : do it with numberOfLeadingZeros instead ?
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
     * Computes bitwise "not" of this BitVector
     * 
     * @return bitwise "not", as a new BitVector
     */
    public BitVector not() {
        int[] notElements = new int[elements.length];
        for(int i = 0; i < notElements.length; i++) {
            notElements[i] = ~ elements[i];
        }
        return new BitVector(notElements);
    }
    
    /**
     * Computes bitwise "and" of this BitVector and given BitVector
     * 
     * @param that
     *            other BitVector
     *            
     * @throws IllegalArgumentException if vectors are not of the same length
     * 
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
     *            
     * @throws IllegalArgumentException if vectors are not of the same length
     * 
     * @return bitwise "or", as a new BitVector
     */
    public BitVector or(BitVector that) {
        int length = elements.length;
        Preconditions.checkArgument(length == that.elements.length);

        int[] res = new int[length];
        for (int i = 0; i < length; i++) {
            res[i] = elements[i] | that.elements[i];
        }
        return new BitVector(res);
    }
    
    /**
     * Computes bitwise "xor" of this BitVector and given BitVector
     * 
     * @param that
     *            other bitVector
     * 
     * @throws IllegalArgumentException if vectors are not of the same length
     * @return bitwise "xor", as a new BitVector
     */
    public BitVector xor(BitVector that) {
        int length = elements.length;
        Preconditions.checkArgument(length == that.elements.length);

        int[] res = new int[length];
        for (int i = 0; i < length; i++) {
            res[i] = elements[i] ^ that.elements[i];
        }
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
     * 
     * @throws IllegalArgumentException
     *             if size is smaller or equals zero or size isn't a multiple of
     *             Integer.SIZE (32)
     */
    public BitVector extractWrapped(int fromIndex, int size) {
        return extract(fromIndex, size, ExtractType.WRAPPED);
    }

    private BitVector extract(int fromIndex, int size, ExtractType type) {
        Preconditions.checkArgument(size > 0 && size % Integer.SIZE == 0);
        int[] extracted = new int[size / Integer.SIZE];
        
        for(int i = 0; i < extracted.length; i++) { //iterates on each 32-bit chunk
            extracted[i] = combinedExtended32bits(fromIndex + Integer.SIZE*i, type);
        }
        
        return new BitVector(extracted);
    }
    
    /**
     * extracts 32 bits from index with given method (ZERO_EXT, WRAPPED)
     * @param i index from wich to extract (positive for left, negative for right)
     * @param type ZERO_EXT will add 0s if out of bounds, WRAPPED will start again if out of bounds
     * @return 32-bit extraction
     */
    private int combinedExtended32bits(int i, ExtractType type) {
        
        int bits = 0;
        int chunk = Math.floorDiv(i, Integer.SIZE); //chunk (32bits) of 'elements' designated by index
        int index = Math.floorMod(i, Integer.SIZE); //relative index in chunk [0;31]
        int complIndex = Integer.SIZE-index;
        
        //Optimal case
        if (index == 0) {
            switch (type) {
            case WRAPPED:
                bits = elements[Math.floorMod(chunk, elements.length)];
                break;
                
            case ZERO_EXT:
                if (chunk >= 0 && chunk < elements.length)
                    bits = elements[chunk];
                break;
            }
        }
        else {
            switch (type) {
            case WRAPPED:
                // MSBs of chunk starting from index as bits LSBs
                bits = Bits.extract(
                        elements[Math.floorMod(chunk, elements.length)], index,
                        complIndex);

                // LSBs of next chunk as bits MSBs
                bits |= Bits.clip(index, elements[Math.floorMod(chunk + 1,
                        elements.length)]) << complIndex;
                break;

            case ZERO_EXT:
                // MSBs of chunk starting from index as bits LSBs
                if (chunk >= 0 && chunk < elements.length)
                    bits |= Bits.extract(elements[chunk], index, complIndex);

                // LSBs of next chunk as bits MSBs
                if (chunk + 1 >= 0 && chunk + 1 < elements.length)
                    bits |= Bits.clip(index, elements[chunk + 1]) << complIndex;
                break;
            }
        }
        return bits;
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
        return extract(-delta, size(), ExtractType.ZERO_EXT);
    }
}
