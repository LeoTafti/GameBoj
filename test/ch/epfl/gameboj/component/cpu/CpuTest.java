package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest {
    Bus bus;
    Cpu cpu;
    
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
        bus.attach(cpu);
        bus.attach(ramController);
    }
    
    @AfterEach
    public void simulateCycle() {
        //
    }
    
    
    @Test
    public void LD_R8_HLR_areCorrectlyExecuted() {
        bus.write(0, 0x46);
        System.out.println(cpu._testGetPcSpAFBCDEHL());
    }

}
