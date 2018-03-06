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
        cpu.attachTo(bus);
        bus.attach(ramController);
    }
    
    @AfterEach
    public void simulateCycle() {
        //
    }
    
    
    @Test
    public void LD_R8_HLR_areCorrectlyExecuted() {
        //Write 10 in HL
        bus.write(0, 0x21); //LD_HL_
        bus.write(11, 10); //10
        
//        bus.write(0, 0x46);
        cpu.cycle(0);
//        cpu.cycle(1);
//        cpu.cycle(2);
//        cpu.cycle(3);
        //But HL = 0 so it will put the first opcode into reg B...
        for(int value : cpu._testGetPcSpAFBCDEHL()) {
            System.out.println(value);
        }
    }

}
