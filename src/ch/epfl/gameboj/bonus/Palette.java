/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.gameboj.gui.ImageConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class Palette extends VBox{

    private final ColorSet colors;
    private final Button button;
    
    public Palette(ColorSet colors, String name) {
        this.colors = colors;
        
        button = new Button(name);
        button.setAlignment(Pos.CENTER);
        button.setOnAction(e -> ImageConverter.setColorSet(colors));
        button.setPadding(new Insets(2));
        GridPane colorSquares = new GridPane();
        colorSquares.setPadding(new Insets(5));
        colorSquares.setAlignment(Pos.CENTER);
        Rectangle c1 = new Rectangle(20, 20),
                c2 = new Rectangle(20, 20),
                c3 = new Rectangle(20, 20),
                c4 = new Rectangle(20, 20);
        //need to declare all rectangles otherwise 'duplicate children added' error
        List<Rectangle> squares = List.of(c1, c2, c3, c4);
        for (int i = 0; i < squares.size(); i++) {
            squares.get(i).setStyle("-fx-fill : #" + colors.getRgbString(i) + ";");
            colorSquares.add(squares.get(i), i%2, i/2);
        }
        getChildren().addAll(button, colorSquares);
    }
    
    
    
}
