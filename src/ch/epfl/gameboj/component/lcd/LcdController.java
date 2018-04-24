package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160, LCD_HEIGHT = 144;

    // 8-bit registers
    private int LCDC = 0, STAT = 0,
            SCY = 0, SCX = 0,
            LY = 0, LYC = 0,
            DMA = 0,
            BGP = 0, OBP0 = 0, OBP1 = 0,
            WY = 0, WX = 0;

    private final Cpu cpu;
    private final Bus bus;
    
    private long nextNonIdleCycle = 0; //TODO : should be 0 or what ? we didn't give it an initial value in cpu.java…
    private long lcdOnCycle; //TODO : inital value
    
    public LcdController(Cpu cpu, Bus bus) {
        //TODO : should we take bus as argument ?
        // there seem to be a contradiction between
        // what he says about attachTo and the constructor...
        this.cpu = Objects.requireNonNull(cpu);
        this.bus = Objects.requireNonNull(bus);

        Ram vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        RamController vRamController = new RamController(vRam,
                AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);

        vRamController.attachTo(this.bus);
    }

    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && screenIsOn())
            lcdOnCycle = cycle;
            //TODO : force nextNonIdleCycle to current cycle value ?
        else if(cycle != nextNonIdleCycle)
            return;
        
        reallyCycle();
        
        
        //From cpu :
//        if (nextNonIdleCycle == Long.MAX_VALUE && pendingInterrupt()) {
//            nextNonIdleCycle = cycle;
//        } else if (cycle != nextNonIdleCycle) {
//            return;
//        }
//        reallyCycle();
    }
    
    private void reallyCycle() {
        //TODO implement
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            
            switch(address) {
            case AddressMap.REGS_LCDC_START:
                return LCDC;
            case AddressMap.REGS_LCDC_START + 1:
                return STAT;
            case AddressMap.REGS_LCDC_START + 2:
                return SCY;
            // TODO : etc ? seems very shitty
            }
        }
        
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return bus.read(address); //TODO : not sure about that
        }
        
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        //TODO : again, switch seems baaadddd ?
    }

    public LcdImage currentImage() {
        // TODO : implement
        return null;
    }
    
    private boolean screenIsOn() {
        return Bits.test(LCDC, 7); //LCDC_STATUS is bit 7
    }

}