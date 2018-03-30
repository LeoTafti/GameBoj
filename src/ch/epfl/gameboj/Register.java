package ch.epfl.gameboj;

public interface Register {

    /**
     * Will automatically be given a definition when implemented by an enum
     * 
     * @returns the ordinal of this enumeration constant
     * @see java.lang.Enum#ordinal()
     */
    abstract int ordinal();

    /**
     * @return index as defined by ordinal()
     */
    default int index() {
        return ordinal();
    }
}
