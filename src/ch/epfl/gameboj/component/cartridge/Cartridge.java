/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {

    private final Component mbc;

    /**
     * Private constructor for Cartridge 
     * Contructs a cartridge with given Memory Bank Controller
     * 
     * @param mbc
     *            associated Memory Bank Controller
     */
    private Cartridge(Component mbc) {
        this.mbc = mbc;
    }

    /**
     * Reads rom data from file, and constructs Rom, corresponding MBC, and
     * Cartridge from it
     * 
     * @param romFile
     *            file containing the ROM data
     * @return Cartridge constructed from romFile data
     * @throws IOException
     *             if any IO problem occurs, including if given romFile doesn't
     *             exist
     * @throws IllegalArgumentException
     *             if given romFile 147th's bit (used to identify MBC type)
     *             isn't 0
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        try (FileInputStream s = new FileInputStream(romFile)) {
            byte[] data = s.readAllBytes();
            Preconditions.checkArgument(data[0x146] == 0);

            return new Cartridge(new MBC0(new Rom(data)));
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        mbc.write(address, data);
    }

}
