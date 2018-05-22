/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Joypad implements Component {

    private static final int LINE_SIZE = 4;
    
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    private final int[] lines;
    private boolean select0, select1;

    private final Cpu cpu;

    /**
     * Constructor for gameboy's joypad consisting of 8 buttons
     * 4 arrows and A, B, select, start
     * @param cpu the gameboy's cpu
     */
    public Joypad(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        lines = new int[2];
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address == AddressMap.REG_P1)
            return Bits.complement8(computeP1());
        return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_P1) {
            data = Bits.complement8(data);
            select0 = Bits.test(data, 4);
            select1 = Bits.test(data, 5);
        }
    }

    /**
     * Simulates GameBoy's key pressed
     * @param key one of the joypad's keys
     */
    public void keyPressed(Key key) {
        update(key, true);
    }

    /**
     * Simulates GameBoy's key released
     * @param key one of joypad's keys
     */
    public void keyReleased(Key key) {
        update(key, false);
    }

    /**
     * Updates joypad's state and potentially requests Cpu
     * @param key one of joypad's keys
     * @param newValue true for pressed, false for released
     */
    private void update(Key key, boolean newValue) {
        int oldP1 = computeP1();
        
        int keyIndex = key.ordinal();
        int line = keyIndex / LINE_SIZE;

        // Update key pressed
        lines[line] = Bits.set(lines[line], keyIndex % LINE_SIZE, newValue);
        
        int newP1 = computeP1();
        if(oldP1 != newP1) //4 msb haven't changed in between, no need to clip the 4 lsb
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }
    
    /**
     * Computes P1 from stored lines and selection bits values
     * @return corresponding P1
     */
    private int computeP1() {
        int line0 = select0 ? lines[0] : 0;
        int line1 = select1 ? lines[1] : 0;
        
        int sBit0 = select0 ? 1 : 0;
        int sBit1 = select1 ? 1 : 0;
        
        return (sBit1<<5) | (sBit0<<4) | (line0 | line1);
    }
}
