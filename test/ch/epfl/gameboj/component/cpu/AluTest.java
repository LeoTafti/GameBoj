/*
* Author : Paul Juillard (288519)
* Date: 19/02
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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
        assertEquals(0xB0, Alu.add(0xFF, 0x1, f)); //ripple threw Z0HC
        
        assertEquals(0x100, Alu.add16L(0, 1)); // 0+1 and 0000
        assertEquals(0x1020, Alu.add16L(8, 8)); //H
        assertEquals(0x100000, Alu.add16L(0x800,  0x800)); //high H doesnt trigger flag
        assertEquals(0x10010, Alu.add16L(0x80, 0x80)); //C
        assertEquals(0x00, Alu.add16L(0X8000, 0X8000)); // high C doesnt trigger flag
        assertEquals(0x30, Alu.add16L(0xFFFF, 0x1)); //ripple threw 00HC
        
        assertEquals(0x100, Alu.add16H(0, 1)); // 0+1 and 0000
        assertEquals(0x1000, Alu.add16H(8, 8)); //low H doesnt trigger flag
        assertEquals(0x100020, Alu.add16H(0x800,  0x800)); //H
        assertEquals(0x10000, Alu.add16H(0x80, 0x80)); //low C doesnt trigger flag
<<<<<<< HEAD
        assertEquals(0x10, Alu.add16H(0X8000, 0X8000)); // C
=======
        //assertEquals(0x90, Alu.add16H(0X8000, 0X8000)); // C
        //assertEquals(0xB0, Alu.add16H(0xFFFF, 0x1)); //ripple threw Z0HC
>>>>>>> branch 'master' of https://gitlab.com/Pjuillard/GameBoj.git
        assertEquals(0x30, Alu.add16H(0xFFFF, 0x1)); //ripple threw 00HC
    
        
    }
    
    //PLEASE PUSH
    
}
