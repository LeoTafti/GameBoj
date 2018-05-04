package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class Joypad implements Component {
    
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }
    
    private enum P1_Bits {
        COL_0_STATE, COL_1_STATE, COL_2_STATE, COL_3_STATE, LINE_0_SELECTION, LINE_1_SELECTION, UNUSED_6, UNUSED_7
    }
    
    private final Cpu cpu;
    
    private int P1;
    
    //TODO : choose whichever implementation is the best
    private int[] line0, line1;
    private int[][] buttons;
    
    public Joypad(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        
        P1 = 0;
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if(address == AddressMap.REG_P1)
            return Bits.complement8(P1);
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if(address == AddressMap.REG_P1) {
            P1 = Bits.set(P1, 4, Bits.test(data, 4));
            P1 = Bits.set(P1, 5, Bits.test(data, 5));
            P1 = Bits.complement8(data);
        }
    }
    
    public void keyPressed(Key key) {
        
    }
    
    public void keyReleased(Key key) {
        
    }

}
