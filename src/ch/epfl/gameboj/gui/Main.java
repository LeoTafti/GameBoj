/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{

    private static final float SIM_SPEED = 1f;
    
    private static final String[] ROM_PATHS = { 
            "roms/Tetris.gb", //0
            "roms/2048.gb", //1
            "roms/snake.gb", //2
            "roms/tasmaniaStory.gb", //3
            "roms/flappyboy.gb", //4
            "roms/DonkeyKong.gb", //5
            "roms/Bomberman.gb", //6
            "roms/SuperMarioLand.gb", //7
            "roms/SuperMarioLand2.gb", //8
            "roms/LegendofZelda,TheLink'sAwakening.gb", //9
            "roms/PokemonRedBlue.gb"}; //10
    private static final String ROM_PATH = ROM_PATHS[9];
//    private static final String ROM_PATH = "/Users/Leo/git/GameBoj/roms/SuperMarioLand2.gb";

//    private static final String ROM_PATH = "/Users/Leo/git/GameBoj/roms/Bomberman.gb";

//    private static final String ROM_PATH = "/Users/Leo/git/GameBoj/roms/Tetris.gb";
    
    
    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //check a single arg is given or terminate
//        if (getParameters().getRaw().size() != 1)
//            System.exit(1);
        
        //construct gameboy with given ROM
        File romFile = new File(ROM_PATH);
        GameBoy gameboj = new GameBoy(Cartridge.ofFile(romFile));
        
        
        //create graphical interface
        ImageView imageView = new ImageView();
        //TODO : we don't need it, but prof says to use it
//        imageView.setFitWidth(LcdController.LCD_WIDTH);
//        imageView.setFitHeight(LcdController.LCD_HEIGHT);
        imageView.setImage(ImageConverter.convert(
                gameboj.lcdController().currentImage()));
        
        BorderPane mainPane = new BorderPane();
        
        //------------------------------ CENTER -----------------------------
        mainPane.setCenter(imageView);
        //TODO should map from KeyEvent to Joypad.Key 
        //but couldnt implement distinction between keyEvent.getCode() and keyEvent.getText();
        // juliens made 2 maps and used 
        
        //TODO add keyreleased!!
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
        
        
        //TODO use private method?
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
        
        //----------------------------- LEFT --------------------------------
//        SplitPane menuPane = new SplitPane();
//        menuPane.setMaxWidth(40);
//        menuPane.setMinWidth(50);
//        mainPane.setLeft(menuPane);
        
        
        
        
        
        
        //Redimensionning
        imageView.fitWidthProperty().bind(mainPane.widthProperty());
        imageView.fitHeightProperty().bind(mainPane.heightProperty());
        Scene scene = new Scene(mainPane);
        
        // simulate gameboj
        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - start; //in nanosec
                long gameboyCycles = (long) (elapsed * GameBoy.CYCLES_PER_NANOSEC * SIM_SPEED);
                gameboj.runUntil(gameboyCycles);
                imageView.setImage(ImageConverter
                        .convert(gameboj.lcdController().currentImage()));
            };
        };
        timer.start();
            //update periodically
                //image
                //key press
        
        primaryStage.setWidth(LcdController.LCD_WIDTH * 2);
        primaryStage.setHeight(LcdController.LCD_HEIGHT * 2);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.minWidthProperty().bind(scene.heightProperty());
        primaryStage.minHeightProperty().bind(scene.widthProperty());
        primaryStage.setTitle("gameboj");
        primaryStage.show();
        imageView.requestFocus();
        
    }

}
