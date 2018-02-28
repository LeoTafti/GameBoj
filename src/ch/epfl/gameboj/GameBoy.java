/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
//  private Ram workRam;
//  private RamController workRamController;
//  private RamController echoRamController;
    
    
    /**
     * Constructor
     * @param cartridge
     */
    public GameBoy(Object cartridge) {
        this.bus = new Bus();
        
        Ram ram = new Ram(8192);
        RamController workRamController = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        RamController echoRamController = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        bus.attach(workRamController);
        bus.attach(echoRamController);
    }
    
    /**
     * getter for Bus
     * @return gameBoy's bus
     */
    public Bus bus() {
        return bus;
    }
}
