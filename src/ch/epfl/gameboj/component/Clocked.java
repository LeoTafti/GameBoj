package ch.epfl.gameboj.component;

public interface Clocked {
    
    /**
     * Asks component to execute operations it has to do during given cycle
     * 
     * @param cycle
     *            the cycle index
     */
    public abstract void cycle(long cycle);
}
