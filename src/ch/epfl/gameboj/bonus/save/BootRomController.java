/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus.save;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.BootRom;
import ch.epfl.gameboj.bonus.save.Cartridge;

public final class BootRomController implements Component {

    private final Cartridge cartridge;
    private boolean bootRomActive = true;

    /**
     * Contructs a boot rom controller for given cartridge
     * 
     * @param cartridge
     *            cartridge for which a boot rom controller is needed
     */
    public BootRomController(Cartridge cartridge) {
        this.cartridge = Objects.requireNonNull(cartridge);
    }

    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (bootRomActive && (address >= AddressMap.BOOT_ROM_START
                && address < AddressMap.BOOT_ROM_END)) {
            return Byte.toUnsignedInt(BootRom.DATA[address]);
        }
        return cartridge.read(address);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomActive = false;
        } else
            cartridge.write(address, data);
    }

}
