/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import javafx.collections.MapChangeListener;

public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160, LCD_HEIGHT = 144;

    private static final int TILE_SIZE = 8,
            IMAGE_TILE_SIZE = 32,
            IMAGE_SIZE = TILE_SIZE * IMAGE_TILE_SIZE,
            SPRITE_LINES = 8, BIG_SPRITE_LINES = 16,
            SPRITE_BYTE_SIZE = 4;

    private static final int WX_CORRECTION = 7,
            SPRITE_X_CORRECTION = 8,
            SPRITE_Y_CORRECTION = 16,
            TILE_INDEX_CORRECTION = 0x80;

    private static final int LINE_CYCLES = 114,
            TOTAL_DMA_CYCLES = 160;
    
    private static final int V_BLANK_LINES = 10;

    private static final int H_BLANK_CYCLES = 51,
            MODE_2_CYCLES = 20,
            MODE_3_CYCLES = 43;

    private static final int TOTAL_SPRITES = 40,
            MAX_SPRITES_PER_LINE = 10;
    
    private final LcdImage BLANK_IMAGE = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    private enum LCDC_Bits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STAT_Bits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED_7
    }
    
    private enum SpriteInfos implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    };

    private enum LcdMode {
        H_BLANK, V_BLANK, MODE_2, MODE_3
    };


    private final Cpu cpu;
    private final RegisterFile<Reg> registerFile;
    private final Ram vRam;
    private final Ram oam;
    private Bus bus;

    private LcdImage currentImage = null;
    private LcdImage.Builder nextImageBuilder;

    private long nextNonIdleCycle;
    private int remainingDMACycles;

    private int winY;
    
    //TODO : remove
    private long debugCycle = 0;

    
    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        registerFile = new RegisterFile<>(Reg.values());
        vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        oam = new Ram(AddressMap.OAM_RAM_SIZE);

        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);

        nextNonIdleCycle = Long.MAX_VALUE; //TODO : still unsure about that

        winY = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END)
            return readRegAt(address);

        else if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return vRam.read(address - AddressMap.VIDEO_RAM_START);
        }
        else if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END)
            return oam.read(address - AddressMap.OAM_START);
        
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            if (address == AddressMap.REG_STAT) {
                setReg(Reg.STAT,
                        Bits.extract(data, 3, 5) << 3 | Bits.clip(3, reg(Reg.STAT)));
            }
            else if (address == AddressMap.REG_LCDC) {
                boolean oldScreenState = screenIsOn();
                setRegAt(address, data);
                if (address == AddressMap.REG_LCDC && oldScreenState != screenIsOn()) {
                    setMode(LcdMode.H_BLANK);
                    setReg(Reg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
            }
            else {
                setRegAt(address, data);

                if (address == AddressMap.REG_LYC)
                    updateLYC_EQ_LY();
                else if (address == AddressMap.REG_DMA) {
                    remainingDMACycles = TOTAL_DMA_CYCLES;
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

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (remainingDMACycles > 0) {
            int index = TOTAL_DMA_CYCLES - remainingDMACycles;
            write(AddressMap.OAM_START + index,
                    bus.read((reg(Reg.DMA) << 8) + index));
            
            remainingDMACycles--;
            // TODO : compute reg(Reg.DMA) only once and not 160 times
        }
        
        if (nextNonIdleCycle == Long.MAX_VALUE && screenIsOn()) {
            nextNonIdleCycle = cycle;
        }
        else if (cycle != nextNonIdleCycle) {
            return;
        }
        
        reallyCycle(cycle);
    }

    /**
     * Changes mode, potentially request adequate interrupts and updates
     * nextNonIdleCycle accordingly
     **/
    //TODO : remove cycle
    private void reallyCycle(long cycle) {
        switch (mode()) {
        case H_BLANK:
            if (reg(Reg.LY) < LCD_HEIGHT - 1) {
                setMode(LcdMode.MODE_2);
                nextNonIdleCycle += MODE_2_CYCLES;
            }
            else {
                setMode(LcdMode.V_BLANK);
                nextNonIdleCycle += LINE_CYCLES;
                cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
                
                //TODO : sometimes prints something (when changing screen) : normal ?
                if(cycle-debugCycle != 17556)
//                    System.out.println(cycle-debugCycle);
                debugCycle = cycle;
                
                
                currentImage = nextImageBuilder.build();
            }
            incLY();
            break;

        case V_BLANK:
            if (reg(Reg.LY) == LCD_HEIGHT + V_BLANK_LINES - 1) {
                setMode(LcdMode.MODE_2);
                nextNonIdleCycle += MODE_2_CYCLES;
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
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

            nextImageBuilder.setLine(reg(Reg.LY),
                    computeLine((reg(Reg.LY) + reg(Reg.SCY)) % IMAGE_SIZE));
            break;
        }
        requestPotentialInterrupt(mode());
    }

    /**
     * Getter for currentImage
     * @return current Lcd Image
     */
    public LcdImage currentImage() {
        if(currentImage == null)
            return BLANK_IMAGE;
        return currentImage;
    }

    private LcdImageLine computeLine(int index) {
        LcdImageLine bg  = computeLine(index, LCDC_Bits.BG_AREA, testBitLCDC(LCDC_Bits.BG))
                    .extractWrapped(reg(Reg.SCX), LCD_WIDTH)
                    .mapColors(reg(Reg.BGP));
        
        int WX_prime = reg(Reg.WX) - WX_CORRECTION;
        
        boolean windowOnLine = testBitLCDC(LCDC_Bits.WIN)
                && WX_prime >= 0 && WX_prime < LCD_WIDTH
                && reg(Reg.LY) >= reg(Reg.WY);

        if (windowOnLine) {
            LcdImageLine win = computeLine(winY, LCDC_Bits.WIN_AREA, true)
                    .extractWrapped(0, LCD_WIDTH)
                    .mapColors(reg(Reg.BGP));
            winY++;
            bg = bg.join(win.shift(WX_prime), WX_prime);
        }
        
        LcdImageLine[] spriteLines = computeSpriteLines(index - reg(Reg.SCY)); //TODO : dégeu
        return composeSpritesAndBG(spriteLines[0], spriteLines[1], bg);
    }

    private LcdImageLine computeLine(int index, LCDC_Bits area, boolean active) {
        LcdImageLine.Builder b = new LcdImageLine.Builder(IMAGE_SIZE);
        if(active) {
            int tileLine = index / TILE_SIZE;
            int line = index % TILE_SIZE;
    
            int areaStart = AddressMap.BG_DISPLAY_DATA[testBitLCDC(area) ? 1 : 0];
    
            for (int tile = 0; tile < IMAGE_TILE_SIZE; tile++) {
    
                int tileIndex = read(areaStart + tileLine * IMAGE_TILE_SIZE + tile);
    
                if (! testBitLCDC(LCDC_Bits.TILE_SOURCE)) {
                    tileIndex = Bits.clip(8, tileIndex + TILE_INDEX_CORRECTION);
                }
                addTileToLine(b, tile, tileIndex, line);
            }
        }
        return b.build();
    }

    /**
     * Make background sprite line and foreground sprite line
     * @param index line being drawn
     * @return array with background sprite line first and foreground sprite line second
     */
    private LcdImageLine[] computeSpriteLines(int index) {
        LcdImageLine bgLine = new LcdImageLine.Builder(LCD_WIDTH).build();
        LcdImageLine fgLine = new LcdImageLine.Builder(LCD_WIDTH).build();
        
        if(testBitLCDC(LCDC_Bits.OBJ)) {
            boolean bigSprites = testBitLCDC(LCDC_Bits.OBJ_SIZE);
            
            int[] sprites = spritesIntersectingLine(index, bigSprites);
            
            for (int spriteIndex : sprites) {
                
                LcdImageLine.Builder spriteLineBuilder = new LcdImageLine.Builder(LCD_WIDTH);
                
                int address = AddressMap.OAM_START + spriteIndex * SPRITE_BYTE_SIZE;
                
                int y = read(address) - SPRITE_Y_CORRECTION;
                int x = read(address + 1) - SPRITE_X_CORRECTION;
                int tileIndex = read(address + 2);
                int infos = read(address + 3);
                
                int tileLine = index - y;
                                
                //TODO : normally, this should suffice
                if (testBitSprite(SpriteInfos.FLIP_V, infos))
                    tileLine = bigSprites ? BIG_SPRITE_LINES - tileLine - 1 : SPRITE_LINES - tileLine - 1;
                    
                    
                
                int[] lineBytes = getLineBytes(tileIndex, tileLine, AddressMap.TILE_SOURCE[1]);
                
                /* Horizontal flip
                Since tiles (bytes) read from the bus are stored as is in the vRam, we have to reverse them
                when computing a NOT reversed sprite, and we don't have to reverse them when computing
                a reversed one*/
                if(!testBitSprite(SpriteInfos.FLIP_H, infos)) {
                    lineBytes[0] = Bits.reverse8(lineBytes[0]);
                    lineBytes[1] = Bits.reverse8(lineBytes[1]);
                }
                
                spriteLineBuilder.setBytes(0, lineBytes[1], lineBytes[0]);
                
                int palette = testBitSprite(SpriteInfos.PALETTE, infos) ? reg(Reg.OBP1) : reg(Reg.OBP0);
                LcdImageLine spriteLine = spriteLineBuilder.build().shift(x).mapColors(palette);
                
                if(testBitSprite(SpriteInfos.BEHIND_BG, infos))
                        bgLine = spriteLine.below(bgLine);
                else fgLine = spriteLine.below(fgLine);
            }
        }
        return new LcdImageLine[] {bgLine, fgLine};
    }

    /**
     * Get tile-line from memory and adds it to currently building line
     * 
     * @param b
     *            current line builder
     * @param tile
     *            number of the tile being draw in its respective line
     * @param tileIndex
     *            index of line in memory
     * @param tileLineIndex
     *            line of the designated tile to be added
     */
    private void addTileToLine(LcdImageLine.Builder lb, int tile, int tileIndex, int lineIndex) {
        int startAddress = AddressMap.TILE_SOURCE[testBitLCDC(LCDC_Bits.TILE_SOURCE) ? 1 : 0];
        
        int[] lineBytes = getLineBytes(tileIndex, lineIndex, startAddress);
        lb.setBytes(tile, Bits.reverse8(lineBytes[1]), Bits.reverse8(lineBytes[0]));
    }

    private int[] getLineBytes(int tileIndex, int lineIndex, int startAddress) {
        int address = startAddress + tileIndex * 2 * Byte.SIZE + 2 * lineIndex;
        return new int[] { read(address), read(address + 1) };
    }

    private int[] spritesIntersectingLine(int line, boolean bigSprites) {
        int[] sprites = new int[10];
        int spriteCount = 0;
        for (int sprite = 0; sprite <= TOTAL_SPRITES && spriteCount < MAX_SPRITES_PER_LINE; sprite++) {

            int spriteAddress = AddressMap.OAM_START + sprite * SPRITE_BYTE_SIZE;
            int spriteY = read(spriteAddress) - SPRITE_Y_CORRECTION;

            int range = bigSprites ? BIG_SPRITE_LINES : SPRITE_LINES;

            if (line >= spriteY && line <= spriteY + range - 1) {
                int spriteX = read(spriteAddress + 1);
                sprites[spriteCount] = (spriteX << Byte.SIZE) | sprite;
                spriteCount++;
            }
        }
        Arrays.sort(sprites, 0, spriteCount);

        int[] spritesIndex = new int[spriteCount];
        for (int i = 0; i < spriteCount; i++) {
            spritesIndex[i] = Bits.clip(8, sprites[i]);
        }

        return spritesIndex;
    }
    
    private LcdImageLine composeSpritesAndBG(LcdImageLine bgSprites, LcdImageLine fgSprites, LcdImageLine bg) {
        BitVector newOpacity = bg.opacity().or(bgSprites.opacity().not());
        return bgSprites.below(bg, newOpacity).below(fgSprites);
    }

    private void requestPotentialInterrupt(LcdMode mode) {
        if(!mode.equals(LcdMode.MODE_3)
                && testBit(Reg.STAT, mode.ordinal() + 3)) //corresponding STAT_Bits index is greater by 3 compared to LcdMode
                cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
    }

    private void incLY() {
        int LY = reg(Reg.LY);
        setReg(Reg.LY, ++LY % (LCD_HEIGHT + V_BLANK_LINES));
        updateLYC_EQ_LY();
    }

    private void updateLYC_EQ_LY() {
        boolean newValue = reg(Reg.LYC) == reg(Reg.LY);
        setBitSTAT(STAT_Bits.LYC_EQ_LY, newValue);

        if(testBitSTAT(STAT_Bits.INT_LYC) && newValue)
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

    private boolean testBitSprite(SpriteInfos bit, int infos) {
        return Bits.test(infos, bit.index());
    }

    private void setBit(Reg r, int index, boolean newValue) {
        setReg(r, Bits.set(reg(r), index, newValue));
    }

    private void setBitSTAT(STAT_Bits bit, boolean newValue) {
        setBit(Reg.STAT, bit.index(), newValue);
    }
    
}
