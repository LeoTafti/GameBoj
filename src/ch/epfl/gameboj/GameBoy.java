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

    private final Bus bus;
    private final Cpu cpu;
    private final Timer timer;

    private long cycleCount;

    /**
     * GameBoy constructor
     * 
     * @param cartridge the cartridge of the game
     * @throws NullPointException
     *             if given cartridge is null
     */
    public GameBoy(Cartridge cartridge) {
        bus = new Bus();
        
        cpu = new Cpu();
        cpu.attachTo(bus);
        
        timer = new Timer(cpu);
        timer.attachTo(bus);
        
        cycleCount = 0;

        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        RamController workRamController = new RamController(ram,
                AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        RamController echoRamController = new RamController(ram,
                AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);

        workRamController.attachTo(bus);
        echoRamController.attachTo(bus);

        BootRomController romController = new BootRomController(
                Objects.requireNonNull(cartridge));
        romController.attachTo(bus);
        
    }


    /**
     * Runs gameboy up to given cycle value - 1
     * 
     * @param cycle
     *            number of cycles to run
     * @throws IllegalArgumentException
     *             if current gameboy cycle is greater than given cycle value
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycleCount <= cycle);

        while (cycleCount < cycle) {
            timer.cycle(cycleCount);
            cpu.cycle(cycleCount);
            ++cycleCount;
        }
    }
    
    /**
     * Getter for bus
     * 
     * @return GameBoy's bus
     */
    public Bus bus() {
        return bus;
    }
    
    /**
     * Getter for cpu
     * 
     * @return GameBoy's cpu
     */
    public Cpu cpu() {
        return cpu;
    }
    
    /**
     * Getter for timer
     */
    public Timer timer() {
        return timer;
    }

    /**
     * Getter for number of cycles the gameboy has already run for
     */
    public long cycles() {
        return cycleCount;
    }

}
