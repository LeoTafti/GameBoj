/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

public final class LcdImageLine {

    private static final int IDENTITY_PALETTE = 0b11_10_01_00;
    
    private final BitVector msb, lsb, opacity;

    /**
     * Constructor for LcdImageLine : creates a new line of pixels, represented
     * by 3 BitVectors (msb, lsb, opacity)
     * 
     * @param msb vector of line's pixels' msb
     * @param lsb vector of line's pixels' lsb
     * @param opacity vector of line's pixels' opacity
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());
        
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    public static final class Builder {
        BitVector.Builder msbB, lsbB;
        
        /**
         * Constructor for Builder : allows to create a new LcdImage
         * incrementally
         * 
         * @param size size of line, in pixels
         * 
         * @throws IllegalArgumentException
         *             if given size isn't bigger than 0 and a multiple of Integer.SIZE (32)
         */
        public Builder(int size) {
            Preconditions.checkArgument(size > 0 && size % Integer.SIZE == 0);
            
            msbB = new BitVector.Builder(size);
            lsbB = new BitVector.Builder(size);
        }

        /**
         * Sets byte at given index
         * 
         * @param index
         *            index of byte to set (in bytes)
         * @param mb
         *            new msb-byte
         * @param lb
         *            new lsb-byte
         * @return this, allows method chaining
         * 
         * @throws IndexOutOfBoundsException
         *             if given index is out-of-bounds (ie. not in [0, size()/Byte.SIZE[ )       
         * @throws IllegalArgumentException
         *             if mb or lb isn't an 8-bit value
         */
        public Builder setBytes(int index, int mb, int lb) {
            Objects.checkIndex(index, size()/Byte.SIZE);
            Preconditions.checkBits8(mb);
            Preconditions.checkBits8(lb);

            msbB.setByte(index, mb);
            lsbB.setByte(index, lb);
            return this;
        }

        /**
         * Builds LcdImageLine from previously specified pixels
         * 
         * @return new LcdImageLine from previously specified pixels
         */
        public LcdImageLine build() {
            BitVector msb = msbB.build();
            BitVector lsb = lsbB.build();

            return new LcdImageLine(msb, lsb, lsb.or(msb));
        }

        /**
         * Getter for Builder's size
         * 
         * @return Builder's size in bits
         */
        public int size() {
            // msbB and lsbB have the same size
            return msbB.size(); 
        }
    }

    /**
     * Getter for size
     * 
     * @return size of line, in pixels
     */
    public int size() {
        // msb, lsb, and opacity have the same size
        return msb.size(); 
    }

    /**
     * Getter for msb
     * 
     * @return msb of line
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Getter for lsb
     * 
     * @return lsb of line
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Getter for opacity
     * 
     * @return opacity of line
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * Shifts line by given number of pixels (delta)
     * 
     * @param delta
     *            number of pixels to shift by (positive for a left-shift,
     *            negative for a right-shift)
     * @return Shifted line, as new LcdImageLine
     * @see BitVector#shift(int delta)
     */
    public LcdImageLine shift(int delta) {
        return new LcdImageLine(msb.shift(delta), lsb.shift(delta), opacity.shift(delta));
    }

    /**
     * Extracts a size-pixels line from wrapped extension of this line, starting
     * from given index
     * 
     * @param fromIndex
     *            first pixel to extract
     * @param size
     *            Extracted line size (in bits)
     * @return Extracted line, as new LcdImageLine
     * 
     * @throws IllegalArgumentException
     *             if given size isn't bigger than 0 and a multiple of Integer.SIZE (32)
     * 
     * @see BitVector#extractWrapped(int fromIndex, int size)
     */
    public LcdImageLine extractWrapped(int fromIndex, int size) {
        Preconditions.checkArgument(size > 0 && size % Integer.SIZE == 0);
        
        return new LcdImageLine(msb.extractWrapped(fromIndex, size),
                lsb.extractWrapped(fromIndex, size),
                opacity.extractWrapped(fromIndex, size));
    }

    /**
     * Transforms color according to given palette, which represents the mapping
     * 
     * @param palette
     *            value used to represent the color mapping
     * @return new LcdImageLine with modified colors, according to given palette
     * @throws IllegalArgumentException
     *             if given palette isn't an 8-bit value
     */
    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);

        if (palette == IDENTITY_PALETTE)
            return this;

        BitVector newMsb = msb;
        BitVector newLsb = lsb;

        for (int color = 0; color < 4; color++) {
            int newColor = Bits.extract(palette, color * 2, 2);


            if (newColor == color)
                continue;

            int colorMsb = Bits.test(color, 1) ? 1 : 0;
            int colorLsb = Bits.test(color, 0) ? 1 : 0;
            int newColorMsb = Bits.test(newColor, 1) ? 1 : 0;
            int newColorLsb = Bits.test(newColor, 0) ? 1 : 0;


            BitVector maskMsb = colorMsb == 1 ? msb : msb.not();
            BitVector maskLsb = colorLsb == 1 ? lsb : lsb.not();
            BitVector changePos = maskMsb.and(maskLsb);

            //If same do nothing, otherwise flip (using xor) bits at required change positions
            newMsb = colorMsb == newColorMsb ? newMsb : newMsb.xor(changePos);
            newLsb = colorLsb == newColorLsb ? newLsb : newLsb.xor(changePos);
        }

        return new LcdImageLine(newMsb, newLsb, opacity);
    }

    /**
     * Composes a line by taking pixels from top if they are opaque, otherwise
     * pixels from this
     * 
     * @param top
     *            same-sized line, to be "placed" on top
     * 
     * @return new LcdImageLine composed from this and that according to top opacity
     * 
     * @throws IllegalArgumentException
     *            if given top line isn't same-sized as this
     */
    public LcdImageLine below(LcdImageLine top) {
        return below(top, top.opacity);
    }

    /**
     * Composes a line by taking pixels from top if given opacity BitVector at
     * this index is 1, otherwise pixels from this
     * 
     * @param top
     *            same-sized line, to be "placed" on top
     * @param opacity
     *            opacity BitVector, allows to choose which pixel we want to
     *            take from each LcdImageLine
     *            
     * @return new LcdImageLine composed from this and that according to given opacity
     * 
     * @throws IllegalArgumentException
     *            if given line isn't the same-size as this
     */
    public LcdImageLine below(LcdImageLine top, BitVector opacity) {
        Preconditions.checkArgument(top.size() == size());
        
        BitVector msb = opacity.and(top.msb).or(opacity.not().and(this.msb));
        BitVector lsb = opacity.and(top.lsb).or(opacity.not().and(this.lsb));
        BitVector op = opacity.or(this.opacity);

        return new LcdImageLine(msb, lsb, op);
    }

    /**
     * "Joins" this line with given same-sized other LcdImageLine
     * 
     * @param other
     *            same-size line
     * @param fromIndex
     *            index from which to take other line bits
     * @return joined line, as new LcdImageLine
     * 
     * @throws IllegalArgumentException
     *             if given line isn't the same size as this
     */
    public LcdImageLine join(LcdImageLine other, int fromIndex) {
        Preconditions.checkArgument(other.size() == size());
        BitVector mask = new BitVector(size(), true).shift(fromIndex);

        BitVector joinedMsb = other.msb.and(mask).or(this.msb.and(mask.not()));
        BitVector joinedLsb = other.lsb.and(mask).or(this.lsb.and(mask.not()));
        BitVector joinedOpacity = other.opacity.and(mask)
                .or(this.opacity.and(mask.not()));

        return new LcdImageLine(joinedMsb, joinedLsb, joinedOpacity);
    }

    /**
     * Computes pixel color at given index
     * 
     * @param index
     *            pixel index in line
     * @return pixel color at given index
     * 
     * @throws IndexOutOfBoundsException
     *             if given index is out-of-bounds (not in [0, size()[ )
     */
    public int pixelColor(int index) {
        Objects.checkIndex(index, size());

        if (opacity.testBit(index)) {
            int msbI = msb.testBit(index) ? 1 : 0;
            int lsbI = lsb.testBit(index) ? 1 : 0;
            return (msbI << 1) | lsbI;
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object thatO) {
        if (!(thatO instanceof LcdImageLine))
            return false;

        LcdImageLine that = (LcdImageLine) thatO;
        return (msb.equals(that.msb) && lsb.equals(that.lsb)
                && opacity.equals(that.opacity));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }
}
