/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    private Cpu cpu;
    
    
    private long cycleCount = 0;
    
    
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
        
        cpu = new Cpu();
        cpu.attachTo(bus);
    }
    
    /**
     * getter for Bus
     * @return GameBoy's bus
     */
    public Bus bus() {
        return bus;
    }
    
    /**
     * getter for Cpu
     * @return GameBoy's Cpu
     */
    public Cpu cpu() {
        return cpu;
    }
    
    /**
     * runs cycles up to given value
     * @param cycle
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle < cycles());
        
        while(cycleCount < cycle) {
            cpu.cycle(cycleCount);
            ++cycleCount;
        }
        
    }
    
    /**
     * getter for cycle count
     * @return
     */
    public long cycles() {
        return cycleCount;
    }
}
