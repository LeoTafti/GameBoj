/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    
    private static ColorSet colorSet = ColorSet.GAMEBOY;
    
    /**
     * Creates a JavaFX image from given LcdImage
     * @param lcdImage lcdImage to convert
     * @return corresponding JavaFX image
     */
    public static Image convert(LcdImage lcdImage) {
        
        WritableImage fxImage = new WritableImage(LcdController.LCD_WIDTH,
                          LcdController.LCD_HEIGHT);
        PixelWriter pixW = fxImage.getPixelWriter();
        
        for (int y = 0; y < LcdController.LCD_HEIGHT; y++)
            for (int x = 0; x < LcdController.LCD_WIDTH; x++)
                pixW.setArgb(x, y, colorSet.get(lcdImage.get(x, y)));
        return fxImage;
    }
    
    /**
     * sets the image converter colors to a new color palette
     * @param palette a new or referenced color set
     */
    public static void setColorSet(ColorSet palette) {
        colorSet = palette;
    }
    
    /**
     * Creates a new color set and assigns it to this image converter based on the given rgb ratios
     * @param r red ratio
     * @param g green ratio
     * @param b blue ratio
     * 
     * @throws IllegalArgumentException if any of the ratios is not between 0 and 1 (included)
     */
    public static void setCustomColors(double r, double g, double b) {
        Preconditions.checkArgument( 0 <= r && r <= 1);
        Preconditions.checkArgument( 0 <= g && g <= 1);
        Preconditions.checkArgument( 0 <= b && b <= 1);
        
        colorSet = new ColorSet(r, g, b);
    }
}
