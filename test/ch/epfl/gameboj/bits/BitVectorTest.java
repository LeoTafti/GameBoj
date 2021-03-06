/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import ch.epfl.gameboj.bits.BitVector.Builder;

public class BitVectorTest {
    
    private static String ZERO_32 = "00000000000000000000000000000000";
    private static String ONE_32 = "11111111111111111111111111111111";
    
    @Test
    public void ExtractTest0() {
        int[] chunks = { 0b1010_1010, 0b0000_0000,  0b1111_1111, 0b00000000};
        
        BitVector.Builder b = new Builder(Integer.SIZE);
        for(int i = 0; i < Integer.BYTES; i++) {
            b.setByte(i, chunks[i]);
        }
        
        BitVector v = b.build();        
        assertEquals("00000000111111110000000010101010", v.extractWrapped(0, 32).toString());
    }
    
    @Test
    public void ExtractWrappedTest() {
        int[] chunks = {0b1111_1111, 0b1111_1111, 0b1111_1111, 0b1111_1111,
                            0x01, 0, 0 , 0,
                            0, 0, 0, 0x80 };
        
        BitVector.Builder b = new Builder(Integer.SIZE*3);
        for(int i = 0; i < Integer.BYTES*3; i++) {
            b.setByte(i, chunks[i]);
        }
        BitVector v = b.build();        
        
        assertEquals(ONE_32, v.extractWrapped(0, 32).toString());
        assertEquals("00000000000000000000000000000001", v.extractWrapped(32, 32).toString());
        assertEquals(ONE_32, v.extractWrapped(-1, 32).toString());
        assertEquals(ONE_32, v.extractWrapped(95, 32).toString());
        assertEquals("00000000000000000000000000000011", v.extractWrapped(31, 32).toString());
    }
    
    @Test
    public void ExtractZEROTest() {
        int[] chunks = {0b1111_1111, 0b1111_1111, 0b1111_1111, 0b1111_1111,
                            0x01, 0, 0 , 0,
                            0, 0, 0, 0x80 };
        
        BitVector.Builder b = new Builder(Integer.SIZE*3);
        for(int i = 0; i < Integer.BYTES*3; i++) {
            b.setByte(i, chunks[i]);
        }
        BitVector v = b.build();
        
        assertEquals(ONE_32, v.extractZeroExtended(0, 32).toString());
        assertEquals("00000000000000000000000000000001", v.extractZeroExtended(32, 32).toString());
        assertEquals("11111111111111111111111111111110", v.extractZeroExtended(-1, 32).toString());
        assertEquals("00000000000000000000000000000001", v.extractZeroExtended(95, 32).toString());
        assertEquals(ZERO_32, v.extractZeroExtended(97, 32).toString());
    }
    
    
    
    /*------------Constructor Tests------------*/
    @Test
    public void constructorsWorkProperly() {
        BitVector v0_1 = new BitVector(32);
        BitVector v0_2 = new BitVector(32, false);
        BitVector v1_1 = new BitVector(32, true);
        BitVector v1_2 = new BitVector(64, true);
        
        assertEquals(ZERO_32, v0_1.toString());
        assertEquals(ZERO_32, v0_2.toString());
        assertEquals(ONE_32, v1_1.toString());
        assertEquals(ONE_32 + ONE_32, v1_2.toString());
    }
    
    @Test
    public void constructorFailsOnInvalidSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector(16));
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector(0, true));
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector(65, false));
    }
    
    /*--------------Size test--------------*/
    @Test
    public void sizeReturnsBitNumber() {
        assertEquals(32, new BitVector(32).size());
        assertEquals(64, new BitVector(64, true).size());
    }
    
    /*-------------And/Or/Shift tests------------*/
    @Test
    public void andWorksProperly() {
        BitVector a = new BitVector(32, true);
        BitVector b = new BitVector(32, false);
        assertEquals(ZERO_32, a.and(b).toString());
    }
    
    @Test
    public void orWorksProperly() {
        BitVector a = new BitVector(32, true);
        BitVector b = new BitVector(32, false);
        assertEquals(ONE_32, a.or(b).toString());
    }
    
    @Test
    public void easyShiftWorksProperly() {
        BitVector a = new BitVector(64, true);
        a = a.shift(32); //0xFFFFFFFF_00000000
        assertEquals(64, a.size());
        assertEquals(ONE_32 + ZERO_32, a.toString());
        
        a = a.shift(-32);
        assertEquals(ZERO_32 + ONE_32, a.toString());
        
        BitVector b = new BitVector(64, true);
        b = b.shift(-32);
        
        assertEquals(ZERO_32 + ONE_32, b.toString());
        
        BitVector c = new BitVector(96, true);
        c = c.shift(32);    //1_1_0
        c = c.shift(-64);   //0_0_1
        c = c.shift(32);    //0_1_0
        
        assertEquals(ZERO_32 + ONE_32 + ZERO_32, c.toString());
    }
    
    @Test
    public void harderShiftWorksProperly() {
        BitVector a = new BitVector.Builder(64)
                .setByte(7, 0x80)
                .build();
        a = a.shift(-63);
        assertEquals(ZERO_32 + "00000000000000000000000000000001", a.toString());
        
        BitVector b = new BitVector.Builder(64)
                .setByte(4, 1)
                .build();
        b = b.shift(1);
        assertEquals("00000000000000000000000000000010" + ZERO_32, b.toString());
        
        BitVector c = new BitVector.Builder(32)
                .setByte(0, 0b1111_0000)
                .setByte(1, 0b0000_1111)
                .build();
        c = c.shift(4);
        assertEquals("00000000000000001111111100000000", c.toString());
    }
    
    @Test
    public void andOrShiftWorkTogether() {
        BitVector a = new BitVector(64, true);
        a = a.shift(32); //0xFFFFFFFF_00000000
        
        BitVector b = new BitVector(64, true);
        b = b.shift(-16); //0x0000FFFF_FFFFFFFF
        assertEquals("00000000000000001111111111111111" + ONE_32, b.toString());
        
        BitVector c = a.and(b); //0x0000FFFF_00000000
        assertEquals("00000000000000001111111111111111" + ZERO_32, c.toString());
        
        BitVector d = new BitVector(64, true)
                .shift(48)
                .shift(-24); //0x000000FF_FF00000000
        
        assertEquals("00000000000000000000000011111111" + ZERO_32, c.and(d).toString());
        assertEquals("00000000000000001111111111111111" + "11111111000000000000000000000000", c.or(d).toString());
        
    }
    
    @Test
    public void andOrFailOnDifferentSizes() {
        BitVector a = new BitVector(32);
        BitVector b = new BitVector(64);
        
        assertThrows(IllegalArgumentException.class,
                () -> a.and(b));
        assertThrows(IllegalArgumentException.class,
                () -> b.and(a));
        
        assertThrows(IllegalArgumentException.class,
                () -> a.or(b));
        assertThrows(IllegalArgumentException.class,
                () -> b.or(a));
    }
    /*------------TestBit tests--------------*/
    @Test
    public void testBitFailsOnInvalidIndex() {
        BitVector a = new BitVector(32);
        BitVector b = new BitVector(64, true);
        
        assertThrows(IllegalArgumentException.class,
                () -> a.testBit(32));
        assertThrows(IllegalArgumentException.class,
                () -> b.testBit(64));
        assertThrows(IllegalArgumentException.class,
                () -> a.testBit(-1));
        assertThrows(IllegalArgumentException.class,
                () -> b.testBit(-10));
    }
    
    @Test
    public void testBitWorksProperly() {
        BitVector a = new BitVector.Builder(64)
                .setByte(4, 0b0001_0000)
                .build();
        assertEquals(true, a.testBit(36));
        assertEquals(false, a.testBit(35));
        
        BitVector b = new BitVector.Builder(64)
                .setByte(0, 0b0000_00001)
                .setByte(7, 0b1000_0000)
                .build();
        assertEquals(true, b.testBit(0));
        assertEquals(true, b.testBit(63));
        for(int i = 1; i<63; i++) {
            assertEquals(false, b.testBit(i));
        }
    }
    
    /*------------Builder Tests--------------*/
    @Test
    public void builderWorksCorrectly() {
        BitVector v = new BitVector.Builder(32)
                .setByte(0, 0b1111_0000)
                .setByte(1, 0b1010_1010)
                .setByte(3, 0b1100_1100)
                .build();
        assertEquals("11001100000000001010101011110000", v.toString());
    }
    
    @Test
    public void builderWorksCorrectly2() {
        BitVector v = new BitVector.Builder(32)
                .setByte(0, 0b0000_0000)
                .setByte(1, 0b0000_1010)
                .setByte(3, 0b0000_0000)
                .build();
        assertEquals("00000000000000000000101000000000", v.toString());
    }
    
    @Test
    public void builderWorksCorrectlyForMultipleIntSize() {
        BitVector v = new BitVector.Builder(Integer.SIZE*3)
                .setByte(0, 0)
                .setByte(4, 0b1010_1010)
                .setByte(8, 0b1100_1100)
                .build();
        
        assertEquals("00000000000000000000000011001100"
                +"00000000000000000000000010101010"
                +"00000000000000000000000000000000", v.toString());
    }
    
    @Test
    public void builderThrowsExceptionOnceBuilt() {
        BitVector.Builder bvb = new BitVector.Builder(32);
        bvb.build();
        assertThrows(IllegalStateException.class,
                () -> bvb.build());
        assertThrows(IllegalStateException.class,
                () -> bvb.setByte(0, 0));
    }
    
    @Test
    public void builderFailsOnInvalidSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector.Builder(16));
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector.Builder(0));
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector.Builder(65));
    }
    
    @Test
    public void builderSetByteFailsOnInvalidIndex() {
        BitVector.Builder bvb = new BitVector.Builder(64);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bvb.setByte(8, 0));
        assertThrows(IndexOutOfBoundsException.class,
                () -> bvb.setByte(-1, 0));
        assertThrows(IndexOutOfBoundsException.class,
                () -> bvb.setByte(10, 0));
    }
}
