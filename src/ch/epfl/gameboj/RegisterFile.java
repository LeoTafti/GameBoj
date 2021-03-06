package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {
    private final byte[] regs;

    /**
     * Constructs a RegisterFile of 8-bit registers (1 byte each)
     * 
     * @param allRegs
     *            all the elements of the enum type E (E.values())
     */
    public RegisterFile(E[] allRegs) {
        regs = new byte[allRegs.length];
    }

    /**
     * Gets the 8-bit value stored in given register
     * 
     * @param reg
     *            the register in which to look
     * @return value stored in given register, between 0 (inclusive) and 0xFF
     *         (inclusive)
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(regs[reg.index()]);
    }

    /**
     * Modifies value stored in given register
     * 
     * @param reg
     *            the register to modify
     * @param newValue
     *            the new value to store
     * @throws IllegalArgumentException
     *             if given value isn't an 8-bit value
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        regs[reg.index()] = (byte) newValue;
    }

    /**
     * Tests value of given bit of given register, returns true if value is 1,
     * false if 0
     * 
     * @param reg
     *            the register in which to look
     * @param b
     *            the bit to check for {@link Bits#test(int bits, Bit bit)}
     * @return true if given bit of register is 1, false if 0
     */
    public boolean testBit(E reg, Bit b) {       
        return Bits.test(get(reg), b);
    }

    /**
     * Modifies value of given bit in given register
     * 
     * @param reg
     *            register to modify
     * @param bit
     *            the bit to modify
     * @param newValue
     *            value to assign to bit
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}
