package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
import ch.epfl.gameboj.component.cpu.Opcode.Family;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;

class CpuTest {
    private Bus bus;
    private Cpu cpu;
    
    private final int RAM_SIZE = 20 ;
    
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
    
    private static ArrayList<Opcode> buildOpcodeFamilyTable(Opcode.Family family) {
        
        ArrayList<Opcode> table = new ArrayList<Opcode>();
        for (Opcode o: Opcode.values()) {
            if(o.family == family) {
                table.add(o);
            }
        }
        return table;
    }
    
//    private void initiateReg(int regId, int value) {
//        Preconditions.checkBits8(value);
//        cpu.setReg(reg, value);
//    }
    
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
    
    private void initiateRegs(int a, int f, int b, int c, int d, int e, int h, int l) {
        cpu.setAllRegs(a, f, b, c, d, e, h, l);
    }
    
    private void initiateRegs16 (int bc, int de, int hl, int af) {
        initiateReg16(0b11, af);
        initiateReg16(0b00, bc);
        initiateReg16(0b01, de);
        initiateReg16(0b10, hl);   
    }
    
    
    @Test
    public void LD_R8_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 1, 0, 0xFF, 0);
 
        writeAllBytes(Opcode.LD_A_B.encoding,
                      Opcode.LD_C_D.encoding,
                      Opcode.LD_E_H.encoding);
        
        cycleCpu(Opcode.LD_A_B.cycles+
                 Opcode.LD_C_D.cycles+
                 Opcode.LD_E_H.cycles);
          
        assertArrayEquals(new int[] {3, 0, 0, 0, 0, 1, 1, 0XFF, 0XFF, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void LD_R8_N8_isCorrectlyExecuted() {
        writeAllBytes(Opcode.LD_A_N8.encoding,
                0x1);
        cycleCpu(Opcode.LD_A_N8.cycles);
        
        assertArrayEquals(new int[] {2, 0, 1, 0, 0, 0, 0, 0, 0, 0}, cpu._testGetPcSpAFBCDEHL());
    }
     
    @Test
    public void LD_R8_HLR_isCorrectlyExecuted() {
        // NOP opcode in A
        initiateRegs(0, 0, 3, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.LD_C_C.encoding,
                      Opcode.LD_A_HLR.encoding,
                      Opcode.NOP.encoding,
                      Opcode.LD_L_B.encoding,
                      Opcode.LD_C_HLR.encoding);
        cycleCpu(Opcode.LD_C_C.cycles+
                Opcode.LD_A_HLR.cycles+
                Opcode.NOP.cycles+
                Opcode.LD_L_B.cycles+
                Opcode.LD_C_HLR.cycles);
        
        
        assertArrayEquals(new int[] {5, 0, Opcode.LD_C_C.encoding, 0, 3, Opcode.LD_L_B.encoding, 0, 0, 0, 3},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //TODO add limit cases
    //Doesnt increment HL
    @Test
    public void LD_A_HLRI_isCorrectlyExecuted() {
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.LD_B_B.encoding, 
                Opcode.LD_A_HLRI.encoding);
        int dur = Opcode.LD_A_HLRI.cycles +
                Opcode.LD_A_HLRI.cycles;
        cycleCpu(dur);
        
        assertArrayEquals(new int[] {3, 0, Opcode.LD_B_B.encoding, 0, 0, 0, 0, 0, 0, 1},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //TODO add limit cases
    //doesnt decrement HL
    @Test
    public void LD_A_HLRD_isCorrectlyExecuted() {
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0xF);
        writeAllBytes(Opcode.LD_A_HLRD.encoding);
        bus.write(0xF, 0xF);
        cycleCpu(Opcode.LD_A_HLRD.cycles);
        
        
        assertArrayEquals(new int[] {4, 0, 0xF, 0, 0, 0, 0, 0, 0, 0xE},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //cannot test addresses above 0XFF00
    @Test
    public void LD_A_N8R_isCorrectlyExecuted() {
        writeAllBytes(Opcode.LD_A_N8R.encoding,
                1);
        bus.write(0xFF01, 4);
        cycleCpu(Opcode.LD_A_N8R.cycles);
        
        assertArrayEquals(new int[] {Opcode.LD_A_N8R.totalBytes, 0, 4, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    // TODO cannot be tested because methods uses addresses above 0xFF00
    @Test
    public void LD_A_CR_isCorrectlyExecuted() {
        initiateRegs(0, 0, 0, 1, 0, 0, 0, 0);
        writeAllBytes(Opcode.LD_A_CR.encoding);
        bus.write(0xF, 0xF);
        cycleCpu(Opcode.LD_A_CR.cycles);
        
        assertArrayEquals(new int[] {Opcode.LD_A_CR.totalBytes, 0, 0xF, 0, 0, 1, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
        
    @Test
    public void LD_A_N16R_isCorrectlyExecuted() {
        writeAllBytes(Opcode.LD_A_N16R.encoding, 
                0xF, 0);
        bus.write(0xF, 0xF);
        cycleCpu(Opcode.LD_A_N16R.cycles);
        
        assertArrayEquals(new int[] {Opcode.LD_A_N16R.totalBytes, 0, 0xF, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //TODO doesnt pass test
    @Test
    public void LD_A_BCR_isCorrectlyExecuted() {
        initiateRegs16(0xF, 0, 0, 0);
        bus.write(0xF, 0xF);
        writeAllBytes(Opcode.LD_A_BCR.encoding);
        
        cycleCpu(Opcode.LD_A_BCR.cycles);
        
        assertArrayEquals(new int[] {3, 0, 0xF, 0, 0, 0xF, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //doesnt pass test
    @Test
    public void LD_A_DER_isCorrectlyExecuted() {
        initiateRegs16(0, 0xF, 0, 0);
        writeAllBytes(Opcode.LD_A_DER.encoding);
        bus.write(0xF, 0xF);
        cycleCpu(Opcode.LD_A_DER.cycles);
        
        assertArrayEquals(new int[] {3, 0, 0xF, 0, 0, 0, 0, 0xF, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void LD_R16SP_N16_isCorrectlyExecuted() {
        writeAllBytes(Opcode.LD_BC_N16.encoding,0x1,0xFF,
                      Opcode.LD_DE_N16.encoding,0x1,0xFF,
                      Opcode.LD_HL_N16.encoding,0x1,0xFF);
//                      Opcode.LD_SP_N16.encoding,0xFF,0x1);
        int dur = Opcode.LD_BC_N16.cycles +
                  Opcode.LD_DE_N16.cycles +
                  Opcode.LD_HL_N16.cycles +
                  Opcode.LD_SP_N16.cycles;
        cycleCpu(dur);
        
        assertArrayEquals(new int[] {dur, 0, 0, 0, 0xFF, 0x01, 0xFF, 0x01, 0xFF, 0x01},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    //TODO add limit cases
    //TODO doesnt increment SP
    @Test
    public void POP_R16_isCorrectlyExecuted() {
        initiateRegs16(0xA, 0, 0, 0);
        writeAllBytes(0, Opcode.POP_BC.encoding);
        cycleCpu(Opcode.POP_BC.cycles + 1);
        
        assertArrayEquals(new int[] {4, 2, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void LD_HLR_R8_isCorrectlyExecuted() {
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0xF);
        writeAllBytes(Opcode.LD_HLR_A.encoding);
        bus.write(0xF, 0xF);
        cycleCpu(Opcode.LD_HLR_A.cycles);
        
        assertEquals(1, bus.read(0xF));
    }
    
    //TODO add limit cases
    @Test
    public void LD_HLRI_A_isCorrectlyExecuted() {
        bus.write(0xA, 0xF);
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0xA);
        writeAllBytes(Opcode.LD_HLRI_A.encoding);
        cycleCpu(Opcode.LD_HLRI_A.cycles);
        
        assertArrayEquals(new int[] {Opcode.LD_HLRI_A.totalBytes, 0, 1, 0, 0, 0, 0, 0, 0, 0xB},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(1, bus.read(0xA));
    }
    
    //TODO add limit cases
    @Test
    public void LD_HLRD_A_isCorrectlyExecuted() {
        bus.write(0xF, 0xF);
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0xF);
        writeAllBytes(Opcode.LD_HLRD_A.encoding);
        cycleCpu(Opcode.LD_HLRD_A.cycles);
        
        assertArrayEquals(new int[] {Opcode.LD_HLRI_A.totalBytes, 0, 1, 0, 0, 0, 0, 0, 0, 0xE},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(1, bus.read(0xF));
    }
    
    //cannot be tested need access to addresses above 0XFF00
    @Test
    public void LD_N8R_A_isCorrectlyExecuted() {
        initiateRegs(0xF, 0, 0, 0, 0, 0, 0, 0);
        bus.write(0xFF04, 1);
        writeAllBytes(Opcode.LD_N8R_A.encoding, 04);
        cycleCpu(Opcode.LD_N8R_A.cycles);
        
        assertEquals(0xF, bus.read(0xFF04));
    }
    
    //TODO cannot be tested need access to addresses above 0XFF00
    @Test
    public void LD_CR_A_isCorrectlyExecuted() {
        initiateRegs(0xF, 0, 0, 5, 0, 0, 0, 0);
        bus.write(0xFF05, 1);
        writeAllBytes(Opcode.LD_CR_A.encoding);
        cycleCpu(Opcode.LD_CR_A.cycles);
        
        assertEquals(0xF, bus.read(0xFF05));
    }
    
    @Test
    public void LD_N16R_A_isCorrectlyExecuted() {
        initiateRegs(0xF, 0, 0, 0, 0, 0, 0, 0);
        bus.write(0xF, 1);
        writeAllBytes(Opcode.LD_N16R_A.encoding, 0xF, 0); //big endian addresses??
        cycleCpu(Opcode.LD_N16R_A.cycles);
        
        assertEquals(0xF, bus.read(0xF));
    }
    
    @Test
    public void LD_BCR_A_isCorrectlyExecuted() {
        initiateRegs(0xF, 0, 0, 0xF, 0, 0, 0, 0);
        bus.write(0xF, 1);
        writeAllBytes(Opcode.LD_BCR_A.encoding);
        cycleCpu(Opcode.LD_BCR_A.cycles);
        
        assertEquals(0xF, bus.read(0xF));
    }
    
    @Test
    public void LD_DER_A_isCorrectlyExecuted() {
        initiateRegs(0xF, 0, 0, 0, 0, 0xA, 0, 0 );
        bus.write(0xA, 1);
        writeAllBytes(Opcode.LD_DER_A.encoding);
        cycleCpu(Opcode.LD_DER_A.cycles);
        
        assertEquals(0xF, bus.read(0xA));
    }
    
    //TODO test not passed
    @Test
    public void LD_HLR_N8_isCorrectlyExecuted() {
        initiateRegs16(0, 0, 0xA, 0);
        bus.write(0xA, 3);
        writeAllBytes(Opcode.LD_HLR_N8.encoding, 0xF);
        cycleCpu(Opcode.LD_HLR_N8.cycles);
        
        assertEquals(0xF, bus.read(0xA));
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

        assertArrayEquals(new int[] {3, 0, 0, 0, 0, 0, 0, 0, 1, 4},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //TODO write in SP? using 0 atm but not clean
    @Test
    public void LD_N16R_SP_isCorrectlyExecuted() {
        bus.write(0xF, 0xF);
        writeAllBytes(Opcode.LD_N16R_SP.encoding, 0xF, 0);
        cycleCpu(Opcode.LD_N16R_SP.cycles);
        
        assertEquals(0, bus.read(0xF));
    }
    
    //TODO doesnt pass test
    @Test
    public void LD_SP_HL_isCorrectlyExecuted() {
        initiateRegs16(0, 0, 0xF, 0);
        writeAllBytes(Opcode.LD_SP_HL.encoding);
        cycleCpu(Opcode.LD_SP_HL.cycles);
        
        assertArrayEquals(new int[] {3, 0xF, 0, 0, 0, 0, 0, 0, 0, 0xF},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //TODO need to setup SP here
    @Test
    public void PUSH_R16_isCorrectlyExecuted() {
        
    }

    //TODO add limit cases

}
