/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;

public class LcdImageLineTest {

    // -------------------- CONSTRUCTOR TEST --------------------
   
    @Test
    public void LcdImageLineFailOnDifferentSizeBitVector() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine(bit64_0(), bit64_0(), allZeros()));
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine( bit64_0(), allZeros(), bit64_0()));
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine( allZeros(), bit64_0(),  bit64_0()));
    }
    
    // -------------------- BUILDER TEST     --------------------
       
    @Test
    public void BuilderConstructorsFailOnInvalidSize() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine.Builder(-1));
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine.Builder(0));
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine.Builder(31));
        assertThrows(IllegalArgumentException.class, 
                () -> new LcdImageLine.Builder(-32));
    }
    
    @Test
    public void BuilderSizeWorks() {
        LcdImageLine.Builder b = new LcdImageLine.Builder(32);
        LcdImageLine.Builder d = new LcdImageLine.Builder(64);
        
        assertEquals(32, b.size());
        assertEquals(64, d.size());
        
    }
    
    @Test
    public void setBytesWorks() {
        
        LcdImageLine.Builder b = new LcdImageLine.Builder(32);
        LcdImageLine l = b.build();       
        
//        assertEquals(colorLine_0(), l);
        
        b.setBytes(0, 0xF0, 0x0F);
        
        assertEquals(lsb0FmsbF0(), b.build());
        
    }
    
    @Test
    public void setBytesWorksOnBigLine() {
        
        LcdImageLine.Builder b = new LcdImageLine.Builder(64);
        
        assertEquals(bigColorLine_0(), b.build());
        
        b.setBytes(4, 0xF0, 0x0F);
        
        assertEquals(bigLsb0FmsbF0(), b.build() );
    }
    
    @Test
    public void setBytesFailsOnInvalidIndex() {
        LcdImageLine.Builder b = new LcdImageLine.Builder(32);
        
        assertThrows( IndexOutOfBoundsException.class,
                () -> b.setBytes(4, 0xf, 0xf));
        assertThrows( IndexOutOfBoundsException.class,
                () -> b.setBytes(-1, 0xf, 0xf));
    }
    
    @Test
    public void setBytesFailsOnInvalidBytes() {
        LcdImageLine.Builder b = new LcdImageLine.Builder(32);
        
        assertThrows( IllegalArgumentException.class,
                () -> b.setBytes(0, 0x100, 0xf));
        assertThrows( IllegalArgumentException.class,
                () -> b.setBytes(0, 0xf, 0x100));
    }
    
    // -------------------- GETTERS TEST     --------------------
    
    @Test
    public void sizeWorks() {
        assertEquals(32, colorLine_0().size());
        assertEquals(64, bigColorLine_0().size());
        
    }
    
    @Test
    public void msbWorks() {
        assertEquals(bit32_ff00(), colorLine_3_2_1_0().msb());
//        assertEquals(bit64_00f0_0000(), bigLsb0FmsbF0().msb());
    }
    
    @Test
    public void lsbWorks() {
        assertEquals(bit32_f0f0(), colorLine_3_2_1_0().lsb());
//        assertEquals(bit64_000f_0000(), bigLsb0FmsbF0().lsb());
        
    }
    
//    @Test
//    public void opacityWorks() {
//        
//    }
    
    // -------------------- SHIFT TEST       --------------------
    @Test
    public void TrivialShiftRightWorks() {
        LcdImageLine l = line_1().shift(-1);
        assertEquals(singleBit32(), l.msb());
        assertEquals(singleBit32(), l.lsb());
        assertEquals(singleBit32(), l.opacity());
    }
    
    @Test
    public void TrivialShiftLeftWorks() {
        LcdImageLine l = line_1().shift(1);
        assertEquals(singleBit2(), l.msb());
        assertEquals(singleBit2(), l.lsb());
        assertEquals(singleBit2(), l.opacity());
    }
    
    @Test
    public void shiftDoesNothingForNullDelta() {
        assertEquals(colorLine_1_2_3_0(), colorLine_1_2_3_0().shift(0));
    }
    
    @Test
    public void shiftWorksForBigDelta() {
        LcdImageLine l = line_1().shift(33);
        assertEquals(singleBit2(), l.msb());
        assertEquals(singleBit2(), l.lsb());
        assertEquals(singleBit2(), l.opacity());
        
        LcdImageLine m = line_1().shift(-65);
        assertEquals(singleBit32(), m.msb());
        assertEquals(singleBit32(), m.lsb());
        assertEquals(singleBit32(), m.opacity());
    }
    
    // -------------------- EXTRACT TEST     --------------------
    
    @Test
    public void trivialExtractWorks() {
        
    }
    
    @Test
    public void extractFailsForInvalidIndex1() {
        
    }
    
    @Test
    public void extractFailsForInvalidIndex2() {
        
    }
    
    @Test
    public void extractWorksForBigSize() {
        
    }
    
    // -------------------- MAP COLORS TEST  --------------------
    
    @Test
    public void changeColorAllColors() {
        
        
        LcdImageLine l = colorLine_3_2_1_0();
        LcdImageLine ex = colorLine_1_2_3_0();
        int palette = 0b00_11_10_01;
        LcdImageLine colored = l.mapColors(palette);
        
        assertEquals(colored.msb().toString(), ex.msb().toString());
        assertEquals(colored.lsb().toString(), ex.lsb().toString());
        
        
    }
    
    @Test
    public void changeColorTrivial() {
        LcdImageLine l = colorLine_3_2_1_0();
        LcdImageLine ex = colorLine_3();
        int palette = 0b11_11_11_11;
        LcdImageLine colored = l.mapColors(palette);
        
        assertEquals(ex.msb().toString(), colored.msb().toString());
        assertEquals(ex.lsb().toString(), colored.lsb().toString());        
    }
    
    public void mapColorsFailsForinvalidPalette() {
        
    }
    
    @Test
    public void mapColorsWorkForNonTrivialCase() {
        
    }
    
    
    // -------------------- BELOW TEST       --------------------
    
    @Test
    public void trivialBelowWorks() {
        
    }
    
    @Test
    public void belowFailsOnNonEqualLengths() {
        
    }
    
    @Test
    public void trivialBelowWorksGivenOpacity() {
        
    }
    
    @Test
    public void belowFailsOnWrongLengthOpacity() {
        
    }
    
    @Test
    public void belowWorksOnNonTrivialLines() {
        
    }
    
    
    // -------------------- JOIN TEST        --------------------
    @Test
    public void joinWorksOnTrivialValues() {
        
    }
    
    @Test
    public void joinFailsOnDifferentLengths() {
        
    }
    
    @Test
    public void joinFailsOnInvalidIndex() {
        
    }
    
    @Test
    public void joinWorksOnNonTrivialValues() {
        
    }
    
    // -------------------- PIXEL COLOR TEST --------------------
    
    
    
    // ++++++++++++++++++++ PRESET LINES ++++++++++++++++++++++++
    
    
    private LcdImageLine colorLine_0() {
        int[] MSBchunks = { 0, 0, 0, 0};
        int[] LSBchunks = { 0, 0, 0, 0};
        int[] OPchunks =  { 0, 0, 0, 0};
        
        BitVector.Builder msbB = new Builder(Integer.SIZE);
        BitVector.Builder lsbB = new Builder(Integer.SIZE);
        BitVector.Builder opB =  new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            msbB.setByte(i, MSBchunks[i]);
            lsbB.setByte(i, LSBchunks[i]);
            opB.setByte(i, OPchunks[i]);
        }
        
        BitVector msb = msbB.build();
        BitVector lsb = lsbB.build();
        BitVector op = opB.build();
        
        return new LcdImageLine(msb, lsb, op);
    }
    
    private LcdImageLine colorLine_3() {
        int[] MSBchunks = { 0xff, 0xff, 0xff, 0xff};
        int[] LSBchunks = { 0xff, 0xff, 0xff, 0xff};
        int[] OPchunks =  { 0xff, 0xff, 0xff, 0xff};
        
        BitVector.Builder msbB = new Builder(Integer.SIZE);
        BitVector.Builder lsbB = new Builder(Integer.SIZE);
        BitVector.Builder opB =  new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            msbB.setByte(i, MSBchunks[i]);
            lsbB.setByte(i, LSBchunks[i]);
            opB.setByte(i, OPchunks[i]);
        }
        
        BitVector msb = msbB.build();
        BitVector lsb = lsbB.build();
        BitVector op = opB.build();
        
        return new LcdImageLine(msb, lsb, op);
    }

    private LcdImageLine colorLine_3_2_1_0() {
        int[] MSBchunks = { 0b0000_0000, 0b0000_0000,  0b1111_1111, 0b1111_1111};
        int[] LSBchunks = { 0b0000_0000, 0b1111_1111, 0b0000_0000, 0b1111_1111};
        int[] OPchunks = { 0xff, 0xff, 0xff, 0xff};
        
        BitVector.Builder msbB = new Builder(Integer.SIZE);
        BitVector.Builder lsbB = new Builder(Integer.SIZE);
        BitVector.Builder opB =  new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            msbB.setByte(i, MSBchunks[i]);
            lsbB.setByte(i, LSBchunks[i]);
            opB.setByte(i, OPchunks[i]);
        }
        
        BitVector msb = msbB.build();
        BitVector lsb = lsbB.build();
        BitVector op = opB.build();
        
        return new LcdImageLine(msb, lsb, op);
        
    }
    
    private LcdImageLine colorLine_1_2_3_0() {
        
        int[] MSBchunks = {  0b0000_0000, 0b1111_1111, 0b1111_1111, 0b0000_0000};
        int[] LSBchunks = {  0b1111_1111, 0b0000_0000, 0b1111_1111, 0b0000_0000};
        int[] OPchunks = { 0xff, 0xff, 0xff, 0xff};
        
        BitVector.Builder msbB = new Builder(Integer.SIZE);
        BitVector.Builder lsbB = new Builder(Integer.SIZE);
        BitVector.Builder opB =  new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            msbB.setByte(i, MSBchunks[i]);
            lsbB.setByte(i, LSBchunks[i]);
            opB.setByte(i, OPchunks[i]);
        }
        
        BitVector msb = msbB.build();
        BitVector lsb = lsbB.build();
        BitVector op = opB.build();
        
        return new LcdImageLine(msb, lsb, op);
    }
    
    private LcdImageLine line_1() {
        
        return new LcdImageLine(singleBit1(), singleBit1(), singleBit1());
        
    }
    
    private LcdImageLine lsb0FmsbF0() {
            int[] MSBchunks = { 0xf0, 0, 0, 0};
            int[] LSBchunks = { 0x0f, 0, 0, 0};
            int[] OPchunks =  { 0, 0, 0, 0};
            
            BitVector.Builder msbB = new Builder(Integer.SIZE);
            BitVector.Builder lsbB = new Builder(Integer.SIZE);
            BitVector.Builder opB =  new Builder(Integer.SIZE);
            for(int i = 0; i < Integer.BYTES; i++) {
                msbB.setByte(i, MSBchunks[i]);
                lsbB.setByte(i, LSBchunks[i]);
                opB.setByte(i, OPchunks[i]);
            }
            
            BitVector msb = msbB.build();
            BitVector lsb = lsbB.build();
            BitVector op = opB.build();
            
            return new LcdImageLine(msb, lsb, op);
    }
    
    private LcdImageLine bigColorLine_0() {
        int[] MSBchunks = { 0, 0, 0, 0, 0, 0, 0, 0};
        int[] LSBchunks = { 0, 0, 0, 0, 0, 0, 0, 0};
        int[] OPchunks =  { 0, 0, 0, 0, 0, 0, 0, 0};
        
        BitVector.Builder msbB = new Builder(Integer.SIZE*2);
        BitVector.Builder lsbB = new Builder(Integer.SIZE*2);
        BitVector.Builder opB =  new Builder(Integer.SIZE*2);
        for(int i = 0; i < Integer.BYTES*2; i++) {
            msbB.setByte(i, MSBchunks[i]);
            lsbB.setByte(i, LSBchunks[i]);
            opB.setByte(i, OPchunks[i]);
        }
        
        BitVector msb = msbB.build();
        BitVector lsb = lsbB.build();
        BitVector op = opB.build();
        
        return new LcdImageLine(msb, lsb, op);
    }
    
    private LcdImageLine bigLsb0FmsbF0() {
        int[] MSBchunks = { 0, 0, 0, 0, 0xf0, 0, 0, 0};
        int[] LSBchunks = { 0, 0, 0, 0, 0x0f, 0, 0, 0};
        int[] OPchunks =  { 0, 0, 0, 0, 0, 0, 0, 0};
        
        BitVector.Builder msbB = new Builder(Integer.SIZE*2);
        BitVector.Builder lsbB = new Builder(Integer.SIZE*2);
        BitVector.Builder opB =  new Builder(Integer.SIZE*2);
        for(int i = 0; i < Integer.BYTES*2; i++) {
            msbB.setByte(i, MSBchunks[i]);
            lsbB.setByte(i, LSBchunks[i]);
            opB.setByte(i, OPchunks[i]);
        }
        
        BitVector msb = msbB.build();
        BitVector lsb = lsbB.build();
        BitVector op = opB.build();
        
        return new LcdImageLine(msb, lsb, op);
   }
    
    private BitVector allOnes() {
        int[] chunks = { 0xff, 0xff, 0xff, 0xff};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    private BitVector allZeros() {
        int[] chunks = { 0, 0, 0, 0};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    private BitVector singleBit1() {
        int[] chunks = { 1, 0, 0, 0};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    private BitVector singleBit32() {
        int[] chunks = { 0, 0, 0, 0x80};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    private BitVector singleBit2() {
        int[] chunks = { 0x02, 0, 0, 0};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    private BitVector bit32_ff00() {
        int[] chunks = { 0, 0, 0xff, 0xff };
        
        BitVector.Builder b = new BitVector.Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
        
    }
    
    private BitVector bit32_f0f0() {
        int[] chunks = { 0, 0xff, 0, 0xff };
        
        BitVector.Builder b = new BitVector.Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
        
    }
    
    private BitVector bit64_00f0_0000() {
        int[] chunks = {0, 0, 0, 0, 0, 0x0f, 0, 0 };
        
        BitVector.Builder b = new BitVector.Builder(Integer.SIZE*2);
        for(int i = 0; i < Integer.BYTES * 2; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
        
    }
    
    private BitVector bit64_000f_0000() {
        int[] chunks = {0, 0, 0, 0, 0, 0xf, 0, 0 };
        
        BitVector.Builder b = new BitVector.Builder(Integer.SIZE*2);
        for(int i = 0; i < Integer.BYTES * 2; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
        
    }
    
    
    
    private BitVector bit64_0() {
        int[] chunks = { 0, 0, 0, 0, 0, 0, 0, 0};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES * 2; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    private BitVector bit64_1() {
        int[] chunks = {  0xff, 0xff, 0xff, 0xff,
                          0xff, 0xff, 0xff, 0xff};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES * 2; i++) {
            b.setByte(i, chunks[i]);
        }
        
        return b.build();
    }
    
    
}
