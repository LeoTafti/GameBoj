/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class GameBoy {
    
    private Bus bus;
    private Cpu cpu;
    private Timer timer;
    
    
    private long cycleCount = 0;
    
    
    /**
     * Constructor
     * @param cartridge
     * @throws NullPointException if cartridge is null
     */
    public GameBoy(Cartridge cartridge) {

        this.bus = new Bus();
        
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        RamController workRamController = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        RamController echoRamController = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        workRamController.attachTo(bus);
        echoRamController.attachTo(bus);
        
        cpu = new Cpu();
        cpu.attachTo(bus);
        
        BootRomController romController = new BootRomController(Objects.requireNonNull(cartridge));
        romController.attachTo(bus);
        
        timer = new Timer(cpu);
        timer.attachTo(bus);
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
     * getter for timer
     */
    public Timer timer() {
        return timer;
    }
    
    /**
     * runs cycles up to given value
     * @param cycle gameboy will stop cycling at given value
     * @throws IllegalArgumentException if current cycle is bigger then requested finish cycle
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycleCount < cycle);
        
//        while(cycleCount < cycle) {
//            timer.cycle(cycleCount);
//            cpu.cycle(cycleCount);
//            ++cycleCount; //TODO : ++ or = cycle ?
//        }
        
        //TODO : remove
        for(long i = cycles(); i < cycle; i++) {
            timer.cycle(i);
            cpu.cycle(i);
        }
        cycleCount = cycle;
        
    }
    
    /**
     * getter for cycle count
     */
    public long cycles() {
        return cycleCount;
    }
    
}
