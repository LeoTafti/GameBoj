/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;

public class LcdImageLineTest {

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
    
}
