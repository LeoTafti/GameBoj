package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {

    private Component mbc;
    //TODO : no need to have an attribute Rom, do we ? (supposedly same as GameBoy.class)
    
    private Cartridge(Component mbc) {
        this.mbc = mbc;
    }
    
    public static Cartridge ofFile(File romFile) throws IOException{
        try(FileInputStream s = new FileInputStream(romFile)){
            byte[] data = new byte[0x8000];
            int b, i = 0;
            while((b = s.read()) != -1) { //TODO : what if romFile has more than 0x8000 values ?
                data[i] = (byte)b; //TODO : correct to cast here ?
                i++;
                if((i == 0x146) && (b != 0)){ //TODO : performance : put it here or outside of while ?
                    throw new IllegalArgumentException();
                }
            }
            
            return new Cartridge(
                    new MBC0(new Rom(data)));
        }
        catch(IOException e) {  //TODO : should be done explicitly or just add throws 
                                // to signature ?
            throw e;
        }
    }
    
    @Override
    public int read(int address) {
        //TODO : should we validate address here too since MBC read already does it ?
        //      Remark : applies in general to the whole project : should we
        //      check again even if we know it will be checked down the line ?
        return mbc.read(address);
    }

    @Override
    public void write(int address, int data) {
        //TODO : Same remark as in read()
        mbc.write(address, data);
    }

}
