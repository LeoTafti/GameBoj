/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {

    private enum Reg implements Register { A, F, B, C, D, E, H, L }
    private enum Reg16 implements Register { AF, BC, DE, HL, PC, SP }
    
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
//    private RegisterFile<Reg16> register16File = new RegisterFile<>(Reg16.values());
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
   
    private long nextNonIdleCycle;
    
    private Bus bus;
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }
    
    @Override
    public void cycle(long cycle) {
        
        if(cycle != nextNonIdleCycle) return;
//        int nextProgramAddress = register16File.get(Reg16.PC);
        
        int nextProgramAddress = reg16(Reg16.PC);
        int op = bus.read(nextProgramAddress);
        dispatch(op);
        
    };
    
    public void dispatch(int op) {
        //TODO : may not be logical at all, think about what it really is supposed to do !
        Opcode opcode = DIRECT_OPCODE_TABLE[op];
        //TODO : implement each case
        //TODO : take care of op not beeing associated with an instruction
        switch(opcode.family) {
            case NOP: {
            } break;
            case LD_R8_HLR: {
            } break;
            case LD_A_HLRU: {
            } break;
            case LD_A_N8R: {
            } break;
            case LD_A_CR: {
            } break;
            case LD_A_N16R: {
            } break;
            case LD_A_BCR: {
            } break;
            case LD_A_DER: {
            } break;
            case LD_R8_N8: {
            } break;
            case LD_R16SP_N16: {
            } break;
            case POP_R16: {
            } break;
            case LD_HLR_R8: {
            } break;
            case LD_HLRU_A: {
            } break;
            case LD_N8R_A: {
            } break;
            case LD_CR_A: {
            } break;
            case LD_N16R_A: {
            } break;
            case LD_BCR_A: {
            } break;
            case LD_DER_A: {
            } break;
            case LD_HLR_N8: {
            } break;
            case LD_N16R_SP: {
            } break;
            case LD_R8_R8: {
            } break;
            case LD_SP_HL: {
            } break;
            case PUSH_R16: {
            } break;
//            default:
//                throw new NullPointerException();
        }
        
        //TODO : update PC value, wait for x cycles, etc (cf guidlines 2.5.1.3)
        nextNonIdleCycle += opcode.cycles;
//        register16File.set(Reg16.PC, newValue);
    }

    
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        
    }

    public int[] _testGetPcSpAFBCDEHL() {
        
        return new int[0];
    }
    
    /**
     * Creates table of Opcodes of given kind 
     *      indexed by opcodes encodings
     * @param kind DIRECT or PREFIXED
     * @return table of 256 opcodes
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        
        Opcode[] table = new Opcode[256];
        for (Opcode o: Opcode.values()) {
            if(o.kind == kind) {
                table[o.encoding] = o;
            }
        }
        return table;
    }
    
    private int read8(int address) {
        return bus.read(address);
    }
    
    private int read8AtHl() {
//        return bus.read(register16File.get(Reg16.HL));
        return bus.read(reg16(Reg16.HL));
    }
    
    private int read8AfterOpcode() {
//        return bus.read(register16File.get(Reg16.PC) + 1);
        return bus.read(reg16(Reg16.PC) + 1);
    }
    
    private int read16(int address) {
        //little endian
        int lsByte = bus.read(address);
        int msByte = bus.read(address + 1);
        return Bits.make16(msByte, lsByte);
    }
    
    private int read16AfterOpcode() {
//        return read16(register16File.get(Reg16.PC) + 1);
        return read16(reg16(Reg16.PC));
    }
    
    private void write8(int address, int v) {
        bus.write(address, v);
    }
    
    private void write16(int address, int v) {
        bus.write(address, Bits.clip(8, v));            //writes 8 lsb first
        bus.write(address + 1 , Bits.extract(v, 8, 8)); //then 8 msb
    }
    
    private void write8AtHl(int v) {
//        bus.write(register16File.get(Reg16.HL), v);
        bus.write(reg16(Reg16.HL), v);
    }
    
    private void push16(int v) {
//        int newAddress = register16File.get(Reg16.SP) - 2;
//        register16File.set(Reg16.SP, newAddress);
        int newAddress = reg16(Reg16.SP) - 2;
        
        //TODO : see comment in setReg16SP(), eventually modify param Reg16.AF to Reg16.SP
        setReg16SP(Reg16.AF, newAddress);
        write16(newAddress, v);
    }
    
    private int pop16() {
//        int address = register16File.get(Reg16.SP);
//        int value = read16(address);
//        register16File.set(Reg16.SP, address - 2);
        int address = reg16(Reg16.SP);
        int value = read16(address);
        
        //TODO : same remark as above in push16()
        setReg16SP(Reg16.AF, address - 2);
        return value;
    }
    
    private int reg16(Reg16 r) {
        switch (r) {
        case AF :
            return Bits.make16(registerFile.get(Reg.A), registerFile.get(Reg.F));
        case BC :
            return Bits.make16(registerFile.get(Reg.B), registerFile.get(Reg.C));
        case DE :
            return Bits.make16(registerFile.get(Reg.D), registerFile.get(Reg.E));
        case HL:
            return Bits.make16(registerFile.get(Reg.H), registerFile.get(Reg.L));
        case PC :
            
        case SP :
            
        }
    }
    
    private void setReg16(Reg16 r, int newV) {
        
    }
    
    private void setReg16SP(Reg16 r, int newV) {
        
    }
}
