/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {
    private ArrayList<Component> attachedComponents = new ArrayList<>();
    
    public void attach(Component component) {
        attachedComponents.add(Objects.requireNonNull(component));
    }
    
    /**
     * returns value stored at given address, if any of the components has data there, otherwise returns 0xFF
     * @param address read location
     * @return value at given address
     * @throws IllegalArgumentException if address isn't a 16 bits value
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        int value = Component.NO_DATA;
        
        for(Component c : attachedComponents) {
            value = c.read(address);
            if(value != Component.NO_DATA)
                return value;
        }
        return 0xFF;
    }
    
    /**
     * Writes given data at given address for all attached components
     * @param address write location
     * @param data value to be written
     * @throws IllegalArgumentExcepetion if address isn't 16 bit value, or data isn't 8 bit value
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        for(Component c : attachedComponents) {
            c.write(address, data);
        }
    }
}
