/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class MBC0 implements Component {

    private static final int ROM_SIZE = 0x8000;
    
    private final Rom rom;

    /**
     * Constructs Memory Bank Controller (type 0) for given Rom
     * 
     * @param rom
     *            rom for which an MBC0 is needed
     * @throws IllegalArgumentException
     *             if size of rom is different then expected ROM_SIZE
     * @throws NullPointerException
     *             if given rom doesn't exist
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == ROM_SIZE);

        this.rom = rom;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < ROM_SIZE) {
            return rom.read(address);
        }
        return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        // Does nothing
    }

}
