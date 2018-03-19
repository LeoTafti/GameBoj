/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.gameboj.component.cpu.Opcode;

public class GameBoyTest {

    GameBoy g;
    
    @Before
    public void init() {
        g = new GameBoy(null);
    }
    
    @Test
    public void FibonacciRun() {
        byte[] fibProg = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
              }; 
        
        for(int i = 0; i < fibProg.length; ++i ) {
            g.bus().write(i, Byte.toUnsignedInt(fibProg[i]));
        }
        
        g.runUntil(100);
        
        assertArrayEquals(new int[] {56, 65486, 89, 0, 0, 0, 0, 0, 0, 0},
                g.cpu()._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void hundredLoop() {
        byte[] prog = new byte[] {
                (byte)Opcode.LD_B_N8.encoding,
                (byte)0x64,
                (byte)Opcode.ADD_A_N8.encoding,
                (byte)0x01,
                (byte)Opcode.CP_A_B.encoding,
                (byte)Opcode.JR_Z_E8.encoding,
                (byte)0xFC,
                (byte)Opcode.LD_C_B.encoding
        };
        
        for(int i = 0; i < prog.length; ++i ) {
            g.bus().write(i, Byte.toUnsignedInt(prog[i]));
        }
        
        g.runUntil(500);
        
        assertArrayEquals(new int[] {56, 65286, 100, 0x80, 100, 100, 0, 0, 0, 0},
                g.cpu()._testGetPcSpAFBCDEHL());
    }
    
}
