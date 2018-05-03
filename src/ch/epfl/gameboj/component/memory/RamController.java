/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component {

    private final int startAddress, endAddress;
    private final Ram ram;

    /**
     * Constructs controller for given ram, giving access between startAddress
     * (inclusive) and endAddress (exclusive)
     * 
     * @param ram
     *            memory to access
     * @param startAddress
     *            (inclusive)
     * @param endAddress
     *            (exclusive)
     * @throws IllegalArgumentException
     *             if both addresses aren't 16 bits or if address interval is
     *             negative, or if address interval is bigger than ram's size
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Preconditions.checkArgument(endAddress - startAddress >= 0
                && endAddress - startAddress <= ram.size());

        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.ram = Objects.requireNonNull(ram);
    }

    /**
     * Constructs controller for given ram, giving access to totality of ram's
     * memory starting at startAddress
     * 
     * @param ram
     *            memory to access
     * @param startAddress
     *            (inclusive)
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (isInBound(address)) {
            return (ram.read(address - startAddress));
        } else {
            return Component.NO_DATA;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if (isInBound(address)) {
            ram.write(address - startAddress, data);
        }
    }

    /**
     * Checks if given address is valid, considering ram's
     * startAddress/endAddress
     * 
     * @param address
     *            given address
     * @return true for valid or false for invalid
     */
    private boolean isInBound(int address) {
        return (!(address < startAddress || address >= endAddress));
        //TODO : remove
//        if (address < startAddress || address >= endAddress) {
//            return false;
//        } else
//            return true;
    }

}
