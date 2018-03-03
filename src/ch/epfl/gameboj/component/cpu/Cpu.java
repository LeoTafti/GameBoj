/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {

    private enum Reg implements Register { A, F, B, C, D, E, H, L }
    private enum Reg16 implements Register { AF, BC, DE, HL, PC, SP }
    
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
    private RegisterFile<Reg16> register16File = new RegisterFile<>(Reg16.values());
    
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
        // TODO maybe
        int op = register16File.get(Reg16.PC);
        dispatch(op);
        
    };
    
    public void dispatch(int op) {
        Opcode opcode = DIRECT_OPCODE_TABLE[op];
        
        //TODO : implement each case
        
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
        }
        
        //TODO : update PC value, wait for x cycles, etc (cf guidlines 2.5.1.3)
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
}
