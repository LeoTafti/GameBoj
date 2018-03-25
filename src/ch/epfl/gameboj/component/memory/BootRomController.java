package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BootRomController implements Component {
    
    private Cartridge cartridge;
    private boolean bootRomActive = true;
    
    /**
     * Contructs a boot rom controller for given cartridge
     * @param cartridge cartridge for which a boot rom controller is needed
     */
    public BootRomController(Cartridge cartridge) {
        this.cartridge = Objects.requireNonNull(cartridge);
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(bootRomActive && (address >= 0 && address <= 0xFF)) {
            return Byte.toUnsignedInt(BootRom.DATA[address]);
        }
        else if(address < 0x8000) {
            return cartridge.read(address);
        }
        else {
            return NO_DATA;
            }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if(address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomActive = false;
        }
        else if(address < 0x8000){ // ROM_END not in addressmap, could be added, or use VIDEO_RAM_START
            cartridge.write(address, data);
        }
            
    }

}
