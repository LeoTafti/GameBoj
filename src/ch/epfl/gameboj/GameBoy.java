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
        workRamController = new RamController(workRam, 0xC000, 0xE000);
        
        echoRam = new Ram(7680);
        echoRamController = new RamController(workRam, 0xE000, 0xFE00);
        
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
