/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Opcode;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoyTest {

    Bus bus;
    Cpu cpu;
    
    @Before
    public void init() {
        cpu = new Cpu();
        bus = new Bus();
        Ram ram = new Ram(30);
        RamController ramController = new RamController(ram, 0);
        cpu.attachTo(bus);
        ramController.attachTo(bus);
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
        
        for(int i = 0; i<fibProg.length; i++) {
            bus.write(i, Byte.toUnsignedInt(fibProg[i]));
        }
        
        int i = 0;
        while(cpu._testGetPcSpAFBCDEHL()[0] != 8) {
            cpu.cycle(i);
            i++;
        }
        
        assertArrayEquals(new int[] {8, 0xFFFF, 89, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void hundredLoop() {
        byte[] prog = new byte[] {
                (byte)Opcode.LD_SP_N16.encoding, (byte)0xFF, (byte)0xFF,
                (byte)Opcode.LD_B_N8.encoding,
                (byte)100,
                (byte)Opcode.ADD_A_N8.encoding,
                (byte)1,
                (byte)Opcode.CP_A_B.encoding,
                (byte)Opcode.JR_NZ_E8.encoding,
                (byte)0xFB,
                (byte)Opcode.LD_C_B.encoding
        };
        
        
        for(int i = 0; i<prog.length; i++) {
            bus.write(i, Byte.toUnsignedInt(prog[i]));
        }
        
        int i = 0;
        while(cpu._testGetPcSpAFBCDEHL()[5] != 100) {
            cpu.cycle(i);
            System.out.println(cpu._testGetPcSpAFBCDEHL()[0]);
            i++;
        }
        
        assertArrayEquals(new int[] {11, 0xFFFF, 100, 0xC0, 100, 100, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
}
