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
//
//    private int P1;
    private boolean select0, select1;
    private final int[] lines; // TODO : can be done with two int line0 and
                               // line1, prob more efficient but slighlty longer
                               // code

    private final Cpu cpu;

    public Joypad(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

//        P1 = 0;
        lines = new int[2];
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address == AddressMap.REG_P1)
            return Bits.complement8(computeP1());
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_P1) {
            //TODO : very strange but functions better without it...
            data = Bits.complement8(data);
            select0 = Bits.test(data, 4);
            select1 = Bits.test(data, 5);
//            System.out.println("p1 written: " + Integer.toBinaryString(P1));
        }
    }

    public void keyPressed(Key key) {
//        System.out.println("pressed : " + key);
//        System.out.println("p1 before : " + Integer.toBinaryString(P1));
        update(key, true);
    }

    public void keyReleased(Key key) {
//        System.out.println("released : " + key);
        update(key, false);
    }

    private void update(Key key, boolean newValue) {
        int oldP1 = computeP1(); //no need to clip only states ?
        
        int keyIndex = key.ordinal();
        int line = keyIndex / LINE_SIZE;

        // update key pressed
        lines[line] = Bits.set(lines[line], keyIndex % LINE_SIZE, newValue);
        
        int newP1 = computeP1();
        if(oldP1 != newP1) //TODO : ok to compare full P1 instead of just state ? test it
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }
    
    private int computeP1() {
        //TODO : replace select0 by an int value ?
        int line0 = select0 ? lines[0] : 0;
        int line1 = select1 ? lines[1] : 0;
        
        int s0 = select0 ? 1 : 0;
        int s1 = select1 ? 1 : 0;
        
        return (s1<<5) | (s0<<4) | (line0 | line1); //TODO : 11... ? or inversed ?
    }
}
