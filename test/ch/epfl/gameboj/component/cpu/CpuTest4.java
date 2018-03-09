/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.BeforeEach;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest4 {
    private Bus bus;
    private Cpu cpu;
    
    private final int RAM_SIZE = 0xFFFF ;
    
    @BeforeEach
    public void initialize() {
        /*Create a new Cpu and a new Ram of 20 bytes
         * Creates a simple RamController for given Ram and attaches Cpu and
         * RamController to Bus.
         */
        
        cpu = new Cpu();
        bus = new Bus();
        Ram ram = new Ram(RAM_SIZE);
        RamController ramController = new RamController(ram, 0);
        cpu.attachTo(bus);
        ramController.attachTo(bus);
    }
    
    private void cycleCpu(long cycles) {
        for(int i = 0; i<cycles; i++) {
            cpu.cycle(i);
        }
    }
    
    /**
     * writes all bytes at addresses starting at startAddress
     * @param startAdress
     * @param bytes
     */
    private void writeAllBytes(int startAdress, int ... bytes) {
        Preconditions.checkBits16(startAdress);
        if(bytes.length > RAM_SIZE)
            throw new IllegalArgumentException("not enough ram for that");
        for(int i = 0; i<bytes.length; i++) {
            int instr = bytes[i];
            Preconditions.checkBits8(instr);
            bus.write(startAdress+i, instr);
        }
    }
    
    /**
     * writes all bytes at adresses STARTING AT 0
     * @param bytes
     */
    private void writeAllBytes(int ... bytes) {
        if(bytes.length > RAM_SIZE)
            throw new IllegalArgumentException("not enough ram for that");
        for(int i = 0; i<bytes.length; i++) {
            int instr = bytes[i];
            Preconditions.checkBits8(instr);
            bus.write(i, instr);
        }
    }
    
    /**
     * writes all opcodes and cycles 
     * OPCODES ONLY
     * @param opcodes Opcode list
     * @return number of cycles
     */
    private int execute(Opcode ... opcodes) {
        int cyclesum = 0;
        for(int i = 0; i<opcodes.length; i++) {
            Opcode op = opcodes[i];
            bus.write(i, op.encoding);
            cyclesum += op.cycles;
        }
        cycleCpu(cyclesum);
        return cyclesum;
    }
    
    private void initiateRegs(int a, int f, int b, int c, int d, int e, int h, int l) {
        cpu.setAllRegs(a, f, b, c, d, e, h, l);
    }
    
    private void initiateRegs16(int af, int bc, int de, int hl) {
        cpu.setAllRegs16(af, bc, de, hl);
    }
}
