/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public class RegisterFileTest {

    private enum Reg implements Register { A, F, B, C, D, E, H, L }
    
    private enum AllBits implements Bit {
        B00, B01, B02, B03, B04, B05, B06, B07,
        B08, B09, B10, B11, B12, B13, B14, B15,
        B16, B17, B18, B19, B20, B21, B22, B23,
        B24, B25, B26, B27, B28, B29, B30, B31,
    };
    
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
    
    public RegisterFileTest() {}
    
    
    @Test
    public void testBitWorks() {
        registerFile.set(Reg.A, 4);
        assertEquals(true , registerFile.testBit(Reg.A, AllBits.B02));
        assertEquals(false, registerFile.testBit(Reg.A, AllBits.B00));
    }
    
    @Test
    public void setBitWorks() {
        registerFile.set(Reg.A, 4);
        
        registerFile.setBit(Reg.A, AllBits.B00, true);
        assertEquals(5, registerFile.get(Reg.A));
        
        registerFile.setBit(Reg.A, AllBits.B00, false);
        assertEquals(4, registerFile.get(Reg.A));
    }
}
