package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest {
    private Bus bus;
    private Cpu cpu;
    
    @BeforeEach
    public void initialize() {
        /*Create a new Cpu and a new Ram of 20 bytes
         * Creates a simple RamController for given Ram and attaches Cpu and
         * RamController to Bus.
         */
        
        cpu = new Cpu();
        bus = new Bus();
        Ram ram = new Ram(20);
        RamController ramController = new RamController(ram, 0);
        cpu.attachTo(bus);
        ramController.attachTo(bus);
    }

    private void cycleCpu(long cycles) {
        for(int i = 0; i<cycles; i++) {
            cpu.cycle(i);
        }
    }
    
    
    @Test
    public void LD_HL_N16_isCorrectlyExecuted() {
        //Write 0x14 in HL
        bus.write(0, Opcode.LD_HL_N16.encoding); //LD_HL_
        bus.write(1, 0x4);
        bus.write(2, 0x1);
       
        cycleCpu(Opcode.LD_HL_N16.cycles);

        assertArrayEquals(new int[] {3, 0, 0, 0, 0, 0, 0, 0, 1, 4}, cpu._testGetPcSpAFBCDEHL());
    }

}
