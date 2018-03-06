package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import ch.epfl.gameboj.component.cpu.Opcode;

class CpuTest {
    private Bus bus;
    private Cpu cpu;
    
    private final int RAM_SIZE = 20;
    
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
    
    private void writeAllBytes(int ... bytes) {
        if(bytes.length > RAM_SIZE)
            throw new IllegalArgumentException("not enough ram for that");
        for(int i = 0; i<bytes.length; i++) {
            int instr = bytes[i];
            Preconditions.checkBits8(instr);
            bus.write(i, instr);
        }
    }
     
    private Opcode getOpcode(int encoding) {
        Preconditions.checkBits8(encoding);
        for(Opcode a: Opcode.values()) {
            if(encoding == a.encoding) return a;
        }
        return Opcode.NOP; 
    }
    
    @Test
    public void AllOpcodesRun() {
        
        assertAll( () -> {
        // LD opcodes
        for(int i = Opcode.LD_B_B.encoding; i<=Opcode.LD_HLR_A.encoding; ++i) {
            Opcode op = getOpcode(i);
            switch (op.cycles) {
            case 1 : writeAllBytes(i);
            case 2 : writeAllBytes(i, 0);
            case 3 : writeAllBytes(i, 0, 0);
            }
            cycleCpu(op.cycles);     
        }
        for(int i = Opcode.LD_SP_HL.encoding; i<=Opcode.LD_CR_A.encoding; ++i) {
            Opcode op = getOpcode(i);
            switch (op.cycles) {
            case 1 : writeAllBytes(i);
            case 2 : writeAllBytes(i, 0);
            case 3 : writeAllBytes(i, 0, 0);
            }
            cycleCpu(op.cycles);         
        }
        for(int i = Opcode.LD_A_N8R.encoding; i<=Opcode.LD_N8R_A.encoding; ++i) {
            Opcode op = getOpcode(i);
            switch (op.cycles) {
            case 1 : writeAllBytes(i);
            case 2 : writeAllBytes(i, 0);
            case 3 : writeAllBytes(i, 0, 0);
            }
            cycleCpu(op.cycles);         
        }
        for(int i = Opcode.LD_A_N16R.encoding; i<=Opcode.LD_N16R_SP.encoding; ++i) {
            Opcode op = getOpcode(i);
            switch (op.cycles) {
            case 1 : writeAllBytes(i);
            case 2 : writeAllBytes(i, 0);
            case 3 : writeAllBytes(i, 0, 0);
            }
            cycleCpu(op.cycles);     
        }
        //POP and PUSH
        for(int i = Opcode.PUSH_BC.encoding; i<=Opcode.POP_AF.encoding; ++i) {
            Opcode op = getOpcode(i);
            switch (op.cycles) {
            case 1 : writeAllBytes(i);
            case 2 : writeAllBytes(i, 0);
            case 3 : writeAllBytes(i, 0, 0);
            }
            cycleCpu(op.cycles);         
        }
        });
        
    }
    
    @Test
    public void LD_HL_N16_isCorrectlyExecuted() {
        //Write 0x14 in HL
//        bus.write(0, Opcode.LD_HL_N16.encoding); //LD_HL_
//        bus.write(1, 0x4);
//        bus.write(2, 0x1);
//       
        writeAllBytes(Opcode.LD_HL_N16.encoding,
                0x4,
                0x1);
        
        cycleCpu(Opcode.LD_HL_N16.cycles);

        assertArrayEquals(new int[] {3, 0, 0, 0, 0, 0, 0, 0, 1, 4}, cpu._testGetPcSpAFBCDEHL());
    }

}
