/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.component.Component;

public final class cpu implements Component, Clocked {

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
      }
    private enum Reg16 implements Register {
        AF, BC, DE, HL, PC, SP
    }
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);
   
    private long nextNonIdleCycle;
    
    
    @Override
    public void cycle(long cycle) {
        
        if(cycle != nextNonIdleCycle) return;
        // TODO maybe
        //int op = RegisterFile.get(Reg16.PC);
        
    };
    
    public void dispatch(Opcode  op) {
        
    switch( op) {
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
    }
    
    
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub
        
    }

    public int[] _testGetPcSpAFBCDEHL() {
        
        return new int[0];
    }
    
    /**
     * creates table of the opcodes of given kind 
     * @param kind DIRECT or PREFIXED
     * @return table of 256 opcodes
     */
    static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        
        Opcode[] table = new Opcode[256]; //TODO TODO hardcoding 256
        int i = 0;
        for (Opcode o: Opcode.values()) {
            if( o.kind == kind) {
                table[i] = o;
                i++;
            }
        }
        return new Opcode[1];
    }
}
