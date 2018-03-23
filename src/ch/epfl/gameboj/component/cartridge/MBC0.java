package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class MBC0 implements Component {
    
    private Rom rom;
    private static final int ROM_SIZE = 0x8000;
    
    public MBC0(Rom rom) {
        if (rom.size() != ROM_SIZE) {
            throw new IllegalArgumentException();
        }
        
        this.rom = Objects.requireNonNull(rom);
    }
    
    //TODO : should we write javadoc when overriding ?
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return rom.read(address);
    }

    @Override
    public void write(int address, int data) {
        //Does nothing
    }

}
