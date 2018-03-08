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
        int op = read8(PC);
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
                setReg(extractReg(opcode, 3), read8AtHl());
            } break;
            case LD_A_HLRU: {
                setReg(Reg.A , read8AtHl());
                setReg16(Reg16.HL, reg16(Reg16.HL) + extractHlIncrement(opcode));
            } break;
            case LD_A_N8R: {
                setReg(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
            } break;
            case LD_A_CR: {
                setReg(Reg.A, read8(AddressMap.REGS_START + reg(Reg.C)));
            } break;
            case LD_A_N16R: {
                setReg(Reg.A, read8(read16AfterOpcode()));
            } break;
            case LD_A_BCR: {
                setReg(Reg.A, read8(reg16(Reg16.BC)));
            } break;
            case LD_A_DER: {
                setReg(Reg.A, read8(reg16(Reg16.DE)));
            } break;
            case LD_R8_N8: {
                setReg(extractReg(opcode, 3), read8AfterOpcode());
            } break;
            case LD_R16SP_N16: {
                setReg16SP(extractReg16(opcode), read16AfterOpcode());
            } break;
            case POP_R16: {
                setReg16(extractReg16(opcode), pop16());
            } break;
            case LD_HLR_R8: {
                write8AtHl(reg(extractReg(opcode, 0)));
            } break;
            case LD_HLRU_A: {
                write8(reg16(Reg16.HL), reg(Reg.A));
//                write8AtHl(read8AtHl()+extractHlIncrement(opcode));
                setReg16(Reg16.HL, reg16(Reg16.HL)+extractHlIncrement(opcode));
            } break;
            case LD_N8R_A: {
                write8(AddressMap.REGS_START+read8AfterOpcode(), reg(Reg.A));
            } break;
            case LD_CR_A: {
                write8(AddressMap.REGS_START+reg(Reg.C), reg(Reg.A));
            } break;
            case LD_N16R_A: {
                write8(read16AfterOpcode(), reg(Reg.A));
            } break;
            case LD_BCR_A: {
                write8(reg16(Reg16.BC), reg(Reg.A));
            } break;
            case LD_DER_A: {
                write8(reg16(Reg16.DE), reg(Reg.A));
            } break;
            case LD_HLR_N8: {
                write8AtHl(read8AfterOpcode());
            } break;
            case LD_N16R_SP: {
                write8(read16AfterOpcode(), SP);
            } break;
            case LD_R8_R8: {
                Reg r = extractReg(opcode, 3);
                Reg s = extractReg(opcode, 0);
                if(r!=s)
                    setReg(r, reg(s));
            } break;
            case LD_SP_HL: {
                SP = reg16(Reg16.HL);
            } break;
            case PUSH_R16: {
                push16(reg16(extractReg16(opcode)));
            } break;
            default:
                throw new NullPointerException();
        }
        
        //TODO : PC overflow ???
        nextNonIdleCycle += opcode.cycles;
        PC += opcode.totalBytes;
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
                reg(Reg.A),
                reg(Reg.F),
                reg(Reg.B),
                reg(Reg.C),
                reg(Reg.D),
                reg(Reg.E),
                reg(Reg.H),
                reg(Reg.L)
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
    
    /**
     * Reads 8-bits value at given address from bus
     * @param address address of value
     * @return value at BUS[address]
     */
    private int read8(int address) {
        return bus.read(address);
    }
    
    /**
     * Reads 8-bits value from bus at address given by regs HL
     * @return value at BUS[HL]
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }
    
    /**
     * Reads 8-bits value from bus at address given by reg PC + 1
     * @return value at BUS[PC+1]
     */
    private int read8AfterOpcode() {
        assert PC != 0xFFFF;
        
        return read8(PC + 1);
    }
    
    /**
     * Reads 16-bits value from bus at given address
     * @param address address first 8 bits of value
     * @return 16-bits value made from BUS[address] (8 lsb) and BUS[address+1] (8 msb)
     */
    private int read16(int address) {
        assert address != 0xFFFF;
        
        //little endian
        int lsByte = read8(address);
        int msByte = read8(address + 1);
        return Bits.make16(msByte, lsByte);
    }
    
    /**
     * Reads 16-bits value from bus at address given by reg PC + 1
     * @return 16-bits value made from BUS[PC + 1] (8 lsb) and BUS[PC + 2] (8 msb)
     * @see Cpu#read16(int address)
     */
    private int read16AfterOpcode() {
        assert PC != 0xFFFE;
        assert PC != 0xFFFF;
        
        return read16(PC + 1);
    }
    
    /**
     * Writes given 8-bits value on bus at given address
     * @param address write location
     * @param v value to write
     */
    private void write8(int address, int v) {
        bus.write(address, v);
    }
    
    /**
     * Writes given 16-bits value on bus at given address
     *      First 8 lsb at BUS[address], then 8 msb at BUS[address + 1]
     *      (little endian)
     * @param address write location
     * @param v value to write
     */
    private void write16(int address, int v) {
        assert address != 0xFFFF;
        
        write8(address, Bits.clip(8, v));            //writes 8 lsb first
        write8(address + 1 , Bits.extract(v, 8, 8)); //then 8 msb
    }
    
    /**
     * Writes given 8-bits value on bus at address given by regs HL
     * @param v value to write
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    }
    
    /**
     * Decrements SP by 2, then writes given 16-bits value at address given by new SP value
     * (ie. writes given value at old SP - 2)
     * @param v value to write
     */
    private void push16(int v) {
        SP -= 2;
        SP = Bits.clip(16, SP);
        
        write16(SP, v);
    }
    
    /**
     * Reads 16-bits value from bus at address given by SP, then increments SP by 2
     * @return value at BUS[old SP]
     */
    private int pop16() {
        int value = read16(SP);
        System.out.println(value);
        SP += 2;
        if(SP > 0xFFFF) {
            SP = Bits.clip(16, SP);
        }
        return value;
    }
    
    private int reg(Reg r) {
        return registerFile.get(r);
    }
    
    /**
     * Returns value stored in pair of 8-bits regs
     *      (note : 8 msb in first reg, 8 lsb in second reg)
     * @param r pair of 8-bits regs
     * @return value stored in given 16-bits reg
     */
    private int reg16(Reg16 r) {
        
        //TODO : better method (more concise) than switch ?
        
        int highB = 0, lowB = 0;
        
        switch (r) {
        case AF :
            highB = reg(Reg.A);
            lowB = reg(Reg.F);
            break;
        case BC :
            highB = reg(Reg.B);
            lowB = reg(Reg.C);
            break;
        case DE :
            highB = reg(Reg.D);
            lowB = reg(Reg.E);
            break;
        case HL:
            highB = reg(Reg.H);
            lowB = reg(Reg.L);
            break;
        }
        return Bits.make16(highB, lowB);
    }
    /**
     * sets register to given value
     */
    private void setReg(Reg r, int newV) {
        registerFile.set(r, newV);
    }
    
    /**
     * Puts given 16-bits value in given pair of 8-bits regs
     *      note : 8 msb in first reg, 8 lsb in second reg
     * @param r pair of 8-bits registers
     * @param newV value to store
     */
    private void setReg16(Reg16 r, int newV) {
        int highB = Bits.extract(newV, 8, 8);
        int lowB = Bits.clip(8, newV);
        
        switch (r) {
        case AF :
            setReg(Reg.A, highB);
            setReg(Reg.F, lowB & ((1 << 4)-1)<<4); //takes only 4 msb of lowB (ie. the flags, rest is 0)
            break;
        case BC :
            setReg(Reg.B, highB);
            setReg(Reg.C, lowB);
            break;
        case DE :
            setReg(Reg.D, highB);
            setReg(Reg.E, lowB);
            break;
        case HL:
            setReg(Reg.H, highB);
            setReg(Reg.L, lowB);
            break;
        }
    }
    
    /**
     * Puts given 16-bits value in given pair of 8-bits reas
     *      If given 16-bits reg is AF, puts given value in SP instead
     *      @see Cpu#setReg16(Reg16 r, int newV)
     * @param r pair of 8-bits regs
     * @param newV value to store
     */
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
            //TODO : what about if 0b11 is used to represent SP ?
        default:
            throw new IllegalArgumentException();
        }
    }
    
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }
    
    protected void reset() {
        for(Reg reg : Reg.values()) {
            setReg(reg, 0);
        }
        SP = 0;
        PC = 0;
    }
    // TODO remove before commit
    protected void setAllRegs(int a, int f, int b, int c, int d, int e, int h, int l) {
        setReg(Reg.A, a);
        setReg(Reg.F, f);
        setReg(Reg.B, b);
        setReg(Reg.C, c);
        setReg(Reg.D, d);
        setReg(Reg.E, e);
        setReg(Reg.H, h);
        setReg(Reg.L, l);
    }
    //TODO remove before commit
    protected void setAllRegs16(int af, int bc, int de, int hl) {
        setReg16(Reg16.AF, af);
        setReg16(Reg16.BC, bc);
        setReg16(Reg16.DE, de);
        setReg16(Reg16.HL, hl);
    }
    
    //TODO remove before commit
    protected void setSP(int sp) {
        SP = sp;
    }
}