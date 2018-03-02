package ch.epfl.gameboj;

public interface Register {
    
    abstract int ordinal();

    /**
     * @return index as defined in enum type
     */
    default int index() {
        return ordinal();
    }
}
