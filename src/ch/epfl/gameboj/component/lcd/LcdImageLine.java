package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

public final class LcdImageLine {
    private final BitVector msb, lsb, opacity;
    private final int size; //optional, but handy
    // Should we define a size attribute instead of calling BitVector.size()
    // everywhere ?

    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        size = msb.size();
        Preconditions.checkArgument(
                size == lsb.size() && size == opacity.size());

        // Since BitVector is immutable, no need to copy here
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    public int size() {
        return size;
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
        // TODO implement
        return null;
    }

    public LcdImageLine below(LcdImageLine top) {
        Preconditions.checkArgument(top.size == size);
        //TODO implement
        return null;
    }

    public LcdImageLine below(LcdImageLine top, BitVector opacity) {
        Preconditions.checkArgument(top.size == size);
        BitVector msb = opacity.and(top.msb).or
        return null;
    }

    public LcdImageLine join(LcdImageLine other, int fromIndex) {
        Preconditions.checkArgument(other.size == size);
        // TODO implement
        return null;
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
