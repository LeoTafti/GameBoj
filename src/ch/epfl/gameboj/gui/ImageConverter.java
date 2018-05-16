/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    //Hex code of colors associated to gameboy's colors, indexed by encoding
    private static final int[] argbColors = {0xff_ff_ff_ff,
                                      0xff_d3_d3_d3,
                                      0xff_a9_a9_a9,
                                      0xff_00_00_00};
    
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
                pixW.setArgb(x, y, argbColors[lcdImage.get(x,  y)]);
        
        return fxImage;
    }
}
