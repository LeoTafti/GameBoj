/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {

    private enum Reg implements Register { A, F, B, C, D, E, H, L }
    private enum Reg16 implements Register { AF, BC, DE, HL}
    
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
    private int SP = 0, PC = 0;
    
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
        
        //TODO : not sure about that
        int op = bus.read(PC);
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
                registerFile.set(extractReg(opcode, 3), read8AtHl());
            } break;
            case LD_A_HLRU: {
                registerFile.set(Reg.A , read8AtHl());
                write8AtHl(read8AtHl()+extractHlIncrement(opcode));
            } break;
            case LD_A_N8R: {
                registerFile.set(Reg.A, read8AfterOpcode());
            } break;
            case LD_A_CR: {
                registerFile.set(Reg.A, read8(AddressMap.REGS_START + registerFile.get(Reg.C)));
            } break;
            case LD_A_N16R: {
                registerFile.set(Reg.A, read8(read16AfterOpcode()));
            } break;
            case LD_A_BCR: {
                registerFile.set(Reg.A, read8(reg16(Reg16.BC)));
            } break;
            case LD_A_DER: {
                registerFile.set(Reg.A, read8(reg16(Reg16.DE)));
            } break;
            case LD_R8_N8: {
                registerFile.set(extractReg(opcode, 3), read8AfterOpcode());
            } break;
            case LD_R16SP_N16: {
                setReg16(extractReg16(opcode), read8AfterOpcode());
            } break;
            case POP_R16: {
                setReg16(extractReg16(opcode), pop16());
            } break;
            case LD_HLR_R8: {
                write8(reg16(Reg16.HL), read8AfterOpcode());
            } break;
            case LD_HLRU_A: {
                write8(reg16(Reg16.HL), registerFile.get(Reg.A));
                write8AtHl(read8AtHl()+extractHlIncrement(opcode));
            } break;
            case LD_N8R_A: {
                write8(AddressMap.REGS_START+read8AfterOpcode(), registerFile.get(Reg.A));
            } break;
            case LD_CR_A: {
                write8(AddressMap.REGS_START+registerFile.get(Reg.C), registerFile.get(Reg.A));
            } break;
            case LD_N16R_A: {
                write8(read16AfterOpcode(), registerFile.get(Reg.A));
            } break;
            case LD_BCR_A: {
                write8(reg16(Reg16.BC), registerFile.get(Reg.A));
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
        PC += opcode.totalBytes; //TODO : not sure of that
    }

    
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        
    }

    /**
     * creates table containing the PcSpAFBCDEHL 
     *  register's information
     * @return
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] array = {PC,
                SP,
                registerFile.get(Reg.A),
                registerFile.get(Reg.F),
                registerFile.get(Reg.B),
                registerFile.get(Reg.C),
                registerFile.get(Reg.D),
                registerFile.get(Reg.E),
                registerFile.get(Reg.H),
                registerFile.get(Reg.L)
        };
        return array;
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
        return bus.read(reg16(Reg16.HL));
    }
    
    private int read8AfterOpcode() {
        return bus.read(PC + 1);
    }
    
    private int read16(int address) {
        //little endian
        int lsByte = bus.read(address);
        int msByte = bus.read(address + 1);
        return Bits.make16(msByte, lsByte);
    }
    
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }
    
    private void write8(int address, int v) {
        bus.write(address, v);
    }
    
    private void write16(int address, int v) {
        bus.write(address, Bits.clip(8, v));            //writes 8 lsb first
        bus.write(address + 1 , Bits.extract(v, 8, 8)); //then 8 msb
    }
    
    private void write8AtHl(int v) {
        bus.write(reg16(Reg16.HL), v);
    }
    
    private void push16(int v) {
        SP -= 2;
        write16(SP, v);
    }
    
    private int pop16() {
        int value = read16(SP);
        SP += 2;
        return value;
    }
    
    private int reg16(Reg16 r) {
        
        //TODO : better method (more concise) than switch ?
        
        int highB = 0, lowB = 0;
        
        switch (r) {
        case AF :
            highB = registerFile.get(Reg.A);
            lowB = registerFile.get(Reg.F);
            break;
        case BC :
            highB = registerFile.get(Reg.B);
            lowB = registerFile.get(Reg.C);
            break;
        case DE :
            highB = registerFile.get(Reg.D);
            lowB = registerFile.get(Reg.E);
            break;
        case HL:
            highB = registerFile.get(Reg.H);
            lowB = registerFile.get(Reg.L);
            break;
        }
        return Bits.make16(highB, lowB);
    }
    
    private void setReg16(Reg16 r, int newV) {
        int highB = Bits.extract(newV, 8, 8);
        int lowB = Bits.clip(newV, 8);
        
        switch (r) {
        case AF :
            registerFile.set(Reg.A, highB);
            registerFile.set(Reg.F, 0);
            break;
        case BC :
            registerFile.set(Reg.B, highB);
            registerFile.set(Reg.C, lowB);
            break;
        case DE :
            registerFile.set(Reg.D, highB);
            registerFile.set(Reg.E, lowB);
            break;
        case HL:
            registerFile.set(Reg.H, highB);
            registerFile.set(Reg.L, lowB);
            break;
        }
    }
    
    private void setReg16SP(Reg16 r, int newV) {
        if(r == Reg16.AF) {
            SP = newV;
        }
        setReg16(r, newV);
    }
    
    private Reg extractReg(Opcode opcode, int startBit) {
        int regCode = Bits.extract(opcode.encoding, startBit, 3);
        switch (regCode) {
        case 0b000:
            return Reg.B;
        case 0b001:
            return Reg.C;
        case 0b010:
            return Reg.D;
        case 0b011:
            return Reg.E;
        case 0b100:
            return Reg.H;
        case 0b101:
            return Reg.L;
        case 0b111:
            return Reg.A;
            
        default:
            throw new IllegalArgumentException();
        }
    }
    
    private Reg16 extractReg16(Opcode opcode) {
        int regsCode = Bits.extract(opcode.encoding, 4, 2);
        switch (regsCode) {
        case 0b00:
            return Reg16.BC;
        case 0b01:
            return Reg16.DE;
        case 0b10:
            return Reg16.HL;
        case 0b11:
            return Reg16.AF;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }
}
