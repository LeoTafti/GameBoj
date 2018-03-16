/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import org.junit.Test;

public class gameBoyTest {

    GameBoy g = new GameBoy(null);
    
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
    }
    
}
