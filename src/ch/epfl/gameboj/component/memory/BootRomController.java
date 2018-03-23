package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BootRomController implements Component {
    
    private Cartridge cartridge;
    private boolean bootRomActive = true;
    
    public BootRomController(Cartridge cartridge) {
        if(cartridge == null)
            throw new NullPointerException();
        
        this.cartridge = cartridge;
    }
    
    @Override
    public int read(int address) {
        //TODO : again, should check arg ?
        if(bootRomActive && (address >= 0 && address < 0xFF)) {
            //TODO : project guidelines say last two bytes are written at 0xFE and 0xFF
            //      but it seems that 0xFF should be excluded (?)
            return BootRom.DATA[address];
        }
        return cartridge.read(address);
    }

    @Override
    public void write(int address, int data) {
        if(address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomActive = false;
            //TODO : should we still write the value at this address ? (probably)
            //      if yes, simply remove else{}
        }
        else {
            cartridge.write(address, data);
        }
            
    }

}
