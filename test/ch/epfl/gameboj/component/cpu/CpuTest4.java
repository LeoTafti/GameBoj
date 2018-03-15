/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    
    /**
     * writes all opcodes and cycles 
     * OPCODES ONLY
     * @param opcodes Opcode list
     * @return number of cycles
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
    
    // :::::::::::::::::::: ADD TESTS ::::::::::::::::::
    @Test
    public void ADD_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.ADD_A_N8.encoding, 0xff);
        cycleCpu(Opcode.ADD_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.ADD_A_N8.totalBytes, 0, 0xff, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADD_A_N8_isCorrectlyExecuted2() {
        //Z0HC and overflow
        initiateRegs(1, 0x30, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.ADD_A_N8.encoding, 0xff);
        cycleCpu(Opcode.ADD_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.ADD_A_N8.totalBytes, 0, 0, 0xB0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADD_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0xF0, 0x30, 0xF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.ADD_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0xF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADD_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 0x01, 0xF0);
        bus.write(0x1F0, 0xF);
        int i = execute(Opcode.ADD_A_HLR);
        assertArrayEquals(new int[] {i, 0, 0xF, 0, 0, 0, 0, 0, 0x01, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADC_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0xF, 0x10, 0, 1, 0, 0, 0, 0);
        int i = execute(Opcode.ADC_A_C);
        assertArrayEquals(new int[] {i, 0, 0x11, 0x20, 0, 1, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADC_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.ADC_A_N8.encoding, 0xff);
        cycleCpu(Opcode.ADC_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.ADD_A_N8.totalBytes, 0, 0, 0xB0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADC_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0x10, 0, 0, 0, 0, 0x01, 0xF0);
        bus.write(0x01F0, 1);
        int i = execute(Opcode.ADC_A_HLR);
        assertArrayEquals(new int[] {i, 0, 0, 0xB0, 0, 0, 0, 0, 0x01, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void INC_R8_isCorrectlyExecuted() {
        
        initiateRegs(0xE, 0x10, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.INC_A, Opcode.INC_B);
        assertArrayEquals(new int[] {i, 0, 0xF, 0xB0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void INC_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0x01, 0xF0);
        bus.write(0x01F0, 0xFF);
        int i = execute(Opcode.INC_HLR);
        assertArrayEquals(new int[] {i, 0, 0, 0xA0, 0, 0, 0, 0, 0x01, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x01F0));
    }
    
    @Test
    public void INC_R16SP_isCorrectlyExecuted() {
        
        initiateRegs(0x1, 0x10, 0, 0xFF, 0, 0, 0, 0);
        int i = execute(Opcode.INC_SP, Opcode.INC_BC);
        assertArrayEquals(new int[] {i, 1, 1, 0x10, 1, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADD_HL_R16SP_isCorrectlyExecuted() {
        initiateRegs(0, 0, 1, 1, 0, 0, 0xFE, 0xFF);
        int i = execute(Opcode.ADD_HL_BC);
        assertArrayEquals(new int[] {i, 0, 0, 0x30, 1, 1, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ADD_HL_R16SP_isCorrectlyExecuted2() {
        initiateRegs(0, 0, 0, 0, 0, 0, 0xFE, 0xFF);
        cpu.setSP(0x0101);
        int i = execute(Opcode.ADD_HL_SP);
        assertArrayEquals(new int[] {i, 0x0101, 0, 0x30, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void LD_HLSP_S8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0xA, 0xF);
        cpu.setSP(0x0FFF);
        writeAllBytes(Opcode.LD_HL_SP_N8.encoding, 1);
        cycleCpu(Opcode.LD_HL_SP_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.LD_HL_SP_N8.totalBytes, 0xFFF, 0, 0x30, 0, 0, 0, 0, 0X10, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void LD_HLSP_S8_isCorrectlyExecuted2() {
        //underflow and sub
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        cpu.setSP(0x7F);
        writeAllBytes(Opcode.ADD_SP_N.encoding, 0x80);
        cycleCpu(Opcode.ADD_SP_N.cycles);
        
        assertArrayEquals(new int[] {Opcode.ADD_SP_N.totalBytes, 0xFFFF, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    // ::::::::::::::::::::::::: SUB TESTS :::::::::::::::::::::
    @Test
    public void SUB_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.SUB_A_N8.encoding, 0xFF);
        cycleCpu(Opcode.SUB_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.SUB_A_N8.totalBytes, 0, 0xFF, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SUB_A_N8_isCorrectlyExecuted2() {
        
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.SUB_A_N8.encoding, 1);
        cycleCpu(Opcode.SUB_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.SUB_A_N8.totalBytes, 0, 0, 0xC0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SUB_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SUB_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0x70, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SUB_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.SUB_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0x70, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SBC_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0x10, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.SBC_A_N8.encoding, 0xFE);
        cycleCpu(Opcode.SBC_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.SBC_A_N8.totalBytes, 0, 0xFF, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SBC_A_N8_isCorrectlyExecuted2() {
        
        initiateRegs(2, 0x10, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.SBC_A_N8.encoding, 1);
        cycleCpu(Opcode.SBC_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.SBC_A_N8.totalBytes, 0, 0, 0xC0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SBC_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0x10, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SBC_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0xFE, 0x70, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SBC_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0xFE, 0x10, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.SBC_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0xFE, 0x70, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_R8_isCorrectlyExecuted() {
        
        initiateRegs(1, 0x10, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DEC_A, Opcode.DEC_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x70, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_R8_isCorrectlyExecuted2() {
        
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DEC_A);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xC0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_R8_isCorrectlyExecuted3() {
        
        initiateRegs(0x80, 0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DEC_A);
        
        assertArrayEquals(new int[] {i, 0, 0x7F, 0x60, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0);
        int i = execute(Opcode.DEC_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x70, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(bus.read(0x1F0), 0xFF);
    }
    
    @Test
    public void DEC_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 1);
        int i = execute(Opcode.DEC_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xD0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(bus.read(0x1F0), 0);
    }
    
    @Test
    public void CP_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.CP_A_N8.encoding, 1);
        cycleCpu(Opcode.CP_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.CP_A_N8.totalBytes, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CP_A_N8_isCorrectlyExecuted2() {
        
        initiateRegs(1, 0, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.CP_A_N8.encoding, 1);
        cycleCpu(Opcode.CP_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.CP_A_N8.totalBytes, 0, 1, 0xC0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CP_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 1, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CP_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x70, 1, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CP_A_R8_isCorrectlyExecuted2() {
        
        initiateRegs(1, 0, 1, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CP_A_B);
        
        assertArrayEquals(new int[] {i, 0, 1, 0xC0, 1, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CP_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 1);
        int i = execute(Opcode.CP_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x70, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(bus.read(0x1F0), 1);
    }
    
    @Test
    public void CP_A_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(1, 0, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 1);
        int i = execute(Opcode.CP_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 1, 0xC0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(bus.read(0x1F0), 1);
    }
    
    @Test
    public void DEC_R16SP_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0, 0, 0xF, 0xFF, 0, 0);
        int i = execute(Opcode.DEC_DE);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xF0, 0, 0, 0xF, 0xFE, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_R16SP_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DEC_DE);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x30, 0, 0, 0xFF, 0xFF, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_R16SP_isCorrectlyExecuted3() {
        
        initiateRegs(0xF, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DEC_SP);
        
        assertArrayEquals(new int[] {i, 0xFFFF, 0xF, 0x30, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DEC_R16SP_isCorrectlyExecuted4() {
        
        initiateRegs(0xF, 0x30, 0, 0, 0, 0, 0, 0);
        cpu.setSP(1);
        int i = execute(Opcode.DEC_SP);
        
        assertArrayEquals(new int[] {i, 0, 0xF, 0x30, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //::::::::::::::::::::::::::::: BIT OPERATIONS :::::::::::::::::::::::::
    
    @Test
    public void AND_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.AND_A_N8.encoding, 0xFF);
        cycleCpu(Opcode.AND_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.AND_A_N8.totalBytes, 0, 0, 0xA0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_N8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.AND_A_N8.encoding, 0);
        cycleCpu(Opcode.AND_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.AND_A_N8.totalBytes, 0, 0, 0xA0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_N8_isCorrectlyExecuted3() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.AND_A_N8.encoding, 1);
        cycleCpu(Opcode.AND_A_N8.cycles);
        
        assertArrayEquals(new int[] {Opcode.AND_A_N8.totalBytes, 0, 0x1, 0x20, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.AND_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xA0, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_R8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x80, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.AND_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xA0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_R8_isCorrectlyExecuted3() {
        
        initiateRegs(0xFF, 0x80, 1, 0, 0, 0, 0, 0);
        int i = execute(Opcode.AND_A_B);
        
        assertArrayEquals(new int[] {i, 0, 1, 0x20, 1, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.AND_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xA0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x80, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0);
        int i = execute(Opcode.AND_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xA0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void AND_A_HLR_isCorrectlyExecuted3() {
        
        initiateRegs(0xFF, 0x80, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 1);
        int i = execute(Opcode.AND_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 1, 0x20, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.OR_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_R8_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.OR_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.OR_A_N8.encoding, 0xFF);
        cycleCpu(Opcode.OR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.OR_A_N8.totalBytes, 0, 0xFF, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_N8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x80, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.OR_A_N8.encoding, 0);
        cycleCpu(Opcode.OR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.OR_A_N8.totalBytes, 0, 0xFF, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_N8_isCorrectlyExecuted3() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.OR_A_N8.encoding, 0);
        cycleCpu(Opcode.OR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.OR_A_N8.totalBytes, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0);
        int i = execute(Opcode.OR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.OR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void OR_A_HLR_isCorrectlyExecuted3() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0);
        int i = execute(Opcode.OR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.XOR_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_R8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.XOR_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_R8_isCorrectlyExecuted3() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.XOR_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_R8_isCorrectlyExecute4() {
        
        initiateRegs(0xFF, 0x80, 0xFF, 0, 0, 0, 0, 0);
        int i = execute(Opcode.XOR_A_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0xFF, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_N8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.XOR_A_N8.encoding, 0xFF);
        cycleCpu(Opcode.XOR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.XOR_A_N8.totalBytes, 0, 0xFF, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_N8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.XOR_A_N8.encoding, 0);
        cycleCpu(Opcode.XOR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.XOR_A_N8.totalBytes, 0, 0xFF, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_N8_isCorrectlyExecuted3() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.XOR_A_N8.encoding, 0xFF);
        cycleCpu(Opcode.XOR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.XOR_A_N8.totalBytes, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_N8_isCorrectlyExecuted4() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 0, 0);
        writeAllBytes(Opcode.XOR_A_N8.encoding, 0);
        cycleCpu(Opcode.XOR_A_N8.cycles);
        assertArrayEquals(new int[] {Opcode.XOR_A_N8.totalBytes, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.XOR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0);
        int i = execute(Opcode.XOR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_HLR_isCorrectlyExecuted3() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0);
        int i = execute(Opcode.XOR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void XOR_A_HLR_isCorrectlyExecuted4() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.XOR_A_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CPL_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CPL);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0xE0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CPL_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CPL);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x70, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CPL_isCorrectlyExecuted3() {
        
        initiateRegs(0xF, 0x20, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CPL);
        
        assertArrayEquals(new int[] {i, 0, 0xF0, 0x60, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    //:::::::::::::::::::::::::::::: ROTATE & SHIFT :::::::::::::::::::::::::::::
    
    @Test
    public void ROTCA_isCorrectlyExecuted() {
        
        initiateRegs(0x90, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.RLCA);
        
        assertArrayEquals(new int[] {i, 0, 0x21, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ROTCA_isCorrectlyExecuted2() {
        initiateRegs(0x21, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.RRCA);
        
        assertArrayEquals(new int[] {i, 0, 0x90, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ROTA_isCorrectlyExecuted() {
        
        initiateRegs(0b10010000, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.RLA);
        
        assertArrayEquals(new int[] {i, 0, 0b0010_0001, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0x10, 0xF0, 0, 0, 0, 0, 0, 0);
        i = execute(Opcode.RLA);
        
        assertArrayEquals(new int[] {i, 0, 0b0010_0001, 0x0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0b1001_0001, 0, 0, 0, 0, 0, 0, 0);
        i = execute(Opcode.RRA);
        
        assertArrayEquals(new int[] {i, 0, 0b0100_1000, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0b1001_0011, 0, 0, 0, 0, 0, 0, 0);
        i = execute(Opcode.RRA);
        
        assertArrayEquals(new int[] {i, 0, 0b0100_1001, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ROTC_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0x90, 0, 0, 0, 0, 0);
        int i = execute(Opcode.RLC_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0x21, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0xF0, 0x21, 0, 0, 0, 0, 0);
        i = execute(Opcode.RRC_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0x90, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0xF0, 0b0100_0001, 0, 0, 0, 0, 0);
        i = execute(Opcode.RLC_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0b1000_0010, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0x80, 0x82, 0, 0, 0, 0, 0);
        i = execute(Opcode.RRC_B);
        assertArrayEquals(new int[] {i, 0, 0, 0, 0b0100_0001, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ROT_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0b10010000, 0, 0, 0, 0, 0);
        int i = execute(Opcode.RL_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0b0010_0001, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0xF0, 0x10, 0, 0, 0, 0, 0);
        i = execute(Opcode.RL_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0b0010_0001, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0xF0, 0b1001_0001, 0, 0, 0, 0, 0);
        i = execute(Opcode.RR_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0b1100_1000, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0, 0b1001_0000, 0, 0, 0, 0, 0);
        i = execute(Opcode.RR_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0b0100_1000, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0, 0x80, 0, 0, 0, 0, 0);
        i = execute(Opcode.RL_B);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void ROTC_HLR_isCorrectlyExecuted() {
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0001);
        int i = execute(Opcode.RLC_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0010_0011, bus.read(0x1F0));
        
        
        cpu.reset();
        initiateRegs(0, 0, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b0001_0001);
        i = execute(Opcode.RLC_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0010_0010, bus.read(0x1F0));
        
        cpu.reset();
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0001);
        i = execute(Opcode.RRC_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1100_1000, bus.read(0x1F0));
        
        cpu.reset();
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0001);
        i = execute(Opcode.RRC_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1100_1000, bus.read(0x1F0));
    }
    
    @Test
    public void ROT_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0001);
        int i = execute(Opcode.RL_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0010_0011, bus.read(0x1F0));
        
        
        cpu.reset();
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0001);
        i = execute(Opcode.RR_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1100_1000, bus.read(0x1F0));
        
        
        cpu.reset();
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b0001_0001);
        i = execute(Opcode.RL_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0010_0011, bus.read(0x1F0));
        
        
        cpu.reset();
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0000);
        i = execute(Opcode.RR_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1100_1000, bus.read(0x1F0));
        
        
        cpu.reset();
        initiateRegs(0, 0, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1001_0000);
        i = execute(Opcode.RR_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0100_1000, bus.read(0x1F0));
        
    }
    
    @Test
    public void SWAP_R8_isCorrectlyExecuted() {
        
        initiateRegs(0b1000_0001, 0xFF, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SWAP_A);
        
        assertArrayEquals(new int[] {i, 0, 0b0001_1000, 0x0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
        
        cpu.reset();
        initiateRegs(0, 0xFF, 0, 0, 0, 0, 0, 0b0000_0000);
        i = execute(Opcode.SWAP_L);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 0, 0b0000_0000},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SWAP_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xFF, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b1000_0001);
        
        int i = execute(Opcode.SWAP_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x0, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0001_1000, bus.read(0x1F0));
        
        
        cpu.reset();
        initiateRegs(0, 0xFF, 0, 0, 0, 0, 0x1, 0xF0);
        bus.write(0x1F0, 0b0000_0000);
        
        i = execute(Opcode.SWAP_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 0x1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0000_0000, bus.read(0x1F0));
    }
    
    @Test
    public void SLA_R8_isCorrectlyExecuted() {
        
        initiateRegs(1, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SLA_A);
        
        assertArrayEquals(new int[] {i, 0, 0x2, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SLA_R8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFF, 0x60, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SLA_A);
        
        assertArrayEquals(new int[] {i, 0, 0xFE, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SLA_R8_isCorrectlyExecuted3() {
        
        initiateRegs(0x80, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SLA_A);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SRA_R8_isCorrectlyExecuted() {
        
        initiateRegs(0xFF, 0x20, 0, 0, 0, 0, 0, 0);
        int i =execute(Opcode.SRA_A);
        
        assertArrayEquals(new int[] {i, 0, 0xFF, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SRA_R8_isCorrectlyExecuted2() {
        
        initiateRegs(0x80, 0x20, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SRA_A);
        
        assertArrayEquals(new int[] {i, 0, 0xC0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SRA_R8_isCorrectlyExecuted3() {
        
        initiateRegs(0x9, 0x60, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SRA_A);
        
        assertArrayEquals(new int[] {i, 0, 0b0000_0100, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SRA_R8_isCorrectlyExecuted4() {
        
        initiateRegs(0x1, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SRA_A);
         
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SRL_R8_isCorrectlyExecuted() {
        
        initiateRegs(1, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SRL_A);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SRL_R8_isCorrectlyExecuted2() {
        
        initiateRegs(0xFE, 0x60, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SRL_A);
        
        assertArrayEquals(new int[] {i, 0, 0x7F, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SLA_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0xFF);
        int i = execute(Opcode.SLA_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xFE, bus.read(0x1F0));
    }
    
    @Test
    public void SLA_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x90, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0x80);
        int i = execute(Opcode.SLA_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x1F0));
    }
    
    @Test
    public void SRA_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x60, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0x81);
        int i = execute(Opcode.SRA_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xC0, bus.read(0x1F0));
    }
    
    @Test
    public void SRA_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x90, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 1);
        int i = execute(Opcode.SRA_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x1F0));
    }
    
    @Test
    public void SRL_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x60, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 0x81);
        int i = execute(Opcode.SRL_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0x40, bus.read(0x1F0));
    }
    
    @Test
    public void SRL_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0x1F0, 1);
        int i = execute(Opcode.SRL_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x1F0));
    }
    
    //::::::::::::::::::::::::::::::: BIT TEST & SET :::::::::::::::::::::::::::::
    
    @Test
    public void BIT_U3_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.BIT_0_A);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xB0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void BIT_U3_R8_isCorrectlyExecuted2() {
        
        initiateRegs(1, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.BIT_0_A);
        
        assertArrayEquals(new int[] {i, 0, 1, 0x30, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void BIT_U3_R8_isCorrectlyExecuted3() {
        
        initiateRegs(0x80, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.BIT_7_A);
        
        assertArrayEquals(new int[] {i, 0, 0x80, 0x30, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void BIT_U3_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0X1F0, 0);
        int i = execute(Opcode.BIT_0_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xB0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x1F0));
    }
    
    @Test
    public void BIT_U3_HLR_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x60, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0X1F0, 1);
        int i = execute(Opcode.BIT_0_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x20, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(1, bus.read(0x1F0));
    }
    
    @Test
    public void BIT_U3_HLR_isCorrectlyExecuted3() {
        
        initiateRegs(0, 0x70, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0X1F0, 0x80);
        int i = execute(Opcode.BIT_7_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x30, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0x80, bus.read(0x1F0));
    }
    
    @Test
    public void SET_U3_R8_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 1, 0x80, 0, 0, 0, 0);
        int i = execute(Opcode.SET_0_A, Opcode.SET_1_B, Opcode.SET_7_C);
        
        assertArrayEquals(new int[] {i, 0, 1, 0xF0, 3, 0x80, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void RES_U3_R8_isCorrectlyExecuted() {
        
        initiateRegs(1, 0xF0, 3, 0x80, 0, 0, 0, 0);
        int i = execute(Opcode.RES_0_A, Opcode.RES_1_B, Opcode.RES_7_C);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xF0, 1, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SET_U3_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0X1F0, 0);
        int i = execute(Opcode.SET_0_HLR, Opcode.SET_1_HLR, Opcode.SET_7_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xF0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0x83, bus.read(0x1F0));
    }
    
    @Test
    public void RES_U3_HLR_isCorrectlyExecuted() {
        
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 1, 0xF0);
        bus.write(0X1F0, 0xFF);
        int i = execute(Opcode.RES_2_HLR, Opcode.RES_3_HLR, Opcode.RES_4_HLR);
        
        assertArrayEquals(new int[] {i, 0, 0, 0xF0, 0, 0, 0, 0, 1, 0xF0},
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xE3, bus.read(0x1F0));
    }
    
    //:::::::::::::::::::::::::::::::: MISC ::::::::::::::::::::::::::::::::::::::::
    
    
    @Test
    public void DAA_isCorrectlyExecuted() {
        
        initiateRegs(0x6D, 0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0x73, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted2() {
        
        initiateRegs(0x0F, 0x60, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 9, 0x40, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted3() {
        
        initiateRegs(0x0FF, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0x99, 0x50, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted4() {
        
        initiateRegs(0x0FF, 0xB0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0x65, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted5() {
        
        initiateRegs(0, 0x90, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0x60, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted6() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x80, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted7() {
        
        initiateRegs(0xA0, 0x20, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0x06, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void DAA_isCorrectlyExecuted8() {
        
        initiateRegs(0xA0, 0x0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.DAA);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x90, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SCF_isCorrectlyExecuted() {
        
        initiateRegs(0, 0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SCF);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SCF_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x10, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SCF);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void SCF_isCorrectlyExecuted3() {
        
        initiateRegs(0, 0xF0, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.SCF);
        
        assertArrayEquals(new int[] {i, 0, 0, 0b10010000, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CCF_isCorrectlyExecuted() {
        
        initiateRegs(0, 0x20, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CCF);
        
        assertArrayEquals(new int[] {i, 0, 0, 0x10, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    public void CCF_isCorrectlyExecuted2() {
        
        initiateRegs(0, 0x30, 0, 0, 0, 0, 0, 0);
        int i = execute(Opcode.CCF);
        
        assertArrayEquals(new int[] {i, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                cpu._testGetPcSpAFBCDEHL());
    }
   
}

