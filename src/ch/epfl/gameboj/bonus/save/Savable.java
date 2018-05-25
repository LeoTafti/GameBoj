package ch.epfl.gameboj.bonus.save;

public interface Savable {
    public abstract byte[] save();
    public abstract void load(byte[] data);
}
