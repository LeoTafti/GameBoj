/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;

public class BitVectorTest {
    
    private static int[] ALL_MASKS = new int[] {
            0b00000000000000000000000000000001,
            0b00000000000000000000000000000010,
            0b00000000000000000000000000000100,
            0b00000000000000000000000000001000,
            0b00000000000000000000000000010000,
            0b00000000000000000000000000100000,
            0b00000000000000000000000001000000,
            0b00000000000000000000000010000000,
            0b00000000000000000000000100000000,
            0b00000000000000000000001000000000,
            0b00000000000000000000010000000000,
            0b00000000000000000000100000000000,
            0b00000000000000000001000000000000,
            0b00000000000000000010000000000000,
            0b00000000000000000100000000000000,
            0b00000000000000001000000000000000,
            0b00000000000000010000000000000000,
            0b00000000000000100000000000000000,
            0b00000000000001000000000000000000,
            0b00000000000010000000000000000000,
            0b00000000000100000000000000000000,
            0b00000000001000000000000000000000,
            0b00000000010000000000000000000000,
            0b00000000100000000000000000000000,
            0b00000001000000000000000000000000,
            0b00000010000000000000000000000000,
            0b00000100000000000000000000000000,
            0b00001000000000000000000000000000,
            0b00010000000000000000000000000000,
            0b00100000000000000000000000000000,
            0b01000000000000000000000000000000,
            0b10000000000000000000000000000000,
    };
    
    private static BitVector[] REF_VECTORS = {
        new BitVector(32, false),  new BitVector(32, true),
        
    };
    
    private static enum REF_VECTOR { empty32, full32, msb32, lsb32 };
    
    @Test
    public void ExtractTest() {
        byte[] chunks = {(byte) 0b1111_1111,(byte) 0b1111_1111, (byte) 0b1111_1111, (byte) 0b1111_1111,
                            (byte) 0x01, 0, 0 , 0,
                            0, 0, 0, (byte)0x80 };
        
        BitVector.Builder b = new Builder(Integer.SIZE*3);
        for(int i = 0; i < Integer.BYTES*3; i++) {
            b.setByte(Byte.SIZE*i, chunks[i]);
        }
        BitVector v = b.build();
        System.out.println(v.toString());
        System.out.println(v.extractWrapped(0, 32));
        
        assertEquals(REF_VECTORS[REF_VECTOR.full32.ordinal()], v.extractWrapped(0, 32));
        assertEquals(0x1, v.extractWrapped(32, 32));
        assertEquals(0xffffffff, v.extractWrapped(-1, 32));
        assertEquals(0xffffffff, v.extractWrapped(95, 32));
        assertEquals(0x3, v.extractWrapped(31, 32));
    }
}
