/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

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
