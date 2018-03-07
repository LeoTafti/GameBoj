package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

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
            System.out.println(Integer.toBinaryString(instr));
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
    
    private static ArrayList<Opcode> buildOpcodeFamilyTable(Opcode.Family family) {
        
        ArrayList<Opcode> table = new ArrayList<Opcode>();
        for (Opcode o: Opcode.values()) {
            if(o.family == family) {
                table.add(o);
            }
        }
        return table;
    }
    
    private void initiateReg(int regId, int value) {
        Preconditions.checkBits8(value);
        System.out.print("*");
        writeAllBytes((regId<<3) + 0b110, value); //LD r8 n8
        cycleCpu(2);
        
    }
    /**
     * 
     * @param regId reg16 id 
     * @param value 2 bytes in BIG-ENDIAN format
     */
    private void initiateReg16(int regId, int value) {
        Preconditions.checkBits16(value);
        Preconditions.checkArgument(regId<=0b11);
        int msb = Bits.extract(value, 8, 8);
        int lsb = Bits.clip(8, value);
        writeAllBytes((regId<<4) + 0b1, msb , lsb); //LD r8 n8
        cycleCpu(3);
    }
    
    private void initiateRegs(int a, int b, int c, int d, int e, int h, int l) {
        Preconditions.checkBits8(a);
        Preconditions.checkBits8(b);
        Preconditions.checkBits8(c);
        Preconditions.checkBits8(d);
        Preconditions.checkBits8(e);
        Preconditions.checkBits8(h);
        Preconditions.checkBits8(l);
        initiateReg(0b111, a);
        initiateReg(0b000, b);
        initiateReg(0b001, c);
        initiateReg(0b010, d);
        initiateReg(0b011, e);
        initiateReg(0b100, h);
        initiateReg(0b101, l);
    }
    
    private void initiateRegs16 (int bc, int de, int hl, int af) {
        initiateReg16(0b11, af);
        initiateReg16(0b00, bc);
        initiateReg16(0b01, de);
        initiateReg16(0b10, hl);        
    }
    
    //useless opcodes are not encoded this way
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
    public void LD_R8_N8_isCorrectlyExecuted() {
        writeAllBytes(Opcode.LD_A_N8.encoding,
                0x1);
        cycleCpu(Opcode.LD_A_N8.cycles);
        
        assertArrayEquals(new int[] {2, 0, 1, 0, 0, 0, 0, 0, 0, 0}, cpu._testGetPcSpAFBCDEHL());
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
    
    @Test
    public void LD_R8_R8_isCorrectlyExecuted() {
        
        initiateRegs(1, 1, 1, 0, 0xF, 0, 0);
             
        writeAllBytes(Opcode.LD_A_B.encoding,
                      Opcode.LD_C_D.encoding,
                      Opcode.LD_E_H.encoding);
        
        cycleCpu(Opcode.LD_A_B.cycles+
                 Opcode.LD_C_D.cycles+
                 Opcode.LD_E_H.cycles);
        int[] regVals = cpu._testGetPcSpAFBCDEHL();
        System.out.println("testGet:");
        for (int i = 0; i<regVals.length; ++i) {
        System.out.println(regVals[i]);
        }   
        assertArrayEquals(new int[] {3, 0, 0, 0, 0, 1, 1, 0XFF, 0XFF, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        
    }

}
