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

public final class Timer implements Clocked, Component{

    private Cpu cpu;
    
    private int DIV = 0, TIMA = 0, TMA = 0, TAC = 0;
    
    private final int[] tacValues = {9, 3, 5, 7};
    
    
    /**
     * Constructs Timer to associated cpu
     * @throws NullPointerException if cpu is null
     */
    public Timer(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
    }
    
    @Override
    public void cycle(long cycle) {
        if(timerIsOn()) { //doesn't change anything for blaargtest
        
            boolean s0 = state();
        
            DIV = Bits.clip(16, DIV + 4);
            //TODO : what does "4" stand for ? Define a static final attribute
        
            incIfChange(s0);
        }
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        switch (address) {
        case AddressMap.REG_DIV:
            return DIV >> 8;
        case AddressMap.REG_TIMA:
            return TIMA;
        case AddressMap.REG_TMA:
            return TMA;
        case AddressMap.REG_TAC:
            return TAC;
        default:
            return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        boolean s0 = state();
        
        switch(address) {        
        case AddressMap.REG_DIV: {
            DIV = 0;
            }break;               
        case AddressMap.REG_TIMA:
            TIMA = data;
            break;               
        case AddressMap.REG_TMA: 
            TMA = data;
            break;               
        case AddressMap.REG_TAC: {
            TAC = data;
            }break;               
        }            
        
        incIfChange(s0);
        
    }
    
    /**
     *      
     * @return index of timer to increment
     */
    private int TIMA_setup() {
        return tacValues[Bits.extract(TAC, 0, 2)];
    }
    
    /**
     * Tests value of bit of index 2 of TAC
     * @return true if ON
     */
    private boolean timerIsOn() {
        return Bits.test(TAC, 2);
    }
    
    /**
     * State of timer
     * @return true if timer is ON and (TIMA-setup)-th bit is 1
     */
    private boolean state() {
        return (timerIsOn() && Bits.test(DIV, TIMA_setup()));
    }

    /**
     * If previous state is true and current state is false increments TIMA.
     * If this produces an overflow,
     *      requests cpu interrupt and resets TIMA to TMA value
     * @param previousState value of the previous state
     */
    private void incIfChange(boolean previousState) {
        
        if(previousState && !state()) {
            
            TIMA += 1;
            
            if(TIMA > 0xFF) {
                TIMA = TMA;
                cpu.requestInterrupt(Interrupt.TIMER);
            }
        }
    }

}
