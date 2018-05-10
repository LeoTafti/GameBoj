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

    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    private static final int LINE_SIZE = 4;

    private int P1;
    private final int[] lines; // TODO : can be done with two int line0 and
                               // line1, prob more efficient but slighlty longer
                               // code

    private final Cpu cpu;

    public Joypad(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        P1 = 0;
        lines = new int[2];
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address == AddressMap.REG_P1)
            return Bits.complement8(P1);
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_P1) {
            //TODO : very strange but functions better without it...
            System.out.println("data: " + Integer.toBinaryString(data));
            data = Bits.complement8(data);
            P1 = Bits.set(P1, 4, Bits.test(data, 4));
            P1 = Bits.set(P1, 5, Bits.test(data, 5));
            System.out.println("p1 written: " + Integer.toBinaryString(P1));
        }
    }

    public void keyPressed(Key key) {
        System.out.println("pressed : " + key);
        System.out.println("p1 before : " + Integer.toBinaryString(P1));
        update(key, true);
    }

    public void keyReleased(Key key) {
//        System.out.println("released : " + key);
        update(key, false);
    }

    private void update(Key key, boolean newValue) {
        int keyIndex = key.ordinal();
        int line = keyIndex / LINE_SIZE;

        // update key pressed
        lines[line] = Bits.set(lines[line], keyIndex % LINE_SIZE, newValue);

        // update P1
        int line0 = Bits.test(P1, 4) ? lines[0] : 0;
        int line1 = Bits.test(P1, 5) ? lines[1] : 0;

        int oldStates = Bits.clip(4, P1);
        int newStates = line0 | line1;
//        P1 = (Bits.extract(P1, 4, 4) << 4) | newStates;
        P1 = (P1 & 0xf0) | newStates;
        System.out.println("p1 after : " + Integer.toBinaryString(P1));
        
        if(oldStates != newStates)
            cpu.requestInterrupt(Interrupt.JOYPAD);
        
//        System.out.println(Integer.toBinaryString(lines[0]));
//        System.out.println(Integer.toBinaryString(lines[1]));
        
    }
}
