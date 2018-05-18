/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class SliderLabel extends Label{
//TODO add documentation
    private final Slider slider;
    private String text;
    
    public SliderLabel(Slider valueSource) {
        this.slider = valueSource;
        update();
    }
    
    public void update() {
        text = String.format("%1.3f", slider.getValue());
    }

    public void append(String s) {
        text += s;
    }
}
