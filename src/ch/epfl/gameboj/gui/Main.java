/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.util.List;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class Main extends Application{

    private static final float SIM_SPEED = 4f;
    
    //TODO : remove all of this
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
        //TODO : uncomment
//        if (getParameters().getRaw().size() != 1)
//            System.exit(1);
        
        //construct gameboy with given ROM
        File romFile = new File(ROM_PATH);
        GameBoy gameboj = new GameBoy(Cartridge.ofFile(romFile));
        
        
        //++++++++++++++++++++++++++++++++++ LAYOUT +++++++++++++++++++++++++++
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefHeight(LcdController.LCD_HEIGHT*2);
        
        //----------------------------- LEFT --------------------------------
        SplitPane menuPane = new SplitPane();
        menuPane.setMinWidth(80);
        mainPane.setLeft(menuPane);
        
        //------------------------------ CENTER -----------------------------
        
        Pane backgroundPane = new Pane();
//        backgroundPane.setBlendMode(BlendMode.GREEN);
        
        
        ImageView lcdPane = new ImageView();
        //TODO : we don't need it, but prof says to use it
        lcdPane.setFitWidth(LcdController.LCD_WIDTH);
        lcdPane.setFitHeight(LcdController.LCD_HEIGHT);
        lcdPane.setPreserveRatio(true);
        lcdPane.setImage(ImageConverter.convert(gameboj.lcdController().currentImage()));
        
        Pane joyPane = new Pane();
        joyPane.maxHeightProperty().bind(lcdPane.fitHeightProperty());
        joyPane.minHeightProperty().bind(lcdPane.fitHeightProperty());
        joyPane.maxWidthProperty().bind(lcdPane.fitWidthProperty());
        joyPane.minWidthProperty().bind(lcdPane.fitWidthProperty());
        
        int shortS = 15, longS = 25, radius = 15, middleMargin = 30, topMargin = 20, interSpace = 10;
        
        Rectangle up = new Rectangle( interSpace*2+longS, topMargin, shortS, longS),
            down     = new Rectangle( interSpace*2+longS, topMargin + interSpace*2 + longS + shortS, shortS, longS),
            left     = new Rectangle( interSpace, topMargin + interSpace + longS, longS, shortS),
            right    = new Rectangle( interSpace*3 + longS + shortS, topMargin + interSpace + longS, longS, shortS);
        
        Circle a   = new Circle(interSpace*2 + longS*2 + shortS + middleMargin + radius + interSpace , topMargin*2 + radius , radius),
            b      = new Circle(interSpace*2 + longS*2 + shortS + middleMargin , topMargin*2 + interSpace + shortS + longS, radius),
            select = new Circle(interSpace*4 + longS + shortS, topMargin + interSpace*2 + longS*3 + shortS, radius/2),
            start  = new Circle(interSpace*3 + longS + shortS + middleMargin, topMargin + interSpace*2 + longS*3 + shortS, radius/2);
        
        List<Shape> joyShapes = List.of(up, down, left, right, a, b, start,
                select);
        joyShapes.forEach(s -> s.setFill(Color.DARKSLATEBLUE));
        
        joyPane.getChildren().addAll(joyShapes);
        
        backgroundPane.getChildren().addAll(lcdPane, joyPane);
        joyPane.translateYProperty().bind(lcdPane.fitHeightProperty());
        
        
        lcdPane.fitWidthProperty().bind(backgroundPane.widthProperty());
        lcdPane.fitHeightProperty().bind(lcdPane.fitWidthProperty().multiply(LcdController.LCD_HEIGHT/(double)LcdController.LCD_WIDTH));
        joyPane.minWidthProperty().bind(lcdPane.fitWidthProperty());
        joyPane.maxWidthProperty().bind(lcdPane.fitWidthProperty());
        joyPane.minHeightProperty().bind(lcdPane.fitHeightProperty());
        joyPane.maxHeightProperty().bind(lcdPane.fitHeightProperty());
        
//        lcdPane.fitHeightProperty().bind(lcdPane.fitWidthProperty().multiply();
        //Bind divider to lcdHeight
//        centerPane.getDividers().get(0).positionProperty().bind(backgroundPane.heightProperty().divide(2)); 
//        centerPane.heightProperty().bind(lcdPane.fitHeightProperty().multiply(2));
        
//        menuPane.heightProperty().bind(mainPane.)
        
        mainPane.setCenter(backgroundPane);
        
                
        //TODO should map from KeyEvent to Joypad.Key 
        //but couldnt implement distinction between keyEvent.getCode() and keyEvent.getText();
        //juliens made 2 maps and used 
        
        //++++++++++++++++++++++++++++++++++++++++++ FUNCTIONNEMENT +++++++++++++++++++++++++++++++++++++
        
        
        // ----------------------------------------- keyboard interaction -------------------------------
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
        
        Map<String, Shape> joyButtonMap = new HashMap<>(Map.of(
                "a", a,
                "b", b,
                "s", start,
                " ", select));
        Map<KeyCode, Shape> joyArrowMap = new HashMap<>(Map.of(
                KeyCode.UP, up,
                KeyCode.DOWN, down,
                KeyCode.LEFT, left,
                KeyCode.RIGHT, right));
        
        backgroundPane.setOnKeyPressed(e -> {
        
            Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                    joystickMap.get(e.getCode()));
            if (p != null)
                gameboj.joypad().keyPressed(p);

            Shape s = joyButtonMap.getOrDefault(e.getText(),
                    joyArrowMap.get(e.getCode()));
            if (s != null)
                s.setFill(Color.DEEPPINK);

        });

        
        backgroundPane.setOnKeyReleased(e -> {    
        
            Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                    joystickMap.get(e.getCode()));
            if (p != null)
                gameboj.joypad().keyReleased(p);
            
            Shape s = joyButtonMap.getOrDefault(e.getText(),
                    joyArrowMap.get(e.getCode()));
            if (s != null)
                s.setFill(Color.DARKSLATEBLUE);
            
        });
            

        
        // ------------------------------------------------------ gameboj simulation ---------------------------------
        long beginning = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - beginning; //in nanosec
                long gameboyCycles = (long) (elapsed * GameBoy.CYCLES_PER_NANOSEC * SIM_SPEED);
                gameboj.runUntil(gameboyCycles);
                lcdPane.setImage(ImageConverter
                        .convert(gameboj.lcdController().currentImage()));
            };
        };
        timer.start();        
        
        Scene scene = new Scene(mainPane);
        
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("gameboj");
        primaryStage.show();
        backgroundPane.requestFocus();
    }

}
