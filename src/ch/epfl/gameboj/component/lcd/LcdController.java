/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.List;

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
    private static final int SPRITE_LINES = 8, BIG_SPRITE_LINES = 16;
    private static final int SPRITE_BYTE_SIZE = 4;

    private static final int WX_CORRECTION = 7;
    private static final int SPRITE_X_CORRECTION = 8;
    private static final int SPRITE_Y_CORRECTION = 16;

    private static final int LINE_CYCLES = 114;
    private static final int V_BLANK_LINES = 10;

    private static final int H_BLANK_CYCLES = 51;
    private static final int MODE_2_CYCLES = 20;
    private static final int MODE_3_CYCLES = 43;
    
    private static final int TOTAL_SPRITES = 40;
    private static final int TOTAL_DMA_CYCLES = 160;
    
    //TODO : less modifiers ? :D
    
    // 8-bit registers
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    private enum LCDC_Bits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STAT_Bits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED_7
    }

    private enum LcdMode {
        H_BLANK, V_BLANK, MODE_2, MODE_3
    };
    
    private enum SpriteCarac implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG 
    };

    private final Cpu cpu;
    private final RegisterFile<Reg> registerFile;
    private final Ram vRam;
    private final Ram oam;
    private Bus bus;

    private LcdImage currentImage;
    private LcdImage.Builder nextImageBuilder;

    private long nextNonIdleCycle;
    private int remainingDMACycles;
    
    private int winY;

    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        registerFile = new RegisterFile<>(Reg.values());
        vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        oam = new Ram(AddressMap.OAM_RAM_SIZE);

        currentImage = new LcdImage(
                List.of(new LcdImageLine.Builder(160).build()));
        // TODO : just so that DebugMainLive works, BUT still shows that maybe
        // we should give it a value and not let it be null before building
        // first image ?

        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);

        nextNonIdleCycle = Long.MAX_VALUE;
        
        winY = 0;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END)
            return readRegAt(address);

        else if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END)
            return vRam.read(address - AddressMap.VIDEO_RAM_START);

        else if (address >= AddressMap.OAM_START
                && address < AddressMap.OAM_END)
            oam.read(address - AddressMap.OAM_START);

        return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            if (address == AddressMap.REG_LCDC_STAT) {
                setReg(Reg.STAT, Bits.extract(data, 3, 5) << 3
                        | Bits.clip(3, reg(Reg.STAT)));
            } else {
                setRegAt(address, data);

                if (address == AddressMap.REG_LCDC && !screenIsOn()) {
                    setMode(LcdMode.H_BLANK);
                    setReg(Reg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                } 
                else if (address == AddressMap.REG_LCDC_LYC)
                    updateLYC_EQ_LY();
                else if (address == AddressMap.REG_LCDC_DMA) {
                    remainingDMACycles = TOTAL_DMA_CYCLES;
                    //TODO if not necessary to reproduce incremental copying
//                    for (int i = 0; i < TOTAL_DMA_CYCLES; i++) {
//                        write(AddressMap.OAM_END - remainingDMACycles,
//                                read(reg(Reg.DMA) << 8 + 
//                                        (TOTAL_DMA_CYCLES - remainingDMACycles)));
//                    }
//                    nextNonIdleCycle += TOTAL_DMA_CYCLES;
                }
            }
        }
        else if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            vRam.write(address - AddressMap.VIDEO_RAM_START, data);
        }
        else if (address >= AddressMap.OAM_START
                && address < AddressMap.OAM_END) {
            oam.write(address - AddressMap.OAM_START, data);
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }


    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE && screenIsOn()) {
            nextNonIdleCycle = cycle;
        }
        //TODO do we need to simulate the incremental process of the DMA state?
        //      Yes, probably : "la méthode cycle doit être augmentée pour copier
        //                      le prochain octet vers la mémoire d'attributs d'objets […]"
        else if(remainingDMACycles != 0) {
            write(AddressMap.OAM_END - remainingDMACycles,
                    read(reg(Reg.DMA) << 8 + (TOTAL_DMA_CYCLES - remainingDMACycles)));
            //TODO : reaminingDMACycles decrementation somewhere ?
            //TODO : compute reg(Reg.DMA) only once and not 160 times ?
            
            //TODO which is better?
//            oam.write(TOTAL_DMA_CYCLES - remainingDMACycles,
//                    read(reg(Reg.DMA) << 8 + (TOTAL_DMA_CYCLES - remainingDMACycles)));
        }
        else if (cycle != nextNonIdleCycle)
            return;

        reallyCycle(cycle);
    }

    private void reallyCycle(long cycle) {
        switch (mode()) {
        case H_BLANK:
            if (reg(Reg.LY) < LCD_HEIGHT - 1) {
                setMode(LcdMode.MODE_2);
                nextNonIdleCycle += MODE_2_CYCLES;
            } else {
                setMode(LcdMode.V_BLANK);
                nextNonIdleCycle += LINE_CYCLES;
                cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
                currentImage = nextImageBuilder.build();
            }
            incLY();
            break;

        case V_BLANK:
            if (reg(Reg.LY) == LCD_HEIGHT + V_BLANK_LINES - 1) {
                setMode(LcdMode.MODE_2);
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                nextNonIdleCycle++;
                winY = 0;
            } else
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

            nextImageBuilder.setLine(reg(Reg.LY),
                    computeLine((reg(Reg.LY) + reg(Reg.SCY)) % IMAGE_SIZE));
            break;
        }
        requestPotentialInterrupt();
    }

    public LcdImage currentImage() {
        return currentImage;
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
        int WX_prime = reg(Reg.WX) - WX_CORRECTION;
        
        boolean windowOnLine = testBitLCDC(LCDC_Bits.WIN)
                && WX_prime >= 0 && WX_prime < LCD_WIDTH
                && reg(Reg.LY) >= reg(Reg.WY);

        LcdImageLine bg = computeLine(index, LCDC_Bits.BG_AREA)
                .extractWrapped(reg(Reg.SCX), LCD_WIDTH);

        if (windowOnLine) {
            LcdImageLine win = computeLine(winY, LCDC_Bits.WIN_AREA)
                    .extractWrapped(0, LCD_WIDTH);
            winY++;
            return bg.join(win.shift(WX_prime), WX_prime)
                    .mapColors(reg(Reg.BGP));
        }
        return bg.mapColors(reg(Reg.BGP));
    }

    private LcdImageLine computeLine(int index, LCDC_Bits area) {
        // TODO : using LCDC_Bits even though we only expect to get WIN_AREA or
        // BG_AREA is not nice, but it simplifies the code considerably compared
        // to using another enum. Since private method, I'd say it's okay (?)
        
        LcdImageLine.Builder b = new LcdImageLine.Builder(IMAGE_SIZE);
        int tileLine = index / TILE_SIZE;
        int line = index % TILE_SIZE;

        int areaStart = AddressMap.BG_DISPLAY_DATA[testBitLCDC(area) ? 1 : 0];

        for (int tile = 0; tile < IMAGE_TILE_SIZE; tile++) {

            int tileIndex = read(areaStart + tileLine * IMAGE_TILE_SIZE + tile);

            if (testBitLCDC(LCDC_Bits.TILE_SOURCE) == false) {
                tileIndex += tileIndex <= 0x7f ? 0x80 : -0x80;
            }

            addTileToLine(b, tile, tileIndex, line);
        }
        return b.build();
    }
    
    private LcdImageLine computeSpriteLine(int index) {
        LcdImageLine.Builder slB;
        LcdImageLine.Builder lB = new LcdImageLine.Builder(IMAGE_SIZE);
        
        int areaStart = AddressMap.BG_DISPLAY_DATA[1];
        
        int[] sprites = spritesIntersectingLine(index);
        
        for (int sprite = 0; sprite < sprites.length; sprite++) {
            
            slB = new LcdImageLine.Builder(IMAGE_SIZE);
            int spriteY = read(AddressMap.OAM_START + sprite * SPRITE_BYTE_SIZE + 1) - SPRITE_Y_CORRECTION;
            int spriteCarac = read(AddressMap.OAM_START + sprite * SPRITE_BYTE_SIZE + 3);
            //TODO if(v-flip) else and 16-line height
            int tileLine = index - spriteY;
            int tileIndex = areaStart + sprite * TILE_SIZE;
            int[] lineBytes = getLineBytes(tileIndex, tileLine);
            if(testBitsSprite(SpriteCarac.FLIP_H, spriteCarac))
                for( int b : lineBytes) Bits.reverse8(b);
            slB.;
        }
        
        return null;
        
    }
    
    /**
     * get tile-line from memory and adds it to currently building line
     * @param b current line builder
     * @param tile number of the tile being draw in its respective line
     * @param tileIndex index of line in memory
     * @param tileLineIndex line of the designated tile to be added
     */
    private void addTileToLine(LcdImageLine.Builder b, int tile, int tileIndex,
            int tileLineIndex) {
        
        int[] lineBytes = getLineBytes(tileIndex, tileLineIndex);
        
        b.setBytes(tile, Bits.reverse8(lineBytes[0]), Bits.reverse8(lineBytes[1]));
    }
    
    private int[] getLineBytes(int tileIndex, int tileLine) {
        
        int tileSource = AddressMap.TILE_SOURCE[testBitLCDC(
                LCDC_Bits.TILE_SOURCE) ? 1 : 0];

        int address = tileSource + tileIndex * 2 * Byte.SIZE + 2 * tileLine;

        return new int[] { read(address), read(address + 1) };
    }
       
    
    private int[] spritesIntersectingLine(int line) {
        int[] sprites = new int[10];
        int spriteCount = 0;
        for(int sprite = 0; sprite <= TOTAL_SPRITES; sprite++) {
            
            int spriteAddress = AddressMap.OAM_START+sprite*SPRITE_BYTE_SIZE;
            int spriteY = read(spriteAddress + 1) - SPRITE_Y_CORRECTION;
            
            boolean spriteInRange = reg(Reg.LY) >= spriteY && reg(Reg.LY) <= spriteY + SPRITE_LINES;
            boolean bigSpriteInRange = testBitLCDC(LCDC_Bits.OBJ_SIZE)
                                && reg(Reg.LY) >= spriteY && reg(Reg.LY) <= spriteY + BIG_SPRITE_LINES;
                    
            if( spriteInRange || bigSpriteInRange) { //dont need to correct x coordinate here
                int spriteX = read(spriteAddress);
                sprites[spriteCount] = (spriteX << Byte.SIZE) | sprite;
                spriteCount++ ;
            }
            
            if(spriteCount == 10) break;
        }
        
        Arrays.sort(sprites, 0, spriteCount);
        int[] spritesIndex = new int[spriteCount];
        for (int i = 0; i < spritesIndex.length; i++) {
            spritesIndex[i] = Bits.clip(8, sprites[i]);
        }
        return spritesIndex;
    }

    private void salut() {
        
    }
    
    private void requestPotentialInterrupt() {
        if (Bits.extract(reg(Reg.STAT), 3, 3) != 0)
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

    //TODO : remove if still unused
    private boolean testBitSTAT(STAT_Bits bit) {
        return testBit(Reg.STAT, bit.index());
    }
    
    private boolean testBitsSprite(SpriteCarac bit, int caracs) {
        return Bits.test(caracs, bit.ordinal());
    }

    private void setBit(Reg r, int index, boolean newValue) {
        setReg(r, Bits.set(reg(r), index, newValue));
    }

    //TODO : remove if still unused
    private void setBitLCDC(LCDC_Bits bit, boolean newValue) {
        setBit(Reg.LCDC, bit.index(), newValue);
    }

    private void setBitSTAT(STAT_Bits bit, boolean newValue) {
        setBit(Reg.STAT, bit.index(), newValue);
    }
}
