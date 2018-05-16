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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class MainBonus extends Application{

    private static double SIM_SPEED = 2f;
    
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
            };
    private static final String ROM_PATH = ROM_PATHS[0];    
    
    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        //construct gameboy with given ROM
        File romFile = new File(ROM_PATH);
        GameBoy gameboj = new GameBoy(Cartridge.ofFile(romFile));
        
        
        //++++++++++++++++++++++++++++++++++ LAYOUT +++++++++++++++++++++++++++
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefHeight(LcdController.LCD_HEIGHT*2);
        
        //----------------------------- LEFT --------------------------------
        VBox menuPane = new VBox();
        menuPane.setMinWidth(80);
        mainPane.setLeft(menuPane);
        
        Slider speedSlider = new Slider(0.2, 4.0, 1);
        
        
        MenuBar speedBar = new MenuBar();
        Menu speedMenu = new Menu();
        ToggleGroup speedSwitch = new ToggleGroup();
        RadioButton half       = new RadioButton("0.5x");
        RadioButton normal     = new RadioButton("1x");
        RadioButton one_half   = new RadioButton("1.5x");
        RadioButton twice      = new RadioButton("2x");
        RadioButton thrice     = new RadioButton("3x");
        RadioButton ultraSpeed = new RadioButton("4x");
        List<RadioButton> speeds = List.of(
                half,
                normal,
                one_half,
                twice,
                thrice,
                ultraSpeed);
        speeds.forEach(s -> s.setToggleGroup(speedSwitch));
        
        ToggleButton oneOrTwo = new ToggleButton("speed");
        oneOrTwo.setSelected(false);
        oneOrTwo.setOnSwipeLeft(e -> SIM_SPEED *= 2);
        
        
        menuPane.getChildren().addAll(speeds);
        menuPane.getChildren().add(speedSlider);
        menuPane.getChildren().add(oneOrTwo);
        
        
        //------------------------------ CENTER -----------------------------
        
        VBox backgroundPane = new VBox();

        ImageView lcdPane = new ImageView();
        //TODO : we don't need it, but prof says to use it
        lcdPane.setFitWidth(LcdController.LCD_WIDTH*2);
        lcdPane.setFitHeight(LcdController.LCD_HEIGHT*2);
        lcdPane.setPreserveRatio(true);
        lcdPane.setImage(ImageConverter.convert(gameboj.lcdController().currentImage()));
        
        Pane joyPane = new Pane();
        
        int shortS = 15, longS = 25, radius = 15, middleMargin = 30, topMargin = 20, interSpace = 10;
        
        Rectangle up = new Rectangle( interSpace*2+longS, topMargin, shortS, longS),
            down     = new Rectangle( interSpace*2+longS, topMargin + interSpace*2 + longS + shortS, shortS, longS),
            left     = new Rectangle( interSpace, topMargin + interSpace + longS, longS, shortS),
            right    = new Rectangle( interSpace*3 + longS + shortS, topMargin + interSpace + longS, longS, shortS);
        
        Circle a   = new Circle(interSpace*2 + longS*2 + shortS + middleMargin + radius + interSpace , topMargin*2 + radius , radius),
            b      = new Circle(interSpace*2 + longS*2 + shortS + middleMargin , topMargin*2 + interSpace + shortS + longS, radius),
            select = new Circle(interSpace*4 + longS + shortS, topMargin + interSpace*2 + longS*3 + shortS, radius/2),
            start  = new Circle(interSpace*3 + longS + shortS + middleMargin, topMargin + interSpace*2 + longS*3 + shortS, radius/2);
        
        List<Shape> joyShapes = List.of(up, down, left, right, a, b, start, select);
        
        joyShapes.forEach(s -> {
            s.setFill(Color.DARKSLATEBLUE);
            s.setLayoutY(LcdController.LCD_HEIGHT/2.0);
        });
            
        joyPane.getChildren().addAll(joyShapes);
        
        
        backgroundPane.getChildren().addAll(lcdPane, joyPane);
        backgroundPane.setMinHeight(LcdController.LCD_HEIGHT*4);
        
        joyPane.translateXProperty().bind(lcdPane.fitWidthProperty().subtract(LcdController.LCD_WIDTH).divide(2));
        //responsiveness attempt
//        double lcdRatio = (LcdController.LCD_HEIGHT/(double)LcdController.LCD_WIDTH);
//        lcdPane.fitWidthProperty().bind(backgroundPane.widthProperty());
//        lcdPane.fitHeightProperty().bind(lcdPane.fitWidthProperty().multiply(lcdRatio));
        
//        joyPane.minWidthProperty().bind(lcdPane.fitWidthProperty());
//        joyPane.maxWidthProperty().bind(lcdPane.fitWidthProperty());
//        joyPane.minHeightProperty().bind(lcdPane.fitHeightProperty());
//        joyPane.maxHeightProperty().bind(lcdPane.fitHeightProperty());
        
//        joyPane.translateYProperty().bind(lcdPane.fitHe);
//        joyPane.scaleXProperty().bind(lcdPane.fitWidthProperty().divide(LcdController.LCD_WIDTH));
//        joyPane.scaleYProperty().bind(lcdPane.fitHeightProperty().divide(LcdController.LCD_HEIGHT).multiply(lcdRatio));
//        joyPane.set
        
        mainPane.setCenter(backgroundPane);

        
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
