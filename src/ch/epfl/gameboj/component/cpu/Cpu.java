/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;

public final class Cpu implements Component, Clocked {

    private enum Reg implements Register { A, F, B, C, D, E, H, L }
    private enum Reg16 implements Register { AF, BC, DE, HL}
    
    private enum FlagSrc { V0, V1, ALU, CPU }
    
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
      }
    
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
    private int SP = 0, PC = 0;
    private boolean IME = false;
    private int IE = 0, IF = 0;
    
    private static final int INTERRUPT_HANDLING_CYCLES = 5;
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.PREFIXED);
   
    private long nextNonIdleCycle;
    
    private Bus bus;
    
    private Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }
    
    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && pendingInterrupt()) {
            nextNonIdleCycle = cycle;
        }
        else if(cycle != nextNonIdleCycle) {
            return;
        }

//        System.out.println("Calling really cycle with cycle : " + cycle);
        reallyCycle();
    };
    
    private void reallyCycle() {
        if(IME == true && pendingInterrupt()) {
            handleInterrupt();
        }
        else {
            Opcode opcode = getOpcode();
            dispatch(opcode);

            nextNonIdleCycle += opcode.cycles;
        }
    }
    
    private void handleInterrupt() {
        IME = false;
        
        int interruptID = Integer.numberOfTrailingZeros(IE & IF);
        
        IF = Bits.set(IF, interruptID, false);
       
        push16(PC);
        
        PC = AddressMap.INTERRUPTS[interruptID];
        
        nextNonIdleCycle += INTERRUPT_HANDLING_CYCLES;
        
    }

    private boolean pendingInterrupt() {
        return (IE & IF) != 0;
    }
    
    /**
     * Given an opcode executes corresponding operation
     * @param opcode opcode to execute
     * @throws NullPointerException if given opcode doesn't correspond
     *      to any of the one's handled here
     */
    private void dispatch(Opcode opcode) {
        
        int nextPC = Bits.clip(16, PC + opcode.totalBytes);

//        System.out.println("-----------Entering dispatch -----------");
//        System.out.println("PC = " + PC);
//        System.out.println("opcode name : " + opcode.name());
//        System.out.println("Bus[HL] : " + read8AtHl());
//        System.out.println(Arrays.toString(_testGetPcSpAFBCDEHL()));
        
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

                setReg16SP(r16, Bits.clip(16, reg16SP(r16)-1));
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
                int vf = Alu.xor(reg(Reg.A),
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
                int vf = Alu.rotate(extractRotDir(opcode), reg(Reg.A));
                setRegFromAlu(Reg.A, vf);
                combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            } break;
            case ROTA: {
                int vf = Alu.rotate(extractRotDir(opcode), reg(Reg.A), getFlag(Flag.C));
                setRegFromAlu(Reg.A, vf);
                combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
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
            case SCCF: {
                if(getCFlagSCCF(opcode)) {
                    combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V1);
                }
                else {
                    combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
                }
            } break;
            
         // Jumps
            case JP_HL: {
                nextPC = reg16(Reg16.HL);
            } break;
            case JP_N16: {
                nextPC = read16AfterOpcode();
            } break;
            case JP_CC_N16: {
                if(evaluateCondition(opcode)) {
                    nextPC = read16AfterOpcode();
                    nextNonIdleCycle += opcode.additionalCycles;
                }
            } break;
            case JR_E8: {
                nextPC = add16_E8(nextPC);
//                nextPC += Bits.clip(16, Bits.signExtend8(read8AfterOpcode()));
//                nextPC = Bits.clip(16, nextPC);
            } break;
            case JR_CC_E8: {
                if(evaluateCondition(opcode)) {
                    
//                    System.out.println("------" +
//                            "PC = " + nextPC + " ; e8 = " + Bits.signExtend8(read8AfterOpcode()) +
//                            " ; condition = " + Bits.extract(opcode.encoding, 3, 2) + " ; flag z = " + getFlag(Flag.Z));
//                   
//                    
                    nextPC = add16_E8(nextPC);
//                   
//                    System.out.println("------" + "next PC = " + nextPC);

                    
//                    nextPC += Bits.clip(16, Bits.signExtend8(read8AfterOpcode()));
//                    nextPC = Bits.clip(16, nextPC);
                    nextNonIdleCycle += opcode.additionalCycles;
                }
            } break;

            // Calls and returns
            case CALL_N16: {
                push16(nextPC);
                nextPC = read16AfterOpcode();
            } break;
            case CALL_CC_N16: {
                if(evaluateCondition(opcode)) {
                    push16(nextPC);
                    nextPC = read16AfterOpcode();
                    nextNonIdleCycle += opcode.additionalCycles;
                }
            } break;
            case RST_U3: {
                push16(nextPC);
                nextPC = 8*Bits.extract(opcode.encoding, 3, 3);
            } break;
            case RET: {
                nextPC = pop16();
            } break;
            case RET_CC: {
                if(evaluateCondition(opcode)) {
                    nextPC = pop16();
                    nextNonIdleCycle += opcode.additionalCycles;
                }
            } break;

            // Interrupts
            case EDI: {
                if(Bits.test(opcode.encoding, 3)) {
                    IME = true;
                }
                else IME = false;
            } break;
            case RETI: {
                IME = true;
                nextPC = pop16();
            } break;

            // Misc control
            case HALT: {
                nextNonIdleCycle = Long.MAX_VALUE;
            } break;
            case STOP:
              throw new Error("STOP is not implemented");
              
            default:
                throw new NullPointerException();
        }
//        PC = Bits.clip(16, nextPC);
        PC = nextPC;
    }

    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if(address == AddressMap.REG_IE)
            return IE;
        else if (address == AddressMap.REG_IF)
            return IF;
        else if(address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END)
            return highRam.read(address-AddressMap.HIGH_RAM_START);
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if(address == AddressMap.REG_IE)
            IE = data;
        else if (address == AddressMap.REG_IF)
            IF = data;
        else if(address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END)
            highRam.write(address-AddressMap.HIGH_RAM_START, data);
    }

    /**
     * creates table containing the PcSpAFBCDEHL 
     *  register's information, for debugging/testing purposes
     * @return array of all register values
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
    
    /**
     * Returns value stored in pair of 8-bit regs, or 16-bit reg SP if given r is Reg16.AF
     * @param r pair of 8-bit regs
     * @return value store in given 16-bit reg
     * @see Cpu#reg16(Reg16 r)
     */
    private int reg16SP(Reg16 r) {
        if(r == Reg16.AF) {
            return SP;
        }
        return reg16(r);
    }
    

    /**
     * Gets (eventual) initial carry from opcode encoding and C Flag
     * @param opcode opcode of ADC operation
     * @return initial carry (true for 1, false for 0)
     */
    private boolean getInitialCarry(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) && Bits.test(reg(Reg.F), 4);
    }
    

    /**
     * Gets (eventual) initial borrow from opcode encoding and C Flag
     * @param opcode opcode of SBC operation
     * @return initial borrow (true for 1, false for 0)
     */
    private boolean getInitialBorrow(Opcode opcode) {
        return getInitialCarry(opcode);
    }
    

    /**
     * Computes new C flag value from bit 3 of given opcode encoding and actual C flag value
     * @param opcode opcode of SCF or CFF operation
     * @return new C flag value
     */
    private boolean getCFlagSCCF(Opcode opcode) {
        return !(Bits.test(opcode.encoding, 3) && Bits.test(reg(Reg.F), 4));
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
    
    /**
     * Extracts value from given int and puts it in given reg
     * @param r register in which to store value
     * @param vf packed value and flags
     */
    private void setRegFromAlu(Reg r, int vf) {
        setReg(r, Alu.unpackValue(vf));
    }
    
    /**
     * Extracts flags from given int and puts them in reg F
     * @param valueFlags packed value and flags
     */
    private void setFlags(int valueFlags) {
        setReg(Reg.F, Alu.unpackFlags(valueFlags));
    }
    
    /**
     * Extracts flags and value from given int, and puts them in reg F, resp. in given reg.
     * @param r register in which to store value
     * @param vf packed value and flags
     */
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }
    
    /**
    * Computes new flag value given a flag source (CPU, ALU, V0, V1), the actual flag and packed value and flags from ALU
    * @param vf packed value and flags
    * @param flagSrc source of flag to pick
    * @param flag flag for which a new value needs to be asserted
    * @return
    */
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
    * Gets flag value stored in reg F
    * @param f flag to get the value of
    * @return flag value as boolean (true for 1, false for 0)
    */
   private boolean getFlag(Flag f) {
       return flagValue(0, FlagSrc.CPU, f);
   }
    
    /**
     * Allows to set Cpu flags (so reg F) by combining old Cpu flags value (by using FlagSrc.CPU)
     * , flags returned by Alu (by using FlagSrc.ALU) or arbitrary values (FlagSrc.V0 for 0, FlagSrc.V1 for 1)
     * @param vf packed value and flags form Alu
     * @param z flag z source
     * @param n flag n source
     * @param h flag h source
     * @param c flag c source
     */
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
     * @return Reg reg identity
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

    /**
     * Extracts from given opcode encoding direction of rotation
     * @param opcode opcode in which rotation direction is encoded
     * @return
     */
    private RotDir extractRotDir(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }
    
    /**
     * Extracts from given opcode encoding index of bit to test/modify
     * @param opcode opcode in which bit index is encoded
     * @return index of bit
     */
    private int extractBitIndex(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3); 
    }
    

    /**
     * Extracts from given opcode encoding value of bit to set
     * @param opcode opcode in which new bit value is encoded
     * @return value of bit
     */
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
        int val = Bits.clip(16, Bits.signExtend8(read8AfterOpcode()));
        int valueFlags = Alu.add16L(SP, val);
        
        combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        
        return Alu.unpackValue(valueFlags);
    }
    
    /**
     * Adds next byte as signed and clips
     * @return value + (signed) byte *clipped*
     */
    private int add16_E8(int value) {
        return Bits.clip(16, value + Bits.signExtend8(read8AfterOpcode()));
    }

    /**
     * Given an interrupt, sets IF corresponding bit to 1
     * @param i interrupt
     */
    public void requestInterrupt(Interrupt i) {
        IF = Bits.set(IF, i.index(), true);
    }
    
    /**
     * Extracts condition from given opcode encoding, evaluates it and returns its value
     * @param opcode opcode in which condition is encoded
     * @return condition value (true/false)
     */
    private boolean evaluateCondition(Opcode opcode) {
        int condition = Bits.extract(opcode.encoding, 3, 2);
        switch(condition) {
        case 0b00:
            //nz
            return !(getFlag(Flag.Z));
        case 0b01:
            //z
            return getFlag(Flag.Z);
        case 0b10:
            //nc
            return !(getFlag(Flag.C));
        case 0b11:
            //c
            return getFlag(Flag.C);
            default :
                throw new IllegalArgumentException();
        }
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
    
    // :::::::::::::::::::::: TESTING UTILITARIES ::::::::::::::::
    
    
    // TODO remove before commit
    protected void reset() {
        for(Reg reg : Reg.values()) {
            setReg(reg, 0);
        }
        SP = 0;
        PC = 0;
        IME = false;
        IE = 0;
        IF = 0;
        nextNonIdleCycle = 0;
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
   
    //TODO remove before commit
    protected void setInterruptRegs(boolean ime, int ie, int If) {
        IME = ime;
        IE = ie;
        IF = If;
   }
    
    //TODO remove before commit
    protected void setPC(int pc) {
        Preconditions.checkBits16(pc); 
        PC = pc;
    }
    
    //TODO remove before commit
    protected int[] get_IME_IE_IF() {
        int ime_val = IME ? 1:0 ;
        return new int[] {ime_val, IE, IF};
    }
    
    //TODO remove before commit
    public int readAtBus(int address) {
        return bus.read(address);
    }    
}