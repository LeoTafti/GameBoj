/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

public final class ColorSet {

    private final int[] colors = new int[4];
    
    /**
     * Make a new 4-color set with RGB values, 
     * each byte represents alpha, red, blue, green, respectively
     * @param c1 first color (darkest on original gameboy)
     * @param c2 second color
     * @param c3 third color
     * @param c4 fourth color (white on original gameboy)
     */
    public ColorSet(int[] colors) {
       Preconditions.checkArgument(colors.length == 4);

       for (int i = 0; i < colors.length; i++) 
           this.colors[i] = colors[i];     
    }
    
    
    /**
     * Make a new 4-color set with RGB values
     * out of red, green and blue ratios
     * @param red red ratio (between 0 and 1)
     * @param green green ratio (between 0 and 1)
     * @param blue blue ratio (between 0 and 1)
     * @throws IllegalArgumentException if r, g or b is not between 0 and 1 (inclusive)
     */
    public ColorSet(double red, double green, double blue) {
        Preconditions.checkArgument( 0 <= red && red <= 1);
        Preconditions.checkArgument( 0 <= green && green <= 1);
        Preconditions.checkArgument( 0 <= blue && blue <= 1);
        double[] ratios = {blue, green, red};
        final int[] brightness = { 0xfd, 0xd3, 0xa9, 0x02};

        for (int c = 0; c < colors.length; c++) {
            colors[c] = 0xff_00_00_00;
            for(int r = 0; r < ratios.length; r++)
                colors[c] |= (int)(brightness[c] * ratios[r]) << (Byte.SIZE * r);
        }
    }
    
    /**
     * returns the requested color as argb
     * @param index index of the color (from 0 to 3)
     * @return color as int with bytes representing respectively alpha-red-green-blue
     */
    public int get(int index) {
        return colors[index];
    }
    
    //TODO comment
    public String getRgbString(int index) {
        return String.format("%1$06X",(colors[index] - 0xff_00_00_00)); //TODO static var
    }
    
    //TODO implement
    public static void randomize( ) {}
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < colors.length; i++) {
            sb.append(Integer.toHexString(colors[i])).append(',');
            
        }
        sb.append(']');
        return sb.toString();
    }
    
    public static final ColorSet GAMEBOY = new ColorSet(new int[] {0xff_ff_ff_ff,
            0xff_d3_d3_d3,
            0xff_a9_a9_a9,
            0xff_00_00_00});
    
    
    public static final ColorSet DESERT = new ColorSet(new int[] {
            0xff595358,
            0xffC9ADA7,
            0xffBFB48F,
            0xffF2E9E4});
    
    public static final ColorSet FOREST = new ColorSet(new int[] {
            0xff294936,
            0xff856A5D,
            0xffC0CAAD,
            0xffA3B18A});
    
    public static final ColorSet SEASIDE = new ColorSet(new int[] {
            0xff315659,
            0xff034078,
            0xff1282A2,
            0xffF0F0C9});
    
    public static final ColorSet CITY = new ColorSet(new int[] {
            0xff493843,
            0xffF6F8FF,
            0xffCCDAD1,
            0xff9CAFB7});
    
    public static final ColorSet WONDERLAND = new ColorSet(new int[] {
            0xffFFA0FD,
            0xff81F4E1,
            0xffFFB86F,
            0xffA9CEF4});
    
    public static final ColorSet MOUNTAIN = new ColorSet(new int[] {
            0xff5D576B,
            0xffA89F68,
            0xffA6A2A2,
            0xffE5E6E4});
    
    public static ColorSet Random = new ColorSet(new int[] {
      0xff,
      0xff,
      0xff,
      0xff});

    
//    public static final ColorSet EMPTY = new ColorSet(new int[] {
//            0xff,
//            0xff,
//            0xff,
//            0xff});
}
