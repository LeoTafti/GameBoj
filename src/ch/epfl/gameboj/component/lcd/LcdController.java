package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160, LCD_HEIGHT = 144;

    // 8-bit registers
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }
    
    private enum LCDC_Bits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }
    
    private enum STAT_Bits implements Bit{
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED_7
    }
    
    private enum LcdMode {H_BLANK , V_BLANK, MODE_2, MODE_3 };
    
    private final Cpu cpu;
    private Bus bus;
    
    private final Ram vRam;
    
    private final RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());
    
    private long nextNonIdleCycle = 0; //TODO : should be 0 or what ? we didn't give it an initial value in cpu.java…
    private long lcdOnCycle; //TODO : @289
    
    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
    }
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        
        this.bus = bus;
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
        interrupt();
        
        //from cpu
//        if (IME && pendingInterrupt()) {
//            handleInterrupt();
//        } else {
//            Opcode opcode = getOpcode();
//            dispatch(opcode);
//
//            nextNonIdleCycle += opcode.cycles;
//        }
    }
        
    private void interrupt() {
        if(mode() == LcdMode.V_BLANK)
            cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
        else if (Bits.extract(reg(Reg.STAT), 3, 3) != 0)
                cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
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
            readRegAtAddress(address);
        }
        
        //TODO distinguish between states:
        // BG_AREA 0 and BG_AREA 1          Bits.test(LCDC, 3);
        // WIN_AREA 0 and WIN_AREA 1        Bits.test(LCDC, 6);
        //  TILESOURCE0 and TILESOURCE1     Bits.test(LCDC, 4);
        // ====> implemented private methods to make it clean, maybe do so as well for the first range 
        // even for cpu?
        
        //TODO should we have a single if? this way optimises time though
        
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            //TODO : strange ?
            if(tileSourceRange(address) || tileAreaRange(address))
                return vRam.read(address - AddressMap.VIDEO_RAM_START);
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            if(address == AddressMap.REG_LCDC_STAT) {
                setReg(Reg.STAT,
                        Bits.clip(3, reg(Reg.STAT)) & (Bits.extract(data, 3, 5)<<3));
            }
            else {
                setRegAtAddress(address, data);
                
                if(address == AddressMap.REG_LCDC && !screenIsOn()) {
                    setMode(LcdMode.H_BLANK);
                    setReg(Reg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                else if(address == AddressMap.REG_LCDC_LY || address == AddressMap.REG_LCDC_LYC)
                    updateLYC_EQ_LY();
            }
        }
        //TODO does write depend on LCDC modes?
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
                bus.write(address, data);
        }
    }

   
    //TODO call in setReg for case LY and LYC
   private void updateLYC_EQ_LY() {
       setBitSTAT(STAT_Bits.LYC_EQ_LY, reg(Reg.LYC) == reg(Reg.LY));
       interrupt();
   }
   
   public LcdImage currentImage() {
        // TODO : implement
        return null;
    }

    private boolean tileSourceRange(int address) {
        boolean source0 = (address >= AddressMap.TILE_SOURCE_0_START
                && address < AddressMap.TILE_SOURCE_0_END )
                && !testBitLCDC(LCDC_Bits.TILE_SOURCE);
        boolean source1 = (address >= AddressMap.TILE_SOURCE_1_START
                && address < AddressMap.TILE_SOURCE_1_END )
                && testBitLCDC(LCDC_Bits.TILE_SOURCE);
        return source0 || source1;
    }

    private boolean tileAreaRange(int address) {
        boolean area0 = (address >= AddressMap.TILE_AREA_0_START
                && address < AddressMap.TILE_AREA_0_END )
                && !(testBitLCDC(LCDC_Bits.BG_AREA) && testBitLCDC(LCDC_Bits.WIN_AREA));
        boolean area1 = (address >= AddressMap.TILE_AREA_1_START
                && address < AddressMap.TILE_AREA_1_END )
                && (testBitLCDC(LCDC_Bits.BG_AREA) || testBitLCDC(LCDC_Bits.WIN_AREA));
        return area0 || area1;
    }

    private boolean screenIsOn() {
        return testBitLCDC(LCDC_Bits.LCD_STATUS);
    }
    
    private LcdMode mode() {
        
        switch (Bits.clip(2, reg(Reg.STAT))) {
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
    
    private void setMode(LcdMode mode) {
        setReg(Reg.STAT,
                reg(Reg.STAT) << 2 | mode.ordinal());
    }
    
    /**
     * Returns value stored in given 8-bit reg
     * 
     * @param r
     *            register
     * @return value stored in register
     */
    private int reg(Reg r) {
        return registerFile.get(r);
    }
    
    /**
     * Sets given reg with given value
     * 
     * @param r
     *            register in which to put value
     * @param newV
     *            new value to store
     */
    private void setReg(Reg r, int newV) {
        registerFile.set(r, newV);
    }
    
    private int readRegAtAddress(int address) {
        return reg(Reg.values()[address - AddressMap.REG_LCDC]);
    }
    
    private void setRegAtAddress(int address, int newV) {
        setReg(Reg.values()[address - AddressMap.REG_LCDC], newV);
    }
    
    
    private boolean testBit(Reg r, int index) {
        return Bits.test(reg(r), index);
    }
    private boolean testBitLCDC(LCDC_Bits bit) {
        return testBit(Reg.LCDC, bit.index());
    }
    private boolean testBitSTAT(STAT_Bits bit) {
        return testBit(Reg.STAT, bit.index());
    }
    
    private void setBit(Reg r, int index, boolean newValue) {
        setReg(r, Bits.set(reg(r), index, newValue));
    }
    private void setBitLCDC(LCDC_Bits bit, boolean newValue) {
        setBit(Reg.LCDC, bit.index(), newValue);
    }
    private void setBitSTAT(STAT_Bits bit, boolean newValue) {
        setBit(Reg.STAT, bit.index(), newValue);
    }
}
