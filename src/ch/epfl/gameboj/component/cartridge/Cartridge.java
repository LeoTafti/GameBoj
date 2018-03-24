package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {

    private Component mbc;
    
    /**
     * Private constructor for Cartridge
     * Contructs a cartridge with given Memory Bank Controller
     * @param mbc associated Memory Bank Controller 
     */
    private Cartridge(Component mbc) {
        this.mbc = mbc;
    }
    
    /**
     * Reads rom data from file, and constructs Rom, corresponding MBC, and Cartridge from it
     * @param romFile file in which rom data is written
     * @return Cartridge constructed from romFile data
     * @throws IOException if any IO problem occurs, including if given romFile doesn't exist
     */
    public static Cartridge ofFile(File romFile) throws IOException{
        try(FileInputStream s = new FileInputStream(romFile)){
            byte[] data = new byte[(int)romFile.length()];
            
            data = s.readAllBytes();
            if(data[0x146] != 0)
                throw new IllegalArgumentException("This cartridge requires another MBC than MBC0 (not implemented/emulated)");
            
            return new Cartridge(
                    new MBC0(new Rom(data)));
        }
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        mbc.write(address, data);
    }

}
