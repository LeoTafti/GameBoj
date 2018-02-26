/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.component.cpu.Alu.RotDir;

class AluTest {

    boolean t = true;
    boolean f = false;

    @Test
    void MaskWorksForKnownValues() {
        
        assertEquals(0, Alu.maskZNHC(f, f, f, f));
        assertEquals(0xF0, Alu.maskZNHC(t,  t,  t, t));
        assertEquals(0x10, Alu.maskZNHC(f,  f,  f, t));
        assertEquals(0x20, Alu.maskZNHC(f, f, t, f));
        assertEquals(0x40, Alu.maskZNHC(f, t, f, f));
        assertEquals(0x80, Alu.maskZNHC(t, f, f, f));
    }
    
//    @Test
//    void packValueWorksOnKnownValues() {
//        
//        assertEquals(0, Alu.packValueZNHC(0, f, f, f, f)); //0
//        assertEquals(0x100, Alu.packValueZNHC(1, f, f, f, f)); //1
//        assertEquals(0xFF00, Alu.packValueZNHC(0xFF, f, f, f, f)); //max int value
//        assertEquals(0xFFF0, Alu.packValueZNHC(0xFF, t, t, t, t)); //max pack value
//    }
//    
//    @Test
//    void packValueWorksFailsOnInvalidInt() {
//        assertThrows(IllegalArgumentException.class, () -> Alu.packValueZNHC(-1, f, f, f, f)); //negative
//        assertThrows(IllegalArgumentException.class, () -> Alu.packValueZNHC(0x10000, f, f, f, f)); //17 bit
//        
//    }
    
    @Test
    void unpackValueWorksOnKnownValues() {
        assertEquals(0, Alu.unpackValue(0xF0)); // 0 and ZNHC
        assertEquals(1, Alu.unpackValue(0x1F0)); //1 and ZNHC
        assertEquals(1, Alu.unpackValue(0x100)); //1 and 0000
        assertEquals(1, Alu.unpackValue(0x140)); //1 and 0N00
        assertEquals(0xFFFF, Alu.unpackValue(0xFFFFF0)); //max int and ZNHC
    }
    
    @Test
    void unpackFlagsWorksOnKnownValue() {
        assertEquals(0xF, Alu.unpackFlags(0xF0)); // 0 and ZNHC
        assertEquals(0xF, Alu.unpackFlags(0x1F0)); //1 and ZNHC
        assertEquals(0, Alu.unpackFlags(0x100)); //1 and 0000
        assertEquals(0x8, Alu.unpackFlags(0x180)); //1 and Z000
        assertEquals(0x4, Alu.unpackFlags(0x140)); //1 and 0N00
        assertEquals(0x2, Alu.unpackFlags(0x120)); //1 and 00H0
        assertEquals(0x1, Alu.unpackFlags(0x110)); //1 and 000C
        assertEquals(0xF, Alu.unpackFlags(0xFFFFF0)); //max int and ZNHC
    }
    
    // not required (TA)
//    @Test
//    void unpackFailsOnInvalidInt() {
//        assertThrows(IndexOutOfBoundsException.class, 
//                () -> Alu.unpackValue(0x1)); //not formatted
//        assertThrows(IndexOutOfBoundsException.class, 
//                () -> Alu.unpackValue(0x1000000)); //17bit int and 0000
//        assertThrows(IndexOutOfBoundsException.class, 
//                () -> Alu.unpackFlags(0x1000000)); //17bit int and 0000
//    
//    }
    
    @Test
    void addWorksOnKnownValues() {
        assertEquals(0x080, Alu.add(0, 0, f));
        assertEquals(0x100, Alu.add(0, 1, f));
        assertEquals(0x100, Alu.add(0, 0, t)); //arg carry
        assertEquals(0x1020, Alu.add(8, 8, f)); //H
        assertEquals(0x90, Alu.add(0x80, 0x80, f)); //C
        assertEquals(0xB0, Alu.add(0xFF, 0x1, f)); //ripple through Z0HC
        
        assertEquals(0x100, Alu.add16L(0, 1)); // 0+1 and 0000
        assertEquals(0x1020, Alu.add16L(8, 8)); //H
        assertEquals(0x100000, Alu.add16L(0x800,  0x800)); //high H doesnt trigger flag
        assertEquals(0x10010, Alu.add16L(0x80, 0x80)); //C
        assertEquals(0x00, Alu.add16L(0X8000, 0X8000)); // high C doesnt trigger flag
        assertEquals(0x30, Alu.add16L(0xFFFF, 0x1)); //ripple through 00HC
        assertEquals(0x120030, Alu.add16L(0x11FF, 0x0001));
        
        assertEquals(0x100, Alu.add16H(0, 1)); // 0+1 and 0000
        assertEquals(0x1000, Alu.add16H(8, 8)); //low H doesnt trigger flag
        assertEquals(0x100020, Alu.add16H(0x800,  0x800)); //H
        assertEquals(0x10000, Alu.add16H(0x80, 0x80)); //low C doesnt trigger flag
        assertEquals(0x10, Alu.add16H(0X8000, 0X8000)); // C
        assertEquals(0x000030, Alu.add16H(0xFFFF, 0x1)); //ripple through 00HC
        assertEquals(0x100020 , Alu.add16H(0x0FFF, 0x0001));
        assertEquals(0x120000, Alu.add16H(0x11FF, 0x0001));
        
    }
    
    @Test
    void andWorksOnKnownValues() {
        assertEquals(0xA0, Alu.and(0, 0)); // 0
        assertEquals(0xA0, Alu.and(0xFF, 0)); // 0
        assertEquals(0x120, Alu.and(0xFF, 1)); //1
        assertEquals(0x820, Alu.and(0xFF, 0x8)); // H
        assertEquals(0xFF20, Alu.and(0xFF, 0xFF)); //full int
    }
    
    @Test
    void andFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, 
              () -> Alu.and(384, 0)); //9bits 1
        assertThrows(IllegalArgumentException.class, 
                () -> Alu.and(0, 384)); //9bits 2
        assertThrows(IllegalArgumentException.class, 
                () -> Alu.and(-1, 0)); //negative
    }
    
    @Test
    void orWorksOnKnownValues() {
        assertEquals(0x80, Alu.or(0, 0)); // 0 & Z
        assertEquals(0xFF00, Alu.or(0xFF, 0)); //
        assertEquals(0xFF00, Alu.or(0xF0, 0xF)); //1
        assertEquals(0xFF00, Alu.or(0xFF, 0xFF)); //all 1
    }
    
    @Test
    void orFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, 
                () -> Alu.or(384, 0)); //9bits
        assertThrows(IllegalArgumentException.class, 
                () -> Alu.or(0, 384)); //9bits
          assertThrows(IllegalArgumentException.class, 
                  () -> Alu.or(-1, 0)); //negative
    }
    
    @Test
    void xorWorksOnKnownValues() {
        assertEquals(0x80, Alu.xor(0, 0)); // 0
        assertEquals(0x80, Alu.xor(1, 1)); // 1&1 = 0
        assertEquals(0x80, Alu.xor(0xFF, 0xFF)); // 0
        assertEquals(0xFF00, Alu.xor(0xFF, 0));
        assertEquals(0xFF00, Alu.xor(0xF0, 0xF));
        assertEquals(0x80, Alu.xor(0xF, 0xF));
    }
    
    @Test
    void xorFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, 
                () -> Alu.xor(384, 0)); //9bits
        assertThrows(IllegalArgumentException.class, 
                () -> Alu.xor(0, 384)); //9bits
          assertThrows(IllegalArgumentException.class, 
                  () -> Alu.xor(-1, 0)); //negative
    }
    
    @Test
    void shiftWorksOnKnownValues() {
        assertEquals(0x80, Alu.shiftLeft(0)); //0 Z000
        assertEquals(0x200, Alu.shiftLeft(1));
        assertEquals(0x90, Alu.shiftLeft(0x80)); //last bit shift Z00C
        assertEquals(0xFE10, Alu.shiftLeft(0xFF)); //full int and C
        assertEquals(0x1000, Alu.shiftLeft(0x8));
        
        assertEquals(0x80, Alu.shiftRightA(0)); //0
        assertEquals(0x90, Alu.shiftRightA(1)); //0 Z00C
        assertEquals(0xC000, Alu.shiftRightA(0x80)); //arithmetic shift 1100...
        assertEquals(0xFF10, Alu.shiftRightA(0xFF)); //full int and C
        assertEquals(0x400, Alu.shiftRightA(0x8));
        
        assertEquals(0x80, Alu.shiftRightL(0)); //0
        assertEquals(0x90, Alu.shiftRightL(1)); //0 Z00C
        assertEquals(0x4000, Alu.shiftRightL(0x80)); //non-arithmetic shift 0100...
        assertEquals(0x7F10, Alu.shiftRightL(0xFF)); //full int and C
        assertEquals(0x400, Alu.shiftRightL(0x8));
    }
    
    @Test
    void shiftFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftLeft(384)); // 9bits
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(-1)); // negative

        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightA(384)); // 9bits
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightA(-1)); // negative
        
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightL(384)); // 9bits
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightL(-1)); // negative
        
    }
    
    @Test
    void rotateWorksOnKnownValues() { 
        assertEquals(0x80, Alu.rotate(RotDir.LEFT, 0)); // L 0 Z000
        assertEquals(0x80, Alu.rotate(RotDir.RIGHT, 0)); // R 0 Z000
        assertEquals(0x200, Alu.rotate(RotDir.LEFT, 1)); // L 1
        assertEquals(0x8010, Alu.rotate(RotDir.RIGHT, 1)); // R 1 Z00C
        assertEquals(0x110, Alu.rotate(RotDir.LEFT, 0x80)); // L most-sign bit Z00C
        assertEquals(0xFF10, Alu.rotate(RotDir.LEFT, 0xFF)); //L full int 000C
        assertEquals(0xFF10, Alu.rotate(RotDir.RIGHT, 0xFF)); // R full int 000C
        
        assertEquals(0x80, Alu.rotate(RotDir.LEFT, 0, f)); // L 0 f Z000
        assertEquals(0x100, Alu.rotate(RotDir.LEFT, 0, t)); // L 0 t 0000
        assertEquals(0x80, Alu.rotate(RotDir.RIGHT, 0, f)); // R 0 f Z000
        //assertEquals(0x8000, Alu.rotate(RotDir.RIGHT, 0, t)); // R 0 t 0000
        assertEquals(0x90, Alu.rotate(RotDir.LEFT, 0x80, f)); // L most-sign bit f Z00C
        assertEquals(0x110, Alu.rotate(RotDir.LEFT, 0x80, t)); // L most_sign bit t 000C
        assertEquals(0x4000, Alu.rotate(RotDir.RIGHT, 0x80, f)); // R most-sign bit f 0000
        assertEquals(0xC000, Alu.rotate(RotDir.RIGHT, 0x80, t)); // R most-sign bit t 0000
        assertEquals(0x200, Alu.rotate(RotDir.LEFT, 1, f)); // L least-sign bit f 0000
        assertEquals(0x300, Alu.rotate(RotDir.LEFT, 1, t)); // L least-sign bit t 0000
        assertEquals(0x90, Alu.rotate(RotDir.RIGHT, 1, f)); // R least-sign bit f Z00C
        assertEquals(0x8010, Alu.rotate(RotDir.RIGHT, 1, t)); // R least-sign bit t 000C
        assertEquals(0xFE10, Alu.rotate(RotDir.LEFT, 0xFF, f)); // L full int f 000C
        assertEquals(0xFF10, Alu.rotate(RotDir.LEFT, 0xFF, t)); // L full int t 000C
        assertEquals(0x7F10, Alu.rotate(RotDir.RIGHT, 0xFF, f)); // R full int f 000C
        assertEquals(0xFF10, Alu.rotate(RotDir.LEFT, 0xFF, t)); // R full int t 000C
    }

    @Test
    void rotateFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, 384)); // 9bits
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, 384));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, 384, f)); 
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, 384, t));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, 384, f)); 
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, 384, t)); 
        
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -1)); // negative
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, -1)); 
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -1, f));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -1, t)); 
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, -1, f)); 
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -1, t)); 
    }
    
    @Test
    void subWorksOnValidInput() {
        assertEquals(0x00C0, Alu.sub(0x0, 0x0));
        assertEquals(0x00C0, Alu.sub(0xFF, 0xFF));
        assertEquals(0x00C0, Alu.sub(0x10, 0x10));
        assertEquals(0x9050, Alu.sub(0x10,  0x80));
        assertEquals(0xFF70, Alu.sub(0x01, 0x01, true));
        assertEquals(0b11000000, Alu.sub(0, 0));
        assertEquals(0xE160, Alu.sub(0xF0, 0x0F));
        assertEquals(0xD040 , Alu.sub(0xE0, 0x10));
        assertEquals(0x00C0, Alu.sub(0x01, 0x00, true));
    }
    
    @Test
    void subFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(-1, 0x00)); //negative int
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(0x100, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(0x100, 0x00)); //9bit int
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(0x00, 0x100));
    }
    
    @Test
    void bcdAdjustWorksOnKnownValues() {
        assertEquals(0x80, Alu.bcdAdjust(0, false, false, false)); // 0
        assertEquals(0x600, Alu.bcdAdjust(0, false, true, false)); // 0 and H -> 6
        assertEquals(0xC0, Alu.bcdAdjust(0x6, true, true, false)); // 6 and NH -> 0 and ZN
        assertEquals(0x6010, Alu.bcdAdjust(0, false, false, true)); // 0 and C -> 60 and C
        assertEquals(0xD0, Alu.bcdAdjust(0x60, true, false, true)); // 60 and NC -> 0 and ZNC
        assertEquals(0x6610, Alu.bcdAdjust(0, false, true, true)); // 0 and HC -> 66 and C
        assertEquals(0x1000, Alu.bcdAdjust(0xA, false, false, false)); //10 and 4LSB > 9 -> 16
       //TODO assertEquals(0x0, Alu.bcdAdjust(0xA, true, false, false)); //10 and 4LSB > 9 but N -> 10
       // assertEquals(0x6000, Alu.bcdAdjust(0xA0, false, false, false)); //100 and 4MSB > 99 -> 160
       // assertEquals(0x6400, Alu.bcdAdjust(0xA0, true, false, false)); //100 and 4MSB > 99 but N -> 100
        assertEquals(0x7300, Alu.bcdAdjust(0x6D, false, false, false)); //given test
        assertEquals(0x0940, Alu.bcdAdjust(0x0F, true, true, false)); //given test
        
    }
    
    @Test
    void bcdAdjustFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(-1, false, false, false)); //negative int
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(0X100, false, false, false)); //9bit int
    }
    
    @Test
    void swapWorksOnValidInput() {
        assertEquals(0x0F00, Alu.swap(0xF0));
        assertEquals(0x0080, Alu.swap(0x00));
    }
    
    @Test
    void swapFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(0x100)); //9bit int
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(-1)); //negative int
    }
    
    @Test
    void testBitWorksOnKnownValues() {
        assertEquals(0b10100000, Alu.testBit(0x80, 7));
        assertEquals(0b00100000, Alu.testBit(0x80, 6));
        
        assertEquals(0b10100000, Alu.testBit(0x01, 0));
        assertEquals(0b00100000, Alu.testBit(0x02, 0));
    }
    
    @Test
    void testBitFailsOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(-1, 0)); //negative int
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(0x100, 8)); //9bit int
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0x80, 8)); //index too big
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0x00, -1)); //negative index
    }
    
}
