/*
* Author : Paul Juillard (288519)
* Date: 19/02
*/

package ch.epfl.gameboj;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {

    private Bus bus;
    private Ram workRam;
    private Ram echoRam;
    private RamController workRamController;
    private RamController echoRamController;
    
    /**
     * Constructor
     * @param cartirdge
     */
    public GameBoy(Object cartirdge) {
        
        bus = new Bus();
        
        workRam = new Ram(8192);
        workRamController = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        
        echoRam = new Ram(7680);
        echoRamController = new RamController(echoRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        bus.attach(workRam);
        bus.attach(echoRam);        
    }
    
    /**
     * getter for Bus
     * @return gameBoy's bus
     */
    public Bus bus() {
        return bus;
    }
}
