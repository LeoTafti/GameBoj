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
    private enum LcdMode {H_BLANK , V_BLANK, MODE_2, MODE_3 };
    private enum TileSource{ TILE_SOURCE_0, TILE_SOURCE_1 };
    
    private final Cpu cpu;
    private final Bus bus;
    
    private long nextNonIdleCycle = 0; //TODO : should be 0 or what ? we didn't give it an initial value in cpu.java…
    private long lcdOnCycle; //TODO : inital value
    
    public LcdController(Cpu cpu, Bus bus) {
        //TODO : should we take bus as argument ?
        // there seem to be a contradiction between
        // what he says about attachTo and the constructor..
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
        
        /*
         * TODO "Une manière simple de rétablir la correspondance est d'inverser
         * l'ordre des octets lus depuis la mémoire graphique avant de les
         * placer dans les vecteurs représentant les lignes, et c'est ce que
         * nous ferons dans le simulateur."
         */
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            
            switch(address) {
            case AddressMap.REG_LCDC:
                return LCDC;
            case AddressMap.REG_LCDC_STAT:
                return STAT;
            case AddressMap.REG_LCDC_SCY:
                return SCY;
            case AddressMap.REG_LCDC_SCX:
                return SCX;
            case AddressMap.REG_LCDC_LY:
                return LY;
            case AddressMap.REG_LCDC_LYC:
                return LYC;
            case AddressMap.REG_LCDC_BGP:
                return BGP;
            case AddressMap.REG_LCDC_OBP0:
                return OBP0;
            case AddressMap.REG_LCDC_OBP1:
                return OBP1;
            case AddressMap.REG_LCDC_WY:
                return WY;
            case AddressMap.REG_LCDC_WX:
                return WX;
            // TODO : etc ? seems very shitty
//            default: return NO_DATA;
            }
        }
        //TODO distinguish between states ( TILESOURCE0 and TILESOURCE1 )
        
        
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            if(address >= AddressMap.TILE_SOURCE_START && address < AddressMap.TILE_SOURCE_END)
                switch(tileSource()) {
                case TILE_SOURCE_0 : //if ... else ...
                case TILE_SOURCE_1 : // if ... else ...
                //WTF ITS HORRIBLE
                }
            return bus.read(address);
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            
            switch(address) {
            case AddressMap.REG_LCDC:
                break; 
            case AddressMap.REG_LCDC_STAT:
                break; 
            case AddressMap.REG_LCDC_SCY:
                break; 
            case AddressMap.REG_LCDC_SCX:
                break; 
            case AddressMap.REG_LCDC_LY:
                break; 
            case AddressMap.REG_LCDC_LYC:
                break; 
            case AddressMap.REG_LCDC_BGP:
                break; 
            case AddressMap.REG_LCDC_OBP0:
                break; 
            case AddressMap.REG_LCDC_OBP1:
                break; 
            case AddressMap.REG_LCDC_WY:
                break; 
            case AddressMap.REG_LCDC_WX:
                break;
                //            default: return NO_DATA;
            }
        }
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
                bus.write(address, data);
        }
        
        
        //TODO : again, switch seems baaadddd ?
    }

    public LcdImage currentImage() {
        // TODO : implement
        return null;
    }
    
    private boolean screenIsOn() {
        return Bits.test(LCDC, 7); //LCDC_STATUS is bit 7
    }
    
    private LcdMode mode() {
        
        switch (Bits.clip(2, STAT)) {
        case 0:
            return LcdMode.H_BLANK;
        case 1:
            return LcdMode.V_BLANK;
        case 2:
            return LcdMode.MODE_2;
        case 3:
            return LcdMode.MODE_3;
        }
        throw new IllegalStateException();
    }
    
    private TileSource tileSource() {
        return Bits.test(LCDC, 4)?
                TileSource.TILE_SOURCE_0 : TileSource.TILE_SOURCE_1;
    }

}
