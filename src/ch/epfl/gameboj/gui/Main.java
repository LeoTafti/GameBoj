/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;


import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
    
//   "roms/Tetris.gb"
//   "roms/2048.gb"
//   "roms/snake.gb"
//   "roms/tasmaniaStory.gb"
//   "roms/flappyboy.gb"
//   "roms/DonkeyKong.gb"
//   "roms/Bomberman.gb"
//   "roms/SuperMarioLand.gb"
//   "roms/SuperMarioLand2.gb"
//   "roms/LegendofZelda,TheLink'sAwakening.gb"
    
    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        //construct gameboy with given ROM
        if (getParameters().getRaw().size() != 1)
            System.exit(1);
        
        File romFile = new File(getParameters().getRaw().get(0));
        GameBoy gameboj = new GameBoy(Cartridge.ofFile(romFile));
        
        
        // layout
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefHeight(LcdController.LCD_HEIGHT*2);        
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(LcdController.LCD_WIDTH*2);
        imageView.setFitHeight(LcdController.LCD_HEIGHT*2);
        imageView.setPreserveRatio(true);
        imageView.setImage(ImageConverter.convert(gameboj.lcdController().currentImage()));
        
        mainPane.setCenter(imageView);       
        
        // keyboard interaction
        Map<String, Joypad.Key> buttonMap = new HashMap<>(Map.of(
            "a", Joypad.Key.A,
            "b", Joypad.Key.B,
            "s", Joypad.Key.START,
            " ", Joypad.Key.SELECT));
        Map<KeyCode, Joypad.Key> joystickMap = new HashMap<>(Map.of(
            KeyCode.UP, Joypad.Key.UP,
            KeyCode.DOWN, Joypad.Key.DOWN,
            KeyCode.LEFT, Joypad.Key.LEFT,
            KeyCode.RIGHT, Joypad.Key.RIGHT));
        
        imageView.setOnKeyPressed(e -> {
            Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                    joystickMap.get(e.getCode()));
            if (p != null)
                gameboj.joypad().keyPressed(p);
        });

        imageView.setOnKeyReleased(e -> {
            Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                    joystickMap.get(e.getCode()));
            if (p != null)
                gameboj.joypad().keyReleased(p);           
        });
            
        // gameboj simulation
        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - start; //in nanosec
                long gameboyCycles = (long) (elapsed * GameBoy.CYCLES_PER_NANOSEC);
                System.out.println("e" + elapsed);
                System.out.println("c" + gameboyCycles);
                gameboj.runUntil(gameboyCycles);
                imageView.setImage(ImageConverter
                        .convert(gameboj.lcdController().currentImage()));
            };
        };
        timer.start();        
        
        
        Scene scene = new Scene(mainPane);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("GAMEBOJ");
        primaryStage.show();
        imageView.requestFocus();
    }

}
