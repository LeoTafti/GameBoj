/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest5 {
    
    private Bus bus;
    private Cpu cpu;
    
    private final int RAM_SIZE = 0xFFFF ;
    
    @Before
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
//    private void writeAllBytes(int startAdress, int ... bytes) {
//        Preconditions.checkBits16(startAdress);
//        if(bytes.length > RAM_SIZE)
//            throw new IllegalArgumentException("not enough ram for that");
//        for(int i = 0; i<bytes.length; i++) {
//            int instr = bytes[i];
//            Preconditions.checkBits8(instr);
//            bus.write(startAdress+i, instr);
//        }
//    }
    
    /**
     * writes all bytes at adresses STARTING AT 0
     * @param bytes
     */
    private void writeAllBytes(int ... bytes) {
        if(bytes.length > RAM_SIZE)
            throw new IllegalArgumentException("not enough ram for that");
        for(int i = 0; i<bytes.length; i ++) {
            int instr = bytes[i];
            Preconditions.checkBits8(instr);
            bus.write(i, instr);
        }
    }
    
    private void writeAllBytesFromAddress(int startAddress, int ... bytes) {
        Preconditions.checkBits16(startAddress);
        if(bytes.length > RAM_SIZE)
            throw new IllegalArgumentException("not enough ram for that");
        for(int i = 0; i<bytes.length; i ++) {
            int instr = bytes[i];
            Preconditions.checkBits8(instr);
            bus.write(i+startAddress, instr);
        }
    }
    
    /**
     * writes all opcodes and cycles 
     * OPCODES ONLY
     * @param opcodes Opcode list
     * @return number of bytes read (new PC value)
     */
//    private int execute(Opcode ... opcodes) {
//        int cyclesum = 0;
//        int pcInc = 0;
//        for(int i = 0; i<opcodes.length * 2; i ++) {
//            
//            Opcode op = opcodes[i];
//            if(op.kind == op.kind.PREFIXED) {  //write prefix if prefixed opcode
//                bus.write(i, 0xCB);
//                i ++ ;
//            }
//            bus.write(i, op.encoding);
//            cyclesum += op.cycles;
//            pcInc += op.totalBytes;
//        }
//        cycleCpu(cyclesum);
//        return pcInc;
//    }
    
//    private int execute(Opcode opcode) {
//        if(opcode.kind == Opcode.Kind.PREFIXED) {
//            bus.write(0, 0xCB);
//        }
//        bus.write(1, opcode.encoding);
//        
//        cycleCpu(opcode.cycles);
//        
//        return opcode.totalBytes;
//    }
    
    private int execute(Opcode ... opcodes) {
        int i = 0;
        int pcValue = 0;
        int totalCycles = 0;
        
        for(Opcode opcode : opcodes) {
            if(opcode.kind == Opcode.Kind.PREFIXED) {
                bus.write(i, 0xCB);
                i++;
            }
            bus.write(i, opcode.encoding);
            i++;
            pcValue += opcode.totalBytes;
            totalCycles += opcode.cycles;
        }
        
        cycleCpu(totalCycles);
        
        return pcValue;
    }
    
    private void initiateRegs(int a, int f, int b, int c, int d, int e, int h, int l) {
        cpu.setAllRegs(a, f, b, c, d, e, h, l);
    }
    
    private void initiateRegs16(int af, int bc, int de, int hl) {
        cpu.setAllRegs16(af, bc, de, hl);
    }
    
    private void initiateInterruptRegs( boolean ime, int ie, int If ) {
        Preconditions.checkBits8(ie);
        Preconditions.checkBits8(If);
        
        cpu.setInterruptRegs(ime, ie, If);
    }
    
    // ::::::::::::::::::::::: Jump Tests :::::::::::::::::::::::::::::
    
    @Test
    public void JP_HL_isCorrectlyExecuted() {
        
        initiateRegs(1, 0x10, 0, 0, 0, 0, 1, 0xF0);
        
        int i = execute(Opcode.JP_HL);
        
        assertArrayEquals(new int[] {0x1F0, 0, 1, 0x10, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void JP_N16_isCorrectlyExecuted() {
        
        // TODO verify bus reads/writes big-endian
        writeAllBytes(Opcode.JP_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_N16.cycles);
        
        assertArrayEquals(new int[] {0x1F0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_NZ_N16_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x70, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_NZ_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_NZ_N16.cycles);

        
        assertArrayEquals(new int[] {0x1F0, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_Z_N16_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_Z_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_Z_N16.cycles);

        
        assertArrayEquals(new int[] {0x1F0, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_NC_N16_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xE0, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_NC_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_NC_N16.cycles);

        
        assertArrayEquals(new int[] {0x1F0, 0, 0, 0xE0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_C_N16_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_C_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_C_N16.cycles);

        
        assertArrayEquals(new int[] {0x1F0, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_NZ_N16_doesNothing() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_NZ_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_NZ_N16.cycles);

        
        assertArrayEquals(new int[] {Opcode.JP_NZ_N16.totalBytes, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_Z_N16_doesNothing() {
        
        initiateRegs(0, 0x70, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_Z_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_Z_N16.cycles);

        
        assertArrayEquals(new int[] {Opcode.JP_Z_N16.totalBytes, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_NC_N16_doesNothing() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_NC_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_NC_N16.cycles);

        
        assertArrayEquals(new int[] {Opcode.JP_NC_N16.totalBytes, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JP_C_N16_doesNothing() {
        
        initiateRegs(0, 0xE0, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JP_C_N16.encoding, 0xF0, 1);
        cycleCpu(Opcode.JP_C_N16.cycles);

        
        assertArrayEquals(new int[] {Opcode.JP_C_N16.totalBytes, 0, 0, 0xE0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_E8_isCorrectlyExecuted() {
                
        writeAllBytes(Opcode.JR_E8.encoding, 1);
        cycleCpu(Opcode.JR_E8.cycles);
        
        assertArrayEquals(new int[] {Opcode.JR_E8.totalBytes + 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());        
    }
    
    @Test
    public void JR_E8_UnderflowsCorrectly() {
                
        writeAllBytes(Opcode.JR_E8.encoding, 0xFD);
        cycleCpu(Opcode.JR_E8.cycles);
        // PC = 2 - 3
        assertArrayEquals(new int[] {0xFFFF, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());        
    }
    
    @Test
    public void JR_E8_OverflowsCorrectly() {
                
        bus.write(0xFFFC, Opcode.JR_E8.encoding);
        bus.write(0xFFFE, 2);
        cpu.setPC(0xFFFC);
        cycleCpu(Opcode.JR_E8.cycles);
        
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());        
    }
    
    @Test
    public void JR_CC_E8_isCorrectlyExecutedOnHighPC() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        bus.write(0xFF, Opcode.JR_Z_E8.encoding);
        bus.write(0x100, 1);
        cpu.setPC(0xFF);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {0x102, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());   
    }
    
    @Test
    public void JR_CC_E8_isCorrectlyExecutedOnHighPC2() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        bus.write(0xFF, Opcode.JR_Z_E8.encoding);
        bus.write(0x100, 0xFF);
        cpu.setPC(0xFF);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {0x100, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());   
    }
    
    @Test
    public void JR_CC_E8_UnderflowsCorrectly() {
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
                
        writeAllBytes(Opcode.JR_Z_E8.encoding, 0xFD);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {0xFFFF, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());        
    }
    
    @Test //TODO PC NOT CLIPPED
    public void JR_CC_E8_OverflowsCorrectly() {
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
                
        bus.write(0xFFFD, Opcode.JR_Z_E8.encoding);
        bus.write(0xFFFE, 1);
        cpu.setPC(0xFFFD);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {0, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());        
    }
    
    @Test
    public void JR_NZ_E8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x70, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_NZ_E8.encoding, 0x10);
        cycleCpu(Opcode.JR_NZ_E8.cycles);
        
        assertArrayEquals(new int[] {0x12, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_NZ_E8_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x70, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_NZ_E8.encoding, 0xFD);
        cycleCpu(Opcode.JR_NZ_E8.cycles);
        
        assertArrayEquals(new int[] {0xFFFF, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_Z_E8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_Z_E8.encoding, 0x70);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {0x72, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_Z_E8_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_Z_E8.encoding, 0xFD);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {0xFFFF, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_NC_E8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xE0, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_NC_E8.encoding, 0xD);
        cycleCpu(Opcode.JR_NC_E8.cycles);
        
        assertArrayEquals(new int[] {0xF, 0, 0, 0xE0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_NC_E8_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0xE0, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_NC_E8.encoding, 0xFD);
        cycleCpu(Opcode.JR_NC_E8.cycles);
        
        assertArrayEquals(new int[] {0xFFFF, 0, 0, 0xE0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_C_E8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_C_E8.encoding, 0x70);
        cycleCpu(Opcode.JR_C_E8.cycles);
        
        assertArrayEquals(new int[] {0x72, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_NZ_E8_doesNothing() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_NZ_E8.encoding, 0xF);
        cycleCpu(Opcode.JR_NZ_E8.cycles);
        
        assertArrayEquals(new int[] {Opcode.JR_NZ_E8.totalBytes, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_Z_E8_doesNothing() {
        
        initiateRegs(0, 0x70, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_Z_E8.encoding, 0xF);
        cycleCpu(Opcode.JR_Z_E8.cycles);
        
        assertArrayEquals(new int[] {Opcode.JR_Z_E8.totalBytes, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_NC_E8_doesNothing() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_NC_E8.encoding, 0xF);
        cycleCpu(Opcode.JR_NC_E8.cycles);
        
        assertArrayEquals(new int[] {Opcode.JR_C_E8.totalBytes, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    @Test
    public void JR_C_E8_doesNothing() {
        
        initiateRegs(0, 0xE0, 0, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.JR_C_E8.encoding, 0xF);
        cycleCpu(Opcode.JR_C_E8.cycles);
        
        assertArrayEquals(new int[] {Opcode.JR_C_E8.totalBytes, 0, 0, 0xE0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
    }
    
    // ::::::::::::::::::::::: Calls and Returns Tests ::::::::::::::::
    
    @Test
    public void CALL_N16_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int PC_prime = initialPC + Opcode.CALL_N16.totalBytes;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        writeAllBytesFromAddress(initialPC, Opcode.CALL_N16.encoding, 0x34, 0x12);
        cycleCpu(Opcode.CALL_N16.cycles);
        
        assertArrayEquals(new int[] {0x1234, initialSP - 2, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        int PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        int PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
    }
    
    @Test
    public void CALL_C_N16_isCorrectlyExecuted() {
        int regF = 0b0001_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        
        int initialPC = 0xABCD;
        int PC_prime = initialPC + Opcode.CALL_C_N16.totalBytes;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        writeAllBytesFromAddress(initialPC, Opcode.CALL_C_N16.encoding, 0x34, 0x12);
        cycleCpu(Opcode.CALL_C_N16.cycles);
        
        assertArrayEquals(new int[] {0x1234, initialSP - 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        int PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        int PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
        
        cpu.reset();
        
        //Tests that it does nothing if condition is not satisfied
        regF = 0b0000_0000;
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        
        initialPC = 0xABCD;
        initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        writeAllBytesFromAddress(initialPC, Opcode.CALL_C_N16.encoding, 0x34, 0x12);
        cycleCpu(Opcode.CALL_C_N16.cycles);
        
        assertArrayEquals(new int[] {initialPC + Opcode.CALL_C_N16.totalBytes, initialSP, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
    }
    
    @Test
    public void CALL_NC_N16_isCorrectlyExecuted() {
        int regF = 0b0000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        
        int initialPC = 0xABCD;
        int PC_prime = initialPC + Opcode.CALL_NC_N16.totalBytes;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        writeAllBytesFromAddress(initialPC, Opcode.CALL_NC_N16.encoding, 0x34, 0x12);
        cycleCpu(Opcode.CALL_NC_N16.cycles);
        
        assertArrayEquals(new int[] {0x1234, initialSP - 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        int PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        int PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
    }
    
    @Test
    public void CALL_Z_N16_isCorrectlyExecuted() {
        int regF = 0b1000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        
        int initialPC = 0xABCD;
        int PC_prime = initialPC + Opcode.CALL_Z_N16.totalBytes;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        writeAllBytesFromAddress(initialPC, Opcode.CALL_Z_N16.encoding, 0x34, 0x12);
        cycleCpu(Opcode.CALL_Z_N16.cycles);
        
        assertArrayEquals(new int[] {0x1234, initialSP - 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        int PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        int PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
    }
    
    @Test
    public void CALL_NZ_N16_isCorrectlyExecuted() {
        int regF = 0b0000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        
        int initialPC = 0xABCD;
        int PC_prime = initialPC + Opcode.CALL_NZ_N16.totalBytes;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        writeAllBytesFromAddress(initialPC, Opcode.CALL_NZ_N16.encoding, 0x34, 0x12);
        cycleCpu(Opcode.CALL_NZ_N16.cycles);
        
        assertArrayEquals(new int[] {0x1234, initialSP - 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        int PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        int PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
    }
    
    @Test
    public void RST_U3_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int PC_prime = initialPC + Opcode.RST_2.totalBytes;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        
        bus.write(initialPC, Opcode.RST_2.encoding);
        cycleCpu(Opcode.RST_2.cycles);
        
        assertArrayEquals(new int[] {8*2, initialSP - 2, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        int PC_prime_MSB = (PC_prime & 0xFF00) >> 8;
        int PC_prime_LSB = PC_prime & 0x00FF;
        assertEquals(PC_prime_LSB, bus.read(initialSP-2));
        assertEquals(PC_prime_MSB, bus.read(initialSP-1));
        
    }
    
    @Test
    public void RET_isCorrectlyExecuted() {
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        int newPC = 0x12;
        bus.write(initialPC, Opcode.RET.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET.cycles);
        
        assertArrayEquals(new int[] {newPC, initialSP + 2, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void RET_C_isCorrectlyExecuted() {
        int regF = 0b0001_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        int newPC = 0x12;
        bus.write(initialPC, Opcode.RET_C.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_C.cycles);
        
        assertArrayEquals(new int[] {newPC, initialSP + 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        // Tests that does nothing when condition isn't satisfied
        
        regF = 0b1110_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        initialPC = 0xABCD;
        initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        newPC = 0x12;
        bus.write(initialPC, Opcode.RET_C.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_C.cycles);
        
        assertArrayEquals(new int[] {initialPC+Opcode.RET_C.totalBytes, initialSP, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void RET_NC_isCorrectlyExecuted() {
        int regF = 0b0000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        int newPC = 0x12;
        bus.write(initialPC, Opcode.RET_NC.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_NC.cycles);
        
        assertArrayEquals(new int[] {newPC, initialSP + 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        // Tests that does nothing when condition isn't satisfied
        
        regF = 0b0001_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        initialPC = 0xABCD;
        initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        newPC = 0x12;
        bus.write(initialPC, Opcode.RET_NC.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_NC.cycles);
        
        assertArrayEquals(new int[] {initialPC+Opcode.RET_NC.totalBytes, initialSP, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void RET_Z_isCorrectlyExecuted() {
        int regF = 0b1000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        int newPC = 0x12;
        bus.write(initialPC, Opcode.RET_Z.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_Z.cycles);
        
        assertArrayEquals(new int[] {newPC, initialSP + 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        // Tests that does nothing when condition isn't satisfied
        
        regF = 0b0000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        initialPC = 0xABCD;
        initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        newPC = 0x12;
        bus.write(initialPC, Opcode.RET_Z.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_Z.cycles);
        
        assertArrayEquals(new int[] {initialPC+Opcode.RET_Z.totalBytes, initialSP, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void RET_NZ_isCorrectlyExecuted() {
        int regF = 0b0000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        int initialPC = 0xABCD;
        int initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        int newPC = 0x12;
        bus.write(initialPC, Opcode.RET_NZ.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_NZ.cycles);
        
        assertArrayEquals(new int[] {newPC, initialSP + 2, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        // Tests that does nothing when condition isn't satisfied
        
        regF = 0b1000_0000;
        
        initiateRegs(0, regF, 0, 0, 0, 0, 0, 0);
        initialPC = 0xABCD;
        initialSP = 0xFFAB;
        cpu.setPC(initialPC);
        cpu.setSP(initialSP);
        
        newPC = 0x12;
        bus.write(initialPC, Opcode.RET_NZ.encoding);
        bus.write(initialSP, newPC);
        cycleCpu(Opcode.RET_NZ.cycles);
        
        assertArrayEquals(new int[] {initialPC+Opcode.RET_NZ.totalBytes, initialSP, 0, regF, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    // ::::::::::::::::::::::: Interrupts Tests :::::::::::::::::::::::
    
    @Test
    public void EI_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        initiateInterruptRegs(false, 0, 0);
        
        int i = execute(Opcode.EI);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        assertArrayEquals(new int[] {1, 0, 0},
                cpu.get_IME_IE_IF());
    }
    
    @Test
    public void DI_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        initiateInterruptRegs(true, 0, 0);
        
        int i = execute(Opcode.DI);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        assertArrayEquals(new int[] {0, 0, 0},
                cpu.get_IME_IE_IF());
    }
    
    @Test
    public void RETI_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        initiateInterruptRegs(false, 0, 0);
        cpu.setSP(0xAB);
        bus.write(0xAB, 0xCD);
        
        execute(Opcode.RETI);
        
        assertArrayEquals(new int[] {0xCD, 0xAB + 2, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        assertArrayEquals(new int[] {1, 0, 0},
                cpu.get_IME_IE_IF());
    }
    
    // ::::::::::::::::::::::: MiscControl Tests ::::::::::::::::::::::
    
    @Test
    public void LD_A_doesNothingAfter_HALT() {
        
        
        initiateRegs(0, 0, 0xf, 0, 0, 0, 0, 0);
        int i = execute(Opcode.HALT, Opcode.LD_A_B);
        
        assertArrayEquals(new int[] {Opcode.HALT.totalBytes, 0, 0, 0, 0xf, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test //TODO is it supposed to not stop?
    public void HALT_doesNothingIfPendingInterrupt() {
        
        initiateInterruptRegs(false, 0x10, 0x10);
        initiateRegs(0, 0, 0xf, 0, 0, 0, 0, 0);
        int i = execute(Opcode.HALT, Opcode.LD_A_B);
        
        assertArrayEquals(new int[] {Opcode.HALT.totalBytes, 0, 0xf, 0, 0xf, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void interruptWakesCPUAfter_HALT() {
        
        initiateInterruptRegs(false, 0, 0);
        initiateRegs(0, 0, 0xf, 0, 0, 0, 0, 0);
        
        writeAllBytes(Opcode.HALT.encoding, Opcode.LD_A_B.encoding);
        
        assertArrayEquals(new int[] {Opcode.HALT.totalBytes, 0, 0, 0, 0xf, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.requestInterrupt(Interrupt.VBLANK);
        
        cycleCpu(5); // uggly but whatever
        
        assertArrayEquals(new int[] {Opcode.HALT.totalBytes + Opcode.LD_A_B.totalBytes,
                0, 0xf, 0, 0xf, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void STOPThrowsError() {
        
        assertThrows(Error.class,
                () -> { int i = execute(Opcode.STOP);});
    
    }
    
}