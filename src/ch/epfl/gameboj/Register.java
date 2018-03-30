package ch.epfl.gameboj;

public interface Register {

    /*
     * will automatically be given a definition when implemented by an enum
     */
    abstract int ordinal();

    /**
     * @return index as defined in enum type
     */
    default int index() {
        return ordinal();
    }
}
