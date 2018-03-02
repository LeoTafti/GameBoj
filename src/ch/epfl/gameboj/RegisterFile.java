package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> { //TODO : extends Register ?
    private byte[] regs;
    
    public RegisterFile(E[] allRegs) {
        regs = new byte[allRegs.length];
    }
    
    public int get(E reg) {
        return Byte.toUnsignedInt(regs[reg.index()]);
    }
    
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        regs[reg.index()] = (byte)newValue;
    }
    
    public boolean testBit(E reg, Bit b) {
        return Bits.test(regs[reg.index()], b);
    }
    
    public void setBit(E reg, Bit bit, boolean newValue) {
        Bits.set(regs[reg.index()], bit.index(), newValue);
    }
}
