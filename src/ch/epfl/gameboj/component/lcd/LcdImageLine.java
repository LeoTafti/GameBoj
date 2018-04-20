package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

public final class LcdImageLine {
    private final BitVector msb, lsb, opacity;
    // Should we define a size attribute instead of calling BitVector.size()
    // everywhere ?

    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && msb.size() == opacity.size());

        // Since BitVector is immutable, no need to copy here
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }
    
    public static final class Builder{
        BitVector msb, lsb;
        
        public Builder(BitVector msb, BitVector lsb) {
            this.msb = msb;
            this.lsb = lsb;
        }
        
        public Builder setBytes(int index, int b) {
            
        }
        
        public LcdImageLine build() {
            
        }
    }

    public int size() {
        return msb.size(); //ok since msb, lsb, and opacity all have same size
    }

    public BitVector msb() {
        return msb;
    }

    public BitVector lsb() {
        return lsb;
    }

    public BitVector opacity() {
        return opacity;
    }

    public LcdImageLine shift(int d) {
        return new LcdImageLine(msb.shift(d), lsb.shift(d), opacity.shift(d));
    }

    public LcdImageLine extractWrapped(int fromIndex, int size) {
        return new LcdImageLine(
                msb.extractWrapped(fromIndex, size),
                lsb.extractWrapped(fromIndex, size),
                opacity.extractWrapped(fromIndex, size));
    }

    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);
        
        if(palette == 0b11_10_01_00)    //identity palette
            return this;
        
        BitVector newMsb = msb;
        BitVector newLsb = lsb;
        
        for(int color = 0; color < 4; color++) {
            int newColor = palette & (0b11 << 2*color);
            
            if (newColor == color)  //color doesn't change
                continue;
            
            int colorMsb = color & 0b10;
            int colorLsb = color & 0b01;
            int newColorMsb = newColor & 0b10;
            int newColorLsb = newColor & 0b01;
            
            BitVector maskMsb = colorMsb == 1 ? msb : msb.not();
            BitVector maskLsb = colorLsb == 1 ? lsb : lsb.not();
            BitVector changePos = maskMsb.and(maskLsb);
            
            newMsb = colorMsb == newColorMsb ? msb : msb.xor(changePos);
            newLsb = colorLsb == newColorLsb ? lsb : lsb.xor(changePos);
        }
        
        return new LcdImageLine(newMsb, newLsb, opacity);
    }

    public LcdImageLine below(LcdImageLine top) {
        return below(top, top.opacity);
    }

    public LcdImageLine below(LcdImageLine top, BitVector opacity) {
        Preconditions.checkArgument(top.size() == size());
        //(opacity AND top.msb) OR (NOT(opacity) AND this.msb)
        BitVector msb = opacity.and(top.msb).or(opacity.not().and(this.msb)); 
        BitVector lsb = opacity.and(top.lsb).or(opacity.not().and(this.lsb));
        
        BitVector op = opacity.and(this.opacity);
        
        return new LcdImageLine(msb, lsb, op);
    }

    public LcdImageLine join(LcdImageLine other, int fromIndex) {
        Preconditions.checkArgument(other.size() == size());
        BitVector mask = new BitVector(size(), true).shift(fromIndex);
        
        // (other.mbs AND mask) OR (this.msb AND (NOT mask))
        //
        // selects (fromIndex)-least significant bits of this.msb
        // and concatenates (size-fromIndex)-most significant bits of other.msb
        BitVector joinedMsb = other.msb.and(mask).or(this.msb.and(mask.not()));
        BitVector joinedLsb = other.lsb.and(mask).or(this.lsb.and(mask.not()));
        BitVector joinedOpacity = other.opacity.and(mask).or(this.opacity.and(mask.not()));
        
        return new LcdImageLine(joinedMsb, joinedLsb, joinedOpacity);
    }
    
    public int pixelColor(int index) {
        if(opacity.testBit(index)) {
            int msbI = msb.testBit(index) ? 1 : 0;
            int lsbI = lsb.testBit(index) ? 1 : 0;
            return msbI << 1 | lsbI;
        }
        return 0;
    }

    @Override
    public boolean equals(Object thatO) {
        if (!(thatO instanceof LcdImageLine))
            return false;

        LcdImageLine that = (LcdImageLine) thatO;
        return (msb.equals(that.msb) && lsb.equals(that.lsb)
                && opacity.equals(that.opacity));
    }

    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }
}
