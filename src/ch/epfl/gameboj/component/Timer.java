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
    
    private int[] tacValues = {9, 3, 5, 7};
    
    
    
    
    /**
     * Constructs Timer to associated cpu
     * @throws NullPointerException if cpu is null
     */
    public Timer(Cpu cpu) {
        
        cpu = Objects.requireNonNull(cpu);
        
    }
    
    @Override
    public void cycle(long cycle) {
        
        if(timerIsOn()) {
        
            boolean ps = state();
        
            DIV= Bits.clip(16, DIV + 4);
        
            incIfChange(ps);
        
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
        
        switch(address) {        
        case AddressMap.REG_DIV: {
            boolean ps = state();
            DIV = data << 8;
            incIfChange(ps);}
            break;               
        case AddressMap.REG_TIMA:
            TIMA = data;
            break;               
        case AddressMap.REG_TMA: 
            TMA = data;
            break;               
        case AddressMap.REG_TAC: {
            boolean ps = state();
            TAC = data;
            incIfChange(ps);}
            break;               
        }            
        
    }
    
    /**
     * @return log2 of TIMA's increment rate
     */
    private int TIMA_setup() {
        return tacValues[Bits.extract(TAC, 0, 2)];
    }
    
    /**
     * @return true if ON
     */
    private boolean timerIsOn() {
        return Bits.test(TAC, 2);
    }
    
    /**
     * State of timer
     * @return true if ON and (TIMA-setup)-th bit is 1
     */
    private boolean state() {
        return (timerIsOn() && Bits.test(DIV, TIMA_setup()));
    }

    /**
     * increments TIMA, requests cpu interrupt if overflows, resets to TMA
     */
    private void incIfChange(boolean previousState) {
        
        if(previousState != state()) {
        
            TIMA += 1;
            
            if(TIMA > 0xFF) {
                TIMA = TMA;
                cpu.requestInterrupt(Interrupt.TIMER);
            }
        }
    }

}
