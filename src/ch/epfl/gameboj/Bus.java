package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {
    private ArrayList<Component> attachedComponents;
    
    public void attach(Component component) {
        attachedComponents.add(Objects.requireNonNull(component));
    }
    
    //bien compris ce que dois faire cette méthode ?
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        int value = Component.NO_DATA;
        for(Component c : attachedComponents) {
            value = c.read(address);
        }
        
        if (value == Component.NO_DATA) {
            throw new IllegalArgumentException();
        }
        return value;
    }
}
