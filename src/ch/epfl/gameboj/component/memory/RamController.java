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
     * @throws IllegalArgumentExcepetion
     *             if both addresses aren't 16 bits or if address interval is
     *             negative, or if address interval is bigger than ram's size
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);

        if (endAddress - startAddress < 0
                || endAddress - startAddress > ram.size()) {
            throw new IllegalArgumentException();
        }

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

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (isInBound(address)) {
            return (ram.read(address - startAddress));
        } else {
            return Component.NO_DATA;
        }
    }

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
        if (address < startAddress || address >= endAddress) {
            return false;
        } else
            return true;
    }

}
