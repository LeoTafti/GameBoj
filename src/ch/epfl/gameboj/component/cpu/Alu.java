/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;

public final class Alu {
    
    /**
     * non-instantiable class
     */
    private Alu() {
        //nothing here
    }
    
    /**
     * Defines Flag enum type
     * purpose : a flag gives additional informations on results
     */
    public enum Flag implements Bit{
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, Z, N, H, C;
    }
    
    public enum RotDir{
        LEFT, RIGHT;
    }
    
    public static int maskZNHC (boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(0, z, n, h, c);
    }
    
    public static int unpackValue(int valueFlags) {
        
    }
    
    private static int packValueZNHC(int v,boolean z, boolean n, boolean h, boolean c) {
        int packed = v << 8;
        if(z) {
            packed = packed | Flag.Z.mask();
        }
        if(n) {
            packed = packed | Flag.N.mask();
        }
        if(h) {
            packed = packed | Flag.H.mask();
        }
        if(c) {
            packed = packed | Flag.H.mask();
        }
        
        return packed;
    }
    
    
}
