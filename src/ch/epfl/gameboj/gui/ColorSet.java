/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;

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
        double[] ratios = {green, blue, red};
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
    
    /**
     * Returns red, green and blue values as hex string (in css color formating)
     * @param index which color to translate
     * @return String formatted for css use
     */
    public String getRgbString(int index) {
        return String.format("%1$06X",(colors[index] - 0xff_00_00_00)); //TODO static var
    }
    
    /**
     * Randomizes Random ColorSet's colors
     */
    public static void randomize( ) {
        for (int c = 0; c < Random.colors.length; c++) {
            Random.colors[c] = 0xff_00_00_00;
            for(int rgb = 0; rgb < 3; rgb++)
                Random.colors[c] |= (int)(0xff * Math.random()) << (Byte.SIZE * rgb);
        } 
    }
    
    public static final ColorSet GAMEBOY = new ColorSet(new int[] {
            0xff_ff_ff_ff,
            0xff_d3_d3_d3,
            0xff_a9_a9_a9,
            0xff_00_00_00,
            });
    
    
    public static final ColorSet DESERT = new ColorSet(new int[] {
            0xffF2E9E4,
            0xffBFB48F,
            0xffC9ADA7,
            0xff595358,
            });
    
    public static final ColorSet FOREST = new ColorSet(new int[] {
            0xffA3B18A,
            0xffC0CAAD,
            0xff856A5D,
            0xff294936,
            });
    
    public static final ColorSet SEASIDE = new ColorSet(new int[] {
            0xff8EE3EF,
            0xffFFF1D0,
            0xff9BC4CB,
            0xff23395B,
            });
    
    public static final ColorSet CITY = new ColorSet(new int[] {
            0xff9CAFB7,
            0xffCCDAD1,
            0xffF6F8FF,
            0xff493843,
            });
    
    public static final ColorSet WONDERLAND = new ColorSet(new int[] {
            0xffF9E6AA,
            0xffFF6400,
            0xff81009A,
            0xff0051A6
            });
    
    public static final ColorSet MOUNTAIN = new ColorSet(new int[] {
            0xffE5E6E4,
            0xffA6A2A2,
            0xffA89F68,
            0xff5D576B
            });
    
    public static final ColorSet PASTEL = new ColorSet(new int[] {
            0xffF8DEE9,
            0xff888888,
            0xffD6F1F8,
            0xff000000
            });
    
    public static final ColorSet INVERTED  = new ColorSet(new int[] {
            0xff000000,
            0xffa9a9a9,
            0xffd3d3d3,
            0xffffffff
    });
    
    /* randomize using ColorSet.randomize() !!! */
    public static ColorSet Random = new ColorSet(Math.random(), Math.random(), Math.random());

    
//    public static final ColorSet EMPTY = new ColorSet(new int[] {
//            0xff,
//            0xff,
//            0xff,
//            0xff});
}
