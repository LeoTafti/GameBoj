package ch.epfl.gameboj.component.lcd;

import java.io.ObjectStreamClass;
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

public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160, LCD_HEIGHT = 144;
    
    private static final int TILE_SIZE = 8;
    private static final int IMAGE_TILE_SIZE = 32;
    private static final int IMAGE_SIZE = TILE_SIZE * IMAGE_TILE_SIZE;
    
    private static final int WX_CORRECTION = 7;
    
    private static final int LINE_CYCLES= 114;
    private static final int V_BLANK_LINES = 10;
    
    private static final int H_BLANK_CYCLES = 51;
    private static final int V_BLANK_CYCLES = 1140;
    private static final int MODE_2_CYCLES = 20;
    private static final int MODE_3_CYCLES = 43;


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
    
    private enum LcdMode { H_BLANK , V_BLANK, MODE_2, MODE_3 };
    
    private enum Area { BG, WIN }
    
    private int winY = 0;
    
    private final Cpu cpu;
    private final RegisterFile<Reg> registerFile;
    private final Ram vRam;
    private Bus bus;
    
    private LcdImage currentImage;
    private LcdImage.Builder nextImageBuilder;
    
    //TODO : should be 0 or what ? we didn't give it an initial value in cpu.java…
    private long nextNonIdleCycle;
    
    
    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        
        registerFile = new RegisterFile<>(Reg.values());
        vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        
        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
  
        nextNonIdleCycle = Long.MAX_VALUE;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            return readRegAt(address); //TODO : we had forgot the return, but now it fails miserably haha
        }
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
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
                        Bits.extract(data, 3, 5)<<3 | Bits.clip(3, reg(Reg.STAT)));
            }
            else {
                setRegAt(address, data);
                
                if(address == AddressMap.REG_LCDC && !screenIsOn()) {
                    setMode(LcdMode.H_BLANK);
                    setReg(Reg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                else if(address == AddressMap.REG_LCDC_LYC)
                    updateLYC_EQ_LY();
            }
        }
        //TODO does write depend on LCDC modes?
        else if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
                vRam.write(address - AddressMap.VIDEO_RAM_START, data);
        }
    }
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        
        this.bus = bus;
    }

    public LcdImage currentImage() {
           return currentImage;
       }

    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && screenIsOn()) {
            nextNonIdleCycle = cycle;
        }
        
        else if(cycle != nextNonIdleCycle)
            return;
        
        reallyCycle(cycle);
    }
    
    private void reallyCycle(long cycle) {
        switch(mode()) {
        case H_BLANK:
            if(reg(Reg.LY) < LCD_HEIGHT - 1) {
                setMode(LcdMode.MODE_2);
                nextNonIdleCycle += MODE_2_CYCLES;
            }
            else {
                setMode(LcdMode.V_BLANK);
                nextNonIdleCycle += LINE_CYCLES;
                cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
                currentImage = nextImageBuilder.build();
            }
            incLY();
            break;
            
        case V_BLANK:
            if(reg(Reg.LY) == LCD_HEIGHT + V_BLANK_LINES - 1) {
                setMode(LcdMode.MODE_2);
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                nextNonIdleCycle++;
                winY = 0;
            }
            else 
                nextNonIdleCycle += LINE_CYCLES;
            incLY();
            break;
            
        case MODE_2:
            setMode(LcdMode.MODE_3);
            nextNonIdleCycle += MODE_3_CYCLES;
            break;
            
        case MODE_3:
            setMode(LcdMode.H_BLANK);
            nextNonIdleCycle += H_BLANK_CYCLES;
            
            nextImageBuilder.setLine(reg(Reg.LY), computeLine((reg(Reg.LY) + reg(Reg.SCY)) % IMAGE_SIZE));
            break;
        }    
        requestPotentialInterrupt();
    }

    private void incLY() {
        int LY = reg(Reg.LY);
        setReg(Reg.LY, ++LY % (LCD_HEIGHT + V_BLANK_LINES));
        updateLYC_EQ_LY();
    }

    private void updateLYC_EQ_LY() {
           setBitSTAT(STAT_Bits.LYC_EQ_LY, reg(Reg.LYC) == reg(Reg.LY));
           requestPotentialInterrupt();
       }

    private LcdImageLine computeLine(int index) {
        LcdImageLine.Builder bgLineBuilder = new LcdImageLine.Builder(IMAGE_SIZE);
        LcdImageLine.Builder winLineBuilder = new LcdImageLine.Builder(IMAGE_SIZE);
        
        int bgTileLine = index / TILE_SIZE;
        int bgLine = index % TILE_SIZE;
        int winTileLine = winY / TILE_SIZE;
        int winLine = winY % TILE_SIZE;
        
        int WX_prime = reg(Reg.WX) - WX_CORRECTION;
        
        int bg_area = AddressMap.BG_DISPLAY_DATA[testBitLCDC(LCDC_Bits.BG_AREA) ? 1 : 0];
        int win_area = AddressMap.BG_DISPLAY_DATA[testBitLCDC(LCDC_Bits.WIN_AREA) ? 1 : 0];
        
        boolean windowOnLine = testBitLCDC(LCDC_Bits.WIN)
                && WX_prime >= 0 &&   WX_prime < LCD_WIDTH
                && reg(Reg.LY) >= reg(Reg.WY);
                
        for(int tile = 0; tile < IMAGE_TILE_SIZE; tile++) { 
            int bgTileIndex = read(bg_area + bgTileLine * IMAGE_TILE_SIZE + tile);
            int winTileIndex = read(win_area + winTileLine * IMAGE_TILE_SIZE + tile);

            if(testBitLCDC(LCDC_Bits.TILE_SOURCE) == false) {
                bgTileIndex += bgTileIndex <= 0x7f ? 0x80 : -0x80;
                winTileIndex += winTileIndex <= 0x7f ? 0x80 : -0x80;
            }
            addTileLine(bgLineBuilder, tile, bgTileIndex, bgLine);
            
                    
            if(windowOnLine) {
                addTileLine(winLineBuilder, tile, winTileIndex, winLine); 
            }
        }
        
        LcdImageLine bg = bgLineBuilder.build().extractWrapped(reg(Reg.SCX), LCD_WIDTH);
        LcdImageLine win = winLineBuilder.build().extractWrapped(0, LCD_WIDTH);
        
        if(windowOnLine) {
            winY++;
            return bg.join(win, WX_prime).mapColors(reg(Reg.BGP));
        }
        return bg;
    }
    
    private LcdImageLine computeLine(Area area, int index) {
        
    }
    
    private void addTileLine(LcdImageLine.Builder b, int tile, int tileIndex, int lineIndex) {
        int tileSource = AddressMap.TILE_SOURCE[testBitLCDC(LCDC_Bits.TILE_SOURCE) ? 1 : 0];
        
        int address = tileSource + tileIndex * 16 + 2*lineIndex;
        
        int lb = read(address);
        int mb = read(address + 1);
        
        b.setBytes(tile, Bits.reverse8(mb), Bits.reverse8(lb));
    }

    private void requestPotentialInterrupt() {
       if(Bits.extract(reg(Reg.STAT), 3, 3) != 0)
           cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
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
                Bits.extract(reg(Reg.STAT), 2, 6) << 2 | mode.ordinal());
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
    private int readRegAt(int address) {
        return reg(Reg.values()[address - AddressMap.REG_LCDC]);
    }
    private void setRegAt(int address, int newV) {
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
