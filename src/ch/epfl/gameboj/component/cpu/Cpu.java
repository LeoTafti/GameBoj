/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;

public final class Cpu implements Component, Clocked {

    private enum Reg implements Register { A, F, B, C, D, E, H, L }
    private enum Reg16 implements Register { AF, BC, DE, HL}
    
    private enum FlagSrc { V0, V1, ALU, CPU }
    
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
    private int SP = 0, PC = 0;
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.PREFIXED);
   
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
        Opcode opcode = getOpcode();
        dispatch(opcode);
        
        //TODO : PC overflow ???
        nextNonIdleCycle += opcode.cycles;
        PC += opcode.totalBytes;
        
    };
    
    /**
     * Given an opcode executes corresponding operation
     * @param opcode opcode to execute
     * @throws NullPointerException if given opcode doesn't correspond
     *      to any of the one's handled here
     */
    private void dispatch(Opcode opcode) {
        switch(opcode.family) {
            case NOP: {
            } break;
            case LD_R8_HLR: { 
                setReg(extractReg(opcode, 3), read8AtHl());
            } break;
            case LD_A_HLRU: {
                setReg(Reg.A , read8AtHl());
                setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
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
                setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL)+extractHlIncrement(opcode)));
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
                write16(read16AfterOpcode(), SP);
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
            
            // Add
            case ADD_A_R8: {
                int vf = Alu.add(
                        reg(Reg.A),
                        reg(extractReg(opcode, 0)),
                        getInitialCarry(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                setRegFromAlu(Reg.A, vf);
                
            } break;
            case ADD_A_N8: {
                int vf = Alu.add(
                        reg(Reg.A),
                        read8AfterOpcode(),
                        getInitialCarry(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                setRegFromAlu(Reg.A, vf);
            } break;
            case ADD_A_HLR: {
                int vf = Alu.add(
                        reg(Reg.A),
                        read8AtHl(),
                        getInitialCarry(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                setRegFromAlu(Reg.A, vf);
            } break;
            case INC_R8: {
                Reg r8 = extractReg(opcode, 3);
                int vf = Alu.add(reg(r8), 1);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
                setRegFromAlu(r8, vf);
            } break;
            case INC_HLR: {
                int vf = Alu.add(read8AtHl(), 1);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
                write8AtHl(Alu.unpackValue(vf));
            } break;
            case INC_R16SP: {
                Reg16 r16 = extractReg16(opcode);
                int vf = Alu.add16H(
                        reg16SP(r16),
                        1);
                combineAluFlags(vf, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU);
                setReg16SP(r16, Alu.unpackValue(vf));
            } break;
            case ADD_HL_R16SP: {
                int vf = Alu.add16H(
                        reg16(Reg16.HL),
                        reg16SP(extractReg16(opcode)));
                combineAluFlags(vf, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                setReg16(Reg16.HL, Alu.unpackValue(vf));
            } break;
            case LD_HLSP_S8: {
                int res = addSP_e8();
                if(Bits.test(opcode.encoding, 4))
                    setReg16(Reg16.HL, res);
                else {
                    SP = res;
                }
            } break;

            // Subtract
            case SUB_A_R8: {
                int vf = Alu.sub(
                        reg(Reg.A),
                        reg(extractReg(opcode, 0)),
                        getInitialBorrow(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
                setRegFromAlu(Reg.A, vf);
            } break;
            case SUB_A_N8: {
                int vf = Alu.sub(
                        reg(Reg.A),
                        read8AfterOpcode(),
                        getInitialBorrow(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
                setRegFromAlu(Reg.A, vf);
            } break;
            case SUB_A_HLR: {
                int vf = Alu.sub(
                        reg(Reg.A),
                        read8AtHl(),
                        getInitialBorrow(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
                setRegFromAlu(Reg.A, vf);
            } break;
            case DEC_R8: {
                Reg r8 = extractReg(opcode, 3);
                int vf = Alu.sub(reg(r8), 1);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
                setRegFromAlu(r8, vf);
            } break;
            case DEC_HLR: {
                int vf = Alu.sub(read8AtHl(), 1);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
                write8AtHl(Alu.unpackValue(vf));
            } break;
          //TODO : for all CP , since same as corresponding SUB, just ignoring result
            //      write a method instead of copying / pasting ?
            case CP_A_R8: {
                int vf = Alu.sub(
                        reg(Reg.A),
                        reg(extractReg(opcode, 0)),
                        getInitialBorrow(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
            } break;
            case CP_A_N8: {
                int vf = Alu.sub(
                        reg(Reg.A),
                        read8AfterOpcode(),
                        getInitialBorrow(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
            } break;
            case CP_A_HLR: {
                int vf = Alu.sub(
                        reg(Reg.A),
                        read8AtHl(),
                        getInitialBorrow(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.ALU);
            } break;
            case DEC_R16SP: {
                Reg16 r16 = extractReg16(opcode);
                
                //TODO : modify ! sub only works on 8 bit values, will throw an exception
                
                int vf = Alu.sub(
                        reg16SP(r16),
                        1);
                combineAluFlags(vf, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU);
                setReg16SP(r16, Alu.unpackValue(vf));
            } break;

            // And, or, xor, complement
            case AND_A_N8: {
                int vf = Alu.and(reg(Reg.A),
                        read8AfterOpcode());
                setRegFlags(Reg.A, vf);
            } break;
            case AND_A_R8: {
                int vf = Alu.and(reg(Reg.A),
                        reg(extractReg(opcode, 0)));
                setRegFlags(Reg.A, vf);
            } break;
            case AND_A_HLR: {
                int vf = Alu.and(reg(Reg.A),
                        read8AtHl());
                setRegFlags(Reg.A, vf);
            } break;
            case OR_A_R8: {
                int vf = Alu.or(reg(Reg.A),
                        reg(extractReg(opcode, 0)));
                setRegFlags(Reg.A, vf);
            } break;
            case OR_A_N8: {
                int vf = Alu.or(reg(Reg.A),
                        read8AfterOpcode());
                setRegFlags(Reg.A, vf);
            } break;
            case OR_A_HLR: {
                int vf = Alu.or(reg(Reg.A),
                        read8AtHl());
                setRegFlags(Reg.A, vf);
            } break;
            case XOR_A_R8: {
                int vf = Alu.xor(reg(Reg.A),
                        reg(extractReg(opcode, 0)));
                setRegFlags(Reg.A, vf);
            } break;
            case XOR_A_N8: {
                int vf = Alu.or(reg(Reg.A),
                        read8AfterOpcode());
                setRegFlags(Reg.A, vf);
            } break;
            case XOR_A_HLR: {
                int vf = Alu.xor(reg(Reg.A),
                        read8AtHl());
                setRegFlags(Reg.A, vf);
            } break;
            case CPL: {
                setReg(Reg.A, Bits.complement8(reg(Reg.A)));
                combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
            } break;

            // Rotate, shift
            case ROTCA: {
                setRegFlags(Reg.A, Alu.rotate(extractRotDir(opcode), reg(Reg.A)));
            } break;
            case ROTA: {
                setRegFlags(Reg.A, Alu.rotate(extractRotDir(opcode), reg(Reg.A), getFlag(Flag.C)));              
            } break;
            case ROTC_R8: {
                setRegFlags(extractReg(opcode, 0), 
                        Alu.rotate(extractRotDir(opcode), reg(extractReg(opcode, 0))));
            } break;
            case ROT_R8: {
                setRegFlags(extractReg(opcode, 0),
                        Alu.rotate(extractRotDir(opcode), reg(extractReg(opcode, 0)), getFlag(Flag.C)));
            } break;
            case ROTC_HLR: {
                write8AtHlAndSetFlags(Alu.rotate(extractRotDir(opcode), read8AtHl()));
            } break;
            case ROT_HLR: {
                write8AtHlAndSetFlags(Alu.rotate(extractRotDir(opcode), read8AtHl(), getFlag(Flag.C)));
            } break;
            case SWAP_R8: {
                setRegFlags(extractReg(opcode, 0), Alu.swap(reg(extractReg(opcode, 0))));    
            } break;
            case SWAP_HLR: {
                write8AtHlAndSetFlags(Alu.swap(read8AtHl()));
            } break;
            case SLA_R8: {
                setRegFlags(extractReg(opcode, 0), Alu.shiftLeft(reg(extractReg(opcode, 0))));
            } break;
            case SRA_R8: {
                setRegFlags(extractReg(opcode, 0), Alu.shiftRightA(reg(extractReg(opcode, 0))));
            } break;
            case SRL_R8: {
                setRegFlags(extractReg(opcode, 0), Alu.shiftRightL(reg(extractReg(opcode, 0))));
            } break;
            case SLA_HLR: {
                write8AtHlAndSetFlags(Alu.shiftLeft(read8AtHl()));
            } break;
            case SRA_HLR: {
                write8AtHlAndSetFlags(Alu.shiftRightA(read8AtHl()));
            } break;
            case SRL_HLR: {
                write8AtHlAndSetFlags(Alu.shiftRightL(read8AtHl()));
            } break;

            // Bit test and set
            case BIT_U3_R8: {
                combineAluFlags( Alu.testBit(reg(extractReg(opcode, 0)),  extractBitIndex(opcode)),
                        FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
            } break;
            case BIT_U3_HLR: {
                combineAluFlags( Alu.testBit(read8AtHl(),  extractBitIndex(opcode)),
                        FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
            } break;
            case CHG_U3_R8: {
                setReg(extractReg(opcode, 0),
                        Bits.set(reg(extractReg(opcode, 0)), extractBitIndex(opcode), extractBitValue(opcode)));
            } break;
            case CHG_U3_HLR: {
                write8AtHl(Bits.set(read8AtHl(), extractBitIndex(opcode), extractBitValue(opcode)));
            } break;

            // Misc. ALU
            case DAA: {
                int vf = Alu.bcdAdjust(reg(Reg.A), getFlag(Flag.N), getFlag(Flag.H), getFlag(Flag.C));
                setRegFromAlu(Reg.A, vf);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
            } break;
            case SCCF: { //method setFlag better??
                setReg(Reg.F, Bits.set(reg(Reg.F), 4, getCFlagSCCF(opcode)));
                setReg(Reg.F, Bits.set(reg(Reg.F), 5, false));
                setReg(Reg.F, Bits.set(reg(Reg.F), 6, false));
            } break;
            
            default:
                throw new NullPointerException();
        }
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
     * Reads 8-bit value at given address from bus
     * @param address address of value
     * @return value at BUS[address]
     */
    private int read8(int address) {
        return bus.read(address);
    }
    
    /**
     * Reads 8-bit value from bus at address given by regs HL
     * @return value at BUS[HL]
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }
    
    /**
     * Reads 8-bit value from bus at address given by reg PC + 1
     * @return value at BUS[PC+1]
     */
    private int read8AfterOpcode() {
        assert PC != 0xFFFF;
        
        return read8(PC + 1);
    }
    
    /**
     * Reads 16-bit value from bus at given address
     * @param address address first 8 bits of value
     * @return 16-bit value made from BUS[address] (8 lsb) and BUS[address+1] (8 msb)
     */
    private int read16(int address) {
        assert address != 0xFFFF;
        
        //little endian
        int lsByte = read8(address);
        int msByte = read8(address + 1);
        return Bits.make16(msByte, lsByte);
    }
    
    /**
     * Reads 16-bit value from bus at address given by reg PC + 1
     * @return 16-bit value made from BUS[PC + 1] (8 lsb) and BUS[PC + 2] (8 msb)
     * @see Cpu#read16(int address)
     */
    private int read16AfterOpcode() {
        assert PC != 0xFFFE;
        assert PC != 0xFFFF;
        
        return read16(PC + 1);
    }
    
    /**
     * Writes given 8-bit value on bus at given address
     * @param address write location
     * @param v value to write
     */
    private void write8(int address, int v) {
        bus.write(address, v);
    }
    
    /**
     * Writes given 16-bit value on bus at given address
     *      First 8 lsb at BUS[address], then 8 msb at BUS[address + 1]
     *      (little endian)
     * @param address write location
     * @param v value to write
     * @throws IllegalArgumentException if address or v aren't 16-bit values
     */
    private void write16(int address, int v) {
        assert address != 0xFFFF;
        
        Preconditions.checkBits16(address);
        Preconditions.checkBits16(v);
        
        write8(address, Bits.clip(8, v));            //writes 8 lsb first
        write8(address + 1 , Bits.extract(v, 8, 8)); //then 8 msb
    }
    
    /**
     * Writes given 8-bit value on bus at address given by regs HL
     * @param v value to write
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    }
    
    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }
    

    /**
     * Returns value stored in given reg
     * @param r register
     * @return value store in register
     */
    private int reg(Reg r) {
        return registerFile.get(r);
    }
    
    /**
     * Returns value stored in pair of 8-bit regs
     *      (note : 8 msb in first reg, 8 lsb in second reg)
     * @param r pair of 8-bit regs
     * @return value stored in given 16-bit reg
     */
    private int reg16(Reg16 r) {
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
    
    private int reg16SP(Reg16 r) {
        if(r == Reg16.AF) {
            return SP;
        }
        return reg16(r);
    }
    
    private boolean flagValue(int vf, FlagSrc flagSrc, Flag flag) {
        switch(flagSrc) {
        case V0:
           return false;
        case V1:
           return true;
        case ALU:
            return Bits.test(vf, flag.index());
        case CPU:
            return Bits.test(reg(Reg.F), flag.index());
        default:
                throw new IllegalArgumentException("Unknown FlagSrc");
        }
    }

    /**
     * 
     * @param f Z or N or H or C
     * @return **F**-flag value as boolean
     */
    private boolean getFlag(Flag f) {
        return flagValue(reg(Reg.F), FlagSrc.CPU, f);
        
        //TODO : why give reg(Reg.F) as vf ???
    }
    

    /**
     * Gets (eventual) initial carry from opcode encoding and C Flag
     * @param opcode opcode of ADD operation
     * @return initial carry (true for 1, false for 0)
     */
    private boolean getInitialCarry(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) && Bits.test(reg(Reg.F), 4);
    }
    

    /**
     * Gets (eventual) initial borrow from opcode encoding and C Flag
     * @param opcode opcode of SUB operation
     * @return initial borrow (true for 1, false for 0)
     */
    private boolean getInitialBorrow(Opcode opcode) {
        return getInitialCarry(opcode);
    }
    

    private boolean getCFlagSCCF(Opcode opcode) {
        return !Bits.test(opcode.encoding, 3) && Bits.test(reg(Reg.F), 4);
    }
    

    /**
     * Gets opcode of next op, for direct and prefixed operations
     * @return opcode of next operation
     */
    private Opcode getOpcode() {
        int op = read8(PC);
        if(op == 0xCB) {
            op = read8(PC+1);
            return PREFIXED_OPCODE_TABLE[op];
        }
        return DIRECT_OPCODE_TABLE[op];
    }
    

    /**
     * Sets given reg with given value
     * @param r register in which to put value
     * @param newV new value to store
     */
    private void setReg(Reg r, int newV) {
        registerFile.set(r, newV);
    }
    
    private void setRegFromAlu(Reg r, int vf) {
        setReg(r, Alu.unpackValue(vf));
    }
    

    /**
     * Puts given 16-bit value in given pair of 8-bit regs
     *      note : 8 msb in first reg, 8 lsb in second reg
     * @param r pair of 8-bit registers
     * @param newV value to store
     * @throws IllegalArgumentException if newV isn't a 16-bit value
     */
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        
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
     * Puts given 16-bit value in given pair of 8-bit reas
     *      If given 16-bit reg is AF, puts given value in SP instead
     *      @see Cpu#setReg16(Reg16 r, int newV)
     * @param r pair of 8-bit regs
     * @param newV value to store
     * @throws IllegalArgumentException if newV isn't a 16-bit value
     */
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if(r == Reg16.AF) {
            SP = newV;
        }
        else {
            setReg16(r, newV);
        }
    }
    
    private void setFlags(int valueFlags) {
        setReg(Reg.F, Alu.unpackFlags(valueFlags));
    }
    

    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }
    

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        boolean newZ, newN, newH, newC;
        
        newZ = flagValue(vf, z, Flag.Z);
        newN = flagValue(vf, n, Flag.N);
        newH = flagValue(vf, h, Flag.H);
        newC = flagValue(vf, c, Flag.C);
        
        setReg(Reg.F, Alu.maskZNHC(newZ, newN, newH, newC));
    }
    

    /**
     * Extracts 8-bit reg identity form opcode encoding, from given bit index
     * @param opcode opcode in which reg is encoded
     * @param startBit index from which to extract reg id
     * @return Reg value
     */
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
   
    /**
     * Extracts 16-bit reg identity from opcode encoding
     * @param opcode opcode in which reg is encoded
     * @return Reg value
     */
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

    /**
     * Extracts and return HL increment from opcode encoding
     * @param opcode opcode in which HL increment is encoded
     * @return HL increment, -1 or +1
     */
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }

    private RotDir extractRotDir(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }
    

    private int extractBitIndex(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3); 
    }
    

    private boolean extractBitValue(Opcode opcode) {
        return Bits.test(opcode.encoding, 6);
    }

    /**
     * Decrements SP by 2, then writes given 16-bit value at address given by new SP value
     * (ie. writes given value at old SP - 2)
     * @param v value to write
     */
    private void push16(int v) {
        SP -= 2;
        SP = Bits.clip(16, SP);
        
        write16(SP, v);
    }

    /**
     * Reads 16-bit value from bus at address given by SP, then increments SP by 2
     * @return value at BUS[old SP]
     */
    private int pop16() {
        int value = read16(SP);
        SP += 2;
        SP = Bits.clip(16, SP);
        return value;
    }

    /**
     * Reads 8-bit value after opcode (signed !) adds it with SP value, takes care of Flags
     *      and returns the result (!)
     * @return result of add16L with 8-bit value and SP value
     */
    private int addSP_e8() {
        // TODO TODO why clip 16 for single byte?
        int val = Bits.clip(16, Bits.signExtend8(read8AfterOpcode()));
        int valueFlags = Alu.add16L(SP, val);
        //TODO : 2.2.1.6, step 3 : does it mean that we must set SP here or just use its value ?
        //probably just use value, since then decide where to store result       
//        SP = Alu.unpackValue(valueFlags);
        
        combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        
        return Alu.unpackValue(valueFlags);
        //TODO : remember to STORE result in either SP or HL, cf. 2.2.1.6 step 5 !
        
        //NOTE that we could also pass opcode as argument, and take care of storing the result in here
        //TODO : decide what's the best way
        
//        if(Bits.test(opcode.encoding, 4)) {
//            setReg16(Reg16.HL, Alu.unpackValue(valueFlags));
//        }   
//        else {
//            SP = Alu.unpackValue(valueFlags);
//        }
        
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